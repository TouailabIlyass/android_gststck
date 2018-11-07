package android.support.constraint.solver.widgets;

import android.support.constraint.solver.LinearSystem;
import android.support.constraint.solver.Metrics;
import android.support.constraint.solver.widgets.ConstraintWidget.DimensionBehaviour;

public class Optimizer {
    static final int FLAG_CHAIN_DANGLING = 1;
    static final int FLAG_RECOMPUTE_BOUNDS = 2;
    static final int FLAG_USE_OPTIMIZE = 0;
    public static final int OPTIMIZATION_BARRIER = 2;
    public static final int OPTIMIZATION_CHAIN = 4;
    public static final int OPTIMIZATION_DIMENSIONS = 8;
    public static final int OPTIMIZATION_DIRECT = 1;
    public static final int OPTIMIZATION_NONE = 0;
    public static final int OPTIMIZATION_RATIO = 16;
    public static final int OPTIMIZATION_STANDARD = 3;
    static boolean[] flags = new boolean[3];

    static void checkMatchParent(ConstraintWidgetContainer container, LinearSystem system, ConstraintWidget widget) {
        if (container.mListDimensionBehaviors[0] != DimensionBehaviour.WRAP_CONTENT && widget.mListDimensionBehaviors[0] == DimensionBehaviour.MATCH_PARENT) {
            int left = widget.mLeft.mMargin;
            int right = container.getWidth() - widget.mRight.mMargin;
            widget.mLeft.mSolverVariable = system.createObjectVariable(widget.mLeft);
            widget.mRight.mSolverVariable = system.createObjectVariable(widget.mRight);
            system.addEquality(widget.mLeft.mSolverVariable, left);
            system.addEquality(widget.mRight.mSolverVariable, right);
            widget.mHorizontalResolution = 2;
            widget.setHorizontalDimension(left, right);
        }
        if (container.mListDimensionBehaviors[1] != DimensionBehaviour.WRAP_CONTENT && widget.mListDimensionBehaviors[1] == DimensionBehaviour.MATCH_PARENT) {
            left = widget.mTop.mMargin;
            right = container.getHeight() - widget.mBottom.mMargin;
            widget.mTop.mSolverVariable = system.createObjectVariable(widget.mTop);
            widget.mBottom.mSolverVariable = system.createObjectVariable(widget.mBottom);
            system.addEquality(widget.mTop.mSolverVariable, left);
            system.addEquality(widget.mBottom.mSolverVariable, right);
            if (widget.mBaselineDistance > 0 || widget.getVisibility() == 8) {
                widget.mBaseline.mSolverVariable = system.createObjectVariable(widget.mBaseline);
                system.addEquality(widget.mBaseline.mSolverVariable, widget.mBaselineDistance + left);
            }
            widget.mVerticalResolution = 2;
            widget.setVerticalDimension(left, right);
        }
    }

    private static boolean optimizableMatchConstraint(ConstraintWidget constraintWidget, int orientation) {
        if (constraintWidget.mListDimensionBehaviors[orientation] != DimensionBehaviour.MATCH_CONSTRAINT) {
            return false;
        }
        int i = 1;
        if (constraintWidget.mDimensionRatio != 0.0f) {
            DimensionBehaviour[] dimensionBehaviourArr = constraintWidget.mListDimensionBehaviors;
            if (orientation != 0) {
                i = 0;
            }
            return dimensionBehaviourArr[i] == DimensionBehaviour.MATCH_CONSTRAINT ? false : false;
        } else {
            if (orientation != 0) {
                if (constraintWidget.mMatchConstraintDefaultHeight == 0 && constraintWidget.mMatchConstraintMinHeight == 0) {
                    if (constraintWidget.mMatchConstraintMaxHeight != 0) {
                    }
                }
                return false;
            } else if (constraintWidget.mMatchConstraintDefaultWidth == 0 && constraintWidget.mMatchConstraintMinWidth == 0 && constraintWidget.mMatchConstraintMaxWidth == 0) {
                return true;
            } else {
                return false;
            }
            return true;
        }
    }

