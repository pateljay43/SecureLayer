package com.csulb.cscs579.securelayer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FileSelector extends Activity implements AdapterView.OnItemClickListener {
    // keeps track of currect directory
    private ArrayList<String> path;
    // stores every file's name in current directory
    private List<String> values;
    // adapter to fill the list view of UI element
    private ArrayAdapter<String> adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_selector);
        ListView listView = (ListView) findViewById(R.id.list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        // Use the current directory as title
        path = new ArrayList<>();
        path.add("/sdcard");
        if (getIntent().hasExtra("path")) {
            path.clear();
            path.add(getIntent().getStringExtra("path"));
        }
        values = new ArrayList<>();
        // generate data for the list
        refreshList();
        // Put the data into the list
        adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1, values);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);
    }

    /**
     * refresh the list to enlist new directory's files
     */
    private void refreshList() {
        setTitle(this.path.get(this.path.size() - 1));

        // Read all files sorted into the values-array
        values.clear();
        File dir = new File(this.path.get(this.path.size() - 1));
        if (!dir.canRead()) {
            setTitle(getTitle() + " (inaccessible)");
        }
        String[] list = dir.list();
        if (list != null) {
            for (String file : list) {
                if (!file.startsWith(".")) {
                    values.add(file);
                }
            }
        }
        Collections.sort(values);
    }


    /**
     * if the selected item is a file (not directory) then return it to main activity
     * else refresh the list with selected directory's files
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String filename = values.get(position);
        if (this.path.get(this.path.size() - 1).endsWith(File.separator)) {
            filename = this.path.get(this.path.size() - 1) + filename;
        } else {
            filename = this.path.get(this.path.size() - 1) + File.separator + filename;
        }
        if (new File(filename).isDirectory()) {
//            Toast.makeText(this, filename + " is a directory", Toast.LENGTH_LONG).show();
            this.path.add(filename);
            refreshList();
            // notify the list to be regenerated
            ((ArrayAdapter) parent.getAdapter()).notifyDataSetChanged();
        } else {
//            Toast.makeText(this, filename + " is not a directory", Toast.LENGTH_LONG).show();
            Intent returnIntent = new Intent();
            returnIntent.putExtra("result", new String[]{values.get(position), filename});
            setResult(RESULT_OK, returnIntent);
            finish();
        }
    }

    /**
     * return to previous directory or return to main activity if current directory is /sdcard
     */
    @Override
    public void onBackPressed() {
        if (this.path.get(this.path.size() - 1).equalsIgnoreCase("/sdcard")) {
            finish();
        } else {
            this.path.remove(this.path.size() - 1);
            refreshList();
            // notify the list to be regenerated
            this.adapter.notifyDataSetChanged();
        }
    }
}

