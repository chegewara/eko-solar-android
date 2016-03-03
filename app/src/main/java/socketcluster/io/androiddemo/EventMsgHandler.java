package socketcluster.io.androiddemo;

import android.util.Log;

import io.socketcluster.socketclusterandroidclient.SCSocketService;

/**
 * Created by Dariusz Krempa on 2016-03-03.
 */
public class EventMsgHandler extends MyBroadcastReceiver {

    @Override
    protected void handleEvents(String event, String data) {
        switch(event){

            case SCSocketService.EVENT_ON_EVENT_MESSAGE:
                Log.d("", "onEvent: "+data);
                break;

        }

    }
}
