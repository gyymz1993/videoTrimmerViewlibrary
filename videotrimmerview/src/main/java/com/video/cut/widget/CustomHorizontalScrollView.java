package com.video.cut.widget;

import android.content.Context;
import android.support.v4.widget.NestedScrollView;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class CustomHorizontalScrollView extends NestedScrollView {
    private Context context;
    private ScrollViewListenner listenner;
    private CustomHorizontalScrollView currentView;

    public CustomHorizontalScrollView(Context context) {
        super(context);
        // TODO Auto-generated constructor stub
        this.context = context;
    }

    public CustomHorizontalScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
        this.context = context;
    }

    public CustomHorizontalScrollView(Context context, AttributeSet attrs,
                                      int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        // TODO Auto-generated method stub
        currentView = this;
        return false;
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        // TODO Auto-generated method stub
        if (null != listenner) {
            this.listenner.onScrollChanged(currentView, l, t, oldl, oldt);
        }
        super.onScrollChanged(l, t, oldl, oldt);
    }

    public interface ScrollViewListenner {
        public void onScrollChanged(CustomHorizontalScrollView view, int l,
                                    int t, int oldl, int oldt);
    }

    public void setScrollViewListenner(ScrollViewListenner listenner) {
        this.listenner = listenner;
    }

    /**
    *
    *阻尼：1000为将惯性滚动速度缩小1000倍，近似drag操作。
    @Override
    public void fling(int velocity) {
        super.fling(velocity / 1000);
    }
    */
}