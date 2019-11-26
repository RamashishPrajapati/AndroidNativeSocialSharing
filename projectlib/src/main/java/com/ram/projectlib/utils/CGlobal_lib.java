package com.ram.projectlib.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import androidx.core.content.FileProvider;

/**
 * Created by Ramashish Prajapati on 19-02-2019
 **/
public class CGlobal_lib {
    private static CGlobal_lib instance;
    private Context context;
    public static String msVersion;
    Uri bmpUri;
    private static String authority = null;
    SharedPreferences.Editor mEditorVersionPersistent = null;
    private SharedPreferences mPrefsVersionPersistent = null;


    public CGlobal_lib(Context context) {
        try {
            this.context = context;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static synchronized CGlobal_lib getInstance(Context context) {
        if (instance == null) {
            instance = new CGlobal_lib(context);
        }
        return instance;
    }



    public SharedPreferences.Editor getPersistentPreferenceEditor(Context context) {
        if (mEditorVersionPersistent == null) {
            mEditorVersionPersistent = getPersistentPreference(context).edit();
        }
        return mEditorVersionPersistent;
    }

    public SharedPreferences getPersistentPreference(Context context) {
        if (mPrefsVersionPersistent == null) {
            mPrefsVersionPersistent = context.getApplicationContext()
                    .getSharedPreferences(
                            CGlobal_Constant.SHARED_PREFERENCE_PERSISTENT,
                            Context.MODE_PRIVATE);
        }
        return mPrefsVersionPersistent;
    }


    /*Check app is install our not in our phone*/
    public boolean appInstalledOrNot(String uri) {
        PackageManager pm = context.getPackageManager();
        boolean app_installed = false;
        try {
            pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
            app_installed = true;
        } catch (PackageManager.NameNotFoundException e) {
            app_installed = false;
        }
        return app_installed;
    }

    public void showMessage(String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

    /*Geting the app authority if FileProvider is not in image path*/
    public String GetAuthority(Context context) {
        if (authority == null) {
            try {
                PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_PROVIDERS);
                ProviderInfo[] providers = packageInfo.providers;
                if (providers != null) {
                    for (ProviderInfo provider : providers) {
                        if (provider.name.equals(FileProvider.class.getName()) && provider.packageName.equals(context.getPackageName())
                                && provider.authority.length() > 0) {
                            authority = provider.authority;
                            break;
                        }
                    }
                }
            } catch (Exception e) {
                Log.e("Unity", "Exception:", e);
            }
        }
        return authority;
    }




}
