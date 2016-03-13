package eu.eko_solar.logistyka;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.NotificationCompat;

import io.socketcluster.socketclusterandroidclient.SCSocketService;

/**
 * Created by Dariusz Krempa on 2016-03-03.
 */
abstract public class MyBroadcastReceiver extends BroadcastReceiver{
    protected static SCSocketService socket;
    protected Context context;
    NotificationCompat.Builder mBuilder;
    final int NOTIFICATION_ID = 1;
    NotificationManager mNotificationManager;
    NotificationCompat.InboxStyle inboxStyle;
    NotificationCompat.Builder builder;
    PendingIntent contentIntent;

    public MyBroadcastReceiver(Context context){
        this.context = context;
        inboxStyle = new NotificationCompat.InboxStyle();

        mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Intent myApp = new Intent(context.getApplicationContext(), context.getClass());
        myApp.setAction(Intent.ACTION_MAIN);
        myApp.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        contentIntent = PendingIntent.getActivity(context.getApplicationContext(), 0, myApp, 0);

        mBuilder = (NotificationCompat.Builder) new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.truck)
                .setContentTitle("SCSocketCluster")
                .setAutoCancel(true);

    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String event = intent.getStringExtra("event");
        String data = intent.getStringExtra("data");
        handleEvents(event,data);
    }

    protected abstract void handleEvents(String event, String data);

    public static void setService(SCSocketService socket){
        MyBroadcastReceiver.socket = socket;
    }

    public interface MyMsgSocket{
        void newJobMsg();
        void newEvent();
    }

    protected void notification(){
        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }
}
