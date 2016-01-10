package io.jari.dumpert;


import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.MediaController;


/**
 * Custom mediacontroller that adds a fullscreen button.
 */
public class FullscreenMediaController extends MediaController {

    public interface OnMediaControllerInteractionListener {
        void onRequestFullScreen();
    }

    Context mContext;
    private OnMediaControllerInteractionListener mListener;

    public FullscreenMediaController(Context context) {
        super(context);
        mContext = context;
    }

    public void setListener(OnMediaControllerInteractionListener listener) {
        mListener = listener;
    }

    @Override
    public void setAnchorView(View view) {
        super.setAnchorView(view);

        FrameLayout.LayoutParams frameParams = new FrameLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        frameParams.gravity = Gravity.END | Gravity.TOP;


        ImageButton fullscreenButton = (ImageButton) LayoutInflater.from(mContext)
                .inflate(R.layout.layout_fullscreen_button, null);

        fullscreenButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onRequestFullScreen();
                }
            }
        });

        addView(fullscreenButton, frameParams);
    }
}