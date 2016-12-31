package com.kksionek.queuedroid.view;

import android.os.Build;
import android.transition.TransitionManager;
import android.view.ViewGroup;

public class AnimationUtils {

    private AnimationUtils(){}

    public static void beginDelayedTransition(ViewGroup viewGroup) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            TransitionManager.beginDelayedTransition(viewGroup);
    }
}
