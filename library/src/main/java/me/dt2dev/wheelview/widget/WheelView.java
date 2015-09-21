package me.dt2dev.wheelview.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.DecelerateInterpolator;
import android.widget.Scroller;

/**
 * Created by darktiny on 9/17/15.
 */
public class WheelView extends View {

    private static final int DEFAULT_ITEM_COUNT = 3;
    private static final int DEFAULT_ITEM_HEIGHT = 48;
    private static final int DEFAULT_DIVIDER_HEIGHT = 2;
    private static final int DEFAULT_TEXT_SIZE = 18;

    private String[] mValues;
    private int mSelection;
    private OnSelectionChangedListener mListener;

    private int mItemCount;
    private int mItemWidth;
    private int mItemHeight;
    private int mDividerHeight;
    private int mTopDividerPos;
    private int mBottomDividerPos;

    private TextPaint mTextPaint;
    private TextPaint mFadeTextPaint;
    private Drawable mDividerDrawable;
    private Rect mTextBounds = new Rect();

    private float mLastMoveY;
    private int mCurrentScrollOffset;
    private int mMinFlingVelocity;
    private int mMaxFlingVelocity;
    private int mPreviousScrollerY;
    private Scroller mFlingScroller;
    private Scroller mAdjustScroller;
    private VelocityTracker mVelocityTracker;

    public WheelView(Context context) {
        this(context, null);
    }

    public WheelView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WheelView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public WheelView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        Resources resources = getResources();
        TypedArray typedArray = context.obtainStyledAttributes(
                attrs, R.styleable.WheelView, defStyleAttr, defStyleRes
        );

