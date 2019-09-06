package com.example.eric.datepicktest;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Author: yunhaoguo
 * Date: 2019-09-06
 */
public class LoopView extends View {

    private static final int DEFAULT_TEXT_SIZE = (int) (Resources.getSystem().getDisplayMetrics().density * 15);
    private static final int DEFAULT_VISIBLE_ITEMS = 9;

    public enum ACTION {
        CLICK, FLING, DRAG
    }

    // STRATEGY
    protected Handler handler;
    private GestureDetector flingGestureDetector;
    protected OnItemSelectedListener onItemSelectedListener;
    protected ScheduledExecutorService mExecutor = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> mFuture;

    // DISPLAYING
    private float scaleX = 1.05F;
    private Paint paintOuterText;
    private Paint paintCenterText;
    private Paint paintIndicator;
    private int textSize;
    int cellHeight;
    private int outerTextColor;
    private int centerTextColor;
    private int dividerColor;
    protected boolean isLoop;
    private int upperDividerY;
    private int lowerDividerY;
    protected int totalScrollY;
    private int itemsVisibleCount;
    private int textOffset;

    // DATA
    protected List<String> items;
    protected int initPosition;
    private int selectedItem;
    private int currentItemIndex;
    private int mOffset = 0;
    private float previousY;
    private long lastDownActionStartTime = 0;
    private Rect tempRect = new Rect();


    public LoopView(Context context) {
        super(context);
        initLoopView(context, null);
    }

    public LoopView(Context context, AttributeSet attributeset) {
        super(context, attributeset);
        initLoopView(context, attributeset);
    }

    public LoopView(Context context, AttributeSet attributeset, int defStyleAttr) {
        super(context, attributeset, defStyleAttr);
        initLoopView(context, attributeset);
    }

    /**
     * set outer text color
     *
     * @param centerTextColor
     */
    public void setCenterTextColor(int centerTextColor) {
        this.centerTextColor = centerTextColor;
        paintCenterText.setColor(centerTextColor);
    }

    /**
     * set center text color
     *
     * @param outerTextColor
     */
    public void setOuterTextColor(int outerTextColor) {
        this.outerTextColor = outerTextColor;
        paintOuterText.setColor(outerTextColor);
    }

    /**
     * set divider color
     *
     * @param dividerColor
     */
    public void setDividerColor(int dividerColor) {
        this.dividerColor = dividerColor;
        paintIndicator.setColor(dividerColor);
        paintIndicator.setStrokeWidth(3);
    }

    private void initLoopView(Context context, AttributeSet attributeset) {
        handler = new MessageHandler(this);
        flingGestureDetector = new GestureDetector(context, new LoopViewGestureListener(this));
        flingGestureDetector.setIsLongpressEnabled(false);

        TypedArray typedArray = context.obtainStyledAttributes(attributeset, R.styleable.LoopView);
        textSize = typedArray.getInteger(R.styleable.LoopView_textsize, DEFAULT_TEXT_SIZE);
        textSize = (int) (Resources.getSystem().getDisplayMetrics().density * textSize);
        centerTextColor = typedArray.getInteger(R.styleable.LoopView_centerTextColor, 0xff313131);
        outerTextColor = typedArray.getInteger(R.styleable.LoopView_outerTextColor, 0xffafafaf);
        dividerColor = typedArray.getInteger(R.styleable.LoopView_dividerTextColor, 0xffc5c5c5);
        itemsVisibleCount =
                typedArray.getInteger(R.styleable.LoopView_itemsVisibleCount, DEFAULT_VISIBLE_ITEMS);
        if (itemsVisibleCount % 2 == 0) {
            itemsVisibleCount = DEFAULT_VISIBLE_ITEMS;
        }
        isLoop = typedArray.getBoolean(R.styleable.LoopView_isLoop, true);
        typedArray.recycle();

        totalScrollY = 0;
        initPosition = -1;

        initPaints();
    }

    /**
     * visible item count, must be odd number
     *
     * @param visibleNumber
     */
    public void setItemsVisibleCount(int visibleNumber) {
        if (visibleNumber % 2 == 0) {
            return;
        }
        if (visibleNumber != itemsVisibleCount) {
            itemsVisibleCount = visibleNumber;
        }
    }

