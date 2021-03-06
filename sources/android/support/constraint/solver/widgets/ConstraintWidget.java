package android.support.constraint.solver.widgets;

import android.support.constraint.solver.Cache;
import android.support.constraint.solver.LinearSystem;
import android.support.constraint.solver.Metrics;
import android.support.constraint.solver.SolverVariable;
import android.support.constraint.solver.widgets.ConstraintAnchor.Strength;
import android.support.constraint.solver.widgets.ConstraintAnchor.Type;
import java.util.ArrayList;

public class ConstraintWidget {
    protected static final int ANCHOR_BASELINE = 4;
    protected static final int ANCHOR_BOTTOM = 3;
    protected static final int ANCHOR_LEFT = 0;
    protected static final int ANCHOR_RIGHT = 1;
    protected static final int ANCHOR_TOP = 2;
    private static final boolean AUTOTAG_CENTER = false;
    public static final int CHAIN_PACKED = 2;
    public static final int CHAIN_SPREAD = 0;
    public static final int CHAIN_SPREAD_INSIDE = 1;
    public static float DEFAULT_BIAS = 0.5f;
    static final int DIMENSION_HORIZONTAL = 0;
    static final int DIMENSION_VERTICAL = 1;
    protected static final int DIRECT = 2;
    public static final int GONE = 8;
    public static final int HORIZONTAL = 0;
    public static final int INVISIBLE = 4;
    public static final int MATCH_CONSTRAINT_PERCENT = 2;
    public static final int MATCH_CONSTRAINT_RATIO = 3;
    public static final int MATCH_CONSTRAINT_SPREAD = 0;
    public static final int MATCH_CONSTRAINT_WRAP = 1;
    protected static final int SOLVER = 1;
    public static final int UNKNOWN = -1;
    public static final int VERTICAL = 1;
    public static final int VISIBLE = 0;
    private static final int WRAP = -2;
    protected ArrayList<ConstraintAnchor> mAnchors;
    ConstraintAnchor mBaseline;
    int mBaselineDistance;
    ConstraintAnchor mBottom;
    boolean mBottomHasCentered;
    ConstraintAnchor mCenter;
    ConstraintAnchor mCenterX;
    ConstraintAnchor mCenterY;
    private float mCircleConstraintAngle;
    private Object mCompanionWidget;
    private int mContainerItemSkip;
    private String mDebugName;
    protected float mDimensionRatio;
    protected int mDimensionRatioSide;
    int mDistToBottom;
    int mDistToLeft;
    int mDistToRight;
    int mDistToTop;
    private int mDrawHeight;
    private int mDrawWidth;
    private int mDrawX;
    private int mDrawY;
    int mHeight;
    float mHorizontalBiasPercent;
    boolean mHorizontalChainFixedPosition;
    int mHorizontalChainStyle;
    ConstraintWidget mHorizontalNextWidget;
    public int mHorizontalResolution;
    boolean mHorizontalWrapVisited;
    boolean mIsHeightWrapContent;
    boolean mIsWidthWrapContent;
    ConstraintAnchor mLeft;
    boolean mLeftHasCentered;
    protected ConstraintAnchor[] mListAnchors;
    protected DimensionBehaviour[] mListDimensionBehaviors;
    protected ConstraintWidget[] mListNextMatchConstraintsWidget;
    protected ConstraintWidget[] mListNextVisibleWidget;
    int mMatchConstraintDefaultHeight;
    int mMatchConstraintDefaultWidth;
    int mMatchConstraintMaxHeight;
    int mMatchConstraintMaxWidth;
    int mMatchConstraintMinHeight;
    int mMatchConstraintMinWidth;
    float mMatchConstraintPercentHeight;
    float mMatchConstraintPercentWidth;
    private int[] mMaxDimension;
    protected int mMinHeight;
    protected int mMinWidth;
    protected int mOffsetX;
    protected int mOffsetY;
    ConstraintWidget mParent;
    ResolutionDimension mResolutionHeight;
    ResolutionDimension mResolutionWidth;
    float mResolvedDimensionRatio;
    int mResolvedDimensionRatioSide;
    ConstraintAnchor mRight;
    boolean mRightHasCentered;
    ConstraintAnchor mTop;
    boolean mTopHasCentered;
    private String mType;
    float mVerticalBiasPercent;
    boolean mVerticalChainFixedPosition;
    int mVerticalChainStyle;
    ConstraintWidget mVerticalNextWidget;
    public int mVerticalResolution;
    boolean mVerticalWrapVisited;
    private int mVisibility;
    float[] mWeight;
    int mWidth;
    private int mWrapHeight;
    private int mWrapWidth;
    protected int mX;
    protected int mY;

    public enum ContentAlignment {
        BEGIN,
        MIDDLE,
        END,
        TOP,
        VERTICAL_MIDDLE,
        BOTTOM,
        LEFT,
        RIGHT
    }

    public enum DimensionBehaviour {
        FIXED,
        WRAP_CONTENT,
        MATCH_CONSTRAINT,
        MATCH_PARENT
    }

    public int getMaxHeight() {
        return this.mMaxDimension[1];
    }

    public int getMaxWidth() {
        return this.mMaxDimension[0];
    }

    public void setMaxWidth(int maxWidth) {
        this.mMaxDimension[0] = maxWidth;
    }

    public void setMaxHeight(int maxWidth) {
        this.mMaxDimension[1] = maxWidth;
    }

    public boolean isSpreadWidth() {
        return this.mMatchConstraintDefaultWidth == 0 && this.mDimensionRatio == 0.0f && this.mMatchConstraintMinWidth == 0 && this.mMatchConstraintMaxWidth == 0 && this.mListDimensionBehaviors[0] == DimensionBehaviour.MATCH_CONSTRAINT;
    }

    public boolean isSpreadHeight() {
        return this.mMatchConstraintDefaultHeight == 0 && this.mDimensionRatio == 0.0f && this.mMatchConstraintMinHeight == 0 && this.mMatchConstraintMaxHeight == 0 && this.mListDimensionBehaviors[1] == DimensionBehaviour.MATCH_CONSTRAINT;
    }

    public void reset() {
        this.mLeft.reset();
        this.mTop.reset();
        this.mRight.reset();
        this.mBottom.reset();
        this.mBaseline.reset();
        this.mCenterX.reset();
        this.mCenterY.reset();
        this.mCenter.reset();
        this.mParent = null;
        this.mCircleConstraintAngle = 0.0f;
        this.mWidth = 0;
        this.mHeight = 0;
        this.mDimensionRatio = 0.0f;
        this.mDimensionRatioSide = -1;
        this.mX = 0;
        this.mY = 0;
        this.mDrawX = 0;
        this.mDrawY = 0;
        this.mDrawWidth = 0;
        this.mDrawHeight = 0;
        this.mOffsetX = 0;
        this.mOffsetY = 0;
        this.mBaselineDistance = 0;
        this.mMinWidth = 0;
        this.mMinHeight = 0;
        this.mWrapWidth = 0;
        this.mWrapHeight = 0;
        this.mHorizontalBiasPercent = DEFAULT_BIAS;
        this.mVerticalBiasPercent = DEFAULT_BIAS;
        this.mListDimensionBehaviors[0] = DimensionBehaviour.FIXED;
        this.mListDimensionBehaviors[1] = DimensionBehaviour.FIXED;
        this.mCompanionWidget = null;
        this.mContainerItemSkip = 0;
        this.mVisibility = 0;
        this.mType = null;
        this.mHorizontalWrapVisited = false;
        this.mVerticalWrapVisited = false;
        this.mHorizontalChainStyle = 0;
        this.mVerticalChainStyle = 0;
        this.mHorizontalChainFixedPosition = false;
        this.mVerticalChainFixedPosition = false;
        this.mWeight[0] = 0.0f;
        this.mWeight[1] = 0.0f;
        this.mHorizontalResolution = -1;
        this.mVerticalResolution = -1;
        this.mMaxDimension[0] = ActivityChooserViewAdapter.MAX_ACTIVITY_COUNT_UNLIMITED;
        this.mMaxDimension[1] = ActivityChooserViewAdapter.MAX_ACTIVITY_COUNT_UNLIMITED;
        this.mMatchConstraintDefaultWidth = 0;
        this.mMatchConstraintDefaultHeight = 0;
        this.mMatchConstraintPercentWidth = 1.0f;
        this.mMatchConstraintPercentHeight = 1.0f;
        this.mMatchConstraintMaxWidth = ActivityChooserViewAdapter.MAX_ACTIVITY_COUNT_UNLIMITED;
        this.mMatchConstraintMaxHeight = ActivityChooserViewAdapter.MAX_ACTIVITY_COUNT_UNLIMITED;
        this.mMatchConstraintMinWidth = 0;
        this.mMatchConstraintMinHeight = 0;
        this.mResolvedDimensionRatioSide = -1;
        this.mResolvedDimensionRatio = 1.0f;
        if (this.mResolutionWidth != null) {
            this.mResolutionWidth.reset();
        }
        if (this.mResolutionHeight != null) {
            this.mResolutionHeight.reset();
        }
    }

    public void resetResolutionNodes() {
        for (int i = 0; i < 6; i++) {
            this.mListAnchors[i].getResolutionNode().reset();
        }
    }

    public void updateResolutionNodes() {
        for (int i = 0; i < 6; i++) {
            this.mListAnchors[i].getResolutionNode().update();
        }
    }

    public void analyze(int optimizationLevel) {
        Optimizer.analyze(optimizationLevel, this);
    }

    public void resolve() {
    }

    public boolean isFullyResolved() {
        if (this.mLeft.getResolutionNode().state == 1 && this.mRight.getResolutionNode().state == 1 && this.mTop.getResolutionNode().state == 1 && this.mBottom.getResolutionNode().state == 1) {
            return true;
        }
        return false;
    }

    public ResolutionDimension getResolutionWidth() {
        if (this.mResolutionWidth == null) {
            this.mResolutionWidth = new ResolutionDimension();
        }
        return this.mResolutionWidth;
    }

    public ResolutionDimension getResolutionHeight() {
        if (this.mResolutionHeight == null) {
            this.mResolutionHeight = new ResolutionDimension();
        }
        return this.mResolutionHeight;
    }

    public ConstraintWidget() {
        this.mHorizontalResolution = -1;
        this.mVerticalResolution = -1;
        this.mMatchConstraintDefaultWidth = 0;
        this.mMatchConstraintDefaultHeight = 0;
        this.mMatchConstraintMinWidth = 0;
        this.mMatchConstraintMaxWidth = 0;
        this.mMatchConstraintPercentWidth = 1.0f;
        this.mMatchConstraintMinHeight = 0;
        this.mMatchConstraintMaxHeight = 0;
        this.mMatchConstraintPercentHeight = 1.0f;
        this.mResolvedDimensionRatioSide = -1;
        this.mResolvedDimensionRatio = 1.0f;
        this.mMaxDimension = new int[]{ActivityChooserViewAdapter.MAX_ACTIVITY_COUNT_UNLIMITED, ActivityChooserViewAdapter.MAX_ACTIVITY_COUNT_UNLIMITED};
        this.mCircleConstraintAngle = 0.0f;
        this.mLeft = new ConstraintAnchor(this, Type.LEFT);
        this.mTop = new ConstraintAnchor(this, Type.TOP);
        this.mRight = new ConstraintAnchor(this, Type.RIGHT);
        this.mBottom = new ConstraintAnchor(this, Type.BOTTOM);
        this.mBaseline = new ConstraintAnchor(this, Type.BASELINE);
        this.mCenterX = new ConstraintAnchor(this, Type.CENTER_X);
        this.mCenterY = new ConstraintAnchor(this, Type.CENTER_Y);
        this.mCenter = new ConstraintAnchor(this, Type.CENTER);
        this.mListAnchors = new ConstraintAnchor[]{this.mLeft, this.mRight, this.mTop, this.mBottom, this.mBaseline, this.mCenter};
        this.mAnchors = new ArrayList();
        this.mListDimensionBehaviors = new DimensionBehaviour[]{DimensionBehaviour.FIXED, DimensionBehaviour.FIXED};
        this.mParent = null;
        this.mWidth = 0;
        this.mHeight = 0;
        this.mDimensionRatio = 0.0f;
        this.mDimensionRatioSide = -1;
        this.mX = 0;
        this.mY = 0;
        this.mDrawX = 0;
        this.mDrawY = 0;
        this.mDrawWidth = 0;
        this.mDrawHeight = 0;
        this.mOffsetX = 0;
        this.mOffsetY = 0;
        this.mBaselineDistance = 0;
        this.mHorizontalBiasPercent = DEFAULT_BIAS;
        this.mVerticalBiasPercent = DEFAULT_BIAS;
        this.mContainerItemSkip = 0;
        this.mVisibility = 0;
        this.mDebugName = null;
        this.mType = null;
        this.mHorizontalChainStyle = 0;
        this.mVerticalChainStyle = 0;
        this.mWeight = new float[]{0.0f, 0.0f};
        this.mListNextMatchConstraintsWidget = new ConstraintWidget[]{null, null};
        this.mListNextVisibleWidget = new ConstraintWidget[]{null, null};
        this.mHorizontalNextWidget = null;
        this.mVerticalNextWidget = null;
        addAnchors();
    }

