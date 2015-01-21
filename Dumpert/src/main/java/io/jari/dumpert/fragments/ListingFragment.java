package io.jari.dumpert.fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.nispok.snackbar.Snackbar;
import com.nispok.snackbar.listeners.ActionClickListener;
import io.jari.dumpert.R;
import io.jari.dumpert.Utils;
import io.jari.dumpert.adapters.CardAdapter;
import io.jari.dumpert.animators.SlideInOutBottomItemAnimator;
import io.jari.dumpert.api.API;
import io.jari.dumpert.api.Item;

/**
 * JARI.IO
 * Date: 15-1-15
 * Time: 14:05
 */
public class ListingFragment extends Fragment {
    View main;
    public SharedPreferences preferences;
    RecyclerView recyclerView;
    SwipeRefreshLayout swipeRefreshLayout;

    private boolean loading = false;
    int pastVisibleItems, visibleItemCount, totalItemCount;
    int page = 1;
    String currentPath;

    /**
     * Return the listing path
     * @return listing path
     */
    public String getCurrentPath() {
        return null;
    }
    
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        currentPath = getCurrentPath();
        preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        //use activity inflater rather than our own inflator due to android supportv4 bug
        main = inflater.inflate(R.layout.main, container, false);
        
        swipeRefreshLayout = (SwipeRefreshLayout) main.findViewById(R.id.swiperefresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(true);
                if(cardAdapter != null) cardAdapter.removeAll();
                loadData(true, currentPath);
                page = 0;
            }
        });

        recyclerView = (RecyclerView) main.findViewById(R.id.recycler);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        recyclerView.setHasFixedSize(true);

        recyclerView.setItemAnimator(new SlideInOutBottomItemAnimator(recyclerView));

        // use a linear layout manager
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(linearLayoutManager);

        recyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                visibleItemCount = linearLayoutManager.getChildCount();
                totalItemCount = linearLayoutManager.getItemCount();
                pastVisibleItems = linearLayoutManager.findFirstVisibleItemPosition();

                if (!loading && (visibleItemCount + pastVisibleItems) >= totalItemCount) {
                    page++;
                    addData(page, currentPath);
                    loading = true;
                }
            }
        });

        this.loadData(false, currentPath);

        return main;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    Snackbar offlineSnackbar;
    boolean offlineSnackDismissed = false;

    public void offlineSnack() {
        if(getActivity() == null) return;

        if (Utils.isOffline(getActivity())) {
            ((ActionBarActivity)getActivity()).getSupportActionBar().setSubtitle(R.string.cached_version);
            if (offlineSnackDismissed) return;

            if (offlineSnackbar != null && offlineSnackbar.isShowing()) offlineSnackbar.dismiss();

            offlineSnackbar = Snackbar.with(getActivity()).text(getResources().getString(R.string.tip_offline))
                    .duration(999999999)
                    .animation(false)
                    .swipeToDismiss(false)
                    .actionLabel(R.string.tip_close)
                    .actionListener(new ActionClickListener() {
                        @Override
                        public void onActionClicked(Snackbar snackbar) {
                            offlineSnackDismissed = true;
                        }
                    });

            offlineSnackbar.show(getActivity());
        } else {
            if(((ActionBarActivity)getActivity()).getSupportActionBar().getSubtitle() == getResources().getString(R.string.cached_version)) {
                ((ActionBarActivity)getActivity()).getSupportActionBar().setSubtitle("");
            }
            if (offlineSnackDismissed) return;

            if (offlineSnackbar != null && offlineSnackbar.isShowing()) offlineSnackbar.dismiss();
        }
    }

    CardAdapter cardAdapter;

    public void loadData(final boolean refresh, final String path) {
        this.offlineSnack();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final Item[] items = API.getListing(getActivity(), path);
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            cardAdapter = new CardAdapter(new Item[0], getActivity());
                            recyclerView.setAdapter(cardAdapter);

                            if (refresh) {
                                swipeRefreshLayout.setRefreshing(false);
                                cardAdapter.removeAll();
                            }

                            cardAdapter.addItems(items);
                        }
                    });

                } catch (Exception e) {
                    errorSnack(e);
                    e.printStackTrace();
                }
                finally {
                    try {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (refresh)
                                    swipeRefreshLayout.setRefreshing(false);
                                else dismissLoader();
                            }
                        });
                    }
                    catch(Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    public void dismissLoader() {
        final View prog = main.findViewById(R.id.progressBar);
        prog.animate().alpha(0f).setDuration(500).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                prog.setVisibility(View.GONE);
            }
        });
    }

    public void errorSnack(final Exception e) {
        if(getActivity() == null) return;

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Snackbar.with(getActivity())
                        .text(R.string.items_failed)
                        .textColor(Color.parseColor("#FFCDD2"))
                        .actionLabel(R.string.moreinfo)
                        .duration(10000)
                        .actionListener(new ActionClickListener() {
                            @Override
                            public void onActionClicked(Snackbar snackbar) {
                                new AlertDialog.Builder(getActivity())
                                        .setTitle(R.string.moreinfo)
                                        .setMessage(e.getClass().getName() + " " + e.getMessage())
                                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                            }
                                        })
                                        .create()
                                        .show();
                            }
                        })
                        .show(getActivity());
            }
        });
    }

    public void addData(final Integer page, final String path) {
        this.offlineSnack();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final Item[] items = API.getListing(page, getActivity(), path);
                    if (items.length == 0) ListingFragment.this.page--; //if API returned nothing, put page number back
                    loading = false;
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            cardAdapter.addItems(items);
                        }
                    });
                } catch (Exception e) {
                    ListingFragment.this.page--;
                    errorSnack(e);
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
