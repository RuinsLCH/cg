package com.wordpress.laaptu;

import static android.provider.BaseColumns._ID;
import static com.wordpress.laaptu.DbConstants.Word;
import static com.wordpress.laaptu.DbConstants.Time;

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class RawDataActivity extends Activity {
	
	private TextView RawText;
	private Button getButton;
	private DBHelper dbhelper = null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_raw_data);
		
		RawText = (TextView) findViewById(R.id.rawText01);
		getButton = (Button) findViewById(R.id.ButtonGet);
		
		dbhelper = new DBHelper(this); 
		//SQLiteDatabase db = dbhelper.getWritableDatabase();
        //db.execSQL("DELETE FROM favorite"); //delete all rows in a table
        //db.close();
        getButton.setOnClickListener(new Button.OnClickListener(){ 
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
            	// µù¥U±µ¦¬¾¹
        		
            	show();
        		
        		
            }         
        });
      
        
        
        
		
	}
	
	private Cursor getCursor(){
    	SQLiteDatabase db = dbhelper.getReadableDatabase();
    	String[] columns = {_ID, Time, Word };
    	
    	Cursor cursor = db.query("favorite", columns, null, null, null, null, null);
    	startManagingCursor(cursor);
    	
    	return cursor;
    }
	
	private void show(){
    	
    	Cursor cursor = getCursor();
    	
    	StringBuilder resultData = new StringBuilder("RESULT: \n");
    	
    	while(cursor.moveToNext()){
    		int id = cursor.getInt(0);
    		String time = cursor.getString(1);
    		String word = cursor.getString(2);
    		
    		resultData.append(id).append("\tword: {");
    		resultData.append(word).append("}\ttime: {");
    		resultData.append(time).append("}\n");
    		
    	}
    	
    	
    	RawText.setText(resultData);
    }
	
	
	
}
