package comp3011;

/*
 * Adelaide University COMP3011 Assignment 3.
 *
 * Author: Duong Quang Huy a2950659
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import comp3011.assignment1.model.WhisperResponse;
import comp3011.assignment1.model.WhisperSegment;

//cuts the video's audio into 10 second pieces,
//sends each piece to the subtitle server, and gives the lines to FrameSubtitler.
public class SubtitleFetcher {

    private static final String SERVER_URL = "http://localhost:8080/api/v1/subtitles";
    private static final int SAMPLE_RATE = 16000;
    private static final int CHUNK_SECONDS = 10; // length of a piece sent to server

    private final File videoFile;
    private final FrameSubtitler subtitler;
    private final RestClient restClient = RestClient.create();

    private Thread thread;
    private volatile boolean running = false;

    public SubtitleFetcher(File videoFile, FrameSubtitler subtitler) {
        this.videoFile = videoFile;
        this.subtitler = subtitler;
    }

    // Start fetching sub in a separate thread
    public void start() {
        running = true;
        thread = Thread.ofPlatform().daemon(true).start(this::run);
    }

    // Stop fetch
    public void stop() {
        running = false;

        if (thread != null) {
            thread.interrupt();
        }
    }

    // Read audio from the video and split it into chunks
    private void run() {
        try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(videoFile)) {

            grabber.setSampleRate(SAMPLE_RATE);
            grabber.setAudioChannels(1);
            grabber.setSampleMode(FrameGrabber.SampleMode.SHORT);
            grabber.start();

            if (!grabber.hasAudio()) {
                System.out.println("Video has no audio");
                return;
            }

            ByteArrayOutputStream audioData = new ByteArrayOutputStream();

            long sampleCount = 0;
            long chunkStartUs = 0;

            while (running) {
                Frame frame = grabber.grabSamples();

                if (frame == null) {
                    break;
                }

                if (frame.samples == null) {
                    continue;
                }

                // Convert audio samples into bytes
                ShortBuffer samples = (ShortBuffer) frame.samples[0];

                while (samples.hasRemaining()) {
                    short sample = samples.get();

                    audioData.write(sample & 0xFF);
                    audioData.write((sample >> 8) & 0xFF);

                    sampleCount++;
                }

                // Send audio after collecting 10 seconds
                if (sampleCount >= SAMPLE_RATE * CHUNK_SECONDS) {
                    sendPiece(audioData.toByteArray(), chunkStartUs);

                    chunkStartUs += sampleCount * 1_000_000L / SAMPLE_RATE;

                    sampleCount = 0;
                    audioData.reset();
                }
            }

            // Send any remaining audio
            if (running && sampleCount > 0) {
                sendPiece(audioData.toByteArray(), chunkStartUs);
            }

        } catch (Exception e) {
            System.out.println("Subtitle fetcher error: " + e.getMessage());
        }
    }

    // Send audio to the server and get subtitles
    private void sendPiece(byte[] audioData, long chunkStartUs) {
        try {
            byte[] wavData = makeWav(audioData);

            ByteArrayResource audioFile = new ByteArrayResource(wavData) {
                @Override
                public String getFilename() {
                    return "piece.wav";
                }
            };

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("audio", audioFile);

            WhisperResponse response = restClient.post()
                    .uri(SERVER_URL)
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(body)
                    .retrieve()
                    .body(WhisperResponse.class);

            if (response == null || response.segments == null) {
                System.out.println("No subtitles returned");
                return;
            }

            List<Subtitle> subtitles = new ArrayList<>();

            // Convert subtitle times to video timestamps
            for (WhisperSegment segment : response.segments) {
                long startUs = chunkStartUs + (long) (segment.start * 1_000_000);
                long endUs = chunkStartUs + (long) (segment.end * 1_000_000);

                Subtitle subtitle = new Subtitle(
                        segment.text.trim(),
                        startUs,
                        endUs
                );

                subtitles.add(subtitle);
            }

            subtitler.addSubtitles(subtitles);

            System.out.println("Received " + subtitles.size() + " subtitles");

        } catch (Exception e) {
            System.out.println("Failed to fetch subtitles: " + e.getMessage());
        }
    }

    // Convert raw audio data to WAV
    private byte[] makeWav(byte[] audioData) throws IOException {
        AudioFormat format = new AudioFormat(
                SAMPLE_RATE, 16, 1, true, false
        );

        AudioInputStream audioStream = new AudioInputStream(
                new ByteArrayInputStream(audioData),
                format,
                audioData.length / 2
        );

        ByteArrayOutputStream output = new ByteArrayOutputStream();

        AudioSystem.write(audioStream, AudioFileFormat.Type.WAVE, output);

        return output.toByteArray();
    }
}
