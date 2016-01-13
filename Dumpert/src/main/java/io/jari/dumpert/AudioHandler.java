package io.jari.dumpert;

import android.app.Activity;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.view.KeyEvent;
import android.view.View;
import android.widget.MediaController;

import java.io.IOException;

/**
 * JARI.IO
 * Date: 26-12-14
 * Time: 1:20
 *
 * AudioHandler creates a mediacontroller that controls a audio playback stream.
 */
public class AudioHandler implements MediaPlayer.OnBufferingUpdateListener,
        MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener,
        MediaController.MediaPlayerControl {

    public MediaPlayer mediaPlayer;
    public MediaController controller;
    private View anchor;
    private Activity context;

    public void playAudio(String url, Activity context, View anchor) throws IOException {
        this.anchor = anchor;
        this.context = context;

        mediaPlayer = new MediaPlayer();
        mediaPlayer.setDataSource(url);
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnBufferingUpdateListener(this);
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.prepare();
    }

    public void onPrepared(MediaPlayer mediaplayer) {
        controller = new MediaController(context) {
            @Override
            public void show(int timeout) {
                //we NEVER want to pass anything BUT 0, because controller always has to be visible.
                //so ignore the passed argument, and pass 0
                super.show(0);
            }

            @Override
            public boolean dispatchKeyEvent(KeyEvent event) {
                //mediacontroller tries some funny stuff here, so use SUPER HACKY METHODS! yay!
                if(event.getKeyCode() == KeyEvent.KEYCODE_BACK)
                    context.onBackPressed();
                return false;
            }
        };
        mediaplayer.start();
        controller.setMediaPlayer(this);
        controller.setAnchorView(anchor);
        controller.setEnabled(true);
        controller.show();
    }

    public void start() {
        mediaPlayer.start();
    }

    public void pause() {
        mediaPlayer.pause();
    }

    public int getDuration() {
        return mediaPlayer.getDuration();
    }

    public int getCurrentPosition() {
        return mediaPlayer.getCurrentPosition();
    }

    public void seekTo(int i) {
        mediaPlayer.seekTo(i);
    }

    public boolean isPlaying() {
        return mediaPlayer.isPlaying();
    }

    public int getBufferPercentage() {
        return 0;
    }

    public boolean canPause() {
        return true;
    }

    public boolean canSeekBackward() {
        return true;
    }

    public boolean canSeekForward() {
        return true;
    }

    @Override
    public int getAudioSessionId() {
        return 1;
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {

    }

    @Override
    public void onCompletion(MediaPlayer mp) {

    }

}
