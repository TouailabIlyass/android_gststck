package android.support.constraint;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources.NotFoundException;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Build.VERSION;
import android.support.constraint.solver.Metrics;
import android.support.constraint.solver.widgets.ConstraintAnchor;
import android.support.constraint.solver.widgets.ConstraintAnchor.Strength;
import android.support.constraint.solver.widgets.ConstraintAnchor.Type;
import android.support.constraint.solver.widgets.ConstraintWidget;
import android.support.constraint.solver.widgets.ConstraintWidget.DimensionBehaviour;
import android.support.constraint.solver.widgets.ConstraintWidgetContainer;
import android.support.constraint.solver.widgets.Guideline;
import android.support.constraint.solver.widgets.ResolutionAnchor;
import android.support.v4.internal.view.SupportMenu;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import java.util.ArrayList;
import java.util.HashMap;

public class ConstraintLayout extends ViewGroup {
    static final boolean ALLOWS_EMBEDDED = false;
    private static final boolean DEBUG = false;
    public static final int DESIGN_INFO_ID = 0;
    private static final String TAG = "ConstraintLayout";
    private static final boolean USE_CONSTRAINTS_HELPER = true;
    public static final String VERSION = "ConstraintLayout-1.1.0";
    SparseArray<View> mChildrenByIds = new SparseArray();
    private ArrayList<ConstraintHelper> mConstraintHelpers = new ArrayList(4);
    private ConstraintSet mConstraintSet = null;
    private int mConstraintSetId = -1;
    private HashMap<String, Integer> mDesignIds = new HashMap();
    private boolean mDirtyHierarchy = USE_CONSTRAINTS_HELPER;
    private int mLastMeasureHeight = -1;
    int mLastMeasureHeightMode = 0;
    int mLastMeasureHeightSize = -1;
    private int mLastMeasureWidth = -1;
    int mLastMeasureWidthMode = 0;
    int mLastMeasureWidthSize = -1;
    ConstraintWidgetContainer mLayoutWidget = new ConstraintWidgetContainer();
    private int mMaxHeight = ActivityChooserViewAdapter.MAX_ACTIVITY_COUNT_UNLIMITED;
    private int mMaxWidth = ActivityChooserViewAdapter.MAX_ACTIVITY_COUNT_UNLIMITED;
    private Metrics mMetrics;
    private int mMinHeight = 0;
    private int mMinWidth = 0;
    private int mOptimizationLevel = 3;
    private final ArrayList<ConstraintWidget> mVariableDimensionsWidgets = new ArrayList(100);

    public static class LayoutParams extends MarginLayoutParams {
        public static final int BASELINE = 5;
        public static final int BOTTOM = 4;
        public static final int CHAIN_PACKED = 2;
        public static final int CHAIN_SPREAD = 0;
        public static final int CHAIN_SPREAD_INSIDE = 1;
        public static final int END = 7;
        public static final int HORIZONTAL = 0;
        public static final int LEFT = 1;
        public static final int MATCH_CONSTRAINT = 0;
        public static final int MATCH_CONSTRAINT_PERCENT = 2;
        public static final int MATCH_CONSTRAINT_SPREAD = 0;
        public static final int MATCH_CONSTRAINT_WRAP = 1;
        public static final int PARENT_ID = 0;
        public static final int RIGHT = 2;
        public static final int START = 6;
        public static final int TOP = 3;
        public static final int UNSET = -1;
        public static final int VERTICAL = 1;
        public int baselineToBaseline;
        public int bottomToBottom;
        public int bottomToTop;
        public float circleAngle;
        public int circleConstraint;
        public int circleRadius;
        public boolean constrainedHeight;
        public boolean constrainedWidth;
        public String dimensionRatio;
        int dimensionRatioSide;
        float dimensionRatioValue;
        public int editorAbsoluteX;
        public int editorAbsoluteY;
        public int endToEnd;
        public int endToStart;
        public int goneBottomMargin;
        public int goneEndMargin;
        public int goneLeftMargin;
        public int goneRightMargin;
        public int goneStartMargin;
        public int goneTopMargin;
        public int guideBegin;
        public int guideEnd;
        public float guidePercent;
        public boolean helped;
        public float horizontalBias;
        public int horizontalChainStyle;
        boolean horizontalDimensionFixed;
        public float horizontalWeight;
        boolean isGuideline;
        boolean isHelper;
        boolean isInPlaceholder;
        public int leftToLeft;
        public int leftToRight;
        public int matchConstraintDefaultHeight;
        public int matchConstraintDefaultWidth;
        public int matchConstraintMaxHeight;
        public int matchConstraintMaxWidth;
        public int matchConstraintMinHeight;
        public int matchConstraintMinWidth;
        public float matchConstraintPercentHeight;
        public float matchConstraintPercentWidth;
        boolean needsBaseline;
        public int orientation;
        int resolveGoneLeftMargin;
        int resolveGoneRightMargin;
        int resolvedGuideBegin;
        int resolvedGuideEnd;
        float resolvedGuidePercent;
        float resolvedHorizontalBias;
        int resolvedLeftToLeft;
        int resolvedLeftToRight;
        int resolvedRightToLeft;
        int resolvedRightToRight;
        public int rightToLeft;
        public int rightToRight;
        public int startToEnd;
        public int startToStart;
        public int topToBottom;
        public int topToTop;
        public float verticalBias;
        public int verticalChainStyle;
        boolean verticalDimensionFixed;
        public float verticalWeight;
        ConstraintWidget widget;

        private static class Table {
            public static final int ANDROID_ORIENTATION = 1;
            public static final int LAYOUT_CONSTRAINED_HEIGHT = 28;
            public static final int LAYOUT_CONSTRAINED_WIDTH = 27;
            public static final int LAYOUT_CONSTRAINT_BASELINE_CREATOR = 43;
            public static final int LAYOUT_CONSTRAINT_BASELINE_TO_BASELINE_OF = 16;
            public static final int LAYOUT_CONSTRAINT_BOTTOM_CREATOR = 42;
            public static final int LAYOUT_CONSTRAINT_BOTTOM_TO_BOTTOM_OF = 15;
            public static final int LAYOUT_CONSTRAINT_BOTTOM_TO_TOP_OF = 14;
            public static final int LAYOUT_CONSTRAINT_CIRCLE = 2;
            public static final int LAYOUT_CONSTRAINT_CIRCLE_ANGLE = 4;
            public static final int LAYOUT_CONSTRAINT_CIRCLE_RADIUS = 3;
            public static final int LAYOUT_CONSTRAINT_DIMENSION_RATIO = 44;
            public static final int LAYOUT_CONSTRAINT_END_TO_END_OF = 20;
            public static final int LAYOUT_CONSTRAINT_END_TO_START_OF = 19;
            public static final int LAYOUT_CONSTRAINT_GUIDE_BEGIN = 5;
            public static final int LAYOUT_CONSTRAINT_GUIDE_END = 6;
            public static final int LAYOUT_CONSTRAINT_GUIDE_PERCENT = 7;
            public static final int LAYOUT_CONSTRAINT_HEIGHT_DEFAULT = 32;
            public static final int LAYOUT_CONSTRAINT_HEIGHT_MAX = 37;
            public static final int LAYOUT_CONSTRAINT_HEIGHT_MIN = 36;
            public static final int LAYOUT_CONSTRAINT_HEIGHT_PERCENT = 38;
            public static final int LAYOUT_CONSTRAINT_HORIZONTAL_BIAS = 29;
            public static final int LAYOUT_CONSTRAINT_HORIZONTAL_CHAINSTYLE = 47;
            public static final int LAYOUT_CONSTRAINT_HORIZONTAL_WEIGHT = 45;
            public static final int LAYOUT_CONSTRAINT_LEFT_CREATOR = 39;
            public static final int LAYOUT_CONSTRAINT_LEFT_TO_LEFT_OF = 8;
            public static final int LAYOUT_CONSTRAINT_LEFT_TO_RIGHT_OF = 9;
            public static final int LAYOUT_CONSTRAINT_RIGHT_CREATOR = 41;
            public static final int LAYOUT_CONSTRAINT_RIGHT_TO_LEFT_OF = 10;
            public static final int LAYOUT_CONSTRAINT_RIGHT_TO_RIGHT_OF = 11;
            public static final int LAYOUT_CONSTRAINT_START_TO_END_OF = 17;
            public static final int LAYOUT_CONSTRAINT_START_TO_START_OF = 18;
            public static final int LAYOUT_CONSTRAINT_TOP_CREATOR = 40;
            public static final int LAYOUT_CONSTRAINT_TOP_TO_BOTTOM_OF = 13;
            public static final int LAYOUT_CONSTRAINT_TOP_TO_TOP_OF = 12;
            public static final int LAYOUT_CONSTRAINT_VERTICAL_BIAS = 30;
            public static final int LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE = 48;
            public static final int LAYOUT_CONSTRAINT_VERTICAL_WEIGHT = 46;
            public static final int LAYOUT_CONSTRAINT_WIDTH_DEFAULT = 31;
            public static final int LAYOUT_CONSTRAINT_WIDTH_MAX = 34;
            public static final int LAYOUT_CONSTRAINT_WIDTH_MIN = 33;
            public static final int LAYOUT_CONSTRAINT_WIDTH_PERCENT = 35;
            public static final int LAYOUT_EDITOR_ABSOLUTEX = 49;
            public static final int LAYOUT_EDITOR_ABSOLUTEY = 50;
            public static final int LAYOUT_GONE_MARGIN_BOTTOM = 24;
            public static final int LAYOUT_GONE_MARGIN_END = 26;
            public static final int LAYOUT_GONE_MARGIN_LEFT = 21;
            public static final int LAYOUT_GONE_MARGIN_RIGHT = 23;
            public static final int LAYOUT_GONE_MARGIN_START = 25;
            public static final int LAYOUT_GONE_MARGIN_TOP = 22;
            public static final int UNUSED = 0;
            public static final SparseIntArray map = new SparseIntArray();

            private Table() {
            }

            static {
                map.append(C0012R.styleable.ConstraintLayout_Layout_layout_constraintLeft_toLeftOf, 8);
                map.append(C0012R.styleable.ConstraintLayout_Layout_layout_constraintLeft_toRightOf, 9);
                map.append(C0012R.styleable.ConstraintLayout_Layout_layout_constraintRight_toLeftOf, 10);
                map.append(C0012R.styleable.ConstraintLayout_Layout_layout_constraintRight_toRightOf, 11);
                map.append(C0012R.styleable.ConstraintLayout_Layout_layout_constraintTop_toTopOf, 12);
                map.append(C0012R.styleable.ConstraintLayout_Layout_layout_constraintTop_toBottomOf, 13);
                map.append(C0012R.styleable.ConstraintLayout_Layout_layout_constraintBottom_toTopOf, 14);
                map.append(C0012R.styleable.ConstraintLayout_Layout_layout_constraintBottom_toBottomOf, 15);
                map.append(C0012R.styleable.ConstraintLayout_Layout_layout_constraintBaseline_toBaselineOf, 16);
                map.append(C0012R.styleable.ConstraintLayout_Layout_layout_constraintCircle, 2);
                map.append(C0012R.styleable.ConstraintLayout_Layout_layout_constraintCircleRadius, 3);
                map.append(C0012R.styleable.ConstraintLayout_Layout_layout_constraintCircleAngle, 4);
                map.append(C0012R.styleable.ConstraintLayout_Layout_layout_editor_absoluteX, 49);
                map.append(C0012R.styleable.ConstraintLayout_Layout_layout_editor_absoluteY, 50);
                map.append(C0012R.styleable.ConstraintLayout_Layout_layout_constraintGuide_begin, 5);
                map.append(C0012R.styleable.ConstraintLayout_Layout_layout_constraintGuide_end, 6);
                map.append(C0012R.styleable.ConstraintLayout_Layout_layout_constraintGuide_percent, 7);
                map.append(C0012R.styleable.ConstraintLayout_Layout_android_orientation, 1);
                map.append(C0012R.styleable.ConstraintLayout_Layout_layout_constraintStart_toEndOf, 17);
                map.append(C0012R.styleable.ConstraintLayout_Layout_layout_constraintStart_toStartOf, 18);
                map.append(C0012R.styleable.ConstraintLayout_Layout_layout_constraintEnd_toStartOf, 19);
                map.append(C0012R.styleable.ConstraintLayout_Layout_layout_constraintEnd_toEndOf, 20);
                map.append(C0012R.styleable.ConstraintLayout_Layout_layout_goneMarginLeft, 21);
                map.append(C0012R.styleable.ConstraintLayout_Layout_layout_goneMarginTop, 22);
                map.append(C0012R.styleable.ConstraintLayout_Layout_layout_goneMarginRight, 23);
                map.append(C0012R.styleable.ConstraintLayout_Layout_layout_goneMarginBottom, 24);
                map.append(C0012R.styleable.ConstraintLayout_Layout_layout_goneMarginStart, 25);
                map.append(C0012R.styleable.ConstraintLayout_Layout_layout_goneMarginEnd, 26);
                map.append(C0012R.styleable.ConstraintLayout_Layout_layout_constraintHorizontal_bias, 29);
                map.append(C0012R.styleable.ConstraintLayout_Layout_layout_constraintVertical_bias, 30);
                map.append(C0012R.styleable.ConstraintLayout_Layout_layout_constraintDimensionRatio, 44);
                map.append(C0012R.styleable.ConstraintLayout_Layout_layout_constraintHorizontal_weight, 45);
                map.append(C0012R.styleable.ConstraintLayout_Layout_layout_constraintVertical_weight, 46);
                map.append(C0012R.styleable.ConstraintLayout_Layout_layout_constraintHorizontal_chainStyle, 47);
                map.append(C0012R.styleable.ConstraintLayout_Layout_layout_constraintVertical_chainStyle, 48);
                map.append(C0012R.styleable.ConstraintLayout_Layout_layout_constrainedWidth, 27);
                map.append(C0012R.styleable.ConstraintLayout_Layout_layout_constrainedHeight, 28);
                map.append(C0012R.styleable.ConstraintLayout_Layout_layout_constraintWidth_default, 31);
                map.append(C0012R.styleable.ConstraintLayout_Layout_layout_constraintHeight_default, 32);
                map.append(C0012R.styleable.ConstraintLayout_Layout_layout_constraintWidth_min, 33);
                map.append(C0012R.styleable.ConstraintLayout_Layout_layout_constraintWidth_max, 34);
                map.append(C0012R.styleable.ConstraintLayout_Layout_layout_constraintWidth_percent, 35);
                map.append(C0012R.styleable.ConstraintLayout_Layout_layout_constraintHeight_min, 36);
                map.append(C0012R.styleable.ConstraintLayout_Layout_layout_constraintHeight_max, 37);
                map.append(C0012R.styleable.ConstraintLayout_Layout_layout_constraintHeight_percent, 38);
                map.append(C0012R.styleable.ConstraintLayout_Layout_layout_constraintLeft_creator, 39);
                map.append(C0012R.styleable.ConstraintLayout_Layout_layout_constraintTop_creator, 40);
                map.append(C0012R.styleable.ConstraintLayout_Layout_layout_constraintRight_creator, 41);
                map.append(C0012R.styleable.ConstraintLayout_Layout_layout_constraintBottom_creator, 42);
                map.append(C0012R.styleable.ConstraintLayout_Layout_layout_constraintBaseline_creator, 43);
            }
        }

        public void reset() {
            if (this.widget != null) {
                this.widget.reset();
            }
        }

