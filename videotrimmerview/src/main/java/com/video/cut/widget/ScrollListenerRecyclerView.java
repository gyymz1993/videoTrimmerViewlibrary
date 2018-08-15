package com.video.cut.widget;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.video.cut.utils.UnitConverter;

import static java.lang.Math.*;

public class ScrollListenerRecyclerView extends RecyclerView {
    //记录滚动的总距离，类似view中的mScrollX和mScrollY
    private int sx = 0, sy = 0;
    //自己定义的滚动监听接口
    public interface RecyclerScrollChangeListener{
        void ScrollChange(int y);
    }
    private RecyclerScrollChangeListener mScrollListener = null;

    public ScrollListenerRecyclerView(Context context) {
        this(context,null);
    }

    public ScrollListenerRecyclerView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public ScrollListenerRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        addScrollListener();

    }
    //重写scrollTo方法，通过内部的scrollBy实现scrollTo的功能
    @Override
    public void scrollTo(int x, int y) {
        super.scrollTo(x, y);
        //计算滚动的相对距离，通过传进来的x和当前的sx差值就是scrollBy需要滚动的值
        scrollBy(x - sx, y - sy);
    }


    //重写scrollTo方法，通过内部的scrollBy实现scrollTo的功能
    public void scrollTo(int x) {
        super.scrollTo(x, 0);
        //计算滚动的相对距离，通过传进来的x和当前的sx差值就是scrollBy需要滚动的值
        scrollBy(x - sx, 0);
    }

    //添加view的scrolllistener，在view滚动时根据滚动的距离自己计算sx，sy
    private void addScrollListener(){
        addOnScrollListener(new OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
//                oldsx = sx;
//                oldsy = sy
                //因为dx和dy是相对上次的滚动距离，所以通过一开始的累加可以得到总的sx和sy
                sx += dx;
                sy += dy;
                super.onScrolled(recyclerView, dx, dy);
                //此处如果有需要监听滚动变化的话在这回调
                if(mScrollListener != null){
                    mScrollListener.ScrollChange(sx);
                }
            }
        });
    }

    public void addLayout(){
        RelativeLayout relativeLayout=new RelativeLayout(getContext());
        addView(relativeLayout);
    }


    public void addView(float left){
        RelativeLayout.LayoutParams params=new RelativeLayout.LayoutParams(UnitConverter.dpToPx(5),UnitConverter.dpToPx(5));
        params.leftMargin = (int) Math.ceil(left);
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        //imageViewHots.add(imageView);
       // ryHotLayout.addView(imageView,params);
    }

    //设置监听
    public void setRecyclerScrollListener(RecyclerScrollChangeListener scrollListener) {
        this.mScrollListener = scrollListener;
    }
}