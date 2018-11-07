package android.support.constraint.solver.widgets;

import android.support.constraint.solver.LinearSystem;
import android.support.constraint.solver.SolverVariable;
import android.support.constraint.solver.widgets.ConstraintWidget.DimensionBehaviour;

class Chain {
    private static final boolean DEBUG = false;

    Chain() {
    }

    static void applyChainConstraints(ConstraintWidgetContainer constraintWidgetContainer, LinearSystem system, int orientation) {
        int offset;
        int chainsSize;
        ConstraintWidget[] chainsArray;
        if (orientation == 0) {
            offset = 0;
            chainsSize = constraintWidgetContainer.mHorizontalChainsSize;
            chainsArray = constraintWidgetContainer.mHorizontalChainsArray;
        } else {
            offset = 2;
            chainsSize = constraintWidgetContainer.mVerticalChainsSize;
            chainsArray = constraintWidgetContainer.mVerticalChainsArray;
        }
        for (int i = 0; i < chainsSize; i++) {
            ConstraintWidget first = chainsArray[i];
            if (!constraintWidgetContainer.optimizeFor(4)) {
                applyChainConstraints(constraintWidgetContainer, system, orientation, offset, first);
            } else if (!Optimizer.applyChainOptimized(constraintWidgetContainer, system, orientation, offset, first)) {
                applyChainConstraints(constraintWidgetContainer, system, orientation, offset, first);
            }
        }
    }

