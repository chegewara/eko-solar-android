package socketcluster.io.socketclusterandroidclient;
import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import com.fangjian.WebViewJavascriptBridge;

import org.json.JSONObject;
import org.json.JSONStringer;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;

import java.io.InputStream;

import java.util.HashMap;
import java.util.Map;


public class SCSocketService extends Service {
    private String host;
    private String port;
    private Boolean isHttps;
    private WebView webView;
    private WebViewJavascriptBridge bridge;
    private ISocketCluster socketClusterDelegate;
    private final String TAG = "SCClient";
    private Activity mContext;
    private final IBinder binder = new SCSocketBinder();
    
    public class SCSocketBinder extends Binder {
    		public SCSocketService getBinder(){
    			return SCSocketService.this;
    		}
    };
    
    @Override
    public IBinder onBind(Intent intent){
    		return binder;
    };
    @Override
    public void onCreate(){
    }

    public void setDelegate(ISocketCluster delegate) {
        socketClusterDelegate = delegate;
        if(webView == null) {
            this.mContext = (Activity) delegate;
            this.setupSCWebClient(mContext);
            this.registerHandles();
        }
    }

    class UserServerHandler implements WebViewJavascriptBridge.WVJBHandler{
        @Override
        public void handle(String data, WebViewJavascriptBridge.WVJBResponseCallback jsCallback) {
            Log.d("UserServerHandler","Received message from javascript: "+ data);
            if (null !=jsCallback) {
                jsCallback.callback("Java said:Right back atcha");
            }
        }
    }


    private void setupSCWebClient(Activity context) {
        webView = new WebView(context);
        bridge = new WebViewJavascriptBridge(context, webView, new UserServerHandler());
        webView.setWebViewClient(
                new WebViewClient() {
                    public boolean shouldOverrideUrlLoading(WebView view, String url) {
                        view.loadUrl(url);
                        return false;
                    }
                });
//        webView.loadUrl("http:///ns1.diskstation.eu:3010/user_client.html");
        webView.loadUrl("file:///android_asset/user_client.html");

    }
    
