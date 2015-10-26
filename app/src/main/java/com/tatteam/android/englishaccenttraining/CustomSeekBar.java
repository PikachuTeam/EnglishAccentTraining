package com.tatteam.android.englishaccenttraining;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

/**
 * Created by Thanh on 20/09/2015.
 */
public class CustomSeekBar extends RelativeLayout implements View.OnTouchListener {
    private RelativeLayout indicatorView;
    private OnSeekbarChangeListener listener;

    private boolean lock = false;

    public CustomSeekBar(Context context) {
        super(context);
    }

    public CustomSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomSeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setup() {
        this.post(new Runnable() {
            @Override
            public void run() {
                setClickable(true);
                setupIndicator();
                CustomSeekBar.this.setOnTouchListener(CustomSeekBar.this);
            }
        });
    }

    private void setupIndicator() {
        indicatorView = new RelativeLayout(getContext());
        indicatorView.setLayoutParams(new LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT));
        indicatorView.setBackgroundResource(R.drawable.seekbar_background_2);

        CustomSeekBar.this.addView(indicatorView);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_MOVE || event.getAction() == MotionEvent.ACTION_DOWN) {
            lock = true;
            indicatorView.setLayoutParams(new LayoutParams((int) event.getX(), ViewGroup.LayoutParams.MATCH_PARENT));
            indicatorView.invalidate();
        }
        if (event.getAction() == MotionEvent.ACTION_UP) {
            lock = false;
            if (listener != null) {
                listener.onSeekbarChangeListener(indicatorView.getWidth(), this.getWidth());
            }
        }
        return false;
    }

    public void updateIndicator(float percent) {
        if (!lock && indicatorView != null)
            indicatorView.setLayoutParams(new LayoutParams((int) (percent * (float) this.getWidth()), ViewGroup.LayoutParams.MATCH_PARENT));
    }

    public interface OnSeekbarChangeListener {
        void onSeekbarChangeListener(int smallViewWidth, int parentWidth);
    }

    public void setSeekbarChangeListener(OnSeekbarChangeListener listener) {
        this.listener = listener;
    }
}
