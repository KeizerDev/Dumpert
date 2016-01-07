package io.jari.dumpert.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.MediaController;
import android.widget.VideoView;
import com.nispok.snackbar.Snackbar;
import com.nispok.snackbar.listeners.ActionClickListener;

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
                switch (visibility) {
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

                Snackbar.with(VideoActivity.this)
                        .text(R.string.video_failed)
                        .textColor(Color.parseColor("#FFCDD2"))
                        .actionLabel(R.string.reload)
                        .actionListener(new ActionClickListener() {
                            @Override
                            public void onActionClicked(Snackbar snackbar) {
                                Log.v(TAG, "reloading activity");

                                VideoActivity reload = VideoActivity.this;
                                Intent reloadIntent = reload.getIntent();

                                Log.d(TAG, "reloading "+reload.getLocalClassName()
                                                + "\n" + "  reloadIntent: " + reloadIntent.toString()
                                                + "\n" + "  url:          " + url
                                                + "\n" + "  pos:          " + Integer.toString(pos));

                                reloadIntent.putExtra("url", url);
                                reloadIntent.putExtra("pos", pos);
                                reload.finish();
                                startActivity(reloadIntent);
                                reload.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                            }
                        })
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

        Log.d(TAG, "Starting fullscreen video " + url + " at " + Integer.toString(pos) + "ms");

        activity.startActivity(intent);
    }
}
