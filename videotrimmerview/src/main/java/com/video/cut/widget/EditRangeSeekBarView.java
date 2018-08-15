package com.video.cut.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

import com.video.cut.R;
import com.video.cut.models.BaseLetterBean;
import com.video.cut.utils.DeviceUtil;
import com.video.cut.utils.UnitConverter;


public class EditRangeSeekBarView extends View {

    public static final int INVALID_POINTER_ID = 255;
    private static final String TAG = EditRangeSeekBarView.class.getSimpleName();
    private static final int paddingTop = UnitConverter.dpToPx(0);
    private final Paint mShadow = new Paint();
    private float rangeL = UnitConverter.getDisplayMetrics().widthPixels / 2 - UnitConverter.dpToPx(35);
    private float tempL;
    private float rangeR;
    private float xDistance = 0;
    private float aboutSpacing;
    private OnUpdateSeekBarProgress updateSeekBarProgress;
    private int mActivePointerId = INVALID_POINTER_ID;
    private int mScaledTouchSlop;
    private Bitmap thumbImageLeft;
    private Bitmap thumbImageRight;
    private Bitmap thumbPressedImage;
    private Paint paint;
    private Paint rectPaint;
    private Paint bgPaint;
    private int thumbWidth;
    private float thumbHalfWidth;
    private float thumbPaddingTop = 0;
    private float mDownMotionX;
    private boolean mIsDragging;
    private Thumb pressedThumb;
    private boolean notifyWhileDragging = false;
    private int whiteColorRes = getContext().getResources().getColor(R.color.white);
    private float mMinLeftWidth = UnitConverter.getDisplayMetrics().widthPixels / 2;
    private int criticalPoint = DeviceUtil.getDeviceWidth() / 2 - UnitConverter.dpToPx(35);
    private float mMaxRightWidth;
    private long downTime;
    private float mAverageMsPx;//每毫秒所占的px
    private BaseLetterBean mLetterBean;
    private long defaultShortTime=3000L;
    private float defaultMinWidth;


    public EditRangeSeekBarView(Context context) {
        super(context);
    }

    public EditRangeSeekBarView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public EditRangeSeekBarView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public EditRangeSeekBarView(Context context, float averageMsPx, BaseLetterBean letterBean) {
        super(context);
        setFocusable(true);
        setFocusableInTouchMode(true);
        initData(averageMsPx, letterBean);
        initView();
    }

    public void setmAverageMsPx(float mAverageMsPx) {
        this.mAverageMsPx = mAverageMsPx;
    }


    private void initData(float averageMsPx, BaseLetterBean letterBean) {
        rangeL = criticalPoint;
        mLetterBean = letterBean;
        mAverageMsPx = averageMsPx;
        defaultMinWidth=defaultShortTime / mAverageMsPx;
        rangeR = rangeL + defaultMinWidth;
        aboutSpacing = rangeR - rangeL;
        tempL = rangeL;
    }

    public void updataRandSeekBar(BaseLetterBean letterBean){
        mLetterBean=letterBean;
        rangeL = criticalPoint;
        rangeR = rangeL + (letterBean.getDuration() / mAverageMsPx);
        aboutSpacing = rangeR - rangeL;
        tempL = rangeL;
        invalidate();
    }

    public void initRandSeekBarInit(BaseLetterBean letterBean){
        mLetterBean=letterBean;
        rangeL = criticalPoint;
        rangeR = rangeL + (defaultShortTime / mAverageMsPx);
        aboutSpacing = rangeR - rangeL;
        tempL = rangeL;
        invalidate();
    }

