package eu.eko_solar.logistyka;

import android.content.Context;
import android.net.Uri;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.JSONValue;

import io.socketcluster.socketclusterandroidclient.SCSocketService;

import static eu.eko_solar.logistyka.MainActivity.*;

/**
 * Created by Dariusz Krempa on 2016-03-03.
 */
public class EventMsgHandler extends MyBroadcastReceiver {

    public EventMsgHandler(Context context) {
        super(context);
    }

    @Override
    protected void handleEvents(String event, String data) {
        switch(event) {
            case SCSocketService.EVENT_ON_EVENT_MESSAGE:
                JSONObject jsonObject;
                String channel = "";
                String txt = driverID;
                //TODO switch case to handle different event channels
                try {
                    jsonObject = new JSONObject(data);
                    channel = jsonObject.getString("event");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                switch (channel) {
                    case "jobs":
                        try {
                            String json = "";
                            jsonObject = new JSONObject(data);
                            String dataMsg = jsonObject.getString("data");
                            json = (String) JSONValue.parse(dataMsg);
                            JSONArray array = new JSONArray(json);
                            for (int i = 0; i < array.length(); i++) {
                                JobContent.addItem(new JobContent.Job(array.getString(i)));
                            }
                            ((MyMsgSocket) context).newJobMsg();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        if(notifications && JobContent.jobsList.size()>0) {
                            if(vibrate)
                                mBuilder.setVibrate(new long[]{1500, 1500, 1500});

                            mBuilder.setSound(Uri.parse(ringtone));
                            mBuilder.setContentTitle("Dostępne zlecenia.");
                            notification();
                        }
                        break;
                }
                break;
        }
    }
}