    private void initPaints() {
        paintOuterText = new Paint();
        paintOuterText.setColor(outerTextColor);
        paintOuterText.setAntiAlias(true);
        paintOuterText.setTextSize(textSize);
        paintOuterText.setFakeBoldText(false);
        paintOuterText.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));

        paintCenterText = new Paint();
        paintCenterText.setColor(centerTextColor);
        paintCenterText.setAntiAlias(true);
        paintCenterText.setTextScaleX(scaleX);
        paintCenterText.setTextSize(textSize);
        paintCenterText.setFakeBoldText(true);
        paintCenterText.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));

        paintIndicator = new Paint();
        paintIndicator.setColor(dividerColor);
        paintIndicator.setAntiAlias(true);

    }

    /**
     * measure之后重新计算格子的高度，分割线坐标
     */
    private void remeasure() {
        if (items == null) {
            return;
        }

        int measuredHeight = getMeasuredHeight();

        if (getMeasuredWidth() == 0 || measuredHeight == 0) {
            return;
        }

        paintCenterText.getTextBounds("\u661F\u671F", 0, 2, tempRect); // 星期
        cellHeight = measuredHeight / itemsVisibleCount;
        textOffset = cellHeight - (cellHeight - tempRect.height()) / 2;
        upperDividerY = (int) ((measuredHeight - cellHeight) / 2.0F);
        lowerDividerY = (int) ((measuredHeight + cellHeight) / 2.0F);
        if (initPosition == -1) {
            if (isLoop) {
                initPosition = (items.size() + 1) / 2;
            } else {
                initPosition = 0;
            }
        }

        currentItemIndex = initPosition;
    }

    /**
     * 滚动到整数位
     *
     * @param action
     */
    void smoothScroll(ACTION action) {
        cancelFuture();
        if (action == ACTION.FLING || action == ACTION.DRAG) {
            float itemHeight = cellHeight;
            mOffset = (int) (totalScrollY % itemHeight);
            if (mOffset < 0) {
                mOffset += itemHeight;
            }
            if ((float) mOffset > itemHeight / 2.0F) {
                mOffset = (int) (itemHeight - (float) mOffset);
            } else {
                mOffset = -mOffset;
            }
        }
        mFuture =
                mExecutor.scheduleWithFixedDelay(new SmoothScrollTimerTask(this, mOffset), 0, 10, TimeUnit.MILLISECONDS);
    }

    protected final void scrollBy(float velocityY) {
        cancelFuture();
        // change this number, can change fling speed
        int velocityFling = 10;
        mFuture = mExecutor.scheduleWithFixedDelay(new InertiaTimerTask(this, velocityY), 0, velocityFling,
                TimeUnit.MILLISECONDS);
    }

    public void cancelFuture() {
        if (mFuture != null && !mFuture.isCancelled()) {
            mFuture.cancel(true);
            mFuture = null;
        }
    }

    public void setIsLoop(boolean isLoop) {
        this.isLoop = isLoop;
    }

    /**
     * set text size in dp
     *
     * @param sizeInDp
     */
    public final void setTextSize(float sizeInDp) {
        if (sizeInDp > 0.0F) {
            textSize = (int) (getContext().getResources().getDisplayMetrics().density * sizeInDp);
            paintOuterText.setTextSize(textSize);
            paintCenterText.setTextSize(textSize);
        }
    }

    public final void setInitPosition(int initPosition) {
        if (initPosition < 0) {
            this.initPosition = 0;
        } else {
            if (items != null && items.size() > initPosition) {
                this.initPosition = initPosition;
            }
        }
    }

    public final void setListener(OnItemSelectedListener onItemSelectedListener) {
        this.onItemSelectedListener = onItemSelectedListener;
    }

    public final void setItems(List<String> items) {

        this.items = items;
        remeasure();
        invalidate();
    }

    public final int getSelectedItem() {
        int result = selectedItem % items.size();
        if (result < 0) {
            result += items.size();
        }
        return result;
    }

    protected final void onItemSelected() {
        if (onItemSelectedListener != null) {
            postDelayed(new OnItemSelectedRunnable(this), 200L);
        }
    }

    public void setScaleX(float scaleX) {
        this.scaleX = scaleX;
    }

    public void setCurrentPosition(int position) {
        if (items == null || items.isEmpty()) {
            return;
        }
        int size = items.size();
        if (position >= 0 && position < size && position != selectedItem) {
            initPosition = position;
            totalScrollY = 0;
            mOffset = 0;
            invalidate();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (items == null || items.size() == 0) {
            return;
        }

        int currentItemCountOffset = totalScrollY / cellHeight;
        currentItemIndex = initPosition + currentItemCountOffset % items.size();

        if (!isLoop) {
            if (currentItemIndex < 0) {
                currentItemIndex = 0;
            }
            if (currentItemIndex > items.size() - 1) {
                currentItemIndex = items.size() - 1;
            }
        } else {
            if (currentItemIndex < 0) {
                currentItemIndex = items.size() + currentItemIndex;
            }
            if (currentItemIndex > items.size() - 1) {
                currentItemIndex = currentItemIndex - items.size();
            }
        }

        int itemScrollOffset = totalScrollY % cellHeight;

        int rightX = getWidth() - getPaddingRight();

        canvas.drawLine(getPaddingLeft(), upperDividerY, rightX, upperDividerY, paintIndicator);
        canvas.drawLine(getPaddingLeft(), lowerDividerY, rightX, lowerDividerY, paintIndicator);

        int i = -1;
        while (i <= itemsVisibleCount) {
            int currentItemIndex = i + currentItemCountOffset + initPosition - itemsVisibleCount / 2;

            if (!isLoop && (currentItemIndex < 0 || currentItemIndex >= items.size())) {
                i++;
                continue;
            }
            canvas.save();
            float itemHeight = cellHeight;
            int translateY = cellHeight * i - itemScrollOffset;
            canvas.translate(0.0F, translateY);

            String content = getContent(currentItemIndex);
            if (translateY <= upperDividerY && cellHeight + translateY >= upperDividerY) {
                // first divider
                canvas.save();
                canvas.clipRect(0, 0, rightX, upperDividerY - translateY);
                canvas.drawText(content, getTextX(content, paintOuterText, tempRect),
                        textOffset, paintOuterText);
                canvas.restore();
                canvas.save();
                canvas.clipRect(0, upperDividerY - translateY, rightX, (int) (itemHeight));
                canvas.drawText(content, getTextX(content, paintCenterText, tempRect),
                        textOffset, paintCenterText);
                canvas.restore();
            } else if (translateY <= lowerDividerY && cellHeight + translateY >= lowerDividerY) {
                // second divider
                canvas.save();
                canvas.clipRect(0, 0, rightX, lowerDividerY - translateY);
                canvas.drawText(content, getTextX(content, paintCenterText, tempRect),
                        textOffset, paintCenterText);
                canvas.restore();
                canvas.save();
                canvas.clipRect(0, lowerDividerY - translateY, rightX, (int) (itemHeight));
                canvas.drawText(content, getTextX(content, paintOuterText, tempRect),
                        textOffset, paintOuterText);
                canvas.restore();
            } else if (translateY >= upperDividerY && cellHeight + translateY <= lowerDividerY) {
                // center item
                canvas.clipRect(0, 0, rightX, (int) (itemHeight));
                canvas.drawText(content, getTextX(content, paintCenterText, tempRect),
                        textOffset, paintCenterText);
            } else {
                // other item
                canvas.clipRect(0, 0, rightX, (int) (itemHeight));
                canvas.drawText(content, getTextX(content, paintOuterText, tempRect),
                        textOffset, paintOuterText);
            }
            canvas.restore();

            int centerY = translateY + cellHeight / 2;
            if (centerY >= upperDividerY && centerY <= lowerDividerY) {
                selectedItem = currentItemIndex;
            }

            i++;
        }
    }

    private String getContent(int currentItemIndex) {
        int index = (currentItemIndex) % items.size();
        if (index < 0) {
            index += items.size();
        }
        return items.get(index);
    }

    // text start drawing position
    private int getTextX(String a, Paint paint, Rect rect) {
        paint.getTextBounds(a, 0, a.length(), rect);
        int textWidth = rect.width();
        textWidth *= scaleX;
        return (getWidth() - getPaddingRight() - getPaddingLeft() - textWidth) / 2 + getPaddingLeft();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        remeasure();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Fling
        boolean eventConsumed = flingGestureDetector.onTouchEvent(event);
        float itemHeight = cellHeight;

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                cancelFuture();
                lastDownActionStartTime = System.currentTimeMillis();
                previousY = event.getRawY();
                if (getParent() != null) {
                    getParent().requestDisallowInterceptTouchEvent(true);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                float dy = previousY - event.getRawY();
                previousY = event.getRawY();
                totalScrollY = (int) (totalScrollY + dy);
                if (!isLoop) {
                    // cannot scroll out of border
                    float top = getMinScrollY();
                    float bottom = getMaxScrollY();

                    if (totalScrollY < top) {
                        totalScrollY = (int) top;
                    } else if (totalScrollY > bottom) {
                        totalScrollY = (int) bottom;
                    }
                }
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
            default:
                if (!eventConsumed) {
                    if ((System.currentTimeMillis() - lastDownActionStartTime) > 120) {
                        smoothScroll(ACTION.DRAG);
                    } else {
                        smoothScroll(ACTION.CLICK);
                        int circlePosition = (int) ((event.getY() + itemHeight / 2) / itemHeight);
                        float extraOffset = (totalScrollY % itemHeight + itemHeight) % itemHeight;
                        mOffset = (int) ((circlePosition - itemsVisibleCount / 2) * itemHeight - extraOffset);
                    }
                }
                if (getParent() != null) {
                    getParent().requestDisallowInterceptTouchEvent(false);
                }
                invalidate();
                break;
        }
        return true;
    }

    public float getMaxScrollY() {
        return (items.size() - initPosition - 1) * cellHeight;
    }

    public float getMinScrollY() {
        return -initPosition * cellHeight;
    }

    public void setCenterTextBold(boolean isBold) {
        paintCenterText.setFakeBoldText(isBold);
    }
}

