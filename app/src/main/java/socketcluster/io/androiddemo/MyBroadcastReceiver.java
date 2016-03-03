package socketcluster.io.androiddemo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import io.socketcluster.socketclusterandroidclient.SCSocketService;

/**
 * Created by Dariusz Krempa on 2016-03-03.
 */
abstract public class MyBroadcastReceiver extends BroadcastReceiver{
    private SCSocketService socket;

    @Override
    public void onReceive(Context context, Intent intent) {
        String event = intent.getStringExtra("event");
        String data = intent.getStringExtra("data");
        handleEvents(event,data);
    }

    protected abstract void handleEvents(String event, String data);

    public void setService(SCSocketService socket){
        this.socket = socket;
    }

    public interface MyMsgSocket{
        public SCSocketService setService();
    }
}