    public ConstraintWidget(int x, int y, int width, int height) {
        this.mHorizontalResolution = -1;
        this.mVerticalResolution = -1;
        this.mMatchConstraintDefaultWidth = 0;
        this.mMatchConstraintDefaultHeight = 0;
        this.mMatchConstraintMinWidth = 0;
        this.mMatchConstraintMaxWidth = 0;
        this.mMatchConstraintPercentWidth = 1.0f;
        this.mMatchConstraintMinHeight = 0;
        this.mMatchConstraintMaxHeight = 0;
        this.mMatchConstraintPercentHeight = 1.0f;
        this.mResolvedDimensionRatioSide = -1;
        this.mResolvedDimensionRatio = 1.0f;
        this.mMaxDimension = new int[]{ActivityChooserViewAdapter.MAX_ACTIVITY_COUNT_UNLIMITED, ActivityChooserViewAdapter.MAX_ACTIVITY_COUNT_UNLIMITED};
        this.mCircleConstraintAngle = 0.0f;
        this.mLeft = new ConstraintAnchor(this, Type.LEFT);
        this.mTop = new ConstraintAnchor(this, Type.TOP);
        this.mRight = new ConstraintAnchor(this, Type.RIGHT);
        this.mBottom = new ConstraintAnchor(this, Type.BOTTOM);
        this.mBaseline = new ConstraintAnchor(this, Type.BASELINE);
        this.mCenterX = new ConstraintAnchor(this, Type.CENTER_X);
        this.mCenterY = new ConstraintAnchor(this, Type.CENTER_Y);
        this.mCenter = new ConstraintAnchor(this, Type.CENTER);
        this.mListAnchors = new ConstraintAnchor[]{this.mLeft, this.mRight, this.mTop, this.mBottom, this.mBaseline, this.mCenter};
        this.mAnchors = new ArrayList();
        this.mListDimensionBehaviors = new DimensionBehaviour[]{DimensionBehaviour.FIXED, DimensionBehaviour.FIXED};
        this.mParent = null;
        this.mWidth = 0;
        this.mHeight = 0;
        this.mDimensionRatio = 0.0f;
        this.mDimensionRatioSide = -1;
        this.mX = 0;
        this.mY = 0;
        this.mDrawX = 0;
        this.mDrawY = 0;
        this.mDrawWidth = 0;
        this.mDrawHeight = 0;
        this.mOffsetX = 0;
        this.mOffsetY = 0;
        this.mBaselineDistance = 0;
        this.mHorizontalBiasPercent = DEFAULT_BIAS;
        this.mVerticalBiasPercent = DEFAULT_BIAS;
        this.mContainerItemSkip = 0;
        this.mVisibility = 0;
        this.mDebugName = null;
        this.mType = null;
        this.mHorizontalChainStyle = 0;
        this.mVerticalChainStyle = 0;
        this.mWeight = new float[]{0.0f, 0.0f};
        this.mListNextMatchConstraintsWidget = new ConstraintWidget[]{null, null};
        this.mListNextVisibleWidget = new ConstraintWidget[]{null, null};
        this.mHorizontalNextWidget = null;
        this.mVerticalNextWidget = null;
        this.mX = x;
        this.mY = y;
        this.mWidth = width;
        this.mHeight = height;
        addAnchors();
        forceUpdateDrawPosition();
    }

    public ConstraintWidget(int width, int height) {
        this(0, 0, width, height);
    }

    public void resetSolverVariables(Cache cache) {
        this.mLeft.resetSolverVariable(cache);
        this.mTop.resetSolverVariable(cache);
        this.mRight.resetSolverVariable(cache);
        this.mBottom.resetSolverVariable(cache);
        this.mBaseline.resetSolverVariable(cache);
        this.mCenter.resetSolverVariable(cache);
        this.mCenterX.resetSolverVariable(cache);
        this.mCenterY.resetSolverVariable(cache);
    }

    private void addAnchors() {
        this.mAnchors.add(this.mLeft);
        this.mAnchors.add(this.mTop);
        this.mAnchors.add(this.mRight);
        this.mAnchors.add(this.mBottom);
        this.mAnchors.add(this.mCenterX);
        this.mAnchors.add(this.mCenterY);
        this.mAnchors.add(this.mCenter);
        this.mAnchors.add(this.mBaseline);
    }

    public boolean isRoot() {
        return this.mParent == null;
    }

    public boolean isRootContainer() {
        return (this instanceof ConstraintWidgetContainer) && (this.mParent == null || !(this.mParent instanceof ConstraintWidgetContainer));
    }

    public boolean isInsideConstraintLayout() {
        ConstraintWidget widget = getParent();
        if (widget == null) {
            return false;
        }
        while (widget != null) {
            if (widget instanceof ConstraintWidgetContainer) {
                return true;
            }
            widget = widget.getParent();
        }
        return false;
    }

    public boolean hasAncestor(ConstraintWidget widget) {
        ConstraintWidget parent = getParent();
        if (parent == widget) {
            return true;
        }
        if (parent == widget.getParent()) {
            return false;
        }
        while (parent != null) {
            if (parent == widget || parent == widget.getParent()) {
                return true;
            }
            parent = parent.getParent();
        }
        return false;
    }

    public WidgetContainer getRootWidgetContainer() {
        ConstraintWidget root = this;
        while (root.getParent() != null) {
            root = root.getParent();
        }
        if (root instanceof WidgetContainer) {
            return (WidgetContainer) root;
        }
        return null;
    }

    public ConstraintWidget getParent() {
        return this.mParent;
    }

    public void setParent(ConstraintWidget widget) {
        this.mParent = widget;
    }

    public void setWidthWrapContent(boolean widthWrapContent) {
        this.mIsWidthWrapContent = widthWrapContent;
    }

    public boolean isWidthWrapContent() {
        return this.mIsWidthWrapContent;
    }

    public void setHeightWrapContent(boolean heightWrapContent) {
        this.mIsHeightWrapContent = heightWrapContent;
    }

    public boolean isHeightWrapContent() {
        return this.mIsHeightWrapContent;
    }

    public void connectCircularConstraint(ConstraintWidget target, float angle, int radius) {
        immediateConnect(Type.CENTER, target, Type.CENTER, radius, 0);
        this.mCircleConstraintAngle = angle;
    }

    public String getType() {
        return this.mType;
    }

    public void setType(String type) {
        this.mType = type;
    }

    public void setVisibility(int visibility) {
        this.mVisibility = visibility;
    }

    public int getVisibility() {
        return this.mVisibility;
    }

    public String getDebugName() {
        return this.mDebugName;
    }

    public void setDebugName(String name) {
        this.mDebugName = name;
    }

    public void setDebugSolverName(LinearSystem system, String name) {
        this.mDebugName = name;
        SolverVariable left = system.createObjectVariable(this.mLeft);
        SolverVariable top = system.createObjectVariable(this.mTop);
        SolverVariable right = system.createObjectVariable(this.mRight);
        SolverVariable bottom = system.createObjectVariable(this.mBottom);
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(name);
        stringBuilder.append(".left");
        left.setName(stringBuilder.toString());
        stringBuilder = new StringBuilder();
        stringBuilder.append(name);
        stringBuilder.append(".top");
        top.setName(stringBuilder.toString());
        stringBuilder = new StringBuilder();
        stringBuilder.append(name);
        stringBuilder.append(".right");
        right.setName(stringBuilder.toString());
        stringBuilder = new StringBuilder();
        stringBuilder.append(name);
        stringBuilder.append(".bottom");
        bottom.setName(stringBuilder.toString());
        if (this.mBaselineDistance > 0) {
            SolverVariable baseline = system.createObjectVariable(this.mBaseline);
            StringBuilder stringBuilder2 = new StringBuilder();
            stringBuilder2.append(name);
            stringBuilder2.append(".baseline");
            baseline.setName(stringBuilder2.toString());
        }
    }

    public String toString() {
        String stringBuilder;
        StringBuilder stringBuilder2 = new StringBuilder();
        if (this.mType != null) {
            StringBuilder stringBuilder3 = new StringBuilder();
            stringBuilder3.append("type: ");
            stringBuilder3.append(this.mType);
            stringBuilder3.append(" ");
            stringBuilder = stringBuilder3.toString();
        } else {
            stringBuilder = "";
        }
        stringBuilder2.append(stringBuilder);
        if (this.mDebugName != null) {
            stringBuilder3 = new StringBuilder();
            stringBuilder3.append("id: ");
            stringBuilder3.append(this.mDebugName);
            stringBuilder3.append(" ");
            stringBuilder = stringBuilder3.toString();
        } else {
            stringBuilder = "";
        }
        stringBuilder2.append(stringBuilder);
        stringBuilder2.append("(");
        stringBuilder2.append(this.mX);
        stringBuilder2.append(", ");
        stringBuilder2.append(this.mY);
        stringBuilder2.append(") - (");
        stringBuilder2.append(this.mWidth);
        stringBuilder2.append(" x ");
        stringBuilder2.append(this.mHeight);
        stringBuilder2.append(") wrap: (");
        stringBuilder2.append(this.mWrapWidth);
        stringBuilder2.append(" x ");
        stringBuilder2.append(this.mWrapHeight);
        stringBuilder2.append(")");
        return stringBuilder2.toString();
    }

    int getInternalDrawX() {
        return this.mDrawX;
    }

    int getInternalDrawY() {
        return this.mDrawY;
    }

    public int getInternalDrawRight() {
        return this.mDrawX + this.mDrawWidth;
    }

    public int getInternalDrawBottom() {
        return this.mDrawY + this.mDrawHeight;
    }

    public int getX() {
        return this.mX;
    }

    public int getY() {
        return this.mY;
    }

    public int getWidth() {
        if (this.mVisibility == 8) {
            return 0;
        }
        return this.mWidth;
    }

    public int getOptimizerWrapWidth() {
        int w = this.mWidth;
        if (this.mListDimensionBehaviors[0] != DimensionBehaviour.MATCH_CONSTRAINT) {
            return w;
        }
        if (this.mMatchConstraintDefaultWidth == 1) {
            w = Math.max(this.mMatchConstraintMinWidth, w);
        } else if (this.mMatchConstraintMinWidth > 0) {
            w = this.mMatchConstraintMinWidth;
            this.mWidth = w;
        } else {
            w = 0;
        }
        if (this.mMatchConstraintMaxWidth <= 0 || this.mMatchConstraintMaxWidth >= w) {
            return w;
        }
        return this.mMatchConstraintMaxWidth;
    }

    public int getOptimizerWrapHeight() {
        int h = this.mHeight;
        if (this.mListDimensionBehaviors[1] != DimensionBehaviour.MATCH_CONSTRAINT) {
            return h;
        }
        if (this.mMatchConstraintDefaultHeight == 1) {
            h = Math.max(this.mMatchConstraintMinHeight, h);
        } else if (this.mMatchConstraintMinHeight > 0) {
            h = this.mMatchConstraintMinHeight;
            this.mHeight = h;
        } else {
            h = 0;
        }
        if (this.mMatchConstraintMaxHeight <= 0 || this.mMatchConstraintMaxHeight >= h) {
            return h;
        }
        return this.mMatchConstraintMaxHeight;
    }

    public int getWrapWidth() {
        return this.mWrapWidth;
    }

    public int getHeight() {
        if (this.mVisibility == 8) {
            return 0;
        }
        return this.mHeight;
    }

    public int getWrapHeight() {
        return this.mWrapHeight;
    }

    public int getDrawX() {
        return this.mDrawX + this.mOffsetX;
    }

    public int getDrawY() {
        return this.mDrawY + this.mOffsetY;
    }

    public int getDrawWidth() {
        return this.mDrawWidth;
    }

    public int getDrawHeight() {
        return this.mDrawHeight;
    }

    public int getDrawBottom() {
        return getDrawY() + this.mDrawHeight;
    }

    public int getDrawRight() {
        return getDrawX() + this.mDrawWidth;
    }

    protected int getRootX() {
        return this.mX + this.mOffsetX;
    }

    protected int getRootY() {
        return this.mY + this.mOffsetY;
    }

    public int getMinWidth() {
        return this.mMinWidth;
    }

    public int getMinHeight() {
        return this.mMinHeight;
    }

    public int getLeft() {
        return getX();
    }

    public int getTop() {
        return getY();
    }

    public int getRight() {
        return getX() + this.mWidth;
    }

    public int getBottom() {
        return getY() + this.mHeight;
    }

    public float getHorizontalBiasPercent() {
        return this.mHorizontalBiasPercent;
    }

    public float getVerticalBiasPercent() {
        return this.mVerticalBiasPercent;
    }

    public boolean hasBaseline() {
        return this.mBaselineDistance > 0;
    }

    public int getBaselineDistance() {
        return this.mBaselineDistance;
    }

    public Object getCompanionWidget() {
        return this.mCompanionWidget;
    }

    public ArrayList<ConstraintAnchor> getAnchors() {
        return this.mAnchors;
    }

    public void setX(int x) {
        this.mX = x;
    }

    public void setY(int y) {
        this.mY = y;
    }

    public void setOrigin(int x, int y) {
        this.mX = x;
        this.mY = y;
    }

    public void setOffset(int x, int y) {
        this.mOffsetX = x;
        this.mOffsetY = y;
    }

    public void setGoneMargin(Type type, int goneMargin) {
        switch (type) {
            case LEFT:
                this.mLeft.mGoneMargin = goneMargin;
                return;
            case TOP:
                this.mTop.mGoneMargin = goneMargin;
                return;
            case RIGHT:
                this.mRight.mGoneMargin = goneMargin;
                return;
            case BOTTOM:
                this.mBottom.mGoneMargin = goneMargin;
                return;
            default:
                return;
        }
    }

    public void updateDrawPosition() {
        int left = this.mX;
        int top = this.mY;
        int right = this.mX + this.mWidth;
        int bottom = this.mY + this.mHeight;
        this.mDrawX = left;
        this.mDrawY = top;
        this.mDrawWidth = right - left;
        this.mDrawHeight = bottom - top;
    }

    public void forceUpdateDrawPosition() {
        int left = this.mX;
        int top = this.mY;
        int right = this.mX + this.mWidth;
        int bottom = this.mY + this.mHeight;
        this.mDrawX = left;
        this.mDrawY = top;
        this.mDrawWidth = right - left;
        this.mDrawHeight = bottom - top;
    }

    public void setDrawOrigin(int x, int y) {
        this.mDrawX = x - this.mOffsetX;
        this.mDrawY = y - this.mOffsetY;
        this.mX = this.mDrawX;
        this.mY = this.mDrawY;
    }

    public void setDrawX(int x) {
        this.mDrawX = x - this.mOffsetX;
        this.mX = this.mDrawX;
    }

