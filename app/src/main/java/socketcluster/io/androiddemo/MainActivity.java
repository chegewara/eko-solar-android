package socketcluster.io.androiddemo;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import socketcluster.io.socketclusterandroidclient.ISocketCluster;
import socketcluster.io.socketclusterandroidclient.SCSocketService;

  
public class MainActivity extends AppCompatActivity implements LocationListener {

    private static String TAG = "SCDemo";
    private SCSocketService sc;
    private Boolean bound = false;
    private LocationManager locationManager;
    private Intent chatheadService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        chatheadService = new Intent(getApplicationContext(), ChatHeadService.class);
        startService(chatheadService);

        /********** get Gps location service LocationManager object ***********/
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        /* CAL METHOD requestLocationUpdates */

        // Parameters :
        //   First(provider)    :  the name of the provider with which to register
        //   Second(minTime)    :  the minimum time interval for notifications,
        //                         in milliseconds. This field is only used as a hint
        //                         to conserve power, and actual time between location
        //                         updates may be greater or lesser than this value.
        //   Third(minDistance) :  the minimum distance interval for notifications, in meters
        //   Fourth(listener)   :  a {#link LocationListener} whose onLocationChanged(Location)
        //                         method will be called for each location update


        locationManager.requestLocationUpdates( LocationManager.GPS_PROVIDER,
                1500,   // 3 sec
                20, this);

        /********* After registration onLocationChanged method  ********/
        /********* called periodically after each 3 sec ***********/

        // Connect button
        final Button connectBtn = (Button) findViewById(R.id.btnConnect);
        final Button subsBtn = (Button) findViewById(R.id.button);

        subsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sc.subscriptions(true);
            }
        });

        connectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
        final Button loginBtn = (Button) findViewById(R.id.loginBtn);
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sc.emitEvent("login", "test");
            }
        });

        final Button subToWeatherBtn = (Button) findViewById(R.id.btnSubWeather);
        subToWeatherBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
            }
        });
        final Button unSubToWeatherBtn = (Button) findViewById(R.id.btnUnSubWeather);
        unSubToWeatherBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
            }
        });

        final Button pubToWeatherBtn = (Button) findViewById(R.id.btnPubWeather);
        pubToWeatherBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
            }
        });
    }

    private ServiceConnection conn = new ServiceConnection(){

    		@Override
    		public void onServiceConnected(ComponentName component, IBinder binder){
    			SCSocketService.SCSocketBinder scSocketBinder = (SCSocketService.SCSocketBinder) binder;
    			sc = scSocketBinder.getBinder();
                sc.setDelegate(new SCSocketHandler(), MainActivity.this);
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
        sc.deauthenticate();
        stopService(chatheadService);
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

    /**
     * Called when the location has changed.
     * <p/>
     * <p> There are no restrictions on the use of the supplied Location object.
     *
     * @param location The new location, as a Location object.
     */

    @Override
    public void onLocationChanged(Location location) {

        sc.emitEvent("location", "{lat :"+ location.getLatitude()+", lng:" +location.getLongitude()+"}");
    //    this.lat = location.getLatitude();
    //    this.lng = location.getLongitude();

        //TODO wysłać  pozycję do bazy danych
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