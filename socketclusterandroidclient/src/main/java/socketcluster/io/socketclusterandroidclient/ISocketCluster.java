package socketcluster.io.socketclusterandroidclient;

/**
 * Created by lihanli on 9/08/2015.
 */
public interface ISocketCluster {

	/**
     *  scSocket.on(event)
     *  @param {String} event
     *  @param {String} data
     */
	void socketClusterReceivedEvent(String event, String data);

	/**
     *  scSocket.on(channel)
     *  @name - channel name
     *  @data - data received
     */
    void socketClusterChannelReceivedEvent(String name, String data);
	
    /**
     *  Handler scSocket.on(connect)
     *  @param data - json object
     *  data.id
     *  data.isAuthenticated
     *  data.authToken
     *  data.authError:{name: errName, message: errMsg} - json object if error occurred with authToken
     */
    void socketClusterDidConnect(String data);

	/**
     *  Handler scSocket.on(disconnect)
     */
	void socketClusterDidDisconnect();
    
    /**
     *  Handler scSocket.on(error)
     *  @error - error object
     */
    void socketClusterOnError(String error);
    
    /**
     *  Handler scSocket.on(kickOut)
     */
    void socketClusterOnKickOut(String data);
    
    /**
     *  Handler scSocker.on(subscribe)
     */
    void socketClusterOnSubscribe();
    
    /**
     *  Handler scSocket.on(subscribeFail)
     *  @error
     */
    void socketClusterOnSubscribeFail(String error);
    
    /**
     *  Handler scSocket.on(unsubscribe)
     */
    void socketClusterOnUnsubscribe();
    
    /**
     *  Handler scSocket.on(authenticate)
     *  @data - error object, null if succes authenticated
     */
    void socketClusterOnAuthenticate(String data);
    
    /**
     *  Handler scSocket.on(deauthenticate)
     */
    void socketClusterOnDeauthenticate();
    
    void socketClusterOnGetState(String state);
    void socketClusterOnSubscribeStateChange(String state);
    void socketClusterOnAuthStateChange(String state);
}