    public void setDrawY(int y) {
        this.mDrawY = y - this.mOffsetY;
        this.mY = this.mDrawY;
    }

    public void setDrawWidth(int drawWidth) {
        this.mDrawWidth = drawWidth;
    }

    public void setDrawHeight(int drawHeight) {
        this.mDrawHeight = drawHeight;
    }

    public void setWidth(int w) {
        this.mWidth = w;
        if (this.mWidth < this.mMinWidth) {
            this.mWidth = this.mMinWidth;
        }
    }

    public void setHeight(int h) {
        this.mHeight = h;
        if (this.mHeight < this.mMinHeight) {
            this.mHeight = this.mMinHeight;
        }
    }

    public void setHorizontalMatchStyle(int horizontalMatchStyle, int min, int max, float percent) {
        this.mMatchConstraintDefaultWidth = horizontalMatchStyle;
        this.mMatchConstraintMinWidth = min;
        this.mMatchConstraintMaxWidth = max;
        this.mMatchConstraintPercentWidth = percent;
        if (percent < 1.0f && this.mMatchConstraintDefaultWidth == 0) {
            this.mMatchConstraintDefaultWidth = 2;
        }
    }

    public void setVerticalMatchStyle(int verticalMatchStyle, int min, int max, float percent) {
        this.mMatchConstraintDefaultHeight = verticalMatchStyle;
        this.mMatchConstraintMinHeight = min;
        this.mMatchConstraintMaxHeight = max;
        this.mMatchConstraintPercentHeight = percent;
        if (percent < 1.0f && this.mMatchConstraintDefaultHeight == 0) {
            this.mMatchConstraintDefaultHeight = 2;
        }
    }

    public void setDimensionRatio(String ratio) {
        if (ratio != null) {
            if (ratio.length() != 0) {
                int dimensionRatioSide = -1;
                float dimensionRatio = 0.0f;
                int len = ratio.length();
                int commaIndex = ratio.indexOf(44);
                if (commaIndex <= 0 || commaIndex >= len - 1) {
                    commaIndex = 0;
                } else {
                    String dimension = ratio.substring(null, commaIndex);
                    if (dimension.equalsIgnoreCase("W")) {
                        dimensionRatioSide = 0;
                    } else if (dimension.equalsIgnoreCase("H")) {
                        dimensionRatioSide = 1;
                    }
                    commaIndex++;
                }
                int colonIndex = ratio.indexOf(58);
                if (colonIndex < 0 || colonIndex >= len - 1) {
                    String r = ratio.substring(commaIndex);
                    if (r.length() > 0) {
                        try {
                            dimensionRatio = Float.parseFloat(r);
                        } catch (NumberFormatException e) {
                        }
                    }
                } else {
                    String nominator = ratio.substring(commaIndex, colonIndex);
                    String denominator = ratio.substring(colonIndex + 1);
                    if (nominator.length() > 0 && denominator.length() > 0) {
                        try {
                            float nominatorValue = Float.parseFloat(nominator);
                            float denominatorValue = Float.parseFloat(denominator);
                            if (nominatorValue > 0.0f && denominatorValue > 0.0f) {
                                dimensionRatio = dimensionRatioSide == 1 ? Math.abs(denominatorValue / nominatorValue) : Math.abs(nominatorValue / denominatorValue);
                            }
                        } catch (NumberFormatException e2) {
                        }
                    }
                }
                if (dimensionRatio > 0.0f) {
                    this.mDimensionRatio = dimensionRatio;
                    this.mDimensionRatioSide = dimensionRatioSide;
                }
                return;
            }
        }
        this.mDimensionRatio = 0.0f;
    }

    public void setDimensionRatio(float ratio, int dimensionRatioSide) {
        this.mDimensionRatio = ratio;
        this.mDimensionRatioSide = dimensionRatioSide;
    }

    public float getDimensionRatio() {
        return this.mDimensionRatio;
    }

    public int getDimensionRatioSide() {
        return this.mDimensionRatioSide;
    }

    public void setHorizontalBiasPercent(float horizontalBiasPercent) {
        this.mHorizontalBiasPercent = horizontalBiasPercent;
    }

    public void setVerticalBiasPercent(float verticalBiasPercent) {
        this.mVerticalBiasPercent = verticalBiasPercent;
    }

    public void setMinWidth(int w) {
        if (w < 0) {
            this.mMinWidth = 0;
        } else {
            this.mMinWidth = w;
        }
    }

    public void setMinHeight(int h) {
        if (h < 0) {
            this.mMinHeight = 0;
        } else {
            this.mMinHeight = h;
        }
    }

    public void setWrapWidth(int w) {
        this.mWrapWidth = w;
    }

    public void setWrapHeight(int h) {
        this.mWrapHeight = h;
    }

    public void setDimension(int w, int h) {
        this.mWidth = w;
        if (this.mWidth < this.mMinWidth) {
            this.mWidth = this.mMinWidth;
        }
        this.mHeight = h;
        if (this.mHeight < this.mMinHeight) {
            this.mHeight = this.mMinHeight;
        }
    }

    public void setFrame(int left, int top, int right, int bottom) {
        int w = right - left;
        int h = bottom - top;
        this.mX = left;
        this.mY = top;
        if (this.mVisibility == 8) {
            this.mWidth = 0;
            this.mHeight = 0;
            return;
        }
        if (this.mListDimensionBehaviors[0] == DimensionBehaviour.FIXED && w < this.mWidth) {
            w = this.mWidth;
        }
        if (this.mListDimensionBehaviors[1] == DimensionBehaviour.FIXED && h < this.mHeight) {
            h = this.mHeight;
        }
        this.mWidth = w;
        this.mHeight = h;
        if (this.mHeight < this.mMinHeight) {
            this.mHeight = this.mMinHeight;
        }
        if (this.mWidth < this.mMinWidth) {
            this.mWidth = this.mMinWidth;
        }
    }

    public void setHorizontalDimension(int left, int right) {
        this.mX = left;
        this.mWidth = right - left;
        if (this.mWidth < this.mMinWidth) {
            this.mWidth = this.mMinWidth;
        }
    }

    public void setVerticalDimension(int top, int bottom) {
        this.mY = top;
        this.mHeight = bottom - top;
        if (this.mHeight < this.mMinHeight) {
            this.mHeight = this.mMinHeight;
        }
    }

    public void setBaselineDistance(int baseline) {
        this.mBaselineDistance = baseline;
    }

    public void setCompanionWidget(Object companion) {
        this.mCompanionWidget = companion;
    }

    public void setContainerItemSkip(int skip) {
        if (skip >= 0) {
            this.mContainerItemSkip = skip;
        } else {
            this.mContainerItemSkip = 0;
        }
    }

    public int getContainerItemSkip() {
        return this.mContainerItemSkip;
    }

    public void setHorizontalWeight(float horizontalWeight) {
        this.mWeight[0] = horizontalWeight;
    }

    public void setVerticalWeight(float verticalWeight) {
        this.mWeight[1] = verticalWeight;
    }

    public void setHorizontalChainStyle(int horizontalChainStyle) {
        this.mHorizontalChainStyle = horizontalChainStyle;
    }

    public int getHorizontalChainStyle() {
        return this.mHorizontalChainStyle;
    }

    public void setVerticalChainStyle(int verticalChainStyle) {
        this.mVerticalChainStyle = verticalChainStyle;
    }

    public int getVerticalChainStyle() {
        return this.mVerticalChainStyle;
    }

    public boolean allowedInBarrier() {
        return this.mVisibility != 8;
    }

    public void connectedTo(ConstraintWidget source) {
    }

    public void immediateConnect(Type startType, ConstraintWidget target, Type endType, int margin, int goneMargin) {
        ConstraintAnchor startAnchor = getAnchor(startType);
        startAnchor.connect(target.getAnchor(endType), margin, goneMargin, Strength.STRONG, 0, true);
    }

    public void connect(ConstraintAnchor from, ConstraintAnchor to, int margin, int creator) {
        connect(from, to, margin, Strength.STRONG, creator);
    }

    public void connect(ConstraintAnchor from, ConstraintAnchor to, int margin) {
        connect(from, to, margin, Strength.STRONG, 0);
    }

    public void connect(ConstraintAnchor from, ConstraintAnchor to, int margin, Strength strength, int creator) {
        if (from.getOwner() == this) {
            connect(from.getType(), to.getOwner(), to.getType(), margin, strength, creator);
        }
    }

    public void connect(Type constraintFrom, ConstraintWidget target, Type constraintTo, int margin) {
        connect(constraintFrom, target, constraintTo, margin, Strength.STRONG);
    }

    public void connect(Type constraintFrom, ConstraintWidget target, Type constraintTo) {
        connect(constraintFrom, target, constraintTo, 0, Strength.STRONG);
    }

    public void connect(Type constraintFrom, ConstraintWidget target, Type constraintTo, int margin, Strength strength) {
        connect(constraintFrom, target, constraintTo, margin, strength, 0);
    }

    public void connect(Type constraintFrom, ConstraintWidget target, Type constraintTo, int margin, Strength strength, int creator) {
        ConstraintWidget constraintWidget = this;
        Type type = constraintFrom;
        ConstraintWidget constraintWidget2 = target;
        Type type2 = constraintTo;
        int i = creator;
        if (type == Type.CENTER) {
            ConstraintWidget constraintWidget3;
            Strength strength2;
            int i2;
            if (type2 == Type.CENTER) {
                ConstraintAnchor bottom;
                ConstraintAnchor left = getAnchor(Type.LEFT);
                ConstraintAnchor right = getAnchor(Type.RIGHT);
                ConstraintAnchor top = getAnchor(Type.TOP);
                ConstraintAnchor bottom2 = getAnchor(Type.BOTTOM);
                boolean centerX = false;
                boolean centerY = false;
                if ((left == null || !left.isConnected()) && (right == null || !right.isConnected())) {
                    constraintWidget3 = constraintWidget2;
                    strength2 = strength;
                    bottom = bottom2;
                    i2 = i;
                    connect(Type.LEFT, constraintWidget3, Type.LEFT, 0, strength2, i2);
                    connect(Type.RIGHT, constraintWidget3, Type.RIGHT, 0, strength2, i2);
                    centerX = true;
                } else {
                    bottom = bottom2;
                }
                if (top == null || !top.isConnected()) {
                    if (bottom == null || !bottom.isConnected()) {
                        constraintWidget3 = constraintWidget2;
                        strength2 = strength;
                        i2 = i;
                        connect(Type.TOP, constraintWidget3, Type.TOP, 0, strength2, i2);
                        connect(Type.BOTTOM, constraintWidget3, Type.BOTTOM, 0, strength2, i2);
                        centerY = true;
                    }
                }
                if (centerX && centerY) {
                    getAnchor(Type.CENTER).connect(constraintWidget2.getAnchor(Type.CENTER), 0, i);
                } else if (centerX) {
                    getAnchor(Type.CENTER_X).connect(constraintWidget2.getAnchor(Type.CENTER_X), 0, i);
                } else if (centerY) {
                    getAnchor(Type.CENTER_Y).connect(constraintWidget2.getAnchor(Type.CENTER_Y), 0, i);
                }
            } else {
                Type type3;
                if (type2 != Type.LEFT) {
                    if (type2 != Type.RIGHT) {
                        if (type2 == Type.TOP || type2 == Type.BOTTOM) {
                            constraintWidget3 = constraintWidget2;
                            type3 = type2;
                            strength2 = strength;
                            i2 = i;
                            connect(Type.TOP, constraintWidget3, type3, 0, strength2, i2);
                            connect(Type.BOTTOM, constraintWidget3, type3, 0, strength2, i2);
                            getAnchor(Type.CENTER).connect(target.getAnchor(constraintTo), 0, i);
                        }
                    }
                }
                constraintWidget3 = constraintWidget2;
                type3 = type2;
                strength2 = strength;
                i2 = i;
                connect(Type.LEFT, constraintWidget3, type3, 0, strength2, i2);
                connect(Type.RIGHT, constraintWidget3, type3, 0, strength2, i2);
                getAnchor(Type.CENTER).connect(target.getAnchor(constraintTo), 0, i);
            }
        } else if (type == Type.CENTER_X && (type2 == Type.LEFT || type2 == Type.RIGHT)) {
            left = getAnchor(Type.LEFT);
            targetAnchor = target.getAnchor(constraintTo);
            right = getAnchor(Type.RIGHT);
            left.connect(targetAnchor, 0, i);
            right.connect(targetAnchor, 0, i);
            getAnchor(Type.CENTER_X).connect(targetAnchor, 0, i);
        } else if (type == Type.CENTER_Y && (type2 == Type.TOP || type2 == Type.BOTTOM)) {
            left = target.getAnchor(constraintTo);
            getAnchor(Type.TOP).connect(left, 0, i);
            getAnchor(Type.BOTTOM).connect(left, 0, i);
            getAnchor(Type.CENTER_Y).connect(left, 0, i);
        } else if (type == Type.CENTER_X && type2 == Type.CENTER_X) {
            getAnchor(Type.LEFT).connect(constraintWidget2.getAnchor(Type.LEFT), 0, i);
            getAnchor(Type.RIGHT).connect(constraintWidget2.getAnchor(Type.RIGHT), 0, i);
            getAnchor(Type.CENTER_X).connect(target.getAnchor(constraintTo), 0, i);
        } else if (type == Type.CENTER_Y && type2 == Type.CENTER_Y) {
            getAnchor(Type.TOP).connect(constraintWidget2.getAnchor(Type.TOP), 0, i);
            getAnchor(Type.BOTTOM).connect(constraintWidget2.getAnchor(Type.BOTTOM), 0, i);
            getAnchor(Type.CENTER_Y).connect(target.getAnchor(constraintTo), 0, i);
        } else {
            left = getAnchor(constraintFrom);
            targetAnchor = target.getAnchor(constraintTo);
            if (left.isValidConnection(targetAnchor)) {
                int margin2;
                ConstraintAnchor bottom3;
                if (type == Type.BASELINE) {
                    right = getAnchor(Type.TOP);
                    bottom3 = getAnchor(Type.BOTTOM);
                    if (right != null) {
                        right.reset();
                    }
                    if (bottom3 != null) {
                        bottom3.reset();
                    }
                    margin2 = 0;
                } else {
                    ConstraintAnchor centerX2;
                    if (type != Type.TOP) {
                        if (type != Type.BOTTOM) {
                            if (type == Type.LEFT || type == Type.RIGHT) {
                                right = getAnchor(Type.CENTER);
                                if (right.getTarget() != targetAnchor) {
                                    right.reset();
                                }
                                bottom3 = getAnchor(constraintFrom).getOpposite();
                                centerX2 = getAnchor(Type.CENTER_X);
                                if (centerX2.isConnected()) {
                                    bottom3.reset();
                                    centerX2.reset();
                                }
                            }
                            margin2 = margin;
                        }
                    }
                    right = getAnchor(Type.BASELINE);
                    if (right != null) {
                        right.reset();
                    }
                    bottom3 = getAnchor(Type.CENTER);
                    if (bottom3.getTarget() != targetAnchor) {
                        bottom3.reset();
                    }
                    centerX2 = getAnchor(constraintFrom).getOpposite();
                    ConstraintAnchor centerY2 = getAnchor(Type.CENTER_Y);
                    if (centerY2.isConnected()) {
                        centerX2.reset();
                        centerY2.reset();
                    }
                    margin2 = margin;
                }
                left.connect(targetAnchor, margin2, strength, i);
                targetAnchor.getOwner().connectedTo(left.getOwner());
                return;
            }
        }
        Strength strength3 = strength;
    }

