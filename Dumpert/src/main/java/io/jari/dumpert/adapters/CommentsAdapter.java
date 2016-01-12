package io.jari.dumpert.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;

import io.jari.dumpert.R;
import io.jari.dumpert.api.API;
import io.jari.dumpert.api.Comment;
import io.jari.dumpert.dialogs.ReplyDialog;

public class CommentsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    ArrayList<Comment> dataSet;
    Activity activity;
    String itemID;

    public CommentsAdapter(Comment[] comments, Activity activity, String itemID) {
        this.dataSet = new ArrayList<>(Arrays.asList(comments));
        this.activity = activity;
        this.itemID = itemID;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View comment = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_comment, parent, false);
        return new CommentView(activity, comment);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Comment comment = dataSet.get(position);
        ((CommentView)holder).update(comment);
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }

    @Override
    public void onViewDetachedFromWindow(RecyclerView.ViewHolder holder) {
        super.onViewDetachedFromWindow(holder);

        holder.itemView.findViewById(R.id.comment_votes).setVisibility(View.GONE);

        if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            ((TextView)holder.itemView.findViewById(R.id.comment_author_newbie)).setTextColor(activity.getResources().getColor(R.color.grey_700, activity.getTheme()));
            ((TextView)holder.itemView.findViewById(R.id.comment_author)).setTextColor(activity.getResources().getColor(R.color.grey_700, activity.getTheme()));
        } else {
            ((TextView)holder.itemView.findViewById(R.id.comment_author_newbie)).setTextColor(activity.getResources().getColor(R.color.grey_700));
            ((TextView)holder.itemView.findViewById(R.id.comment_author)).setTextColor(activity.getResources().getColor(R.color.grey_700));
        }
    }

    public void removeAll() {
        for (int i = dataSet.size()-1; i >= 0; i--) {
            remove(dataSet.get(i));
        }
    }

    public void add(Comment item) {
        dataSet.add(item);
        notifyItemInserted(dataSet.size() - 1);
    }

    public void addItems(Comment[] items) {
        for(Comment item : items) {
            add(item);
        }
    }

    public void remove(Comment item) {
        int position = dataSet.indexOf(item);
        dataSet.remove(position);
        notifyItemRemoved(position);
    }

    // might come in handy in the future, but for now this is used to grab the entryID of a post
    public Comment getItem(int index) {
        if(getItemCount() >= index)
            return dataSet.get(index);

        return null;
    }

    class CommentView extends RecyclerView.ViewHolder {
        private static final String TAG = "CommentView";
        Comment comment;
        Activity activity;
        View view;

        public CommentView(Activity activity, View itemView) {
            super(itemView);
            this.activity = activity;
            this.view = itemView;
        }

        public void update(final Comment comment) {
            this.comment = comment;

            // regular comment items
            TextView best = (TextView)view.findViewById(R.id.comment_best);
            TextView author_newbie = (TextView)view.findViewById(R.id.comment_author_newbie);
            TextView author = (TextView)view.findViewById(R.id.comment_author);
            TextView message = (TextView)view.findViewById(R.id.comment_message);
            TextView time = (TextView)view.findViewById(R.id.comment_time);
            final TextView score = (TextView)view.findViewById(R.id.comment_score);

            if(comment.best) {
                view.setBackgroundResource(R.drawable.best_ripple);
            } else {
                // @fixme setBackgroundDrawable is deprecated...
                view.setBackgroundDrawable(activity.obtainStyledAttributes(new int[]{
                        android.R.attr.selectableItemBackground
                }).getDrawable(0));
            }

            String username = activity.getSharedPreferences("dumpert", 0).getString("username", "");
            boolean highlightself = PreferenceManager.getDefaultSharedPreferences(activity).getBoolean("highlightself", true);
            if(!username.equals("") && highlightself) {
                if(comment.author.equals(username)) {
                    if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                        author_newbie.setTextColor(activity.getResources().getColor(R.color.highlight, activity.getTheme()));
                        author.setTextColor(activity.getResources().getColor(R.color.highlight, activity.getTheme()));
                    } else {
                        author_newbie.setTextColor(activity.getResources().getColor(R.color.highlight));
                        author.setTextColor(activity.getResources().getColor(R.color.highlight));
                    }
                }
            }

            if(comment.newbie) {
                author_newbie.setVisibility(View.VISIBLE);
                author_newbie.setText(comment.author);
                author.setVisibility(View.GONE);
            } else {
                author.setVisibility(View.VISIBLE);
                author.setText(comment.author);
                author_newbie.setVisibility(View.GONE);
            }

            best.setVisibility(!comment.best ? View.GONE : View.VISIBLE);
            score.setText(comment.score == null ? "" : Integer.toString(comment.score));
            message.setText(Html.fromHtml(comment.content));
            message.setMovementMethod(LinkMovementMethod.getInstance());
            time.setText(comment.time);

            Log.d(TAG, "got message: " + comment.content);

            // if we can vote on it
            if(!comment.entry.equals("")) {
                // voting items
                SharedPreferences credentials = activity.getSharedPreferences("dumpert", 0);
                String session = credentials.getString("session", "");
                final GridLayout votes = (GridLayout) view.findViewById(R.id.comment_votes);
                final AppCompatImageButton upvote = (AppCompatImageButton)
                        view.findViewById(R.id.upvote);
                final AppCompatImageButton downvote = (AppCompatImageButton)
                        view.findViewById(R.id.downvote);
                AppCompatImageButton reply = (AppCompatImageButton) view.findViewById(R.id.comment);

                // hide score, since we already see it in the comment
                view.findViewById(R.id.votes).setVisibility(view.GONE);

                if(session.equals("")) {
                    // not logged in
                    reply.setVisibility(View.GONE);
                } else {
                    reply.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ReplyDialog.newInstance(itemID, comment).show(activity.getFragmentManager(), "Reply");
                        }
                    });
                }

                // show vote and reply layout when comment is clicked
                View.OnClickListener layoutClickListener = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (votes.getVisibility() == View.VISIBLE) {
                            votes.setVisibility(View.GONE);
                        } else {
                            votes.setVisibility(View.VISIBLE);
                        }
                    }
                };

                // adds functionality to the up and downvote buttons
                View.OnClickListener voteClickListener = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String link = "http://www.geenstijl.nl/modlinks/domod.php?entry="
                                + comment.entry + "&cid=" + comment.id;
                        int mod = 0;

                        switch(v.getId()) {
                            case R.id.upvote:
                                link += "&mod=1&callback=?";
                                // @todo: listen if the vote is counted on Dumpert.
                                mod = Integer.parseInt(score.getText().toString())+1;
                                upvote.setOnClickListener(null);
                                break;
                            case R.id.downvote:
                                link += "&mod=-1&callback=?";
                                // @todo: listen if the vote is counted on Dumpert.
                                mod = Integer.parseInt(score.getText().toString())-1;
                                downvote.setOnClickListener(null);
                                break;
                        }

                        API.vote(link);
                        score.setText(Integer.toString(mod));
                    }
                };

                view.setOnClickListener(layoutClickListener);
                upvote.setOnClickListener(voteClickListener);
                downvote.setOnClickListener(voteClickListener);
            }
        }
    }

}
