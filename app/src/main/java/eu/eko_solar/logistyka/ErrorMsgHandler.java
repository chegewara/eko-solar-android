package eu.eko_solar.logistyka;

import android.content.Context;

import io.socketcluster.socketclusterandroidclient.SCSocketService;

/**
 * Created by Dariusz Krempa on 2016-03-03.
 */
public class ErrorMsgHandler extends MyBroadcastReceiver {
    public ErrorMsgHandler(Context context) {
        super(context);
    }

    @Override
    protected void handleEvents(String event, String data) {
        switch (event){
            case SCSocketService.EVENT_ON_ERROR:
                switch (event){
                    default:
                        break;
                }
                break;
        }
    }
}