    public void resetAllConstraints() {
        resetAnchors();
        setVerticalBiasPercent(DEFAULT_BIAS);
        setHorizontalBiasPercent(DEFAULT_BIAS);
        if (!(this instanceof ConstraintWidgetContainer)) {
            if (getHorizontalDimensionBehaviour() == DimensionBehaviour.MATCH_CONSTRAINT) {
                if (getWidth() == getWrapWidth()) {
                    setHorizontalDimensionBehaviour(DimensionBehaviour.WRAP_CONTENT);
                } else if (getWidth() > getMinWidth()) {
                    setHorizontalDimensionBehaviour(DimensionBehaviour.FIXED);
                }
            }
            if (getVerticalDimensionBehaviour() == DimensionBehaviour.MATCH_CONSTRAINT) {
                if (getHeight() == getWrapHeight()) {
                    setVerticalDimensionBehaviour(DimensionBehaviour.WRAP_CONTENT);
                } else if (getHeight() > getMinHeight()) {
                    setVerticalDimensionBehaviour(DimensionBehaviour.FIXED);
                }
            }
        }
    }

    public void resetAnchor(ConstraintAnchor anchor) {
        if (getParent() == null || !(getParent() instanceof ConstraintWidgetContainer) || !((ConstraintWidgetContainer) getParent()).handlesInternalConstraints()) {
            ConstraintAnchor left = getAnchor(Type.LEFT);
            ConstraintAnchor right = getAnchor(Type.RIGHT);
            ConstraintAnchor top = getAnchor(Type.TOP);
            ConstraintAnchor bottom = getAnchor(Type.BOTTOM);
            ConstraintAnchor center = getAnchor(Type.CENTER);
            ConstraintAnchor centerX = getAnchor(Type.CENTER_X);
            ConstraintAnchor centerY = getAnchor(Type.CENTER_Y);
            if (anchor == center) {
                if (left.isConnected() && right.isConnected() && left.getTarget() == right.getTarget()) {
                    left.reset();
                    right.reset();
                }
                if (top.isConnected() && bottom.isConnected() && top.getTarget() == bottom.getTarget()) {
                    top.reset();
                    bottom.reset();
                }
                this.mHorizontalBiasPercent = 0.5f;
                this.mVerticalBiasPercent = 0.5f;
            } else if (anchor == centerX) {
                if (left.isConnected() && right.isConnected() && left.getTarget().getOwner() == right.getTarget().getOwner()) {
                    left.reset();
                    right.reset();
                }
                this.mHorizontalBiasPercent = 0.5f;
            } else if (anchor == centerY) {
                if (top.isConnected() && bottom.isConnected() && top.getTarget().getOwner() == bottom.getTarget().getOwner()) {
                    top.reset();
                    bottom.reset();
                }
                this.mVerticalBiasPercent = 0.5f;
            } else {
                if (anchor != left) {
                    if (anchor != right) {
                        if ((anchor == top || anchor == bottom) && top.isConnected() && top.getTarget() == bottom.getTarget()) {
                            center.reset();
                        }
                    }
                }
                if (left.isConnected() && left.getTarget() == right.getTarget()) {
                    center.reset();
                }
            }
            anchor.reset();
        }
    }

    public void resetAnchors() {
        ConstraintWidget parent = getParent();
        if (parent == null || !(parent instanceof ConstraintWidgetContainer) || !((ConstraintWidgetContainer) getParent()).handlesInternalConstraints()) {
            int mAnchorsSize = this.mAnchors.size();
            for (int i = 0; i < mAnchorsSize; i++) {
                ((ConstraintAnchor) this.mAnchors.get(i)).reset();
            }
        }
    }

    public void resetAnchors(int connectionCreator) {
        ConstraintWidget parent = getParent();
        if (parent == null || !(parent instanceof ConstraintWidgetContainer) || !((ConstraintWidgetContainer) getParent()).handlesInternalConstraints()) {
            int mAnchorsSize = this.mAnchors.size();
            for (int i = 0; i < mAnchorsSize; i++) {
                ConstraintAnchor anchor = (ConstraintAnchor) this.mAnchors.get(i);
                if (connectionCreator == anchor.getConnectionCreator()) {
                    if (anchor.isVerticalAnchor()) {
                        setVerticalBiasPercent(DEFAULT_BIAS);
                    } else {
                        setHorizontalBiasPercent(DEFAULT_BIAS);
                    }
                    anchor.reset();
                }
            }
        }
    }

    public void disconnectWidget(ConstraintWidget widget) {
        ArrayList<ConstraintAnchor> anchors = getAnchors();
        int anchorsSize = anchors.size();
        for (int i = 0; i < anchorsSize; i++) {
            ConstraintAnchor anchor = (ConstraintAnchor) anchors.get(i);
            if (anchor.isConnected() && anchor.getTarget().getOwner() == widget) {
                anchor.reset();
            }
        }
    }

    public void disconnectUnlockedWidget(ConstraintWidget widget) {
        ArrayList<ConstraintAnchor> anchors = getAnchors();
        int anchorsSize = anchors.size();
        for (int i = 0; i < anchorsSize; i++) {
            ConstraintAnchor anchor = (ConstraintAnchor) anchors.get(i);
            if (anchor.isConnected() && anchor.getTarget().getOwner() == widget && anchor.getConnectionCreator() == 2) {
                anchor.reset();
            }
        }
    }

    public ConstraintAnchor getAnchor(Type anchorType) {
        switch (anchorType) {
            case LEFT:
                return this.mLeft;
            case TOP:
                return this.mTop;
            case RIGHT:
                return this.mRight;
            case BOTTOM:
                return this.mBottom;
            case BASELINE:
                return this.mBaseline;
            case CENTER:
                return this.mCenter;
            case CENTER_X:
                return this.mCenterX;
            case CENTER_Y:
                return this.mCenterY;
            case NONE:
                return null;
            default:
                throw new AssertionError(anchorType.name());
        }
    }

    public DimensionBehaviour getHorizontalDimensionBehaviour() {
        return this.mListDimensionBehaviors[0];
    }

    public DimensionBehaviour getVerticalDimensionBehaviour() {
        return this.mListDimensionBehaviors[1];
    }

    public void setHorizontalDimensionBehaviour(DimensionBehaviour behaviour) {
        this.mListDimensionBehaviors[0] = behaviour;
        if (behaviour == DimensionBehaviour.WRAP_CONTENT) {
            setWidth(this.mWrapWidth);
        }
    }

    public void setVerticalDimensionBehaviour(DimensionBehaviour behaviour) {
        this.mListDimensionBehaviors[1] = behaviour;
        if (behaviour == DimensionBehaviour.WRAP_CONTENT) {
            setHeight(this.mWrapHeight);
        }
    }

    public boolean isInHorizontalChain() {
        if ((this.mLeft.mTarget == null || this.mLeft.mTarget.mTarget != this.mLeft) && (this.mRight.mTarget == null || this.mRight.mTarget.mTarget != this.mRight)) {
            return false;
        }
        return true;
    }

    public ConstraintWidget getHorizontalChainControlWidget() {
        if (!isInHorizontalChain()) {
            return null;
        }
        ConstraintWidget found = null;
        ConstraintWidget found2 = this;
        while (found == null && found2 != null) {
            ConstraintAnchor anchor = found2.getAnchor(Type.LEFT);
            ConstraintAnchor targetAnchor = null;
            ConstraintAnchor targetOwner = anchor == null ? null : anchor.getTarget();
            ConstraintWidget target = targetOwner == null ? null : targetOwner.getOwner();
            if (target == getParent()) {
                return found2;
            }
            if (target != null) {
                targetAnchor = target.getAnchor(Type.RIGHT).getTarget();
            }
            if (targetAnchor == null || targetAnchor.getOwner() == found2) {
                found2 = target;
            } else {
                found = found2;
            }
        }
        return found;
    }

    public boolean isInVerticalChain() {
        if ((this.mTop.mTarget == null || this.mTop.mTarget.mTarget != this.mTop) && (this.mBottom.mTarget == null || this.mBottom.mTarget.mTarget != this.mBottom)) {
            return false;
        }
        return true;
    }

    public ConstraintWidget getVerticalChainControlWidget() {
        if (!isInVerticalChain()) {
            return null;
        }
        ConstraintWidget found = null;
        ConstraintWidget found2 = this;
        while (found == null && found2 != null) {
            ConstraintAnchor anchor = found2.getAnchor(Type.TOP);
            ConstraintAnchor targetAnchor = null;
            ConstraintAnchor targetOwner = anchor == null ? null : anchor.getTarget();
            ConstraintWidget target = targetOwner == null ? null : targetOwner.getOwner();
            if (target == getParent()) {
                return found2;
            }
            if (target != null) {
                targetAnchor = target.getAnchor(Type.BOTTOM).getTarget();
            }
            if (targetAnchor == null || targetAnchor.getOwner() == found2) {
                found2 = target;
            } else {
                found = found2;
            }
        }
        return found;
    }

