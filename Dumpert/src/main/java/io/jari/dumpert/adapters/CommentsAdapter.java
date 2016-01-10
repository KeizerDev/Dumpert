package io.jari.dumpert.adapters;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import io.jari.dumpert.R;
import io.jari.dumpert.api.Comment;

import java.util.ArrayList;
import java.util.Arrays;

public class CommentsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    ArrayList<Comment> dataSet;
    Activity activity;

    public CommentsAdapter(Comment[] comments, Activity activity) {
        this.dataSet = new ArrayList<>(Arrays.asList(comments));
        this.activity = activity;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View comment = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.comment, parent, false);
        return new CommentView(comment);
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

    class CommentView extends RecyclerView.ViewHolder {
        Comment comment;
        View view;
        public CommentView(View itemView) {
            super(itemView);
            this.view = itemView;
        }

        public void update(Comment comment) {
            this.comment = comment;

            TextView best = (TextView)view.findViewById(R.id.comment_best);
            TextView author = (TextView)view.findViewById(R.id.comment_author);
            TextView message = (TextView)view.findViewById(R.id.comment_message);
            TextView time = (TextView)view.findViewById(R.id.comment_time);
            TextView score = (TextView)view.findViewById(R.id.comment_score);

            if(comment.best)
                view.setBackgroundResource(R.drawable.best_ripple);
            else view.setBackgroundDrawable(activity.obtainStyledAttributes(new int[]{android.R.attr.selectableItemBackground}).getDrawable(0));

            best.setVisibility(!comment.best ? View.GONE : View.VISIBLE);
            author.setText(comment.author);
            score.setText(comment.score == null ? "" : Integer.toString(comment.score));
            message.setText(Html.fromHtml(comment.content));
            message.setMovementMethod(LinkMovementMethod.getInstance());
//            Linkify.addLinks(message, Linkify.ALL);
            time.setText(comment.time);
        }
    }
}