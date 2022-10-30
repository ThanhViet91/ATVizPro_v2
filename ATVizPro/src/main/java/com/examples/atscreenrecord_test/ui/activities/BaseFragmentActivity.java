package com.examples.atscreenrecord_test.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

public class BaseFragmentActivity extends AppCompatActivity {
    private FragmentManager mFragmentManager;
    private final String TAG = BaseFragmentActivity.class.getSimpleName();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        mFragmentManager = getSupportFragmentManager();
        super.onCreate(savedInstanceState);
//        setTransitionOnCreate();
    }

    @Override
    public void finish() {
        super.finish();
//        setTransitionFinish();
    }

    public void goToHome() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        clearBackStack();
        finish();
    }

    protected void clearBackStack() {
        try {
            for (int i = 0; i < mFragmentManager.getBackStackEntryCount(); ++i) {
                mFragmentManager.popBackStack();
            }
        } catch (Exception e) {
            Log.e(TAG, String.valueOf(e));
        }
    }
}