        public LayoutParams(LayoutParams source) {
            super(source);
            this.guideBegin = -1;
            this.guideEnd = -1;
            this.guidePercent = -1.0f;
            this.leftToLeft = -1;
            this.leftToRight = -1;
            this.rightToLeft = -1;
            this.rightToRight = -1;
            this.topToTop = -1;
            this.topToBottom = -1;
            this.bottomToTop = -1;
            this.bottomToBottom = -1;
            this.baselineToBaseline = -1;
            this.circleConstraint = -1;
            this.circleRadius = 0;
            this.circleAngle = 0.0f;
            this.startToEnd = -1;
            this.startToStart = -1;
            this.endToStart = -1;
            this.endToEnd = -1;
            this.goneLeftMargin = -1;
            this.goneTopMargin = -1;
            this.goneRightMargin = -1;
            this.goneBottomMargin = -1;
            this.goneStartMargin = -1;
            this.goneEndMargin = -1;
            this.horizontalBias = 0.5f;
            this.verticalBias = 0.5f;
            this.dimensionRatio = null;
            this.dimensionRatioValue = 0.0f;
            this.dimensionRatioSide = 1;
            this.horizontalWeight = 0.0f;
            this.verticalWeight = 0.0f;
            this.horizontalChainStyle = 0;
            this.verticalChainStyle = 0;
            this.matchConstraintDefaultWidth = 0;
            this.matchConstraintDefaultHeight = 0;
            this.matchConstraintMinWidth = 0;
            this.matchConstraintMinHeight = 0;
            this.matchConstraintMaxWidth = 0;
            this.matchConstraintMaxHeight = 0;
            this.matchConstraintPercentWidth = 1.0f;
            this.matchConstraintPercentHeight = 1.0f;
            this.editorAbsoluteX = -1;
            this.editorAbsoluteY = -1;
            this.orientation = -1;
            this.constrainedWidth = false;
            this.constrainedHeight = false;
            this.horizontalDimensionFixed = ConstraintLayout.USE_CONSTRAINTS_HELPER;
            this.verticalDimensionFixed = ConstraintLayout.USE_CONSTRAINTS_HELPER;
            this.needsBaseline = false;
            this.isGuideline = false;
            this.isHelper = false;
            this.isInPlaceholder = false;
            this.resolvedLeftToLeft = -1;
            this.resolvedLeftToRight = -1;
            this.resolvedRightToLeft = -1;
            this.resolvedRightToRight = -1;
            this.resolveGoneLeftMargin = -1;
            this.resolveGoneRightMargin = -1;
            this.resolvedHorizontalBias = 0.5f;
            this.widget = new ConstraintWidget();
            this.helped = false;
            this.guideBegin = source.guideBegin;
            this.guideEnd = source.guideEnd;
            this.guidePercent = source.guidePercent;
            this.leftToLeft = source.leftToLeft;
            this.leftToRight = source.leftToRight;
            this.rightToLeft = source.rightToLeft;
            this.rightToRight = source.rightToRight;
            this.topToTop = source.topToTop;
            this.topToBottom = source.topToBottom;
            this.bottomToTop = source.bottomToTop;
            this.bottomToBottom = source.bottomToBottom;
            this.baselineToBaseline = source.baselineToBaseline;
            this.circleConstraint = source.circleConstraint;
            this.circleRadius = source.circleRadius;
            this.circleAngle = source.circleAngle;
            this.startToEnd = source.startToEnd;
            this.startToStart = source.startToStart;
            this.endToStart = source.endToStart;
            this.endToEnd = source.endToEnd;
            this.goneLeftMargin = source.goneLeftMargin;
            this.goneTopMargin = source.goneTopMargin;
            this.goneRightMargin = source.goneRightMargin;
            this.goneBottomMargin = source.goneBottomMargin;
            this.goneStartMargin = source.goneStartMargin;
            this.goneEndMargin = source.goneEndMargin;
            this.horizontalBias = source.horizontalBias;
            this.verticalBias = source.verticalBias;
            this.dimensionRatio = source.dimensionRatio;
            this.dimensionRatioValue = source.dimensionRatioValue;
            this.dimensionRatioSide = source.dimensionRatioSide;
            this.horizontalWeight = source.horizontalWeight;
            this.verticalWeight = source.verticalWeight;
            this.horizontalChainStyle = source.horizontalChainStyle;
            this.verticalChainStyle = source.verticalChainStyle;
            this.constrainedWidth = source.constrainedWidth;
            this.constrainedHeight = source.constrainedHeight;
            this.matchConstraintDefaultWidth = source.matchConstraintDefaultWidth;
            this.matchConstraintDefaultHeight = source.matchConstraintDefaultHeight;
            this.matchConstraintMinWidth = source.matchConstraintMinWidth;
            this.matchConstraintMaxWidth = source.matchConstraintMaxWidth;
            this.matchConstraintMinHeight = source.matchConstraintMinHeight;
            this.matchConstraintMaxHeight = source.matchConstraintMaxHeight;
            this.matchConstraintPercentWidth = source.matchConstraintPercentWidth;
            this.matchConstraintPercentHeight = source.matchConstraintPercentHeight;
            this.editorAbsoluteX = source.editorAbsoluteX;
            this.editorAbsoluteY = source.editorAbsoluteY;
            this.orientation = source.orientation;
            this.horizontalDimensionFixed = source.horizontalDimensionFixed;
            this.verticalDimensionFixed = source.verticalDimensionFixed;
            this.needsBaseline = source.needsBaseline;
            this.isGuideline = source.isGuideline;
            this.resolvedLeftToLeft = source.resolvedLeftToLeft;
            this.resolvedLeftToRight = source.resolvedLeftToRight;
            this.resolvedRightToLeft = source.resolvedRightToLeft;
            this.resolvedRightToRight = source.resolvedRightToRight;
            this.resolveGoneLeftMargin = source.resolveGoneLeftMargin;
            this.resolveGoneRightMargin = source.resolveGoneRightMargin;
            this.resolvedHorizontalBias = source.resolvedHorizontalBias;
            this.widget = source.widget;
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public LayoutParams(android.content.Context r20, android.util.AttributeSet r21) {
            /*
            r19 = this;
            r1 = r19;
            r19.<init>(r20, r21);
            r2 = -1;
            r1.guideBegin = r2;
            r1.guideEnd = r2;
            r3 = -1082130432; // 0xffffffffbf800000 float:-1.0 double:NaN;
            r1.guidePercent = r3;
            r1.leftToLeft = r2;
            r1.leftToRight = r2;
            r1.rightToLeft = r2;
            r1.rightToRight = r2;
            r1.topToTop = r2;
            r1.topToBottom = r2;
            r1.bottomToTop = r2;
            r1.bottomToBottom = r2;
            r1.baselineToBaseline = r2;
            r1.circleConstraint = r2;
            r3 = 0;
            r1.circleRadius = r3;
            r4 = 0;
            r1.circleAngle = r4;
            r1.startToEnd = r2;
            r1.startToStart = r2;
            r1.endToStart = r2;
            r1.endToEnd = r2;
            r1.goneLeftMargin = r2;
            r1.goneTopMargin = r2;
            r1.goneRightMargin = r2;
            r1.goneBottomMargin = r2;
            r1.goneStartMargin = r2;
            r1.goneEndMargin = r2;
            r5 = 1056964608; // 0x3f000000 float:0.5 double:5.222099017E-315;
            r1.horizontalBias = r5;
            r1.verticalBias = r5;
            r6 = 0;
            r1.dimensionRatio = r6;
            r1.dimensionRatioValue = r4;
            r6 = 1;
            r1.dimensionRatioSide = r6;
            r1.horizontalWeight = r4;
            r1.verticalWeight = r4;
            r1.horizontalChainStyle = r3;
            r1.verticalChainStyle = r3;
            r1.matchConstraintDefaultWidth = r3;
            r1.matchConstraintDefaultHeight = r3;
            r1.matchConstraintMinWidth = r3;
            r1.matchConstraintMinHeight = r3;
            r1.matchConstraintMaxWidth = r3;
            r1.matchConstraintMaxHeight = r3;
            r7 = 1065353216; // 0x3f800000 float:1.0 double:5.263544247E-315;
            r1.matchConstraintPercentWidth = r7;
            r1.matchConstraintPercentHeight = r7;
            r1.editorAbsoluteX = r2;
            r1.editorAbsoluteY = r2;
            r1.orientation = r2;
            r1.constrainedWidth = r3;
            r1.constrainedHeight = r3;
            r1.horizontalDimensionFixed = r6;
            r1.verticalDimensionFixed = r6;
            r1.needsBaseline = r3;
            r1.isGuideline = r3;
            r1.isHelper = r3;
            r1.isInPlaceholder = r3;
            r1.resolvedLeftToLeft = r2;
            r1.resolvedLeftToRight = r2;
            r1.resolvedRightToLeft = r2;
            r1.resolvedRightToRight = r2;
            r1.resolveGoneLeftMargin = r2;
            r1.resolveGoneRightMargin = r2;
            r1.resolvedHorizontalBias = r5;
            r5 = new android.support.constraint.solver.widgets.ConstraintWidget;
            r5.<init>();
            r1.widget = r5;
            r1.helped = r3;
            r5 = android.support.constraint.C0012R.styleable.ConstraintLayout_Layout;
            r7 = r20;
            r8 = r21;
            r5 = r7.obtainStyledAttributes(r8, r5);
            r9 = r5.getIndexCount();
            r10 = r3;
        L_0x00a0:
            if (r10 >= r9) goto L_0x0462;
        L_0x00a2:
            r11 = r5.getIndex(r10);
            r12 = android.support.constraint.ConstraintLayout.LayoutParams.Table.map;
            r12 = r12.get(r11);
            r13 = -2;
            switch(r12) {
                case 0: goto L_0x0456;
                case 1: goto L_0x0449;
                case 2: goto L_0x0432;
                case 3: goto L_0x0424;
                case 4: goto L_0x0408;
                case 5: goto L_0x03fa;
                case 6: goto L_0x03ef;
                case 7: goto L_0x03e4;
                case 8: goto L_0x03ce;
                case 9: goto L_0x03b8;
                case 10: goto L_0x03a2;
                case 11: goto L_0x038b;
                case 12: goto L_0x0374;
                case 13: goto L_0x035d;
                case 14: goto L_0x0346;
                case 15: goto L_0x032f;
                case 16: goto L_0x0318;
                case 17: goto L_0x0301;
                case 18: goto L_0x02ea;
                case 19: goto L_0x02d3;
                case 20: goto L_0x02bc;
                case 21: goto L_0x02b0;
                case 22: goto L_0x02a4;
                case 23: goto L_0x0298;
                case 24: goto L_0x028c;
                case 25: goto L_0x0280;
                case 26: goto L_0x0274;
                case 27: goto L_0x0268;
                case 28: goto L_0x025c;
                case 29: goto L_0x0250;
                case 30: goto L_0x0244;
                case 31: goto L_0x022f;
                case 32: goto L_0x0217;
                case 33: goto L_0x0201;
                case 34: goto L_0x01eb;
                case 35: goto L_0x01d9;
                case 36: goto L_0x01c3;
                case 37: goto L_0x01ad;
                case 38: goto L_0x019f;
                case 39: goto L_0x019e;
                case 40: goto L_0x019d;
                case 41: goto L_0x019c;
                case 42: goto L_0x019b;
                case 43: goto L_0x00b0;
                case 44: goto L_0x00e4;
                case 45: goto L_0x00dd;
                case 46: goto L_0x00d6;
                case 47: goto L_0x00cf;
                case 48: goto L_0x00c8;
                case 49: goto L_0x00bf;
                case 50: goto L_0x00b6;
                default: goto L_0x00b0;
            };
        L_0x00b0:
            r13 = r4;
            r4 = r6;
            r6 = r2;
            r2 = r3;
            goto L_0x045a;
        L_0x00b6:
            r13 = r1.editorAbsoluteY;
            r13 = r5.getDimensionPixelOffset(r11, r13);
            r1.editorAbsoluteY = r13;
            goto L_0x00b0;
        L_0x00bf:
            r13 = r1.editorAbsoluteX;
            r13 = r5.getDimensionPixelOffset(r11, r13);
            r1.editorAbsoluteX = r13;
            goto L_0x00b0;
        L_0x00c8:
            r13 = r5.getInt(r11, r3);
            r1.verticalChainStyle = r13;
            goto L_0x00b0;
        L_0x00cf:
            r13 = r5.getInt(r11, r3);
            r1.horizontalChainStyle = r13;
            goto L_0x00b0;
        L_0x00d6:
            r13 = r5.getFloat(r11, r4);
            r1.verticalWeight = r13;
            goto L_0x00b0;
        L_0x00dd:
            r13 = r5.getFloat(r11, r4);
            r1.horizontalWeight = r13;
            goto L_0x00b0;
        L_0x00e4:
            r13 = r5.getString(r11);
            r1.dimensionRatio = r13;
            r13 = 2143289344; // 0x7fc00000 float:NaN double:1.058925634E-314;
            r1.dimensionRatioValue = r13;
            r1.dimensionRatioSide = r2;
            r13 = r1.dimensionRatio;
            if (r13 == 0) goto L_0x00b0;
        L_0x00f4:
            r13 = r1.dimensionRatio;
            r13 = r13.length();
            r14 = r1.dimensionRatio;
            r15 = 44;
            r14 = r14.indexOf(r15);
            if (r14 <= 0) goto L_0x0126;
        L_0x0104:
            r15 = r13 + -1;
            if (r14 >= r15) goto L_0x0126;
        L_0x0108:
            r15 = r1.dimensionRatio;
            r15 = r15.substring(r3, r14);
            r2 = "W";
            r2 = r15.equalsIgnoreCase(r2);
            if (r2 == 0) goto L_0x0119;
        L_0x0116:
            r1.dimensionRatioSide = r3;
            goto L_0x0123;
        L_0x0119:
            r2 = "H";
            r2 = r15.equalsIgnoreCase(r2);
            if (r2 == 0) goto L_0x0123;
        L_0x0121:
            r1.dimensionRatioSide = r6;
        L_0x0123:
            r14 = r14 + 1;
            goto L_0x0127;
        L_0x0126:
            r14 = 0;
        L_0x0127:
            r2 = r1.dimensionRatio;
            r15 = 58;
            r2 = r2.indexOf(r15);
            if (r2 < 0) goto L_0x0184;
        L_0x0131:
            r15 = r13 + -1;
            if (r2 >= r15) goto L_0x0184;
        L_0x0135:
            r15 = r1.dimensionRatio;
            r15 = r15.substring(r14, r2);
            r3 = r1.dimensionRatio;
            r6 = r2 + 1;
            r3 = r3.substring(r6);
            r6 = r15.length();
            if (r6 <= 0) goto L_0x0181;
        L_0x0149:
            r6 = r3.length();
            if (r6 <= 0) goto L_0x0181;
        L_0x014f:
            r6 = java.lang.Float.parseFloat(r15);	 Catch:{ NumberFormatException -> 0x017d }
            r16 = java.lang.Float.parseFloat(r3);	 Catch:{ NumberFormatException -> 0x017d }
            r17 = (r6 > r4 ? 1 : (r6 == r4 ? 0 : -1));
            if (r17 <= 0) goto L_0x017a;
        L_0x015b:
            r17 = (r16 > r4 ? 1 : (r16 == r4 ? 0 : -1));
            if (r17 <= 0) goto L_0x017a;
        L_0x015f:
            r4 = r1.dimensionRatioSide;	 Catch:{ NumberFormatException -> 0x017d }
            r18 = r2;
            r2 = 1;
            if (r4 != r2) goto L_0x0171;
        L_0x0166:
            r2 = r16 / r6;
            r2 = java.lang.Math.abs(r2);	 Catch:{ NumberFormatException -> 0x016f }
            r1.dimensionRatioValue = r2;	 Catch:{ NumberFormatException -> 0x016f }
            goto L_0x017c;
        L_0x016f:
            r0 = move-exception;
            goto L_0x0180;
        L_0x0171:
            r2 = r6 / r16;
            r2 = java.lang.Math.abs(r2);	 Catch:{ NumberFormatException -> 0x016f }
            r1.dimensionRatioValue = r2;	 Catch:{ NumberFormatException -> 0x016f }
            goto L_0x017c;
        L_0x017a:
            r18 = r2;
        L_0x017c:
            goto L_0x0183;
        L_0x017d:
            r0 = move-exception;
            r18 = r2;
        L_0x0180:
            goto L_0x0183;
        L_0x0181:
            r18 = r2;
        L_0x0183:
            goto L_0x019a;
        L_0x0184:
            r18 = r2;
            r2 = r1.dimensionRatio;
            r2 = r2.substring(r14);
            r3 = r2.length();
            if (r3 <= 0) goto L_0x019a;
        L_0x0192:
            r3 = java.lang.Float.parseFloat(r2);	 Catch:{ NumberFormatException -> 0x0199 }
            r1.dimensionRatioValue = r3;	 Catch:{ NumberFormatException -> 0x0199 }
            goto L_0x019a;
        L_0x0199:
            r0 = move-exception;
        L_0x019a:
            goto L_0x01e7;
        L_0x019b:
            goto L_0x01e7;
        L_0x019c:
            goto L_0x01e7;
        L_0x019d:
            goto L_0x01e7;
        L_0x019e:
            goto L_0x01e7;
        L_0x019f:
            r2 = r1.matchConstraintPercentHeight;
            r2 = r5.getFloat(r11, r2);
            r3 = 0;
            r2 = java.lang.Math.max(r3, r2);
            r1.matchConstraintPercentHeight = r2;
            goto L_0x01e7;
        L_0x01ad:
            r2 = r1.matchConstraintMaxHeight;	 Catch:{ Exception -> 0x01b6 }
            r2 = r5.getDimensionPixelSize(r11, r2);	 Catch:{ Exception -> 0x01b6 }
            r1.matchConstraintMaxHeight = r2;	 Catch:{ Exception -> 0x01b6 }
            goto L_0x01e7;
        L_0x01b6:
            r0 = move-exception;
            r2 = r0;
            r3 = r1.matchConstraintMaxHeight;
            r3 = r5.getInt(r11, r3);
            if (r3 != r13) goto L_0x01c2;
        L_0x01c0:
            r1.matchConstraintMaxHeight = r13;
        L_0x01c2:
            goto L_0x01e7;
        L_0x01c3:
            r2 = r1.matchConstraintMinHeight;	 Catch:{ Exception -> 0x01cc }
            r2 = r5.getDimensionPixelSize(r11, r2);	 Catch:{ Exception -> 0x01cc }
            r1.matchConstraintMinHeight = r2;	 Catch:{ Exception -> 0x01cc }
            goto L_0x01e7;
        L_0x01cc:
            r0 = move-exception;
            r2 = r0;
            r3 = r1.matchConstraintMinHeight;
            r3 = r5.getInt(r11, r3);
            if (r3 != r13) goto L_0x01d8;
        L_0x01d6:
            r1.matchConstraintMinHeight = r13;
        L_0x01d8:
            goto L_0x01e7;
        L_0x01d9:
            r2 = r1.matchConstraintPercentWidth;
            r2 = r5.getFloat(r11, r2);
            r3 = 0;
            r2 = java.lang.Math.max(r3, r2);
            r1.matchConstraintPercentWidth = r2;
        L_0x01e7:
            r2 = 0;
            r4 = 1;
            goto L_0x0405;
        L_0x01eb:
            r2 = r1.matchConstraintMaxWidth;	 Catch:{ Exception -> 0x01f4 }
            r2 = r5.getDimensionPixelSize(r11, r2);	 Catch:{ Exception -> 0x01f4 }
            r1.matchConstraintMaxWidth = r2;	 Catch:{ Exception -> 0x01f4 }
            goto L_0x01e7;
        L_0x01f4:
            r0 = move-exception;
            r2 = r0;
            r3 = r1.matchConstraintMaxWidth;
            r3 = r5.getInt(r11, r3);
            if (r3 != r13) goto L_0x0200;
        L_0x01fe:
            r1.matchConstraintMaxWidth = r13;
        L_0x0200:
            goto L_0x01e7;
        L_0x0201:
            r2 = r1.matchConstraintMinWidth;	 Catch:{ Exception -> 0x020a }
            r2 = r5.getDimensionPixelSize(r11, r2);	 Catch:{ Exception -> 0x020a }
            r1.matchConstraintMinWidth = r2;	 Catch:{ Exception -> 0x020a }
            goto L_0x01e7;
        L_0x020a:
            r0 = move-exception;
            r2 = r0;
            r3 = r1.matchConstraintMinWidth;
            r3 = r5.getInt(r11, r3);
            if (r3 != r13) goto L_0x0216;
        L_0x0214:
            r1.matchConstraintMinWidth = r13;
        L_0x0216:
            goto L_0x01e7;
        L_0x0217:
            r2 = 0;
            r3 = r5.getInt(r11, r2);
            r1.matchConstraintDefaultHeight = r3;
            r2 = r1.matchConstraintDefaultHeight;
            r3 = 1;
            if (r2 != r3) goto L_0x022b;
        L_0x0223:
            r2 = "ConstraintLayout";
            r3 = "layout_constraintHeight_default=\"wrap\" is deprecated.\nUse layout_height=\"WRAP_CONTENT\" and layout_constrainedHeight=\"true\" instead.";
            android.util.Log.e(r2, r3);
            goto L_0x01e7;
        L_0x022b:
            r4 = r3;
            r2 = 0;
            goto L_0x0405;
        L_0x022f:
            r2 = 0;
            r3 = r5.getInt(r11, r2);
            r1.matchConstraintDefaultWidth = r3;
            r3 = r1.matchConstraintDefaultWidth;
            r4 = 1;
            if (r3 != r4) goto L_0x0405;
        L_0x023b:
            r3 = "ConstraintLayout";
            r6 = "layout_constraintWidth_default=\"wrap\" is deprecated.\nUse layout_width=\"WRAP_CONTENT\" and layout_constrainedWidth=\"true\" instead.";
            android.util.Log.e(r3, r6);
            goto L_0x0405;
        L_0x0244:
            r2 = r3;
            r4 = r6;
            r3 = r1.verticalBias;
            r3 = r5.getFloat(r11, r3);
            r1.verticalBias = r3;
            goto L_0x0405;
        L_0x0250:
            r2 = r3;
            r4 = r6;
            r3 = r1.horizontalBias;
            r3 = r5.getFloat(r11, r3);
            r1.horizontalBias = r3;
            goto L_0x0405;
        L_0x025c:
            r2 = r3;
            r4 = r6;
            r3 = r1.constrainedHeight;
            r3 = r5.getBoolean(r11, r3);
            r1.constrainedHeight = r3;
            goto L_0x0405;
        L_0x0268:
            r2 = r3;
            r4 = r6;
            r3 = r1.constrainedWidth;
            r3 = r5.getBoolean(r11, r3);
            r1.constrainedWidth = r3;
            goto L_0x0405;
        L_0x0274:
            r2 = r3;
            r4 = r6;
            r3 = r1.goneEndMargin;
            r3 = r5.getDimensionPixelSize(r11, r3);
            r1.goneEndMargin = r3;
            goto L_0x0405;
        L_0x0280:
            r2 = r3;
            r4 = r6;
            r3 = r1.goneStartMargin;
            r3 = r5.getDimensionPixelSize(r11, r3);
            r1.goneStartMargin = r3;
            goto L_0x0405;
        L_0x028c:
            r2 = r3;
            r4 = r6;
            r3 = r1.goneBottomMargin;
            r3 = r5.getDimensionPixelSize(r11, r3);
            r1.goneBottomMargin = r3;
            goto L_0x0405;
        L_0x0298:
            r2 = r3;
            r4 = r6;
            r3 = r1.goneRightMargin;
            r3 = r5.getDimensionPixelSize(r11, r3);
            r1.goneRightMargin = r3;
            goto L_0x0405;
        L_0x02a4:
            r2 = r3;
            r4 = r6;
            r3 = r1.goneTopMargin;
            r3 = r5.getDimensionPixelSize(r11, r3);
            r1.goneTopMargin = r3;
            goto L_0x0405;
        L_0x02b0:
            r2 = r3;
            r4 = r6;
            r3 = r1.goneLeftMargin;
            r3 = r5.getDimensionPixelSize(r11, r3);
            r1.goneLeftMargin = r3;
            goto L_0x0405;
        L_0x02bc:
            r2 = r3;
            r4 = r6;
            r3 = r1.endToEnd;
            r3 = r5.getResourceId(r11, r3);
            r1.endToEnd = r3;
            r3 = r1.endToEnd;
            r6 = -1;
            if (r3 != r6) goto L_0x0406;
        L_0x02cb:
            r3 = r5.getInt(r11, r6);
            r1.endToEnd = r3;
            goto L_0x0406;
        L_0x02d3:
            r4 = r6;
            r6 = r2;
            r2 = r3;
            r3 = r1.endToStart;
            r3 = r5.getResourceId(r11, r3);
            r1.endToStart = r3;
            r3 = r1.endToStart;
            if (r3 != r6) goto L_0x0406;
        L_0x02e2:
            r3 = r5.getInt(r11, r6);
            r1.endToStart = r3;
            goto L_0x0406;
        L_0x02ea:
            r4 = r6;
            r6 = r2;
            r2 = r3;
            r3 = r1.startToStart;
            r3 = r5.getResourceId(r11, r3);
            r1.startToStart = r3;
            r3 = r1.startToStart;
            if (r3 != r6) goto L_0x0406;
        L_0x02f9:
            r3 = r5.getInt(r11, r6);
            r1.startToStart = r3;
            goto L_0x0406;
        L_0x0301:
            r4 = r6;
            r6 = r2;
            r2 = r3;
            r3 = r1.startToEnd;
            r3 = r5.getResourceId(r11, r3);
            r1.startToEnd = r3;
            r3 = r1.startToEnd;
            if (r3 != r6) goto L_0x0406;
        L_0x0310:
            r3 = r5.getInt(r11, r6);
            r1.startToEnd = r3;
            goto L_0x0406;
        L_0x0318:
            r4 = r6;
            r6 = r2;
            r2 = r3;
            r3 = r1.baselineToBaseline;
            r3 = r5.getResourceId(r11, r3);
            r1.baselineToBaseline = r3;
            r3 = r1.baselineToBaseline;
            if (r3 != r6) goto L_0x0406;
        L_0x0327:
            r3 = r5.getInt(r11, r6);
            r1.baselineToBaseline = r3;
            goto L_0x0406;
        L_0x032f:
            r4 = r6;
            r6 = r2;
            r2 = r3;
            r3 = r1.bottomToBottom;
            r3 = r5.getResourceId(r11, r3);
            r1.bottomToBottom = r3;
            r3 = r1.bottomToBottom;
            if (r3 != r6) goto L_0x0406;
        L_0x033e:
            r3 = r5.getInt(r11, r6);
            r1.bottomToBottom = r3;
            goto L_0x0406;
        L_0x0346:
            r4 = r6;
            r6 = r2;
            r2 = r3;
            r3 = r1.bottomToTop;
            r3 = r5.getResourceId(r11, r3);
            r1.bottomToTop = r3;
            r3 = r1.bottomToTop;
            if (r3 != r6) goto L_0x0406;
        L_0x0355:
            r3 = r5.getInt(r11, r6);
            r1.bottomToTop = r3;
            goto L_0x0406;
        L_0x035d:
            r4 = r6;
            r6 = r2;
            r2 = r3;
            r3 = r1.topToBottom;
            r3 = r5.getResourceId(r11, r3);
            r1.topToBottom = r3;
            r3 = r1.topToBottom;
            if (r3 != r6) goto L_0x0406;
        L_0x036c:
            r3 = r5.getInt(r11, r6);
            r1.topToBottom = r3;
            goto L_0x0406;
        L_0x0374:
            r4 = r6;
            r6 = r2;
            r2 = r3;
            r3 = r1.topToTop;
            r3 = r5.getResourceId(r11, r3);
            r1.topToTop = r3;
            r3 = r1.topToTop;
            if (r3 != r6) goto L_0x0406;
        L_0x0383:
            r3 = r5.getInt(r11, r6);
            r1.topToTop = r3;
            goto L_0x0406;
        L_0x038b:
            r4 = r6;
            r6 = r2;
            r2 = r3;
            r3 = r1.rightToRight;
            r3 = r5.getResourceId(r11, r3);
            r1.rightToRight = r3;
            r3 = r1.rightToRight;
            if (r3 != r6) goto L_0x0406;
        L_0x039a:
            r3 = r5.getInt(r11, r6);
            r1.rightToRight = r3;
            goto L_0x0406;
        L_0x03a2:
            r4 = r6;
            r6 = r2;
            r2 = r3;
            r3 = r1.rightToLeft;
            r3 = r5.getResourceId(r11, r3);
            r1.rightToLeft = r3;
            r3 = r1.rightToLeft;
            if (r3 != r6) goto L_0x0406;
        L_0x03b1:
            r3 = r5.getInt(r11, r6);
            r1.rightToLeft = r3;
            goto L_0x0406;
        L_0x03b8:
            r4 = r6;
            r6 = r2;
            r2 = r3;
            r3 = r1.leftToRight;
            r3 = r5.getResourceId(r11, r3);
            r1.leftToRight = r3;
            r3 = r1.leftToRight;
            if (r3 != r6) goto L_0x0406;
        L_0x03c7:
            r3 = r5.getInt(r11, r6);
            r1.leftToRight = r3;
            goto L_0x0406;
        L_0x03ce:
            r4 = r6;
            r6 = r2;
            r2 = r3;
            r3 = r1.leftToLeft;
            r3 = r5.getResourceId(r11, r3);
            r1.leftToLeft = r3;
            r3 = r1.leftToLeft;
            if (r3 != r6) goto L_0x0406;
        L_0x03dd:
            r3 = r5.getInt(r11, r6);
            r1.leftToLeft = r3;
            goto L_0x0405;
        L_0x03e4:
            r2 = r3;
            r4 = r6;
            r3 = r1.guidePercent;
            r3 = r5.getFloat(r11, r3);
            r1.guidePercent = r3;
            goto L_0x0405;
        L_0x03ef:
            r2 = r3;
            r4 = r6;
            r3 = r1.guideEnd;
            r3 = r5.getDimensionPixelOffset(r11, r3);
            r1.guideEnd = r3;
            goto L_0x0405;
        L_0x03fa:
            r2 = r3;
            r4 = r6;
            r3 = r1.guideBegin;
            r3 = r5.getDimensionPixelOffset(r11, r3);
            r1.guideBegin = r3;
        L_0x0405:
            r6 = -1;
        L_0x0406:
            r13 = 0;
            goto L_0x045a;
        L_0x0408:
            r2 = r3;
            r4 = r6;
            r3 = r1.circleAngle;
            r3 = r5.getFloat(r11, r3);
            r6 = 1135869952; // 0x43b40000 float:360.0 double:5.611943214E-315;
            r3 = r3 % r6;
            r1.circleAngle = r3;
            r3 = r1.circleAngle;
            r13 = 0;
            r3 = (r3 > r13 ? 1 : (r3 == r13 ? 0 : -1));
            if (r3 >= 0) goto L_0x0430;
        L_0x041c:
            r3 = r1.circleAngle;
            r3 = r6 - r3;
            r3 = r3 % r6;
            r1.circleAngle = r3;
            goto L_0x0430;
        L_0x0424:
            r2 = r3;
            r13 = r4;
            r4 = r6;
            r3 = r1.circleRadius;
            r3 = r5.getDimensionPixelSize(r11, r3);
            r1.circleRadius = r3;
        L_0x0430:
            r6 = -1;
            goto L_0x045a;
        L_0x0432:
            r2 = r3;
            r13 = r4;
            r4 = r6;
            r3 = r1.circleConstraint;
            r3 = r5.getResourceId(r11, r3);
            r1.circleConstraint = r3;
            r3 = r1.circleConstraint;
            r6 = -1;
            if (r3 != r6) goto L_0x045a;
        L_0x0442:
            r3 = r5.getInt(r11, r6);
            r1.circleConstraint = r3;
            goto L_0x045a;
        L_0x0449:
            r13 = r4;
            r4 = r6;
            r6 = r2;
            r2 = r3;
            r3 = r1.orientation;
            r3 = r5.getInt(r11, r3);
            r1.orientation = r3;
            goto L_0x045a;
        L_0x0456:
            r13 = r4;
            r4 = r6;
            r6 = r2;
            r2 = r3;
        L_0x045a:
            r10 = r10 + 1;
            r3 = r2;
            r2 = r6;
            r6 = r4;
            r4 = r13;
            goto L_0x00a0;
        L_0x0462:
            r5.recycle();
            r19.validate();
            return;
            */
            throw new UnsupportedOperationException("Method not decompiled: android.support.constraint.ConstraintLayout.LayoutParams.<init>(android.content.Context, android.util.AttributeSet):void");
        }

        public void validate() {
            this.isGuideline = false;
            this.horizontalDimensionFixed = ConstraintLayout.USE_CONSTRAINTS_HELPER;
            this.verticalDimensionFixed = ConstraintLayout.USE_CONSTRAINTS_HELPER;
            if (this.width == -2 && this.constrainedWidth) {
                this.horizontalDimensionFixed = false;
                this.matchConstraintDefaultWidth = 1;
            }
            if (this.height == -2 && this.constrainedHeight) {
                this.verticalDimensionFixed = false;
                this.matchConstraintDefaultHeight = 1;
            }
            if (this.width == 0 || this.width == -1) {
                this.horizontalDimensionFixed = false;
                if (this.width == 0 && this.matchConstraintDefaultWidth == 1) {
                    this.width = -2;
                    this.constrainedWidth = ConstraintLayout.USE_CONSTRAINTS_HELPER;
                }
            }
            if (this.height == 0 || this.height == -1) {
                this.verticalDimensionFixed = false;
                if (this.height == 0 && this.matchConstraintDefaultHeight == 1) {
                    this.height = -2;
                    this.constrainedHeight = ConstraintLayout.USE_CONSTRAINTS_HELPER;
                }
            }
            if (this.guidePercent != -1.0f || this.guideBegin != -1 || this.guideEnd != -1) {
                this.isGuideline = ConstraintLayout.USE_CONSTRAINTS_HELPER;
                this.horizontalDimensionFixed = ConstraintLayout.USE_CONSTRAINTS_HELPER;
                this.verticalDimensionFixed = ConstraintLayout.USE_CONSTRAINTS_HELPER;
                if (!(this.widget instanceof Guideline)) {
                    this.widget = new Guideline();
                }
                ((Guideline) this.widget).setOrientation(this.orientation);
            }
        }

        public LayoutParams(int width, int height) {
            super(width, height);
            this.guideBegin = -1;
            this.guideEnd = -1;
            this.guidePercent = -1.0f;
            this.leftToLeft = -1;
            this.leftToRight = -1;
            this.rightToLeft = -1;
            this.rightToRight = -1;
            this.topToTop = -1;
            this.topToBottom = -1;
            this.bottomToTop = -1;
            this.bottomToBottom = -1;
            this.baselineToBaseline = -1;
            this.circleConstraint = -1;
            this.circleRadius = 0;
            this.circleAngle = 0.0f;
            this.startToEnd = -1;
            this.startToStart = -1;
            this.endToStart = -1;
            this.endToEnd = -1;
            this.goneLeftMargin = -1;
            this.goneTopMargin = -1;
            this.goneRightMargin = -1;
            this.goneBottomMargin = -1;
            this.goneStartMargin = -1;
            this.goneEndMargin = -1;
            this.horizontalBias = 0.5f;
            this.verticalBias = 0.5f;
            this.dimensionRatio = null;
            this.dimensionRatioValue = 0.0f;
            this.dimensionRatioSide = 1;
            this.horizontalWeight = 0.0f;
            this.verticalWeight = 0.0f;
            this.horizontalChainStyle = 0;
            this.verticalChainStyle = 0;
            this.matchConstraintDefaultWidth = 0;
            this.matchConstraintDefaultHeight = 0;
            this.matchConstraintMinWidth = 0;
            this.matchConstraintMinHeight = 0;
            this.matchConstraintMaxWidth = 0;
            this.matchConstraintMaxHeight = 0;
            this.matchConstraintPercentWidth = 1.0f;
            this.matchConstraintPercentHeight = 1.0f;
            this.editorAbsoluteX = -1;
            this.editorAbsoluteY = -1;
            this.orientation = -1;
            this.constrainedWidth = false;
            this.constrainedHeight = false;
            this.horizontalDimensionFixed = ConstraintLayout.USE_CONSTRAINTS_HELPER;
            this.verticalDimensionFixed = ConstraintLayout.USE_CONSTRAINTS_HELPER;
            this.needsBaseline = false;
            this.isGuideline = false;
            this.isHelper = false;
            this.isInPlaceholder = false;
            this.resolvedLeftToLeft = -1;
            this.resolvedLeftToRight = -1;
            this.resolvedRightToLeft = -1;
            this.resolvedRightToRight = -1;
            this.resolveGoneLeftMargin = -1;
            this.resolveGoneRightMargin = -1;
            this.resolvedHorizontalBias = 0.5f;
            this.widget = new ConstraintWidget();
            this.helped = false;
        }

        public LayoutParams(android.view.ViewGroup.LayoutParams source) {
            super(source);
            this.guideBegin = -1;
            this.guideEnd = -1;
            this.guidePercent = -1.0f;
            this.leftToLeft = -1;
            this.leftToRight = -1;
            this.rightToLeft = -1;
            this.rightToRight = -1;
            this.topToTop = -1;
            this.topToBottom = -1;
            this.bottomToTop = -1;
            this.bottomToBottom = -1;
            this.baselineToBaseline = -1;
            this.circleConstraint = -1;
            this.circleRadius = 0;
            this.circleAngle = 0.0f;
            this.startToEnd = -1;
            this.startToStart = -1;
            this.endToStart = -1;
            this.endToEnd = -1;
            this.goneLeftMargin = -1;
            this.goneTopMargin = -1;
            this.goneRightMargin = -1;
            this.goneBottomMargin = -1;
            this.goneStartMargin = -1;
            this.goneEndMargin = -1;
            this.horizontalBias = 0.5f;
            this.verticalBias = 0.5f;
            this.dimensionRatio = null;
            this.dimensionRatioValue = 0.0f;
            this.dimensionRatioSide = 1;
            this.horizontalWeight = 0.0f;
            this.verticalWeight = 0.0f;
            this.horizontalChainStyle = 0;
            this.verticalChainStyle = 0;
            this.matchConstraintDefaultWidth = 0;
            this.matchConstraintDefaultHeight = 0;
            this.matchConstraintMinWidth = 0;
            this.matchConstraintMinHeight = 0;
            this.matchConstraintMaxWidth = 0;
            this.matchConstraintMaxHeight = 0;
            this.matchConstraintPercentWidth = 1.0f;
            this.matchConstraintPercentHeight = 1.0f;
            this.editorAbsoluteX = -1;
            this.editorAbsoluteY = -1;
            this.orientation = -1;
            this.constrainedWidth = false;
            this.constrainedHeight = false;
            this.horizontalDimensionFixed = ConstraintLayout.USE_CONSTRAINTS_HELPER;
            this.verticalDimensionFixed = ConstraintLayout.USE_CONSTRAINTS_HELPER;
            this.needsBaseline = false;
            this.isGuideline = false;
            this.isHelper = false;
            this.isInPlaceholder = false;
            this.resolvedLeftToLeft = -1;
            this.resolvedLeftToRight = -1;
            this.resolvedRightToLeft = -1;
            this.resolvedRightToRight = -1;
            this.resolveGoneLeftMargin = -1;
            this.resolveGoneRightMargin = -1;
            this.resolvedHorizontalBias = 0.5f;
            this.widget = new ConstraintWidget();
            this.helped = false;
        }

        @TargetApi(17)
        public void resolveLayoutDirection(int layoutDirection) {
            int preLeftMargin = this.leftMargin;
            int preRightMargin = this.rightMargin;
            super.resolveLayoutDirection(layoutDirection);
            this.resolvedRightToLeft = -1;
            this.resolvedRightToRight = -1;
            this.resolvedLeftToLeft = -1;
            this.resolvedLeftToRight = -1;
            this.resolveGoneLeftMargin = -1;
            this.resolveGoneRightMargin = -1;
            this.resolveGoneLeftMargin = this.goneLeftMargin;
            this.resolveGoneRightMargin = this.goneRightMargin;
            this.resolvedHorizontalBias = this.horizontalBias;
            this.resolvedGuideBegin = this.guideBegin;
            this.resolvedGuideEnd = this.guideEnd;
            this.resolvedGuidePercent = this.guidePercent;
            if (1 == getLayoutDirection() ? ConstraintLayout.USE_CONSTRAINTS_HELPER : false) {
                boolean startEndDefined = false;
                if (this.startToEnd != -1) {
                    this.resolvedRightToLeft = this.startToEnd;
                    startEndDefined = ConstraintLayout.USE_CONSTRAINTS_HELPER;
                } else if (this.startToStart != -1) {
                    this.resolvedRightToRight = this.startToStart;
                    startEndDefined = ConstraintLayout.USE_CONSTRAINTS_HELPER;
                }
                if (this.endToStart != -1) {
                    this.resolvedLeftToRight = this.endToStart;
                    startEndDefined = ConstraintLayout.USE_CONSTRAINTS_HELPER;
                }
                if (this.endToEnd != -1) {
                    this.resolvedLeftToLeft = this.endToEnd;
                    startEndDefined = ConstraintLayout.USE_CONSTRAINTS_HELPER;
                }
                if (this.goneStartMargin != -1) {
                    this.resolveGoneRightMargin = this.goneStartMargin;
                }
                if (this.goneEndMargin != -1) {
                    this.resolveGoneLeftMargin = this.goneEndMargin;
                }
                if (startEndDefined) {
                    this.resolvedHorizontalBias = 1.0f - this.horizontalBias;
                }
                if (this.isGuideline && this.orientation == 1) {
                    if (this.guidePercent != -1.0f) {
                        this.resolvedGuidePercent = 1.0f - this.guidePercent;
                        this.resolvedGuideBegin = -1;
                        this.resolvedGuideEnd = -1;
                    } else if (this.guideBegin != -1) {
                        this.resolvedGuideEnd = this.guideBegin;
                        this.resolvedGuideBegin = -1;
                        this.resolvedGuidePercent = -1.0f;
                    } else if (this.guideEnd != -1) {
                        this.resolvedGuideBegin = this.guideEnd;
                        this.resolvedGuideEnd = -1;
                        this.resolvedGuidePercent = -1.0f;
                    }
                }
            } else {
                if (this.startToEnd != -1) {
                    this.resolvedLeftToRight = this.startToEnd;
                }
                if (this.startToStart != -1) {
                    this.resolvedLeftToLeft = this.startToStart;
                }
                if (this.endToStart != -1) {
                    this.resolvedRightToLeft = this.endToStart;
                }
                if (this.endToEnd != -1) {
                    this.resolvedRightToRight = this.endToEnd;
                }
                if (this.goneStartMargin != -1) {
                    this.resolveGoneLeftMargin = this.goneStartMargin;
                }
                if (this.goneEndMargin != -1) {
                    this.resolveGoneRightMargin = this.goneEndMargin;
                }
            }
            if (this.endToStart == -1 && this.endToEnd == -1 && this.startToStart == -1 && this.startToEnd == -1) {
                if (this.rightToLeft != -1) {
                    this.resolvedRightToLeft = this.rightToLeft;
                    if (this.rightMargin <= 0 && preRightMargin > 0) {
                        this.rightMargin = preRightMargin;
                    }
                } else if (this.rightToRight != -1) {
                    this.resolvedRightToRight = this.rightToRight;
                    if (this.rightMargin <= 0 && preRightMargin > 0) {
                        this.rightMargin = preRightMargin;
                    }
                }
                if (this.leftToLeft != -1) {
                    this.resolvedLeftToLeft = this.leftToLeft;
                    if (this.leftMargin <= 0 && preLeftMargin > 0) {
                        this.leftMargin = preLeftMargin;
                    }
                } else if (this.leftToRight != -1) {
                    this.resolvedLeftToRight = this.leftToRight;
                    if (this.leftMargin <= 0 && preLeftMargin > 0) {
                        this.leftMargin = preLeftMargin;
                    }
                }
            }
        }
    }

