package com.annisasyaka.user.mobileftpclient;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPSClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by USER on 12/4/2018.
 */

public class Files extends Activity {
    FTPSClient ftps;
    ArrayList<String> folders = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_files);

        String[] fileName = null;
        FTPFile[] files;

        ftps = (FTPSClient)Globals.ftps;

        try{
            String pwd = ftps.printWorkingDirectory();
            if (pwd != null){
                TextView MyTextView = (TextView) findViewById(R.id.workindir);
                MyTextView.setText(pwd);
            }
            Log.d("Ftpspwd", pwd);
            fileName = ftps.listNames();

            files = ftps.listDirectories();
            for (FTPFile file : files){
                folders.add(file.getName());
            }

        } catch (IOException e) {
            Log.e("Error", e.getLocalizedMessage() );
            e.printStackTrace();
            Intent i = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(i);
        }

        final ListView listview = (ListView) findViewById(R.id.listview);
        final ArrayList<String> list = new ArrayList<String>();
        for (int i = 0; i < fileName.length; i++){
            list.add(fileName[i]);
        }
        final ArrayAdapter adapter = new StableArrayAdapter(this,
                android.R.layout.simple_list_item_1, list);
        listview.setAdapter(adapter);

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, final View view,
                                    int position, long id) {
                final String item = (String) parent.getItemAtPosition(position);
                view.animate().setDuration(1000).alpha((float)0.5).withEndAction(new Runnable() {
                            @Override
                            public void run() {
                                for (String folder : folders){
                                    if(item.equals(folder)){
                                        try{
                                            ftps.cwd(item);
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                            Intent i = new Intent(getApplicationContext(),
                                                    MainActivity.class);
                                            startActivity(i);
                                        }
                                        Intent i = new Intent(getApplicationContext(),
                                                Files.class);
                                        startActivity(i);
                                    }
                                }
                            }
                    });
            }
        });

        listview.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> parent,
                                           View view, int position, long id) {
                final String item = (String) parent.getItemAtPosition(position);
                view.animate().setDuration(1000).alpha((float)0.5)
                        .withEndAction(new Runnable() {
                            @Override
                            public void run() {
                                Intent i = new Intent(getApplicationContext(), Download.class);
                                i.putExtra("file",item);
                                for (String folder : folders){
                                    if (item.equals(folder)){
                                        i.putExtra("isDir", true);
                                    }
                                }
                                startActivity(i);
                            }
                        });
                return true;
            }
        });


    }
    @Override
    public void onBackPressed(){
        try {
            ftps.changeToParentDirectory();
        } catch (IOException e) {
            e.printStackTrace();
            Intent i = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(i);
        }
        Intent i = new Intent(getApplicationContext(), Files.class);
        startActivity(i);
    }

    private class StableArrayAdapter extends ArrayAdapter<String>{
        HashMap<String, Integer> mIdMap = new HashMap<String, Integer>();
        public StableArrayAdapter(Context context, int textViewResourceId,
                                  List<String> objects){
            super(context, textViewResourceId, objects);
            for (int i = 0; i < objects.size(); i++){
                mIdMap.put(objects.get(i), i);
            }
        }

        @Override
        public long getItemId(int position){
            String item = getItem(position);
            return mIdMap.get(item);
        }

        @Override
        public boolean hasStableIds(){
            return true;
        }
    }
}
