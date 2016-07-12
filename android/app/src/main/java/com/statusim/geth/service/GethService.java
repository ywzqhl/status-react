package com.statusim.geth.service;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.*;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.lang.ref.WeakReference;

import com.github.status_im.status_go.Statusgo;
import com.statusim.R;
import org.json.JSONObject;

import java.io.File;

public class GethService extends Service {

    private static final String TAG = "GethService";

    private static boolean isGethStarted = false;
    private static boolean isGethInitialized = false;
    private final Handler handler = new Handler();

    private static String dataFolder;

    private static GethService gethService = null;

    private static int WHISPER_NOTIFICATION_ID = 1;

    static class IncomingHandler extends Handler {

        private final WeakReference<GethService> service;

        IncomingHandler(GethService service) {

            this.service = new WeakReference<GethService>(service);
        }

        @Override
        public void handleMessage(Message message) {

            GethService service = this.service.get();
            if (service != null) {
                if (!service.handleMessage(message)) {
                    super.handleMessage(message);
                }
            }
        }
    }

    final Messenger serviceMessenger = new Messenger(new IncomingHandler(this));


    public static void signalEvent(String jsonEvent) {
        System.out.println("\n\n\nIT WOOOOOORKS1111!!!!!!\n\n\n");
        Log.d(TAG, jsonEvent);
        if (gethService != null) {
            try {
                gethService.onGethEvent(new JSONObject(jsonEvent));
            } catch (Exception e) {
                Log.e(TAG, "Exception converting event to json: ", e);
            }
        }
    }

    public void sendNotification(int id, String title, String content) {
        final NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setContentTitle("Scheduled Notification")
                .setAutoCancel(true)
                .setContentTitle(title)
                .setContentText(content)
                .setSmallIcon(R.drawable.icon_tab_chats);

       /* PendingIntent pendingIntent = PendingIntent.getActivity(this,
                NOTIFICATION_ID,
                new Intent(this, NotificationActivity.class),
                PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);
        builder.setDeleteIntent(NotificationEventReceiver.getDeleteIntent(this));
        */

        final NotificationManager manager = (NotificationManager) this.getSystemService(Service.NOTIFICATION_SERVICE);
        manager.notify(id, builder.build());
    }

