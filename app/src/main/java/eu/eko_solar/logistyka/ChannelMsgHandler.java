package eu.eko_solar.logistyka;

import android.content.Context;
import android.net.Uri;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.JSONValue;

import io.socketcluster.socketclusterandroidclient.SCSocketService;

import static eu.eko_solar.logistyka.MainActivity.notifications;
import static eu.eko_solar.logistyka.MainActivity.vibrate;

/**
 * Created by Dariusz Krempa on 2016-03-03.
 */
public class ChannelMsgHandler extends MyBroadcastReceiver {
    public ChannelMsgHandler(Context context) {
        super(context);
    }

    @Override
    protected void handleEvents(String event, String data) {
        switch(event){
            case SCSocketService.EVENT_ON_SUBSCRIBED_MESSAGE:
                JSONObject jsonObject = new JSONObject();
                String channel = "";
                String json = "";
                String jobID = "";
                String driverID = "";
                try {
                    jsonObject = new JSONObject(data);
                    channel = jsonObject.getString("channel");
                    String dataMsg = jsonObject.getString("data");
                    json = (String) JSONValue.parse(dataMsg);
                    jsonObject = new JSONObject(json);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                switch (channel){
                    case "newJob":
                        try {
                            jobID = jsonObject.getString("_id");
                            driverID = jsonObject.getString("driver");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        break;

                    case "updateJob":
                        try {
                            jobID = jsonObject.getString("_id");
                            driverID = jsonObject.getString("driver");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        JobContent.removeItem(jobID);
                        if(driverID.equals(MainActivity.driverID)) {
                            JobContent.Job job = new JobContent.Job(json);
                            JobContent.addItem(job);
                            if(notifications){
                                if(vibrate)
                                    mBuilder.setVibrate(new long[]{1500, 1500, 1500});

                                mBuilder.setSound(Uri.parse(MainActivity.ringtone));
                                inboxStyle = new NotificationCompat.InboxStyle();

                                String[] events = new String[6];
                                events[0] = "Adres: " +job.address;
                                events[1] = "Kontakt: "+job.contact;
                                events[2] = "Rodzaj: "+job.job_type;
                                events[3] = "Rozmiar: "+job.size;
                                events[4] = "Gotówka: "+job.cash;
                                events[5] = "Uwagi: "+job.info;

                                // Sets a title for the Inbox style big view
                                inboxStyle.setBigContentTitle("Nowe zlecenie:");

                                // Moves events into the big view
                                for (int i=0; i < events.length; i++) {
                                    inboxStyle.addLine(events[i]);
                                }

                                mBuilder.setStyle(inboxStyle);
                                mBuilder.setContentTitle("Nowe zlecenie.");
                                notification();
                            }
                            Log.d("ChannelMsgHandler", "updateJob: " + jsonObject.toString());
                        }
                        ((MyMsgSocket) context).newJobMsg();
                        break;
                    case "deleteJob":
                        try {
                            jobID = jsonObject.getString("_id");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        if(notifications) {
                            if(vibrate)
                                mBuilder.setVibrate(new long[]{1500, 1500, 1500});

                            mBuilder.setSound(Uri.parse(MainActivity.ringtone));
                            mBuilder.setContentTitle("Usunięto zlecenie.");
                            notification();
                        }
                        JobContent.removeItem(jobID);
                        ((MyMsgSocket) context).newJobMsg();
                        break;
                }
                break;
        }
    }
}