    public void setDesignInformation(int type, Object value1, Object value2) {
        if (type == 0 && (value1 instanceof String) && (value2 instanceof Integer)) {
            if (this.mDesignIds == null) {
                this.mDesignIds = new HashMap();
            }
            String name = (String) value1;
            int index = name.indexOf("/");
            if (index != -1) {
                name = name.substring(index + 1);
            }
            this.mDesignIds.put(name, Integer.valueOf(((Integer) value2).intValue()));
        }
    }

    public Object getDesignInformation(int type, Object value) {
        if (type == 0 && (value instanceof String)) {
            String name = (String) value;
            if (this.mDesignIds != null && this.mDesignIds.containsKey(name)) {
                return this.mDesignIds.get(name);
            }
        }
        return null;
    }

    public ConstraintLayout(Context context) {
        super(context);
        init(null);
    }

    public ConstraintLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public ConstraintLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    public void setId(int id) {
        this.mChildrenByIds.remove(getId());
        super.setId(id);
        this.mChildrenByIds.put(getId(), this);
    }

    private void init(AttributeSet attrs) {
        this.mLayoutWidget.setCompanionWidget(this);
        this.mChildrenByIds.put(getId(), this);
        this.mConstraintSet = null;
        if (attrs != null) {
            TypedArray a = getContext().obtainStyledAttributes(attrs, C0012R.styleable.ConstraintLayout_Layout);
            int N = a.getIndexCount();
            for (int i = 0; i < N; i++) {
                int attr = a.getIndex(i);
                if (attr == C0012R.styleable.ConstraintLayout_Layout_android_minWidth) {
                    this.mMinWidth = a.getDimensionPixelOffset(attr, this.mMinWidth);
                } else if (attr == C0012R.styleable.ConstraintLayout_Layout_android_minHeight) {
                    this.mMinHeight = a.getDimensionPixelOffset(attr, this.mMinHeight);
                } else if (attr == C0012R.styleable.ConstraintLayout_Layout_android_maxWidth) {
                    this.mMaxWidth = a.getDimensionPixelOffset(attr, this.mMaxWidth);
                } else if (attr == C0012R.styleable.ConstraintLayout_Layout_android_maxHeight) {
                    this.mMaxHeight = a.getDimensionPixelOffset(attr, this.mMaxHeight);
                } else if (attr == C0012R.styleable.ConstraintLayout_Layout_layout_optimizationLevel) {
                    this.mOptimizationLevel = a.getInt(attr, this.mOptimizationLevel);
                } else if (attr == C0012R.styleable.ConstraintLayout_Layout_constraintSet) {
                    int id = a.getResourceId(attr, 0);
                    try {
                        this.mConstraintSet = new ConstraintSet();
                        this.mConstraintSet.load(getContext(), id);
                    } catch (NotFoundException e) {
                        this.mConstraintSet = null;
                    }
                    this.mConstraintSetId = id;
                }
            }
            a.recycle();
        }
        this.mLayoutWidget.setOptimizationLevel(this.mOptimizationLevel);
    }

