package socketcluster.io.androiddemo;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.JSONValue;

import java.util.HashMap;
import java.util.Map;

import socketcluster.io.socketclusterandroidclient.ISocketCluster;
import socketcluster.io.socketclusterandroidclient.SCSocketService;

  
public class MainActivity extends Activity implements ISocketCluster {

    private static String TAG = "SCDemo";
    private String authToken = null;
    private SCSocketService sc;
    private Boolean bound = false;
    private TextView subState;
    private TextView connState;
    private TextView authState;
    private String options;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(savedInstanceState != null){
            this.authToken = savedInstanceState.getString("authToken");
            options = savedInstanceState.getString("options");
        }

        // Connect button
        final Button connectBtn = (Button) findViewById(R.id.btnConnect);
        final CheckBox checkBox = (CheckBox) findViewById(R.id.checkBox);

        connectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Map map = new HashMap();
                String host = ((EditText)findViewById(R.id.textHost)).getText().toString();
                String port = ((EditText)findViewById(R.id.textPort)).getText().toString();
                Boolean isHttps;

                isHttps = checkBox.isChecked();
                map.put("hostname", host);
                map.put("secure", isHttps);
                map.put("port", port);
                map.put("rejectUnauthorized", false);
                options = JSONValue.toJSONString(map);
                sc.connect(options);
                sc.getState();
            }
        });
        // Disconnect button
        final Button disconnectBtn = (Button) findViewById(R.id.btnDisconnect);
        disconnectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sc.disconnect();
            }
        });
        // Listen to Rand event button handler
        final Button listenToRandBtn = (Button) findViewById(R.id.btnListenRand);
        listenToRandBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sc.emitEvent("login", "test");
            }
        });

        final Button subToWeatherBtn = (Button) findViewById(R.id.btnSubWeather);
        subToWeatherBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                String channel = ((EditText) findViewById(R.id.channel)).getText().toString();
                sc.subscribeToChannel(channel);
            }
        });
        final Button unSubToWeatherBtn = (Button) findViewById(R.id.btnUnSubWeather);
        unSubToWeatherBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                String channel = ((EditText) findViewById(R.id.channel)).getText().toString();
                sc.unsubscribeFromChannel(channel);
            }
        });

        final Button pubToWeatherBtn = (Button) findViewById(R.id.btnPubWeather);
        pubToWeatherBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                String channel = ((EditText) findViewById(R.id.channel)).getText().toString();
                sc.publishToChannel(channel, "CLOUDY");
            }
        });

        authState = (TextView) findViewById(R.id.State1);
        connState = (TextView) findViewById(R.id.State2);
        subState = (TextView) findViewById(R.id.State);

    }

    private ServiceConnection conn = new ServiceConnection(){

    		@Override
    		public void onServiceConnected(ComponentName component, IBinder binder){
    			SCSocketService.SCSocketBinder scSocketBinder = (SCSocketService.SCSocketBinder) binder;
    			sc = scSocketBinder.getBinder();
                sc.setDelegate(MainActivity.this);
                bound = true;
    		}

    		@Override
    		public void onServiceDisconnected(ComponentName component){
    			bound = false;
    		}
    };

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState){
        savedInstanceState.putString("authToken", authToken);
        savedInstanceState.putString("options", options);
    }

    @Override
    protected void onStart(){
   		super.onStart();
   		Intent intent = new Intent(this, SCSocketService.class);
   		bindService(intent, conn, Context.BIND_AUTO_CREATE);
//        startService(intent);
    }

    @Override
    protected void onStop(){
   		super.onStop();
        if(bound){
            unbindService(conn);
            bound = false;
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
    }

    @Override
    protected void onPause(){
        super.onPause();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void socketClusterReceivedEvent(String name, String data) {
        Log.i(TAG, "ReceivedEvent " + name);
        Log.i(TAG, "ReceivedEvent " + data);
    }

    @Override
    public void socketClusterChannelReceivedEvent(String name, String data) {
        Log.i(TAG, "socketClusterChannelReceivedEvent " + name + " data: " + data);
    }

    @Override
    public void socketClusterDidConnect(String data) {
        String id = "";
        try {
            JSONObject obj = new JSONObject(data).getJSONObject("data");
            id = obj.getString("id");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.i(TAG, "SocketClusterDidConnect");
    }

    @Override
    public void socketClusterDidDisconnect() {
        Log.i(TAG, "socketClusterDidDisconnect");
    }

    @Override
    public void socketClusterOnError(String error) {
        Log.i(TAG, "socketClusterOnError");
    }

    @Override
    public void socketClusterOnKickOut(String data) {
        String channel = "";
        try {
            channel = new JSONObject(data).getString("channel");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.i(TAG, "socketClusterOnKickOut");
    }

    @Override
    public void socketClusterOnSubscribe() {
        String data = sc.subscriptions(true);
        subState.setText(data);
        Log.i(TAG, "socketClusterOnSubscribe");
    }

    @Override
    public void socketClusterOnSubscribeFail(String err) {
        Log.i(TAG, "socketClusterOnSubscribeFail");
    }
    @Override
    public void socketClusterOnUnsubscribe() {
        Log.i(TAG, "socketClusterOnUnsubscribe");
    }

    @Override
    public void socketClusterOnAuthenticate(String data) {
        authToken = data;
    }

    @Override
    public void socketClusterOnDeauthenticate() {
    }

    @Override
    public void socketClusterOnGetState(String state) {
    }

	@Override
	public void socketClusterOnSubscribeStateChange(String state) {
        String data = sc.subscriptions(true);
        subState.setText(data);
	}

	@Override
	public void socketClusterOnAuthStateChange(String state) {
        authState.setText(state);
	}

}