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

import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import java.util.ArrayList;
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
    
    public VideoPlayerController(
            boolean audioEnabled,
            List<FrameProcessor> frameProcessors, // Passed processors from VideoPlayerApp
            FrameSubtitler subtitler,
            BiConsumer<Integer, Integer> videoSizeChangedHandler) {
        this.view = new VideoPlayerView();
        this.subtitler = subtitler;
        // TEMP test for step 10A (remove in 10C)
        if (subtitler != null) {
            List<Subtitle> test = new ArrayList<>();
            test.add(new Subtitle("First test line", 1_000_000, 4_000_000));
            test.add(new Subtitle("Very short line, shown 1 second", 6_000_000, 6_200_000));
            test.add(new Subtitle("Very long line, cut to 7 seconds", 10_000_000, 30_000_000));
            subtitler.addSubtitles(test);
        }
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
    }

    public void shutdown() {
        model.shutdown();
    }

    private void startOver() {
        model.startOver();
    }

    private void backFiveSeconds() {
        model.backFiveSeconds();
    }

    private void togglePause() {
        model.togglePause();
    }

    private void forwardFiveSeconds() {
        model.forwardFiveSeconds();
    }

    private void toggleAudioOutput() {
        model.toggleAudioOutput();
    }
    
    // Turn subtitles on/off 
    private void toggleSubtitles() {
    	if (subtitler == null) {
    		return; // no -S => no toggle
    	}
    	boolean on = !subtitler.isEnabled();
    	subtitler.setEnabled(on);
    	view.showSubtitlesEnabled(on);
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
        view.setStatus(status);
    }

    private void onPlaybackStateChanged(boolean playing, boolean paused) {
        if (playing && !paused) {
            view.showPauseAction();
        } else {
            view.showPlayAction();
        }
    }

    private void onAudioOutputStateChanged(boolean audioOutputEnabled) {
        view.showAudioEnabled(audioOutputEnabled);
    }
}
