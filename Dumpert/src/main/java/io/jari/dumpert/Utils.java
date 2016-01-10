package io.jari.dumpert;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * JARI.IO
 * Date: 15-12-14
 * Time: 21:20
 */
public class Utils {

    static boolean forceOffline = false;
    public static boolean isOffline(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return !Utils.forceOffline &&
                ((activeNetwork == null) || !activeNetwork.isConnectedOrConnecting());
    }
}
