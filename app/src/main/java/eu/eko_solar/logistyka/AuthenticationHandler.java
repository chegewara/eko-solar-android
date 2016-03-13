package eu.eko_solar.logistyka;

import android.content.Context;
import android.util.Log;

import io.socketcluster.socketclusterandroidclient.SCSocketService;

/**
 * Created by Dariusz Krempa on 2016-03-03.
 */
public class AuthenticationHandler extends MyBroadcastReceiver {
    private static final String TAG = "Authenticate";

    public AuthenticationHandler(Context context) {
        super(context);
    }

    @Override
    protected void handleEvents(String event, String data) {
        switch (event){
            case SCSocketService.EVENT_ON_AUTHENTICATE:
                Log.d(TAG, "authenticated: " + data);
                break;

            case SCSocketService.EVENT_ON_AUTHENTICATE_STATE_CHANGE:
                Log.d(TAG, "authChanged: "+data);
                break;

            case SCSocketService.EVENT_ON_DEAUTHENTICATE:
                Log.d(TAG, "deauthenticated: "+data);
                break;
        }
    }
}