    public void addToSolver(LinearSystem system) {
        boolean z;
        int width;
        int height;
        boolean useRatio;
        boolean z2;
        boolean applyPosition;
        boolean verticalParentWrapContent;
        boolean z3;
        SolverVariable baseline;
        SolverVariable bottom;
        SolverVariable top;
        SolverVariable right;
        boolean z4;
        boolean z5;
        boolean wrapContent;
        boolean useVerticalRatio;
        LinearSystem linearSystem;
        boolean applyPosition2;
        LinearSystem linearSystem2 = system;
        SolverVariable left = linearSystem2.createObjectVariable(this.mLeft);
        SolverVariable right2 = linearSystem2.createObjectVariable(this.mRight);
        SolverVariable top2 = linearSystem2.createObjectVariable(this.mTop);
        SolverVariable bottom2 = linearSystem2.createObjectVariable(this.mBottom);
        SolverVariable baseline2 = linearSystem2.createObjectVariable(this.mBaseline);
        boolean inHorizontalChain = false;
        boolean inVerticalChain = false;
        boolean horizontalParentWrapContent = false;
        boolean verticalParentWrapContent2 = false;
        if (this.mParent != null) {
            z = r15.mParent != null && r15.mParent.mListDimensionBehaviors[0] == DimensionBehaviour.WRAP_CONTENT;
            horizontalParentWrapContent = z;
            z = r15.mParent != null && r15.mParent.mListDimensionBehaviors[1] == DimensionBehaviour.WRAP_CONTENT;
            verticalParentWrapContent2 = z;
            if ((r15.mLeft.mTarget != null && r15.mLeft.mTarget.mTarget == r15.mLeft) || (r15.mRight.mTarget != null && r15.mRight.mTarget.mTarget == r15.mRight)) {
                ((ConstraintWidgetContainer) r15.mParent).addChain(r15, 0);
                inHorizontalChain = true;
            }
            if ((r15.mTop.mTarget != null && r15.mTop.mTarget.mTarget == r15.mTop) || (r15.mBottom.mTarget != null && r15.mBottom.mTarget.mTarget == r15.mBottom)) {
                ((ConstraintWidgetContainer) r15.mParent).addChain(r15, 1);
                inVerticalChain = true;
            }
            if (horizontalParentWrapContent && r15.mVisibility != 8 && r15.mLeft.mTarget == null && r15.mRight.mTarget == null) {
                linearSystem2.addGreaterThan(linearSystem2.createObjectVariable(r15.mParent.mRight), right2, 0, 1);
            }
            if (verticalParentWrapContent2 && r15.mVisibility != 8 && r15.mTop.mTarget == null && r15.mBottom.mTarget == null && r15.mBaseline == null) {
                linearSystem2.addGreaterThan(linearSystem2.createObjectVariable(r15.mParent.mBottom), bottom2, 0, 1);
            }
        }
        boolean inHorizontalChain2 = inHorizontalChain;
        boolean inVerticalChain2 = inVerticalChain;
        inVerticalChain = verticalParentWrapContent2;
        int width2 = r15.mWidth;
        if (width2 < r15.mMinWidth) {
            width2 = r15.mMinWidth;
        }
        int height2 = r15.mHeight;
        if (height2 < r15.mMinHeight) {
            height2 = r15.mMinHeight;
        }
        z = r15.mListDimensionBehaviors[0] != DimensionBehaviour.MATCH_CONSTRAINT;
        boolean verticalDimensionFixed = r15.mListDimensionBehaviors[1] != DimensionBehaviour.MATCH_CONSTRAINT;
        boolean useRatio2 = false;
        r15.mResolvedDimensionRatioSide = r15.mDimensionRatioSide;
        r15.mResolvedDimensionRatio = r15.mDimensionRatio;
        if (r15.mDimensionRatio > 0.0f && r15.mVisibility != 8) {
            useRatio2 = true;
            if (r15.mListDimensionBehaviors[0] == DimensionBehaviour.MATCH_CONSTRAINT && r15.mListDimensionBehaviors[1] == DimensionBehaviour.MATCH_CONSTRAINT) {
                setupDimensionRatio(horizontalParentWrapContent, inVerticalChain, z, verticalDimensionFixed);
            } else if (r15.mListDimensionBehaviors[0] == DimensionBehaviour.MATCH_CONSTRAINT) {
                r15.mResolvedDimensionRatioSide = 0;
                width = (int) (r15.mResolvedDimensionRatio * ((float) r15.mHeight));
                height = height2;
                useRatio = true;
                z2 = true;
                if (!useRatio) {
                    if (r15.mResolvedDimensionRatioSide == 0) {
                        if (r15.mResolvedDimensionRatioSide == -1) {
                        }
                    }
                    useRatio2 = z2;
                    inHorizontalChain = (r15.mListDimensionBehaviors[0] == DimensionBehaviour.WRAP_CONTENT || !(r15 instanceof ConstraintWidgetContainer)) ? false : z2;
                    verticalDimensionFixed = inHorizontalChain;
                    inHorizontalChain = true;
                    if (r15.mCenter.isConnected()) {
                        inHorizontalChain = false;
                    }
                    applyPosition = inHorizontalChain;
                    if (r15.mHorizontalResolution == 2) {
                        verticalParentWrapContent = inVerticalChain;
                        z3 = false;
                        baseline = baseline2;
                        bottom = bottom2;
                        top = top2;
                        right = right2;
                        applyConstraints(linearSystem2, horizontalParentWrapContent, r15.mParent == null ? linearSystem2.createObjectVariable(r15.mParent.mLeft) : null, r15.mParent == null ? linearSystem2.createObjectVariable(r15.mParent.mRight) : null, r15.mListDimensionBehaviors[0], verticalDimensionFixed, r15.mLeft, r15.mRight, r15.mX, width, r15.mMinWidth, r15.mMaxDimension[0], r15.mHorizontalBiasPercent, useRatio2, inHorizontalChain2, r15.mMatchConstraintDefaultWidth, r15.mMatchConstraintMinWidth, r15.mMatchConstraintMaxWidth, r15.mMatchConstraintPercentWidth, applyPosition);
                    } else {
                        verticalParentWrapContent = inVerticalChain;
                        z4 = horizontalParentWrapContent;
                        z5 = z;
                        baseline = baseline2;
                        bottom = bottom2;
                        top = top2;
                        right = right2;
                        z3 = false;
                    }
                    if (this.mVerticalResolution == 2) {
                        wrapContent = (r7.mListDimensionBehaviors[1] == DimensionBehaviour.WRAP_CONTENT || !(r7 instanceof ConstraintWidgetContainer)) ? z3 : true;
                        useVerticalRatio = (useRatio || !(r7.mResolvedDimensionRatioSide == 1 || r7.mResolvedDimensionRatioSide == -1)) ? z3 : true;
                        if (r7.mBaselineDistance > 0) {
                            baseline2 = top;
                            linearSystem = system;
                        } else if (r7.mBaseline.getResolutionNode().state != 1) {
                            linearSystem = system;
                            r7.mBaseline.getResolutionNode().addResolvedValue(linearSystem);
                            bottom2 = baseline;
                            baseline2 = top;
                        } else {
                            linearSystem = system;
                            bottom2 = baseline;
                            baseline2 = top;
                            linearSystem.addEquality(bottom2, baseline2, getBaselineDistance(), 6);
                            if (r7.mBaseline.mTarget != null) {
                                linearSystem.addEquality(bottom2, linearSystem.createObjectVariable(r7.mBaseline.mTarget), 0, 6);
                                applyPosition2 = false;
                                applyConstraints(linearSystem, verticalParentWrapContent, r7.mParent == null ? linearSystem.createObjectVariable(r7.mParent.mTop) : null, r7.mParent == null ? linearSystem.createObjectVariable(r7.mParent.mBottom) : null, r7.mListDimensionBehaviors[1], wrapContent, r7.mTop, r7.mBottom, r7.mY, height, r7.mMinHeight, r7.mMaxDimension[1], r7.mVerticalBiasPercent, useVerticalRatio, inVerticalChain2, r7.mMatchConstraintDefaultHeight, r7.mMatchConstraintMinHeight, r7.mMatchConstraintMaxHeight, r7.mMatchConstraintPercentHeight, applyPosition2);
                                if (useRatio) {
                                    if (r7.mResolvedDimensionRatioSide != 1) {
                                        linearSystem.addRatio(bottom, baseline2, right, left, r7.mResolvedDimensionRatio, 6);
                                    } else {
                                        linearSystem.addRatio(right, left, bottom, baseline2, r7.mResolvedDimensionRatio, 6);
                                    }
                                }
                                if (r7.mCenter.isConnected()) {
                                    linearSystem.addCenterPoint(r7, r7.mCenter.getTarget().getOwner(), (float) Math.toRadians((double) (r7.mCircleConstraintAngle + 90.0f)), r7.mCenter.getMargin());
                                }
                            }
                        }
                        applyPosition2 = applyPosition;
                        if (r7.mParent == null) {
                        }
                        if (r7.mParent == null) {
                        }
                        applyConstraints(linearSystem, verticalParentWrapContent, r7.mParent == null ? linearSystem.createObjectVariable(r7.mParent.mTop) : null, r7.mParent == null ? linearSystem.createObjectVariable(r7.mParent.mBottom) : null, r7.mListDimensionBehaviors[1], wrapContent, r7.mTop, r7.mBottom, r7.mY, height, r7.mMinHeight, r7.mMaxDimension[1], r7.mVerticalBiasPercent, useVerticalRatio, inVerticalChain2, r7.mMatchConstraintDefaultHeight, r7.mMatchConstraintMinHeight, r7.mMatchConstraintMaxHeight, r7.mMatchConstraintPercentHeight, applyPosition2);
                        if (useRatio) {
                            if (r7.mResolvedDimensionRatioSide != 1) {
                                linearSystem.addRatio(right, left, bottom, baseline2, r7.mResolvedDimensionRatio, 6);
                            } else {
                                linearSystem.addRatio(bottom, baseline2, right, left, r7.mResolvedDimensionRatio, 6);
                            }
                        }
                        if (r7.mCenter.isConnected()) {
                            linearSystem.addCenterPoint(r7, r7.mCenter.getTarget().getOwner(), (float) Math.toRadians((double) (r7.mCircleConstraintAngle + 90.0f)), r7.mCenter.getMargin());
                        }
                    }
                }
                useRatio2 = false;
                if (r15.mListDimensionBehaviors[0] == DimensionBehaviour.WRAP_CONTENT) {
                }
                verticalDimensionFixed = inHorizontalChain;
                inHorizontalChain = true;
                if (r15.mCenter.isConnected()) {
                    inHorizontalChain = false;
                }
                applyPosition = inHorizontalChain;
                if (r15.mHorizontalResolution == 2) {
                    verticalParentWrapContent = inVerticalChain;
                    z4 = horizontalParentWrapContent;
                    z5 = z;
                    baseline = baseline2;
                    bottom = bottom2;
                    top = top2;
                    right = right2;
                    z3 = false;
                } else {
                    if (r15.mParent == null) {
                    }
                    if (r15.mParent == null) {
                    }
                    verticalParentWrapContent = inVerticalChain;
                    z3 = false;
                    baseline = baseline2;
                    bottom = bottom2;
                    top = top2;
                    right = right2;
                    applyConstraints(linearSystem2, horizontalParentWrapContent, r15.mParent == null ? linearSystem2.createObjectVariable(r15.mParent.mLeft) : null, r15.mParent == null ? linearSystem2.createObjectVariable(r15.mParent.mRight) : null, r15.mListDimensionBehaviors[0], verticalDimensionFixed, r15.mLeft, r15.mRight, r15.mX, width, r15.mMinWidth, r15.mMaxDimension[0], r15.mHorizontalBiasPercent, useRatio2, inHorizontalChain2, r15.mMatchConstraintDefaultWidth, r15.mMatchConstraintMinWidth, r15.mMatchConstraintMaxWidth, r15.mMatchConstraintPercentWidth, applyPosition);
                }
                if (this.mVerticalResolution == 2) {
                    if (r7.mListDimensionBehaviors[1] == DimensionBehaviour.WRAP_CONTENT) {
                    }
                    if (useRatio) {
                    }
                    if (r7.mBaselineDistance > 0) {
                        baseline2 = top;
                        linearSystem = system;
                    } else if (r7.mBaseline.getResolutionNode().state != 1) {
                        linearSystem = system;
                        bottom2 = baseline;
                        baseline2 = top;
                        linearSystem.addEquality(bottom2, baseline2, getBaselineDistance(), 6);
                        if (r7.mBaseline.mTarget != null) {
                            linearSystem.addEquality(bottom2, linearSystem.createObjectVariable(r7.mBaseline.mTarget), 0, 6);
                            applyPosition2 = false;
                            if (r7.mParent == null) {
                            }
                            if (r7.mParent == null) {
                            }
                            applyConstraints(linearSystem, verticalParentWrapContent, r7.mParent == null ? linearSystem.createObjectVariable(r7.mParent.mTop) : null, r7.mParent == null ? linearSystem.createObjectVariable(r7.mParent.mBottom) : null, r7.mListDimensionBehaviors[1], wrapContent, r7.mTop, r7.mBottom, r7.mY, height, r7.mMinHeight, r7.mMaxDimension[1], r7.mVerticalBiasPercent, useVerticalRatio, inVerticalChain2, r7.mMatchConstraintDefaultHeight, r7.mMatchConstraintMinHeight, r7.mMatchConstraintMaxHeight, r7.mMatchConstraintPercentHeight, applyPosition2);
                            if (useRatio) {
                                if (r7.mResolvedDimensionRatioSide != 1) {
                                    linearSystem.addRatio(bottom, baseline2, right, left, r7.mResolvedDimensionRatio, 6);
                                } else {
                                    linearSystem.addRatio(right, left, bottom, baseline2, r7.mResolvedDimensionRatio, 6);
                                }
                            }
                            if (r7.mCenter.isConnected()) {
                                linearSystem.addCenterPoint(r7, r7.mCenter.getTarget().getOwner(), (float) Math.toRadians((double) (r7.mCircleConstraintAngle + 90.0f)), r7.mCenter.getMargin());
                            }
                        }
                    } else {
                        linearSystem = system;
                        r7.mBaseline.getResolutionNode().addResolvedValue(linearSystem);
                        bottom2 = baseline;
                        baseline2 = top;
                    }
                    applyPosition2 = applyPosition;
                    if (r7.mParent == null) {
                    }
                    if (r7.mParent == null) {
                    }
                    applyConstraints(linearSystem, verticalParentWrapContent, r7.mParent == null ? linearSystem.createObjectVariable(r7.mParent.mTop) : null, r7.mParent == null ? linearSystem.createObjectVariable(r7.mParent.mBottom) : null, r7.mListDimensionBehaviors[1], wrapContent, r7.mTop, r7.mBottom, r7.mY, height, r7.mMinHeight, r7.mMaxDimension[1], r7.mVerticalBiasPercent, useVerticalRatio, inVerticalChain2, r7.mMatchConstraintDefaultHeight, r7.mMatchConstraintMinHeight, r7.mMatchConstraintMaxHeight, r7.mMatchConstraintPercentHeight, applyPosition2);
                    if (useRatio) {
                        if (r7.mResolvedDimensionRatioSide != 1) {
                            linearSystem.addRatio(right, left, bottom, baseline2, r7.mResolvedDimensionRatio, 6);
                        } else {
                            linearSystem.addRatio(bottom, baseline2, right, left, r7.mResolvedDimensionRatio, 6);
                        }
                    }
                    if (r7.mCenter.isConnected()) {
                        linearSystem.addCenterPoint(r7, r7.mCenter.getTarget().getOwner(), (float) Math.toRadians((double) (r7.mCircleConstraintAngle + 90.0f)), r7.mCenter.getMargin());
                    }
                }
            } else {
                z2 = true;
                if (r15.mListDimensionBehaviors[1] == DimensionBehaviour.MATCH_CONSTRAINT) {
                    r15.mResolvedDimensionRatioSide = 1;
                    if (r15.mDimensionRatioSide == -1) {
                        r15.mResolvedDimensionRatio = 1.0f / r15.mResolvedDimensionRatio;
                    }
                    height2 = (int) (r15.mResolvedDimensionRatio * ((float) r15.mWidth));
                }
                width = width2;
                height = height2;
                useRatio = useRatio2;
                if (!useRatio) {
                    if (r15.mResolvedDimensionRatioSide == 0) {
                        if (r15.mResolvedDimensionRatioSide == -1) {
                        }
                    }
                    useRatio2 = z2;
                    if (r15.mListDimensionBehaviors[0] == DimensionBehaviour.WRAP_CONTENT) {
                    }
                    verticalDimensionFixed = inHorizontalChain;
                    inHorizontalChain = true;
                    if (r15.mCenter.isConnected()) {
                        inHorizontalChain = false;
                    }
                    applyPosition = inHorizontalChain;
                    if (r15.mHorizontalResolution == 2) {
                        if (r15.mParent == null) {
                        }
                        if (r15.mParent == null) {
                        }
                        verticalParentWrapContent = inVerticalChain;
                        z3 = false;
                        baseline = baseline2;
                        bottom = bottom2;
                        top = top2;
                        right = right2;
                        applyConstraints(linearSystem2, horizontalParentWrapContent, r15.mParent == null ? linearSystem2.createObjectVariable(r15.mParent.mLeft) : null, r15.mParent == null ? linearSystem2.createObjectVariable(r15.mParent.mRight) : null, r15.mListDimensionBehaviors[0], verticalDimensionFixed, r15.mLeft, r15.mRight, r15.mX, width, r15.mMinWidth, r15.mMaxDimension[0], r15.mHorizontalBiasPercent, useRatio2, inHorizontalChain2, r15.mMatchConstraintDefaultWidth, r15.mMatchConstraintMinWidth, r15.mMatchConstraintMaxWidth, r15.mMatchConstraintPercentWidth, applyPosition);
                    } else {
                        verticalParentWrapContent = inVerticalChain;
                        z4 = horizontalParentWrapContent;
                        z5 = z;
                        baseline = baseline2;
                        bottom = bottom2;
                        top = top2;
                        right = right2;
                        z3 = false;
                    }
                    if (this.mVerticalResolution == 2) {
                        if (r7.mListDimensionBehaviors[1] == DimensionBehaviour.WRAP_CONTENT) {
                        }
                        if (useRatio) {
                        }
                        if (r7.mBaselineDistance > 0) {
                            baseline2 = top;
                            linearSystem = system;
                        } else if (r7.mBaseline.getResolutionNode().state != 1) {
                            linearSystem = system;
                            r7.mBaseline.getResolutionNode().addResolvedValue(linearSystem);
                            bottom2 = baseline;
                            baseline2 = top;
                        } else {
                            linearSystem = system;
                            bottom2 = baseline;
                            baseline2 = top;
                            linearSystem.addEquality(bottom2, baseline2, getBaselineDistance(), 6);
                            if (r7.mBaseline.mTarget != null) {
                                linearSystem.addEquality(bottom2, linearSystem.createObjectVariable(r7.mBaseline.mTarget), 0, 6);
                                applyPosition2 = false;
                                if (r7.mParent == null) {
                                }
                                if (r7.mParent == null) {
                                }
                                applyConstraints(linearSystem, verticalParentWrapContent, r7.mParent == null ? linearSystem.createObjectVariable(r7.mParent.mTop) : null, r7.mParent == null ? linearSystem.createObjectVariable(r7.mParent.mBottom) : null, r7.mListDimensionBehaviors[1], wrapContent, r7.mTop, r7.mBottom, r7.mY, height, r7.mMinHeight, r7.mMaxDimension[1], r7.mVerticalBiasPercent, useVerticalRatio, inVerticalChain2, r7.mMatchConstraintDefaultHeight, r7.mMatchConstraintMinHeight, r7.mMatchConstraintMaxHeight, r7.mMatchConstraintPercentHeight, applyPosition2);
                                if (useRatio) {
                                    if (r7.mResolvedDimensionRatioSide != 1) {
                                        linearSystem.addRatio(bottom, baseline2, right, left, r7.mResolvedDimensionRatio, 6);
                                    } else {
                                        linearSystem.addRatio(right, left, bottom, baseline2, r7.mResolvedDimensionRatio, 6);
                                    }
                                }
                                if (r7.mCenter.isConnected()) {
                                    linearSystem.addCenterPoint(r7, r7.mCenter.getTarget().getOwner(), (float) Math.toRadians((double) (r7.mCircleConstraintAngle + 90.0f)), r7.mCenter.getMargin());
                                }
                            }
                        }
                        applyPosition2 = applyPosition;
                        if (r7.mParent == null) {
                        }
                        if (r7.mParent == null) {
                        }
                        applyConstraints(linearSystem, verticalParentWrapContent, r7.mParent == null ? linearSystem.createObjectVariable(r7.mParent.mTop) : null, r7.mParent == null ? linearSystem.createObjectVariable(r7.mParent.mBottom) : null, r7.mListDimensionBehaviors[1], wrapContent, r7.mTop, r7.mBottom, r7.mY, height, r7.mMinHeight, r7.mMaxDimension[1], r7.mVerticalBiasPercent, useVerticalRatio, inVerticalChain2, r7.mMatchConstraintDefaultHeight, r7.mMatchConstraintMinHeight, r7.mMatchConstraintMaxHeight, r7.mMatchConstraintPercentHeight, applyPosition2);
                        if (useRatio) {
                            if (r7.mResolvedDimensionRatioSide != 1) {
                                linearSystem.addRatio(right, left, bottom, baseline2, r7.mResolvedDimensionRatio, 6);
                            } else {
                                linearSystem.addRatio(bottom, baseline2, right, left, r7.mResolvedDimensionRatio, 6);
                            }
                        }
                        if (r7.mCenter.isConnected()) {
                            linearSystem.addCenterPoint(r7, r7.mCenter.getTarget().getOwner(), (float) Math.toRadians((double) (r7.mCircleConstraintAngle + 90.0f)), r7.mCenter.getMargin());
                        }
                    }
                }
                useRatio2 = false;
                if (r15.mListDimensionBehaviors[0] == DimensionBehaviour.WRAP_CONTENT) {
                }
                verticalDimensionFixed = inHorizontalChain;
                inHorizontalChain = true;
                if (r15.mCenter.isConnected()) {
                    inHorizontalChain = false;
                }
                applyPosition = inHorizontalChain;
                if (r15.mHorizontalResolution == 2) {
                    verticalParentWrapContent = inVerticalChain;
                    z4 = horizontalParentWrapContent;
                    z5 = z;
                    baseline = baseline2;
                    bottom = bottom2;
                    top = top2;
                    right = right2;
                    z3 = false;
                } else {
                    if (r15.mParent == null) {
                    }
                    if (r15.mParent == null) {
                    }
                    verticalParentWrapContent = inVerticalChain;
                    z3 = false;
                    baseline = baseline2;
                    bottom = bottom2;
                    top = top2;
                    right = right2;
                    applyConstraints(linearSystem2, horizontalParentWrapContent, r15.mParent == null ? linearSystem2.createObjectVariable(r15.mParent.mLeft) : null, r15.mParent == null ? linearSystem2.createObjectVariable(r15.mParent.mRight) : null, r15.mListDimensionBehaviors[0], verticalDimensionFixed, r15.mLeft, r15.mRight, r15.mX, width, r15.mMinWidth, r15.mMaxDimension[0], r15.mHorizontalBiasPercent, useRatio2, inHorizontalChain2, r15.mMatchConstraintDefaultWidth, r15.mMatchConstraintMinWidth, r15.mMatchConstraintMaxWidth, r15.mMatchConstraintPercentWidth, applyPosition);
                }
                if (this.mVerticalResolution == 2) {
                    if (r7.mListDimensionBehaviors[1] == DimensionBehaviour.WRAP_CONTENT) {
                    }
                    if (useRatio) {
                    }
                    if (r7.mBaselineDistance > 0) {
                        baseline2 = top;
                        linearSystem = system;
                    } else if (r7.mBaseline.getResolutionNode().state != 1) {
                        linearSystem = system;
                        bottom2 = baseline;
                        baseline2 = top;
                        linearSystem.addEquality(bottom2, baseline2, getBaselineDistance(), 6);
                        if (r7.mBaseline.mTarget != null) {
                            linearSystem.addEquality(bottom2, linearSystem.createObjectVariable(r7.mBaseline.mTarget), 0, 6);
                            applyPosition2 = false;
                            if (r7.mParent == null) {
                            }
                            if (r7.mParent == null) {
                            }
                            applyConstraints(linearSystem, verticalParentWrapContent, r7.mParent == null ? linearSystem.createObjectVariable(r7.mParent.mTop) : null, r7.mParent == null ? linearSystem.createObjectVariable(r7.mParent.mBottom) : null, r7.mListDimensionBehaviors[1], wrapContent, r7.mTop, r7.mBottom, r7.mY, height, r7.mMinHeight, r7.mMaxDimension[1], r7.mVerticalBiasPercent, useVerticalRatio, inVerticalChain2, r7.mMatchConstraintDefaultHeight, r7.mMatchConstraintMinHeight, r7.mMatchConstraintMaxHeight, r7.mMatchConstraintPercentHeight, applyPosition2);
                            if (useRatio) {
                                if (r7.mResolvedDimensionRatioSide != 1) {
                                    linearSystem.addRatio(bottom, baseline2, right, left, r7.mResolvedDimensionRatio, 6);
                                } else {
                                    linearSystem.addRatio(right, left, bottom, baseline2, r7.mResolvedDimensionRatio, 6);
                                }
                            }
                            if (r7.mCenter.isConnected()) {
                                linearSystem.addCenterPoint(r7, r7.mCenter.getTarget().getOwner(), (float) Math.toRadians((double) (r7.mCircleConstraintAngle + 90.0f)), r7.mCenter.getMargin());
                            }
                        }
                    } else {
                        linearSystem = system;
                        r7.mBaseline.getResolutionNode().addResolvedValue(linearSystem);
                        bottom2 = baseline;
                        baseline2 = top;
                    }
                    applyPosition2 = applyPosition;
                    if (r7.mParent == null) {
                    }
                    if (r7.mParent == null) {
                    }
                    applyConstraints(linearSystem, verticalParentWrapContent, r7.mParent == null ? linearSystem.createObjectVariable(r7.mParent.mTop) : null, r7.mParent == null ? linearSystem.createObjectVariable(r7.mParent.mBottom) : null, r7.mListDimensionBehaviors[1], wrapContent, r7.mTop, r7.mBottom, r7.mY, height, r7.mMinHeight, r7.mMaxDimension[1], r7.mVerticalBiasPercent, useVerticalRatio, inVerticalChain2, r7.mMatchConstraintDefaultHeight, r7.mMatchConstraintMinHeight, r7.mMatchConstraintMaxHeight, r7.mMatchConstraintPercentHeight, applyPosition2);
                    if (useRatio) {
                        if (r7.mResolvedDimensionRatioSide != 1) {
                            linearSystem.addRatio(right, left, bottom, baseline2, r7.mResolvedDimensionRatio, 6);
                        } else {
                            linearSystem.addRatio(bottom, baseline2, right, left, r7.mResolvedDimensionRatio, 6);
                        }
                    }
                    if (r7.mCenter.isConnected()) {
                        linearSystem.addCenterPoint(r7, r7.mCenter.getTarget().getOwner(), (float) Math.toRadians((double) (r7.mCircleConstraintAngle + 90.0f)), r7.mCenter.getMargin());
                    }
                }
            }
        }
        z2 = true;
        width = width2;
        height = height2;
        useRatio = useRatio2;
        if (!useRatio) {
            if (r15.mResolvedDimensionRatioSide == 0) {
                if (r15.mResolvedDimensionRatioSide == -1) {
                }
            }
            useRatio2 = z2;
            if (r15.mListDimensionBehaviors[0] == DimensionBehaviour.WRAP_CONTENT) {
            }
            verticalDimensionFixed = inHorizontalChain;
            inHorizontalChain = true;
            if (r15.mCenter.isConnected()) {
                inHorizontalChain = false;
            }
            applyPosition = inHorizontalChain;
            if (r15.mHorizontalResolution == 2) {
                if (r15.mParent == null) {
                }
                if (r15.mParent == null) {
                }
                verticalParentWrapContent = inVerticalChain;
                z3 = false;
                baseline = baseline2;
                bottom = bottom2;
                top = top2;
                right = right2;
                applyConstraints(linearSystem2, horizontalParentWrapContent, r15.mParent == null ? linearSystem2.createObjectVariable(r15.mParent.mLeft) : null, r15.mParent == null ? linearSystem2.createObjectVariable(r15.mParent.mRight) : null, r15.mListDimensionBehaviors[0], verticalDimensionFixed, r15.mLeft, r15.mRight, r15.mX, width, r15.mMinWidth, r15.mMaxDimension[0], r15.mHorizontalBiasPercent, useRatio2, inHorizontalChain2, r15.mMatchConstraintDefaultWidth, r15.mMatchConstraintMinWidth, r15.mMatchConstraintMaxWidth, r15.mMatchConstraintPercentWidth, applyPosition);
            } else {
                verticalParentWrapContent = inVerticalChain;
                z4 = horizontalParentWrapContent;
                z5 = z;
                baseline = baseline2;
                bottom = bottom2;
                top = top2;
                right = right2;
                z3 = false;
            }
            if (this.mVerticalResolution == 2) {
                if (r7.mListDimensionBehaviors[1] == DimensionBehaviour.WRAP_CONTENT) {
                }
                if (useRatio) {
                }
                if (r7.mBaselineDistance > 0) {
                    baseline2 = top;
                    linearSystem = system;
                } else if (r7.mBaseline.getResolutionNode().state != 1) {
                    linearSystem = system;
                    r7.mBaseline.getResolutionNode().addResolvedValue(linearSystem);
                    bottom2 = baseline;
                    baseline2 = top;
                } else {
                    linearSystem = system;
                    bottom2 = baseline;
                    baseline2 = top;
                    linearSystem.addEquality(bottom2, baseline2, getBaselineDistance(), 6);
                    if (r7.mBaseline.mTarget != null) {
                        linearSystem.addEquality(bottom2, linearSystem.createObjectVariable(r7.mBaseline.mTarget), 0, 6);
                        applyPosition2 = false;
                        if (r7.mParent == null) {
                        }
                        if (r7.mParent == null) {
                        }
                        applyConstraints(linearSystem, verticalParentWrapContent, r7.mParent == null ? linearSystem.createObjectVariable(r7.mParent.mTop) : null, r7.mParent == null ? linearSystem.createObjectVariable(r7.mParent.mBottom) : null, r7.mListDimensionBehaviors[1], wrapContent, r7.mTop, r7.mBottom, r7.mY, height, r7.mMinHeight, r7.mMaxDimension[1], r7.mVerticalBiasPercent, useVerticalRatio, inVerticalChain2, r7.mMatchConstraintDefaultHeight, r7.mMatchConstraintMinHeight, r7.mMatchConstraintMaxHeight, r7.mMatchConstraintPercentHeight, applyPosition2);
                        if (useRatio) {
                            if (r7.mResolvedDimensionRatioSide != 1) {
                                linearSystem.addRatio(bottom, baseline2, right, left, r7.mResolvedDimensionRatio, 6);
                            } else {
                                linearSystem.addRatio(right, left, bottom, baseline2, r7.mResolvedDimensionRatio, 6);
                            }
                        }
                        if (r7.mCenter.isConnected()) {
                            linearSystem.addCenterPoint(r7, r7.mCenter.getTarget().getOwner(), (float) Math.toRadians((double) (r7.mCircleConstraintAngle + 90.0f)), r7.mCenter.getMargin());
                        }
                    }
                }
                applyPosition2 = applyPosition;
                if (r7.mParent == null) {
                }
                if (r7.mParent == null) {
                }
                applyConstraints(linearSystem, verticalParentWrapContent, r7.mParent == null ? linearSystem.createObjectVariable(r7.mParent.mTop) : null, r7.mParent == null ? linearSystem.createObjectVariable(r7.mParent.mBottom) : null, r7.mListDimensionBehaviors[1], wrapContent, r7.mTop, r7.mBottom, r7.mY, height, r7.mMinHeight, r7.mMaxDimension[1], r7.mVerticalBiasPercent, useVerticalRatio, inVerticalChain2, r7.mMatchConstraintDefaultHeight, r7.mMatchConstraintMinHeight, r7.mMatchConstraintMaxHeight, r7.mMatchConstraintPercentHeight, applyPosition2);
                if (useRatio) {
                    if (r7.mResolvedDimensionRatioSide != 1) {
                        linearSystem.addRatio(right, left, bottom, baseline2, r7.mResolvedDimensionRatio, 6);
                    } else {
                        linearSystem.addRatio(bottom, baseline2, right, left, r7.mResolvedDimensionRatio, 6);
                    }
                }
                if (r7.mCenter.isConnected()) {
                    linearSystem.addCenterPoint(r7, r7.mCenter.getTarget().getOwner(), (float) Math.toRadians((double) (r7.mCircleConstraintAngle + 90.0f)), r7.mCenter.getMargin());
                }
            }
        }
        useRatio2 = false;
        if (r15.mListDimensionBehaviors[0] == DimensionBehaviour.WRAP_CONTENT) {
        }
        verticalDimensionFixed = inHorizontalChain;
        inHorizontalChain = true;
        if (r15.mCenter.isConnected()) {
            inHorizontalChain = false;
        }
        applyPosition = inHorizontalChain;
        if (r15.mHorizontalResolution == 2) {
            verticalParentWrapContent = inVerticalChain;
            z4 = horizontalParentWrapContent;
            z5 = z;
            baseline = baseline2;
            bottom = bottom2;
            top = top2;
            right = right2;
            z3 = false;
        } else {
            if (r15.mParent == null) {
            }
            if (r15.mParent == null) {
            }
            verticalParentWrapContent = inVerticalChain;
            z3 = false;
            baseline = baseline2;
            bottom = bottom2;
            top = top2;
            right = right2;
            applyConstraints(linearSystem2, horizontalParentWrapContent, r15.mParent == null ? linearSystem2.createObjectVariable(r15.mParent.mLeft) : null, r15.mParent == null ? linearSystem2.createObjectVariable(r15.mParent.mRight) : null, r15.mListDimensionBehaviors[0], verticalDimensionFixed, r15.mLeft, r15.mRight, r15.mX, width, r15.mMinWidth, r15.mMaxDimension[0], r15.mHorizontalBiasPercent, useRatio2, inHorizontalChain2, r15.mMatchConstraintDefaultWidth, r15.mMatchConstraintMinWidth, r15.mMatchConstraintMaxWidth, r15.mMatchConstraintPercentWidth, applyPosition);
        }
        if (this.mVerticalResolution == 2) {
            if (r7.mListDimensionBehaviors[1] == DimensionBehaviour.WRAP_CONTENT) {
            }
            if (useRatio) {
            }
            if (r7.mBaselineDistance > 0) {
                baseline2 = top;
                linearSystem = system;
            } else if (r7.mBaseline.getResolutionNode().state != 1) {
                linearSystem = system;
                bottom2 = baseline;
                baseline2 = top;
                linearSystem.addEquality(bottom2, baseline2, getBaselineDistance(), 6);
                if (r7.mBaseline.mTarget != null) {
                    linearSystem.addEquality(bottom2, linearSystem.createObjectVariable(r7.mBaseline.mTarget), 0, 6);
                    applyPosition2 = false;
                    if (r7.mParent == null) {
                    }
                    if (r7.mParent == null) {
                    }
                    applyConstraints(linearSystem, verticalParentWrapContent, r7.mParent == null ? linearSystem.createObjectVariable(r7.mParent.mTop) : null, r7.mParent == null ? linearSystem.createObjectVariable(r7.mParent.mBottom) : null, r7.mListDimensionBehaviors[1], wrapContent, r7.mTop, r7.mBottom, r7.mY, height, r7.mMinHeight, r7.mMaxDimension[1], r7.mVerticalBiasPercent, useVerticalRatio, inVerticalChain2, r7.mMatchConstraintDefaultHeight, r7.mMatchConstraintMinHeight, r7.mMatchConstraintMaxHeight, r7.mMatchConstraintPercentHeight, applyPosition2);
                    if (useRatio) {
                        if (r7.mResolvedDimensionRatioSide != 1) {
                            linearSystem.addRatio(bottom, baseline2, right, left, r7.mResolvedDimensionRatio, 6);
                        } else {
                            linearSystem.addRatio(right, left, bottom, baseline2, r7.mResolvedDimensionRatio, 6);
                        }
                    }
                    if (r7.mCenter.isConnected()) {
                        linearSystem.addCenterPoint(r7, r7.mCenter.getTarget().getOwner(), (float) Math.toRadians((double) (r7.mCircleConstraintAngle + 90.0f)), r7.mCenter.getMargin());
                    }
                }
            } else {
                linearSystem = system;
                r7.mBaseline.getResolutionNode().addResolvedValue(linearSystem);
                bottom2 = baseline;
                baseline2 = top;
            }
            applyPosition2 = applyPosition;
            if (r7.mParent == null) {
            }
            if (r7.mParent == null) {
            }
            applyConstraints(linearSystem, verticalParentWrapContent, r7.mParent == null ? linearSystem.createObjectVariable(r7.mParent.mTop) : null, r7.mParent == null ? linearSystem.createObjectVariable(r7.mParent.mBottom) : null, r7.mListDimensionBehaviors[1], wrapContent, r7.mTop, r7.mBottom, r7.mY, height, r7.mMinHeight, r7.mMaxDimension[1], r7.mVerticalBiasPercent, useVerticalRatio, inVerticalChain2, r7.mMatchConstraintDefaultHeight, r7.mMatchConstraintMinHeight, r7.mMatchConstraintMaxHeight, r7.mMatchConstraintPercentHeight, applyPosition2);
            if (useRatio) {
                if (r7.mResolvedDimensionRatioSide != 1) {
                    linearSystem.addRatio(right, left, bottom, baseline2, r7.mResolvedDimensionRatio, 6);
                } else {
                    linearSystem.addRatio(bottom, baseline2, right, left, r7.mResolvedDimensionRatio, 6);
                }
            }
            if (r7.mCenter.isConnected()) {
                linearSystem.addCenterPoint(r7, r7.mCenter.getTarget().getOwner(), (float) Math.toRadians((double) (r7.mCircleConstraintAngle + 90.0f)), r7.mCenter.getMargin());
            }
        }
    }

