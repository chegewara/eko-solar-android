package socketcluster.io.androiddemo;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.fangjian.WebViewJavascriptBridge;

import org.json.simple.JSONValue;

import java.util.HashMap;
import java.util.Map;

import socketcluster.io.socketclusterandroidclient.ISocketCluster;
import socketcluster.io.socketclusterandroidclient.SCSocketService;


public class StartupActivity extends AppCompatActivity {

    private static String TAG = "SCDemo";
    private SCSocketService sc;
    private String options = null;
    private boolean bound;
    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_startup);

        // Connect button
        final Intent mainActivity = new Intent(this, MainActivity.class);
        Button button = (Button) findViewById(R.id.button2);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(mainActivity);
                sc.emitEvent("login", "driverId");
            }
        });
        Map map = new HashMap();
        String host = "ns1.diskstation.eu";
        String port = "3010";

        map.put("hostname", host);
        map.put("port", port);
        options = JSONValue.toJSONString(map);

    }

    private ServiceConnection conn = new ServiceConnection(){

        @Override
        public void onServiceConnected(ComponentName component, IBinder binder){
            SCSocketService.SCSocketBinder scSocketBinder = (SCSocketService.SCSocketBinder) binder;
            sc = scSocketBinder.getBinder();
            sc.setDelegate(new SCSocketHandler(), StartupActivity.this);
            bound = true;
            sc.connect(options);
        }

        @Override
        public void onServiceDisconnected(ComponentName component){
            bound = false;
        }
    };

    @Override
    protected void onStart(){
        super.onStart();
        intent = new Intent(this, SCSocketService.class);
        bindService(intent, conn, Context.BIND_AUTO_CREATE);
        startService(intent);
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
        sc.disconnect();
//        stopService(intent);
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

    public class SCSocketHandler implements ISocketCluster {
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
            Log.i(TAG, "socketClusterOnKickOut from channel: ");
        }

        @Override
        public void socketClusterOnSubscribe() {
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
        }

        @Override
        public void socketClusterOnDeauthenticate() {
        }

        @Override
        public void socketClusterOnGetState(String state) {
        }

        @Override
        public void socketClusterOnSubscribeStateChange(String state) {
        }

        @Override
        public void socketClusterOnAuthStateChange(String state) {
        }
    }

}