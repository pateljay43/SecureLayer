package com.csulb.cscs579.securelayer;

import android.content.Intent;
import android.content.SharedPreferences;
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

import java.security.Key;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

//import org.java_websocket.drafts.Draft_17;

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
    private SharedPreferences sp;
//    private static FloatingActionButton fab;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        connectionStatus = false;
        connectWebSocket();
        selectedFiles = new TreeMap<>();
        values = new ArrayList<>();
        refreshList();

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        getSupportActionBar().setTitle(mSectionsPagerAdapter.getPageTitle(0));

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        // change title in action bar based on the currently displayed page
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
            if (connectionStatus) {     // checks if application is still connected to Secure Server
                // generated fresh fileKey for each file
                // upload the selectedFiles to google drive
                // send Enc(fileKey) to server
                try {
                    sendFiles();
                } catch (Exception ex) {
                    Log.e("Exception", ex.getMessage());
                }
            } else {
                Toast.makeText(HomeActivity.this, "Not connected to Secure Server", Toast.LENGTH_SHORT).show();
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
     * Start another activity which allows to pick any file
     */
    public void pickFile() {
        Intent pickFileIntent = new Intent(this, FileSelector.class);
        startActivityForResult(pickFileIntent, PICK_FILE_REQUEST);
    }

    /**
     * Tries to send the selected files for upload
     */
    public void sendFiles() throws Exception {
        if (!selectedFiles.isEmpty()) {     // check if there are any seleted files to be sent
            // Ciphers and Hashes which will deal with file's encryption and checksum
        } else {
            Toast.makeText(HomeActivity.this, "Please select files to be uploaded!", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Method to handle encryption of the fileData using the key
     *
     * @param key      symmetric key for encrypting file
     * @param fileData file to be encrypted
     * @return encrypted file (cipher text)
     */
    public static byte[] encodeFile(byte[] key, byte[] fileData) throws Exception {
        return null;
    }

    /**
     * Method to handle decrypting of the fileData using the key
     *
     * @param key      symmetric key for decrypting file
     * @param fileData file to be decrypted
     * @return decrypted file (plain text)
     */
    public static byte[] decodeFile(byte[] key, byte[] fileData) throws Exception {
        return null;
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

    /**
     * When a user has selected a file from FileSelector it returns pair of filename and path to that file
     * That details is returned in data
     *
     * @param data data returned by another activity which was started for Result
     */
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

    /**
     * Tries to connect with Secure Server through secure web socket
     */
    private void connectWebSocket() {
        // still working on connection issues
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
            // Show total 2 pages.
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {     // names of the fragments
                case 0:
                    return "Google Drive";
                case 1:
                    return "Upload File";
            }
            return "";
        }
    }
}
