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
        toolbar.setTitle("Select File");

        // Use the current directory as title
        path = new ArrayList<>();
        path.add("/");
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
        String fileURI = values.get(position);  // currently just name of the file.extension
        if (this.path.get(this.path.size() - 1).endsWith(File.separator)) {
            fileURI = this.path.get(this.path.size() - 1) + fileURI;
        } else {
            fileURI = this.path.get(this.path.size() - 1) + File.separator + fileURI;
        }
        if (new File(fileURI).isDirectory()) {
//            Toast.makeText(this, fileURI + " is a directory", Toast.LENGTH_LONG).show();
            this.path.add(fileURI);
            refreshList();
            // notify the list to be regenerated
            ((ArrayAdapter) parent.getAdapter()).notifyDataSetChanged();
        } else {
//            Toast.makeText(this, fileURI + " is not a directory", Toast.LENGTH_LONG).show();
            Intent returnIntent = new Intent();
            returnIntent.putExtra("result", new String[]{values.get(position), fileURI});
            setResult(RESULT_OK, returnIntent);
            finish();
        }
    }

    /**
     * return to previous directory or return to main activity if current directory is /sdard
     */
    @Override
    public void onBackPressed() {
        if (this.path.get(this.path.size() - 1).equalsIgnoreCase("/")) {
            finish();
        } else {
            this.path.remove(this.path.size() - 1);
            refreshList();
            // notify the list to be regenerated
            this.adapter.notifyDataSetChanged();
        }
    }
}