    public void addView(View child, int index, android.view.ViewGroup.LayoutParams params) {
        super.addView(child, index, params);
        if (VERSION.SDK_INT < 14) {
            onViewAdded(child);
        }
    }

    public void removeView(View view) {
        super.removeView(view);
        if (VERSION.SDK_INT < 14) {
            onViewRemoved(view);
        }
    }

    public void onViewAdded(View view) {
        if (VERSION.SDK_INT >= 14) {
            super.onViewAdded(view);
        }
        ConstraintWidget widget = getViewWidget(view);
        if ((view instanceof Guideline) && !(widget instanceof Guideline)) {
            LayoutParams layoutParams = (LayoutParams) view.getLayoutParams();
            layoutParams.widget = new Guideline();
            layoutParams.isGuideline = USE_CONSTRAINTS_HELPER;
            ((Guideline) layoutParams.widget).setOrientation(layoutParams.orientation);
        }
        if (view instanceof ConstraintHelper) {
            ConstraintHelper helper = (ConstraintHelper) view;
            helper.validateParams();
            ((LayoutParams) view.getLayoutParams()).isHelper = USE_CONSTRAINTS_HELPER;
            if (!this.mConstraintHelpers.contains(helper)) {
                this.mConstraintHelpers.add(helper);
            }
        }
        this.mChildrenByIds.put(view.getId(), view);
        this.mDirtyHierarchy = USE_CONSTRAINTS_HELPER;
    }

    public void onViewRemoved(View view) {
        if (VERSION.SDK_INT >= 14) {
            super.onViewRemoved(view);
        }
        this.mChildrenByIds.remove(view.getId());
        ConstraintWidget widget = getViewWidget(view);
        this.mLayoutWidget.remove(widget);
        this.mConstraintHelpers.remove(view);
        this.mVariableDimensionsWidgets.remove(widget);
        this.mDirtyHierarchy = USE_CONSTRAINTS_HELPER;
    }

    public void setMinWidth(int value) {
        if (value != this.mMinWidth) {
            this.mMinWidth = value;
            requestLayout();
        }
    }

    public void setMinHeight(int value) {
        if (value != this.mMinHeight) {
            this.mMinHeight = value;
            requestLayout();
        }
    }

    public int getMinWidth() {
        return this.mMinWidth;
    }

    public int getMinHeight() {
        return this.mMinHeight;
    }

    public void setMaxWidth(int value) {
        if (value != this.mMaxWidth) {
            this.mMaxWidth = value;
            requestLayout();
        }
    }

    public void setMaxHeight(int value) {
        if (value != this.mMaxHeight) {
            this.mMaxHeight = value;
            requestLayout();
        }
    }

    public int getMaxWidth() {
        return this.mMaxWidth;
    }

    public int getMaxHeight() {
        return this.mMaxHeight;
    }

    private void updateHierarchy() {
        int count = getChildCount();
        boolean recompute = false;
        for (int i = 0; i < count; i++) {
            if (getChildAt(i).isLayoutRequested()) {
                recompute = USE_CONSTRAINTS_HELPER;
                break;
            }
        }
        if (recompute) {
            this.mVariableDimensionsWidgets.clear();
            setChildrenConstraints();
        }
    }