        mItemCount = typedArray.getInt(R.styleable.WheelView_itemCount, DEFAULT_ITEM_COUNT);
        int defItemHeight = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, DEFAULT_ITEM_HEIGHT, resources.getDisplayMetrics()
        );
        mItemHeight = typedArray.getDimensionPixelSize(R.styleable.WheelView_itemHeight, defItemHeight);
        int defDividerHeight = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, DEFAULT_DIVIDER_HEIGHT, resources.getDisplayMetrics()
        );
        mDividerHeight = typedArray.getDimensionPixelSize(R.styleable.WheelView_dividerHeight, defDividerHeight);
        mTopDividerPos = mItemHeight * (mItemCount / 2) - mDividerHeight / 2;
        mBottomDividerPos = mItemHeight * (mItemCount / 2 + 1) - mDividerHeight / 2;

        int defTextColorNormal = CompatUtil.getColor(context, R.color.wheel_view_default_text_color_normal);
        int defTextColorFading = CompatUtil.getColor(context, R.color.wheel_view_default_text_color_fading);
        int defTextSize = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP, DEFAULT_TEXT_SIZE, resources.getDisplayMetrics()
        );

        mTextPaint = new TextPaint();
        mTextPaint.setAntiAlias(true);
        mTextPaint.setColor(typedArray.getColor(R.styleable.WheelView_textColorNormal, defTextColorNormal));
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mTextPaint.setTextSize(typedArray.getDimensionPixelSize(R.styleable.WheelView_android_textSize, defTextSize));
        mFadeTextPaint = new TextPaint(mTextPaint);
        mFadeTextPaint.setColor(typedArray.getColor(R.styleable.WheelView_textColorFading, defTextColorFading));

        int defDividerColor = CompatUtil.getColor(context, R.color.wheel_view_default_divider_color);
        mDividerDrawable = new ColorDrawable(typedArray.getColor(R.styleable.WheelView_dividerColor, defDividerColor));

        typedArray.recycle();

        mFlingScroller = new Scroller(getContext(), null, true);
        mAdjustScroller = new Scroller(getContext(), new DecelerateInterpolator(2.5f));

        ViewConfiguration configuration = ViewConfiguration.get(context);
        mMinFlingVelocity = configuration.getScaledMinimumFlingVelocity();
        mMaxFlingVelocity = configuration.getScaledMaximumFlingVelocity();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = mItemHeight * mItemCount;
        mItemWidth = Math.min(mItemWidth, width);
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float centerX = getWidth() / 2.0f;
        float y = mCurrentScrollOffset + mItemHeight / 2f;

        for (String value : mValues) {
            mTextPaint.getTextBounds(value, 0, value.length(), mTextBounds);
            float textTop = y - mTextBounds.height() / 2f;
            float textBottom = y + mTextBounds.height() / 2f;
            if (textBottom > 0 && textTop < getHeight()) {
                if (y >= mTopDividerPos && y < mBottomDividerPos) {
                    canvas.drawText(getEllipsizedString(value, mTextPaint), centerX, textBottom, mTextPaint);
                } else {
                    canvas.drawText(getEllipsizedString(value, mFadeTextPaint), centerX, textBottom, mFadeTextPaint);
                }
            }
            y += mItemHeight;
        }

        int left = (int) (centerX - mItemWidth / 2.0f);
        int right = (int) (centerX + mItemWidth / 2.0f);
        mDividerDrawable.setBounds(left, mTopDividerPos, right, mTopDividerPos + mDividerHeight);
        mDividerDrawable.draw(canvas);
        mDividerDrawable.setBounds(left, mBottomDividerPos, right, mBottomDividerPos + mDividerHeight);
        mDividerDrawable.draw(canvas);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(event);
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                if (!mFlingScroller.isFinished() || !mAdjustScroller.isFinished()) {
                    mFlingScroller.forceFinished(true);
                    mAdjustScroller.forceFinished(true);
                }
                mLastMoveY = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                float currentMoveY = event.getY();
                int deltaMoveY = (int) (currentMoveY - mLastMoveY);
                scrollBy(0, deltaMoveY);
                invalidate();
                mLastMoveY = currentMoveY;
                break;
            case MotionEvent.ACTION_UP:
                mVelocityTracker.computeCurrentVelocity(1000, mMaxFlingVelocity);
                int currentYVelocity = (int) mVelocityTracker.getYVelocity();
                if (Math.abs(currentYVelocity) > mMinFlingVelocity) {
                    fling(currentYVelocity);
                } else {
                    ensureScrollWheelAdjusted();
                }
                break;
            default:
                break;
        }
        return true;
    }

    @Override
    public void scrollBy(int x, int y) {
        mCurrentScrollOffset += y;

        int maxScrollOffset = mItemHeight * (mItemCount / 2);
        if (mCurrentScrollOffset > maxScrollOffset) {
            mCurrentScrollOffset = maxScrollOffset;
        }

        int minScrollOffset = -(getCount() - mItemCount / 2 - 1) * mItemHeight;
        if (mCurrentScrollOffset < minScrollOffset) {
            mCurrentScrollOffset = minScrollOffset;
        }

        if (mListener != null) {
            int newSelection = (int) (mItemCount / 2 - (float) mCurrentScrollOffset / (float) mItemHeight + 0.5f);
            if (newSelection != mSelection) {
                mSelection = newSelection;
                mListener.onSelectionChanged(this, newSelection);
            }
        }
    }

    @Override
    public void computeScroll() {
        Scroller scroller = mFlingScroller;
        if (scroller.isFinished()) {
            scroller = mAdjustScroller;
            if (scroller.isFinished()) {
                return;
            }
        }
        scroller.computeScrollOffset();
        int currentScrollerY = scroller.getCurrY();
        if (mPreviousScrollerY == 0) {
            mPreviousScrollerY = scroller.getStartY();
        }
        scrollBy(0, currentScrollerY - mPreviousScrollerY);
        mPreviousScrollerY = currentScrollerY;
        if (scroller.isFinished()) {
            if (scroller == mFlingScroller) {
                ensureScrollWheelAdjusted();
            }
        } else {
            invalidate();
        }
    }

    private void fling(int velocityY) {
        mPreviousScrollerY = 0;
        if (velocityY > 0) {
            mFlingScroller.fling(0, 0, 0, velocityY, 0, 0, 0, Integer.MAX_VALUE);
        } else {
            mFlingScroller.fling(0, Integer.MAX_VALUE, 0, velocityY, 0, 0, 0, Integer.MAX_VALUE);
        }
        invalidate();
    }

    private void ensureScrollWheelAdjusted() {
        int deltaY = mCurrentScrollOffset % mItemHeight;
        if (deltaY != 0) {
            mPreviousScrollerY = 0;
            if (Math.abs(deltaY) > mItemHeight / 2) {
                deltaY = -deltaY + (deltaY > 0 ? mItemHeight : -mItemHeight);
            } else {
                deltaY = -deltaY;
            }
            mAdjustScroller.startScroll(0, 0, 0, deltaY, 800);
            invalidate();
        }
    }

    private int getCount() {
        return mValues == null ? 0 : mValues.length;
    }

    private String getEllipsizedString(String value, TextPaint paint) {
        return TextUtils.ellipsize(value, paint, mItemWidth, TextUtils.TruncateAt.END).toString();
    }

    public int getSelection() {
        return mSelection;
    }

    public void setValues(String[] values, int selection) {
        this.mValues = values;
        this.mSelection = selection;

        mItemWidth = 0;
        for (String value : values) {
            float width = mTextPaint.measureText(value);
            if (width > mItemWidth) {
                mItemWidth = (int) width;
            }
        }

        if (selection < 0 || selection >= values.length) {
            selection = 0;
        }
        mCurrentScrollOffset = (mItemCount / 2 - selection) * mItemHeight;

        invalidate();
    }

    public void setOnSelectionChangedListener(OnSelectionChangedListener listener) {
        this.mListener = listener;
    }

    public interface OnSelectionChangedListener {
        void onSelectionChanged(WheelView view, int selection);
    }
}