    static void analyze(int optimisationLevel, ConstraintWidget widget) {
        int width;
        widget.updateResolutionNodes();
        ResolutionAnchor leftNode = widget.mLeft.getResolutionNode();
        ResolutionAnchor topNode = widget.mTop.getResolutionNode();
        ResolutionAnchor rightNode = widget.mRight.getResolutionNode();
        ResolutionAnchor bottomNode = widget.mBottom.getResolutionNode();
        boolean optimiseDimensions = (optimisationLevel & 8) == 8;
        if (!(leftNode.type == 4 || rightNode.type == 4)) {
            if (widget.mListDimensionBehaviors[0] == DimensionBehaviour.FIXED) {
                if (widget.mLeft.mTarget == null && widget.mRight.mTarget == null) {
                    leftNode.setType(1);
                    rightNode.setType(1);
                    if (optimiseDimensions) {
                        rightNode.dependsOn(leftNode, 1, widget.getResolutionWidth());
                    } else {
                        rightNode.dependsOn(leftNode, widget.getWidth());
                    }
                } else if (widget.mLeft.mTarget != null && widget.mRight.mTarget == null) {
                    leftNode.setType(1);
                    rightNode.setType(1);
                    if (optimiseDimensions) {
                        rightNode.dependsOn(leftNode, 1, widget.getResolutionWidth());
                    } else {
                        rightNode.dependsOn(leftNode, widget.getWidth());
                    }
                } else if (widget.mLeft.mTarget == null && widget.mRight.mTarget != null) {
                    leftNode.setType(1);
                    rightNode.setType(1);
                    leftNode.dependsOn(rightNode, -widget.getWidth());
                    if (optimiseDimensions) {
                        leftNode.dependsOn(rightNode, -1, widget.getResolutionWidth());
                    } else {
                        leftNode.dependsOn(rightNode, -widget.getWidth());
                    }
                } else if (!(widget.mLeft.mTarget == null || widget.mRight.mTarget == null)) {
                    leftNode.setType(2);
                    rightNode.setType(2);
                    if (optimiseDimensions) {
                        widget.getResolutionWidth().addDependent(leftNode);
                        widget.getResolutionWidth().addDependent(rightNode);
                        leftNode.setOpposite(rightNode, -1, widget.getResolutionWidth());
                        rightNode.setOpposite(leftNode, 1, widget.getResolutionWidth());
                    } else {
                        leftNode.setOpposite(rightNode, (float) (-widget.getWidth()));
                        rightNode.setOpposite(leftNode, (float) widget.getWidth());
                    }
                }
            } else if (widget.mListDimensionBehaviors[0] == DimensionBehaviour.MATCH_CONSTRAINT && optimizableMatchConstraint(widget, 0)) {
                width = widget.getWidth();
                leftNode.setType(1);
                rightNode.setType(1);
                if (widget.mLeft.mTarget == null && widget.mRight.mTarget == null) {
                    if (optimiseDimensions) {
                        rightNode.dependsOn(leftNode, 1, widget.getResolutionWidth());
                    } else {
                        rightNode.dependsOn(leftNode, width);
                    }
                } else if (widget.mLeft.mTarget == null || widget.mRight.mTarget != null) {
                    if (widget.mLeft.mTarget != null || widget.mRight.mTarget == null) {
                        if (!(widget.mLeft.mTarget == null || widget.mRight.mTarget == null)) {
                            if (optimiseDimensions) {
                                widget.getResolutionWidth().addDependent(leftNode);
                                widget.getResolutionWidth().addDependent(rightNode);
                            }
                            if (widget.mDimensionRatio == 0.0f) {
                                leftNode.setType(3);
                                rightNode.setType(3);
                                leftNode.setOpposite(rightNode, 0.0f);
                                rightNode.setOpposite(leftNode, 0.0f);
                            } else {
                                leftNode.setType(2);
                                rightNode.setType(2);
                                leftNode.setOpposite(rightNode, (float) (-width));
                                rightNode.setOpposite(leftNode, (float) width);
                                widget.setWidth(width);
                            }
                        }
                    } else if (optimiseDimensions) {
                        leftNode.dependsOn(rightNode, -1, widget.getResolutionWidth());
                    } else {
                        leftNode.dependsOn(rightNode, -width);
                    }
                } else if (optimiseDimensions) {
                    rightNode.dependsOn(leftNode, 1, widget.getResolutionWidth());
                } else {
                    rightNode.dependsOn(leftNode, width);
                }
            }
        }
        if (topNode.type != 4 && bottomNode.type != 4) {
            if (widget.mListDimensionBehaviors[1] == DimensionBehaviour.FIXED) {
                if (widget.mTop.mTarget == null && widget.mBottom.mTarget == null) {
                    topNode.setType(1);
                    bottomNode.setType(1);
                    if (optimiseDimensions) {
                        bottomNode.dependsOn(topNode, 1, widget.getResolutionHeight());
                    } else {
                        bottomNode.dependsOn(topNode, widget.getHeight());
                    }
                    if (widget.mBaseline.mTarget != null) {
                        widget.mBaseline.getResolutionNode().setType(1);
                        topNode.dependsOn(1, widget.mBaseline.getResolutionNode(), -widget.mBaselineDistance);
                    }
                } else if (widget.mTop.mTarget != null && widget.mBottom.mTarget == null) {
                    topNode.setType(1);
                    bottomNode.setType(1);
                    if (optimiseDimensions) {
                        bottomNode.dependsOn(topNode, 1, widget.getResolutionHeight());
                    } else {
                        bottomNode.dependsOn(topNode, widget.getHeight());
                    }
                    if (widget.mBaselineDistance > 0) {
                        widget.mBaseline.getResolutionNode().dependsOn(1, topNode, widget.mBaselineDistance);
                    }
                } else if (widget.mTop.mTarget == null && widget.mBottom.mTarget != null) {
                    topNode.setType(1);
                    bottomNode.setType(1);
                    if (optimiseDimensions) {
                        topNode.dependsOn(bottomNode, -1, widget.getResolutionHeight());
                    } else {
                        topNode.dependsOn(bottomNode, -widget.getHeight());
                    }
                    if (widget.mBaselineDistance > 0) {
                        widget.mBaseline.getResolutionNode().dependsOn(1, topNode, widget.mBaselineDistance);
                    }
                } else if (widget.mTop.mTarget != null && widget.mBottom.mTarget != null) {
                    topNode.setType(2);
                    bottomNode.setType(2);
                    if (optimiseDimensions) {
                        topNode.setOpposite(bottomNode, -1, widget.getResolutionHeight());
                        bottomNode.setOpposite(topNode, 1, widget.getResolutionHeight());
                        widget.getResolutionHeight().addDependent(topNode);
                        widget.getResolutionWidth().addDependent(bottomNode);
                    } else {
                        topNode.setOpposite(bottomNode, (float) (-widget.getHeight()));
                        bottomNode.setOpposite(topNode, (float) widget.getHeight());
                    }
                    if (widget.mBaselineDistance > 0) {
                        widget.mBaseline.getResolutionNode().dependsOn(1, topNode, widget.mBaselineDistance);
                    }
                }
            } else if (widget.mListDimensionBehaviors[1] == DimensionBehaviour.MATCH_CONSTRAINT && optimizableMatchConstraint(widget, 1)) {
                width = widget.getHeight();
                topNode.setType(1);
                bottomNode.setType(1);
                if (widget.mTop.mTarget == null && widget.mBottom.mTarget == null) {
                    if (optimiseDimensions) {
                        bottomNode.dependsOn(topNode, 1, widget.getResolutionHeight());
                    } else {
                        bottomNode.dependsOn(topNode, width);
                    }
                } else if (widget.mTop.mTarget == null || widget.mBottom.mTarget != null) {
                    if (widget.mTop.mTarget != null || widget.mBottom.mTarget == null) {
                        if (widget.mTop.mTarget != null && widget.mBottom.mTarget != null) {
                            if (optimiseDimensions) {
                                widget.getResolutionHeight().addDependent(topNode);
                                widget.getResolutionWidth().addDependent(bottomNode);
                            }
                            if (widget.mDimensionRatio == 0.0f) {
                                topNode.setType(3);
                                bottomNode.setType(3);
                                topNode.setOpposite(bottomNode, 0.0f);
                                bottomNode.setOpposite(topNode, 0.0f);
                                return;
                            }
                            topNode.setType(2);
                            bottomNode.setType(2);
                            topNode.setOpposite(bottomNode, (float) (-width));
                            bottomNode.setOpposite(topNode, (float) width);
                            widget.setHeight(width);
                            if (widget.mBaselineDistance > 0) {
                                widget.mBaseline.getResolutionNode().dependsOn(1, topNode, widget.mBaselineDistance);
                            }
                        }
                    } else if (optimiseDimensions) {
                        topNode.dependsOn(bottomNode, -1, widget.getResolutionHeight());
                    } else {
                        topNode.dependsOn(bottomNode, -width);
                    }
                } else if (optimiseDimensions) {
                    bottomNode.dependsOn(topNode, 1, widget.getResolutionHeight());
                } else {
                    bottomNode.dependsOn(topNode, width);
                }
            }
        }
    }

