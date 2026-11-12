/*
 * Starter code supplied for Adelaide University COMP3011 Assignment 2.
 * Students are free to modify this file for assessment purposes.
 * 
 * Authors:
 *   1. Simon Ratcliffe, in collaboration with GPT-5.6 Terra
 *   2. 2. Duong Quang Huy a2950659
 *
 * Copyright 2026 Simon Ratcliffe
 */
package comp3011;

import java.io.File;
import java.util.List;
import java.util.function.BiConsumer;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
/**
 * The controller part of the video player model-view-controller architecture.
 * 
 * <p>
 * Wires up events from the {@link VideoPlayerView} to the {@link VideoPlayerModel}.
 * This includes button actions, keyboard shortcuts and display resizing.
 * </p>
 */
public class VideoPlayerController {
    private final VideoPlayerModel model;
    private final VideoPlayerView view;
    private final BiConsumer<Integer, Integer> videoSizeChangedHandler;
    private final FrameSubtitler subtitler; // null when -S not given
    private SubtitleFetcher fetcher; // background thread that gets subtitles from the server
    private File currentFile;
    
    public VideoPlayerController(
            boolean audioEnabled,
            List<FrameProcessor> frameProcessors, // Passed processors from VideoPlayerApp
            FrameSubtitler subtitler,
            BiConsumer<Integer, Integer> videoSizeChangedHandler) {
        this.view = new VideoPlayerView();
        this.subtitler = subtitler;
        // TEMP test for step 10A (remove in 10C)
//        if (subtitler != null) {
//            List<Subtitle> test = new ArrayList<>();
//            test.add(new Subtitle("First test line", 1_000_000, 4_000_000));
//            test.add(new Subtitle("Very short line, shown 1 second", 6_000_000, 6_200_000));
//            test.add(new Subtitle("Very long line, cut to 7 seconds", 10_000_000, 30_000_000));
//            subtitler.addSubtitles(test);
//        }
        this.videoSizeChangedHandler = videoSizeChangedHandler;
        model = new VideoPlayerModel(
                audioEnabled,
                frameProcessors,
                this::onVideoSizeChanged,
                this::onFrameReady,
                this::onStatusChanged,
                this::onPlaybackStateChanged,
                this::onAudioOutputStateChanged);

        view.getStartOverButton().setOnAction(_ -> startOver());
        view.getBackFiveButton().setOnAction(_ -> backFiveSeconds());
        view.getPausePlayButton().setOnAction(_ -> togglePause());
        view.getForwardFiveButton().setOnAction(_ -> forwardFiveSeconds());
        view.getStopButton().setOnAction(_ -> stopAndExit());
        view.getAudioButton().setOnAction(_ -> toggleAudioOutput());
        view.getSubtitleButton().setOnAction(_ -> toggleSubtitles());
        view.showAudioEnabled(model.isAudioOutputEnabled());
    }

    public void installKeyboardShortcuts(Scene scene) {
        scene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                stopAndExit();
                event.consume();
            } else if (event.getCode() == KeyCode.SPACE) {
                togglePause();
                event.consume();
            } else if (event.getCode() == KeyCode.LEFT) {
                backFiveSeconds();
                event.consume();
            } else if (event.getCode() == KeyCode.RIGHT) {
                forwardFiveSeconds();
                event.consume();
            } else if (event.getCode() == KeyCode.HOME) {
                startOver();
                event.consume();
            } else if (event.getCode() == KeyCode.A) {
                toggleAudioOutput();
                event.consume();
            }else if(event.getCode() == KeyCode.S) { // S key = CC button
            	toggleSubtitles();
            	event.consume();
            }
        });
    }

    public VideoPlayerView getView() {
        return view;
    }

    public void play(File file) {
        view.setPlaybackControlsDisabled(false);
        model.play(file);
        currentFile = file;

        view.showSubtitlesEnabled(subtitler.isEnabled()); // button faded if off
        if (subtitler.isEnabled()) {
            startFetcher(); // -S given: start getting subtitles now
        }
    }
    
 // Start (or restart) the background thread that gets subtitles
    private void startFetcher() {
        if (fetcher != null) {
            fetcher.stop();
        }
        fetcher = new SubtitleFetcher(currentFile, subtitler);
        fetcher.start();
        fetcher.seekTo(model.getCurrentTimestampUs()); // begin where the video is now
    }

    public void shutdown() {
    	if(fetcher != null) {
    		fetcher.stop(); // stop old fetcher if there was one
    	}
        model.shutdown();
    }

    private void startOver() {
        model.startOver();
        moveFetcher();
    }

    private void backFiveSeconds() {
        model.backFiveSeconds();
        moveFetcher();
    }

    private void togglePause() {
        model.togglePause();
    }

    private void forwardFiveSeconds() {
        model.forwardFiveSeconds();
        moveFetcher();
    }
    
    // A3: after a seek, tell the subtitle thread where is the vids now
    private void moveFetcher() {
    	if(fetcher!=null) {
    		fetcher.seekTo(model.getCurrentTimestampUs());
    	}
    }
    
    
    private void toggleAudioOutput() {
        model.toggleAudioOutput();
    }
    
    // Turn subtitles on/off 
    private void toggleSubtitles() {
        boolean nowOn = !subtitler.isEnabled();
        subtitler.setEnabled(nowOn);
        view.showSubtitlesEnabled(nowOn);

        // First time subtitles are turned on (started without -S): start getting them
        if (nowOn && fetcher == null && currentFile != null) {
            startFetcher();
        }
    }

    private void stopAndExit() {
        model.stopPlayback();
        javafx.application.Platform.exit();
    }

    private void onVideoSizeChanged(int width, int height) {
        videoSizeChangedHandler.accept(width, height);
    }

    private void onFrameReady(Image image) {
        view.setImage(image);
    }

    private void onStatusChanged(String status) {
        // May be called from the decode thread (end of video, USB removed):
        // hand the UI change to the FX thread
        Platform.runLater(() -> view.setStatus(status));
    }

    private void onPlaybackStateChanged(boolean playing, boolean paused) {
        // May be called from the decode thread: hand the UI change to the FX thread
        Platform.runLater(() -> {
            if (playing && !paused) {
                view.showPauseAction();
            } else {
                view.showPlayAction();
            }
        });
    }

    private void onAudioOutputStateChanged(boolean audioOutputEnabled) {
        view.showAudioEnabled(audioOutputEnabled);
    }
}
