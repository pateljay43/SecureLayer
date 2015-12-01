package com.csulb.cscs579.securelayer;

import android.util.Log;

import com.google.api.client.json.Json;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigInteger;
import java.net.URI;
import java.nio.channels.NotYetConnectedException;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by JAY on 10/22/15.
 */
public class MyWebSocket extends WebSocketClient {
    private boolean connected;
    private static MyWebSocket instance = null;
    private static URI uri;
    private String reply;       // server's reply
    private boolean receivedReply;  // did server replied;
    private boolean sent;       // message sent by calling send method
    private MyKeyStore myKeyStore;
    private String id;
    private String hp;
    private boolean sign_up;

    public static MyWebSocket getInstance(URI _uri,
                                          Draft _draft,
                                          Map<String, String> headers,
                                          int timeout,
                                          MyKeyStore _myKeyStore) {
        if (uri != null && _uri.compareTo(uri) == 0) {
            if (instance == null) {
                instance = new MyWebSocket(_uri, _draft, headers, timeout, _myKeyStore);
            }
        } else {
            instance = new MyWebSocket(_uri, _draft, headers, timeout, _myKeyStore);
        }
        return instance;
    }

    private MyWebSocket(URI _uri,
                        Draft _draft,
                        Map<String, String> headers,
                        int timeout,
                        MyKeyStore _myKeyStore) {
        super(_uri, _draft, headers, timeout);
        uri = _uri;
        connected = false;
        receivedReply = false;
        sent = false;
        myKeyStore = _myKeyStore;
        id = "";
        hp = "";
        sign_up = false;
    }

    /**
     * login or signup with secure server
     *
     * @param _id      user name
     * @param _hp      hash of password
     * @param _sign_up signup with the credentials
     */
    public void connect(String _id, String _hp, boolean _sign_up) {
        id = _id;
        hp = _hp;
        sign_up = _sign_up;
        sent = false;
        super.connect();
    }

    @Override
    public void send(String text) throws NotYetConnectedException {
        super.send(text);
        sent = true;
    }

    @Override
    public void send(byte[] data) throws NotYetConnectedException {
        super.send(data);
        sent = true;
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        try {
//            connected = true;
            Log.e("Websocket", "Opened");
            Log.e("Connection established", ":::::::::::::::::::::::::::::::::::::::::::::");

            JSONObject json = new JSONObject();
            json.put(Constants.id, id);
            json.put(Constants.hp, hp);
            if (sign_up) {
                super.send(new JSONObject().put(Constants.sign_up, json).toString());
            } else {
                super.send(new JSONObject().put(Constants.verify, json).toString());
            }
        } catch (Exception e) {
            Log.e("onOpen", e.getMessage());
        }
    }

    @Override
    public void onMessage(String message) {
        Log.e("Server Reply", message);
        if (sent) {
            receivedReply = true;
            reply = message;
            return;
        }
        JSONObject json = null;
        try {
            json = new JSONObject(message);

        } catch (Exception e) {
            Log.e("on message", e.getMessage());
        }
        if (json != null) {
            Iterator<String> keys = json.keys();
            try {
                while (keys.hasNext()) {
                    String command = keys.next();
                    if (command.equals(Constants.verify)) {   // verified -> save server's pubkey | establish session key
                        JSONObject data = json.getJSONObject(Constants.verify);
                        myKeyStore.setServerKey(data.getString(Constants.publickey));
                        BigInteger dhA = myKeyStore
                                .getdhA((BigInteger) data.get(Constants.g), (BigInteger) data.get(Constants.p));
                        myKeyStore
                                .generateSessionKey((BigInteger) data.get(Constants.dh), (BigInteger) data.get(Constants.p));
                        String dh = myKeyStore.encryptRSA(dhA).toString();
                        super.send(new JSONObject().put(Constants.dh, dh).toString());
                        connected = true;
                    } else if (command.equals(Constants.error)) {
                        Log.e("Error", "from server");
                    }
                }
            } catch (JSONException e) {
                Log.e("not verified", e.getMessage());
                connected = false;
            }
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        Log.e("Closing", ":::::::::::::::::::::::::::::::::::::::::::::");
        Log.e("Reason", reason);
        connected = false;
    }

    @Override
    public void onError(Exception ex) {
        Log.i("Websocket", "Error " + ex.getMessage());
    }

    public String getReply() {
        receivedReply = false;
        String re = reply;
        reply = null;
        return re;
    }

    public boolean isConnected() {
        return connected;
    }

    public boolean receivedReply() {
        return sent ? receivedReply : (receivedReply = false);
    }
}
