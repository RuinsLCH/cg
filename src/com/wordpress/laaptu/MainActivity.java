package com.wordpress.laaptu;

import android.os.Bundle;
import android.util.Log;

import static com.wordpress.laaptu.DbConstants.DWord;

import java.io.*;
import java.util.HashSet;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.view.View;
import android.widget.Button;

public class MainActivity extends Activity {
	
	private Button buttonAssist,buttonRaw,button04;
	static HashSet<String> dictionary;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		buttonAssist = (Button)findViewById(R.id.Button01);
		buttonRaw = (Button)findViewById(R.id.Button02);
		button04 = (Button)findViewById(R.id.Button04);
		dictionary = new HashSet<String>();
		
		buttonRaw.setOnClickListener(new Button.OnClickListener(){ 
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
            	Intent intent = new Intent();
            	intent.setClass(MainActivity.this, RawDataActivity.class);
        		startActivity(intent);             
            }         
        }); 
		
		
		
		buttonAssist.setOnClickListener(new Button.OnClickListener(){ 
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
            	Intent intent = new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS);
        		startActivityForResult(intent, 0);             
            }         
        }); 
		int i=0;
		final String PREFS_NAME = "MyPrefsFile";

		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);

		if (settings.getBoolean("my_first_time", true)) {
		    //the app is being launched for first time, do something        
		    Log.d("Comments", "First time");
		    InputStream is;
			try {
				is = getAssets().open("word_2.0.txt");
				if (is != null){
			    	   BufferedReader BufferedReaderd = new BufferedReader (new InputStreamReader(is, "UTF-8"));
			    	   //INSERT IGNORE INTO `table_1` (`name`) SELECT `name` FROM `table_2`;
			    	   //int max=0;
			    	   while(BufferedReaderd.ready()) {
			    		   
			    		  String tmp=BufferedReaderd.readLine();
			    		 // if(max<tmp.length())
			    		//	  max=tmp.length();
			    		  Log.v("xdxdx", String.valueOf(i++)+tmp);
			              dictionary.add(tmp);
			    	   }
			    	  // Log.v("Time:", String.valueOf(max));
			    	   Log.v("xdxdx", "´¡§¹¤F song");
			    	   }
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    	
		
		             // first time task

		    // record the fact that the app has been started at least once
		    settings.edit().putBoolean("my_first_time", false).commit(); 
		}
		
		   SharedPreferences preferences = getSharedPreferences("count",MODE_WORLD_READABLE);
		
	       int count = preferences.getInt("count", 0);
	       if (count == 0) {
	    	   StringBuilder sb = new StringBuilder("");
	    	   String content ="";
	    	  
	    	   
	    	   
	       }
		
	

	
	}
}