    private void setChildrenConstraints() {
        int i;
        View view;
        int slashIndex;
        int i2;
        int helperCount;
        ConstraintLayout constraintLayout = this;
        boolean isInEditMode = isInEditMode();
        int count = getChildCount();
        boolean z = false;
        int i3 = -1;
        if (isInEditMode) {
            for (i = 0; i < count; i++) {
                view = getChildAt(i);
                try {
                    String IdAsString = getResources().getResourceName(view.getId());
                    setDesignInformation(0, IdAsString, Integer.valueOf(view.getId()));
                    slashIndex = IdAsString.indexOf(47);
                    if (slashIndex != -1) {
                        IdAsString = IdAsString.substring(slashIndex + 1);
                    }
                    getTargetWidget(view.getId()).setDebugName(IdAsString);
                } catch (NotFoundException e) {
                }
            }
        }
        for (i = 0; i < count; i++) {
            ConstraintWidget widget = getViewWidget(getChildAt(i));
            if (widget != null) {
                widget.reset();
            }
        }
        if (constraintLayout.mConstraintSetId != -1) {
            for (i = 0; i < count; i++) {
                view = getChildAt(i);
                if (view.getId() == constraintLayout.mConstraintSetId && (view instanceof Constraints)) {
                    constraintLayout.mConstraintSet = ((Constraints) view).getConstraintSet();
                }
            }
        }
        if (constraintLayout.mConstraintSet != null) {
            constraintLayout.mConstraintSet.applyToInternal(constraintLayout);
        }
        constraintLayout.mLayoutWidget.removeAllChildren();
        i = constraintLayout.mConstraintHelpers.size();
        if (i > 0) {
            for (i2 = 0; i2 < i; i2++) {
                ((ConstraintHelper) constraintLayout.mConstraintHelpers.get(i2)).updatePreLayout(constraintLayout);
            }
        }
        for (i2 = 0; i2 < count; i2++) {
            View child = getChildAt(i2);
            if (child instanceof Placeholder) {
                ((Placeholder) child).updatePreLayout(constraintLayout);
            }
        }
        i2 = 0;
        while (i2 < count) {
            int count2;
            boolean z2;
            int i4;
            child = getChildAt(i2);
            ConstraintWidget widget2 = getViewWidget(child);
            if (widget2 == null) {
                count2 = count;
                z2 = z;
                i4 = i3;
                helperCount = i;
            } else {
                LayoutParams layoutParams = (LayoutParams) child.getLayoutParams();
                layoutParams.validate();
                if (layoutParams.helped) {
                    layoutParams.helped = z;
                } else if (isInEditMode) {
                    try {
                        String IdAsString2 = getResources().getResourceName(child.getId());
                        setDesignInformation(z, IdAsString2, Integer.valueOf(child.getId()));
                        getTargetWidget(child.getId()).setDebugName(IdAsString2.substring(IdAsString2.indexOf("id/") + 3));
                    } catch (NotFoundException e2) {
                    }
                }
                widget2.setVisibility(child.getVisibility());
                if (layoutParams.isInPlaceholder) {
                    widget2.setVisibility(8);
                }
                widget2.setCompanionWidget(child);
                constraintLayout.mLayoutWidget.add(widget2);
                if (!(layoutParams.verticalDimensionFixed && layoutParams.horizontalDimensionFixed)) {
                    constraintLayout.mVariableDimensionsWidgets.add(widget2);
                }
                int resolvedGuideBegin;
                int resolvedGuideEnd;
                if (layoutParams.isGuideline) {
                    Guideline guideline = (Guideline) widget2;
                    resolvedGuideBegin = layoutParams.resolvedGuideBegin;
                    resolvedGuideEnd = layoutParams.resolvedGuideEnd;
                    float resolvedGuidePercent = layoutParams.resolvedGuidePercent;
                    if (VERSION.SDK_INT < 17) {
                        resolvedGuideBegin = layoutParams.guideBegin;
                        resolvedGuideEnd = layoutParams.guideEnd;
                        resolvedGuidePercent = layoutParams.guidePercent;
                    }
                    if (resolvedGuidePercent != -1.0f) {
                        guideline.setGuidePercent(resolvedGuidePercent);
                    } else if (resolvedGuideBegin != i3) {
                        guideline.setGuideBegin(resolvedGuideBegin);
                    } else if (resolvedGuideEnd != i3) {
                        guideline.setGuideEnd(resolvedGuideEnd);
                    }
                } else if (!(layoutParams.leftToLeft == i3 && layoutParams.leftToRight == i3 && layoutParams.rightToLeft == i3 && layoutParams.rightToRight == i3 && layoutParams.startToStart == i3 && layoutParams.startToEnd == i3 && layoutParams.endToStart == i3 && layoutParams.endToEnd == i3 && layoutParams.topToTop == i3 && layoutParams.topToBottom == i3 && layoutParams.bottomToTop == i3 && layoutParams.bottomToBottom == i3 && layoutParams.baselineToBaseline == i3 && layoutParams.editorAbsoluteX == i3 && layoutParams.editorAbsoluteY == i3 && layoutParams.circleConstraint == i3 && layoutParams.width != i3 && layoutParams.height != i3)) {
                    int resolveGoneLeftMargin;
                    LayoutParams layoutParams2;
                    int resolvedLeftToLeft = layoutParams.resolvedLeftToLeft;
                    slashIndex = layoutParams.resolvedLeftToRight;
                    resolvedGuideBegin = layoutParams.resolvedRightToLeft;
                    resolvedGuideEnd = layoutParams.resolvedRightToRight;
                    int resolveGoneLeftMargin2 = layoutParams.resolveGoneLeftMargin;
                    i3 = layoutParams.resolveGoneRightMargin;
                    i4 = layoutParams.resolvedHorizontalBias;
                    count2 = count;
                    int resolvedLeftToLeft2 = resolvedLeftToLeft;
                    if (VERSION.SDK_INT < 17) {
                        int resolvedLeftToLeft3;
                        int i5;
                        count = layoutParams.leftToLeft;
                        resolvedLeftToLeft = layoutParams.leftToRight;
                        resolvedGuideBegin = layoutParams.rightToLeft;
                        resolvedGuideEnd = layoutParams.rightToRight;
                        slashIndex = layoutParams.goneLeftMargin;
                        i3 = layoutParams.goneRightMargin;
                        i4 = layoutParams.horizontalBias;
                        if (count == -1 && resolvedLeftToLeft == -1) {
                            resolvedLeftToLeft3 = count;
                            if (layoutParams.startToStart != -1) {
                                int i6 = resolvedLeftToLeft;
                                resolvedLeftToLeft = layoutParams.startToStart;
                                count = i6;
                                if (resolvedGuideBegin == -1 || resolvedGuideEnd != -1) {
                                    i5 = count;
                                } else {
                                    i5 = count;
                                    if (layoutParams.endToStart != -1) {
                                        resolvedGuideBegin = layoutParams.endToStart;
                                    } else if (layoutParams.endToEnd != -1) {
                                        resolvedGuideEnd = layoutParams.endToEnd;
                                    }
                                }
                                resolvedLeftToLeft2 = i3;
                                resolveGoneLeftMargin = slashIndex;
                                count = -1;
                                i3 = i5;
                            } else if (layoutParams.startToEnd != -1) {
                                count = layoutParams.startToEnd;
                                resolvedLeftToLeft = resolvedLeftToLeft3;
                                if (resolvedGuideBegin == -1) {
                                }
                                i5 = count;
                                resolvedLeftToLeft2 = i3;
                                resolveGoneLeftMargin = slashIndex;
                                count = -1;
                                i3 = i5;
                            }
                        } else {
                            resolvedLeftToLeft3 = count;
                        }
                        count = resolvedLeftToLeft;
                        resolvedLeftToLeft = resolvedLeftToLeft3;
                        if (resolvedGuideBegin == -1) {
                        }
                        i5 = count;
                        resolvedLeftToLeft2 = i3;
                        resolveGoneLeftMargin = slashIndex;
                        count = -1;
                        i3 = i5;
                    } else {
                        count = -1;
                        resolveGoneLeftMargin = resolveGoneLeftMargin2;
                        resolvedLeftToLeft = resolvedLeftToLeft2;
                        resolvedLeftToLeft2 = i3;
                        i3 = slashIndex;
                    }
                    resolveGoneLeftMargin2 = resolvedGuideBegin;
                    resolvedGuideBegin = i4;
                    View view2;
                    if (layoutParams.circleConstraint != count) {
                        count = getTargetWidget(layoutParams.circleConstraint);
                        if (count != 0) {
                            widget2.connectCircularConstraint(count, layoutParams.circleAngle, layoutParams.circleRadius);
                        }
                        int i7 = resolvedLeftToLeft;
                        helperCount = i;
                        view2 = child;
                        resolvedLeftToLeft = resolvedGuideBegin;
                        count = resolvedGuideEnd;
                        i = resolveGoneLeftMargin2;
                        layoutParams2 = layoutParams;
                    } else {
                        float resolvedLeftToLeft4;
                        ConstraintWidget target;
                        if (resolvedLeftToLeft != -1) {
                            count = getTargetWidget(resolvedLeftToLeft);
                            if (count != 0) {
                                resolvedLeftToLeft4 = resolvedGuideBegin;
                                ConstraintWidget constraintWidget = count;
                                ConstraintWidget target2 = count;
                                count = resolvedGuideEnd;
                                helperCount = i;
                                i = resolveGoneLeftMargin2;
                                layoutParams2 = layoutParams;
                                widget2.immediateConnect(Type.LEFT, constraintWidget, Type.LEFT, layoutParams.leftMargin, resolveGoneLeftMargin);
                            } else {
                                helperCount = i;
                                view2 = child;
                                resolvedLeftToLeft4 = resolvedGuideBegin;
                                count = resolvedGuideEnd;
                                i = resolveGoneLeftMargin2;
                                layoutParams2 = layoutParams;
                            }
                        } else {
                            helperCount = i;
                            view2 = child;
                            resolvedLeftToLeft4 = resolvedGuideBegin;
                            count = resolvedGuideEnd;
                            i = resolveGoneLeftMargin2;
                            layoutParams2 = layoutParams;
                            if (i3 != -1) {
                                target = getTargetWidget(i3);
                                if (target != null) {
                                    widget2.immediateConnect(Type.LEFT, target, Type.RIGHT, layoutParams2.leftMargin, resolveGoneLeftMargin);
                                }
                            }
                        }
                        if (i != -1) {
                            target = getTargetWidget(i);
                            if (target != null) {
                                widget2.immediateConnect(Type.RIGHT, target, Type.LEFT, layoutParams2.rightMargin, resolvedLeftToLeft2);
                            }
                        } else if (count != -1) {
                            target = getTargetWidget(count);
                            if (target != null) {
                                widget2.immediateConnect(Type.RIGHT, target, Type.RIGHT, layoutParams2.rightMargin, resolvedLeftToLeft2);
                            }
                        }
                        if (layoutParams2.topToTop != -1) {
                            target = getTargetWidget(layoutParams2.topToTop);
                            if (target != null) {
                                widget2.immediateConnect(Type.TOP, target, Type.TOP, layoutParams2.topMargin, layoutParams2.goneTopMargin);
                            }
                        } else if (layoutParams2.topToBottom != -1) {
                            target = getTargetWidget(layoutParams2.topToBottom);
                            if (target != null) {
                                widget2.immediateConnect(Type.TOP, target, Type.BOTTOM, layoutParams2.topMargin, layoutParams2.goneTopMargin);
                            }
                        }
                        if (layoutParams2.bottomToTop != -1) {
                            target = getTargetWidget(layoutParams2.bottomToTop);
                            if (target != null) {
                                widget2.immediateConnect(Type.BOTTOM, target, Type.TOP, layoutParams2.bottomMargin, layoutParams2.goneBottomMargin);
                            }
                        } else if (layoutParams2.bottomToBottom != -1) {
                            target = getTargetWidget(layoutParams2.bottomToBottom);
                            if (target != null) {
                                widget2.immediateConnect(Type.BOTTOM, target, Type.BOTTOM, layoutParams2.bottomMargin, layoutParams2.goneBottomMargin);
                            }
                        }
                        if (layoutParams2.baselineToBaseline != -1) {
                            View view3 = (View) constraintLayout.mChildrenByIds.get(layoutParams2.baselineToBaseline);
                            ConstraintWidget target3 = getTargetWidget(layoutParams2.baselineToBaseline);
                            if (!(target3 == null || view3 == null || !(view3.getLayoutParams() instanceof LayoutParams))) {
                                LayoutParams targetParams = (LayoutParams) view3.getLayoutParams();
                                layoutParams2.needsBaseline = USE_CONSTRAINTS_HELPER;
                                targetParams.needsBaseline = USE_CONSTRAINTS_HELPER;
                                ConstraintAnchor baseline = widget2.getAnchor(Type.BASELINE);
                                baseline.connect(target3.getAnchor(Type.BASELINE), 0, -1, Strength.STRONG, 0, USE_CONSTRAINTS_HELPER);
                                widget2.getAnchor(Type.TOP).reset();
                                widget2.getAnchor(Type.BOTTOM).reset();
                            }
                        }
                        if (resolvedLeftToLeft4 >= 0.0f && resolvedLeftToLeft4 != 0.5f) {
                            widget2.setHorizontalBiasPercent(resolvedLeftToLeft4);
                        }
                        if (layoutParams2.verticalBias >= 0.0f && layoutParams2.verticalBias != 0.5f) {
                            widget2.setVerticalBiasPercent(layoutParams2.verticalBias);
                        }
                    }
                    if (isInEditMode && !(layoutParams2.editorAbsoluteX == -1 && layoutParams2.editorAbsoluteY == -1)) {
                        widget2.setOrigin(layoutParams2.editorAbsoluteX, layoutParams2.editorAbsoluteY);
                    }
                    if (layoutParams2.horizontalDimensionFixed) {
                        widget2.setHorizontalDimensionBehaviour(DimensionBehaviour.FIXED);
                        widget2.setWidth(layoutParams2.width);
                    } else if (layoutParams2.width == -1) {
                        widget2.setHorizontalDimensionBehaviour(DimensionBehaviour.MATCH_PARENT);
                        widget2.getAnchor(Type.LEFT).mMargin = layoutParams2.leftMargin;
                        widget2.getAnchor(Type.RIGHT).mMargin = layoutParams2.rightMargin;
                    } else {
                        widget2.setHorizontalDimensionBehaviour(DimensionBehaviour.MATCH_CONSTRAINT);
                        widget2.setWidth(0);
                    }
                    if (layoutParams2.verticalDimensionFixed) {
                        z2 = false;
                        i4 = -1;
                        widget2.setVerticalDimensionBehaviour(DimensionBehaviour.FIXED);
                        widget2.setHeight(layoutParams2.height);
                    } else {
                        i4 = -1;
                        if (layoutParams2.height == -1) {
                            widget2.setVerticalDimensionBehaviour(DimensionBehaviour.MATCH_PARENT);
                            widget2.getAnchor(Type.TOP).mMargin = layoutParams2.topMargin;
                            widget2.getAnchor(Type.BOTTOM).mMargin = layoutParams2.bottomMargin;
                            z2 = false;
                        } else {
                            widget2.setVerticalDimensionBehaviour(DimensionBehaviour.MATCH_CONSTRAINT);
                            z2 = false;
                            widget2.setHeight(0);
                        }
                    }
                    if (layoutParams2.dimensionRatio != null) {
                        widget2.setDimensionRatio(layoutParams2.dimensionRatio);
                    }
                    widget2.setHorizontalWeight(layoutParams2.horizontalWeight);
                    widget2.setVerticalWeight(layoutParams2.verticalWeight);
                    widget2.setHorizontalChainStyle(layoutParams2.horizontalChainStyle);
                    widget2.setVerticalChainStyle(layoutParams2.verticalChainStyle);
                    widget2.setHorizontalMatchStyle(layoutParams2.matchConstraintDefaultWidth, layoutParams2.matchConstraintMinWidth, layoutParams2.matchConstraintMaxWidth, layoutParams2.matchConstraintPercentWidth);
                    widget2.setVerticalMatchStyle(layoutParams2.matchConstraintDefaultHeight, layoutParams2.matchConstraintMinHeight, layoutParams2.matchConstraintMaxHeight, layoutParams2.matchConstraintPercentHeight);
                }
                count2 = count;
                i4 = i3;
                helperCount = i;
                z2 = false;
            }
            i2++;
            z = z2;
            i3 = i4;
            count = count2;
            i = helperCount;
        }
        helperCount = i;
    }

    private final ConstraintWidget getTargetWidget(int id) {
        if (id == 0) {
            return this.mLayoutWidget;
        }
        View view = (View) this.mChildrenByIds.get(id);
        if (view == this) {
            return this.mLayoutWidget;
        }
        return view == null ? null : ((LayoutParams) view.getLayoutParams()).widget;
    }

    public final ConstraintWidget getViewWidget(View view) {
        if (view == this) {
            return this.mLayoutWidget;
        }
        return view == null ? null : ((LayoutParams) view.getLayoutParams()).widget;
    }

