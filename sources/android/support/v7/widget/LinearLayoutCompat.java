package android.support.v7.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.annotation.RestrictTo;
import android.support.annotation.RestrictTo.Scope;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.InputDeviceCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.appcompat.C0249R;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class LinearLayoutCompat extends ViewGroup {
    public static final int HORIZONTAL = 0;
    private static final int INDEX_BOTTOM = 2;
    private static final int INDEX_CENTER_VERTICAL = 0;
    private static final int INDEX_FILL = 3;
    private static final int INDEX_TOP = 1;
    public static final int SHOW_DIVIDER_BEGINNING = 1;
    public static final int SHOW_DIVIDER_END = 4;
    public static final int SHOW_DIVIDER_MIDDLE = 2;
    public static final int SHOW_DIVIDER_NONE = 0;
    public static final int VERTICAL = 1;
    private static final int VERTICAL_GRAVITY_COUNT = 4;
    private boolean mBaselineAligned;
    private int mBaselineAlignedChildIndex;
    private int mBaselineChildTop;
    private Drawable mDivider;
    private int mDividerHeight;
    private int mDividerPadding;
    private int mDividerWidth;
    private int mGravity;
    private int[] mMaxAscent;
    private int[] mMaxDescent;
    private int mOrientation;
    private int mShowDividers;
    private int mTotalLength;
    private boolean mUseLargestChild;
    private float mWeightSum;

    @RestrictTo({Scope.LIBRARY_GROUP})
    @Retention(RetentionPolicy.SOURCE)
    public @interface DividerMode {
    }

    public static class LayoutParams extends MarginLayoutParams {
        public int gravity;
        public float weight;

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
            this.gravity = -1;
            TypedArray a = c.obtainStyledAttributes(attrs, C0249R.styleable.LinearLayoutCompat_Layout);
            this.weight = a.getFloat(C0249R.styleable.LinearLayoutCompat_Layout_android_layout_weight, 0.0f);
            this.gravity = a.getInt(C0249R.styleable.LinearLayoutCompat_Layout_android_layout_gravity, -1);
            a.recycle();
        }

        public LayoutParams(int width, int height) {
            super(width, height);
            this.gravity = -1;
            this.weight = 0.0f;
        }

        public LayoutParams(int width, int height, float weight) {
            super(width, height);
            this.gravity = -1;
            this.weight = weight;
        }

        public LayoutParams(android.view.ViewGroup.LayoutParams p) {
            super(p);
            this.gravity = -1;
        }

        public LayoutParams(MarginLayoutParams source) {
            super(source);
            this.gravity = -1;
        }

        public LayoutParams(LayoutParams source) {
            super(source);
            this.gravity = -1;
            this.weight = source.weight;
            this.gravity = source.gravity;
        }
    }

    @RestrictTo({Scope.LIBRARY_GROUP})
    @Retention(RetentionPolicy.SOURCE)
    public @interface OrientationMode {
    }

    public LinearLayoutCompat(Context context) {
        this(context, null);
    }

    public LinearLayoutCompat(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LinearLayoutCompat(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mBaselineAligned = true;
        this.mBaselineAlignedChildIndex = -1;
        this.mBaselineChildTop = 0;
        this.mGravity = 8388659;
        TintTypedArray a = TintTypedArray.obtainStyledAttributes(context, attrs, C0249R.styleable.LinearLayoutCompat, defStyleAttr, 0);
        int index = a.getInt(C0249R.styleable.LinearLayoutCompat_android_orientation, -1);
        if (index >= 0) {
            setOrientation(index);
        }
        index = a.getInt(C0249R.styleable.LinearLayoutCompat_android_gravity, -1);
        if (index >= 0) {
            setGravity(index);
        }
        boolean baselineAligned = a.getBoolean(C0249R.styleable.LinearLayoutCompat_android_baselineAligned, true);
        if (!baselineAligned) {
            setBaselineAligned(baselineAligned);
        }
        this.mWeightSum = a.getFloat(C0249R.styleable.LinearLayoutCompat_android_weightSum, -1.0f);
        this.mBaselineAlignedChildIndex = a.getInt(C0249R.styleable.LinearLayoutCompat_android_baselineAlignedChildIndex, -1);
        this.mUseLargestChild = a.getBoolean(C0249R.styleable.LinearLayoutCompat_measureWithLargestChild, false);
        setDividerDrawable(a.getDrawable(C0249R.styleable.LinearLayoutCompat_divider));
        this.mShowDividers = a.getInt(C0249R.styleable.LinearLayoutCompat_showDividers, 0);
        this.mDividerPadding = a.getDimensionPixelSize(C0249R.styleable.LinearLayoutCompat_dividerPadding, 0);
        a.recycle();
    }

    public void setShowDividers(int showDividers) {
        if (showDividers != this.mShowDividers) {
            requestLayout();
        }
        this.mShowDividers = showDividers;
    }

    public boolean shouldDelayChildPressedState() {
        return false;
    }

    public int getShowDividers() {
        return this.mShowDividers;
    }

    public Drawable getDividerDrawable() {
        return this.mDivider;
    }

    public void setDividerDrawable(Drawable divider) {
        if (divider != this.mDivider) {
            this.mDivider = divider;
            boolean z = false;
            if (divider != null) {
                this.mDividerWidth = divider.getIntrinsicWidth();
                this.mDividerHeight = divider.getIntrinsicHeight();
            } else {
                this.mDividerWidth = 0;
                this.mDividerHeight = 0;
            }
            if (divider == null) {
                z = true;
            }
            setWillNotDraw(z);
            requestLayout();
        }
    }

    public void setDividerPadding(int padding) {
        this.mDividerPadding = padding;
    }

    public int getDividerPadding() {
        return this.mDividerPadding;
    }

    @RestrictTo({Scope.LIBRARY_GROUP})
    public int getDividerWidth() {
        return this.mDividerWidth;
    }

    protected void onDraw(Canvas canvas) {
        if (this.mDivider != null) {
            if (this.mOrientation == 1) {
                drawDividersVertical(canvas);
            } else {
                drawDividersHorizontal(canvas);
            }
        }
    }

    void drawDividersVertical(Canvas canvas) {
        int count = getVirtualChildCount();
        int i = 0;
        while (i < count) {
            View child = getVirtualChildAt(i);
            if (!(child == null || child.getVisibility() == 8 || !hasDividerBeforeChildAt(i))) {
                drawHorizontalDivider(canvas, (child.getTop() - ((LayoutParams) child.getLayoutParams()).topMargin) - this.mDividerHeight);
            }
            i++;
        }
        if (hasDividerBeforeChildAt(count)) {
            int bottom;
            View child2 = getVirtualChildAt(count - 1);
            if (child2 == null) {
                bottom = (getHeight() - getPaddingBottom()) - this.mDividerHeight;
            } else {
                bottom = child2.getBottom() + ((LayoutParams) child2.getLayoutParams()).bottomMargin;
            }
            drawHorizontalDivider(canvas, bottom);
        }
    }

    void drawDividersHorizontal(Canvas canvas) {
        int count = getVirtualChildCount();
        boolean isLayoutRtl = ViewUtils.isLayoutRtl(this);
        int i = 0;
        while (i < count) {
            View child = getVirtualChildAt(i);
            if (!(child == null || child.getVisibility() == 8 || !hasDividerBeforeChildAt(i))) {
                int position;
                LayoutParams lp = (LayoutParams) child.getLayoutParams();
                if (isLayoutRtl) {
                    position = child.getRight() + lp.rightMargin;
                } else {
                    position = (child.getLeft() - lp.leftMargin) - this.mDividerWidth;
                }
                drawVerticalDivider(canvas, position);
            }
            i++;
        }
        if (hasDividerBeforeChildAt(count)) {
            int position2;
            View child2 = getVirtualChildAt(count - 1);
            if (child2 != null) {
                LayoutParams lp2 = (LayoutParams) child2.getLayoutParams();
                if (isLayoutRtl) {
                    position2 = (child2.getLeft() - lp2.leftMargin) - this.mDividerWidth;
                } else {
                    position2 = child2.getRight() + lp2.rightMargin;
                }
            } else if (isLayoutRtl) {
                position2 = getPaddingLeft();
            } else {
                position2 = (getWidth() - getPaddingRight()) - this.mDividerWidth;
            }
            drawVerticalDivider(canvas, position2);
        }
    }

    void drawHorizontalDivider(Canvas canvas, int top) {
        this.mDivider.setBounds(getPaddingLeft() + this.mDividerPadding, top, (getWidth() - getPaddingRight()) - this.mDividerPadding, this.mDividerHeight + top);
        this.mDivider.draw(canvas);
    }

    void drawVerticalDivider(Canvas canvas, int left) {
        this.mDivider.setBounds(left, getPaddingTop() + this.mDividerPadding, this.mDividerWidth + left, (getHeight() - getPaddingBottom()) - this.mDividerPadding);
        this.mDivider.draw(canvas);
    }

    public boolean isBaselineAligned() {
        return this.mBaselineAligned;
    }

    public void setBaselineAligned(boolean baselineAligned) {
        this.mBaselineAligned = baselineAligned;
    }

    public boolean isMeasureWithLargestChildEnabled() {
        return this.mUseLargestChild;
    }

    public void setMeasureWithLargestChildEnabled(boolean enabled) {
        this.mUseLargestChild = enabled;
    }

    public int getBaseline() {
        if (this.mBaselineAlignedChildIndex < 0) {
            return super.getBaseline();
        }
        if (getChildCount() <= this.mBaselineAlignedChildIndex) {
            throw new RuntimeException("mBaselineAlignedChildIndex of LinearLayout set to an index that is out of bounds.");
        }
        View child = getChildAt(this.mBaselineAlignedChildIndex);
        int childBaseline = child.getBaseline();
        if (childBaseline != -1) {
            int childTop = this.mBaselineChildTop;
            if (this.mOrientation == 1) {
                int majorGravity = this.mGravity & 112;
                if (majorGravity != 48) {
                    if (majorGravity == 16) {
                        childTop += ((((getBottom() - getTop()) - getPaddingTop()) - getPaddingBottom()) - this.mTotalLength) / 2;
                    } else if (majorGravity == 80) {
                        childTop = ((getBottom() - getTop()) - getPaddingBottom()) - this.mTotalLength;
                    }
                }
            }
            return (((LayoutParams) child.getLayoutParams()).topMargin + childTop) + childBaseline;
        } else if (this.mBaselineAlignedChildIndex == 0) {
            return -1;
        } else {
            throw new RuntimeException("mBaselineAlignedChildIndex of LinearLayout points to a View that doesn't know how to get its baseline.");
        }
    }

    public int getBaselineAlignedChildIndex() {
        return this.mBaselineAlignedChildIndex;
    }

    public void setBaselineAlignedChildIndex(int i) {
        if (i >= 0) {
            if (i < getChildCount()) {
                this.mBaselineAlignedChildIndex = i;
                return;
            }
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("base aligned child index out of range (0, ");
        stringBuilder.append(getChildCount());
        stringBuilder.append(")");
        throw new IllegalArgumentException(stringBuilder.toString());
    }

    View getVirtualChildAt(int index) {
        return getChildAt(index);
    }

    int getVirtualChildCount() {
        return getChildCount();
    }

    public float getWeightSum() {
        return this.mWeightSum;
    }

    public void setWeightSum(float weightSum) {
        this.mWeightSum = Math.max(0.0f, weightSum);
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (this.mOrientation == 1) {
            measureVertical(widthMeasureSpec, heightMeasureSpec);
        } else {
            measureHorizontal(widthMeasureSpec, heightMeasureSpec);
        }
    }

    @RestrictTo({Scope.LIBRARY})
    protected boolean hasDividerBeforeChildAt(int childIndex) {
        boolean hasVisibleViewBefore = false;
        if (childIndex == 0) {
            if ((this.mShowDividers & 1) != 0) {
                hasVisibleViewBefore = true;
            }
            return hasVisibleViewBefore;
        } else if (childIndex == getChildCount()) {
            if ((this.mShowDividers & 4) != 0) {
                hasVisibleViewBefore = true;
            }
            return hasVisibleViewBefore;
        } else if ((this.mShowDividers & 2) == 0) {
            return false;
        } else {
            hasVisibleViewBefore = false;
            for (int i = childIndex - 1; i >= 0; i--) {
                if (getChildAt(i).getVisibility() != 8) {
                    hasVisibleViewBefore = true;
                    break;
                }
            }
            return hasVisibleViewBefore;
        }
    }

    void measureVertical(int widthMeasureSpec, int heightMeasureSpec) {
        boolean weightedMaxWidth;
        int count;
        int heightMode;
        boolean skippedMeasure;
        int childState;
        boolean matchWidthLocally;
        int margin;
        int delta;
        float totalWeight;
        int alternativeMaxWidth;
        boolean z;
        int i;
        int i2;
        int i3 = widthMeasureSpec;
        int i4 = heightMeasureSpec;
        this.mTotalLength = 0;
        int childState2 = 0;
        float totalWeight2 = 0.0f;
        int count2 = getVirtualChildCount();
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode2 = MeasureSpec.getMode(heightMeasureSpec);
        boolean skippedMeasure2 = false;
        int baselineChildIndex = this.mBaselineAlignedChildIndex;
        boolean useLargestChild = this.mUseLargestChild;
        boolean matchWidth = false;
        int alternativeMaxWidth2 = 0;
        int maxWidth = 0;
        int i5 = 0;
        boolean weightedMaxWidth2 = false;
        int largestChildHeight = 0;
        boolean allFillParent = true;
        while (true) {
            weightedMaxWidth = weightedMaxWidth2;
            if (i5 >= count2) {
                break;
            }
            LayoutParams lp;
            int i6;
            View child = getVirtualChildAt(i5);
            int childState3;
            if (child == null) {
                childState3 = childState2;
                r7.mTotalLength += measureNullChild(i5);
                count = count2;
                heightMode = heightMode2;
                weightedMaxWidth2 = weightedMaxWidth;
                childState2 = childState3;
            } else {
                childState3 = childState2;
                int maxWidth2 = maxWidth;
                if (child.getVisibility() == 8) {
                    i5 += getChildrenSkipCount(child, i5);
                    count = count2;
                    heightMode = heightMode2;
                    weightedMaxWidth2 = weightedMaxWidth;
                    childState2 = childState3;
                    maxWidth = maxWidth2;
                } else {
                    View child2;
                    boolean weightedMaxWidth3;
                    int maxWidth3;
                    int i7;
                    if (hasDividerBeforeChildAt(i5)) {
                        r7.mTotalLength += r7.mDividerHeight;
                    }
                    LayoutParams lp2 = (LayoutParams) child.getLayoutParams();
                    float totalWeight3 = totalWeight2 + lp2.weight;
                    int i8;
                    if (heightMode2 == 1073741824 && lp2.height == 0 && lp2.weight > 0.0f) {
                        childState2 = r7.mTotalLength;
                        i8 = i5;
                        r7.mTotalLength = Math.max(childState2, (lp2.topMargin + childState2) + lp2.bottomMargin);
                        lp = lp2;
                        child2 = child;
                        i4 = alternativeMaxWidth2;
                        count = count2;
                        heightMode = heightMode2;
                        skippedMeasure = true;
                        weightedMaxWidth3 = weightedMaxWidth;
                        childState = childState3;
                        maxWidth3 = maxWidth2;
                        i7 = i8;
                    } else {
                        i8 = i5;
                        i5 = Integer.MIN_VALUE;
                        if (lp2.height == 0 && lp2.weight > 0.0f) {
                            i5 = 0;
                            lp2.height = -2;
                        }
                        i7 = i8;
                        skippedMeasure = skippedMeasure2;
                        childState = childState3;
                        LayoutParams lp3 = lp2;
                        maxWidth3 = maxWidth2;
                        heightMode = heightMode2;
                        heightMode2 = largestChildHeight;
                        largestChildHeight = i3;
                        child2 = child;
                        count = count2;
                        weightedMaxWidth3 = weightedMaxWidth;
                        count2 = i5;
                        i6 = i4;
                        i4 = alternativeMaxWidth2;
                        measureChildBeforeLayout(child, i7, largestChildHeight, 0, i6, totalWeight3 == 0.0f ? r7.mTotalLength : 0);
                        if (count2 != Integer.MIN_VALUE) {
                            lp = lp3;
                            lp.height = count2;
                        } else {
                            lp = lp3;
                        }
                        childState2 = child2.getMeasuredHeight();
                        maxWidth = r7.mTotalLength;
                        r7.mTotalLength = Math.max(maxWidth, (((maxWidth + childState2) + lp.topMargin) + lp.bottomMargin) + getNextLocationOffset(child2));
                        if (useLargestChild) {
                            largestChildHeight = Math.max(childState2, heightMode2);
                        } else {
                            largestChildHeight = heightMode2;
                        }
                    }
                    if (baselineChildIndex >= 0) {
                        childState2 = i7;
                        if (baselineChildIndex == childState2 + 1) {
                            r7.mBaselineChildTop = r7.mTotalLength;
                        }
                    } else {
                        childState2 = i7;
                    }
                    if (childState2 >= baselineChildIndex || lp.weight <= 0.0f) {
                        boolean allFillParent2;
                        matchWidthLocally = false;
                        if (widthMode != 1073741824) {
                            alternativeMaxWidth2 = -1;
                            if (lp.width == -1) {
                                matchWidth = true;
                                matchWidthLocally = true;
                            }
                        } else {
                            alternativeMaxWidth2 = -1;
                        }
                        margin = lp.leftMargin + lp.rightMargin;
                        i6 = child2.getMeasuredWidth() + margin;
                        count2 = Math.max(maxWidth3, i6);
                        heightMode2 = View.combineMeasuredStates(childState, child2.getMeasuredState());
                        boolean allFillParent3 = allFillParent && lp.width == alternativeMaxWidth2;
                        if (lp.weight > 0.0f) {
                            allFillParent2 = allFillParent3;
                            allFillParent3 = Math.max(weightedMaxWidth3, matchWidthLocally ? margin : i6);
                        } else {
                            allFillParent2 = allFillParent3;
                            allFillParent3 = weightedMaxWidth3;
                            i4 = Math.max(i4, matchWidthLocally ? margin : i6);
                        }
                        i5 = childState2 + getChildrenSkipCount(child2, childState2);
                        weightedMaxWidth2 = allFillParent3;
                        alternativeMaxWidth2 = i4;
                        maxWidth = count2;
                        childState2 = heightMode2;
                        totalWeight2 = totalWeight3;
                        skippedMeasure2 = skippedMeasure;
                        allFillParent = allFillParent2;
                    } else {
                        throw new RuntimeException("A child of LinearLayout with index less than mBaselineAlignedChildIndex has weight > 0, which won't work.  Either remove the weight, or don't set mBaselineAlignedChildIndex.");
                    }
                }
            }
            i5++;
            heightMode2 = heightMode;
            count2 = count;
            i3 = widthMeasureSpec;
            i4 = heightMeasureSpec;
        }
        i4 = alternativeMaxWidth2;
        count = count2;
        heightMode = heightMode2;
        skippedMeasure = skippedMeasure2;
        alternativeMaxWidth2 = weightedMaxWidth;
        childState = childState2;
        count2 = maxWidth;
        heightMode2 = largestChildHeight;
        if (r7.mTotalLength > 0) {
            childState2 = count;
            if (hasDividerBeforeChildAt(childState2)) {
                r7.mTotalLength += r7.mDividerHeight;
            }
        } else {
            childState2 = count;
        }
        if (useLargestChild) {
            maxWidth = heightMode;
            if (maxWidth == Integer.MIN_VALUE || maxWidth == 0) {
                r7.mTotalLength = 0;
                largestChildHeight = 0;
                while (largestChildHeight < childState2) {
                    int i9;
                    child = getVirtualChildAt(largestChildHeight);
                    if (child == null) {
                        r7.mTotalLength += measureNullChild(largestChildHeight);
                    } else if (child.getVisibility() == 8) {
                        largestChildHeight += getChildrenSkipCount(child, largestChildHeight);
                    } else {
                        lp = (LayoutParams) child.getLayoutParams();
                        i3 = r7.mTotalLength;
                        i9 = largestChildHeight;
                        r7.mTotalLength = Math.max(i3, (((i3 + heightMode2) + lp.topMargin) + lp.bottomMargin) + getNextLocationOffset(child));
                        largestChildHeight = i9 + 1;
                    }
                    i9 = largestChildHeight;
                    largestChildHeight = i9 + 1;
                }
            }
        } else {
            maxWidth = heightMode;
        }
        r7.mTotalLength += getPaddingTop() + getPaddingBottom();
        largestChildHeight = heightMeasureSpec;
        i3 = View.resolveSizeAndState(Math.max(r7.mTotalLength, getSuggestedMinimumHeight()), largestChildHeight, 0);
        i5 = i3 & ViewCompat.MEASURED_SIZE_MASK;
        margin = i5 - r7.mTotalLength;
        int i10;
        if (skippedMeasure) {
            delta = margin;
            totalWeight = totalWeight2;
            i10 = alternativeMaxWidth2;
        } else if (margin == 0 || totalWeight2 <= 0.0f) {
            i4 = Math.max(i4, alternativeMaxWidth2);
            if (useLargestChild) {
                if (maxWidth != 1073741824) {
                    int i11 = 0;
                    while (true) {
                        i5 = i11;
                        if (i5 >= childState2) {
                            break;
                        }
                        delta = margin;
                        child = getVirtualChildAt(i5);
                        if (child != null) {
                            totalWeight = totalWeight2;
                            i10 = alternativeMaxWidth2;
                            if (child.getVisibility() == 8) {
                                alternativeMaxWidth = i4;
                            } else {
                                LayoutParams totalWeight4 = (LayoutParams) child.getLayoutParams();
                                float childExtra = totalWeight4.weight;
                                if (childExtra > 0.0f) {
                                    LayoutParams lp4 = totalWeight4;
                                    alternativeMaxWidth = i4;
                                    child.measure(MeasureSpec.makeMeasureSpec(child.getMeasuredWidth(), 1073741824), MeasureSpec.makeMeasureSpec(heightMode2, 1073741824));
                                } else {
                                    alternativeMaxWidth = i4;
                                }
                            }
                        } else {
                            totalWeight = totalWeight2;
                            i10 = alternativeMaxWidth2;
                            alternativeMaxWidth = i4;
                        }
                        i11 = i5 + 1;
                        margin = delta;
                        totalWeight2 = totalWeight;
                        alternativeMaxWidth2 = i10;
                        i4 = alternativeMaxWidth;
                    }
                }
                totalWeight = totalWeight2;
                i10 = alternativeMaxWidth2;
                alternativeMaxWidth = i4;
            } else {
                delta = margin;
                totalWeight = totalWeight2;
                i10 = alternativeMaxWidth2;
                alternativeMaxWidth = i4;
            }
            int i12 = maxWidth;
            z = useLargestChild;
            i = heightMode2;
            i2 = baselineChildIndex;
            heightMode2 = widthMeasureSpec;
            if (!(allFillParent || widthMode == 1073741824)) {
                count2 = alternativeMaxWidth;
            }
            setMeasuredDimension(View.resolveSizeAndState(Math.max(count2 + (getPaddingLeft() + getPaddingRight()), getSuggestedMinimumWidth()), heightMode2, childState), i3);
            if (matchWidth) {
                forceUniformWidth(childState2, largestChildHeight);
            }
        } else {
            int i13 = i5;
            delta = margin;
            totalWeight = totalWeight2;
            boolean z2 = alternativeMaxWidth2;
        }
        totalWeight2 = r7.mWeightSum > 0.0f ? r7.mWeightSum : totalWeight;
        r7.mTotalLength = 0;
        i5 = 0;
        alternativeMaxWidth2 = delta;
        while (i5 < childState2) {
            child = getVirtualChildAt(i5);
            z = useLargestChild;
            i = heightMode2;
            if (child.getVisibility() == 8) {
                i12 = maxWidth;
                i2 = baselineChildIndex;
                heightMode2 = widthMeasureSpec;
            } else {
                int delta2;
                int margin2;
                boolean matchWidthLocally2;
                boolean allFillParent4;
                float weightSum;
                LayoutParams lp5 = (LayoutParams) child.getLayoutParams();
                float childExtra2 = lp5.weight;
                if (childExtra2 > 0.0f) {
                    i2 = baselineChildIndex;
                    baselineChildIndex = (int) ((((float) alternativeMaxWidth2) * childExtra2) / totalWeight2);
                    float weightSum2 = totalWeight2 - childExtra2;
                    delta2 = alternativeMaxWidth2 - baselineChildIndex;
                    i6 = getChildMeasureSpec(widthMeasureSpec, ((getPaddingLeft() + getPaddingRight()) + lp5.leftMargin) + lp5.rightMargin, lp5.width);
                    if (lp5.height != 0) {
                        i12 = maxWidth;
                    } else if (maxWidth != 1073741824) {
                        i12 = maxWidth;
                    } else {
                        i12 = maxWidth;
                        child.measure(i6, MeasureSpec.makeMeasureSpec(baselineChildIndex > 0 ? baselineChildIndex : 0, 1073741824));
                        int i14 = baselineChildIndex;
                        childState = View.combineMeasuredStates(childState, child.getMeasuredState() & InputDeviceCompat.SOURCE_ANY);
                        totalWeight2 = weightSum2;
                    }
                    maxWidth = child.getMeasuredHeight() + baselineChildIndex;
                    if (maxWidth < 0) {
                        maxWidth = 0;
                    }
                    child.measure(i6, MeasureSpec.makeMeasureSpec(maxWidth, 1073741824));
                    childState = View.combineMeasuredStates(childState, child.getMeasuredState() & InputDeviceCompat.SOURCE_ANY);
                    totalWeight2 = weightSum2;
                } else {
                    i12 = maxWidth;
                    float f = childExtra2;
                    i2 = baselineChildIndex;
                    heightMode2 = widthMeasureSpec;
                    delta2 = alternativeMaxWidth2;
                }
                maxWidth = lp5.leftMargin + lp5.rightMargin;
                alternativeMaxWidth2 = child.getMeasuredWidth() + maxWidth;
                count2 = Math.max(count2, alternativeMaxWidth2);
                if (widthMode != 1073741824) {
                    margin2 = maxWidth;
                    if (lp5.width == -1) {
                        matchWidthLocally = true;
                        i4 = Math.max(i4, matchWidthLocally ? margin2 : alternativeMaxWidth2);
                        if (allFillParent) {
                            matchWidthLocally2 = matchWidthLocally;
                        } else {
                            matchWidthLocally2 = matchWidthLocally;
                            if (lp5.width == -1) {
                                allFillParent4 = true;
                                maxWidth = r7.mTotalLength;
                                weightSum = totalWeight2;
                                r7.mTotalLength = Math.max(maxWidth, (((maxWidth + child.getMeasuredHeight()) + lp5.topMargin) + lp5.bottomMargin) + getNextLocationOffset(child));
                                allFillParent = allFillParent4;
                                alternativeMaxWidth2 = delta2;
                                totalWeight2 = weightSum;
                            }
                        }
                        allFillParent4 = false;
                        maxWidth = r7.mTotalLength;
                        weightSum = totalWeight2;
                        r7.mTotalLength = Math.max(maxWidth, (((maxWidth + child.getMeasuredHeight()) + lp5.topMargin) + lp5.bottomMargin) + getNextLocationOffset(child));
                        allFillParent = allFillParent4;
                        alternativeMaxWidth2 = delta2;
                        totalWeight2 = weightSum;
                    }
                } else {
                    margin2 = maxWidth;
                }
                matchWidthLocally = false;
                if (matchWidthLocally) {
                }
                i4 = Math.max(i4, matchWidthLocally ? margin2 : alternativeMaxWidth2);
                if (allFillParent) {
                    matchWidthLocally2 = matchWidthLocally;
                } else {
                    matchWidthLocally2 = matchWidthLocally;
                    if (lp5.width == -1) {
                        allFillParent4 = true;
                        maxWidth = r7.mTotalLength;
                        weightSum = totalWeight2;
                        r7.mTotalLength = Math.max(maxWidth, (((maxWidth + child.getMeasuredHeight()) + lp5.topMargin) + lp5.bottomMargin) + getNextLocationOffset(child));
                        allFillParent = allFillParent4;
                        alternativeMaxWidth2 = delta2;
                        totalWeight2 = weightSum;
                    }
                }
                allFillParent4 = false;
                maxWidth = r7.mTotalLength;
                weightSum = totalWeight2;
                r7.mTotalLength = Math.max(maxWidth, (((maxWidth + child.getMeasuredHeight()) + lp5.topMargin) + lp5.bottomMargin) + getNextLocationOffset(child));
                allFillParent = allFillParent4;
                alternativeMaxWidth2 = delta2;
                totalWeight2 = weightSum;
            }
            i5++;
            useLargestChild = z;
            heightMode2 = i;
            baselineChildIndex = i2;
            maxWidth = i12;
        }
        z = useLargestChild;
        i = heightMode2;
        i2 = baselineChildIndex;
        heightMode2 = widthMeasureSpec;
        r7.mTotalLength += getPaddingTop() + getPaddingBottom();
        delta = alternativeMaxWidth2;
        alternativeMaxWidth = i4;
        count2 = alternativeMaxWidth;
        setMeasuredDimension(View.resolveSizeAndState(Math.max(count2 + (getPaddingLeft() + getPaddingRight()), getSuggestedMinimumWidth()), heightMode2, childState), i3);
        if (matchWidth) {
            forceUniformWidth(childState2, largestChildHeight);
        }
    }

    private void forceUniformWidth(int count, int heightMeasureSpec) {
        int uniformMeasureSpec = MeasureSpec.makeMeasureSpec(getMeasuredWidth(), 1073741824);
        for (int i = 0; i < count; i++) {
            View child = getVirtualChildAt(i);
            if (child.getVisibility() != 8) {
                LayoutParams lp = (LayoutParams) child.getLayoutParams();
                if (lp.width == -1) {
                    int oldHeight = lp.height;
                    lp.height = child.getMeasuredHeight();
                    measureChildWithMargins(child, uniformMeasureSpec, 0, heightMeasureSpec, 0);
                    lp.height = oldHeight;
                }
            }
        }
    }

    void measureHorizontal(int widthMeasureSpec, int heightMeasureSpec) {
        boolean baselineAligned;
        int alternativeMaxHeight;
        int i;
        View child;
        int i2;
        LayoutParams lp;
        int i3;
        int i4;
        int i5;
        int i6;
        int widthSize;
        View child2;
        int alternativeMaxHeight2;
        float childExtra;
        float f;
        int i7;
        int count;
        boolean z;
        int i8;
        float weightSum;
        float weightSum2;
        LayoutParams weightedMaxHeight;
        float childExtra2;
        float weightSum3;
        int delta;
        boolean allFillParent;
        int alternativeMaxHeight3;
        boolean childBaseline;
        int index;
        boolean gravity;
        int i9 = widthMeasureSpec;
        int i10 = heightMeasureSpec;
        this.mTotalLength = 0;
        float totalWeight = 0.0f;
        int count2 = getVirtualChildCount();
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        if (this.mMaxAscent == null || r7.mMaxDescent == null) {
            r7.mMaxAscent = new int[4];
            r7.mMaxDescent = new int[4];
        }
        int[] maxAscent = r7.mMaxAscent;
        int[] maxDescent = r7.mMaxDescent;
        boolean matchHeight = false;
        maxAscent[3] = -1;
        maxAscent[2] = -1;
        maxAscent[1] = -1;
        maxAscent[0] = -1;
        maxDescent[3] = -1;
        maxDescent[2] = -1;
        maxDescent[1] = -1;
        maxDescent[0] = -1;
        boolean baselineAligned2 = r7.mBaselineAligned;
        boolean skippedMeasure = false;
        boolean useLargestChild = r7.mUseLargestChild;
        int[] maxDescent2 = maxDescent;
        boolean isExactly = widthMode == 1073741824;
        int childState = 0;
        int largestChildWidth = 0;
        boolean matchHeight2 = matchHeight;
        matchHeight = true;
        int maxHeight = 0;
        int i11 = 0;
        int alternativeMaxHeight4 = 0;
        int weightedMaxHeight2 = 0;
        while (i11 < count2) {
            boolean matchHeightLocally;
            View child3 = getVirtualChildAt(i11);
            int largestChildWidth2;
            if (child3 == null) {
                largestChildWidth2 = largestChildWidth;
                r7.mTotalLength += measureNullChild(i11);
                baselineAligned = baselineAligned2;
                largestChildWidth = largestChildWidth2;
            } else {
                largestChildWidth2 = largestChildWidth;
                int weightedMaxHeight3 = weightedMaxHeight2;
                if (child3.getVisibility() == 8) {
                    i11 += getChildrenSkipCount(child3, i11);
                    baselineAligned = baselineAligned2;
                    largestChildWidth = largestChildWidth2;
                    weightedMaxHeight2 = weightedMaxHeight3;
                } else {
                    LayoutParams lp2;
                    int alternativeMaxHeight5;
                    int maxHeight2;
                    int weightedMaxHeight4;
                    int i12;
                    int childBaseline2;
                    int margin;
                    boolean allFillParent2;
                    if (hasDividerBeforeChildAt(i11)) {
                        r7.mTotalLength += r7.mDividerWidth;
                    }
                    LayoutParams lp3 = (LayoutParams) child3.getLayoutParams();
                    float totalWeight2 = totalWeight + lp3.weight;
                    int i13;
                    if (widthMode == 1073741824 && lp3.width == 0 && lp3.weight > 0.0f) {
                        if (isExactly) {
                            i13 = i11;
                            r7.mTotalLength += lp3.leftMargin + lp3.rightMargin;
                        } else {
                            i13 = i11;
                            i11 = r7.mTotalLength;
                            r7.mTotalLength = Math.max(i11, (lp3.leftMargin + i11) + lp3.rightMargin);
                        }
                        if (baselineAligned2) {
                            largestChildWidth = MeasureSpec.makeMeasureSpec(0, 0);
                            child3.measure(largestChildWidth, largestChildWidth);
                            lp2 = lp3;
                            alternativeMaxHeight5 = alternativeMaxHeight4;
                            maxHeight2 = maxHeight;
                            baselineAligned = baselineAligned2;
                            lp3 = largestChildWidth2;
                            weightedMaxHeight4 = weightedMaxHeight3;
                            i12 = i13;
                            i10 = -1;
                        } else {
                            skippedMeasure = true;
                            lp2 = lp3;
                            alternativeMaxHeight5 = alternativeMaxHeight4;
                            maxHeight2 = maxHeight;
                            baselineAligned = baselineAligned2;
                            weightedMaxHeight4 = weightedMaxHeight3;
                            i12 = i13;
                            i10 = -1;
                            matchHeightLocally = false;
                            matchHeight2 = true;
                            matchHeightLocally = true;
                            weightedMaxHeight2 = lp2.topMargin + lp2.bottomMargin;
                            alternativeMaxHeight4 = child3.getMeasuredHeight() + weightedMaxHeight2;
                            maxHeight = View.combineMeasuredStates(childState, child3.getMeasuredState());
                            if (baselineAligned) {
                                childBaseline2 = child3.getBaseline();
                                if (childBaseline2 != i10) {
                                    if (lp2.gravity < 0) {
                                    }
                                    i9 = ((((lp2.gravity < 0 ? r7.mGravity : lp2.gravity) & 112) >> 4) & -2) >> 1;
                                    maxAscent[i9] = Math.max(maxAscent[i9], childBaseline2);
                                    margin = weightedMaxHeight2;
                                    maxDescent2[i9] = Math.max(maxDescent2[i9], alternativeMaxHeight4 - childBaseline2);
                                    weightedMaxHeight2 = Math.max(maxHeight2, alternativeMaxHeight4);
                                    if (!matchHeight) {
                                    }
                                    if (lp2.weight <= 0.0f) {
                                        if (matchHeightLocally) {
                                        }
                                        i10 = Math.max(weightedMaxHeight4, matchHeightLocally ? margin : alternativeMaxHeight4);
                                        alternativeMaxHeight = alternativeMaxHeight5;
                                    } else {
                                        i10 = weightedMaxHeight4;
                                        if (matchHeightLocally) {
                                        }
                                        alternativeMaxHeight = Math.max(alternativeMaxHeight5, matchHeightLocally ? margin : alternativeMaxHeight4);
                                    }
                                    i9 = i12;
                                    i11 = i9 + getChildrenSkipCount(child3, i9);
                                    childState = maxHeight;
                                    matchHeight = allFillParent2;
                                    alternativeMaxHeight4 = alternativeMaxHeight;
                                    totalWeight = totalWeight2;
                                    largestChildWidth = largestChildWidth2;
                                    maxHeight = weightedMaxHeight2;
                                    weightedMaxHeight2 = i10;
                                }
                            }
                            margin = weightedMaxHeight2;
                            weightedMaxHeight2 = Math.max(maxHeight2, alternativeMaxHeight4);
                            if (matchHeight) {
                            }
                            if (lp2.weight <= 0.0f) {
                                i10 = weightedMaxHeight4;
                                if (matchHeightLocally) {
                                }
                                alternativeMaxHeight = Math.max(alternativeMaxHeight5, matchHeightLocally ? margin : alternativeMaxHeight4);
                            } else {
                                if (matchHeightLocally) {
                                }
                                i10 = Math.max(weightedMaxHeight4, matchHeightLocally ? margin : alternativeMaxHeight4);
                                alternativeMaxHeight = alternativeMaxHeight5;
                            }
                            i9 = i12;
                            i11 = i9 + getChildrenSkipCount(child3, i9);
                            childState = maxHeight;
                            matchHeight = allFillParent2;
                            alternativeMaxHeight4 = alternativeMaxHeight;
                            totalWeight = totalWeight2;
                            largestChildWidth = largestChildWidth2;
                            maxHeight = weightedMaxHeight2;
                            weightedMaxHeight2 = i10;
                        }
                    } else {
                        i13 = i11;
                        i11 = Integer.MIN_VALUE;
                        if (lp3.width == 0 && lp3.weight > 0.0f) {
                            i11 = 0;
                            lp3.width = -2;
                        }
                        i12 = i13;
                        int largestChildWidth3 = largestChildWidth2;
                        LayoutParams lp4 = lp3;
                        weightedMaxHeight4 = weightedMaxHeight3;
                        alternativeMaxHeight5 = alternativeMaxHeight4;
                        maxHeight2 = maxHeight;
                        i9 = i11;
                        childBaseline2 = i10;
                        baselineAligned = baselineAligned2;
                        i10 = -1;
                        measureChildBeforeLayout(child3, i12, i9, totalWeight2 == 0.0f ? r7.mTotalLength : 0, childBaseline2, false);
                        if (i9 != Integer.MIN_VALUE) {
                            lp2 = lp4;
                            lp2.width = i9;
                        } else {
                            lp2 = lp4;
                        }
                        largestChildWidth = child3.getMeasuredWidth();
                        if (isExactly) {
                            r7.mTotalLength += ((lp2.leftMargin + largestChildWidth) + lp2.rightMargin) + getNextLocationOffset(child3);
                        } else {
                            weightedMaxHeight2 = r7.mTotalLength;
                            r7.mTotalLength = Math.max(weightedMaxHeight2, (((weightedMaxHeight2 + largestChildWidth) + lp2.leftMargin) + lp2.rightMargin) + getNextLocationOffset(child3));
                        }
                        if (useLargestChild) {
                            largestChildWidth2 = Math.max(largestChildWidth, largestChildWidth3);
                            matchHeightLocally = false;
                            if (heightMode != 1073741824 && lp2.height == i10) {
                                matchHeight2 = true;
                                matchHeightLocally = true;
                            }
                            weightedMaxHeight2 = lp2.topMargin + lp2.bottomMargin;
                            alternativeMaxHeight4 = child3.getMeasuredHeight() + weightedMaxHeight2;
                            maxHeight = View.combineMeasuredStates(childState, child3.getMeasuredState());
                            if (baselineAligned) {
                                childBaseline2 = child3.getBaseline();
                                if (childBaseline2 != i10) {
                                    i9 = ((((lp2.gravity < 0 ? r7.mGravity : lp2.gravity) & 112) >> 4) & -2) >> 1;
                                    maxAscent[i9] = Math.max(maxAscent[i9], childBaseline2);
                                    margin = weightedMaxHeight2;
                                    maxDescent2[i9] = Math.max(maxDescent2[i9], alternativeMaxHeight4 - childBaseline2);
                                    weightedMaxHeight2 = Math.max(maxHeight2, alternativeMaxHeight4);
                                    allFillParent2 = matchHeight && lp2.height == -1;
                                    if (lp2.weight <= 0.0f) {
                                        i10 = Math.max(weightedMaxHeight4, matchHeightLocally ? margin : alternativeMaxHeight4);
                                        alternativeMaxHeight = alternativeMaxHeight5;
                                    } else {
                                        i10 = weightedMaxHeight4;
                                        alternativeMaxHeight = Math.max(alternativeMaxHeight5, matchHeightLocally ? margin : alternativeMaxHeight4);
                                    }
                                    i9 = i12;
                                    i11 = i9 + getChildrenSkipCount(child3, i9);
                                    childState = maxHeight;
                                    matchHeight = allFillParent2;
                                    alternativeMaxHeight4 = alternativeMaxHeight;
                                    totalWeight = totalWeight2;
                                    largestChildWidth = largestChildWidth2;
                                    maxHeight = weightedMaxHeight2;
                                    weightedMaxHeight2 = i10;
                                }
                            }
                            margin = weightedMaxHeight2;
                            weightedMaxHeight2 = Math.max(maxHeight2, alternativeMaxHeight4);
                            if (matchHeight) {
                            }
                            if (lp2.weight <= 0.0f) {
                                i10 = weightedMaxHeight4;
                                if (matchHeightLocally) {
                                }
                                alternativeMaxHeight = Math.max(alternativeMaxHeight5, matchHeightLocally ? margin : alternativeMaxHeight4);
                            } else {
                                if (matchHeightLocally) {
                                }
                                i10 = Math.max(weightedMaxHeight4, matchHeightLocally ? margin : alternativeMaxHeight4);
                                alternativeMaxHeight = alternativeMaxHeight5;
                            }
                            i9 = i12;
                            i11 = i9 + getChildrenSkipCount(child3, i9);
                            childState = maxHeight;
                            matchHeight = allFillParent2;
                            alternativeMaxHeight4 = alternativeMaxHeight;
                            totalWeight = totalWeight2;
                            largestChildWidth = largestChildWidth2;
                            maxHeight = weightedMaxHeight2;
                            weightedMaxHeight2 = i10;
                        } else {
                            lp3 = largestChildWidth3;
                        }
                    }
                    largestChildWidth2 = lp3;
                    matchHeightLocally = false;
                    matchHeight2 = true;
                    matchHeightLocally = true;
                    weightedMaxHeight2 = lp2.topMargin + lp2.bottomMargin;
                    alternativeMaxHeight4 = child3.getMeasuredHeight() + weightedMaxHeight2;
                    maxHeight = View.combineMeasuredStates(childState, child3.getMeasuredState());
                    if (baselineAligned) {
                        childBaseline2 = child3.getBaseline();
                        if (childBaseline2 != i10) {
                            if (lp2.gravity < 0) {
                            }
                            i9 = ((((lp2.gravity < 0 ? r7.mGravity : lp2.gravity) & 112) >> 4) & -2) >> 1;
                            maxAscent[i9] = Math.max(maxAscent[i9], childBaseline2);
                            margin = weightedMaxHeight2;
                            maxDescent2[i9] = Math.max(maxDescent2[i9], alternativeMaxHeight4 - childBaseline2);
                            weightedMaxHeight2 = Math.max(maxHeight2, alternativeMaxHeight4);
                            if (matchHeight) {
                            }
                            if (lp2.weight <= 0.0f) {
                                if (matchHeightLocally) {
                                }
                                i10 = Math.max(weightedMaxHeight4, matchHeightLocally ? margin : alternativeMaxHeight4);
                                alternativeMaxHeight = alternativeMaxHeight5;
                            } else {
                                i10 = weightedMaxHeight4;
                                if (matchHeightLocally) {
                                }
                                alternativeMaxHeight = Math.max(alternativeMaxHeight5, matchHeightLocally ? margin : alternativeMaxHeight4);
                            }
                            i9 = i12;
                            i11 = i9 + getChildrenSkipCount(child3, i9);
                            childState = maxHeight;
                            matchHeight = allFillParent2;
                            alternativeMaxHeight4 = alternativeMaxHeight;
                            totalWeight = totalWeight2;
                            largestChildWidth = largestChildWidth2;
                            maxHeight = weightedMaxHeight2;
                            weightedMaxHeight2 = i10;
                        }
                    }
                    margin = weightedMaxHeight2;
                    weightedMaxHeight2 = Math.max(maxHeight2, alternativeMaxHeight4);
                    if (matchHeight) {
                    }
                    if (lp2.weight <= 0.0f) {
                        i10 = weightedMaxHeight4;
                        if (matchHeightLocally) {
                        }
                        alternativeMaxHeight = Math.max(alternativeMaxHeight5, matchHeightLocally ? margin : alternativeMaxHeight4);
                    } else {
                        if (matchHeightLocally) {
                        }
                        i10 = Math.max(weightedMaxHeight4, matchHeightLocally ? margin : alternativeMaxHeight4);
                        alternativeMaxHeight = alternativeMaxHeight5;
                    }
                    i9 = i12;
                    i11 = i9 + getChildrenSkipCount(child3, i9);
                    childState = maxHeight;
                    matchHeight = allFillParent2;
                    alternativeMaxHeight4 = alternativeMaxHeight;
                    totalWeight = totalWeight2;
                    largestChildWidth = largestChildWidth2;
                    maxHeight = weightedMaxHeight2;
                    weightedMaxHeight2 = i10;
                }
            }
            i11++;
            baselineAligned2 = baselineAligned;
            i9 = widthMeasureSpec;
            i10 = heightMeasureSpec;
        }
        i10 = weightedMaxHeight2;
        i9 = maxHeight;
        baselineAligned = baselineAligned2;
        alternativeMaxHeight = childState;
        weightedMaxHeight2 = largestChildWidth;
        if (r7.mTotalLength > 0 && hasDividerBeforeChildAt(count2)) {
            r7.mTotalLength += r7.mDividerWidth;
        }
        if (maxAscent[1] == -1 && maxAscent[0] == -1 && maxAscent[2] == -1) {
            if (maxAscent[3] == -1) {
                i = alternativeMaxHeight;
                maxHeight = i9;
                if (useLargestChild && (widthMode == Integer.MIN_VALUE || widthMode == 0)) {
                    r7.mTotalLength = 0;
                    i11 = 0;
                    while (i11 < count2) {
                        child = getVirtualChildAt(i11);
                        if (child == null) {
                            r7.mTotalLength += measureNullChild(i11);
                            i2 = i11;
                        } else if (child.getVisibility() != 8) {
                            i11 += getChildrenSkipCount(child, i11);
                            i11++;
                        } else {
                            lp = (LayoutParams) child.getLayoutParams();
                            if (isExactly) {
                                i2 = i11;
                                i11 = r7.mTotalLength;
                                r7.mTotalLength = Math.max(i11, (((i11 + weightedMaxHeight2) + lp.leftMargin) + lp.rightMargin) + getNextLocationOffset(child));
                            } else {
                                i2 = i11;
                                r7.mTotalLength += ((lp.leftMargin + weightedMaxHeight2) + lp.rightMargin) + getNextLocationOffset(child);
                            }
                        }
                        i11 = i2;
                        i11++;
                    }
                }
                r7.mTotalLength += getPaddingLeft() + getPaddingRight();
                i9 = View.resolveSizeAndState(Math.max(r7.mTotalLength, getSuggestedMinimumWidth()), widthMeasureSpec, 0);
                i11 = i9 & ViewCompat.MEASURED_SIZE_MASK;
                alternativeMaxHeight = i11 - r7.mTotalLength;
                if (!skippedMeasure) {
                    i3 = weightedMaxHeight2;
                    i4 = maxHeight;
                } else if (alternativeMaxHeight != 0 || totalWeight <= 0.0f) {
                    alternativeMaxHeight4 = Math.max(alternativeMaxHeight4, i10);
                    if (useLargestChild && widthMode != 1073741824) {
                        i5 = 0;
                        while (true) {
                            i6 = i5;
                            if (i6 < count2) {
                                break;
                            }
                            widthSize = i11;
                            child2 = getVirtualChildAt(i6);
                            if (child2 == null) {
                                alternativeMaxHeight2 = alternativeMaxHeight4;
                                i4 = maxHeight;
                                if (child2.getVisibility() != 8) {
                                    i3 = weightedMaxHeight2;
                                } else {
                                    LayoutParams lp5 = (LayoutParams) child2.getLayoutParams();
                                    childExtra = lp5.weight;
                                    if (childExtra <= 0.0f) {
                                        i3 = weightedMaxHeight2;
                                        child2.measure(MeasureSpec.makeMeasureSpec(weightedMaxHeight2, 1073741824), MeasureSpec.makeMeasureSpec(child2.getMeasuredHeight(), 1073741824));
                                    } else {
                                        i3 = weightedMaxHeight2;
                                    }
                                }
                            } else {
                                i3 = weightedMaxHeight2;
                                alternativeMaxHeight2 = alternativeMaxHeight4;
                                i4 = maxHeight;
                            }
                            i5 = i6 + 1;
                            i11 = widthSize;
                            alternativeMaxHeight4 = alternativeMaxHeight2;
                            maxHeight = i4;
                            weightedMaxHeight2 = i3;
                        }
                    }
                    i3 = weightedMaxHeight2;
                    f = totalWeight;
                    i7 = i10;
                    count = count2;
                    z = useLargestChild;
                    weightedMaxHeight2 = alternativeMaxHeight4;
                    i6 = maxHeight;
                    i8 = heightMeasureSpec;
                    if (!(matchHeight || heightMode == 1073741824)) {
                        i6 = weightedMaxHeight2;
                    }
                    setMeasuredDimension((i & ViewCompat.MEASURED_STATE_MASK) | i9, View.resolveSizeAndState(Math.max(i6 + (getPaddingTop() + getPaddingBottom()), getSuggestedMinimumHeight()), i8, i << 16));
                    if (matchHeight2) {
                        largestChildWidth = widthMeasureSpec;
                        return;
                    }
                    forceUniformHeight(count, widthMeasureSpec);
                } else {
                    widthSize = i11;
                    i3 = weightedMaxHeight2;
                    i4 = maxHeight;
                }
                weightSum = r7.mWeightSum <= 0.0f ? r7.mWeightSum : totalWeight;
                maxAscent[3] = -1;
                maxAscent[2] = -1;
                maxAscent[1] = -1;
                maxAscent[0] = -1;
                maxDescent2[3] = -1;
                maxDescent2[2] = -1;
                maxDescent2[1] = -1;
                maxDescent2[0] = -1;
                i6 = -1;
                r7.mTotalLength = 0;
                weightedMaxHeight2 = alternativeMaxHeight4;
                maxHeight = i;
                weightSum2 = weightSum;
                i11 = 0;
                while (i11 < count2) {
                    f = totalWeight;
                    totalWeight = getVirtualChildAt(i11);
                    if (totalWeight == null) {
                        i7 = i10;
                        z = useLargestChild;
                        if (!totalWeight.getVisibility()) {
                            count = count2;
                            i8 = heightMeasureSpec;
                        } else {
                            weightedMaxHeight = (LayoutParams) totalWeight.getLayoutParams();
                            childExtra2 = weightedMaxHeight.weight;
                            if (childExtra2 <= 0.0f) {
                                largestChildWidth = (int) ((((float) alternativeMaxHeight) * childExtra2) / weightSum2);
                                weightSum3 = weightSum2 - childExtra2;
                                delta = alternativeMaxHeight - largestChildWidth;
                                count = count2;
                                weightSum2 = getChildMeasureSpec(heightMeasureSpec, ((getPaddingTop() + getPaddingBottom()) + weightedMaxHeight.topMargin) + weightedMaxHeight.bottomMargin, weightedMaxHeight.height);
                                if (weightedMaxHeight.width == 0) {
                                    if (widthMode != 1073741824) {
                                        totalWeight.measure(MeasureSpec.makeMeasureSpec(largestChildWidth <= 0 ? largestChildWidth : 0, 1073741824), weightSum2);
                                        int i14 = largestChildWidth;
                                        maxHeight = View.combineMeasuredStates(maxHeight, totalWeight.getMeasuredState() & ViewCompat.MEASURED_STATE_MASK);
                                    }
                                }
                                alternativeMaxHeight = totalWeight.getMeasuredWidth() + largestChildWidth;
                                if (alternativeMaxHeight < 0) {
                                    alternativeMaxHeight = 0;
                                }
                                totalWeight.measure(MeasureSpec.makeMeasureSpec(alternativeMaxHeight, 1073741824), weightSum2);
                                maxHeight = View.combineMeasuredStates(maxHeight, totalWeight.getMeasuredState() & ViewCompat.MEASURED_STATE_MASK);
                            } else {
                                count = count2;
                                float f2 = childExtra2;
                                useLargestChild = heightMeasureSpec;
                                weightSum3 = weightSum2;
                                delta = alternativeMaxHeight;
                            }
                            if (isExactly) {
                                largestChildWidth = r7.mTotalLength;
                                r7.mTotalLength = Math.max(largestChildWidth, (((totalWeight.getMeasuredWidth() + largestChildWidth) + weightedMaxHeight.leftMargin) + weightedMaxHeight.rightMargin) + getNextLocationOffset(totalWeight));
                            } else {
                                r7.mTotalLength += ((totalWeight.getMeasuredWidth() + weightedMaxHeight.leftMargin) + weightedMaxHeight.rightMargin) + getNextLocationOffset(totalWeight);
                            }
                            matchHeightLocally = heightMode == 1073741824 && weightedMaxHeight.height == -1;
                            alternativeMaxHeight4 = weightedMaxHeight.topMargin + weightedMaxHeight.bottomMargin;
                            alternativeMaxHeight = totalWeight.getMeasuredHeight() + alternativeMaxHeight4;
                            count2 = Math.max(i6, alternativeMaxHeight);
                            weightedMaxHeight2 = Math.max(weightedMaxHeight2, matchHeightLocally ? alternativeMaxHeight4 : alternativeMaxHeight);
                            if (matchHeight) {
                            } else {
                                if (weightedMaxHeight.height) {
                                    matchHeightLocally = true;
                                    if (baselineAligned) {
                                        allFillParent = matchHeightLocally;
                                        alternativeMaxHeight3 = weightedMaxHeight2;
                                    } else {
                                        childBaseline = totalWeight.getBaseline();
                                        allFillParent = matchHeightLocally;
                                        if (childBaseline) {
                                            matchHeightLocally = (weightedMaxHeight.gravity < false ? r7.mGravity : weightedMaxHeight.gravity) & 112;
                                            index = ((matchHeightLocally >> 4) & -2) >> 1;
                                            gravity = matchHeightLocally;
                                            maxAscent[index] = Math.max(maxAscent[index], childBaseline);
                                            alternativeMaxHeight3 = weightedMaxHeight2;
                                            maxDescent2[index] = Math.max(maxDescent2[index], alternativeMaxHeight - childBaseline);
                                        } else {
                                            alternativeMaxHeight3 = weightedMaxHeight2;
                                        }
                                    }
                                    i6 = count2;
                                    weightSum2 = weightSum3;
                                    alternativeMaxHeight = delta;
                                    matchHeight = allFillParent;
                                    weightedMaxHeight2 = alternativeMaxHeight3;
                                }
                            }
                            matchHeightLocally = false;
                            if (baselineAligned) {
                                allFillParent = matchHeightLocally;
                                alternativeMaxHeight3 = weightedMaxHeight2;
                            } else {
                                childBaseline = totalWeight.getBaseline();
                                allFillParent = matchHeightLocally;
                                if (childBaseline) {
                                    alternativeMaxHeight3 = weightedMaxHeight2;
                                } else {
                                    if (weightedMaxHeight.gravity < false) {
                                    }
                                    matchHeightLocally = (weightedMaxHeight.gravity < false ? r7.mGravity : weightedMaxHeight.gravity) & 112;
                                    index = ((matchHeightLocally >> 4) & -2) >> 1;
                                    gravity = matchHeightLocally;
                                    maxAscent[index] = Math.max(maxAscent[index], childBaseline);
                                    alternativeMaxHeight3 = weightedMaxHeight2;
                                    maxDescent2[index] = Math.max(maxDescent2[index], alternativeMaxHeight - childBaseline);
                                }
                            }
                            i6 = count2;
                            weightSum2 = weightSum3;
                            alternativeMaxHeight = delta;
                            matchHeight = allFillParent;
                            weightedMaxHeight2 = alternativeMaxHeight3;
                        }
                    } else {
                        i7 = i10;
                        count = count2;
                        z = useLargestChild;
                        useLargestChild = heightMeasureSpec;
                    }
                    i11++;
                    totalWeight = f;
                    i10 = i7;
                    useLargestChild = z;
                    count2 = count;
                    largestChildWidth = widthMeasureSpec;
                }
                i7 = i10;
                count = count2;
                z = useLargestChild;
                i8 = heightMeasureSpec;
                r7.mTotalLength += getPaddingLeft() + getPaddingRight();
                if (!(maxAscent[1] == -1 && maxAscent[0] == -1 && maxAscent[2] == -1 && maxAscent[3] == -1)) {
                    i6 = Math.max(i6, Math.max(maxAscent[3], Math.max(maxAscent[0], Math.max(maxAscent[1], maxAscent[2]))) + Math.max(maxDescent2[3], Math.max(maxDescent2[0], Math.max(maxDescent2[1], maxDescent2[2]))));
                }
                i = maxHeight;
                i6 = weightedMaxHeight2;
                setMeasuredDimension((i & ViewCompat.MEASURED_STATE_MASK) | i9, View.resolveSizeAndState(Math.max(i6 + (getPaddingTop() + getPaddingBottom()), getSuggestedMinimumHeight()), i8, i << 16));
                if (matchHeight2) {
                    largestChildWidth = widthMeasureSpec;
                    return;
                }
                forceUniformHeight(count, widthMeasureSpec);
            }
        }
        i = alternativeMaxHeight;
        maxHeight = Math.max(i9, Math.max(maxAscent[3], Math.max(maxAscent[0], Math.max(maxAscent[1], maxAscent[2]))) + Math.max(maxDescent2[3], Math.max(maxDescent2[0], Math.max(maxDescent2[1], maxDescent2[2]))));
        r7.mTotalLength = 0;
        i11 = 0;
        while (i11 < count2) {
            child = getVirtualChildAt(i11);
            if (child == null) {
                r7.mTotalLength += measureNullChild(i11);
                i2 = i11;
            } else if (child.getVisibility() != 8) {
                lp = (LayoutParams) child.getLayoutParams();
                if (isExactly) {
                    i2 = i11;
                    i11 = r7.mTotalLength;
                    r7.mTotalLength = Math.max(i11, (((i11 + weightedMaxHeight2) + lp.leftMargin) + lp.rightMargin) + getNextLocationOffset(child));
                } else {
                    i2 = i11;
                    r7.mTotalLength += ((lp.leftMargin + weightedMaxHeight2) + lp.rightMargin) + getNextLocationOffset(child);
                }
            } else {
                i11 += getChildrenSkipCount(child, i11);
                i11++;
            }
            i11 = i2;
            i11++;
        }
        r7.mTotalLength += getPaddingLeft() + getPaddingRight();
        i9 = View.resolveSizeAndState(Math.max(r7.mTotalLength, getSuggestedMinimumWidth()), widthMeasureSpec, 0);
        i11 = i9 & ViewCompat.MEASURED_SIZE_MASK;
        alternativeMaxHeight = i11 - r7.mTotalLength;
        if (!skippedMeasure) {
            i3 = weightedMaxHeight2;
            i4 = maxHeight;
        } else {
            if (alternativeMaxHeight != 0) {
            }
            alternativeMaxHeight4 = Math.max(alternativeMaxHeight4, i10);
            i5 = 0;
            while (true) {
                i6 = i5;
                if (i6 < count2) {
                    break;
                }
                widthSize = i11;
                child2 = getVirtualChildAt(i6);
                if (child2 == null) {
                    i3 = weightedMaxHeight2;
                    alternativeMaxHeight2 = alternativeMaxHeight4;
                    i4 = maxHeight;
                } else {
                    alternativeMaxHeight2 = alternativeMaxHeight4;
                    i4 = maxHeight;
                    if (child2.getVisibility() != 8) {
                        LayoutParams lp52 = (LayoutParams) child2.getLayoutParams();
                        childExtra = lp52.weight;
                        if (childExtra <= 0.0f) {
                            i3 = weightedMaxHeight2;
                        } else {
                            i3 = weightedMaxHeight2;
                            child2.measure(MeasureSpec.makeMeasureSpec(weightedMaxHeight2, 1073741824), MeasureSpec.makeMeasureSpec(child2.getMeasuredHeight(), 1073741824));
                        }
                    } else {
                        i3 = weightedMaxHeight2;
                    }
                }
                i5 = i6 + 1;
                i11 = widthSize;
                alternativeMaxHeight4 = alternativeMaxHeight2;
                maxHeight = i4;
                weightedMaxHeight2 = i3;
            }
            i3 = weightedMaxHeight2;
            f = totalWeight;
            i7 = i10;
            count = count2;
            z = useLargestChild;
            weightedMaxHeight2 = alternativeMaxHeight4;
            i6 = maxHeight;
            i8 = heightMeasureSpec;
            i6 = weightedMaxHeight2;
            setMeasuredDimension((i & ViewCompat.MEASURED_STATE_MASK) | i9, View.resolveSizeAndState(Math.max(i6 + (getPaddingTop() + getPaddingBottom()), getSuggestedMinimumHeight()), i8, i << 16));
            if (matchHeight2) {
                forceUniformHeight(count, widthMeasureSpec);
            }
            largestChildWidth = widthMeasureSpec;
            return;
        }
        if (r7.mWeightSum <= 0.0f) {
        }
        maxAscent[3] = -1;
        maxAscent[2] = -1;
        maxAscent[1] = -1;
        maxAscent[0] = -1;
        maxDescent2[3] = -1;
        maxDescent2[2] = -1;
        maxDescent2[1] = -1;
        maxDescent2[0] = -1;
        i6 = -1;
        r7.mTotalLength = 0;
        weightedMaxHeight2 = alternativeMaxHeight4;
        maxHeight = i;
        weightSum2 = weightSum;
        i11 = 0;
        while (i11 < count2) {
            f = totalWeight;
            totalWeight = getVirtualChildAt(i11);
            if (totalWeight == null) {
                i7 = i10;
                count = count2;
                z = useLargestChild;
                useLargestChild = heightMeasureSpec;
            } else {
                i7 = i10;
                z = useLargestChild;
                if (!totalWeight.getVisibility()) {
                    weightedMaxHeight = (LayoutParams) totalWeight.getLayoutParams();
                    childExtra2 = weightedMaxHeight.weight;
                    if (childExtra2 <= 0.0f) {
                        count = count2;
                        float f22 = childExtra2;
                        useLargestChild = heightMeasureSpec;
                        weightSum3 = weightSum2;
                        delta = alternativeMaxHeight;
                    } else {
                        largestChildWidth = (int) ((((float) alternativeMaxHeight) * childExtra2) / weightSum2);
                        weightSum3 = weightSum2 - childExtra2;
                        delta = alternativeMaxHeight - largestChildWidth;
                        count = count2;
                        weightSum2 = getChildMeasureSpec(heightMeasureSpec, ((getPaddingTop() + getPaddingBottom()) + weightedMaxHeight.topMargin) + weightedMaxHeight.bottomMargin, weightedMaxHeight.height);
                        if (weightedMaxHeight.width == 0) {
                            if (widthMode != 1073741824) {
                                if (largestChildWidth <= 0) {
                                }
                                totalWeight.measure(MeasureSpec.makeMeasureSpec(largestChildWidth <= 0 ? largestChildWidth : 0, 1073741824), weightSum2);
                                int i142 = largestChildWidth;
                                maxHeight = View.combineMeasuredStates(maxHeight, totalWeight.getMeasuredState() & ViewCompat.MEASURED_STATE_MASK);
                            }
                        }
                        alternativeMaxHeight = totalWeight.getMeasuredWidth() + largestChildWidth;
                        if (alternativeMaxHeight < 0) {
                            alternativeMaxHeight = 0;
                        }
                        totalWeight.measure(MeasureSpec.makeMeasureSpec(alternativeMaxHeight, 1073741824), weightSum2);
                        maxHeight = View.combineMeasuredStates(maxHeight, totalWeight.getMeasuredState() & ViewCompat.MEASURED_STATE_MASK);
                    }
                    if (isExactly) {
                        largestChildWidth = r7.mTotalLength;
                        r7.mTotalLength = Math.max(largestChildWidth, (((totalWeight.getMeasuredWidth() + largestChildWidth) + weightedMaxHeight.leftMargin) + weightedMaxHeight.rightMargin) + getNextLocationOffset(totalWeight));
                    } else {
                        r7.mTotalLength += ((totalWeight.getMeasuredWidth() + weightedMaxHeight.leftMargin) + weightedMaxHeight.rightMargin) + getNextLocationOffset(totalWeight);
                    }
                    if (heightMode == 1073741824) {
                    }
                    alternativeMaxHeight4 = weightedMaxHeight.topMargin + weightedMaxHeight.bottomMargin;
                    alternativeMaxHeight = totalWeight.getMeasuredHeight() + alternativeMaxHeight4;
                    count2 = Math.max(i6, alternativeMaxHeight);
                    if (matchHeightLocally) {
                    }
                    weightedMaxHeight2 = Math.max(weightedMaxHeight2, matchHeightLocally ? alternativeMaxHeight4 : alternativeMaxHeight);
                    if (matchHeight) {
                    } else {
                        if (weightedMaxHeight.height) {
                            matchHeightLocally = true;
                            if (baselineAligned) {
                                childBaseline = totalWeight.getBaseline();
                                allFillParent = matchHeightLocally;
                                if (childBaseline) {
                                    if (weightedMaxHeight.gravity < false) {
                                    }
                                    matchHeightLocally = (weightedMaxHeight.gravity < false ? r7.mGravity : weightedMaxHeight.gravity) & 112;
                                    index = ((matchHeightLocally >> 4) & -2) >> 1;
                                    gravity = matchHeightLocally;
                                    maxAscent[index] = Math.max(maxAscent[index], childBaseline);
                                    alternativeMaxHeight3 = weightedMaxHeight2;
                                    maxDescent2[index] = Math.max(maxDescent2[index], alternativeMaxHeight - childBaseline);
                                } else {
                                    alternativeMaxHeight3 = weightedMaxHeight2;
                                }
                            } else {
                                allFillParent = matchHeightLocally;
                                alternativeMaxHeight3 = weightedMaxHeight2;
                            }
                            i6 = count2;
                            weightSum2 = weightSum3;
                            alternativeMaxHeight = delta;
                            matchHeight = allFillParent;
                            weightedMaxHeight2 = alternativeMaxHeight3;
                        }
                    }
                    matchHeightLocally = false;
                    if (baselineAligned) {
                        allFillParent = matchHeightLocally;
                        alternativeMaxHeight3 = weightedMaxHeight2;
                    } else {
                        childBaseline = totalWeight.getBaseline();
                        allFillParent = matchHeightLocally;
                        if (childBaseline) {
                            alternativeMaxHeight3 = weightedMaxHeight2;
                        } else {
                            if (weightedMaxHeight.gravity < false) {
                            }
                            matchHeightLocally = (weightedMaxHeight.gravity < false ? r7.mGravity : weightedMaxHeight.gravity) & 112;
                            index = ((matchHeightLocally >> 4) & -2) >> 1;
                            gravity = matchHeightLocally;
                            maxAscent[index] = Math.max(maxAscent[index], childBaseline);
                            alternativeMaxHeight3 = weightedMaxHeight2;
                            maxDescent2[index] = Math.max(maxDescent2[index], alternativeMaxHeight - childBaseline);
                        }
                    }
                    i6 = count2;
                    weightSum2 = weightSum3;
                    alternativeMaxHeight = delta;
                    matchHeight = allFillParent;
                    weightedMaxHeight2 = alternativeMaxHeight3;
                } else {
                    count = count2;
                    i8 = heightMeasureSpec;
                }
            }
            i11++;
            totalWeight = f;
            i10 = i7;
            useLargestChild = z;
            count2 = count;
            largestChildWidth = widthMeasureSpec;
        }
        i7 = i10;
        count = count2;
        z = useLargestChild;
        i8 = heightMeasureSpec;
        r7.mTotalLength += getPaddingLeft() + getPaddingRight();
        i6 = Math.max(i6, Math.max(maxAscent[3], Math.max(maxAscent[0], Math.max(maxAscent[1], maxAscent[2]))) + Math.max(maxDescent2[3], Math.max(maxDescent2[0], Math.max(maxDescent2[1], maxDescent2[2]))));
        i = maxHeight;
        i6 = weightedMaxHeight2;
        setMeasuredDimension((i & ViewCompat.MEASURED_STATE_MASK) | i9, View.resolveSizeAndState(Math.max(i6 + (getPaddingTop() + getPaddingBottom()), getSuggestedMinimumHeight()), i8, i << 16));
        if (matchHeight2) {
            largestChildWidth = widthMeasureSpec;
            return;
        }
        forceUniformHeight(count, widthMeasureSpec);
    }

    private void forceUniformHeight(int count, int widthMeasureSpec) {
        int uniformMeasureSpec = MeasureSpec.makeMeasureSpec(getMeasuredHeight(), 1073741824);
        for (int i = 0; i < count; i++) {
            View child = getVirtualChildAt(i);
            if (child.getVisibility() != 8) {
                LayoutParams lp = (LayoutParams) child.getLayoutParams();
                if (lp.height == -1) {
                    int oldWidth = lp.width;
                    lp.width = child.getMeasuredWidth();
                    measureChildWithMargins(child, widthMeasureSpec, 0, uniformMeasureSpec, 0);
                    lp.width = oldWidth;
                }
            }
        }
    }

    int getChildrenSkipCount(View child, int index) {
        return 0;
    }

    int measureNullChild(int childIndex) {
        return 0;
    }

    void measureChildBeforeLayout(View child, int childIndex, int widthMeasureSpec, int totalWidth, int heightMeasureSpec, int totalHeight) {
        measureChildWithMargins(child, widthMeasureSpec, totalWidth, heightMeasureSpec, totalHeight);
    }

    int getLocationOffset(View child) {
        return 0;
    }

    int getNextLocationOffset(View child) {
        return 0;
    }

    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (this.mOrientation == 1) {
            layoutVertical(l, t, r, b);
        } else {
            layoutHorizontal(l, t, r, b);
        }
    }

    void layoutVertical(int left, int top, int right, int bottom) {
        int paddingTop;
        int paddingLeft = getPaddingLeft();
        int width = right - left;
        int childRight = width - getPaddingRight();
        int childSpace = (width - paddingLeft) - getPaddingRight();
        int count = getVirtualChildCount();
        int majorGravity = this.mGravity & 112;
        int minorGravity = this.mGravity & GravityCompat.RELATIVE_HORIZONTAL_GRAVITY_MASK;
        if (majorGravity == 16) {
            paddingTop = getPaddingTop() + (((bottom - top) - r6.mTotalLength) / 2);
        } else if (majorGravity != 80) {
            paddingTop = getPaddingTop();
        } else {
            paddingTop = ((getPaddingTop() + bottom) - top) - r6.mTotalLength;
        }
        int i = 0;
        while (true) {
            int i2 = i;
            int paddingLeft2;
            if (i2 < count) {
                int majorGravity2;
                View child = getVirtualChildAt(i2);
                if (child == null) {
                    paddingTop += measureNullChild(i2);
                    majorGravity2 = majorGravity;
                    paddingLeft2 = paddingLeft;
                } else if (child.getVisibility() != 8) {
                    int childWidth = child.getMeasuredWidth();
                    int childHeight = child.getMeasuredHeight();
                    LayoutParams lp = (LayoutParams) child.getLayoutParams();
                    int gravity = lp.gravity;
                    if (gravity < 0) {
                        gravity = minorGravity;
                    }
                    int layoutDirection = ViewCompat.getLayoutDirection(this);
                    int gravity2 = gravity;
                    gravity = GravityCompat.getAbsoluteGravity(gravity, layoutDirection) & 7;
                    majorGravity2 = majorGravity;
                    gravity = gravity != 1 ? gravity != 5 ? lp.leftMargin + paddingLeft : (childRight - childWidth) - lp.rightMargin : ((((childSpace - childWidth) / 2) + paddingLeft) + lp.leftMargin) - lp.rightMargin;
                    if (hasDividerBeforeChildAt(i2) != 0) {
                        paddingTop += r6.mDividerHeight;
                    }
                    gravity2 = paddingTop + lp.topMargin;
                    LayoutParams lp2 = lp;
                    View child2 = child;
                    paddingLeft2 = paddingLeft;
                    paddingLeft = i2;
                    setChildFrame(child, gravity, gravity2 + getLocationOffset(child), childWidth, childHeight);
                    i2 = paddingLeft + getChildrenSkipCount(child2, paddingLeft);
                    paddingTop = gravity2 + ((childHeight + lp2.bottomMargin) + getNextLocationOffset(child2));
                } else {
                    majorGravity2 = majorGravity;
                    paddingLeft2 = paddingLeft;
                    paddingLeft = i2;
                }
                i = i2 + 1;
                majorGravity = majorGravity2;
                paddingLeft = paddingLeft2;
            } else {
                paddingLeft2 = paddingLeft;
                return;
            }
        }
    }

    void layoutHorizontal(int left, int top, int right, int bottom) {
        boolean isLayoutRtl = ViewUtils.isLayoutRtl(this);
        int paddingTop = getPaddingTop();
        int height = bottom - top;
        int childBottom = height - getPaddingBottom();
        int childSpace = (height - paddingTop) - getPaddingBottom();
        int count = getVirtualChildCount();
        int majorGravity = this.mGravity & GravityCompat.RELATIVE_HORIZONTAL_GRAVITY_MASK;
        int minorGravity = this.mGravity & 112;
        boolean baselineAligned = this.mBaselineAligned;
        int[] maxAscent = this.mMaxAscent;
        int[] maxDescent = this.mMaxDescent;
        int layoutDirection = ViewCompat.getLayoutDirection(this);
        int absoluteGravity = GravityCompat.getAbsoluteGravity(majorGravity, layoutDirection);
        if (absoluteGravity != 1) {
            if (absoluteGravity != 5) {
                absoluteGravity = getPaddingLeft();
            } else {
                absoluteGravity = ((getPaddingLeft() + right) - left) - r6.mTotalLength;
            }
        } else {
            int i = layoutDirection;
            absoluteGravity = getPaddingLeft() + (((right - left) - r6.mTotalLength) / 2);
        }
        layoutDirection = absoluteGravity;
        absoluteGravity = 0;
        int dir = 1;
        if (isLayoutRtl) {
            absoluteGravity = count - 1;
            dir = -1;
        }
        int i2 = 0;
        int childLeft = layoutDirection;
        while (true) {
            layoutDirection = i2;
            int[] maxAscent2;
            boolean baselineAligned2;
            int majorGravity2;
            int count2;
            boolean isLayoutRtl2;
            if (layoutDirection < count) {
                int[] maxDescent2;
                int childIndex = absoluteGravity + (dir * layoutDirection);
                View child = getVirtualChildAt(childIndex);
                if (child == null) {
                    childLeft += measureNullChild(childIndex);
                    maxDescent2 = maxDescent;
                    maxAscent2 = maxAscent;
                    baselineAligned2 = baselineAligned;
                    majorGravity2 = majorGravity;
                    count2 = count;
                    isLayoutRtl2 = isLayoutRtl;
                } else {
                    int i3 = layoutDirection;
                    majorGravity2 = majorGravity;
                    if (child.getVisibility() != 8) {
                        int childBaseline;
                        i2 = child.getMeasuredWidth();
                        int childHeight = child.getMeasuredHeight();
                        LayoutParams lp = (LayoutParams) child.getLayoutParams();
                        int childBaseline2 = -1;
                        if (baselineAligned) {
                            baselineAligned2 = baselineAligned;
                            if (!lp.height) {
                                childBaseline = child.getBaseline();
                                layoutDirection = lp.gravity;
                                if (layoutDirection < 0) {
                                    layoutDirection = minorGravity;
                                }
                                layoutDirection &= 112;
                                count2 = count;
                                if (layoutDirection != 16) {
                                    layoutDirection = ((((childSpace - childHeight) / 2) + paddingTop) + lp.topMargin) - lp.bottomMargin;
                                } else if (layoutDirection != 48) {
                                    layoutDirection = lp.topMargin + paddingTop;
                                    if (childBaseline != -1) {
                                        layoutDirection += maxAscent[1] - childBaseline;
                                    }
                                } else if (layoutDirection == 80) {
                                    layoutDirection = paddingTop;
                                } else {
                                    layoutDirection = (childBottom - childHeight) - lp.bottomMargin;
                                    if (childBaseline != -1) {
                                        layoutDirection -= maxDescent[2] - (child.getMeasuredHeight() - childBaseline);
                                    }
                                }
                                if (hasDividerBeforeChildAt(childIndex)) {
                                    childLeft += r6.mDividerWidth;
                                }
                                childLeft += lp.leftMargin;
                                maxDescent2 = maxDescent;
                                maxAscent2 = maxAscent;
                                isLayoutRtl2 = isLayoutRtl;
                                isLayoutRtl = lp;
                                setChildFrame(child, childLeft + getLocationOffset(child), layoutDirection, i2, childHeight);
                                childLeft += (i2 + isLayoutRtl.rightMargin) + getNextLocationOffset(child);
                                layoutDirection = i3 + getChildrenSkipCount(child, childIndex);
                            }
                        } else {
                            baselineAligned2 = baselineAligned;
                        }
                        childBaseline = childBaseline2;
                        layoutDirection = lp.gravity;
                        if (layoutDirection < 0) {
                            layoutDirection = minorGravity;
                        }
                        layoutDirection &= 112;
                        count2 = count;
                        if (layoutDirection != 16) {
                            layoutDirection = ((((childSpace - childHeight) / 2) + paddingTop) + lp.topMargin) - lp.bottomMargin;
                        } else if (layoutDirection != 48) {
                            layoutDirection = lp.topMargin + paddingTop;
                            if (childBaseline != -1) {
                                layoutDirection += maxAscent[1] - childBaseline;
                            }
                        } else if (layoutDirection == 80) {
                            layoutDirection = (childBottom - childHeight) - lp.bottomMargin;
                            if (childBaseline != -1) {
                                layoutDirection -= maxDescent[2] - (child.getMeasuredHeight() - childBaseline);
                            }
                        } else {
                            layoutDirection = paddingTop;
                        }
                        if (hasDividerBeforeChildAt(childIndex)) {
                            childLeft += r6.mDividerWidth;
                        }
                        childLeft += lp.leftMargin;
                        maxDescent2 = maxDescent;
                        maxAscent2 = maxAscent;
                        isLayoutRtl2 = isLayoutRtl;
                        isLayoutRtl = lp;
                        setChildFrame(child, childLeft + getLocationOffset(child), layoutDirection, i2, childHeight);
                        childLeft += (i2 + isLayoutRtl.rightMargin) + getNextLocationOffset(child);
                        layoutDirection = i3 + getChildrenSkipCount(child, childIndex);
                    } else {
                        maxDescent2 = maxDescent;
                        maxAscent2 = maxAscent;
                        baselineAligned2 = baselineAligned;
                        count2 = count;
                        isLayoutRtl2 = isLayoutRtl;
                        layoutDirection = i3;
                    }
                }
                i2 = layoutDirection + 1;
                majorGravity = majorGravity2;
                baselineAligned = baselineAligned2;
                maxDescent = maxDescent2;
                count = count2;
                maxAscent = maxAscent2;
                isLayoutRtl = isLayoutRtl2;
            } else {
                maxAscent2 = maxAscent;
                baselineAligned2 = baselineAligned;
                majorGravity2 = majorGravity;
                count2 = count;
                isLayoutRtl2 = isLayoutRtl;
                return;
            }
        }
    }

    private void setChildFrame(View child, int left, int top, int width, int height) {
        child.layout(left, top, left + width, top + height);
    }

    public void setOrientation(int orientation) {
        if (this.mOrientation != orientation) {
            this.mOrientation = orientation;
            requestLayout();
        }
    }

    public int getOrientation() {
        return this.mOrientation;
    }

    public void setGravity(int gravity) {
        if (this.mGravity != gravity) {
            if ((GravityCompat.RELATIVE_HORIZONTAL_GRAVITY_MASK & gravity) == 0) {
                gravity |= GravityCompat.START;
            }
            if ((gravity & 112) == 0) {
                gravity |= 48;
            }
            this.mGravity = gravity;
            requestLayout();
        }
    }

    public int getGravity() {
        return this.mGravity;
    }

    public void setHorizontalGravity(int horizontalGravity) {
        int gravity = horizontalGravity & GravityCompat.RELATIVE_HORIZONTAL_GRAVITY_MASK;
        if ((GravityCompat.RELATIVE_HORIZONTAL_GRAVITY_MASK & this.mGravity) != gravity) {
            this.mGravity = (this.mGravity & -8388616) | gravity;
            requestLayout();
        }
    }

    public void setVerticalGravity(int verticalGravity) {
        int gravity = verticalGravity & 112;
        if ((this.mGravity & 112) != gravity) {
            this.mGravity = (this.mGravity & -113) | gravity;
            requestLayout();
        }
    }

    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    protected LayoutParams generateDefaultLayoutParams() {
        if (this.mOrientation == 0) {
            return new LayoutParams(-2, -2);
        }
        if (this.mOrientation == 1) {
            return new LayoutParams(-1, -2);
        }
        return null;
    }

    protected LayoutParams generateLayoutParams(android.view.ViewGroup.LayoutParams p) {
        return new LayoutParams(p);
    }

    protected boolean checkLayoutParams(android.view.ViewGroup.LayoutParams p) {
        return p instanceof LayoutParams;
    }

    public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
        super.onInitializeAccessibilityEvent(event);
        event.setClassName(LinearLayoutCompat.class.getName());
    }

    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        info.setClassName(LinearLayoutCompat.class.getName());
    }
}
