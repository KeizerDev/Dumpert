package io.jari.dumpert;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * JARI.IO
 * Date: 22-12-14
 * Time: 2:11
 */
public class NavigationAdapter extends RecyclerView.Adapter {
    ArrayList<NavigationItem> navigationItems;
    Activity activity;

    public NavigationAdapter(NavigationItem[] navigationItems, Activity activity) {
        this.activity = activity;
        this.navigationItems = new ArrayList<NavigationItem>(Arrays.asList(navigationItems));
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(activity).inflate(R.layout.navigationitem, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int i) {
        ViewHolder holder = (ViewHolder)viewHolder;
        holder.update(navigationItems.get(i));
    }

    @Override
    public int getItemCount() {
        return navigationItems.size();
    }

    public void setActive(NavigationItem navigationItem) {
        Integer searchInt = navigationItems.indexOf(navigationItem);
        for(NavigationItem item : navigationItems) {
            item.selected = navigationItems.indexOf(item) == searchInt;
        }

        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        View view;

        public void update(final NavigationItem navigationItem) {
            ImageView image = (ImageView)view.findViewById(R.id.item_image);
            TextView title = (TextView)view.findViewById(R.id.item_name);
            View divider = view.findViewById(R.id.divider);
            View layout = view.findViewById(R.id.item);
            image.setImageDrawable(navigationItem.drawable);
            title.setText(navigationItem.title);

            if(navigationItem.selected)
                layout.setBackgroundResource(R.drawable.selected_ripple);
            else layout.setBackgroundDrawable(activity.obtainStyledAttributes(new int[]{android.R.attr.selectableItemBackground}).getDrawable(0));

            if(navigationItem.hasDivider)
                divider.setVisibility(View.VISIBLE);
            else divider.setVisibility(View.GONE);

            layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (navigationItem.callback != null)
                        navigationItem.callback.onClick(navigationItem);
                }
            });
        }

        public ViewHolder(View itemView) {
            super(itemView);
            this.view = itemView;
        }
    }
}
