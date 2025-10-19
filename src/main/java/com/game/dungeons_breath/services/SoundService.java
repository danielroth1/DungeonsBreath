package com.game.dungeons_breath.services;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

import java.util.HashMap;
import java.util.Map;

import com.game.dungeons_breath.GameViewController;

public class SoundService {

    private HashMap<String, Media> mediaMap;
    private MediaPlayer backgroundMusicPlayer;
    private HashMap<Media, MediaPlayer> mediaPlayerMap;

    public SoundService() {
        mediaMap = new HashMap<String, Media>();
        mediaPlayerMap = new HashMap<Media, MediaPlayer>();

        // // background music
        //// Media media = getMedia(backgroundMusicPath);
        // backgroundMusicPlayer = new MediaPlayer(media);
        // // endless loop
        // backgroundMusicPlayer.setOnEndOfMedia(new Runnable() {
        // @Override
        // public void run() {
        // backgroundMusicPlayer.seek(Duration.ZERO); // Set audio to beginning.
        // backgroundMusicPlayer.play();
        // }
        // });
    }

    public void dispose() {
        for (Map.Entry<Media, MediaPlayer> m : mediaPlayerMap.entrySet()) {
            m.getValue().dispose();
        }
        mediaPlayerMap = null;
    }

    private Media getMedia(String path) {
        Media media = mediaMap.getOrDefault(path, null);
        if (media == null) {
            media = new Media(GameViewController.class.getResource(path).toString());
            mediaMap.put(path, media);
        }
        return media;
    }

    private MediaPlayer getMediaPlayer(Media media) {
        return getMediaPlayer(media, false);
    }

    private MediaPlayer getMediaPlayer(Media media, boolean endless) {
        MediaPlayer mediaPlayer = mediaPlayerMap.getOrDefault(media, null);
        if (mediaPlayer == null) {
            mediaPlayer = new MediaPlayer(media);
            if (endless) {
                // endless loop
                MediaPlayer finalMediaPlayer = mediaPlayer;
                mediaPlayer.setOnEndOfMedia(new Runnable() {
                    @Override
                    public void run() {
                        finalMediaPlayer.seek(Duration.ZERO); // Set audio to beginning.
                        finalMediaPlayer.play();
                    }
                });
            }
            mediaPlayerMap.put(media, mediaPlayer);
        }
        return mediaPlayer;
    }

    public void playSound(String soundPath) {
        playSound(soundPath, 1.0);
    }

    /**
     * Play given sound with the given volume.
     * 
     * @param soundPath - path to the sound file in the resource directory, e.g.
     *                  sounds/<sound-file>.wav
     * @param volume    - value between 0 and 1 where 1 is the originals sound
     *                  volume and 0 is completely quiet.
     */
    public void playSound(String soundPath, double volume) {
        Media media = getMedia(soundPath);
        MediaPlayer mediaPlayer = new MediaPlayer(media);
        mediaPlayer.setVolume(volume);
        mediaPlayer.play();
    }

    public void playContinousSound(String soundPath, boolean fromBeginning, boolean endless) {
        playContinousSound(soundPath, fromBeginning, endless, 1.0);
    }

    public void playContinousSound(String soundPath, boolean fromBeginning, boolean endless, double volume) {
        Media media = getMedia(soundPath);
        MediaPlayer mediaPlayer = getMediaPlayer(media, endless);
        mediaPlayer.setVolume(volume);
        if (fromBeginning)
            mediaPlayer.seek(Duration.ZERO); // Set audio to beginning.
        mediaPlayer.play();
    }

    public void pauseContinousSound(String soundPath, boolean endless) {
        Media media = getMedia(soundPath);
        MediaPlayer mediaPlayer = getMediaPlayer(media, endless);
        mediaPlayer.pause();
    }

    // public void playBackgroundMusic(boolean fromBeginning) {
    // playContinousSound()
    // if (backgroundMusicPlayer == null)
    // return;
    // if (fromBeginning)
    // backgroundMusicPlayer.seek(Duration.ZERO); // Set audio to beginning.
    // backgroundMusicPlayer.play();
    // }
    //
    // public void pauseBackgroundMusic() {
    // if (backgroundMusicPlayer == null)
    // return;
    // backgroundMusicPlayer.pause();
    // }

}