    private void internalMeasureChildren(int parentWidthSpec, int parentHeightSpec) {
        ConstraintLayout constraintLayout = this;
        int i = parentWidthSpec;
        int i2 = parentHeightSpec;
        int heightPadding = getPaddingTop() + getPaddingBottom();
        int widthPadding = getPaddingLeft() + getPaddingRight();
        int widgetsCount = getChildCount();
        int i3 = 0;
        while (i3 < widgetsCount) {
            int heightPadding2;
            View child = constraintLayout.getChildAt(i3);
            if (child.getVisibility() != 8) {
                LayoutParams params = (LayoutParams) child.getLayoutParams();
                ConstraintWidget widget = params.widget;
                if (params.isGuideline) {
                    heightPadding2 = heightPadding;
                } else if (!params.isHelper) {
                    boolean doMeasure;
                    boolean didWrapMeasureWidth;
                    boolean didWrapMeasureHeight;
                    int childWidthMeasureSpec;
                    int childWidthMeasureSpec2;
                    Metrics metrics;
                    int baseline;
                    widget.setVisibility(child.getVisibility());
                    int width = params.width;
                    int height = params.height;
                    if (!(params.horizontalDimensionFixed || params.verticalDimensionFixed || ((!params.horizontalDimensionFixed && params.matchConstraintDefaultWidth == 1) || params.width == -1))) {
                        if (!params.verticalDimensionFixed) {
                            if (params.matchConstraintDefaultHeight != 1) {
                                if (params.height == -1) {
                                }
                            }
                        }
                        doMeasure = false;
                        didWrapMeasureWidth = false;
                        didWrapMeasureHeight = false;
                        if (doMeasure) {
                            heightPadding2 = heightPadding;
                        } else {
                            if (width == 0) {
                                childWidthMeasureSpec = getChildMeasureSpec(i, widthPadding, -2);
                                didWrapMeasureWidth = USE_CONSTRAINTS_HELPER;
                            } else if (width != -1) {
                                childWidthMeasureSpec = getChildMeasureSpec(i, widthPadding, -1);
                            } else {
                                if (width == -2) {
                                    didWrapMeasureWidth = USE_CONSTRAINTS_HELPER;
                                }
                                childWidthMeasureSpec = getChildMeasureSpec(i, widthPadding, width);
                            }
                            childWidthMeasureSpec2 = childWidthMeasureSpec;
                            if (height == 0) {
                                childWidthMeasureSpec = getChildMeasureSpec(i2, heightPadding, -2);
                                didWrapMeasureHeight = USE_CONSTRAINTS_HELPER;
                            } else if (height != -1) {
                                childWidthMeasureSpec = getChildMeasureSpec(i2, heightPadding, -1);
                            } else {
                                if (height == -2) {
                                    didWrapMeasureHeight = USE_CONSTRAINTS_HELPER;
                                }
                                childWidthMeasureSpec = getChildMeasureSpec(i2, heightPadding, height);
                            }
                            child.measure(childWidthMeasureSpec2, childWidthMeasureSpec);
                            if (constraintLayout.mMetrics == null) {
                                metrics = constraintLayout.mMetrics;
                                heightPadding2 = heightPadding;
                                metrics.measures++;
                            } else {
                                heightPadding2 = heightPadding;
                            }
                            widget.setWidthWrapContent(width != -2 ? USE_CONSTRAINTS_HELPER : false);
                            widget.setHeightWrapContent(height != -2 ? USE_CONSTRAINTS_HELPER : false);
                            width = child.getMeasuredWidth();
                            height = child.getMeasuredHeight();
                        }
                        widget.setWidth(width);
                        widget.setHeight(height);
                        if (didWrapMeasureWidth) {
                            widget.setWrapWidth(width);
                        }
                        if (didWrapMeasureHeight) {
                            widget.setWrapHeight(height);
                        }
                        if (params.needsBaseline) {
                            baseline = child.getBaseline();
                            if (baseline != -1) {
                                widget.setBaselineDistance(baseline);
                            }
                        }
                    }
                    doMeasure = USE_CONSTRAINTS_HELPER;
                    didWrapMeasureWidth = false;
                    didWrapMeasureHeight = false;
                    if (doMeasure) {
                        heightPadding2 = heightPadding;
                    } else {
                        if (width == 0) {
                            childWidthMeasureSpec = getChildMeasureSpec(i, widthPadding, -2);
                            didWrapMeasureWidth = USE_CONSTRAINTS_HELPER;
                        } else if (width != -1) {
                            if (width == -2) {
                                didWrapMeasureWidth = USE_CONSTRAINTS_HELPER;
                            }
                            childWidthMeasureSpec = getChildMeasureSpec(i, widthPadding, width);
                        } else {
                            childWidthMeasureSpec = getChildMeasureSpec(i, widthPadding, -1);
                        }
                        childWidthMeasureSpec2 = childWidthMeasureSpec;
                        if (height == 0) {
                            childWidthMeasureSpec = getChildMeasureSpec(i2, heightPadding, -2);
                            didWrapMeasureHeight = USE_CONSTRAINTS_HELPER;
                        } else if (height != -1) {
                            if (height == -2) {
                                didWrapMeasureHeight = USE_CONSTRAINTS_HELPER;
                            }
                            childWidthMeasureSpec = getChildMeasureSpec(i2, heightPadding, height);
                        } else {
                            childWidthMeasureSpec = getChildMeasureSpec(i2, heightPadding, -1);
                        }
                        child.measure(childWidthMeasureSpec2, childWidthMeasureSpec);
                        if (constraintLayout.mMetrics == null) {
                            heightPadding2 = heightPadding;
                        } else {
                            metrics = constraintLayout.mMetrics;
                            heightPadding2 = heightPadding;
                            metrics.measures++;
                        }
                        if (width != -2) {
                        }
                        widget.setWidthWrapContent(width != -2 ? USE_CONSTRAINTS_HELPER : false);
                        if (height != -2) {
                        }
                        widget.setHeightWrapContent(height != -2 ? USE_CONSTRAINTS_HELPER : false);
                        width = child.getMeasuredWidth();
                        height = child.getMeasuredHeight();
                    }
                    widget.setWidth(width);
                    widget.setHeight(height);
                    if (didWrapMeasureWidth) {
                        widget.setWrapWidth(width);
                    }
                    if (didWrapMeasureHeight) {
                        widget.setWrapHeight(height);
                    }
                    if (params.needsBaseline) {
                        baseline = child.getBaseline();
                        if (baseline != -1) {
                            widget.setBaselineDistance(baseline);
                        }
                    }
                }
                i3++;
                heightPadding = heightPadding2;
                constraintLayout = this;
                i = parentWidthSpec;
                i2 = parentHeightSpec;
            }
            heightPadding2 = heightPadding;
            i3++;
            heightPadding = heightPadding2;
            constraintLayout = this;
            i = parentWidthSpec;
            i2 = parentHeightSpec;
        }
    }

    private void updatePostMeasures() {
        int i;
        int widgetsCount = getChildCount();
        int i2 = 0;
        for (i = 0; i < widgetsCount; i++) {
            View child = getChildAt(i);
            if (child instanceof Placeholder) {
                ((Placeholder) child).updatePostMeasure(this);
            }
        }
        i = this.mConstraintHelpers.size();
        if (i > 0) {
            while (i2 < i) {
                ((ConstraintHelper) this.mConstraintHelpers.get(i2)).updatePostMeasure(this);
                i2++;
            }
        }
    }

    private void internalMeasureDimensions(int parentWidthSpec, int parentHeightSpec) {
        int heightPadding;
        int widthPadding;
        int widgetsCount;
        boolean didWrapMeasureWidth;
        ConstraintLayout constraintLayout = this;
        int i = parentWidthSpec;
        int i2 = parentHeightSpec;
        int heightPadding2 = getPaddingTop() + getPaddingBottom();
        int widthPadding2 = getPaddingLeft() + getPaddingRight();
        int widgetsCount2 = getChildCount();
        int i3 = 0;
        while (true) {
            int i4 = 8;
            if (i3 >= widgetsCount2) {
                break;
            }
            boolean didWrapMeasureHeight;
            View child = getChildAt(i3);
            if (child.getVisibility() != 8) {
                LayoutParams params = (LayoutParams) child.getLayoutParams();
                ConstraintWidget widget = params.widget;
                if (params.isGuideline) {
                    heightPadding = heightPadding2;
                    widthPadding = widthPadding2;
                    widgetsCount = widgetsCount2;
                } else if (!params.isHelper) {
                    widget.setVisibility(child.getVisibility());
                    int width = params.width;
                    int height = params.height;
                    if (width == 0) {
                        heightPadding = heightPadding2;
                        widthPadding = widthPadding2;
                        widgetsCount = widgetsCount2;
                    } else if (height == 0) {
                        heightPadding = heightPadding2;
                        widthPadding = widthPadding2;
                        widgetsCount = widgetsCount2;
                    } else {
                        didWrapMeasureWidth = false;
                        didWrapMeasureHeight = false;
                        if (width == -2) {
                            didWrapMeasureWidth = USE_CONSTRAINTS_HELPER;
                        }
                        int childWidthMeasureSpec = getChildMeasureSpec(i, widthPadding2, width);
                        if (height == -2) {
                            didWrapMeasureHeight = USE_CONSTRAINTS_HELPER;
                        }
                        child.measure(childWidthMeasureSpec, getChildMeasureSpec(i2, heightPadding2, height));
                        if (constraintLayout.mMetrics != null) {
                            Metrics metrics = constraintLayout.mMetrics;
                            heightPadding = heightPadding2;
                            widthPadding = widthPadding2;
                            widgetsCount = widgetsCount2;
                            metrics.measures++;
                        } else {
                            heightPadding = heightPadding2;
                            widthPadding = widthPadding2;
                            widgetsCount = widgetsCount2;
                        }
                        widget.setWidthWrapContent(width == -2 ? 1 : 0);
                        widget.setHeightWrapContent(height == -2 ? USE_CONSTRAINTS_HELPER : false);
                        i2 = child.getMeasuredWidth();
                        heightPadding2 = child.getMeasuredHeight();
                        widget.setWidth(i2);
                        widget.setHeight(heightPadding2);
                        if (didWrapMeasureWidth) {
                            widget.setWrapWidth(i2);
                        }
                        if (didWrapMeasureHeight) {
                            widget.setWrapHeight(heightPadding2);
                        }
                        if (params.needsBaseline) {
                            widthPadding2 = child.getBaseline();
                            if (widthPadding2 != -1) {
                                widget.setBaselineDistance(widthPadding2);
                            }
                        }
                        if (params.horizontalDimensionFixed && params.verticalDimensionFixed) {
                            widget.getResolutionWidth().resolve(i2);
                            widget.getResolutionHeight().resolve(heightPadding2);
                        }
                    }
                    widget.getResolutionWidth().invalidate();
                    widget.getResolutionHeight().invalidate();
                }
                i3++;
                heightPadding2 = heightPadding;
                widthPadding2 = widthPadding;
                widgetsCount2 = widgetsCount;
                i2 = parentHeightSpec;
            }
            heightPadding = heightPadding2;
            widthPadding = widthPadding2;
            widgetsCount = widgetsCount2;
            i3++;
            heightPadding2 = heightPadding;
            widthPadding2 = widthPadding;
            widgetsCount2 = widgetsCount;
            i2 = parentHeightSpec;
        }
        heightPadding = heightPadding2;
        widthPadding = widthPadding2;
        widgetsCount = widgetsCount2;
        constraintLayout.mLayoutWidget.solveGraph();
        i2 = 0;
        while (true) {
            heightPadding2 = widgetsCount;
            int heightPadding3;
            int widthPadding3;
            if (i2 < heightPadding2) {
                int i5;
                int widgetsCount3;
                View child2 = getChildAt(i2);
                if (child2.getVisibility() != i4) {
                    LayoutParams params2 = (LayoutParams) child2.getLayoutParams();
                    ConstraintWidget widget2 = params2.widget;
                    if (params2.isGuideline) {
                        i5 = i2;
                        widgetsCount3 = heightPadding2;
                        heightPadding3 = heightPadding;
                        widthPadding3 = widthPadding;
                    } else if (!params2.isHelper) {
                        widget2.setVisibility(child2.getVisibility());
                        i3 = params2.width;
                        int height2 = params2.height;
                        if (i3 == 0 || height2 == 0) {
                            ResolutionAnchor left = widget2.getAnchor(Type.LEFT).getResolutionNode();
                            ResolutionAnchor right = widget2.getAnchor(Type.RIGHT).getResolutionNode();
                            boolean bothHorizontal = (widget2.getAnchor(Type.LEFT).getTarget() == null || widget2.getAnchor(Type.RIGHT).getTarget() == null) ? false : USE_CONSTRAINTS_HELPER;
                            ResolutionAnchor top = widget2.getAnchor(Type.TOP).getResolutionNode();
                            ResolutionAnchor bottom = widget2.getAnchor(Type.BOTTOM).getResolutionNode();
                            boolean bothVertical = (widget2.getAnchor(Type.TOP).getTarget() == null || widget2.getAnchor(Type.BOTTOM).getTarget() == null) ? false : USE_CONSTRAINTS_HELPER;
                            if (i3 != 0 || height2 != 0 || !bothHorizontal || !bothVertical) {
                                boolean resolveWidth;
                                boolean resolveHeight;
                                int childHeightMeasureSpec;
                                didWrapMeasureWidth = false;
                                didWrapMeasureHeight = false;
                                widgetsCount3 = heightPadding2;
                                boolean resolveWidth2 = constraintLayout.mLayoutWidget.getHorizontalDimensionBehaviour() != DimensionBehaviour.WRAP_CONTENT ? USE_CONSTRAINTS_HELPER : false;
                                i5 = i2;
                                boolean i6 = constraintLayout.mLayoutWidget.getVerticalDimensionBehaviour() != DimensionBehaviour.WRAP_CONTENT ? USE_CONSTRAINTS_HELPER : false;
                                if (!resolveWidth2) {
                                    widget2.getResolutionWidth().invalidate();
                                }
                                if (!i6) {
                                    widget2.getResolutionHeight().invalidate();
                                }
                                if (i3 == 0) {
                                    if (resolveWidth2 && widget2.isSpreadWidth() && bothHorizontal && left.isResolved() && right.isResolved()) {
                                        i3 = (int) (right.getResolvedValue() - left.getResolvedValue());
                                        widget2.getResolutionWidth().resolve(i3);
                                        i4 = widthPadding;
                                        widthPadding = getChildMeasureSpec(i, i4, i3);
                                    } else {
                                        i4 = widthPadding;
                                        widthPadding = getChildMeasureSpec(i, i4, true);
                                        didWrapMeasureWidth = USE_CONSTRAINTS_HELPER;
                                        resolveWidth2 = false;
                                    }
                                    resolveWidth = resolveWidth2;
                                } else {
                                    resolveWidth = resolveWidth2;
                                    i4 = widthPadding;
                                    if (i3 == -1) {
                                        widthPadding = getChildMeasureSpec(i, i4, -1);
                                    } else {
                                        if (i3 == -2) {
                                            didWrapMeasureWidth = USE_CONSTRAINTS_HELPER;
                                        }
                                        widthPadding = getChildMeasureSpec(i, i4, i3);
                                    }
                                }
                                heightPadding2 = widthPadding;
                                ResolutionAnchor resolutionAnchor;
                                if (height2 != 0) {
                                    resolveHeight = i6;
                                    resolutionAnchor = left;
                                    i2 = heightPadding;
                                    i = parentHeightSpec;
                                    if (height2 == -1) {
                                        childHeightMeasureSpec = getChildMeasureSpec(i, i2, -1);
                                    } else {
                                        if (height2 == -2) {
                                            didWrapMeasureHeight = USE_CONSTRAINTS_HELPER;
                                        }
                                        childHeightMeasureSpec = getChildMeasureSpec(i, i2, height2);
                                    }
                                } else if (i6 && widget2.isSpreadHeight() && bothVertical && top.isResolved() && bottom.isResolved()) {
                                    height2 = (int) (bottom.getResolvedValue() - top.getResolvedValue());
                                    widget2.getResolutionHeight().resolve(height2);
                                    resolveHeight = i6;
                                    i2 = heightPadding;
                                    childHeightMeasureSpec = getChildMeasureSpec(parentHeightSpec, i2, height2);
                                    resolutionAnchor = left;
                                } else {
                                    resolveHeight = i6;
                                    i2 = heightPadding;
                                    childHeightMeasureSpec = getChildMeasureSpec(parentHeightSpec, i2, -2);
                                    didWrapMeasureHeight = USE_CONSTRAINTS_HELPER;
                                    resolveHeight = null;
                                }
                                int childHeightMeasureSpec2 = childHeightMeasureSpec;
                                child2.measure(heightPadding2, childHeightMeasureSpec2);
                                if (constraintLayout.mMetrics != null) {
                                    Metrics metrics2 = constraintLayout.mMetrics;
                                    heightPadding3 = i2;
                                    widthPadding3 = i4;
                                    metrics2.measures++;
                                } else {
                                    heightPadding3 = i2;
                                    int i7 = heightPadding2;
                                    int i8 = childHeightMeasureSpec2;
                                    widthPadding3 = i4;
                                }
                                widget2.setWidthWrapContent(i3 == -2 ? 1 : 0);
                                widget2.setHeightWrapContent(height2 == -2 ? 1 : 0);
                                i2 = child2.getMeasuredWidth();
                                heightPadding2 = child2.getMeasuredHeight();
                                widget2.setWidth(i2);
                                widget2.setHeight(heightPadding2);
                                if (didWrapMeasureWidth) {
                                    widget2.setWrapWidth(i2);
                                }
                                if (didWrapMeasureHeight) {
                                    widget2.setWrapHeight(heightPadding2);
                                }
                                if (resolveWidth) {
                                    widget2.getResolutionWidth().resolve(i2);
                                } else {
                                    widget2.getResolutionWidth().remove();
                                }
                                if (resolveHeight) {
                                    widget2.getResolutionHeight().resolve(heightPadding2);
                                } else {
                                    widget2.getResolutionHeight().remove();
                                }
                                if (params2.needsBaseline) {
                                    i3 = child2.getBaseline();
                                    if (i3 != -1) {
                                        widget2.setBaselineDistance(i3);
                                    }
                                }
                            }
                        }
                    }
                    i2 = i5 + 1;
                    widgetsCount = widgetsCount3;
                    heightPadding = heightPadding3;
                    widthPadding = widthPadding3;
                    i = parentWidthSpec;
                    i4 = 8;
                }
                i5 = i2;
                widgetsCount3 = heightPadding2;
                heightPadding3 = heightPadding;
                widthPadding3 = widthPadding;
                i2 = i5 + 1;
                widgetsCount = widgetsCount3;
                heightPadding = heightPadding3;
                widthPadding = widthPadding3;
                i = parentWidthSpec;
                i4 = 8;
            } else {
                heightPadding3 = heightPadding;
                widthPadding3 = widthPadding;
                return;
            }
        }
    }

