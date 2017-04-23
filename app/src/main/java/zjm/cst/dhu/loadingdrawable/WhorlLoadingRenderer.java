package zjm.cst.dhu.loadingdrawable;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.util.Log;
import android.view.animation.Interpolator;

/**
 * Created by zjm on 2017/4/22.
 */

public class WhorlLoadingRenderer extends LoadingRenderer {
    //贝塞尔变化的Interpolator，规律是慢快慢
    private static final Interpolator MATERIAL_INTERPOLATOR = new FastOutSlowInInterpolator();
    private static final int DEGREE_180 = 180;
    private static final int DEGREE_360 = 360;
    //循环次数
    private static final int NUM_POINTS = 5;
    //单次绘制所占角度
    private static final float MAX_SWIPE_DEGREES = 0.6f * DEGREE_360;
    //一次循环所占角度
    private static final float FULL_GROUP_ROTATION = 3.0f * DEGREE_360;
    //起点绘制结束时进度
    private static final float START_TRIM_DURATION_OFFSET = 0.5f;
    //终点绘制结束时进度
    private static final float END_TRIM_DURATION_OFFSET = 1.0f;
    //圆半径
    private static final float DEFAULT_CENTER_RADIUS = 12.5f;
    //所画线宽度
    private static final float DEFAULT_STROKE_WIDTH = 2.5f;
    //颜色
    private static final int[] DEFAULT_COLORS = new int[]{
            Color.RED, Color.GREEN, Color.BLUE
    };
    private final Paint mPaint = new Paint();
    private final RectF mTempBounds = new RectF();
    private final RectF mTempArcBounds = new RectF();
    //当前循环位置
    private float mRotationCount;

    private final Animator.AnimatorListener mAnimatorListener = new AnimatorListenerAdapter() {
        @Override
        public void onAnimationStart(Animator animation) {
            super.onAnimationStart(animation);
            //动画开始，循环位置为0
            mRotationCount = 0;
        }

        //一次绘制结束，进行下一次
        @Override
        public void onAnimationRepeat(Animator animator) {
            super.onAnimationRepeat(animator);
            //保存上一次位置
            storeOriginals();
            //下一次起点为上一次终点
            mStartDegrees = mEndDegrees;
            //当前循环位置
            mRotationCount = (mRotationCount + 1) % (NUM_POINTS);
        }
    };

    private int[] mColors;
    //内边距
    private float mStrokeInset;
    //画布旋转角度
    private float mGroupRotation;
    //终点角度
    private float mEndDegrees;
    //起点角度
    private float mStartDegrees;
    //扫过角度
    private float mSwipeDegrees;
    //上一次终点角度
    private float mOriginEndDegrees;
    //上一次起点角度
    private float mOriginStartDegrees;
    //所画线宽度
    private float mStrokeWidth;
    //圆半径
    private float mCenterRadius;

    private WhorlLoadingRenderer(Context context) {
        super(context);
        init(context);
        setupPaint();
        addRenderListener(mAnimatorListener);
    }

    private void init(Context context) {
        mColors = DEFAULT_COLORS;
        mStrokeWidth = dip2px(context, DEFAULT_STROKE_WIDTH);
        mCenterRadius = dip2px(context, DEFAULT_CENTER_RADIUS);
        initStrokeInset(mWidth, mHeight);
    }

    private float dip2px(Context context, float dpValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        return dpValue * scale;
    }

    //计算内边距，使动画居中
    private void initStrokeInset(float width, float height) {
        float minSize = Math.min(width, height);
        float strokeInset = minSize / 2.0f - mCenterRadius;
        float minStrokeInset = (float) Math.ceil(mStrokeWidth / 2.0f);
        mStrokeInset = strokeInset < minStrokeInset ? minStrokeInset : strokeInset;
    }

    private void setupPaint() {
        //平滑画边
        mPaint.setAntiAlias(true);
        //设置画笔粗度
        mPaint.setStrokeWidth(mStrokeWidth);
        //用划的方式进行画
        mPaint.setStyle(Paint.Style.STROKE);
        //设置画笔头类型，半圆
        mPaint.setStrokeCap(Paint.Cap.ROUND);
    }

    private void reAdjustBound() {
        float boundHeight = mBounds.bottom - mBounds.top;
        float boundWidth = mBounds.right - mBounds.left;
        if (boundHeight < mHeight || boundWidth < mWidth) {
            mHeight = boundHeight;
            mWidth = boundWidth;
            initStrokeInset(mHeight, mWidth);
        }
        if (boundHeight > mHeight || boundWidth > mWidth) {
            Rect rect = new Rect(
                    (int) ((boundWidth - mWidth) / 2 + mBounds.left),
                    (int) ((boundHeight - mHeight) / 2 + mBounds.top),
                    (int) ((boundWidth + mWidth) / 2 + mBounds.left),
                    (int) ((boundHeight + mHeight) / 2 + mBounds.top));
            mBounds.set(rect);
        }
    }

    @Override
    protected void draw(Canvas canvas) {
        int saveCount = canvas.save();
        reAdjustBound();
        //设置缓存区域及内边距
        mTempBounds.set(mBounds);
        mTempBounds.inset(mStrokeInset, mStrokeInset);
        //以画布中心为中心进行画布旋转
        canvas.rotate(mGroupRotation, mTempBounds.centerX(), mTempBounds.centerY());
        if (mSwipeDegrees != 0) {
            //针对每一个线
            for (int i = 0; i < mColors.length; i++) {
                //序号越大，线越细，也就是说最外部是序号0
                mPaint.setStrokeWidth(mStrokeWidth / (i + 1));
                mPaint.setColor(mColors[i]);
                //画弧，其中第四个参数为是否画出半径
                canvas.drawArc(createArcBounds(mTempBounds, i), mStartDegrees + DEGREE_180 * (i % 2),
                        mSwipeDegrees, false, mPaint);
            }
        }
        canvas.restoreToCount(saveCount);
    }