    public void initView() {
        mScaledTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
        thumbImageLeft = BitmapFactory.decodeResource(getResources(), R.drawable.right_pro);
        thumbImageRight = BitmapFactory.decodeResource(getResources(), R.drawable.left_pro);
        int width = thumbImageLeft.getWidth();
        int height = thumbImageLeft.getHeight();
        int newWidth = UnitConverter.dpToPx(14);
        int newHeight = UnitConverter.dpToPx(40);
        float scaleWidth = newWidth * 1.0f / width;
        float scaleHeight = newHeight * 1.0f / height;
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        thumbImageLeft = Bitmap.createBitmap(thumbImageLeft, 0, 0, width, height - paddingTop, matrix, true);
        thumbImageRight = Bitmap.createBitmap(thumbImageRight, 0, 0, width, height - paddingTop, matrix, true);
        thumbPressedImage = thumbImageLeft;
        thumbWidth = newWidth;
        thumbHalfWidth = thumbWidth / 2;
        int shadowColor = getContext().getResources().getColor(R.color.shadow_translucent);
        mShadow.setAntiAlias(true);
        mShadow.setColor(shadowColor);
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        rectPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        rectPaint.setStyle(Paint.Style.FILL);
        rectPaint.setColor(whiteColorRes);

        int bgPaintColor = getContext().getResources().getColor(R.color.shadow_translucent_button);
        bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bgPaint.setStyle(Paint.Style.FILL);
        bgPaint.setColor(bgPaintColor);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float bg_middle_left = rangeL;
        float bg_middle_right = getWidth() - getPaddingRight();
        //修改为选中高亮
        Rect leftRect = new Rect((int) bg_middle_left, 0, (int) rangeL, getHeight());
        Rect rightRect = new Rect((int) rangeR, 0, (int) bg_middle_right, getHeight());
        canvas.drawRect(leftRect, mShadow);
        canvas.drawRect(rightRect, mShadow);


        //头部和底部的矩形隐藏
        canvas.drawRect(rangeL, thumbPaddingTop + paddingTop, rangeR, thumbPaddingTop + UnitConverter.dpToPx(2) + paddingTop, rectPaint);
        canvas.drawRect(rangeL, getHeight() - UnitConverter.dpToPx(2), rangeR, getHeight(), rectPaint);
        canvas.drawRect(rangeL, thumbPaddingTop + paddingTop, rangeR, getHeight(), bgPaint);
        drawThumb(rangeL, false, canvas, true);
        drawThumb(rangeR, false, canvas, false);
    }

    private void drawThumb(float screenCoord, boolean pressed, Canvas canvas, boolean isLeft) {
        canvas.drawBitmap(pressed ? thumbPressedImage : (isLeft ? thumbImageLeft : thumbImageRight), screenCoord - (isLeft ? 0 : thumbWidth), paddingTop,
                paint);
    }