    public void fillMetrics(Metrics metrics) {
        this.mMetrics = metrics;
        this.mLayoutWidget.fillMetrics(metrics);
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        boolean fitSizeHeight;
        int paddingLeft;
        int paddingTop;
        int REMEASURES_A;
        int REMEASURES_B;
        int startingWidth;
        int startingHeight;
        int childState;
        boolean containerWrapWidth;
        int childState2;
        ConstraintWidget widget;
        int sizeDependentWidgetsCount;
        int childState3;
        boolean needSolverPass;
        int i;
        ConstraintWidget validLastMeasure;
        View fitSizeWidth;
        ConstraintWidget widget2;
        int i2 = widthMeasureSpec;
        int i3 = heightMeasureSpec;
        long time = System.currentTimeMillis();
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        boolean validLastMeasure2 = (this.mLastMeasureWidth == -1 || r0.mLastMeasureHeight == -1) ? false : USE_CONSTRAINTS_HELPER;
        boolean sameSize = (widthMode == 1073741824 && heightMode == 1073741824 && widthSize == r0.mLastMeasureWidth && heightSize == r0.mLastMeasureHeight) ? USE_CONSTRAINTS_HELPER : false;
        boolean sameMode = (widthMode == r0.mLastMeasureWidthMode && heightMode == r0.mLastMeasureHeightMode) ? USE_CONSTRAINTS_HELPER : false;
        boolean sameMeasure = (sameMode && widthSize == r0.mLastMeasureWidthSize && heightSize == r0.mLastMeasureHeightSize) ? USE_CONSTRAINTS_HELPER : false;
        boolean fitSizeWidth2 = (sameMode && widthMode == Integer.MIN_VALUE && heightMode == 1073741824 && widthSize >= r0.mLastMeasureWidth && heightSize == r0.mLastMeasureHeight) ? USE_CONSTRAINTS_HELPER : false;
        boolean optimiseDimensions;
        boolean needSolverPass2;
        boolean containerWrapHeight;
        int i4;
        boolean fitSizeWidth3;
        View child;
        int startingWidth2;
        int startingHeight2;
        LayoutParams params;
        int i5;
        boolean minHeight;
        int i6;
        boolean z;
        int startingHeight3;
        boolean z2;
        int i7;
        boolean z3;
        if (sameMode) {
            if (widthMode == 1073741824 && heightMode == Integer.MIN_VALUE && widthSize == r0.mLastMeasureWidth && heightSize >= r0.mLastMeasureHeight) {
                fitSizeHeight = USE_CONSTRAINTS_HELPER;
                r0.mLastMeasureWidthMode = widthMode;
                r0.mLastMeasureHeightMode = heightMode;
                r0.mLastMeasureWidthSize = widthSize;
                r0.mLastMeasureHeightSize = heightSize;
                paddingLeft = getPaddingLeft();
                paddingTop = getPaddingTop();
                REMEASURES_A = 0;
                r0.mLayoutWidget.setX(paddingLeft);
                r0.mLayoutWidget.setY(paddingTop);
                REMEASURES_B = 0;
                r0.mLayoutWidget.setMaxWidth(r0.mMaxWidth);
                r0.mLayoutWidget.setMaxHeight(r0.mMaxHeight);
                if (VERSION.SDK_INT < 17) {
                    r0.mLayoutWidget.setRtl(getLayoutDirection() != 1 ? USE_CONSTRAINTS_HELPER : false);
                }
                setSelfDimensionBehaviour(widthMeasureSpec, heightMeasureSpec);
                startingWidth = r0.mLayoutWidget.getWidth();
                startingHeight = r0.mLayoutWidget.getHeight();
                if (r0.mDirtyHierarchy) {
                    r0.mDirtyHierarchy = false;
                    updateHierarchy();
                }
                optimiseDimensions = (r0.mOptimizationLevel & 8) != 8 ? USE_CONSTRAINTS_HELPER : false;
                if (optimiseDimensions) {
                    internalMeasureChildren(widthMeasureSpec, heightMeasureSpec);
                } else {
                    r0.mLayoutWidget.preOptimize();
                    r0.mLayoutWidget.optimizeForDimensions(startingWidth, startingHeight);
                    internalMeasureDimensions(widthMeasureSpec, heightMeasureSpec);
                }
                updatePostMeasures();
                if (getChildCount() > 0) {
                    solveLinearSystem("First pass");
                }
                childState = 0;
                widthSize = r0.mVariableDimensionsWidgets.size();
                heightMode = paddingTop + getPaddingBottom();
                paddingTop = paddingLeft + getPaddingRight();
                if (widthSize <= 0) {
                    needSolverPass2 = false;
                    containerWrapWidth = r0.mLayoutWidget.getHorizontalDimensionBehaviour() != DimensionBehaviour.WRAP_CONTENT ? USE_CONSTRAINTS_HELPER : false;
                    containerWrapHeight = r0.mLayoutWidget.getVerticalDimensionBehaviour() != DimensionBehaviour.WRAP_CONTENT ? USE_CONSTRAINTS_HELPER : false;
                    childState2 = childState;
                    sameSize = Math.max(r0.mLayoutWidget.getWidth(), r0.mMinWidth);
                    sameMode = Math.max(r0.mLayoutWidget.getHeight(), r0.mMinHeight);
                    i4 = 0;
                    while (i4 < widthSize) {
                        fitSizeWidth3 = fitSizeWidth2;
                        widget = (ConstraintWidget) r0.mVariableDimensionsWidgets.get(i4);
                        sizeDependentWidgetsCount = widthSize;
                        child = (View) widget.getCompanionWidget();
                        if (child != null) {
                            startingWidth2 = startingWidth;
                            startingHeight2 = startingHeight;
                        } else {
                            startingHeight2 = startingHeight;
                            params = (LayoutParams) child.getLayoutParams();
                            startingWidth2 = startingWidth;
                            if (params.isHelper == 0) {
                                i5 = i4;
                            } else if (params.isGuideline != 0) {
                                i5 = i4;
                                if (child.getVisibility() == 8) {
                                    if (optimiseDimensions || widget.getResolutionWidth().isResolved() == 0 || widget.getResolutionHeight().isResolved() == 0) {
                                        int widthSpec = 0;
                                        if (params.width == -2 || params.horizontalDimensionFixed == 0) {
                                            i4 = MeasureSpec.makeMeasureSpec(widget.getWidth(), 1073741824);
                                        } else {
                                            i4 = getChildMeasureSpec(i2, paddingTop, params.width);
                                        }
                                        if (params.height == -2 || !params.verticalDimensionFixed) {
                                            i2 = MeasureSpec.makeMeasureSpec(widget.getHeight(), 1073741824);
                                        } else {
                                            i2 = getChildMeasureSpec(i3, heightMode, params.height);
                                        }
                                        child.measure(i4, i2);
                                        if (r0.mMetrics == 0) {
                                            startingWidth = r0.mMetrics;
                                            minHeight = sameMode;
                                            childState3 = childState2;
                                            startingWidth.additionalMeasures += USE_CONSTRAINTS_HELPER;
                                        } else {
                                            minHeight = sameMode;
                                            childState3 = childState2;
                                        }
                                        REMEASURES_A++;
                                        i2 = child.getMeasuredWidth();
                                        i3 = child.getMeasuredHeight();
                                        if (i2 != widget.getWidth()) {
                                            widget.setWidth(i2);
                                            if (optimiseDimensions) {
                                                widget.getResolutionWidth().resolve(i2);
                                            }
                                            if (containerWrapWidth && widget.getRight() > sameSize) {
                                                sameSize = Math.max(sameSize, widget.getRight() + widget.getAnchor(Type.RIGHT).getMargin());
                                            }
                                            needSolverPass2 = USE_CONSTRAINTS_HELPER;
                                        }
                                        if (i3 == widget.getHeight()) {
                                            widget.setHeight(i3);
                                            if (optimiseDimensions) {
                                                widget.getResolutionHeight().resolve(i3);
                                            }
                                            if (containerWrapHeight) {
                                                sameMode = minHeight;
                                            } else {
                                                sameMode = minHeight;
                                                if (widget.getBottom() > sameMode) {
                                                    sameMode = Math.max(sameMode, widget.getBottom() + widget.getAnchor(Type.BOTTOM).getMargin());
                                                }
                                            }
                                            needSolverPass2 = USE_CONSTRAINTS_HELPER;
                                        } else {
                                            sameMode = minHeight;
                                        }
                                        if (params.needsBaseline != 0) {
                                            startingWidth = child.getBaseline();
                                            if (!(startingWidth == -1 || startingWidth == widget.getBaselineDistance())) {
                                                widget.setBaselineDistance(startingWidth);
                                                needSolverPass2 = USE_CONSTRAINTS_HELPER;
                                            }
                                        }
                                        if (VERSION.SDK_INT < 11) {
                                            childState2 = combineMeasuredStates(childState3, child.getMeasuredState());
                                        } else {
                                            childState2 = childState3;
                                        }
                                    }
                                }
                            }
                            i4 = i5 + 1;
                            fitSizeWidth2 = fitSizeWidth3;
                            widthSize = sizeDependentWidgetsCount;
                            startingHeight = startingHeight2;
                            startingWidth = startingWidth2;
                            i2 = widthMeasureSpec;
                            i3 = heightMeasureSpec;
                        }
                        i5 = i4;
                        i4 = i5 + 1;
                        fitSizeWidth2 = fitSizeWidth3;
                        widthSize = sizeDependentWidgetsCount;
                        startingHeight = startingHeight2;
                        startingWidth = startingWidth2;
                        i2 = widthMeasureSpec;
                        i3 = heightMeasureSpec;
                    }
                    startingWidth2 = startingWidth;
                    startingHeight2 = startingHeight;
                    sizeDependentWidgetsCount = widthSize;
                    fitSizeWidth3 = fitSizeWidth2;
                    if (needSolverPass2) {
                        startingWidth = startingHeight2;
                        i3 = startingWidth2;
                    } else {
                        i3 = startingWidth2;
                        r0.mLayoutWidget.setWidth(i3);
                        startingWidth = startingHeight2;
                        r0.mLayoutWidget.setHeight(startingWidth);
                        if (optimiseDimensions) {
                            r0.mLayoutWidget.solveGraph();
                        }
                        solveLinearSystem("2nd pass");
                        needSolverPass = false;
                        if (r0.mLayoutWidget.getWidth() < sameSize) {
                            r0.mLayoutWidget.setWidth(sameSize);
                            needSolverPass = USE_CONSTRAINTS_HELPER;
                        }
                        if (r0.mLayoutWidget.getHeight() < sameMode) {
                            r0.mLayoutWidget.setHeight(sameMode);
                            needSolverPass = USE_CONSTRAINTS_HELPER;
                        }
                        if (needSolverPass) {
                            solveLinearSystem("3rd pass");
                        }
                    }
                    startingHeight = REMEASURES_B;
                    i = 0;
                    while (true) {
                        i2 = i;
                        widthSize = sizeDependentWidgetsCount;
                        if (i2 < widthSize) {
                            break;
                        }
                        validLastMeasure = (ConstraintWidget) r0.mVariableDimensionsWidgets.get(i2);
                        fitSizeWidth = (View) validLastMeasure.getCompanionWidget();
                        if (fitSizeWidth != null) {
                            i6 = i3;
                            z = containerWrapWidth;
                        } else {
                            i6 = i3;
                            z = containerWrapWidth;
                            if (fitSizeWidth.getMeasuredWidth() == validLastMeasure.getWidth()) {
                                if (fitSizeWidth.getMeasuredHeight() != validLastMeasure.getHeight()) {
                                }
                            }
                            startingHeight3 = startingWidth;
                            fitSizeWidth.measure(MeasureSpec.makeMeasureSpec(validLastMeasure.getWidth(), 1073741824), MeasureSpec.makeMeasureSpec(validLastMeasure.getHeight(), 1073741824));
                            if (r0.mMetrics == null) {
                                Metrics metrics = r0.mMetrics;
                                z2 = optimiseDimensions;
                                i7 = widthSize;
                                z3 = containerWrapHeight;
                                widget2 = validLastMeasure;
                                metrics.additionalMeasures += USE_CONSTRAINTS_HELPER;
                            } else {
                                z2 = optimiseDimensions;
                                i7 = widthSize;
                                z3 = containerWrapHeight;
                                widget2 = validLastMeasure;
                            }
                            startingHeight++;
                            i = i2 + 1;
                            i3 = i6;
                            containerWrapWidth = z;
                            startingWidth = startingHeight3;
                            optimiseDimensions = z2;
                            sizeDependentWidgetsCount = i7;
                            containerWrapHeight = z3;
                        }
                        startingHeight3 = startingWidth;
                        z2 = optimiseDimensions;
                        i7 = widthSize;
                        z3 = containerWrapHeight;
                        i = i2 + 1;
                        i3 = i6;
                        containerWrapWidth = z;
                        startingWidth = startingHeight3;
                        optimiseDimensions = z2;
                        sizeDependentWidgetsCount = i7;
                        containerWrapHeight = z3;
                    }
                    startingHeight3 = startingWidth;
                    z2 = optimiseDimensions;
                    i7 = widthSize;
                } else {
                    startingHeight3 = startingHeight;
                    z2 = optimiseDimensions;
                    i7 = widthSize;
                    int i8 = heightSize;
                    boolean z4 = validLastMeasure2;
                    boolean z5 = sameSize;
                    boolean z6 = sameMode;
                    boolean z7 = sameMeasure;
                    fitSizeWidth3 = fitSizeWidth2;
                    childState2 = childState;
                }
                i2 = r0.mLayoutWidget.getWidth() + paddingTop;
                i3 = r0.mLayoutWidget.getHeight() + heightMode;
                if (VERSION.SDK_INT < 11) {
                    widthMode = resolveSizeAndState(i3, heightMeasureSpec, childState2 << 16) & ViewCompat.MEASURED_SIZE_MASK;
                    startingWidth = Math.min(r0.mMaxWidth, resolveSizeAndState(i2, widthMeasureSpec, childState2) & ViewCompat.MEASURED_SIZE_MASK);
                    widthMode = Math.min(r0.mMaxHeight, widthMode);
                    if (r0.mLayoutWidget.isWidthMeasuredTooSmall()) {
                        startingWidth |= 16777216;
                    }
                    if (r0.mLayoutWidget.isHeightMeasuredTooSmall()) {
                        widthMode |= 16777216;
                    }
                    setMeasuredDimension(startingWidth, widthMode);
                    r0.mLastMeasureWidth = startingWidth;
                    r0.mLastMeasureHeight = widthMode;
                }
                paddingLeft = widthMeasureSpec;
                widthSize = heightMeasureSpec;
                setMeasuredDimension(i2, i3);
                r0.mLastMeasureWidth = i2;
                r0.mLastMeasureHeight = i3;
                return;
            }
        }
        fitSizeHeight = false;
        r0.mLastMeasureWidthMode = widthMode;
        r0.mLastMeasureHeightMode = heightMode;
        r0.mLastMeasureWidthSize = widthSize;
        r0.mLastMeasureHeightSize = heightSize;
        paddingLeft = getPaddingLeft();
        paddingTop = getPaddingTop();
        REMEASURES_A = 0;
        r0.mLayoutWidget.setX(paddingLeft);
        r0.mLayoutWidget.setY(paddingTop);
        REMEASURES_B = 0;
        r0.mLayoutWidget.setMaxWidth(r0.mMaxWidth);
        r0.mLayoutWidget.setMaxHeight(r0.mMaxHeight);
        if (VERSION.SDK_INT < 17) {
        } else {
            if (getLayoutDirection() != 1) {
            }
            r0.mLayoutWidget.setRtl(getLayoutDirection() != 1 ? USE_CONSTRAINTS_HELPER : false);
        }
        setSelfDimensionBehaviour(widthMeasureSpec, heightMeasureSpec);
        startingWidth = r0.mLayoutWidget.getWidth();
        startingHeight = r0.mLayoutWidget.getHeight();
        if (r0.mDirtyHierarchy) {
            r0.mDirtyHierarchy = false;
            updateHierarchy();
        }
        if ((r0.mOptimizationLevel & 8) != 8) {
        }
        if (optimiseDimensions) {
            internalMeasureChildren(widthMeasureSpec, heightMeasureSpec);
        } else {
            r0.mLayoutWidget.preOptimize();
            r0.mLayoutWidget.optimizeForDimensions(startingWidth, startingHeight);
            internalMeasureDimensions(widthMeasureSpec, heightMeasureSpec);
        }
        updatePostMeasures();
        if (getChildCount() > 0) {
            solveLinearSystem("First pass");
        }
        childState = 0;
        widthSize = r0.mVariableDimensionsWidgets.size();
        heightMode = paddingTop + getPaddingBottom();
        paddingTop = paddingLeft + getPaddingRight();
        if (widthSize <= 0) {
            startingHeight3 = startingHeight;
            z2 = optimiseDimensions;
            i7 = widthSize;
            int i82 = heightSize;
            boolean z42 = validLastMeasure2;
            boolean z52 = sameSize;
            boolean z62 = sameMode;
            boolean z72 = sameMeasure;
            fitSizeWidth3 = fitSizeWidth2;
            childState2 = childState;
        } else {
            needSolverPass2 = false;
            if (r0.mLayoutWidget.getHorizontalDimensionBehaviour() != DimensionBehaviour.WRAP_CONTENT) {
            }
            if (r0.mLayoutWidget.getVerticalDimensionBehaviour() != DimensionBehaviour.WRAP_CONTENT) {
            }
            containerWrapHeight = r0.mLayoutWidget.getVerticalDimensionBehaviour() != DimensionBehaviour.WRAP_CONTENT ? USE_CONSTRAINTS_HELPER : false;
            childState2 = childState;
            sameSize = Math.max(r0.mLayoutWidget.getWidth(), r0.mMinWidth);
            sameMode = Math.max(r0.mLayoutWidget.getHeight(), r0.mMinHeight);
            i4 = 0;
            while (i4 < widthSize) {
                fitSizeWidth3 = fitSizeWidth2;
                widget = (ConstraintWidget) r0.mVariableDimensionsWidgets.get(i4);
                sizeDependentWidgetsCount = widthSize;
                child = (View) widget.getCompanionWidget();
                if (child != null) {
                    startingHeight2 = startingHeight;
                    params = (LayoutParams) child.getLayoutParams();
                    startingWidth2 = startingWidth;
                    if (params.isHelper == 0) {
                        i5 = i4;
                    } else if (params.isGuideline != 0) {
                        i5 = i4;
                        if (child.getVisibility() == 8) {
                            if (optimiseDimensions) {
                            }
                            int widthSpec2 = 0;
                            if (params.width == -2) {
                            }
                            i4 = MeasureSpec.makeMeasureSpec(widget.getWidth(), 1073741824);
                            if (params.height == -2) {
                            }
                            i2 = MeasureSpec.makeMeasureSpec(widget.getHeight(), 1073741824);
                            child.measure(i4, i2);
                            if (r0.mMetrics == 0) {
                                minHeight = sameMode;
                                childState3 = childState2;
                            } else {
                                startingWidth = r0.mMetrics;
                                minHeight = sameMode;
                                childState3 = childState2;
                                startingWidth.additionalMeasures += USE_CONSTRAINTS_HELPER;
                            }
                            REMEASURES_A++;
                            i2 = child.getMeasuredWidth();
                            i3 = child.getMeasuredHeight();
                            if (i2 != widget.getWidth()) {
                                widget.setWidth(i2);
                                if (optimiseDimensions) {
                                    widget.getResolutionWidth().resolve(i2);
                                }
                                sameSize = Math.max(sameSize, widget.getRight() + widget.getAnchor(Type.RIGHT).getMargin());
                                needSolverPass2 = USE_CONSTRAINTS_HELPER;
                            }
                            if (i3 == widget.getHeight()) {
                                sameMode = minHeight;
                            } else {
                                widget.setHeight(i3);
                                if (optimiseDimensions) {
                                    widget.getResolutionHeight().resolve(i3);
                                }
                                if (containerWrapHeight) {
                                    sameMode = minHeight;
                                } else {
                                    sameMode = minHeight;
                                    if (widget.getBottom() > sameMode) {
                                        sameMode = Math.max(sameMode, widget.getBottom() + widget.getAnchor(Type.BOTTOM).getMargin());
                                    }
                                }
                                needSolverPass2 = USE_CONSTRAINTS_HELPER;
                            }
                            if (params.needsBaseline != 0) {
                                startingWidth = child.getBaseline();
                                widget.setBaselineDistance(startingWidth);
                                needSolverPass2 = USE_CONSTRAINTS_HELPER;
                            }
                            if (VERSION.SDK_INT < 11) {
                                childState2 = childState3;
                            } else {
                                childState2 = combineMeasuredStates(childState3, child.getMeasuredState());
                            }
                        }
                    }
                    i4 = i5 + 1;
                    fitSizeWidth2 = fitSizeWidth3;
                    widthSize = sizeDependentWidgetsCount;
                    startingHeight = startingHeight2;
                    startingWidth = startingWidth2;
                    i2 = widthMeasureSpec;
                    i3 = heightMeasureSpec;
                } else {
                    startingWidth2 = startingWidth;
                    startingHeight2 = startingHeight;
                }
                i5 = i4;
                i4 = i5 + 1;
                fitSizeWidth2 = fitSizeWidth3;
                widthSize = sizeDependentWidgetsCount;
                startingHeight = startingHeight2;
                startingWidth = startingWidth2;
                i2 = widthMeasureSpec;
                i3 = heightMeasureSpec;
            }
            startingWidth2 = startingWidth;
            startingHeight2 = startingHeight;
            sizeDependentWidgetsCount = widthSize;
            fitSizeWidth3 = fitSizeWidth2;
            if (needSolverPass2) {
                startingWidth = startingHeight2;
                i3 = startingWidth2;
            } else {
                i3 = startingWidth2;
                r0.mLayoutWidget.setWidth(i3);
                startingWidth = startingHeight2;
                r0.mLayoutWidget.setHeight(startingWidth);
                if (optimiseDimensions) {
                    r0.mLayoutWidget.solveGraph();
                }
                solveLinearSystem("2nd pass");
                needSolverPass = false;
                if (r0.mLayoutWidget.getWidth() < sameSize) {
                    r0.mLayoutWidget.setWidth(sameSize);
                    needSolverPass = USE_CONSTRAINTS_HELPER;
                }
                if (r0.mLayoutWidget.getHeight() < sameMode) {
                    r0.mLayoutWidget.setHeight(sameMode);
                    needSolverPass = USE_CONSTRAINTS_HELPER;
                }
                if (needSolverPass) {
                    solveLinearSystem("3rd pass");
                }
            }
            startingHeight = REMEASURES_B;
            i = 0;
            while (true) {
                i2 = i;
                widthSize = sizeDependentWidgetsCount;
                if (i2 < widthSize) {
                    break;
                }
                validLastMeasure = (ConstraintWidget) r0.mVariableDimensionsWidgets.get(i2);
                fitSizeWidth = (View) validLastMeasure.getCompanionWidget();
                if (fitSizeWidth != null) {
                    i6 = i3;
                    z = containerWrapWidth;
                    if (fitSizeWidth.getMeasuredWidth() == validLastMeasure.getWidth()) {
                        if (fitSizeWidth.getMeasuredHeight() != validLastMeasure.getHeight()) {
                        }
                    }
                    startingHeight3 = startingWidth;
                    fitSizeWidth.measure(MeasureSpec.makeMeasureSpec(validLastMeasure.getWidth(), 1073741824), MeasureSpec.makeMeasureSpec(validLastMeasure.getHeight(), 1073741824));
                    if (r0.mMetrics == null) {
                        z2 = optimiseDimensions;
                        i7 = widthSize;
                        z3 = containerWrapHeight;
                        widget2 = validLastMeasure;
                    } else {
                        Metrics metrics2 = r0.mMetrics;
                        z2 = optimiseDimensions;
                        i7 = widthSize;
                        z3 = containerWrapHeight;
                        widget2 = validLastMeasure;
                        metrics2.additionalMeasures += USE_CONSTRAINTS_HELPER;
                    }
                    startingHeight++;
                    i = i2 + 1;
                    i3 = i6;
                    containerWrapWidth = z;
                    startingWidth = startingHeight3;
                    optimiseDimensions = z2;
                    sizeDependentWidgetsCount = i7;
                    containerWrapHeight = z3;
                } else {
                    i6 = i3;
                    z = containerWrapWidth;
                }
                startingHeight3 = startingWidth;
                z2 = optimiseDimensions;
                i7 = widthSize;
                z3 = containerWrapHeight;
                i = i2 + 1;
                i3 = i6;
                containerWrapWidth = z;
                startingWidth = startingHeight3;
                optimiseDimensions = z2;
                sizeDependentWidgetsCount = i7;
                containerWrapHeight = z3;
            }
            startingHeight3 = startingWidth;
            z2 = optimiseDimensions;
            i7 = widthSize;
        }
        i2 = r0.mLayoutWidget.getWidth() + paddingTop;
        i3 = r0.mLayoutWidget.getHeight() + heightMode;
        if (VERSION.SDK_INT < 11) {
            paddingLeft = widthMeasureSpec;
            widthSize = heightMeasureSpec;
            setMeasuredDimension(i2, i3);
            r0.mLastMeasureWidth = i2;
            r0.mLastMeasureHeight = i3;
            return;
        }
        widthMode = resolveSizeAndState(i3, heightMeasureSpec, childState2 << 16) & ViewCompat.MEASURED_SIZE_MASK;
        startingWidth = Math.min(r0.mMaxWidth, resolveSizeAndState(i2, widthMeasureSpec, childState2) & ViewCompat.MEASURED_SIZE_MASK);
        widthMode = Math.min(r0.mMaxHeight, widthMode);
        if (r0.mLayoutWidget.isWidthMeasuredTooSmall()) {
            startingWidth |= 16777216;
        }
        if (r0.mLayoutWidget.isHeightMeasuredTooSmall()) {
            widthMode |= 16777216;
        }
        setMeasuredDimension(startingWidth, widthMode);
        r0.mLastMeasureWidth = startingWidth;
        r0.mLastMeasureHeight = widthMode;
    }

