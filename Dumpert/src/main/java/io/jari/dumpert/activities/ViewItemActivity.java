package io.jari.dumpert.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.VideoView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.io.Serializable;
import java.security.InvalidParameterException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.jari.dumpert.AudioHandler;
import io.jari.dumpert.FullscreenMediaController;
import io.jari.dumpert.R;
import io.jari.dumpert.Utils;
import io.jari.dumpert.adapters.CommentsAdapter;
import io.jari.dumpert.animators.SlideInOutBottomItemAnimator;
import io.jari.dumpert.api.API;
import io.jari.dumpert.api.Comment;
import io.jari.dumpert.api.Item;
import io.jari.dumpert.api.ItemInfo;
import io.jari.dumpert.dialogs.ReplyDialog;
import io.jari.dumpert.layouts.NestedSwipeRefreshLayout;

/**
 * JARI.IO
 * Date: 12-12-14
 * Time: 14:50
 */
public class ViewItemActivity extends BaseActivity {
    static String TAG = "DVIA";

    Item item;
    String itemID = null;
    ItemInfo itemInfo;
    TextView votes;
    RecyclerView comments;
    CommentsAdapter commentsAdapter;
    NestedSwipeRefreshLayout swipeRefreshLayout;

    private static SharedPreferences credentials;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewitem);

        credentials = getSharedPreferences("dumpert", 0);
        String session = credentials.getString("session", "");

        item = (Item) getIntent().getSerializableExtra("item");
        final AppCompatImageButton upvote = (AppCompatImageButton) findViewById(R.id.upvote);
        final AppCompatImageButton downvote = (AppCompatImageButton) findViewById(R.id.downvote);
        AppCompatImageButton comment = (AppCompatImageButton) findViewById(R.id.comment);
        votes = (TextView) findViewById(R.id.votes);
        comments = (RecyclerView) findViewById(R.id.comments);

        if(session.equals("")) {
            // not logged in
            comment.setVisibility(View.GONE);
        }

        votes.setText(Integer.toString(item.score));

        Pattern pattern = Pattern.compile("/mediabase/([0-9]*)/([a-z0-9]*)/");
        Matcher matcher = pattern.matcher(item.url);

        if(matcher.find())
            itemID = matcher.group(1) + "." + matcher.group(2);

        final String voteID = itemID.replace(".", "/");

        View.OnClickListener voteListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String link = "http://www.dumpert.nl/";
                int score = 0;

                switch (v.getId()) {
                    case R.id.upvote:
                        link += "rating/" + voteID + "/up";
                        // @todo: listen if the vote is counted on Dumpert.
                        score = Integer.parseInt(votes.getText().toString())+1;
                        upvote.setOnClickListener(null);
                        break;
                    case R.id.downvote:
                        link += "rating/" + voteID + "/down";
                        // @todo: listen if the vote is counted on Dumpert.
                        score = Integer.parseInt(votes.getText().toString())-1;
                        downvote.setOnClickListener(null);
                        break;
                }

                if (itemID != null) {
                    // @fixme: this returns an image of a t-shirt...
                    API.vote(link);
                    votes.setText(Integer.toString(score));
                }
            }
        };

        upvote.setOnClickListener(voteListener);
        downvote.setOnClickListener(voteListener);
        comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ReplyDialog.newInstance(true, item.title, itemID, commentsAdapter.getItem(0).entry).show(getFragmentManager(), "Reply");
            }
        });

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        comments.setHasFixedSize(true);
        comments.setItemAnimator(new SlideInOutBottomItemAnimator(comments));

        // use a linear layout manager
        final LinearLayoutManager commentsLayoutManager = new LinearLayoutManager(this);
        comments.setLayoutManager(commentsLayoutManager);

        ViewCompat.setTransitionName(findViewById(R.id.item_frame), "item");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        if(getSupportActionBar() == null) setSupportActionBar(toolbar);

        if(getSupportActionBar() != null) {
            getSupportActionBar().setTitle(item.title);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        this.loadComments(false);
        this.initHeader();
        this.tip();

        //set up ze refresh
        swipeRefreshLayout = (NestedSwipeRefreshLayout) findViewById(R.id.swiperefresh);
        swipeRefreshLayout.setOnChildScrollUpListener(new NestedSwipeRefreshLayout.OnChildScrollUpListener() {
            @Override
            public boolean canChildScrollUp() {
                return commentsLayoutManager.findFirstVisibleItemPosition() > 0 ||
                        comments.getChildAt(0) == null ||
                        comments.getChildAt(0).getTop() < 0;
            }
        });
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(true);
                commentsAdapter.removeAll();
                loadComments(true);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.itemview, menu);
        return true;
    }

    @Override
    public void onBackPressed() {
        if(item.video) {
            final VideoView videoView = (VideoView) findViewById(R.id.item_video);
            videoView.suspend();
            videoView.setVisibility(View.GONE); //paranoia
            findViewById(R.id.item_video_frame).setVisibility(View.GONE);
            findViewById(R.id.item_frame).setVisibility(View.VISIBLE);
            findViewById(R.id.item_loading).setVisibility(View.GONE);
        } else if(item.audio) {
            findViewById(R.id.item_frame).setVisibility(View.VISIBLE);
            findViewById(R.id.item_video_frame).setAlpha(0f);
            if(audioHandler != null && audioHandler.controller != null)
                audioHandler.controller.hide();
        }

        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            this.onBackPressed();
            return true;
        } else if(id == R.id.nav_share) {
            Intent intent = new Intent(Intent.ACTION_SEND);
            // @todo: update link
            intent.putExtra(Intent.EXTRA_TEXT, this.item.title + " - " + this.item.url
                    + " - gedeeld via Dumpert Reader http://is.gd/jXgC7D");
            intent.setType("text/plain");
            startActivity(intent);
            return true;
        } else if(id == R.id.nav_settings) {
            Intent settings = new Intent(ViewItemActivity.this, PreferencesActivity.class);
            settings.putExtra("activity", "viewItem");
            settings.putExtra("item", getIntent().getSerializableExtra("item"));
            this.startActivity(settings);
            this.finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    int lastVideoPos = 0;

    @Override
    protected void onPause() {
        if(!preferences.getBoolean("autoplay_vids", true)
                || item == null
                || (!item.video && !item.audio)) {
            super.onPause();
            return;
        }
        if(item.video) {
            // videoview starts tripping once activity gets paused.
            // so stop the thing, hide it, show progressbar
            final VideoView videoView = (VideoView) findViewById(R.id.item_video);
            final View videoViewFrame = findViewById(R.id.item_video_frame);
            findViewById(R.id.item_loading).setVisibility(View.VISIBLE);
            findViewById(R.id.item_type).setVisibility(View.GONE);
            findViewById(R.id.item_frame).setVisibility(View.VISIBLE);
            videoViewFrame.setAlpha(0f);
            this.lastVideoPos = videoView.getCurrentPosition();

            // stopPlayback also invalidates the cache already built.
            // pause is better, since we don't want to view the same part over and over.
            // Especially on a bad internet connection.
            videoView.pause();
        } else {
            if(audioHandler != null) audioHandler.pause();
        }

        super.onPause();
    }

    @Override
    protected void onResume() {
        if(preferences.getBoolean("autoplay_vids", true) && itemInfo != null && (item != null)
                && (item.video || item.audio)) {
            if(item.video) {
                //when we return to the activity, restart the video
                // this also happens after lighting the screen. agian and again.
                startMedia(itemInfo, item);
            } else {
                if(audioHandler != null) audioHandler.start();
            }

        }
        super.onResume();
    }

    AudioHandler audioHandler;

    public void startMedia(final ItemInfo itemInfo, final Item item) {
        final View cardFrame = findViewById(R.id.item_frame);
        final View videoViewFrame = findViewById(R.id.item_video_frame);
        final VideoView videoView = (VideoView) findViewById(R.id.item_video);

        if(item.video) {
            videoView.setVideoURI(Uri.parse(itemInfo.media));

            final FullscreenMediaController mediaController = new FullscreenMediaController(this);

            mediaController.setListener(
                    new FullscreenMediaController.OnMediaControllerInteractionListener() {
                @Override
                public void onRequestFullScreen() {
                    VideoActivity.launch(ViewItemActivity.this, itemInfo.media,
                            videoView.getCurrentPosition());
                }
            });

            mediaController.setAnchorView(videoViewFrame);

            // I hate it when the screen goes dark while watching a video.
            videoView.setKeepScreenOn(true);
            videoView.setMediaController(mediaController);

            videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    Log.d("dumpert.viewitem", "onPrepared");
                    cardFrame.setVisibility(View.GONE);
                    videoViewFrame.setAlpha(1f);
//                ViewCompat.setTransitionName(videoViewFrame, "item");
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
                    findViewById(R.id.item_loading).setVisibility(View.GONE);
                    findViewById(R.id.item_type).setVisibility(View.VISIBLE);
                    videoViewFrame.setAlpha(0f);
                    reloadSnack(R.string.error_video_failed, R.string.error_reload);

                    return true;
                }
            });


            videoView.start();

            // resume video after screen is turned on again.
            videoView.seekTo(this.lastVideoPos);
            this.lastVideoPos = 0;
        }

        if(item.audio) {
            //audiohandler is sync, so don't call from main thread
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        final FrameLayout master = (FrameLayout) findViewById(
                                R.id.item_master_frame);
                        audioHandler = new AudioHandler() {
                            @Override
                            public void onPrepared(MediaPlayer mediaplayer) {
                                super.onPrepared(mediaplayer);

                                //hide progressbar etc
                                cardFrame.setVisibility(View.GONE);
                                findViewById(R.id.item_loading).setVisibility(View.GONE);
                                videoViewFrame.setAlpha(1f);
                            }
                        };
                        audioHandler.playAudio(itemInfo.media, ViewItemActivity.this, master);
                    } catch(Exception e) {
                        e.printStackTrace();
                        reloadSnack(R.string.error_audio_failed, R.string.error_reload);
                    }
                }
            }).start();
        }
    }

    public void initHeader() {
        final ImageView itemImage = (ImageView)findViewById(R.id.item_image);
        item = (Item)getIntent().getSerializableExtra("item");
        Picasso.with(this).load(item.imageUrls == null ? null : item.imageUrls[0])
                .into(itemImage, new Callback.EmptyCallback() {
                    @Override
                    public void onSuccess() {
                        if (preferences.getBoolean("dynamiccolors", false)) {
                            Bitmap bitmap = ((BitmapDrawable) itemImage.getDrawable()).getBitmap();

                            // @fixme: generateAsync is deprecated. Use Palette.Builder instead.
                            Palette.generateAsync(bitmap, new Palette.PaletteAsyncListener() {
                                public void onGenerated(Palette palette) {
                                    Palette.Swatch swatch = palette.getVibrantSwatch();
                                    Palette.Swatch swatchDark = palette.getDarkVibrantSwatch();
                                    if (swatch != null) {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
                                                && swatchDark != null)
                                            getWindow().setStatusBarColor(swatchDark.getRgb());

                                        if(getSupportActionBar() != null)
                                            getSupportActionBar()
                                                .setBackgroundDrawable(
                                                        new ColorDrawable(swatch.getRgb()));

                                    }
                                }
                            });
                        }
                    }
                });

        final ImageView itemType = (ImageView)findViewById(R.id.item_type);
        if(item.photo)
            itemType.setImageResource(R.drawable.ic_photo);
        else if(item.video)
            itemType.setImageResource(R.drawable.ic_play_circle_fill);
        else if(item.audio)
            itemType.setImageResource(R.drawable.ic_audiotrack);

        // @fixme: getColor is deprecated. Change to ContextCompat.getColor() instead.
        int gray = getResources().getColor(R.color.gray_bg);
        if(item.audio) {
            FrameLayout itemFrame = (FrameLayout)findViewById(R.id.item_frame);
            FrameLayout master = (FrameLayout)findViewById(R.id.item_master_frame);
            ViewGroup.LayoutParams layoutParams = master.getLayoutParams();
            ViewGroup.LayoutParams layoutParams2 = itemFrame.getLayoutParams();
            layoutParams.height = layoutParams2.height = Math.round(TypedValue
                    .applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100,
                            getResources().getDisplayMetrics()));
            master.setLayoutParams(layoutParams);
            itemFrame.setLayoutParams(layoutParams2);

            itemImage.setBackgroundColor(obtainStyledAttributes(new int[] {
                    R.attr.colorPrimaryDark
            }).getColor(0, gray));
        } else {
            itemImage.setBackgroundColor(gray);
        }

        final ProgressBar progressBar = (ProgressBar)findViewById(R.id.item_loading);
        if(item.video || item.audio) {
            progressBar.setVisibility(View.VISIBLE);
            itemType.setVisibility(View.GONE);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    boolean error = false;
                    try {
                        if(!Utils.isOffline(ViewItemActivity.this))
                            itemInfo = API.getItemInfo(item, ViewItemActivity.this);
                    } catch (Exception e) {
                        error = true;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                reloadSnack(R.string.error_video_failed, R.string.error_reload);
                            }
                        });
                    }

                    final boolean err = error;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (!err && !Utils.isOffline(ViewItemActivity.this)
                                    && preferences.getBoolean("autoplay_vids", true)) {
                                startMedia(itemInfo, item);
                            } else {
                                progressBar.setVisibility(View.GONE);
                                itemType.setVisibility(View.VISIBLE);
                            }
                        }
                    });
                }
            }).start();
        }

        item = (Item)getIntent().getSerializableExtra("item");
        ViewCompat.setTransitionName(itemImage, "item");

        itemImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (item.photo)
                    ImageActivity.launch(ViewItemActivity.this, itemImage, item.imageUrls);
                else if (item.video && itemInfo != null
                        && progressBar.getVisibility() != View.VISIBLE) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(itemInfo.media)));
                }
            }
        });
    }

    public void loadComments(final boolean refresh) {
        if(!refresh) {
            commentsAdapter = new CommentsAdapter(new Comment[0], this);
            comments.setAdapter(commentsAdapter);
        }

        //get id from url
        try {
            if (itemID == null)
                throw new InvalidParameterException("ViewItem got a invalid url passed to it :(");

            final String id = itemID.replace(".", "_");

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        final Comment[] commentsData = API.getComments(id, ViewItemActivity.this);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                commentsAdapter.addItems(commentsData);
                                if (refresh) swipeRefreshLayout.setRefreshing(false);
                                else {
                                    comments.setVisibility(View.VISIBLE);
                                    findViewById(R.id.comments_loader).setVisibility(View.GONE);
                                }
                            }
                        });
                    } catch (IOException e) {
                        e.printStackTrace();
                        reloadSnack(R.string.error_comments_failed, R.string.error_reload);
                    }
                }
            }).start();
        } catch(Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    public void tip() {
        if (!item.photo) return;
        SharedPreferences sharedPreferences = getSharedPreferences("dumpert", 0);
        if (!sharedPreferences.getBoolean("seenItemTip", false)) {
            sharedPreferences.edit().putBoolean("seenItemTip", true).apply();

            final Snackbar snackbar = Snackbar.make(findViewById(R.id.root),
                    R.string.tip_touch_to_enlarge, Snackbar.LENGTH_INDEFINITE);

            snackbar.setAction(R.string.tip_close, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.v(TAG, "dismissing snackbar");
                    snackbar.dismiss();
                }
            });

            snackbar.show();
        }
    }

    public static void launch(Activity activity, View transitionView, Item item) {
        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(activity,
                transitionView, "item");
        Intent intent = new Intent(activity, ViewItemActivity.class);
        intent.putExtra("item", item);
        ActivityCompat.startActivity(activity, intent, options.toBundle());

//        Intent intent = new Intent(activity, ViewItem.class);
//        intent.putExtra("item",  item);
//        activity.startActivity(intent);
    }

    private void reloadSnack(int title, int button) {
        final Snackbar snackbar = Snackbar.make(findViewById(R.id.root), title,
                Snackbar.LENGTH_INDEFINITE);

        snackbar.setAction(button, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v(TAG, "reloading activity");

                ViewItemActivity reload = ViewItemActivity.this;
                Intent reloadIntent = reload.getIntent();
                Serializable item = reload.getIntent().getSerializableExtra("item");
                Bundle bundle = new Bundle();

                bundle.putSerializable("item", item);
                reloadIntent.putExtras(bundle);
                reload.finish();
                startActivity(reloadIntent);
                reload.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        });

        snackbar.show();
    }

}
