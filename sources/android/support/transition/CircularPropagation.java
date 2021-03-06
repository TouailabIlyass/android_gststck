package android.support.transition;

import android.graphics.Rect;
import android.view.ViewGroup;

public class CircularPropagation extends VisibilityPropagation {
    private float mPropagationSpeed = 3.0f;

    public void setPropagationSpeed(float propagationSpeed) {
        if (propagationSpeed == 0.0f) {
            throw new IllegalArgumentException("propagationSpeed may not be 0");
        }
        this.mPropagationSpeed = propagationSpeed;
    }

    public long getStartDelay(ViewGroup sceneRoot, Transition transition, TransitionValues startValues, TransitionValues endValues) {
        CircularPropagation circularPropagation = this;
        TransitionValues transitionValues = startValues;
        if (transitionValues == null && endValues == null) {
            return 0;
        }
        TransitionValues positionValues;
        int viewCenterX;
        int viewCenterY;
        Rect epicenter;
        int epicenterX;
        int epicenterY;
        float distanceFraction;
        long duration;
        int directionMultiplier = 1;
        if (endValues != null) {
            if (getViewVisibility(transitionValues) != 0) {
                positionValues = endValues;
                viewCenterX = getViewX(positionValues);
                viewCenterY = getViewY(positionValues);
                epicenter = transition.getEpicenter();
                if (epicenter == null) {
                    epicenterX = epicenter.centerX();
                    epicenterY = epicenter.centerY();
                    ViewGroup viewGroup = sceneRoot;
                } else {
                    int[] loc = new int[2];
                    sceneRoot.getLocationOnScreen(loc);
                    int epicenterX2 = Math.round(((float) (loc[0] + (sceneRoot.getWidth() / 2))) + sceneRoot.getTranslationX());
                    epicenterY = Math.round(((float) (loc[1] + (sceneRoot.getHeight() / 2))) + sceneRoot.getTranslationY());
                    epicenterX = epicenterX2;
                }
                distanceFraction = distance((float) viewCenterX, (float) viewCenterY, (float) epicenterX, (float) epicenterY) / distance(0.0f, 0.0f, (float) sceneRoot.getWidth(), (float) sceneRoot.getHeight());
                duration = transition.getDuration();
                if (duration < 0) {
                    duration = 300;
                }
                return (long) Math.round((((float) (((long) directionMultiplier) * duration)) / circularPropagation.mPropagationSpeed) * distanceFraction);
            }
        }
        positionValues = transitionValues;
        directionMultiplier = -1;
        viewCenterX = getViewX(positionValues);
        viewCenterY = getViewY(positionValues);
        epicenter = transition.getEpicenter();
        if (epicenter == null) {
            int[] loc2 = new int[2];
            sceneRoot.getLocationOnScreen(loc2);
            int epicenterX22 = Math.round(((float) (loc2[0] + (sceneRoot.getWidth() / 2))) + sceneRoot.getTranslationX());
            epicenterY = Math.round(((float) (loc2[1] + (sceneRoot.getHeight() / 2))) + sceneRoot.getTranslationY());
            epicenterX = epicenterX22;
        } else {
            epicenterX = epicenter.centerX();
            epicenterY = epicenter.centerY();
            ViewGroup viewGroup2 = sceneRoot;
        }
        distanceFraction = distance((float) viewCenterX, (float) viewCenterY, (float) epicenterX, (float) epicenterY) / distance(0.0f, 0.0f, (float) sceneRoot.getWidth(), (float) sceneRoot.getHeight());
        duration = transition.getDuration();
        if (duration < 0) {
            duration = 300;
        }
        return (long) Math.round((((float) (((long) directionMultiplier) * duration)) / circularPropagation.mPropagationSpeed) * distanceFraction);
    }

    private static float distance(float x1, float y1, float x2, float y2) {
        float x = x2 - x1;
        float y = y2 - y1;
        return (float) Math.sqrt((double) ((x * x) + (y * y)));
    }
}
