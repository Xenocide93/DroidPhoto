package com.droidsans.photo.droidphoto.util;

import android.os.Bundle;

import com.google.firebase.analytics.FirebaseAnalytics;

/**
 * Created by Xenocide93 on 6/27/16.
 */

public class FBAnalytic {

    private static final String TYPE_SCREEN = "screen";
    private static final String TYPE_EVENT = "event";
    private static final String TYPE_ACTION = "action";

    // common
    public static final String ACTION_BACK = "click-back";

    public static final String EVENT_EXIT_APP = "exit-app";


    // login & register
    public static final String SCREEN_SPLASH = "splash-login";
    public static final String SCREEN_REGISTER = "register";

    public static final String ACTION_LOGIN = "click-login";
    public static final String ACTION_REGISTER = "click-register";

    public static final String EVENT_AUTO_LOGIN = "auto-login";
    public static final String EVENT_MANUAL_LOGIN = "manual-login";
    public static final String EVENT_REGISTER = "register";
}
