package io.jari.dumpert.layouts;

import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;
import android.util.Log;

public class NestedSwipeRefreshLayout extends SwipeRefreshLayout {
    private OnChildScrollUpListener mOnChildScrollUpListener;

    public interface OnChildScrollUpListener {
        boolean canChildScrollUp();
    }

    public NestedSwipeRefreshLayout(Context context) {
        super(context);
    }

    public NestedSwipeRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setOnChildScrollUpListener(OnChildScrollUpListener listener) {
        mOnChildScrollUpListener = listener;
    }

    @Override
    public boolean canChildScrollUp() {
        if(mOnChildScrollUpListener == null) {
            Log.e(NestedSwipeRefreshLayout.class.getSimpleName(),
                    "onChildScrollUpListener is not defined!");
        }

        return mOnChildScrollUpListener != null && mOnChildScrollUpListener.canChildScrollUp();
    }
}