    private void registerHandles(){

		/**
         *  'connect' event handler
         *  @param data json object
         */
        bridge.registerHandler("onConnectHandler", new WebViewJavascriptBridge.WVJBHandler() {
            @Override
            public void handle(String data, WebViewJavascriptBridge.WVJBResponseCallback jsCallback) {
                socketClusterDelegate.socketClusterDidConnect(data);
            }
        });

		/**
         *  'disconnect' event handler
         */
        bridge.registerHandler("onDisconnectedHandler", new WebViewJavascriptBridge.WVJBHandler() {
            @Override
            public void handle(String data, WebViewJavascriptBridge.WVJBResponseCallback jsCallback) {
                socketClusterDelegate.socketClusterDidDisconnect();
            }
        });

		/**
         *  'error' event handler
         */
        bridge.registerHandler("onErrorHandler", new WebViewJavascriptBridge.WVJBHandler() {
            @Override
            public void handle(String data, WebViewJavascriptBridge.WVJBResponseCallback jsCallback) {
                socketClusterDelegate.socketClusterOnError(data);
            }
        });

		/**
         *  'kickOut' event handler
         *  @param data - channel
         */
        bridge.registerHandler("onKickoutHandler", new WebViewJavascriptBridge.WVJBHandler() {
            @Override
            public void handle(String data, WebViewJavascriptBridge.WVJBResponseCallback jsCallback) {
                socketClusterDelegate.socketClusterOnKickOut(data);
            }
        });

		/**
         *  'subscribeFail' event handler
         *  @data - error
         */
        bridge.registerHandler("onSubscribeFailHandler", new WebViewJavascriptBridge.WVJBHandler() {
            @Override
            public void handle(String data, WebViewJavascriptBridge.WVJBResponseCallback jsCallback) {
                socketClusterDelegate.socketClusterOnSubscribeFail(data);
            }
        });

		/**
         *  'authenticate' event handler
         *  @data - authToken
         */
        bridge.registerHandler("onAuthenticateHandler", new WebViewJavascriptBridge.WVJBHandler(){

            @Override
            public void handle(String data, WebViewJavascriptBridge.WVJBResponseCallback jsCallback) {
                socketClusterDelegate.socketClusterOnAuthenticate(data);
            }
        });

		/**
         *  'deauthenticate' event handler
         */
        bridge.registerHandler("onDeauthenticateHandler", new WebViewJavascriptBridge.WVJBHandler() {
            @Override
            public void handle(String data, WebViewJavascriptBridge.WVJBResponseCallback jsCallback) {
                socketClusterDelegate.socketClusterOnDeauthenticate();
            }
        });

		/**
         *  'unsubscribe' event handler
         */
        bridge.registerHandler("onUnsubscribeHandler", new WebViewJavascriptBridge.WVJBHandler() {
            @Override
            public void handle(String data, WebViewJavascriptBridge.WVJBResponseCallback jsCallback) {
                socketClusterDelegate.socketClusterOnUnsubscribe();
            }
        });

		/**
         *
         */
        bridge.registerHandler("onAuthStateChangeHandler", new WebViewJavascriptBridge.WVJBHandler() {
            @Override
            public void handle(String data, WebViewJavascriptBridge.WVJBResponseCallback jsCallback) {
                socketClusterDelegate.socketClusterOnAuthStateChange(data);
            }
        });

		/**
         *
         */
        bridge.registerHandler("onSubscribeStateChangeHandler", new WebViewJavascriptBridge.WVJBHandler() {
            @Override
            public void handle(String data, WebViewJavascriptBridge.WVJBResponseCallback jsCallback) {
                socketClusterDelegate.socketClusterOnSubscribeStateChange(data);
            }
        });

		/**
         *  'subscribe' event handler
         */
        bridge.registerHandler("onSubscribeHandler", new WebViewJavascriptBridge.WVJBHandler() {
            @Override
            public void handle(String data, WebViewJavascriptBridge.WVJBResponseCallback jsCallback) {
                socketClusterDelegate.socketClusterOnSubscribe();
            }
        });

		/**
         *  'on(event)' event handler
         */
        bridge.registerHandler("onEventReceivedFromSocketCluster", new WebViewJavascriptBridge.WVJBHandler() {
            @Override
            public void handle(String data, WebViewJavascriptBridge.WVJBResponseCallback jsCallback) {
                HashMap<String, String> receivedData = (HashMap)JSONValue.parse(data);
                socketClusterDelegate.socketClusterReceivedEvent(receivedData.get("event"), receivedData.get("data"));
            }
        });
        
        /**
         *  'channel' received data helper
         */
        bridge.registerHandler("onChannelReceivedEventFromSocketCluster", new WebViewJavascriptBridge.WVJBHandler() {
            @Override
            public void handle(String data, WebViewJavascriptBridge.WVJBResponseCallback jsCallback) {
                HashMap<String, String> receivedData = (HashMap) JSONValue.parse(data);
                socketClusterDelegate.socketClusterChannelReceivedEvent(receivedData.get("channel"), receivedData.get("data"));
            }
        });
    }

    private void callJavaScript(WebView view, String methodName, Object...params){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("javascript:try{");
        stringBuilder.append(methodName);
        stringBuilder.append("(");
        String separator = "";
        for (Object param : params) {
            stringBuilder.append(separator);
            separator = ",";
            if(param instanceof String){
                stringBuilder.append("'");
            }
            stringBuilder.append(param);
            if(param instanceof String) {
                stringBuilder.append("'");
            }

        }
        stringBuilder.append(")}catch(error){console.error(error.message);}");
        final String call = stringBuilder.toString();
        Log.i(TAG, "callJavaScript: call=" + call);

        view.loadUrl(call);
    }

