package com.csulb.cscs579.securelayer;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_17;
import org.java_websocket.handshake.ServerHandshake;

import java.io.File;
import java.io.FileInputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class HomeActivity extends AppCompatActivity {
    static final String TAG = "AsymmetricAlgorithmRSA";
    private static final int PICK_FILE_REQUEST = 1;
    private static boolean connectionStatus;
    // store all filenames to show in ListView
    private static List<String> values;
    private static ArrayAdapter<String> adapter;
    // list to store <filename, full URI with filename> pair
    private Map<String, String> selectedFiles;
    private Key publicKey;
    private Key privateKey;
    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private WebSocketClient mWebSocketClient;
    private SharedPreferences sp;
//    private static FloatingActionButton fab;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    public static byte[] generateKey(String password) throws Exception {
        byte[] keyStart = password.getBytes("UTF-8");

        KeyGenerator kgen = KeyGenerator.getInstance("AES");
        SecureRandom sr = SecureRandom.getInstance("SHA1PRNG", "Crypto");
        sr.setSeed(keyStart);
        kgen.init(128, sr);
        SecretKey skey = kgen.generateKey();
        return skey.getEncoded();
    }

    public static byte[] encodeFile(byte[] key, byte[] fileData) throws Exception {

        SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec);

        byte[] encrypted = cipher.doFinal(fileData);

        return encrypted;
    }

    public static byte[] decodeFile(byte[] key, byte[] fileData) throws Exception {
        SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, skeySpec);

        byte[] decrypted = cipher.doFinal(fileData);

        return decrypted;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
//        if (getIntent().hasExtra("key")) {
//            String symmkey = getIntent().getStringExtra("key");
//
//            path.clear();
//            path.add(getIntent().getStringExtra("path"));
//        }
        Log.e("title:;::::::::::", "" + toolbar.getTitle());
        connectionStatus = false;
//        connectWebSocket();
        selectedFiles = new TreeMap<>();
        values = new ArrayList<>();
        refreshList();

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        getSupportActionBar().setTitle(mSectionsPagerAdapter.getPageTitle(0));

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                Log.e("page listener", "onPageSelected");
                getSupportActionBar().setTitle(mSectionsPagerAdapter.getPageTitle(position));
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
        mViewPager.setAdapter(mSectionsPagerAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_select_files) {
            // open FileSelector
            pickFile();
        } else if (id == R.id.action_upload) {
            // send files in selectedFiles
//            if (connectionStatus) {
            // generated fresh fileKey for each file
            // upload the selectedFiles to google drive
            // send Enc(fileKey) to server
            try {
                sendFiles();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
//            } else {
//                Toast.makeText(HomeActivity.this, "Not connected to Secure Server", Toast.LENGTH_SHORT).show();
//            }
            return true;
        } else if (id == R.id.action_clear) {
            selectedFiles.clear();
            // refresh list of selected files
            refreshList();
        }
        return super.onOptionsItemSelected(item);
    }

    public void pickFile() {
        Intent pickFileIntent = new Intent(this, FileSelector.class);
        startActivityForResult(pickFileIntent, PICK_FILE_REQUEST);
    }

    public void sendFiles() throws Exception {
        // Original text
//        String theTestText = "This is just a simple test!";
        if (!selectedFiles.isEmpty()) {
            FileInputStream fileInputStream = null;
            Iterator<String> it = selectedFiles.keySet().iterator();
            File file = null;
            while (it.hasNext()) {
                file = new File(selectedFiles.get(it.next()));
            }

            byte[] bFile = new byte[(int) file.length()];

            //convert file into array of bytes
            fileInputStream = new FileInputStream(file);
            fileInputStream.read(bFile);
            fileInputStream.close();
//            Log.e("File Bytes", "" + new String(bFile));

            try {
                KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
                kpg.initialize(1024);
                KeyPair kp = kpg.genKeyPair();
                publicKey = kp.getPublic();
                privateKey = kp.getPrivate();
            } catch (Exception e) {
                Log.e(TAG, "RSA key pair error");
            }

//            // Encode the original data with RSA private key
//            byte[] encodedBytes = null;
//            try {
//                Cipher c = Cipher.getInstance("RSA");
//                c.init(Cipher.ENCRYPT_MODE, publicKey);
//                encodedBytes = c.doFinal(theTestText.getBytes());
//            } catch (Exception e) {
//                Log.e(TAG, "RSA encryption error");
//            }
            byte[] yourKey = generateKey("password");
            byte[] cipherText = encodeFile(yourKey, bFile);
            Log.e("Cipher Text", new String(cipherText));

            byte[] decodedData = decodeFile(yourKey, cipherText);
            Log.e("Plain Text", new String(decodedData));
        } else {
            Toast.makeText(HomeActivity.this, "Please select files to be uploaded!", Toast.LENGTH_SHORT).show();
        }
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
                Toast.makeText(HomeActivity.this, "Canceled", Toast.LENGTH_SHORT).show();
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
            uri = new URI("ws://http://localhost:8080/SecureServer/actions");
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return;
        }
        Log.e("Websocket", "init");
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
        Log.e("Websocket", "connecting");
        mWebSocketClient.connect();
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";
        private int fragNumber;

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            fragment.setFragNumber(sectionNumber);
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public void setFragNumber(int num) {
            fragNumber = num;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            int sectionNumber = getArguments().getInt(ARG_SECTION_NUMBER);
            Log.e("fragment", "onCreateView");
            View rootView = inflater.inflate(R.layout.fragment_home, container, false);
            TextView textView = (TextView) rootView.findViewById(R.id.section_label);
            textView.setText(getString(R.string.section_format, getArguments().getInt(ARG_SECTION_NUMBER)));
            ListView listView = (ListView) rootView.findViewById(R.id.selectedListOfFiles);
            adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, android.R.id.text1,
                    values);
            listView.setAdapter(adapter);
            return rootView;
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {
        private int sectionNumber;

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            Log.e("SectionPagerAdapter", "getItem: " + position);
            return PlaceholderFragment.newInstance(position);
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Google Drive";
                case 1:
                    return "Upload File";
            }
            return "";
        }
    }
}
