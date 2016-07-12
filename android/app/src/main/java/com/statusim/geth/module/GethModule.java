package com.statusim.geth.module;

import android.app.Activity;
import android.os.Bundle;
import android.os.Message;
import com.facebook.react.bridge.*;
import com.statusim.geth.service.ConnectorHandler;
import com.statusim.geth.service.GethConnector;
import com.statusim.geth.service.GethMessages;
import com.statusim.geth.service.GethService;
import android.util.Log;

import java.util.HashMap;
import java.util.UUID;

public class GethModule extends ReactContextBaseJavaModule implements LifecycleEventListener, ConnectorHandler {

    private static final String TAG = "GethModule";

    protected GethConnector geth = null;
    protected String handlerIdentifier = createIdentifier();

    protected HashMap<String, Callback> callbacks = new HashMap<>();



    public GethModule(ReactApplicationContext reactContext) {
        super(reactContext);
        reactContext.addLifecycleEventListener(this);
    }

    @Override
    public String getName() {
        return "Geth";
    }

    @Override
    public void onHostResume() {  // Actvity `onResume`

        Activity currentActivity = getCurrentActivity();

        if (currentActivity == null) {
            return;
        }
        if (geth == null) {
            geth = new GethConnector(currentActivity, GethService.class);
            geth.registerHandler(this);
        }
        geth.bindService();
    }

    @Override
    public void onHostPause() {  // Actvity `onPause`

        if (geth != null) {
            geth.unbindService();
        }
    }

    @Override
    public void onHostDestroy() {  // Actvity `onDestroy`

        if (geth != null) {
            geth.stopNode(null);
        }
    }

    @Override
    public String getID() {

        return handlerIdentifier;
    }

    @Override
    public void onConnectorConnected() {
    }

    @Override
    public void onConnectorDisconnected() {
    }

    @Override
    public boolean handleMessage(Message message) {

        Log.d(TAG, "Received message: " + message.toString());
        boolean isClaimed = true;
        Bundle data = message.getData();
        String callbackIdentifier = data.getString(GethConnector.CALLBACK_IDENTIFIER);
        Log.d(TAG, "callback identifier: " + callbackIdentifier);
        Callback callback = callbacks.remove(callbackIdentifier);
        if (callback == null) {
            Log.d(TAG, "Could not find callback: " + callbackIdentifier);
        }
        switch (message.what) {
            case GethMessages.MSG_NODE_STARTED:
                if (callback != null) {
                    callback.invoke(true);
                }
                break;
            case GethMessages.MSG_NODE_STOPPED:
                break;
            case GethMessages.MSG_ACCOUNT_CREATED:
                if (callback != null) {
                    callback.invoke(data.getString("data"));
                }
                break;
            case GethMessages.MSG_ACCOUNT_ADDED:
                if (callback != null) {
                    callback.invoke(null, "{ \"address\": \"" + data.getString("address") + "\"}");
                }
                break;
            case GethMessages.MSG_LOGGED_IN:
                if (callback != null) {
                    callback.invoke(null, "{ \"result\": \"" + data.getString("result") + "\"}");
                }
                break;
            case GethMessages.MSG_WHISPER_FILTER_ADDED:
                if (callback != null) {
                    callback.invoke(data.getString("result"));
                }
                break;
            case GethMessages.MSG_WHISPER_FILTER_REMOVED:
                if (callback != null) {
                    callback.invoke();
                }
                break;
            case GethMessages.MSG_WHISPER_FILTERS_CLEARED:
                if (callback != null) {
                    callback.invoke();
                }
                break;
            default:
                isClaimed = false;
        }

        return isClaimed;
    }

    @ReactMethod
    public void startNode(Callback callback, Callback onAlreadyRunning) {

        if(GethService.isRunning()){
            onAlreadyRunning.invoke();
            return;
        }

        Activity currentActivity = getCurrentActivity();

        if (currentActivity == null) {
            callback.invoke("Activity doesn't exist");
            return;
        }

        if (geth == null) {
            callback.invoke("Geth connector is null");
            return;
        }

        String callbackIdentifier = createIdentifier();
        Log.d(TAG, "Created callback identifier: " + callbackIdentifier);
        callbacks.put(callbackIdentifier, callback);

        geth.startNode(callbackIdentifier);
    }

    @ReactMethod
    public void login(String address, String password, Callback callback) {

        Activity currentActivity = getCurrentActivity();

        if (currentActivity == null) {
            callback.invoke("Activity doesn't exist");
            return;
        }

        if (geth == null) {
            callback.invoke("Geth connector is null");
            return;
        }

        String callbackIdentifier = createIdentifier();
        callbacks.put(callbackIdentifier, callback);

        geth.login(callbackIdentifier, address, password);
    }

    @ReactMethod
    public void createAccount(String password, Callback callback) {

        Activity currentActivity = getCurrentActivity();

        if (currentActivity == null) {
            callback.invoke("Activity doesn't exist");
            return;
        }

        if (geth == null) {
            callback.invoke("Geth connector is null");
            return;
        }

        String callbackIdentifier = createIdentifier();
        callbacks.put(callbackIdentifier, callback);

        geth.createAccount(callbackIdentifier, password);
    }

    @ReactMethod
    public void addAccount(String privateKey, Callback callback) {

        Activity currentActivity = getCurrentActivity();

        if (currentActivity == null) {
            callback.invoke("Activity doesn't exist");
            return;
        }

        if (geth == null) {
            callback.invoke("Geth connector is null");
            return;
        }

        String callbackIdentifier = createIdentifier();
        callbacks.put(callbackIdentifier, callback);
        geth.addAccount(callbackIdentifier, privateKey);
    }

    @ReactMethod
    public void addWhisperFilter(String filter, Callback callback) {

        Activity currentActivity = getCurrentActivity();

        if (currentActivity == null) {
            callback.invoke("Activity doesn't exist");
            return;
        }

        if (geth == null) {
            callback.invoke("Geth connector is null");
            return;
        }

        String callbackIdentifier = createIdentifier();
        callbacks.put(callbackIdentifier, callback);
        geth.addWhisperFilter(callbackIdentifier, filter);
    }

    @ReactMethod
    public void removeWhisperFilter(int idFilter, Callback callback) {

        Activity currentActivity = getCurrentActivity();

        if (currentActivity == null) {
            callback.invoke("Activity doesn't exist");
            return;
        }

        if (geth == null) {
            callback.invoke("Geth connector is null");
            return;
        }

        String callbackIdentifier = createIdentifier();
        callbacks.put(callbackIdentifier, callback);
        geth.removeWhisperFilter(callbackIdentifier, idFilter);
    }

    @ReactMethod
    public void clearWhisperFilters(Callback callback) {

        Activity currentActivity = getCurrentActivity();

        if (currentActivity == null) {
            callback.invoke("Activity doesn't exist");
            return;
        }

        if (geth == null) {
            callback.invoke("Geth connector is null");
            return;
        }

        String callbackIdentifier = createIdentifier();
        callbacks.put(callbackIdentifier, callback);
        geth.clearWhisperFilters(callbackIdentifier);
    }

    protected String createIdentifier() {
        return UUID.randomUUID().toString();
    }

}