    public void setupDimensionRatio(boolean hparentWrapContent, boolean vparentWrapContent, boolean horizontalDimensionFixed, boolean verticalDimensionFixed) {
        if (this.mMatchConstraintDefaultWidth == 0) {
            this.mMatchConstraintDefaultWidth = 3;
        }
        if (this.mMatchConstraintDefaultHeight == 0) {
            this.mMatchConstraintDefaultHeight = 3;
        }
        if (this.mResolvedDimensionRatioSide == -1) {
            if (horizontalDimensionFixed && !verticalDimensionFixed) {
                this.mResolvedDimensionRatioSide = 0;
            } else if (!horizontalDimensionFixed && verticalDimensionFixed) {
                this.mResolvedDimensionRatioSide = 1;
                if (this.mDimensionRatioSide == -1) {
                    this.mResolvedDimensionRatio = 1.0f / this.mResolvedDimensionRatio;
                }
            }
        }
        if (this.mResolvedDimensionRatioSide == 0 && (!this.mTop.isConnected() || !this.mBottom.isConnected())) {
            this.mResolvedDimensionRatioSide = 1;
        } else if (this.mResolvedDimensionRatioSide == 1 && !(this.mLeft.isConnected() && this.mRight.isConnected())) {
            this.mResolvedDimensionRatioSide = 0;
        }
        if (this.mResolvedDimensionRatioSide == -1 && !(this.mTop.isConnected() && this.mBottom.isConnected() && this.mLeft.isConnected() && this.mRight.isConnected())) {
            if (this.mTop.isConnected() && this.mBottom.isConnected()) {
                this.mResolvedDimensionRatioSide = 0;
            } else if (this.mLeft.isConnected() && this.mRight.isConnected()) {
                this.mResolvedDimensionRatio = 1.0f / this.mResolvedDimensionRatio;
                this.mResolvedDimensionRatioSide = 1;
            }
        }
        if (this.mResolvedDimensionRatioSide == -1) {
            if (hparentWrapContent && !vparentWrapContent) {
                this.mResolvedDimensionRatioSide = 0;
            } else if (!hparentWrapContent && vparentWrapContent) {
                this.mResolvedDimensionRatio = 1.0f / this.mResolvedDimensionRatio;
                this.mResolvedDimensionRatioSide = 1;
            }
        }
        if (this.mResolvedDimensionRatioSide != -1) {
            return;
        }
        if (this.mMatchConstraintMinWidth > 0 && this.mMatchConstraintMinHeight == 0) {
            this.mResolvedDimensionRatioSide = 0;
        } else if (this.mMatchConstraintMinWidth != 0 || this.mMatchConstraintMinHeight <= 0) {
            this.mResolvedDimensionRatio = 1.0f / this.mResolvedDimensionRatio;
            this.mResolvedDimensionRatioSide = 1;
        } else {
            this.mResolvedDimensionRatio = 1.0f / this.mResolvedDimensionRatio;
            this.mResolvedDimensionRatioSide = 1;
        }
    }