    static boolean applyChainOptimized(ConstraintWidgetContainer container, LinearSystem system, int orientation, int offset, ConstraintWidget first) {
        ConstraintWidget firstVisibleWidget;
        boolean isChainSpread;
        ConstraintWidget widget;
        boolean isChainSpreadInside;
        boolean isChainPacked;
        boolean done;
        LinearSystem linearSystem = system;
        ConstraintWidget head = first;
        ConstraintWidget widget2 = null;
        ConstraintWidget firstVisibleWidget2 = null;
        ConstraintWidget lastVisibleWidget = null;
        boolean z = false;
        ConstraintWidget firstMatchConstraintsWidget = null;
        ConstraintWidget previousMatchConstraintsWidget = null;
        boolean isWrapContent = container.mListDimensionBehaviors[orientation] == DimensionBehaviour.WRAP_CONTENT;
        ConstraintWidget head2 = first;
        if (orientation == 0 && container.isRtl()) {
            while (!z) {
                ConstraintAnchor nextAnchor = head.mListAnchors[offset + 1].mTarget;
                if (nextAnchor != null) {
                    widget2 = nextAnchor.mOwner;
                    firstVisibleWidget = firstVisibleWidget2;
                    if (widget2.mListAnchors[offset].mTarget == null || widget2.mListAnchors[offset].mTarget.mOwner != head) {
                        widget2 = null;
                    }
                } else {
                    firstVisibleWidget = firstVisibleWidget2;
                    widget2 = null;
                }
                if (widget2 != null) {
                    head = widget2;
                } else {
                    z = true;
                }
                firstVisibleWidget2 = firstVisibleWidget;
            }
            firstVisibleWidget = firstVisibleWidget2;
            head2 = head;
            head = first;
            widget2 = null;
            z = false;
        } else {
            firstVisibleWidget = null;
        }
        firstVisibleWidget2 = widget2;
        widget2 = head;
        head = head2;
        if (orientation == 0) {
            isChainSpread = head.mHorizontalChainStyle == 0;
            widget = widget2;
            isChainSpreadInside = head.mHorizontalChainStyle == 1 ? true : null;
            if (head.mHorizontalChainStyle == 2) {
                widget2 = true;
            } else {
                widget2 = null;
            }
            ConstraintWidget constraintWidget = head;
            isChainPacked = widget2;
        } else {
            widget = widget2;
            isChainSpread = head.mVerticalChainStyle == 0;
            boolean isChainSpreadInside2 = head.mVerticalChainStyle == 1;
            isChainPacked = head.mVerticalChainStyle == 2;
            isChainSpreadInside = isChainSpreadInside2;
        }
        float totalSize = 0.0f;
        float totalWeights = 0.0f;
        float totalMargins = 0.0f;
        ConstraintWidget firstVisibleWidget3 = firstVisibleWidget;
        int numVisibleWidgets = 0;
        int numMatchConstraints = 0;
        ConstraintWidget next = firstVisibleWidget2;
        firstVisibleWidget2 = widget;
        while (!z) {
            done = z;
            firstVisibleWidget2.mListNextVisibleWidget[orientation] = null;
            if (firstVisibleWidget2.getVisibility() != 8) {
                if (lastVisibleWidget != null) {
                    lastVisibleWidget.mListNextVisibleWidget[orientation] = firstVisibleWidget2;
                }
                if (firstVisibleWidget3 == null) {
                    firstVisibleWidget3 = firstVisibleWidget2;
                }
                lastVisibleWidget = firstVisibleWidget2;
                numVisibleWidgets++;
                if (orientation == 0) {
                    totalSize += (float) firstVisibleWidget2.getWidth();
                } else {
                    totalSize += (float) firstVisibleWidget2.getHeight();
                }
                if (firstVisibleWidget2 != firstVisibleWidget3) {
                    totalSize += (float) firstVisibleWidget2.mListAnchors[offset].getMargin();
                }
                totalMargins = (totalMargins + ((float) firstVisibleWidget2.mListAnchors[offset].getMargin())) + ((float) firstVisibleWidget2.mListAnchors[offset + 1].getMargin());
            }
            ConstraintAnchor begin = firstVisibleWidget2.mListAnchors[offset];
            firstVisibleWidget2.mListNextMatchConstraintsWidget[orientation] = null;
            int numVisibleWidgets2 = numVisibleWidgets;
            if (firstVisibleWidget2.getVisibility() != 8 && firstVisibleWidget2.mListDimensionBehaviors[orientation] == DimensionBehaviour.MATCH_CONSTRAINT) {
                numMatchConstraints++;
                if (orientation == 0) {
                    if (firstVisibleWidget2.mMatchConstraintDefaultWidth != 0) {
                        return false;
                    }
                    if (!(firstVisibleWidget2.mMatchConstraintMinWidth == 0 && firstVisibleWidget2.mMatchConstraintMaxWidth == 0)) {
                        return false;
                    }
                } else if (firstVisibleWidget2.mMatchConstraintDefaultHeight != 0) {
                    return false;
                } else {
                    if (firstVisibleWidget2.mMatchConstraintMinHeight == 0) {
                        if (firstVisibleWidget2.mMatchConstraintMaxHeight != 0) {
                        }
                    }
                    return false;
                }
                totalWeights += firstVisibleWidget2.mWeight[orientation];
                if (firstMatchConstraintsWidget == null) {
                    firstMatchConstraintsWidget = firstVisibleWidget2;
                } else {
                    previousMatchConstraintsWidget.mListNextMatchConstraintsWidget[orientation] = firstVisibleWidget2;
                }
                previousMatchConstraintsWidget = firstVisibleWidget2;
            }
            ConstraintAnchor nextAnchor2 = firstVisibleWidget2.mListAnchors[offset + 1].mTarget;
            if (nextAnchor2 != null) {
                next = nextAnchor2.mOwner;
                if (next.mListAnchors[offset].mTarget == null || next.mListAnchors[offset].mTarget.mOwner != firstVisibleWidget2) {
                    widget2 = null;
                }
                if (next == null) {
                    firstVisibleWidget2 = next;
                    z = done;
                } else {
                    z = true;
                }
                numVisibleWidgets = numVisibleWidgets2;
            } else {
                widget2 = null;
            }
            next = widget2;
            if (next == null) {
                z = true;
            } else {
                firstVisibleWidget2 = next;
                z = done;
            }
            numVisibleWidgets = numVisibleWidgets2;
        }
        done = z;
        ConstraintWidget constraintWidget2 = next;
        ConstraintWidget last = firstVisibleWidget2;
        ResolutionAnchor firstNode = first.mListAnchors[offset].getResolutionNode();
        ResolutionAnchor lastNode = last.mListAnchors[offset + 1].getResolutionNode();
        ConstraintWidget constraintWidget3;
        ConstraintWidget constraintWidget4;
        ResolutionAnchor resolutionAnchor;
        boolean z2;
        boolean z3;
        int i;
        if (firstNode.target == null) {
            constraintWidget3 = firstVisibleWidget2;
            constraintWidget4 = firstVisibleWidget3;
            resolutionAnchor = lastNode;
            z2 = isWrapContent;
            z3 = isChainSpread;
            i = numMatchConstraints;
        } else if (lastNode.target == null) {
            r38 = isChainPacked;
            constraintWidget3 = firstVisibleWidget2;
            constraintWidget4 = firstVisibleWidget3;
            resolutionAnchor = lastNode;
            z2 = isWrapContent;
            z3 = isChainSpread;
            i = numMatchConstraints;
            firstVisibleWidget2 = numVisibleWidgets;
        } else if (firstNode.target.state != 1 && lastNode.target.state != 1) {
            return false;
        } else {
            if (numMatchConstraints > 0 && numMatchConstraints != numVisibleWidgets) {
                return false;
            }
            float distance;
            float extraMargin = 0.0f;
            if (isChainPacked || isChainSpread || isChainSpreadInside) {
                if (firstVisibleWidget3 != null) {
                    extraMargin = (float) firstVisibleWidget3.mListAnchors[offset].getMargin();
                }
                if (lastVisibleWidget != null) {
                    extraMargin += (float) lastVisibleWidget.mListAnchors[offset + 1].getMargin();
                }
            }
            float firstOffset = firstNode.target.resolvedOffset;
            float lastOffset = lastNode.target.resolvedOffset;
            if (firstOffset < lastOffset) {
                distance = (lastOffset - firstOffset) - totalSize;
            } else {
                distance = (firstOffset - lastOffset) - totalSize;
            }
            int numVisibleWidgets3;
            ConstraintWidget firstVisibleWidget4;
            float f;
            if (numMatchConstraints <= 0 || numMatchConstraints != numVisibleWidgets) {
                r38 = isChainPacked;
                numVisibleWidgets3 = numVisibleWidgets;
                constraintWidget3 = firstVisibleWidget2;
                firstVisibleWidget4 = firstVisibleWidget3;
                resolutionAnchor = lastNode;
                f = lastOffset;
                z3 = isChainSpread;
                if (distance < totalSize) {
                    return false;
                }
                float distance2;
                float f2;
                if (r38) {
                    firstVisibleWidget2 = firstVisibleWidget4;
                    distance = (first.getHorizontalBiasPercent() * (distance - extraMargin)) + firstOffset;
                    next = constraintWidget2;
                    while (firstVisibleWidget2 != null) {
                        if (LinearSystem.sMetrics) {
                            isChainPacked = LinearSystem.sMetrics;
                            isChainPacked.nonresolvedWidgets -= 1;
                            isChainPacked = LinearSystem.sMetrics;
                            isChainPacked.resolvedWidgets++;
                            isChainPacked = LinearSystem.sMetrics;
                            isChainPacked.chainConnectionResolved++;
                        }
                        next = firstVisibleWidget2.mListNextVisibleWidget[orientation];
                        if (next != null || firstVisibleWidget2 == lastVisibleWidget) {
                            if (orientation == 0) {
                                isChainPacked = (float) firstVisibleWidget2.getWidth();
                            } else {
                                isChainPacked = (float) firstVisibleWidget2.getHeight();
                            }
                            distance2 = distance + ((float) firstVisibleWidget2.mListAnchors[offset].getMargin());
                            firstVisibleWidget2.mListAnchors[offset].getResolutionNode().resolve(firstNode.resolvedTarget, distance2);
                            firstVisibleWidget2.mListAnchors[offset + 1].getResolutionNode().resolve(firstNode.resolvedTarget, distance2 + isChainPacked);
                            firstVisibleWidget2.mListAnchors[offset].getResolutionNode().addResolvedValue(linearSystem);
                            firstVisibleWidget2.mListAnchors[offset + 1].getResolutionNode().addResolvedValue(linearSystem);
                            distance = (distance2 + isChainPacked) + ((float) firstVisibleWidget2.mListAnchors[offset + 1].getMargin());
                        }
                        firstVisibleWidget2 = next;
                    }
                    isChainPacked = firstVisibleWidget2;
                    f2 = extraMargin;
                    i = numMatchConstraints;
                    constraintWidget4 = firstVisibleWidget4;
                    firstVisibleWidget2 = numVisibleWidgets3;
                    constraintWidget2 = next;
                } else {
                    if (!z3) {
                        if (!isChainSpreadInside) {
                            f2 = extraMargin;
                            i = numMatchConstraints;
                            isChainPacked = constraintWidget3;
                            constraintWidget4 = firstVisibleWidget4;
                            int i2 = numVisibleWidgets3;
                        }
                    }
                    if (z3) {
                        distance -= extraMargin;
                    } else if (isChainSpreadInside) {
                        distance -= extraMargin;
                    }
                    isChainPacked = firstVisibleWidget4;
                    distance2 = distance / ((float) (numVisibleWidgets3 + 1));
                    if (isChainSpreadInside) {
                        firstVisibleWidget2 = numVisibleWidgets3;
                        if (firstVisibleWidget2 > 1) {
                            distance2 = distance / ((float) (firstVisibleWidget2 - 1));
                        } else {
                            distance2 = distance / 2.0f;
                        }
                    } else {
                        firstVisibleWidget2 = numVisibleWidgets3;
                    }
                    next = firstOffset + distance2;
                    if (!isChainSpreadInside || widget <= 1) {
                        firstVisibleWidget3 = firstVisibleWidget4;
                    } else {
                        firstVisibleWidget3 = firstVisibleWidget4;
                        next = firstOffset + ((float) firstVisibleWidget3.mListAnchors[offset].getMargin());
                    }
                    if (z3 && firstVisibleWidget3 != null) {
                        next += (float) firstVisibleWidget3.mListAnchors[offset].getMargin();
                    }
                    lastNode = next;
                    next = constraintWidget2;
                    while (isChainPacked) {
                        if (LinearSystem.sMetrics != null) {
                            Metrics metrics = LinearSystem.sMetrics;
                            f2 = extraMargin;
                            i = numMatchConstraints;
                            metrics.nonresolvedWidgets--;
                            extraMargin = LinearSystem.sMetrics;
                            constraintWidget4 = firstVisibleWidget3;
                            extraMargin.resolvedWidgets++;
                            extraMargin = LinearSystem.sMetrics;
                            extraMargin.chainConnectionResolved++;
                        } else {
                            f2 = extraMargin;
                            ConstraintWidget constraintWidget5 = next;
                            constraintWidget4 = firstVisibleWidget3;
                            i = numMatchConstraints;
                        }
                        next = isChainPacked.mListNextVisibleWidget[orientation];
                        if (next != null || isChainPacked == lastVisibleWidget) {
                            if (orientation == 0) {
                                extraMargin = (float) isChainPacked.getWidth();
                            } else {
                                extraMargin = (float) isChainPacked.getHeight();
                            }
                            isChainPacked.mListAnchors[offset].getResolutionNode().resolve(firstNode.resolvedTarget, lastNode);
                            isChainPacked.mListAnchors[offset + 1].getResolutionNode().resolve(firstNode.resolvedTarget, lastNode + extraMargin);
                            isChainPacked.mListAnchors[offset].getResolutionNode().addResolvedValue(linearSystem);
                            isChainPacked.mListAnchors[offset + 1].getResolutionNode().addResolvedValue(linearSystem);
                            lastNode += extraMargin + distance2;
                        }
                        isChainPacked = next;
                        extraMargin = f2;
                        numMatchConstraints = i;
                        firstVisibleWidget3 = constraintWidget4;
                    }
                    constraintWidget4 = firstVisibleWidget3;
                    i = numMatchConstraints;
                    ResolutionAnchor resolutionAnchor2 = lastNode;
                    constraintWidget2 = next;
                }
                return true;
            }
            Object obj;
            if (firstVisibleWidget2.getParent() != null) {
                if (firstVisibleWidget2.getParent().mListDimensionBehaviors[orientation] == DimensionBehaviour.WRAP_CONTENT) {
                    return false;
                }
            }
            resolutionAnchor = lastNode;
            distance = (distance + totalSize) - totalMargins;
            firstVisibleWidget2 = firstVisibleWidget3;
            float position = firstOffset;
            if (isChainSpread) {
                distance -= totalMargins - extraMargin;
            }
            if (isChainSpread) {
                position += (float) firstVisibleWidget2.mListAnchors[offset + 1].getMargin();
                lastOffset = firstVisibleWidget2.mListNextVisibleWidget[orientation];
                if (lastOffset != null) {
                    position += (float) lastOffset.mListAnchors[offset].getMargin();
                    firstVisibleWidget2 = firstVisibleWidget2;
                }
            } else {
                f = lastOffset;
                lastOffset = constraintWidget2;
            }
            while (firstVisibleWidget2 != null) {
                if (LinearSystem.sMetrics != null) {
                    ConstraintWidget next2 = lastOffset;
                    lastOffset = LinearSystem.sMetrics;
                    firstVisibleWidget4 = firstVisibleWidget3;
                    r38 = isChainPacked;
                    numVisibleWidgets3 = numVisibleWidgets;
                    lastOffset.nonresolvedWidgets -= true;
                    isChainPacked = LinearSystem.sMetrics;
                    z3 = isChainSpread;
                    isChainPacked.resolvedWidgets += Float.MIN_VALUE;
                    isChainPacked = LinearSystem.sMetrics;
                    isChainPacked.chainConnectionResolved += Float.MIN_VALUE;
                } else {
                    r38 = isChainPacked;
                    numVisibleWidgets3 = numVisibleWidgets;
                    firstVisibleWidget4 = firstVisibleWidget3;
                    obj = lastOffset;
                    z3 = isChainSpread;
                }
                lastOffset = firstVisibleWidget2.mListNextVisibleWidget[orientation];
                if (lastOffset != null || firstVisibleWidget2 == lastVisibleWidget) {
                    isChainPacked = distance / ((float) numMatchConstraints);
                    if (totalWeights > 0.0f) {
                        isChainPacked = (firstVisibleWidget2.mWeight[orientation] * distance) / totalWeights;
                    }
                    position += (float) firstVisibleWidget2.mListAnchors[offset].getMargin();
                    firstVisibleWidget2.mListAnchors[offset].getResolutionNode().resolve(firstNode.resolvedTarget, position);
                    firstVisibleWidget2.mListAnchors[offset + 1].getResolutionNode().resolve(firstNode.resolvedTarget, position + isChainPacked);
                    firstVisibleWidget2.mListAnchors[offset].getResolutionNode().addResolvedValue(linearSystem);
                    firstVisibleWidget2.mListAnchors[offset + 1].getResolutionNode().addResolvedValue(linearSystem);
                    position = (position + isChainPacked) + ((float) firstVisibleWidget2.mListAnchors[offset + 1].getMargin());
                }
                firstVisibleWidget2 = lastOffset;
                firstVisibleWidget3 = firstVisibleWidget4;
                isChainPacked = r38;
                numVisibleWidgets = numVisibleWidgets3;
                isChainSpread = z3;
                next = first;
            }
            numVisibleWidgets3 = numVisibleWidgets;
            firstVisibleWidget4 = firstVisibleWidget3;
            obj = lastOffset;
            z3 = isChainSpread;
            return true;
        }
        return false;
    }
}
