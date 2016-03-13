package eu.eko_solar.logistyka;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.json.simple.JSONValue;

import java.util.HashMap;
import java.util.Map;

import io.socketcluster.socketclusterandroidclient.SCSocketService;


public class MainActivity extends Activity implements LocationListener,
        JobsListFragment.OnJobListListener,
        MyBroadcastReceiver.MyMsgSocket {

    private Intent scSocketService;
    private static String TAG = "SCDemo";
    private SCSocketService scSocket;
    private Boolean bound = false;
    private LocationManager locationManager;
    private Intent chatheadService;
    private String options;
    private EventMsgHandler eventsMsgHandler;
    private ChannelMsgHandler channelMsgHandler;
    private ErrorMsgHandler errorMsgHandler;
    public static String driverID;
    private FragmentTransaction transaction;
    private boolean active;
    private AuthenticationHandler authenticateMsgHandler;
    public static boolean notifications;
    public static boolean vibrate;
    public static String ringtone;
    public static double lng;
    public static double lat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        active = true;
        driverID = getIntent().getStringExtra("driverID");
        setContentView(R.layout.activity_main);
        chatheadService = new Intent(getApplicationContext(), ChatHeadService.class);
        startService(chatheadService);

        eventsMsgHandler = new EventMsgHandler(this);
        channelMsgHandler = new ChannelMsgHandler(this);
        errorMsgHandler = new ErrorMsgHandler(this);
        authenticateMsgHandler = new AuthenticationHandler(this);

        Map map = new HashMap();

        SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

        String host = SP.getString("server_address", "");
        String port = SP.getString("server_port", "");
        notifications = SP.getBoolean("notifications_new_message", false);
        vibrate = SP.getBoolean("notifications_new_message_vibrate", false);
        ringtone = SP.getString("notifications_new_message_ringtone", "");
        map.put("hostname", host);
        map.put("port", port);
        options = JSONValue.toJSONString(map);

        Button button = (Button) findViewById(R.id.btnSubWeather);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scSocket.subscribe("driverJob");
                scSocket.registerEvent(driverID);

            }
        });
        Button button1 = (Button) findViewById(R.id.btnDisconnect);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                transaction = getFragmentManager().beginTransaction();
                transaction
                        .remove(getFragmentManager().findFragmentById(R.id.fragment_job_list))
                        .commit();
                scSocket.unregisterEvent(driverID);
                scSocket.unsubscribe("driverJob");
                scSocket.deauthenticate();
                scSocket.disconnect();

                unbindService(conn);
                bound = false;
                JobContent.ITEM_MAP.clear();
                stopService(scSocketService);
                stopService(chatheadService);
                onDestroy();
                finish();
            }
        });

        /********** get Gps location service LocationManager object ***********/
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        /** CAL METHOD requestLocationUpdates */
        /**
         // Parameters :
         //   First(provider)    :  the name of the provider with which to register
         //   Second(minTime)    :  the minimum time interval for notifications,
         //                         in milliseconds. This field is only used as a hint
         //                         to conserve power, and actual time between location
         //                         updates may be greater or lesser than this value.
         //   Third(minDistance) :  the minimum distance interval for notifications, in meters
         //   Fourth(listener)   :  a {#link LocationListener} whose onLocationChanged(Location)
         //                         method will be called for each location update
         */

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                0,   // 3 sec
                10, this);

        /********* After registration onLocationChanged method  ********/
        /********* called periodically after each 3 sec ***********/

    }

    private ServiceConnection conn = new ServiceConnection(){

        @Override
        public void onServiceConnected(ComponentName component, IBinder binder){
            SCSocketService.SCSocketBinder scSocketBinder = (SCSocketService.SCSocketBinder) binder;
            scSocket = scSocketBinder.getBinder();
            scSocket.setDelegate(MainActivity.this);
            bound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName component){
            bound = false;
        }
    };

    @Override
    protected void onStart(){
        super.onStart();
        scSocketService = new Intent(this, SCSocketService.class);
        bindService(scSocketService, conn, Context.BIND_AUTO_CREATE);
        startService(scSocketService);
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
        LocalBroadcastManager.getInstance(this).registerReceiver(eventsMsgHandler, new IntentFilter("io.socketcluster.eventsreceiver"));
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter("io.socketcluster.eventsreceiver"));
        LocalBroadcastManager.getInstance(this).registerReceiver(channelMsgHandler, new IntentFilter("io.socketcluster.eventsreceiver"));
        LocalBroadcastManager.getInstance(this).registerReceiver(errorMsgHandler, new IntentFilter("io.socketcluster.eventsreceiver"));
        LocalBroadcastManager.getInstance(this).registerReceiver(authenticateMsgHandler, new IntentFilter("io.socketcluster.eventsreceiver"));
        active = true;
        newJobMsg();
    }

    @Override
    protected void onPause(){
        active = false;
        super.onPause();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(eventsMsgHandler);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(channelMsgHandler);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(errorMsgHandler);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(authenticateMsgHandler);
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

    // TODO

    /**
     * Called when the location has changed.
     * <p/>
     * <p> There are no restrictions on the use of the supplied Location object.
     *
     * @param location The new location, as a Location object.
     */

    @Override
    public void onLocationChanged(Location location) {

        scSocket.publish("location", "{lat :" + location.getLatitude() + ", lng:" + location.getLongitude() + ", driver:" + driverID + "}");
            this.lat = location.getLatitude();
            this.lng = location.getLongitude();
    }

    @Override
    public void onProviderDisabled(String provider) {

        /******** Called when User off Gps *********/

        Toast.makeText(getBaseContext(), "Gps turned off ", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onProviderEnabled(String provider) {

        /******** Called when User on Gps  *********/

        Toast.makeText(getBaseContext(), "Gps turned on ", Toast.LENGTH_LONG).show();
    }


    /**
     * Called when the provider status changes. This method is called when
     * a provider is unable to fetch a location or if the provider has recently
     * become available after a period of unavailability.
     *
     * @param provider the name of the location provider associated with this
     *                 update.
     * @param status   {@link android.location.LocationProvider#OUT_OF_SERVICE} if the
     *                 provider is out of service, and this is not expected to change in the
     *                 near future; {@link android.location.LocationProvider#TEMPORARILY_UNAVAILABLE} if
     *                 the provider is temporarily unavailable but is expected to be available
     *                 shortly; and {@link android.location.LocationProvider#AVAILABLE} if the
     *                 provider is currently available.
     * @param extras   an optional Bundle which will contain provider specific
     *                 status variables.
     *                 <p/>
     *                 <p> A number of common key/value pairs for the extras Bundle are listed
     *                 below. Providers that use any of the keys on this list must
     *                 provide the corresponding value as described below.
     *                 <p/>
     *                 <ul>
     *                 <li> satellites - the number of satellites used to derive the fix
     */
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    //TODO

    /**
     * BroadcastReceiver to receive messages from {@link SCSocketService} to handle events
     * Broadcast receiver can be changed or even implemented at new class but has to be to handle events from socketcluster client
     */
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String event = intent.getStringExtra("event");
            String data = intent.getStringExtra("data");
            handleEvents(event,data);
        }
    };

    public void handleEvents(String event, String data) {
        switch(event){

            default:
                break;

            case SCSocketService.EVENT_ON_READY:
                Log.d(TAG, "ready");
                MyBroadcastReceiver.setService(scSocket);
                scSocket.connect(options);
                break;

            case SCSocketService.EVENT_ON_CONNECT:
                Log.d(TAG, "connected: "+data);
                scSocket.subscribe("updateJob");
                scSocket.subscribe("newJob");
                scSocket.subscribe("deleteJob");
                scSocket.registerEvent("jobs");
                scSocket.emitEvent("driverLogin", driverID);
                break;

            case SCSocketService.EVENT_ON_DISCONNECT:
                //  if(!logout)
                //    scSocket.authenticate(authToken);
                Log.d(TAG, "disconnected");
                break;

        }
    }

    @Override
    public void onClickList(JobContent.Job item) {
        if(!active)
            return;
        JobDetailsFragment jobDetailsFragment = new JobDetailsFragment();
        jobDetailsFragment.setJob(item);
        transaction = getFragmentManager().beginTransaction();
        if(item!=null) {
            transaction
                    .replace(R.id.fragment_job_details, jobDetailsFragment)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .commit();
        }else{
            transaction
                    .remove(getFragmentManager().findFragmentById(R.id.fragment_job_details))
                    .commit();
        }
    }

    @Override
    public void newJobMsg() {
        if(!active)
            return;
        JobsListFragment jobListFragment = new JobsListFragment();
        transaction = getFragmentManager().beginTransaction();
        transaction
                .replace(R.id.fragment_job_list, jobListFragment)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .commit();
    }

    @Override
    public void newEvent() {

    }

}