    public void onGethEvent(JSONObject event) {
        try {
            String eventType = event.getString("type");
            switch(eventType) {
                case "whisper":
                    JSONObject whisperEvent = event.getJSONObject("event");
                    sendNotification(WHISPER_NOTIFICATION_ID, "You received a status message", whisperEvent.getString("payload"));
                    break;
                default:
            }

        } catch (Exception e) {
            Log.e(TAG, "Error handling geth event: ", e);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return serviceMessenger.getBinder();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        System.loadLibrary("statusgoraw");
        System.loadLibrary("statusgo");
        gethService = this;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //TODO: stop geth
        stopNode(null);
        isGethStarted = false;
        isGethInitialized = false;
        Log.d(TAG, "Geth Service stopped !");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_STICKY;
    }

    protected boolean handleMessage(Message message) {
        switch (message.what) {

            case GethMessages.MSG_START_NODE:
                Log.d(TAG, "Received start node message." + message.toString());
                startNode(message);
                break;

            case GethMessages.MSG_STOP_NODE:
                stopNode(message);
                break;

            case GethMessages.MSG_CREATE_ACCOUNT:
                createAccount(message);
                break;

            case GethMessages.MSG_ADD_ACCOUNT:
                addAccount(message);
                break;

            case GethMessages.MSG_LOGIN:
                login(message);
                break;

            case GethMessages.MSG_ADD_WHISPER_FILTER:
                addWhisperFilter(message);
                break;

            case GethMessages.MSG_REMOVE_WHISPER_FILTER:
                removeWhisperFilter(message);
                break;

            case GethMessages.MSG_CLEAR_WHISPER_FILTERS:
                clearWhisperFilters(message);
                break;

            default:
                return false;
        }

        return true;
    }

    protected void startNode(Message message) {
        if (!isGethInitialized) {
            isGethInitialized = true;
            Log.d(TAG, "Client messenger1: " + message.replyTo.toString());
            Bundle data = message.getData();
            String callbackIdentifier = data.getString(GethConnector.CALLBACK_IDENTIFIER);
            Log.d(TAG, "Callback identifier: " + callbackIdentifier);
            new StartTask(message.replyTo, callbackIdentifier).execute();
        }
    }

    protected class StartTask extends AsyncTask<Void, Void, Void> {

        protected String callbackIdentifier;
        protected Messenger messenger;

        public StartTask(Messenger messenger, String callbackIdentifier) {
            this.messenger = messenger;
            this.callbackIdentifier = callbackIdentifier;
        }

        protected Void doInBackground(Void... args) {
            startGeth();
            return null;
        }

        protected void onPostExecute(Void results) {
            onGethStarted(messenger, callbackIdentifier);
        }
    }

    protected void onGethStarted(Messenger messenger, String callbackIdentifier) {
        Log.d(TAG, "Geth Service started");
        isGethStarted = true;
        Message replyMessage = Message.obtain(null, GethMessages.MSG_NODE_STARTED, 0, 0, null);
        Bundle replyData = new Bundle();
        Log.d(TAG, "Callback identifier: " + callbackIdentifier);
        replyData.putString(GethConnector.CALLBACK_IDENTIFIER, callbackIdentifier);
        replyMessage.setData(replyData);
        sendReply(messenger, replyMessage);
    }

    protected void startGeth() {


        File extStore = Environment.getExternalStorageDirectory();

        dataFolder = extStore.exists() ?
                extStore.getAbsolutePath() + "/ethereum" :
                getApplicationInfo().dataDir + "/ethereum";
        Log.d(TAG, "Starting background Geth Service in folder: " + dataFolder);
        try {
            final File newFile = new File(dataFolder);
            newFile.mkdir();
        } catch (Exception e) {
            Log.e(TAG, "error making folder: " + dataFolder, e);
        }

        new Thread(new Runnable() {
            public void run() {

                Statusgo.StartNode(dataFolder);
            }
        }).start();
    }

    protected void stopNode(Message message) {
        // TODO: stop node

        createAndSendReply(message, GethMessages.MSG_NODE_STOPPED, null);
    }

    protected void createAccount(Message message) {
        Bundle data = message.getData();
        String password = data.getString("password");
        // TODO: remove second argument
        Log.d(TAG, "Creating account: " + password + " - " + dataFolder);
        String jsonData = Statusgo.CreateAccount(password);
        Log.d(TAG, "Created account: " + jsonData);

        Bundle replyData = new Bundle();
        replyData.putString("data", jsonData);
        createAndSendReply(message, GethMessages.MSG_ACCOUNT_CREATED, replyData);
    }

    protected void addAccount(Message message) {
        Bundle data = message.getData();
        String privateKey = data.getString("privateKey");
        String password = data.getString("password");
        // TODO: add account
        //String address = Statusgo.doAddAccount(privateKey, password);
        String address = "added account address";
        Log.d(TAG, "Added account: " + address);

        Bundle replyData = new Bundle();
        replyData.putString("address", address);
        createAndSendReply(message, GethMessages.MSG_ACCOUNT_ADDED, replyData);
    }

    protected void login(Message message) {
        Bundle data = message.getData();
        String address = data.getString("address");
        String password = data.getString("password");
        // TODO: remove third argument
        String result = Statusgo.UnlockAccount(address, password, 0);
        Log.d(TAG, "Unlocked account: " + result);

        Bundle replyData = new Bundle();
        replyData.putString("result", result);
        createAndSendReply(message, GethMessages.MSG_LOGGED_IN, replyData);
    }

    protected void addWhisperFilter(Message message) {
        Bundle data = message.getData();
        String filter = data.getString("filter");
        String result = Statusgo.addWhisperFilter(filter);
        Log.d(TAG, "Added whisper filter: " + result);

        Bundle replyData = new Bundle();
        replyData.putString("result", result);
        createAndSendReply(message, GethMessages.MSG_WHISPER_FILTER_ADDED, replyData);
    }

    protected void removeWhisperFilter(Message message) {
        Bundle data = message.getData();
        int idFilter = data.getInt("idFilter");
        Statusgo.removeWhisperFilter(idFilter);
        Log.d(TAG, "Removed whisper filter: " + idFilter);

        createAndSendReply(message, GethMessages.MSG_WHISPER_FILTER_REMOVED, null);
    }

    protected void clearWhisperFilters(Message message) {
        Statusgo.clearWhisperFilters();
        Log.d(TAG, "Cleared whisper filters.");

        createAndSendReply(message, GethMessages.MSG_WHISPER_FILTERS_CLEARED, null);
    }

    public static boolean isRunning() {
        return isGethInitialized;
    }

    protected void createAndSendReply(Message message, int replyIdMessage, Bundle replyData) {

        if (message == null) {
            return;
        }
        Message replyMessage = Message.obtain(null, replyIdMessage, 0, 0, message.obj);
        if (replyData == null) {
            replyData = new Bundle();
        }
        Bundle data = message.getData();
        String callbackIdentifier = data.getString(GethConnector.CALLBACK_IDENTIFIER);
        Log.d(TAG, "Callback identifier: " + callbackIdentifier);
        replyData.putString(GethConnector.CALLBACK_IDENTIFIER, callbackIdentifier);
        replyMessage.setData(replyData);

        sendReply(message.replyTo, replyMessage);
    }

    protected void sendReply(Messenger messenger, Message message) {
        try {
            messenger.send(message);

        } catch (Exception e) {

            Log.e(TAG, "Exception sending message id: " + message.what, e);
        }
    }
}
