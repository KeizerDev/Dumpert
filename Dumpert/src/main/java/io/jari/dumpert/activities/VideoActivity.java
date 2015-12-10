package io.jari.dumpert.activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.MediaController;
import android.widget.VideoView;
import com.nispok.snackbar.Snackbar;
import io.jari.dumpert.R;

/**
 * JARI.IO
 * Date: 15-1-15
 * Time: 10:27
 */
public class VideoActivity extends BaseActivity {
    static String TAG = "DVA";

    void setTheme() {
        //no themes used in this activity
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View decorView = getWindow().getDecorView();
        decorView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int visibility) {
                switch(visibility) {
                    case View.SYSTEM_UI_FLAG_VISIBLE:
                        mediaController.show();
                        break;
                }
            }
        });

        setContentView(R.layout.video);

        String url = getIntent().getStringExtra("url");
        int pos = getIntent().getIntExtra("pos", 0);

        start(url, pos);
    }

    void setNavVisibility(boolean visible) {
        int newVis = View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
        if (!visible) {
            newVis = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        }

        getWindow().getDecorView().setSystemUiVisibility(newVis);
    }

    MediaController mediaController;

    void start(String url, int pos) {
        final View videoViewFrame = findViewById(R.id.video_frame);
        final VideoView videoView = (VideoView) findViewById(R.id.video);

        videoView.setVideoURI(Uri.parse(url));

        mediaController = new MediaController(this) {
            @Override
            public void hide() {
                super.hide();
                setNavVisibility(false);
            }

            @Override
            public boolean dispatchKeyEvent(KeyEvent event) {
                //mediacontroller tries some funny stuff here, so use SUPER HACKY METHODS! yay!
                if(event.getKeyCode() == KeyEvent.KEYCODE_BACK)
                    onBackPressed();
                return false;
            }
        };

        mediaController.setAnchorView(videoViewFrame);

        videoView.setMediaController(mediaController);

        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                Log.d("dumpert.video", "onPrepared");
                findViewById(R.id.loading).setVisibility(View.GONE);
            }
        });

        videoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                findViewById(R.id.loading).setVisibility(View.GONE);

                Snackbar.with(VideoActivity.this)
                        .text(R.string.video_failed)
                        .textColor(Color.parseColor("#FFCDD2"))
                        .show(VideoActivity.this);

                return true;
            }
        });

        setNavVisibility(false);
        videoView.start();
        videoView.seekTo(pos);
    }

    public static void launch(Activity activity, String url, int pos) {
        Intent intent = new Intent(activity, VideoActivity.class);
        intent.putExtra("url", url);
        intent.putExtra("pos", pos);

        Log.d(TAG, "Starting fullscreen video "+url+" at "+Integer.toString(pos)+"ms");

        activity.startActivity(intent);
    }
}
