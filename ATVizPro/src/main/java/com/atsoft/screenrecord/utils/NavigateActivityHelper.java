package com.atsoft.screenrecord.utils;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.atsoft.screenrecord.Constants;
import com.atsoft.screenrecord.ui.activities.MainActivity;

public class NavigateActivityHelper {
    private static final String TAG = NavigateActivityHelper.class.getSimpleName();

    public static void navigateToActionView(Activity activity, Uri uri) {
        try {
            if (uri != null) {
                Intent webViewIntent = new Intent(Intent.ACTION_VIEW, uri);
                activity.startActivity(webViewIntent);
            } else {
//                activity.showToast(R.string.e601_error_but_undefined);
            }
        } catch (Exception e) {
//            Log.e(TAG, "Exception", e);
//            activity.showToast(R.string.e667_device_not_support_function);
        }
    }

    public static void navigateToPlayStore(Activity activity, String packageName) {
        if (activity == null || TextUtils.isDigitsOnly(packageName)) return;
        try {
            activity.startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse(Constants.LINK_MARKET + packageName)));
        } catch (android.content.ActivityNotFoundException anfe) {
            Log.e(TAG, "ActivityNotFoundException", anfe);
            try {
                activity.startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse(Constants.LINK_GOOGLE_PLAY + packageName)));
            } catch (Exception e) {
//                Log.e(TAG, "Exception", e);
//                activity.showToast(R.string.e666_not_support_function);
            }
        } catch (Exception e) {
//            Log.e(TAG, "Exception", e);
//            activity.showToast(R.string.e666_not_support_function);
        }
    }


    public static void navigateToHomeScreenActivity(Activity activity, boolean fromHome, boolean fromLogin) {
        Intent intent = new Intent(activity, MainActivity.class);
        intent.putExtra("FROM_LOGIN", fromLogin);
        if (fromHome) {
//            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
//            activity.startActivity(intent, false);
        } else {
//            if (activity instanceof LoginActivity) {
//                intent.putExtra(MoviePagerFragment.FROM_LOGIN, true);
//            } else {
//                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//            }
//            activity.startActivity(intent, false);
//            activity.finish();
        }
    }
}
