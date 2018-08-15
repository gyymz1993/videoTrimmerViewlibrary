package com.video.cut.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.os.Handler;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.video.cut.R;
import com.video.cut.features.trim.VideoEditTrimmerAdapter;
import com.video.cut.interfaces.IVideoTrimmerView;
import com.video.cut.interfaces.SingleCallback;
import com.video.cut.interfaces.TrimVideoListener;
import com.video.cut.models.BaseLetterBean;
import com.video.cut.utils.BackgroundExecutor;
import com.video.cut.utils.DeviceUtil;
import com.video.cut.utils.TrimVideoUtil;
import com.video.cut.utils.UiThreadExecutor;
import com.video.cut.utils.UnitConverter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class TrimmerView extends FrameLayout implements IVideoTrimmerView {
    private static final String TAG = VideoEditTrimmerView.class.getSimpleName();
    public static long MAX_SHOOT_DURATION = 10 * 1000L;//视频最多剪切多长时间10s
    public Handler handler = new Handler();
    public VideoState videoState = VideoState.PLAY;
    /**
     * 是否处于编辑状态
     */
    protected boolean isEditState;
    protected int maxWidth;
    protected long getCurrentPosition;
    EditRangeSeekBarView.OnUpdateSeekBarProgress onUpdateSeekBarProgress;
    private int criticalPoint = DeviceUtil.getDeviceWidth() / 2 - UnitConverter.dpToPx(35);
    private Context mContext;
    private int mMaxWidth = DeviceUtil.getDeviceWidth();
    private OnScrollToVideoProgress onScrollToVideoProgress;
    private ScrollListenerRecyclerView mVideoThumbRecyclerView;
    private LinearLayout mSeekBarLayout;
    private VideoEditTrimmerAdapter mVideoThumbAdapter;
    private TrimVideoListener mOnTrimVideoListener;
    private int mThumbsTotalCount;
    private EditRangeSeekBarView mRangeSeekBarView;
    private BaseLetterBean mLetterBean;
    private RelativeLayout ryHotLayout;
    private CustomHorizontalScrollView nestedScrollView;
    private List<ImageView> imageViewHots = new ArrayList<>();
    private ImageView currentImage;
    private int rvScrollx;
    private TextView id_tv_progress;
    private float mAverageMsPx;//每毫秒所占的px
    private int mDuration = 0;
    private long scrollPos = 0;

    public TrimmerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TrimmerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public EditRangeSeekBarView getRangeSeekBarView() {
        return mRangeSeekBarView;
    }

    public void setInitLetterBean(BaseLetterBean letterBean) {
        mLetterBean = letterBean;
        mRangeSeekBarView.updataRandSeekBar(letterBean);
        ImageView imageView = new ImageView(getContext());
        imageView.setImageResource(R.drawable.shape_dot);
        imageView.setId(letterBean.getId());
        currentImage = imageView;
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(UnitConverter.dpToPx(5), UnitConverter.dpToPx(5));
        params.leftMargin = (int) Math.ceil(getCurrentPosition / mAverageMsPx + criticalPoint - UnitConverter.dpToPx(2));
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        ryHotLayout.addView(imageView, params);
        imageViewHots.add(imageView);
        nestedScrollView.smoothScrollTo(params.leftMargin, 0);
    }

    public void setUpdataBean(BaseLetterBean letterBean) {
        if (imageViewHots.size() != 0 && imageViewHots.size() > 0) {
            for (ImageView imageView : imageViewHots) {
                if (imageView.getId() == letterBean.getId()) {
                    currentImage = imageView;
                }
            }
        }
        mLetterBean = letterBean;
        mRangeSeekBarView.initRandSeekBarInit(letterBean);
    }

    public void updataImageViewPosition(float left) {
        if (currentImage != null) {
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) currentImage.getLayoutParams();
            layoutParams.leftMargin = (int) Math.ceil(left + rvScrollx);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            currentImage.setLayoutParams(layoutParams);
        }
    }

    public void getSeekBarMinWidth(final float scrollX) {
        int seekBarMinWidth = (int) (UnitConverter.getDisplayMetrics().widthPixels / 2 - UnitConverter.dpToPx(35) - scrollX);
        int seekBarmaxWidth = (int) (UnitConverter.getDisplayMetrics().widthPixels / 2 - UnitConverter.dpToPx(35) + maxWidth - scrollX);
        if (mRangeSeekBarView == null) return;
        getCurrentPosition = (long) (mAverageMsPx * scrollX);
        mRangeSeekBarView.setMaxLeftWidth(seekBarMinWidth);
        mRangeSeekBarView.setMaxRightWidth(seekBarmaxWidth);
        mRangeSeekBarView.onMove(getCurrentPosition);
        this.post(new Runnable() {
            @Override
            public void run() {
                nestedScrollView.scrollTo((int) scrollX, 0);
            }
        });
    }

    public ScrollListenerRecyclerView getmVideoThumbRecyclerView() {
        return mVideoThumbRecyclerView;
    }

    public float getAverageMsPx() {
        return mAverageMsPx;
    }

    private void init(Context context) {
        this.mContext = context;
        LayoutInflater.from(context).inflate(R.layout.trimmer_view, this, true);
        id_tv_progress = findViewById(R.id.id_tv_progress);
        mSeekBarLayout = findViewById(R.id.seekBarLayout);
        ryHotLayout = findViewById(R.id.id_ry_hot);
        nestedScrollView = findViewById(R.id.id_nestedScrollView);
        mVideoThumbRecyclerView = findViewById(R.id.video_frames_recyclerView);
        mVideoThumbRecyclerView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false));
        mVideoThumbAdapter = new VideoEditTrimmerAdapter(mContext);
        mVideoThumbAdapter.addHeaderView();
        mVideoThumbAdapter.addFooterView();
        mVideoThumbRecyclerView.setNestedScrollingEnabled(false);
        mVideoThumbRecyclerView.setAdapter(mVideoThumbAdapter);
        mVideoThumbRecyclerView.setRecyclerScrollListener(new ScrollListenerRecyclerView.RecyclerScrollChangeListener() {
            @Override
            public void ScrollChange(int x) {
                rvScrollx = x;
                scrollPos = (long) (mAverageMsPx * x);
                if (scrollPos > mDuration) {
                    scrollPos = mDuration;
                }
                getSeekBarMinWidth(x);
                double progress = Math.ceil(scrollPos / 10.0 / 10.0) / 10;
                id_tv_progress.setText(progress + "");
                if (getVideoState().equals(VideoState.PAUSE) && onScrollToVideoProgress != null) {
                    onScrollToVideoProgress.progress(scrollPos);
                }
            }

        });
        mVideoThumbRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {//拖动中
                    setVideoState(VideoState.PAUSE);
                }
            }
        });
    }

    private void initRangeSeekBarView() {
        float rangeWidth;
        if (mDuration <= MAX_SHOOT_DURATION) {
            mThumbsTotalCount = TrimVideoUtil.MAX_COUNT_RANGE;
            rangeWidth = mMaxWidth;
        } else {
            mThumbsTotalCount = (int) (mDuration * 1.0f / (MAX_SHOOT_DURATION * 1.0f) * TrimVideoUtil.MAX_COUNT_RANGE);
            rangeWidth = mMaxWidth * 1.0f / TrimVideoUtil.MAX_COUNT_RANGE * mThumbsTotalCount;
        }
        mAverageMsPx = mDuration * 1.0f / rangeWidth;
        maxWidth = (int) Math.ceil(DeviceUtil.getDeviceWidth() / TrimVideoUtil.MAX_COUNT_RANGE) * mThumbsTotalCount;
        ViewGroup.LayoutParams layoutParams = ryHotLayout.getLayoutParams();
        layoutParams.width = maxWidth + 2 * criticalPoint;
        addSeekBarView(mLetterBean);
    }

    /**
     * 编辑状态显示Seekbar
     */
    public void setEditState(boolean editState) {
        isEditState = editState;
        refrshEditState();
    }

    public void refrshEditState() {
        if (getRangeSeekBarView() == null) return;
        if (isEditState) {
            getRangeSeekBarView().setVisibility(View.VISIBLE);
        } else {
            getRangeSeekBarView().setVisibility(View.INVISIBLE);
        }
    }

    public void addSeekBarView(BaseLetterBean letterBean) {
        if (letterBean == null) {
            letterBean = new BaseLetterBean();
        }
        mRangeSeekBarView = new EditRangeSeekBarView(mContext, mAverageMsPx, letterBean);
        mRangeSeekBarView.setmAverageMsPx(mAverageMsPx);
        mRangeSeekBarView.setMaxLeftWidth(criticalPoint);
        mRangeSeekBarView.setMaxRightWidth(maxWidth);
        /**
         * 手指抬起更新数据
         */
        mRangeSeekBarView.setUpdateSeekBarProgress(new EditRangeSeekBarView.OnUpdateSeekBarProgress() {
            @Override
            public void onRangeSeekBarValuesChanged(float minValue, float maxValue, long startTime, long endTime) {
                if (minValue >= criticalPoint) {
                    startTime = (long) ((minValue - criticalPoint + scrollPos) * mAverageMsPx);
                }
            }
        });
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        mSeekBarLayout.addView(mRangeSeekBarView, layoutParams);
        mSeekBarLayout.setVisibility(VISIBLE);
        mRangeSeekBarView.setVisibility(INVISIBLE);
        mRangeSeekBarView.invalidate();
        mRangeSeekBarView.setUpdateSeekBarProgress(new EditRangeSeekBarView.OnUpdateSeekBarProgress() {
            @Override
            public void onRangeSeekBarValuesChanged(float minValue, float maxValue, long startTime, long endTime) {

            }
        });
        mRangeSeekBarView.setOnUpdateSeekBarProgressListener(new EditRangeSeekBarView.OnUpdateSeekBarProgressListener() {
            @Override
            public void onLeftChange(float left) {
                updataImageViewPosition(left);
            }

            @Override
            public void onRightChange(float right) {
            }

            @Override
            public void onMove(float left, float right) {
                updataImageViewPosition(left);
            }
        });
    }

    public void setOnUpdateSeekBarProgress(EditRangeSeekBarView.OnUpdateSeekBarProgress onUpdateSeekBarProgress) {
        this.onUpdateSeekBarProgress = onUpdateSeekBarProgress;
        mRangeSeekBarView.setUpdateSeekBarProgress(onUpdateSeekBarProgress);
    }

    public void hideRangeSeekBarView() {
        mSeekBarLayout.setVisibility(GONE);
    }

    public void setProgress(long currentPosition) {
        float v = currentPosition / mAverageMsPx;
    }

    public void initVideoByURI(final String videoUrl) {
        MediaMetadataRetriever metadataRetriever = new MediaMetadataRetriever();
        if (videoUrl.startsWith("http") || videoUrl.startsWith("https")) {
            metadataRetriever.setDataSource(videoUrl, new HashMap());
        } else {
            metadataRetriever.setDataSource(videoUrl);
        }
        mDuration = Integer.valueOf(metadataRetriever.extractMetadata(9));
        //得到秒
        initRangeSeekBarView();
        startShootVideoThumbs(mContext, videoUrl, mThumbsTotalCount, 0, mDuration);
    }


    private void startShootVideoThumbs(final Context context, final String videoUri, final int totalThumbsCount, long startPosition, long endPosition) {
        TrimVideoUtil.backgroundShootVideoThumb(context, videoUri, totalThumbsCount, startPosition, endPosition,
                new SingleCallback<Bitmap, Integer>() {
                    @Override
                    public void onSingleCallback(final Bitmap bitmap, final Integer interval) {
                        UiThreadExecutor.runTask("", new Runnable() {
                            @Override
                            public void run() {
                                Bitmap b;
                                if (totalThumbsCount <= mVideoThumbAdapter.getmBitmaps().size()) {
                                    return;
                                } else {
                                    if (null == bitmap) {
                                        b = mVideoThumbAdapter.getmBitmaps().get(mVideoThumbAdapter.getmBitmaps().size() - 1);
                                    } else {
                                        b = bitmap;
                                    }
                                    mVideoThumbAdapter.addBitmaps(b);
                                }
                            }
                        }, 0L);
                    }
                });
    }

    public void onCancelClicked() {
        if (mOnTrimVideoListener != null) {
            mOnTrimVideoListener.onCancel();
        }
    }


    /**
     * 水平滑动了多少px
     */
    public int calcScrollXDistance() {
        LinearLayoutManager layoutManager = (LinearLayoutManager) mVideoThumbRecyclerView.getLayoutManager();
        int position = layoutManager.findFirstVisibleItemPosition();
        View firstVisibleChildView = layoutManager.findViewByPosition(position);
        int itemWidth = firstVisibleChildView.getWidth();
        //如果大于1加上前面head的宽度
        if (position >= 1) {
            return (position) * itemWidth - firstVisibleChildView.getLeft() + UnitConverter.getDisplayMetrics().widthPixels / 2 - (int) Math.ceil(DeviceUtil.getDeviceWidth() / TrimVideoUtil.MAX_COUNT_RANGE);
        } else {
            return (position) * itemWidth - firstVisibleChildView.getLeft();
        }
    }

    /**
     * Cancel trim thread execut action when finish
     */
    @Override
    public void onDestroy() {
        mOnTrimVideoListener = null;
        BackgroundExecutor.cancelAll("", true);
        UiThreadExecutor.cancelAll("");
    }

    public void setOnScrollToVideoProgress(OnScrollToVideoProgress onScrollToVideoProgress) {
        this.onScrollToVideoProgress = onScrollToVideoProgress;
    }


    public void setOnTrimVideoListener(TrimVideoListener onTrimVideoListener) {
        mOnTrimVideoListener = onTrimVideoListener;
    }

    public VideoState getVideoState() {
        return videoState;
    }

    public void setVideoState(VideoState videoState) {
        this.videoState = videoState;
    }

    public enum VideoState {
        PLAY, PAUSE
    }


    public interface OnScrollToVideoProgress {
        void progress(long progress);
    }

}