    private void setSelfDimensionBehaviour(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int heightPadding = getPaddingTop() + getPaddingBottom();
        int widthPadding = getPaddingLeft() + getPaddingRight();
        DimensionBehaviour widthBehaviour = DimensionBehaviour.FIXED;
        DimensionBehaviour heightBehaviour = DimensionBehaviour.FIXED;
        int desiredWidth = 0;
        int desiredHeight = 0;
        android.view.ViewGroup.LayoutParams params = getLayoutParams();
        if (widthMode == Integer.MIN_VALUE) {
            widthBehaviour = DimensionBehaviour.WRAP_CONTENT;
            desiredWidth = widthSize;
        } else if (widthMode == 0) {
            widthBehaviour = DimensionBehaviour.WRAP_CONTENT;
        } else if (widthMode == 1073741824) {
            desiredWidth = Math.min(r0.mMaxWidth, widthSize) - widthPadding;
        }
        if (heightMode == Integer.MIN_VALUE) {
            heightBehaviour = DimensionBehaviour.WRAP_CONTENT;
            desiredHeight = heightSize;
        } else if (heightMode == 0) {
            heightBehaviour = DimensionBehaviour.WRAP_CONTENT;
        } else if (heightMode == 1073741824) {
            desiredHeight = Math.min(r0.mMaxHeight, heightSize) - heightPadding;
        }
        r0.mLayoutWidget.setMinWidth(0);
        r0.mLayoutWidget.setMinHeight(0);
        r0.mLayoutWidget.setHorizontalDimensionBehaviour(widthBehaviour);
        r0.mLayoutWidget.setWidth(desiredWidth);
        r0.mLayoutWidget.setVerticalDimensionBehaviour(heightBehaviour);
        r0.mLayoutWidget.setHeight(desiredHeight);
        r0.mLayoutWidget.setMinWidth((r0.mMinWidth - getPaddingLeft()) - getPaddingRight());
        r0.mLayoutWidget.setMinHeight((r0.mMinHeight - getPaddingTop()) - getPaddingBottom());
    }

    protected void solveLinearSystem(String reason) {
        this.mLayoutWidget.layout();
        if (this.mMetrics != null) {
            Metrics metrics = this.mMetrics;
            metrics.resolutions++;
        }
    }

    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int i;
        int widgetsCount = getChildCount();
        boolean isInEditMode = isInEditMode();
        int i2 = 0;
        for (i = 0; i < widgetsCount; i++) {
            View child = getChildAt(i);
            LayoutParams params = (LayoutParams) child.getLayoutParams();
            ConstraintWidget widget = params.widget;
            if (child.getVisibility() != 8 || params.isGuideline || params.isHelper || isInEditMode) {
                if (!params.isInPlaceholder) {
                    int l = widget.getDrawX();
                    int t = widget.getDrawY();
                    int r = widget.getWidth() + l;
                    int b = widget.getHeight() + t;
                    child.layout(l, t, r, b);
                    if (child instanceof Placeholder) {
                        View content = ((Placeholder) child).getContent();
                        if (content != null) {
                            content.setVisibility(0);
                            content.layout(l, t, r, b);
                        }
                    }
                }
            }
        }
        i = r0.mConstraintHelpers.size();
        if (i > 0) {
            while (i2 < i) {
                ((ConstraintHelper) r0.mConstraintHelpers.get(i2)).updatePostLayout(r0);
                i2++;
            }
        }
    }

    public void setOptimizationLevel(int level) {
        this.mLayoutWidget.setOptimizationLevel(level);
    }

    public int getOptimizationLevel() {
        return this.mLayoutWidget.getOptimizationLevel();
    }

    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    protected LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(-2, -2);
    }

    protected android.view.ViewGroup.LayoutParams generateLayoutParams(android.view.ViewGroup.LayoutParams p) {
        return new LayoutParams(p);
    }

    protected boolean checkLayoutParams(android.view.ViewGroup.LayoutParams p) {
        return p instanceof LayoutParams;
    }

    public void setConstraintSet(ConstraintSet set) {
        this.mConstraintSet = set;
    }

    public View getViewById(int id) {
        return (View) this.mChildrenByIds.get(id);
    }

    public void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        if (isInEditMode()) {
            int count = getChildCount();
            float cw = (float) getWidth();
            float ch = (float) getHeight();
            float ow = 1080.0f;
            int i = 0;
            int i2 = 0;
            while (i2 < count) {
                int count2;
                float cw2;
                float ch2;
                float ow2;
                View child = getChildAt(i2);
                if (child.getVisibility() == 8) {
                    count2 = count;
                    cw2 = cw;
                    ch2 = ch;
                    ow2 = ow;
                } else {
                    String tag = child.getTag();
                    if (tag != null && (tag instanceof String)) {
                        String[] split = tag.split(",");
                        if (split.length == 4) {
                            int x = Integer.parseInt(split[i]);
                            int y = Integer.parseInt(split[1]);
                            i = (int) ((((float) x) / ow) * cw);
                            x = (int) ((((float) y) / 1920.0f) * ch);
                            y = (int) ((((float) Integer.parseInt(split[2])) / ow) * cw);
                            int h = (int) ((((float) Integer.parseInt(split[3])) / 1920.0f) * ch);
                            Paint paint = new Paint();
                            count2 = count;
                            paint.setColor(SupportMenu.CATEGORY_MASK);
                            cw2 = cw;
                            ch2 = ch;
                            ow2 = ow;
                            Canvas canvas2 = canvas;
                            Paint paint2 = paint;
                            canvas2.drawLine((float) i, (float) x, (float) (i + y), (float) x, paint2);
                            canvas2.drawLine((float) (i + y), (float) x, (float) (i + y), (float) (x + h), paint2);
                            canvas2.drawLine((float) (i + y), (float) (x + h), (float) i, (float) (x + h), paint2);
                            canvas2.drawLine((float) i, (float) (x + h), (float) i, (float) x, paint2);
                            paint.setColor(-16711936);
                            canvas2.drawLine((float) i, (float) x, (float) (i + y), (float) (x + h), paint2);
                            canvas2.drawLine((float) i, (float) (x + h), (float) (i + y), (float) x, paint2);
                        }
                    }
                    count2 = count;
                    cw2 = cw;
                    ch2 = ch;
                    ow2 = ow;
                }
                i2++;
                count = count2;
                cw = cw2;
                ch = ch2;
                ow = ow2;
                i = 0;
            }
        }
        ConstraintLayout constraintLayout = this;
    }

    public void requestLayout() {
        super.requestLayout();
        this.mDirtyHierarchy = USE_CONSTRAINTS_HELPER;
        this.mLastMeasureWidth = -1;
        this.mLastMeasureHeight = -1;
        this.mLastMeasureWidthSize = -1;
        this.mLastMeasureHeightSize = -1;
        this.mLastMeasureWidthMode = 0;
        this.mLastMeasureHeightMode = 0;
    }

    public boolean shouldDelayChildPressedState() {
        return false;
    }
}
