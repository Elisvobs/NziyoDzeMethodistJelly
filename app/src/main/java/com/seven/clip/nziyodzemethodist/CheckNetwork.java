package com.seven.clip.nziyodzemethodist;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

/**
 * Created by bennysway on 21.02.17.
 */

public class CheckNetwork {


    private static final String TAG = CheckNetwork.class.getSimpleName();



    public static boolean isInternetAvailable(Context context)
    {
        NetworkInfo info = ((ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();

        if (info == null)
        {
            Log.d(TAG,"no internet connection");
            return false;
        }
        else
        {
            if(info.isConnected())
            {
                Log.d(TAG," internet connection available...");
                return true;
            }
            else
            {
                Log.d(TAG," internet connection");
                return false;
            }

        }
    }
}