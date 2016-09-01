package com.chinedusokafor.silverbirdmoviis.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by cokafor on 1/25/2015.
 */
public class SilverbirdmoviisAuthenticatorService extends Service {

    // Instance field that stores the authenticator object
    private SilverbirdmoviisAuthenticator mAuthenticator;

    @Override
    public void onCreate() {
        // Create a new authenticator object
        mAuthenticator = new SilverbirdmoviisAuthenticator(this);
    }

    /*
     * When the system binds to this Service to make the RPC call
     * return the authenticator's IBinder.
     */
    @Override
    public IBinder onBind(Intent intent) {
        return mAuthenticator.getIBinder();
    }
}

