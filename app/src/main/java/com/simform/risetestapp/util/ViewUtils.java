package com.simform.risetestapp.util;

import android.app.Activity;
import android.view.View;

public class ViewUtils {

    @SuppressWarnings("unchecked")
    public static <T extends View> T find(Activity activity, int id)
    {
        return (T) activity.findViewById(id);
    }
}
