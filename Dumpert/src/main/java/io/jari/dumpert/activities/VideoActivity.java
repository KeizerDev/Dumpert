package io.jari.dumpert.activities;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.MediaController;
import android.widget.VideoView;

import io.jari.dumpert.R;

/**
 * JARI.IO
 * Date: 15-1-15
 * Time: 10:27
 */
public class VideoActivity extends BaseActivity {
    private final static String TAG = "DVA";
    private String videoUrl;
    private int videoPos;
    private MediaController mediaController;

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
                switch (visibility) {
                    case View.SYSTEM_UI_FLAG_VISIBLE:
                        mediaController.show();
                        break;
                }
            }
        });

        setContentView(R.layout.activity_video);

        videoUrl = getIntent().getStringExtra("url");
        videoPos = getIntent().getIntExtra("pos", 0);

        start(videoUrl, videoPos);
    }

    @Override
    protected void onPause() {
        super.onPause();

        final VideoView videoView = (VideoView) findViewById(R.id.video);

        findViewById(R.id.loading).setVisibility(View.VISIBLE);
        findViewById(R.id.video).setVisibility(View.GONE);
        findViewById(R.id.video_frame).setAlpha(0f);

        // stopPlayback also invalidates the cache already built.
        // pause is better, since we don't want to view the same part over and over.
        // Especially on a bad internet connection.
        this.videoPos = videoView.getCurrentPosition();
        videoView.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();

        findViewById(R.id.loading).setVisibility(View.GONE);
        findViewById(R.id.video).setVisibility(View.VISIBLE);
        findViewById(R.id.video_frame).setAlpha(1f);

        start(videoUrl, videoPos);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    void setNavVisibility(boolean visible) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            int newVis = View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;

            if (!visible) {
                newVis = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
            }

            getWindow().getDecorView().setSystemUiVisibility(newVis);
        }

    }

    void start(final String url, final int pos) {
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

        // I hate it when the screen goes dark while watching a video.
        videoView.setKeepScreenOn(true);
        videoView.setMediaController(mediaController);
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                Log.d("dumpert.video", "onPrepared");
                findViewById(R.id.loading).setVisibility(View.GONE);

                // check if Dumpert thinks the events in this video really happened
                boolean vvs = mp.getVideoHeight() > mp.getVideoWidth();

                if(vvs) {
                    Log.d(TAG, "VVS");
                    // rotate the activity 90 degrees
                    // because we also want the controls on the bottom
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                }
            }
        });

        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                videoView.seekTo(0);
                videoView.pause();
            }
        });

        videoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                findViewById(R.id.loading).setVisibility(View.GONE);

                final Snackbar snackbar = Snackbar.make(findViewById(R.id.root),
                        R.string.error_video_failed, Snackbar.LENGTH_INDEFINITE);

                snackbar.setAction(R.string.error_reload, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.v(TAG, "reloading activity");

                        VideoActivity reload = VideoActivity.this;
                        Intent reloadIntent = reload.getIntent();

                        reloadIntent.putExtra("url", url);
                        reloadIntent.putExtra("pos", pos);
                        reload.finish();
                        startActivity(reloadIntent);
                        reload.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    }
                });

                snackbar.show();

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

        Log.d(TAG, "Starting fullscreen video " + url + " at " + Integer.toString(pos) + "ms");

        activity.startActivity(intent);
    }
}