    private float mCurrentPosition;
    public void onMove(float currentPosition) {
        //if (currentPosition==0)return;
        mCurrentPosition=currentPosition;
        rangeL = (mLetterBean.getStartTime() - currentPosition) / mAverageMsPx + criticalPoint;
        rangeR = (mLetterBean.getStartTime() + mLetterBean.getDuration() - currentPosition) / mAverageMsPx + criticalPoint;

        if (rangeL < mMinLeftWidth) {
            rangeL = mMinLeftWidth;
            rangeR = rangeL+aboutSpacing;
        }
        if (rangeR > mMaxRightWidth) {
            rangeR = mMaxRightWidth;
            rangeL = rangeR - aboutSpacing;
        }
        invalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getPointerCount() > 1) {
            return super.onTouchEvent(event);
        }
        if (!isEnabled()) return false;
        int pointerIndex;// 记录点击点的index
        final int action = event.getAction();
        //获取到手指处的横坐标和纵坐标
        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                downTime = System.currentTimeMillis();
                //记住最后一个手指点击屏幕的点的坐标x，mDownMotionX
                mActivePointerId = event.getPointerId(event.getPointerCount() - 1);
                pointerIndex = event.findPointerIndex(mActivePointerId);
                mDownMotionX = event.getX(pointerIndex);
                aboutSpacing = rangeR - rangeL;
                tempL = rangeL;
                // 判断touch到的是最大值thumb还是最小值thumb
                pressedThumb = evalPressedThumb(mDownMotionX, event.getY());
                //移动操作
                if (pressedThumb == null) {
                    return super.onTouchEvent(event);
                }
                setPressed(true);// 设置该控件被按下了
                onStartTrackingTouch();// 置mIsDragging为true，开始追踪touch事件
                trackTouchEvent(event);
                attemptClaimDrag();
                break;
            case MotionEvent.ACTION_MOVE:
                if (pressedThumb != null) {
                    if (mIsDragging) {
                        trackTouchEvent(event);
                    } else {
                        // Scroll to follow the motion event
                        pointerIndex = event.findPointerIndex(mActivePointerId);
                        final float x = event.getX(pointerIndex);// 手指在控件上点的X坐标
                        // 手指没有点在最大最小值上，并且在控件上有滑动事件
                        if (Math.abs(x - mDownMotionX) > mScaledTouchSlop) {
                            setPressed(true);
                            invalidate();
                            onStartTrackingTouch();
                            trackTouchEvent(event);
                            attemptClaimDrag();
                        }
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                if (mIsDragging) {
                    trackTouchEvent(event);
                    onStopTrackingTouch();
                    setPressed(false);
                } else {
                    onStartTrackingTouch();
                    trackTouchEvent(event);
                    onStopTrackingTouch();
                }
                if (updateSeekBarProgress != null) {
                    long startTime= (long) ((rangeL-criticalPoint) * mAverageMsPx+mCurrentPosition);
                    long endTime= (long) ((rangeR-criticalPoint) * mAverageMsPx+mCurrentPosition);
                    updateSeekBarProgress.onRangeSeekBarValuesChanged(rangeL, rangeR, startTime, endTime);
                }
                invalidate();
                pressedThumb = null;// 手指抬起，则置被touch到的thumb为空
                aboutSpacing = rangeR - rangeL;
                tempL = rangeL;

                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                final int index = event.getPointerCount() - 1;
                mDownMotionX = event.getX(index);
                mActivePointerId = event.getPointerId(index);
                invalidate();
                break;
            case MotionEvent.ACTION_POINTER_UP:
                break;
            case MotionEvent.ACTION_CANCEL:
                break;
            default:
                break;
        }
        return true;
    }

    public void setMaxLeftWidth(float maxLeftWidth) {
        this.mMinLeftWidth = maxLeftWidth;
    }

    public void setMaxRightWidth(float maxRightWidth) {
        this.mMaxRightWidth = maxRightWidth;
    }

    @Override
    public void invalidate() {
        super.invalidate();
    }


