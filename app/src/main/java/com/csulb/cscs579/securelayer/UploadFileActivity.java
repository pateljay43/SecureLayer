package com.csulb.cscs579.securelayer;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_17;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.crypto.Cipher;

public class UploadFileActivity extends Activity {
    static final String TAG = "AsymmetricAlgorithmRSA";
    // unique request-response identifier between activities
    private static final int PICK_FILE_REQUEST = 1;
    private static boolean connectionStatus;
    // list to store <filename, URI to file> pair
    private Map<String, String> selectedFiles;
    // list of files uploaded to google drive
    private Map<String, String> remoteFiles;
    // store all filenames to show in ListView
    private List<String> values;
    // adapter to fill ListView element
    private ArrayAdapter<String> adapter;
    private FloatingActionButton addFileBtn;
    private WebSocketClient mWebSocketClient;
    private SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_file);
        connectionStatus = false;
        connectWebSocket();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        addFileBtn = (FloatingActionButton) findViewById(R.id.fab);
        //        setSupportActionBar(toolbar);
        selectedFiles = new TreeMap<>();
        values = new ArrayList<>();
        refreshList();
        ListView listView = (ListView) findViewById(R.id.selectedFilesList);
        adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1,
                values);
        listView.setAdapter(adapter);
    }

    /**
     * refresh the list of selected files
     */
    private void refreshList() {
        String[] filenames = selectedFiles.keySet().toArray(new String[selectedFiles.size()]);
        values.clear();
        Collections.addAll(values, filenames);
        if (adapter == null) {
            adapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_list_item_1, android.R.id.text1,
                    values);
        } else {
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    /**
     * perform action for the enlisted files (upload or clear the list of files)
     *
     * @param item selected option from option menu
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_upload) {
            // send files in selectedFiles
            if (connectionStatus) {
                try {
                    // generated fresh fileKey for each file
                    // upload the selectedFiles to google drive
                    // send Enc(fileKey) to server
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } else {
                Toast.makeText(UploadFileActivity.this, "Not connected to Secure Server", Toast.LENGTH_SHORT).show();
            }
            return true;
        } else if (id == R.id.action_clear) {
            selectedFiles.clear();
            // refresh list of selected files
            refreshList();
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * called when user press '+' button on main activity
     * opens new activity (FileSelector) to select files to be uploaded
     *
     * @param view Button element which called this method
     */
    public void filePicker(View view) {
//        Intent pickFileIntent = new Intent(this, FileSelector.class);
//        startActivityForResult(pickFileIntent, PICK_FILE_REQUEST);
        // Original text
        String theTestText = "This is just a simple test!";
        Log.e("Input Text", theTestText);
        // Generate key pair for 1024-bit RSA encryption and decryption
        Key publicKey = null;
        Key privateKey = null;
        try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
            kpg.initialize(1024);
            KeyPair kp = kpg.genKeyPair();
            publicKey = kp.getPublic();
            privateKey = kp.getPrivate();
        } catch (Exception e) {
            Log.e(TAG, "RSA key pair error");
        }

        // Encode the original data with RSA private key
        byte[] encodedBytes = null;
        try {
            Cipher c = Cipher.getInstance("RSA");
            c.init(Cipher.ENCRYPT_MODE, publicKey);
            encodedBytes = c.doFinal(theTestText.getBytes());
        } catch (Exception e) {
            Log.e(TAG, "RSA encryption error");
        }
        String s = new String(encodedBytes);
        Log.e("Encoded Text", s);
        // Decode the encoded data with RSA public key
        byte[] decodedBytes = null;
        try {
            Cipher c = Cipher.getInstance("RSA");
            c.init(Cipher.DECRYPT_MODE, privateKey);
            decodedBytes = c.doFinal(encodedBytes);
        } catch (Exception e) {
            Log.e(TAG, "RSA decryption error");
        }
        Log.e("Decoded Text", new String(decodedBytes));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_FILE_REQUEST) {
            if (resultCode == RESULT_OK) {
                String[] result = data.getStringArrayExtra("result");
                selectedFiles.put(result[0], result[1]);
                refreshList();
                Log.e("selected files count", "" + selectedFiles.size());
                // notify the list to be regenerated
            }
            if (resultCode == RESULT_CANCELED) {
//                Toast.makeText(UploadFileActivity.this, "Canceled", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onPause() {
        SharedPreferences sp = getSharedPreferences("syek", MODE_PRIVATE);
        sp.edit().remove("serverPubK");
        super.onPause();
    }

    private void connectWebSocket() {
        URI uri;
        try {
            uri = new URI("ws://75.142.120.49:8080/SecureServer/actions");
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return;
        }

        mWebSocketClient = new WebSocketClient(uri, new Draft_17()) {
            @Override
            public void onOpen(ServerHandshake serverHandshake) {
                Log.e("Websocket", "Opened");
                Log.e("Connection established", ":::::::::::::::::::::::::::::::::::::::::::::");
                mWebSocketClient.send("Hello from " + Build.MANUFACTURER + " " + Build.MODEL);
                connectionStatus = true;
            }

            @Override
            public void onMessage(String s) {
                final String message = s;
                SharedPreferences sp = getSharedPreferences("syek", MODE_PRIVATE);
                sp.edit().putString("serverPubK", message).commit();
                Log.e("From SP", sp.getString("serverPubK", null));
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
//                        TextView textView = (TextView) findViewById(R.id.messages);
//                        textView.setText(textView.getText() + "\n" + message);
                    }
                });
            }

            @Override
            public void onClose(int i, String s, boolean b) {
                Log.e("Closing", ":::::::::::::::::::::::::::::::::::::::::::::");
                Log.e("Websocket", "Closed " + s);
                connectionStatus = false;
            }

            @Override
            public void onError(Exception e) {
                Log.i("Websocket", "Error " + e.getMessage());
            }
        };
        mWebSocketClient.connect();
    }
}
