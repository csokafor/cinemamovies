package com.chinedusokafor.silverbirdmoviis.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

/**
 * Created by cokafor on 1/25/2015.
 */
public class SilverbirdmoviisSyncService extends Service {

    private static final Object sSyncAdapterLock = new Object();
    private static SilverbirdmoviisSyncAdapter silverbirdmoviisSyncAdapter = null;

    @Override
    public void onCreate() {
        Log.d("SilverbirdmoviisSyncService", "onCreate - SilverbirdmoviisSyncService");
        synchronized (sSyncAdapterLock) {
            if (silverbirdmoviisSyncAdapter == null) {
                silverbirdmoviisSyncAdapter = new SilverbirdmoviisSyncAdapter(getApplicationContext(), true);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return silverbirdmoviisSyncAdapter.getSyncAdapterBinder();
    }
}