    //针对每个线确定画布区域
    private RectF createArcBounds(RectF sourceArcBounds, int index) {
        int intervalWidth = 0;
        for (int i = 0; i < index; i++) {
            //两条线间区域差为1.5倍上一个线宽度
            intervalWidth += mStrokeWidth / (i + 1.0f) * 1.5f;
        }
        //确定新区域
        int arcBoundsLeft = (int) (sourceArcBounds.left + intervalWidth);
        int arcBoundsTop = (int) (sourceArcBounds.top + intervalWidth);
        int arcBoundsRight = (int) (sourceArcBounds.right - intervalWidth);
        int arcBoundsBottom = (int) (sourceArcBounds.bottom - intervalWidth);
        mTempArcBounds.set(arcBoundsLeft, arcBoundsTop, arcBoundsRight, arcBoundsBottom);
        return mTempArcBounds;
    }

    @Override
    protected void computeRender(float renderProgress) {
        //当目前变化进度小于起点结束进度
        if (renderProgress <= START_TRIM_DURATION_OFFSET) {
            //起点移动进度=当前变化进度/起点变化进度
            float startTrimProgress = (renderProgress) / (1.0f - START_TRIM_DURATION_OFFSET);
            //起点应该移动后的新角度=原角度+一次绘制角度*转换后起点应该移动的进度
            mStartDegrees = mOriginStartDegrees + MAX_SWIPE_DEGREES * MATERIAL_INTERPOLATOR.getInterpolation(startTrimProgress);
        }

        //当目前变化进度大于起点结束进度
        if (renderProgress > START_TRIM_DURATION_OFFSET) {
            //终点移动进度=当前变化进度/终点变化进度
            float endTrimProgress = (renderProgress - START_TRIM_DURATION_OFFSET) / (END_TRIM_DURATION_OFFSET - START_TRIM_DURATION_OFFSET);
            //终点应该移动后的新角度=原角度+一次绘制角度*转换后终点应该移动的进度
            mEndDegrees = mOriginEndDegrees + MAX_SWIPE_DEGREES * MATERIAL_INTERPOLATOR.getInterpolation(endTrimProgress);
        }
        //生成变化角度
        if (Math.abs(mEndDegrees - mStartDegrees) > 0) {
            mSwipeDegrees = mEndDegrees - mStartDegrees;
        }
        //生成画布旋转角度
        mGroupRotation = ((FULL_GROUP_ROTATION / NUM_POINTS) * renderProgress) + (FULL_GROUP_ROTATION * (mRotationCount / NUM_POINTS));
    }

    @Override
    protected void setAlpha(int alpha) {
        mPaint.setAlpha(alpha);
    }

    @Override
    protected void setColorFilter(ColorFilter cf) {
        mPaint.setColorFilter(cf);
    }

    @Override
    protected void reset() {
        resetOriginals();
    }


    private void storeOriginals() {
        mOriginEndDegrees = mEndDegrees;
        mOriginStartDegrees = mEndDegrees;
    }

    private void resetOriginals() {
        mOriginEndDegrees = 0;
        mOriginStartDegrees = 0;
        mEndDegrees = 0;
        mStartDegrees = 0;
        mSwipeDegrees = 0;
    }

    //应用变化
    private void apply(Builder builder) {
        this.mWidth = builder.mWidth > 0 ? builder.mWidth : this.mWidth;
        this.mHeight = builder.mHeight > 0 ? builder.mHeight : this.mHeight;
        this.mStrokeWidth = builder.mStrokeWidth > 0 ? builder.mStrokeWidth : this.mStrokeWidth;
        this.mCenterRadius = builder.mCenterRadius > 0 ? builder.mCenterRadius : this.mCenterRadius;
        this.mDuration = builder.mDuration > 0 ? builder.mDuration : this.mDuration;
        this.mColors = builder.mColors != null && builder.mColors.length > 0 ? builder.mColors : this.mColors;
        setupPaint();
        initStrokeInset(this.mWidth, this.mHeight);
    }


    public static class Builder {
        private Context mContext;
        private int mWidth;
        private int mHeight;
        private int mStrokeWidth;
        private int mCenterRadius;
        private int mDuration;
        private int[] mColors;

        public Builder(Context mContext) {
            this.mContext = mContext;
        }

        public Builder setWidth(int width) {
            this.mWidth = width;
            return this;
        }

        public Builder setHeight(int height) {
            this.mHeight = height;
            return this;
        }

        public Builder setStrokeWidth(int strokeWidth) {
            this.mStrokeWidth = strokeWidth;
            return this;
        }

        public Builder setCenterRadius(int centerRadius) {
            this.mCenterRadius = centerRadius;
            return this;
        }

        public Builder setDuration(int duration) {
            this.mDuration = duration;
            return this;
        }

        public Builder setColors(int[] colors) {
            this.mColors = colors;
            return this;
        }

        public WhorlLoadingRenderer build() {
            WhorlLoadingRenderer loadingRenderer = new WhorlLoadingRenderer(mContext);
            loadingRenderer.apply(this);
            return loadingRenderer;
        }
    }

}