	/**
     *  Call scSocket.connect
     *  @param options
     */
    public void connect(String options) {
		bridge.callHandler("connectHandler", options);
    }
    
    /**
     * Call scSocket.disconnect
     */
    public void disconnect() {
        bridge.callHandler("disconnectHandler");
    }
    
    /**
     *  Call scSocket.emit
     *  @eventName
     *  @data
     */
    public void emitEvent(String eventName, String eventData) {

        if (null == eventData) {
            eventData = "";
        }
        Map data = new HashMap();
        data.put("event", eventName);
        data.put("data", eventData);
        String jsonText = JSONValue.toJSONString(data);
        bridge.callHandler("emitEventHandler", jsonText);
    }

    /**
     *  Call scSocket.on(event)
     */
    public void registerEvent(String eventName) {
        Map data = new HashMap();
        data.put("event", eventName);
        String jsonText = JSONValue.toJSONString(data);
        bridge.callHandler("onEventHandler", jsonText);
    }

    /**
     *  Call scSocket.on(event)
     */
    public void unregisterEvent(String eventName) {
        Map data = new HashMap();
        data.put("event", eventName);
        String jsonText = JSONValue.toJSONString(data);
        bridge.callHandler("offEventHandler", jsonText);
    }

    /**
     *  Call scSocket.publish 
     *  @channelName 
     *  @eventData
     */
    public void publishToChannel(String channelName, String eventData) {
        Map data = new HashMap();
        data.put("channel", channelName);
        data.put("data", eventData);
        String jsonText = JSONValue.toJSONString(data);
        bridge.callHandler("publishHandler", jsonText);
    }
    
    /**
     *  Call scSocket.subscribe
     *  @channelName
     */
    public void subscribeToChannel(String channelName) {
        Map data = new HashMap();
        data.put("channel", channelName);
        String jsonText = JSONValue.toJSONString(data);
        bridge.callHandler("subscribeHandler", jsonText);
    }
    
    /**
     *  Call scSocket.unsubscribe
     *  @channelName
     */
    public void unsubscribeFromChannel(String channelName) {
        Map data = new HashMap();
        data.put("channel", channelName);
        String jsonText = JSONValue.toJSONString(data);
        bridge.callHandler("unsubscribeHandler", jsonText);
    }
    
    /**
     *  Call scSocket.authenticate
     *  @authToken 
     */
    public void authenticate(String authToken) {
        Map map = new HashMap();
        map.put("authToken", authToken);
        String jsonText = JSONValue.toJSONString(map);
        bridge.callHandler("authenticateHandler", authToken);
    }
    
    /**
     *  Call scSocket.deauthenticate
     */
    public void deauthenticate() {
        bridge.callHandler("deauthenticateHandler");
    }
    
    /**  
     * Call scSocket.getState
     */
    public void getState(){
    	bridge.callHandler("getStateHandler", null, new WebViewJavascriptBridge.WVJBResponseCallback() {
            @Override
            public void callback(String data) {
                socketClusterDelegate.socketClusterOnGetState(data);
            }
        });
    }

    /**
     *  Call scSocket.subscriptions
     *  @pending
     */
    public String subscriptions(Boolean pending){
        final String[] d = {null};
        Map map = new HashMap();
        map.put("pending", pending.toString());
        String jsonText = JSONValue.toJSONString(map);
    	bridge.callHandler("subscriptionsHandler", jsonText, new WebViewJavascriptBridge.WVJBResponseCallback() {
            @Override
            public void callback(String data) {
                d[0] = data;
            }
        });
        return d[0];
    }
    
    /**
     *  Call scSocket.isSubscribed
     *  @channel
     *  @pending
     */
    public void isSubscribed(String channel, Boolean pending){
        Map map = new HashMap();
        map.put("channel", channel);
        map.put("pending", pending.toString());
        String jsonText = JSONValue.toJSONString(map);    
    	bridge.callHandler("isSubscribedHandler", jsonText);
    }
}