    private void trackTouchEvent(MotionEvent event) {
        if (event.getPointerCount() > 1) return;
        final int pointerIndex = event.findPointerIndex(mActivePointerId);// 得到按下点的index
        float x;
        try {
            x = event.getX(pointerIndex);
        } catch (Exception e) {
            return;
        }
        if (Thumb.MIN.equals(pressedThumb)) {
            rangeL = x;
            if (rangeL < mMinLeftWidth) {
                rangeL = mMinLeftWidth;
            }
            if (rangeR - defaultMinWidth <= rangeL) {
                rangeL = rangeR - defaultMinWidth;
            }

            if (onUpdateSeekBarProgressListener!=null){
                onUpdateSeekBarProgressListener.onLeftChange(rangeL);
            }
            float startTime;
            float endTime;
            startTime=(rangeL-criticalPoint)*mAverageMsPx+mCurrentPosition;
            endTime=(rangeR-criticalPoint)*mAverageMsPx+mCurrentPosition;
            mLetterBean.setStartTime(startTime);
            mLetterBean.setDuration(endTime-startTime);
            invalidate();

        } else if (Thumb.MAX.equals(pressedThumb)) {
            rangeR = x;
            if (rangeR - defaultMinWidth <= rangeL) {
                rangeR = rangeL + defaultMinWidth;
            }
            if (rangeR > mMaxRightWidth) {
                rangeR = mMaxRightWidth;
            }
            if (onUpdateSeekBarProgressListener!=null){
                onUpdateSeekBarProgressListener.onRightChange(rangeR);
            }
            float startTime;
            float endTime;
            startTime =(rangeL-criticalPoint)*mAverageMsPx+mCurrentPosition;
            endTime=(rangeR-criticalPoint)*mAverageMsPx+mCurrentPosition;
            mLetterBean.setDuration(endTime-startTime);
            invalidate();

        } else if (Thumb.MOVE.equals(pressedThumb)) {
            if (System.currentTimeMillis() - downTime >= 0) {
                xDistance = x - mDownMotionX;
                rangeL = tempL + xDistance;
                rangeR = rangeL + aboutSpacing;

                if (rangeL < mMinLeftWidth) {
                    rangeL = mMinLeftWidth;
                    rangeR = rangeL+aboutSpacing;
                }
                if (rangeR > mMaxRightWidth) {
                    rangeR = mMaxRightWidth;
                    rangeL = rangeR - aboutSpacing;
                }
                if (onUpdateSeekBarProgressListener!=null){
                    onUpdateSeekBarProgressListener.onMove(rangeL,rangeR);
                }
                float startTime;
                float endTime;
                startTime=(rangeL-criticalPoint)*mAverageMsPx+mCurrentPosition;
                endTime=(rangeR-criticalPoint)*mAverageMsPx+mCurrentPosition;
                mLetterBean.setStartTime(startTime);
                mLetterBean.setDuration(endTime-startTime);
                invalidate();
            }
        }
    }

    /**
     * 计算位于哪个Thumb内
     *
     * @param touchX touchX
     * @return 被touch的是空还是最大值或最小值
     */
    @SuppressLint("LongLogTag")
    private Thumb evalPressedThumb(float touchX, float y) {
        Thumb result = null;
        boolean minThumbPressed = Math.abs(touchX - rangeL) <= thumbHalfWidth * 2;// 触摸点是否在最小值图片范围内
        boolean maxThumbPressed = Math.abs(touchX - rangeR) <= thumbHalfWidth * 2;
        if (minThumbPressed) {
            result = Thumb.MIN;
        } else if (maxThumbPressed) {
            result = Thumb.MAX;
        } else {
            RectF item1RF = new RectF(rangeL, 0, rangeR,
                    UnitConverter.dpToPx(40));
            if (item1RF.contains(touchX, y)) {
                result = Thumb.MOVE;
            }
        }
        return result;
    }

    /**
     * 试图告诉父view不要拦截子控件的drag
     */
    private void attemptClaimDrag() {
        if (getParent() != null) {
            getParent().requestDisallowInterceptTouchEvent(true);
        }
    }

    void onStartTrackingTouch() {
        mIsDragging = true;
    }

    void onStopTrackingTouch() {
        mIsDragging = false;
    }

    public void setNotifyWhileDragging(boolean flag) {
        this.notifyWhileDragging = flag;
    }


    public OnUpdateSeekBarProgress getUpdateSeekBarProgress() {
        return updateSeekBarProgress;
    }

    public void setUpdateSeekBarProgress(OnUpdateSeekBarProgress updateSeekBarProgress) {
        this.updateSeekBarProgress = updateSeekBarProgress;
    }

    public enum Thumb {
        MIN, MAX, MOVE
    }


    public interface OnUpdateSeekBarProgress {
        void onRangeSeekBarValuesChanged(float minValue, float maxValue, long startTime, long endTime);
    }

    OnUpdateSeekBarProgressListener onUpdateSeekBarProgressListener;

    public void setOnUpdateSeekBarProgressListener(OnUpdateSeekBarProgressListener onUpdateSeekBarProgressListener) {
        this.onUpdateSeekBarProgressListener = onUpdateSeekBarProgressListener;
    }

    public interface OnUpdateSeekBarProgressListener {
        void onLeftChange(float left);

        void onRightChange(float right);

        void onMove(float left,float right);
    }


}