    static void applyChainConstraints(ConstraintWidgetContainer container, LinearSystem system, int orientation, int offset, ConstraintWidget first) {
        ConstraintAnchor nextAnchor;
        ConstraintWidget firstVisibleWidget;
        ConstraintWidget constraintWidget;
        boolean done;
        boolean isChainSpreadInside;
        boolean isChainPacked;
        boolean isChainSpread;
        ConstraintWidget lastVisibleWidget;
        float totalWeights;
        ConstraintWidget firstMatchConstraintsWidget;
        ConstraintWidget previousMatchConstraintsWidget;
        ConstraintWidget firstVisibleWidget2;
        ConstraintWidget previousMatchConstraintsWidget2;
        SolverVariable begin;
        ConstraintWidget constraintWidget2;
        ConstraintWidget constraintWidget3;
        ConstraintWidget last;
        ConstraintWidget firstVisibleWidget3;
        SolverVariable endTarget;
        ConstraintAnchor end;
        ConstraintWidgetContainer constraintWidgetContainer = container;
        LinearSystem linearSystem = system;
        ConstraintWidget constraintWidget4 = first;
        ConstraintWidget constraintWidget5 = null;
        ConstraintWidget firstVisibleWidget4 = null;
        ConstraintWidget head = null;
        int numMatchConstraints = constraintWidget4;
        boolean isWrapContent = constraintWidgetContainer.mListDimensionBehaviors[orientation] == DimensionBehaviour.WRAP_CONTENT;
        ConstraintWidget head2 = constraintWidget4;
        if (orientation == 0 && container.isRtl()) {
            ConstraintWidget constraintWidget6 = numMatchConstraints;
            ConstraintWidget next = null;
            constraintWidget5 = constraintWidget6;
            while (head == null) {
                nextAnchor = constraintWidget5.mListAnchors[offset + 1].mTarget;
                if (nextAnchor != null) {
                    firstVisibleWidget = firstVisibleWidget4;
                    firstVisibleWidget4 = nextAnchor.mOwner;
                    if (firstVisibleWidget4.mListAnchors[offset].mTarget != null) {
                        if (firstVisibleWidget4.mListAnchors[offset].mTarget.mOwner == constraintWidget5) {
                            next = firstVisibleWidget4;
                            if (next == null) {
                                constraintWidget5 = next;
                            } else {
                                head = true;
                            }
                            firstVisibleWidget4 = firstVisibleWidget;
                        }
                    }
                    constraintWidget = null;
                } else {
                    firstVisibleWidget = firstVisibleWidget4;
                    constraintWidget = null;
                }
                next = constraintWidget;
                if (next == null) {
                    head = true;
                } else {
                    constraintWidget5 = next;
                }
                firstVisibleWidget4 = firstVisibleWidget;
            }
            firstVisibleWidget = firstVisibleWidget4;
            head2 = constraintWidget5;
            constraintWidget5 = null;
            head = null;
            numMatchConstraints = constraintWidget4;
        } else {
            firstVisibleWidget = null;
        }
        boolean done2 = head;
        head = head2;
        boolean isChainSpread2;
        if (orientation == 0) {
            isChainSpread2 = head.mHorizontalChainStyle == 0;
            done = done2;
            isChainSpreadInside = head.mHorizontalChainStyle;
            if (head.mHorizontalChainStyle) {
                done2 = true;
            } else {
                done2 = false;
            }
            isChainPacked = done2;
            isChainSpread = isChainSpread2;
            lastVisibleWidget = null;
            totalWeights = 0.0f;
            firstMatchConstraintsWidget = null;
            previousMatchConstraintsWidget = null;
            constraintWidget = numMatchConstraints;
            firstVisibleWidget2 = firstVisibleWidget;
        } else {
            done = done2;
            isChainSpread2 = head.mVerticalChainStyle == 0;
            isChainPacked = head.mVerticalChainStyle;
            isChainSpread = isChainSpread2;
            lastVisibleWidget = null;
            totalWeights = 0.0f;
            firstMatchConstraintsWidget = null;
            previousMatchConstraintsWidget = null;
            constraintWidget = numMatchConstraints;
            firstVisibleWidget2 = firstVisibleWidget;
            isChainSpreadInside = head.mVerticalChainStyle == 1;
        }
        numMatchConstraints = 0;
        while (!done) {
            ConstraintWidget firstVisibleWidget5;
            constraintWidget.mListNextVisibleWidget[orientation] = null;
            if (constraintWidget.getVisibility() != 8) {
                if (lastVisibleWidget != null) {
                    lastVisibleWidget.mListNextVisibleWidget[orientation] = constraintWidget;
                }
                if (firstVisibleWidget2 == null) {
                    firstVisibleWidget2 = constraintWidget;
                }
                lastVisibleWidget = constraintWidget;
            }
            ConstraintAnchor begin2 = constraintWidget.mListAnchors[offset];
            int strength = 1;
            int margin = begin2.getMargin();
            if (!(begin2.mTarget == null || constraintWidget == constraintWidget4 || constraintWidget.getVisibility() == 8)) {
                margin += begin2.mTarget.getMargin();
            }
            int margin2 = margin;
            if (!(!isChainPacked || constraintWidget == constraintWidget4 || constraintWidget == firstVisibleWidget2)) {
                strength = 6;
            }
            int strength2 = strength;
            if (constraintWidget == firstVisibleWidget2) {
                firstVisibleWidget5 = firstVisibleWidget2;
                linearSystem.addGreaterThan(begin2.mSolverVariable, begin2.mTarget.mSolverVariable, margin2, 5);
            } else {
                firstVisibleWidget5 = firstVisibleWidget2;
                linearSystem.addGreaterThan(begin2.mSolverVariable, begin2.mTarget.mSolverVariable, margin2, 6);
            }
            linearSystem.addEquality(begin2.mSolverVariable, begin2.mTarget.mSolverVariable, margin2, strength2);
            constraintWidget.mListNextMatchConstraintsWidget[orientation] = null;
            if (constraintWidget.getVisibility() != 8 && constraintWidget.mListDimensionBehaviors[orientation] == DimensionBehaviour.MATCH_CONSTRAINT) {
                ConstraintWidget previousMatchConstraintsWidget3;
                numMatchConstraints++;
                totalWeights += constraintWidget.mWeight[orientation];
                if (firstMatchConstraintsWidget == null) {
                    firstMatchConstraintsWidget = constraintWidget;
                } else {
                    previousMatchConstraintsWidget.mListNextMatchConstraintsWidget[orientation] = constraintWidget;
                }
                constraintWidget5 = constraintWidget;
                if (isWrapContent) {
                    previousMatchConstraintsWidget3 = constraintWidget5;
                    linearSystem.addGreaterThan(constraintWidget.mListAnchors[offset + 1].mSolverVariable, constraintWidget.mListAnchors[offset].mSolverVariable, 0, 6);
                } else {
                    previousMatchConstraintsWidget3 = constraintWidget5;
                }
                previousMatchConstraintsWidget = previousMatchConstraintsWidget3;
            }
            if (isWrapContent) {
                int i = margin2;
                linearSystem.addGreaterThan(constraintWidget.mListAnchors[offset].mSolverVariable, constraintWidgetContainer.mListAnchors[offset].mSolverVariable, 0, 6);
            }
            constraintWidget5 = constraintWidget.mListAnchors[offset + 1].mTarget;
            if (constraintWidget5 != null) {
                firstVisibleWidget4 = constraintWidget5.mOwner;
                if (firstVisibleWidget4.mListAnchors[offset].mTarget == null || firstVisibleWidget4.mListAnchors[offset].mTarget.mOwner != constraintWidget) {
                    firstVisibleWidget4 = null;
                }
            } else {
                firstVisibleWidget4 = null;
            }
            if (firstVisibleWidget4 != null) {
                constraintWidget = firstVisibleWidget4;
            } else {
                done = true;
            }
            constraintWidget5 = firstVisibleWidget4;
            firstVisibleWidget2 = firstVisibleWidget5;
        }
        ConstraintWidget next2 = constraintWidget5;
        ConstraintWidget last2 = constraintWidget;
        if (lastVisibleWidget == null || last2.mListAnchors[offset + 1].mTarget == null) {
            previousMatchConstraintsWidget2 = previousMatchConstraintsWidget;
        } else {
            ConstraintAnchor end2 = lastVisibleWidget.mListAnchors[offset + 1];
            previousMatchConstraintsWidget2 = previousMatchConstraintsWidget;
            linearSystem.addLowerThan(end2.mSolverVariable, last2.mListAnchors[offset + 1].mTarget.mSolverVariable, -end2.getMargin(), 5);
        }
        if (isWrapContent) {
            linearSystem.addGreaterThan(constraintWidgetContainer.mListAnchors[offset + 1].mSolverVariable, last2.mListAnchors[offset + 1].mSolverVariable, last2.mListAnchors[offset + 1].getMargin(), 6);
        }
        boolean isWrapContent2;
        if (numMatchConstraints > 0) {
            constraintWidget = firstMatchConstraintsWidget;
            constraintWidget5 = next2;
            while (constraintWidget != null) {
                constraintWidget5 = constraintWidget.mListNextMatchConstraintsWidget[orientation];
                if (constraintWidget5 != null) {
                    boolean applyEquality;
                    int nextDimensionDefault;
                    float currentWeight = constraintWidget.mWeight[orientation];
                    float nextWeight = constraintWidget5.mWeight[orientation];
                    begin = constraintWidget.mListAnchors[offset].mSolverVariable;
                    SolverVariable end3 = constraintWidget.mListAnchors[offset + 1].mSolverVariable;
                    SolverVariable nextBegin = constraintWidget5.mListAnchors[offset].mSolverVariable;
                    isWrapContent2 = isWrapContent;
                    isWrapContent = constraintWidget5.mListAnchors[offset + 1].mSolverVariable;
                    if (orientation == 0) {
                        constraintWidget2 = head;
                        int currentDimensionDefault = constraintWidget.mMatchConstraintDefaultWidth;
                        constraintWidget3 = constraintWidget;
                        constraintWidget = constraintWidget5.mMatchConstraintDefaultWidth;
                        head = currentDimensionDefault;
                    } else {
                        constraintWidget2 = head;
                        head = constraintWidget.mMatchConstraintDefaultHeight;
                        constraintWidget = constraintWidget5.mMatchConstraintDefaultHeight;
                    }
                    if (head == null || head == 3) {
                        if (constraintWidget != null) {
                            if (constraintWidget == 3) {
                            }
                        }
                        applyEquality = true;
                        if (applyEquality) {
                            nextDimensionDefault = constraintWidget;
                            constraintWidget = system.createRow();
                            constraintWidget.createRowEqualMatchDimensions(currentWeight, totalWeights, nextWeight, begin, end3, nextBegin, isWrapContent);
                            linearSystem.addConstraint(constraintWidget);
                        }
                    }
                    applyEquality = false;
                    if (applyEquality) {
                        nextDimensionDefault = constraintWidget;
                        constraintWidget = system.createRow();
                        constraintWidget.createRowEqualMatchDimensions(currentWeight, totalWeights, nextWeight, begin, end3, nextBegin, isWrapContent);
                        linearSystem.addConstraint(constraintWidget);
                    }
                } else {
                    constraintWidget2 = head;
                    isWrapContent2 = isWrapContent;
                }
                constraintWidget = constraintWidget5;
                isWrapContent = isWrapContent2;
                head = constraintWidget2;
                constraintWidgetContainer = container;
            }
            constraintWidget3 = constraintWidget;
            constraintWidget2 = head;
            isWrapContent2 = isWrapContent;
            next2 = constraintWidget5;
        } else {
            constraintWidget2 = head;
            isWrapContent2 = isWrapContent;
            constraintWidget3 = constraintWidget;
        }
        ConstraintWidget constraintWidget7;
        ConstraintWidget constraintWidget8;
        ConstraintAnchor end4;
        ConstraintAnchor begin3;
        if (firstVisibleWidget2 != null) {
            if (firstVisibleWidget2 != lastVisibleWidget) {
                if (!isChainPacked) {
                    last = last2;
                    firstVisibleWidget3 = firstVisibleWidget2;
                    constraintWidget7 = previousMatchConstraintsWidget2;
                    constraintWidget8 = constraintWidget2;
                }
            }
            ConstraintAnchor begin4 = constraintWidget4.mListAnchors[offset];
            nextAnchor = last2.mListAnchors[offset + 1];
            SolverVariable beginTarget = constraintWidget4.mListAnchors[offset].mTarget != null ? constraintWidget4.mListAnchors[offset].mTarget.mSolverVariable : null;
            endTarget = last2.mListAnchors[offset + 1].mTarget != null ? last2.mListAnchors[offset + 1].mTarget.mSolverVariable : null;
            if (firstVisibleWidget2 == lastVisibleWidget) {
                begin4 = firstVisibleWidget2.mListAnchors[offset];
                nextAnchor = firstVisibleWidget2.mListAnchors[offset + 1];
            }
            end = nextAnchor;
            if (beginTarget == null || endTarget == null) {
                last = last2;
                firstVisibleWidget3 = firstVisibleWidget2;
                constraintWidget7 = previousMatchConstraintsWidget2;
                constraintWidget8 = constraintWidget2;
            } else {
                float bias;
                if (orientation == 0) {
                    head = constraintWidget2;
                    bias = head.mHorizontalBiasPercent;
                } else {
                    head = constraintWidget2;
                    bias = head.mVerticalBiasPercent;
                }
                float bias2 = bias;
                margin = begin4.getMargin();
                if (lastVisibleWidget == null) {
                    lastVisibleWidget = last2;
                }
                last = last2;
                firstVisibleWidget3 = firstVisibleWidget2;
                linearSystem.addCentering(begin4.mSolverVariable, beginTarget, margin, bias2, endTarget, end.mSolverVariable, lastVisibleWidget.mListAnchors[offset + 1].getMargin(), 5);
            }
            ConstraintWidget constraintWidget9 = constraintWidget3;
            firstVisibleWidget = last;
            if ((!isChainSpread || isChainSpreadInside) && firstVisibleWidget3 != null) {
                nextAnchor = firstVisibleWidget3.mListAnchors[offset];
                end4 = lastVisibleWidget.mListAnchors[offset + 1];
                endTarget = nextAnchor.mTarget != null ? nextAnchor.mTarget.mSolverVariable : null;
                SolverVariable endTarget2 = end4.mTarget != null ? end4.mTarget.mSolverVariable : null;
                if (firstVisibleWidget3 == lastVisibleWidget) {
                    nextAnchor = firstVisibleWidget3.mListAnchors[offset];
                    end4 = firstVisibleWidget3.mListAnchors[offset + 1];
                }
                begin3 = nextAnchor;
                end = end4;
                if (endTarget != null && endTarget2 != null) {
                    int beginMargin = begin3.getMargin();
                    if (lastVisibleWidget == null) {
                        lastVisibleWidget = firstVisibleWidget;
                    }
                    linearSystem.addCentering(begin3.mSolverVariable, endTarget, beginMargin, 0.5f, endTarget2, end.mSolverVariable, lastVisibleWidget.mListAnchors[offset + 1].getMargin(), 5);
                    return;
                }
            }
            return;
        }
        last = last2;
        firstVisibleWidget3 = firstVisibleWidget2;
        constraintWidget7 = previousMatchConstraintsWidget2;
        constraintWidget8 = constraintWidget2;
        ConstraintWidget last3;
        ConstraintWidget previousVisibleWidget;
        SolverVariable begin5;
        SolverVariable beginNext;
        int beginMargin2;
        int nextMargin;
        SolverVariable beginNextTarget;
        ConstraintWidget last4;
        if (!isChainSpread || firstVisibleWidget3 == null) {
            last3 = last;
            if (!isChainSpreadInside || firstVisibleWidget3 == null) {
                firstVisibleWidget = last3;
                constraintWidget9 = constraintWidget3;
                if (isChainSpread) {
                }
                nextAnchor = firstVisibleWidget3.mListAnchors[offset];
                end4 = lastVisibleWidget.mListAnchors[offset + 1];
                if (nextAnchor.mTarget != null) {
                }
                endTarget = nextAnchor.mTarget != null ? nextAnchor.mTarget.mSolverVariable : null;
                if (end4.mTarget != null) {
                }
                SolverVariable endTarget22 = end4.mTarget != null ? end4.mTarget.mSolverVariable : null;
                if (firstVisibleWidget3 == lastVisibleWidget) {
                    nextAnchor = firstVisibleWidget3.mListAnchors[offset];
                    end4 = firstVisibleWidget3.mListAnchors[offset + 1];
                }
                begin3 = nextAnchor;
                end = end4;
                if (endTarget != null) {
                }
            }
            ConstraintAnchor endTarget3;
            ConstraintAnchor end5;
            ConstraintAnchor constraintAnchor;
            ConstraintAnchor constraintAnchor2;
            constraintWidget = firstVisibleWidget3;
            constraintWidget9 = constraintWidget;
            while (true) {
                previousVisibleWidget = constraintWidget;
                if (constraintWidget9 == null) {
                    break;
                }
                constraintWidget = constraintWidget9.mListNextVisibleWidget[orientation];
                if (constraintWidget9 == firstVisibleWidget3 || constraintWidget9 == lastVisibleWidget || constraintWidget == null) {
                    next2 = constraintWidget;
                } else {
                    SolverVariable beginNext2;
                    ConstraintAnchor beginNextAnchor;
                    ConstraintWidget next3;
                    if (constraintWidget == lastVisibleWidget) {
                        constraintWidget = null;
                    }
                    firstVisibleWidget2 = constraintWidget;
                    end = constraintWidget9.mListAnchors[offset];
                    begin5 = end.mSolverVariable;
                    if (end.mTarget != null) {
                        constraintWidget = end.mTarget.mSolverVariable;
                    } else {
                        constraintWidget = null;
                    }
                    begin = previousVisibleWidget.mListAnchors[offset + 1].mSolverVariable;
                    beginNext = null;
                    beginMargin2 = end.getMargin();
                    nextMargin = constraintWidget9.mListAnchors[offset + 1].getMargin();
                    if (firstVisibleWidget2 != null) {
                        ConstraintAnchor beginNextAnchor2 = null;
                        constraintWidget = firstVisibleWidget2.mListAnchors[offset];
                        beginNext2 = constraintWidget.mSolverVariable;
                        end2 = constraintWidget;
                        beginNextTarget = constraintWidget.mTarget != null ? constraintWidget.mTarget.mSolverVariable : null;
                    } else {
                        ConstraintWidget constraintWidget10 = null;
                        constraintWidget = constraintWidget9.mListAnchors[offset + 1].mTarget;
                        if (constraintWidget != null) {
                            beginNext = constraintWidget.mSolverVariable;
                        }
                        beginNextAnchor = constraintWidget;
                        beginNextTarget = constraintWidget9.mListAnchors[offset + 1].mSolverVariable;
                        beginNext2 = beginNext;
                        end2 = beginNextAnchor;
                    }
                    if (end2 != null) {
                        nextMargin += end2.getMargin();
                    }
                    margin = nextMargin;
                    if (previousVisibleWidget != null) {
                        beginMargin2 += previousVisibleWidget.mListAnchors[offset + 1].getMargin();
                    }
                    beginMargin = beginMargin2;
                    if (begin5 == null || begin == null || beginNext2 == null || beginNextTarget == null) {
                        next3 = firstVisibleWidget2;
                    } else {
                        beginNextAnchor = end2;
                        next3 = firstVisibleWidget2;
                        linearSystem.addCentering(begin5, begin, beginMargin, 0.5f, beginNext2, beginNextTarget, margin, 4);
                    }
                    next2 = next3;
                }
                constraintWidget = constraintWidget9;
                constraintWidget9 = next2;
            }
            begin3 = firstVisibleWidget3.mListAnchors[offset];
            end = constraintWidget4.mListAnchors[offset].mTarget;
            ConstraintAnchor end6 = lastVisibleWidget.mListAnchors[offset + 1];
            last4 = last3;
            ConstraintAnchor endTarget4 = last4.mListAnchors[offset + 1].mTarget;
            if (end != null) {
                if (firstVisibleWidget3 != lastVisibleWidget) {
                    linearSystem.addEquality(begin3.mSolverVariable, end.mSolverVariable, begin3.getMargin(), 5);
                    endTarget3 = endTarget4;
                    firstVisibleWidget = last4;
                    end5 = end6;
                    constraintAnchor = end;
                    constraintAnchor2 = begin3;
                } else if (endTarget4 != null) {
                    beginNext = begin3.mSolverVariable;
                    SolverVariable solverVariable = end.mSolverVariable;
                    strength = begin3.getMargin();
                    SolverVariable solverVariable2 = end6.mSolverVariable;
                    ConstraintAnchor beginTarget2 = end;
                    end = endTarget4.mSolverVariable;
                    beginMargin = end6.getMargin();
                    endTarget3 = endTarget4;
                    firstVisibleWidget = last4;
                    end5 = end6;
                    end6 = end;
                    end = beginMargin;
                    linearSystem.addCentering(beginNext, solverVariable, strength, 0.5f, solverVariable2, end6, end, 5);
                }
                nextAnchor = endTarget3;
                if (!(nextAnchor == null || firstVisibleWidget3 == lastVisibleWidget)) {
                    end4 = end5;
                    linearSystem.addEquality(end4.mSolverVariable, nextAnchor.mSolverVariable, -end4.getMargin(), 5);
                }
                if (isChainSpread) {
                }
                nextAnchor = firstVisibleWidget3.mListAnchors[offset];
                end4 = lastVisibleWidget.mListAnchors[offset + 1];
                if (nextAnchor.mTarget != null) {
                }
                endTarget = nextAnchor.mTarget != null ? nextAnchor.mTarget.mSolverVariable : null;
                if (end4.mTarget != null) {
                }
                SolverVariable endTarget222 = end4.mTarget != null ? end4.mTarget.mSolverVariable : null;
                if (firstVisibleWidget3 == lastVisibleWidget) {
                    nextAnchor = firstVisibleWidget3.mListAnchors[offset];
                    end4 = firstVisibleWidget3.mListAnchors[offset + 1];
                }
                begin3 = nextAnchor;
                end = end4;
                if (endTarget != null) {
                }
            }
            endTarget3 = endTarget4;
            firstVisibleWidget = last4;
            end5 = end6;
            constraintAnchor = end;
            constraintAnchor2 = begin3;
            nextAnchor = endTarget3;
            end4 = end5;
            linearSystem.addEquality(end4.mSolverVariable, nextAnchor.mSolverVariable, -end4.getMargin(), 5);
            if (isChainSpread) {
            }
            nextAnchor = firstVisibleWidget3.mListAnchors[offset];
            end4 = lastVisibleWidget.mListAnchors[offset + 1];
            if (nextAnchor.mTarget != null) {
            }
            endTarget = nextAnchor.mTarget != null ? nextAnchor.mTarget.mSolverVariable : null;
            if (end4.mTarget != null) {
            }
            SolverVariable endTarget2222 = end4.mTarget != null ? end4.mTarget.mSolverVariable : null;
            if (firstVisibleWidget3 == lastVisibleWidget) {
                nextAnchor = firstVisibleWidget3.mListAnchors[offset];
                end4 = firstVisibleWidget3.mListAnchors[offset + 1];
            }
            begin3 = nextAnchor;
            end = end4;
            if (endTarget != null) {
            }
        }
        constraintWidget = firstVisibleWidget3;
        constraintWidget9 = constraintWidget;
        while (true) {
            previousVisibleWidget = constraintWidget;
            if (constraintWidget9 == null) {
                break;
            }
            SolverVariable beginNext3;
            SolverVariable beginNextTarget2;
            firstVisibleWidget2 = constraintWidget9.mListNextVisibleWidget[orientation];
            if (firstVisibleWidget2 == null) {
                if (constraintWidget9 != lastVisibleWidget) {
                    ConstraintWidget next4 = firstVisibleWidget2;
                    last3 = last;
                    constraintWidget = constraintWidget9;
                    constraintWidget9 = next4;
                    next2 = next4;
                    last = last3;
                }
            }
            end = constraintWidget9.mListAnchors[offset];
            begin5 = end.mSolverVariable;
            solverVariable2 = end.mTarget != null ? end.mTarget.mSolverVariable : null;
            if (previousVisibleWidget != constraintWidget9) {
                solverVariable2 = previousVisibleWidget.mListAnchors[offset + 1].mSolverVariable;
            } else if (constraintWidget9 == firstVisibleWidget3 && previousVisibleWidget == constraintWidget9) {
                solverVariable2 = constraintWidget4.mListAnchors[offset].mTarget != null ? constraintWidget4.mListAnchors[offset].mTarget.mSolverVariable : null;
            }
            beginNextTarget = solverVariable2;
            beginNext = null;
            beginMargin2 = end.getMargin();
            nextMargin = constraintWidget9.mListAnchors[offset + 1].getMargin();
            if (firstVisibleWidget2 != null) {
                nextAnchor = firstVisibleWidget2.mListAnchors[offset];
                beginNext3 = nextAnchor.mSolverVariable;
                beginNextTarget2 = nextAnchor.mTarget != null ? nextAnchor.mTarget.mSolverVariable : null;
                last4 = last;
                end2 = nextAnchor;
            } else {
                ConstraintAnchor beginNextAnchor3 = null;
                last4 = last;
                nextAnchor = last4.mListAnchors[offset + 1].mTarget;
                if (nextAnchor != null) {
                    beginNext = nextAnchor.mSolverVariable;
                }
                ConstraintAnchor beginNextAnchor4 = nextAnchor;
                beginNextTarget2 = constraintWidget9.mListAnchors[offset + 1].mSolverVariable;
                beginNext3 = beginNext;
                end2 = beginNextAnchor4;
            }
            if (end2 != null) {
                nextMargin += end2.getMargin();
            }
            int nextMargin2 = nextMargin;
            if (previousVisibleWidget != null) {
                beginMargin2 += previousVisibleWidget.mListAnchors[offset + 1].getMargin();
            }
            int beginMargin3 = beginMargin2;
            if (begin5 == null || beginNextTarget == null || beginNext3 == null || beginNextTarget2 == null) {
                last3 = last4;
                next4 = firstVisibleWidget2;
                constraintWidget = constraintWidget9;
                constraintWidget9 = next4;
                next2 = next4;
                last = last3;
            } else {
                int margin1 = beginMargin3;
                if (constraintWidget9 == firstVisibleWidget3) {
                    margin1 = firstVisibleWidget3.mListAnchors[offset].getMargin();
                }
                int margin12 = margin1;
                margin1 = nextMargin2;
                if (constraintWidget9 == lastVisibleWidget) {
                    margin1 = lastVisibleWidget.mListAnchors[offset + 1].getMargin();
                }
                beginNextAnchor4 = end2;
                last3 = last4;
                next4 = firstVisibleWidget2;
                linearSystem.addCentering(begin5, beginNextTarget, margin12, 0.5f, beginNext3, beginNextTarget2, margin1, 4);
                constraintWidget = constraintWidget9;
                constraintWidget9 = next4;
                next2 = next4;
                last = last3;
            }
        }
        firstVisibleWidget = last;
        if (isChainSpread) {
        }
        nextAnchor = firstVisibleWidget3.mListAnchors[offset];
        end4 = lastVisibleWidget.mListAnchors[offset + 1];
        if (nextAnchor.mTarget != null) {
        }
        endTarget = nextAnchor.mTarget != null ? nextAnchor.mTarget.mSolverVariable : null;
        if (end4.mTarget != null) {
        }
        SolverVariable endTarget22222 = end4.mTarget != null ? end4.mTarget.mSolverVariable : null;
        if (firstVisibleWidget3 == lastVisibleWidget) {
            nextAnchor = firstVisibleWidget3.mListAnchors[offset];
            end4 = firstVisibleWidget3.mListAnchors[offset + 1];
        }
        begin3 = nextAnchor;
        end = end4;
        if (endTarget != null) {
        }
    }
}