    private void applyConstraints(LinearSystem system, boolean parentWrapContent, SolverVariable parentMin, SolverVariable parentMax, DimensionBehaviour dimensionBehaviour, boolean wrapContent, ConstraintAnchor beginAnchor, ConstraintAnchor endAnchor, int beginPosition, int dimension, int minDimension, int maxDimension, float bias, boolean useRatio, boolean inChain, int matchConstraintDefault, int matchMinDimension, int matchMaxDimension, float matchPercentDimension, boolean applyPosition) {
        ConstraintWidget constraintWidget = this;
        LinearSystem linearSystem = system;
        SolverVariable solverVariable = parentMin;
        SolverVariable solverVariable2 = parentMax;
        int i = minDimension;
        int i2 = maxDimension;
        SolverVariable begin = linearSystem.createObjectVariable(beginAnchor);
        SolverVariable end = linearSystem.createObjectVariable(endAnchor);
        SolverVariable beginTarget = linearSystem.createObjectVariable(beginAnchor.getTarget());
        SolverVariable endTarget = linearSystem.createObjectVariable(endAnchor.getTarget());
        if (linearSystem.graphOptimizer && beginAnchor.getResolutionNode().state == 1 && endAnchor.getResolutionNode().state == 1) {
            if (LinearSystem.getMetrics() != null) {
                Metrics metrics = LinearSystem.getMetrics();
                metrics.resolvedWidgets++;
            }
            beginAnchor.getResolutionNode().addResolvedValue(linearSystem);
            endAnchor.getResolutionNode().addResolvedValue(linearSystem);
            if (!inChain && parentWrapContent) {
                linearSystem.addGreaterThan(solverVariable2, end, 0, 6);
            }
            return;
        }
        int dimension2;
        int i3;
        int i4;
        int matchMaxDimension2;
        int matchConstraintDefault2;
        SolverVariable endTarget2;
        SolverVariable beginTarget2;
        int d;
        int matchMaxDimension3;
        int matchMinDimension2;
        SolverVariable end2;
        boolean variableSize = endTarget;
        if (LinearSystem.getMetrics() != null) {
            metrics = LinearSystem.getMetrics();
            metrics.nonresolvedWidgets++;
        }
        boolean isBeginConnected = beginAnchor.isConnected();
        boolean isEndConnected = endAnchor.isConnected();
        boolean isCenterConnected = constraintWidget.mCenter.isConnected();
        boolean variableSize2 = false;
        int numConnections = 0;
        if (isBeginConnected) {
            numConnections = 0 + 1;
        }
        if (isEndConnected) {
            numConnections++;
        }
        if (isCenterConnected) {
            numConnections++;
        }
        i2 = numConnections;
        int matchConstraintDefault3 = useRatio ? 3 : matchConstraintDefault;
        switch (dimensionBehaviour) {
            case FIXED:
                variableSize2 = false;
                break;
            case WRAP_CONTENT:
                variableSize2 = false;
                break;
            case MATCH_PARENT:
                variableSize2 = false;
                break;
            case MATCH_CONSTRAINT:
                variableSize2 = true;
                break;
            default:
                break;
        }
        if (constraintWidget.mVisibility == 8) {
            dimension2 = 0;
            variableSize2 = false;
        } else {
            dimension2 = dimension;
        }
        boolean variableSize3 = variableSize2;
        if (applyPosition) {
            if (isBeginConnected || isEndConnected || isCenterConnected) {
                numConnections = beginPosition;
                if (isBeginConnected && !isEndConnected) {
                    linearSystem.addEquality(begin, beginTarget, beginAnchor.getMargin(), 6);
                }
            } else {
                linearSystem.addEquality(begin, beginPosition);
            }
        }
        if (variableSize3) {
            int matchMinDimension3;
            int dimension3;
            int matchMaxDimension4;
            SolverVariable end3;
            i3 = maxDimension;
            i4 = minDimension;
            i3 = matchMinDimension;
            if (i3 == -2) {
                i3 = dimension2;
            }
            SolverVariable beginTarget3 = beginTarget;
            matchMaxDimension2 = matchMaxDimension;
            if (matchMaxDimension2 == -2) {
                matchMaxDimension2 = dimension2;
            }
            if (i3 > 0) {
                if (parentWrapContent) {
                    linearSystem.addGreaterThan(end, begin, i3, 6);
                } else {
                    linearSystem.addGreaterThan(end, begin, i3, 6);
                }
                dimension2 = Math.max(dimension2, i3);
            }
            if (matchMaxDimension2 > 0) {
                if (parentWrapContent) {
                    linearSystem.addLowerThan(end, begin, matchMaxDimension2, 1);
                } else {
                    linearSystem.addLowerThan(end, begin, matchMaxDimension2, 6);
                }
                dimension2 = Math.min(dimension2, matchMaxDimension2);
            }
            if (matchConstraintDefault3 == 1) {
                if (parentWrapContent) {
                    linearSystem.addEquality(end, begin, dimension2, 6);
                } else if (inChain) {
                    linearSystem.addEquality(end, begin, dimension2, 4);
                } else {
                    linearSystem.addEquality(end, begin, dimension2, 1);
                }
                matchMinDimension3 = i3;
                dimension3 = dimension2;
                matchConstraintDefault2 = matchConstraintDefault3;
                matchMaxDimension4 = matchMaxDimension2;
                end3 = end;
                endTarget2 = variableSize;
                beginTarget2 = beginTarget3;
                i4 = 2;
            } else if (matchConstraintDefault3 == 2) {
                SolverVariable percentBegin;
                SolverVariable percentEnd;
                int matchMinDimension4 = i3;
                if (beginAnchor.getType() != Type.TOP) {
                    if (beginAnchor.getType() != Type.BOTTOM) {
                        percentBegin = linearSystem.createObjectVariable(constraintWidget.mParent.getAnchor(Type.LEFT));
                        percentEnd = linearSystem.createObjectVariable(constraintWidget.mParent.getAnchor(Type.RIGHT));
                        i4 = 2;
                        dimension3 = dimension2;
                        matchMinDimension3 = matchMinDimension4;
                        matchConstraintDefault2 = matchConstraintDefault3;
                        endTarget2 = variableSize;
                        matchMaxDimension4 = matchMaxDimension2;
                        beginTarget2 = beginTarget3;
                        end3 = end;
                        linearSystem.addConstraint(system.createRow().createRowDimensionRatio(end, begin, percentEnd, percentBegin, matchPercentDimension));
                        variableSize3 = false;
                    }
                }
                i3 = linearSystem.createObjectVariable(constraintWidget.mParent.getAnchor(Type.TOP));
                percentEnd = linearSystem.createObjectVariable(constraintWidget.mParent.getAnchor(Type.BOTTOM));
                percentBegin = i3;
                i4 = 2;
                dimension3 = dimension2;
                matchMinDimension3 = matchMinDimension4;
                matchConstraintDefault2 = matchConstraintDefault3;
                endTarget2 = variableSize;
                matchMaxDimension4 = matchMaxDimension2;
                beginTarget2 = beginTarget3;
                end3 = end;
                linearSystem.addConstraint(system.createRow().createRowDimensionRatio(end, begin, percentEnd, percentBegin, matchPercentDimension));
                variableSize3 = false;
            } else {
                matchMinDimension3 = i3;
                dimension3 = dimension2;
                i4 = 2;
                matchConstraintDefault2 = matchConstraintDefault3;
                matchMaxDimension4 = matchMaxDimension2;
                end3 = end;
                endTarget2 = variableSize;
                beginTarget2 = beginTarget3;
            }
            if (!variableSize3 || i2 == r7 || useRatio) {
                dimension2 = dimension3;
                i3 = matchMinDimension3;
                numConnections = matchMaxDimension4;
                end = end3;
                matchMaxDimension2 = 6;
            } else {
                variableSize3 = false;
                dimension2 = dimension3;
                i3 = matchMinDimension3;
                d = Math.max(i3, dimension2);
                numConnections = matchMaxDimension4;
                if (numConnections > 0) {
                    d = Math.min(numConnections, d);
                }
                end = end3;
                matchMaxDimension2 = 6;
                linearSystem.addEquality(end, begin, d, 6);
            }
            d = dimension2;
            matchMaxDimension3 = numConnections;
            variableSize = variableSize3;
            matchMinDimension2 = i3;
        } else {
            if (wrapContent) {
                linearSystem.addEquality(end, begin, 0, 3);
                i4 = minDimension;
                if (i4 > 0) {
                    linearSystem.addGreaterThan(end, begin, i4, 6);
                }
                i3 = maxDimension;
                if (i3 < ActivityChooserViewAdapter.MAX_ACTIVITY_COUNT_UNLIMITED) {
                    linearSystem.addLowerThan(end, begin, i3, 6);
                }
            } else {
                i3 = maxDimension;
                i4 = minDimension;
                linearSystem.addEquality(end, begin, dimension2, 6);
            }
            matchMaxDimension3 = matchMaxDimension;
            d = dimension2;
            matchConstraintDefault2 = matchConstraintDefault3;
            beginTarget2 = beginTarget;
            endTarget2 = variableSize;
            matchMaxDimension2 = 6;
            i4 = 2;
            variableSize = variableSize3;
            matchMinDimension2 = matchMinDimension;
        }
        boolean z;
        boolean z2;
        int i5;
        int i6;
        if (!applyPosition) {
            dimension2 = matchMaxDimension2;
            end2 = end;
            z = isBeginConnected;
            z2 = isEndConnected;
            endTarget = endTarget2;
            i5 = matchConstraintDefault2;
            i3 = 0;
            beginTarget = parentMax;
            solverVariable = parentMin;
            i6 = minDimension;
            solverVariable2 = begin;
        } else if (inChain) {
            r35 = d;
            dimension2 = matchMaxDimension2;
            end2 = end;
            z = isBeginConnected;
            z2 = isEndConnected;
            endTarget = endTarget2;
            i5 = matchConstraintDefault2;
            d = beginTarget2;
            i3 = 0;
            beginTarget = parentMax;
            solverVariable = parentMin;
            i6 = minDimension;
            solverVariable2 = begin;
        } else {
            if (isBeginConnected || isEndConnected || isCenterConnected) {
                endTarget = null;
                SolverVariable solverVariable3 = parentMax;
                if (isBeginConnected && !isEndConnected) {
                    if (parentWrapContent) {
                        linearSystem.addGreaterThan(solverVariable3, end, 0, 5);
                    }
                    i3 = endTarget;
                    dimension2 = matchMaxDimension2;
                    end2 = end;
                    d = beginTarget2;
                } else if (isBeginConnected || !isEndConnected) {
                    end2 = endTarget2;
                    r2 = parentMin;
                    i6 = minDimension;
                    if (isBeginConnected && isEndConnected) {
                        SolverVariable endTarget3;
                        SolverVariable end4;
                        boolean applyBoundsCheck = false;
                        boolean applyCentering = false;
                        int centeringStrength = 5;
                        if (variableSize) {
                            if (parentWrapContent && i6 == 0) {
                                linearSystem.addGreaterThan(end, begin, 0, matchMaxDimension2);
                            }
                            i3 = matchConstraintDefault2;
                            if (i3 == 0) {
                                i5 = 6;
                                if (matchMaxDimension3 <= 0) {
                                    if (matchMinDimension2 > 0) {
                                    }
                                    matchConstraintDefault3 = i5;
                                    r35 = d;
                                    d = beginTarget2;
                                    linearSystem.addEquality(begin, d, beginAnchor.getMargin(), matchConstraintDefault3);
                                    linearSystem.addEquality(end, end2, -endAnchor.getMargin(), matchConstraintDefault3);
                                    if (matchMaxDimension3 > 0 || matchMinDimension2 > 0) {
                                        applyCentering = true;
                                    }
                                }
                                i5 = 4;
                                applyBoundsCheck = true;
                                matchConstraintDefault3 = i5;
                                r35 = d;
                                d = beginTarget2;
                                linearSystem.addEquality(begin, d, beginAnchor.getMargin(), matchConstraintDefault3);
                                linearSystem.addEquality(end, end2, -endAnchor.getMargin(), matchConstraintDefault3);
                                applyCentering = true;
                            } else {
                                d = beginTarget2;
                                if (i3 == 1) {
                                    applyCentering = true;
                                    applyBoundsCheck = true;
                                    i5 = i3;
                                    centeringStrength = 6;
                                } else if (i3 == 3) {
                                    applyCentering = true;
                                    applyBoundsCheck = true;
                                    matchConstraintDefault3 = 4;
                                    if (!useRatio) {
                                        matchConstraintDefault3 = 6;
                                    }
                                    linearSystem.addEquality(begin, d, beginAnchor.getMargin(), matchConstraintDefault3);
                                    linearSystem.addEquality(end, end2, -endAnchor.getMargin(), matchConstraintDefault3);
                                }
                            }
                        } else {
                            i5 = matchConstraintDefault2;
                            d = beginTarget2;
                            applyCentering = true;
                            if (parentWrapContent) {
                                linearSystem.addGreaterThan(begin, d, beginAnchor.getMargin(), 5);
                                linearSystem.addLowerThan(end, end2, -endAnchor.getMargin(), 5);
                            }
                        }
                        if (applyCentering) {
                            endTarget3 = end2;
                            z = isBeginConnected;
                            solverVariable = r2;
                            z2 = isEndConnected;
                            end4 = end;
                            isEndConnected = begin;
                            linearSystem.addCentering(begin, d, beginAnchor.getMargin(), bias, endTarget3, end4, endAnchor.getMargin(), centeringStrength);
                        } else {
                            endTarget3 = end2;
                            end4 = end;
                            z = isBeginConnected;
                            z2 = isEndConnected;
                            solverVariable = r2;
                            isEndConnected = begin;
                        }
                        if (applyBoundsCheck) {
                            dimension2 = 6;
                            linearSystem.addGreaterThan(isEndConnected, d, beginAnchor.getMargin(), 6);
                            end2 = end4;
                            linearSystem.addLowerThan(end2, endTarget3, -endAnchor.getMargin(), 6);
                        } else {
                            end2 = end4;
                            dimension2 = 6;
                        }
                        if (parentWrapContent) {
                            i3 = 0;
                            linearSystem.addGreaterThan(isEndConnected, solverVariable, 0, dimension2);
                        } else {
                            i3 = 0;
                        }
                        if (parentWrapContent) {
                            linearSystem.addGreaterThan(parentMax, end2, i3, dimension2);
                        } else {
                            beginTarget = parentMax;
                        }
                        return;
                    }
                    i3 = 0;
                    z = isBeginConnected;
                    z2 = isEndConnected;
                    i5 = matchConstraintDefault2;
                    d = beginTarget2;
                    isBeginConnected = r2;
                    endTarget = end2;
                    dimension2 = matchMaxDimension2;
                    end2 = end;
                    isEndConnected = begin;
                    if (parentWrapContent) {
                        linearSystem.addGreaterThan(parentMax, end2, i3, dimension2);
                    } else {
                        beginTarget = parentMax;
                    }
                    return;
                } else {
                    i6 = minDimension;
                    end2 = endTarget2;
                    linearSystem.addEquality(end, end2, -endAnchor.getMargin(), matchMaxDimension2);
                    if (parentWrapContent) {
                        r2 = parentMin;
                        linearSystem.addGreaterThan(begin, r2, 0, 5);
                        r35 = d;
                        i3 = null;
                        z = isBeginConnected;
                        z2 = isEndConnected;
                        i5 = matchConstraintDefault2;
                        d = beginTarget2;
                        isBeginConnected = r2;
                        endTarget = end2;
                        dimension2 = matchMaxDimension2;
                    } else {
                        r35 = d;
                        i3 = null;
                        dimension2 = matchMaxDimension2;
                        z = isBeginConnected;
                        z2 = isEndConnected;
                        i5 = matchConstraintDefault2;
                        d = beginTarget2;
                        isBeginConnected = parentMin;
                        endTarget = end2;
                    }
                    end2 = end;
                    isEndConnected = begin;
                    if (parentWrapContent) {
                        linearSystem.addGreaterThan(parentMax, end2, i3, dimension2);
                    } else {
                        beginTarget = parentMax;
                    }
                    return;
                }
            } else if (parentWrapContent) {
                endTarget = null;
                linearSystem.addGreaterThan(parentMax, end, 0, 5);
                i3 = endTarget;
                dimension2 = matchMaxDimension2;
                end2 = end;
                d = beginTarget2;
            } else {
                dimension2 = matchMaxDimension2;
                end2 = end;
                d = beginTarget2;
                i3 = 0;
            }
            isBeginConnected = parentMin;
            i6 = minDimension;
            isEndConnected = begin;
            if (parentWrapContent) {
                beginTarget = parentMax;
            } else {
                linearSystem.addGreaterThan(parentMax, end2, i3, dimension2);
            }
            return;
        }
        if (i2 < i4 && parentWrapContent) {
            linearSystem.addGreaterThan(solverVariable2, solverVariable, i3, dimension2);
            linearSystem.addGreaterThan(beginTarget, end2, i3, dimension2);
        }
    }

    public void updateFromSolver(LinearSystem system) {
        setFrame(system.getObjectVariableValue(this.mLeft), system.getObjectVariableValue(this.mTop), system.getObjectVariableValue(this.mRight), system.getObjectVariableValue(this.mBottom));
    }
}
