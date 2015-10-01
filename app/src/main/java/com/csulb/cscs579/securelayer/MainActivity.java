package com.csulb.cscs579.securelayer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class MainActivity extends Activity {
    // unique request-response identifier between activities
    private static final int PICK_FILE_REQUEST = 1;
    // list to store <filename, URI to file> pair
    private Map<String, String> selectedFiles;
    // store all filenames to show in ListView
    private List<String> values;
    // adapter to fill ListView element
    private ArrayAdapter<String> adapter;
    private FloatingActionButton addFileBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
        Intent pickFileIntent = new Intent(this, FileSelector.class);
        startActivityForResult(pickFileIntent, PICK_FILE_REQUEST);
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
//                Toast.makeText(MainActivity.this, "Canceled", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
