package com.wordpress.laaptu;

import static com.wordpress.laaptu.DbConstants.DWord;
import static com.wordpress.laaptu.DbConstants.Word;
import static android.provider.BaseColumns._ID;
import static com.wordpress.laaptu.DbConstants.Time;

import java.text.SimpleDateFormat;
import java.util.Date;




import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

 
public class Access extends AccessibilityService {
	
    static final String TAG = "RecorderService";
    private DBHelper dbhelper = null;
    static String word[]=new String[10];
    String tmp;
    static String rankstr;
    private String getEventType(AccessibilityEvent event) {
    
        switch (event.getEventType()) {
            case AccessibilityEvent.TYPE_VIEW_CLICKED:
                return "TYPE_VIEW_CLICKED";
            case AccessibilityEvent.TYPE_VIEW_LONG_CLICKED:
                return "TYPE_VIEW_LONG_CLICKED";
            case AccessibilityEvent.TYPE_VIEW_SELECTED:
                return "TYPE_VIEW_SELECTED";
            case AccessibilityEvent.TYPE_VIEW_FOCUSED:
                return "TYPE_VIEW_FOCUSED";
            case AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED:
                return "TYPE_VIEW_TEXT_CHANGED";
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                return "TYPE_WINDOW_STATE_CHANGED";
            case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED:
                return "TYPE_NOTIFICATION_STATE_CHANGED";
            case AccessibilityEvent.TYPE_VIEW_HOVER_ENTER:
                return "TYPE_VIEW_HOVER_ENTER";
            case AccessibilityEvent.TYPE_VIEW_HOVER_EXIT:
                return "TYPE_VIEW_HOVER_EXIT";
            case AccessibilityEvent.TYPE_TOUCH_EXPLORATION_GESTURE_START:
                return "TYPE_TOUCH_EXPLORATION_GESTURE_START";
            case AccessibilityEvent.TYPE_TOUCH_EXPLORATION_GESTURE_END:
                return "TYPE_TOUCH_EXPLORATION_GESTURE_END";
            case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED:
                return "TYPE_WINDOW_CONTENT_CHANGED";
            case AccessibilityEvent.TYPE_VIEW_SCROLLED:
                return "TYPE_VIEW_SCROLLED";
            case AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED:
                return "TYPE_VIEW_TEXT_SELECTION_CHANGED";
            case AccessibilityEvent.TYPE_ANNOUNCEMENT:
                return "TYPE_ANNOUNCEMENT";
            case AccessibilityEvent.TYPE_VIEW_TEXT_TRAVERSED_AT_MOVEMENT_GRANULARITY:
                return "TYPE_VIEW_TEXT_TRAVERSED_AT_MOVEMENT_GRANULARITY";
            case AccessibilityEvent.TYPE_GESTURE_DETECTION_START:
                return "TYPE_GESTURE_DETECTION_START";
            case AccessibilityEvent.TYPE_GESTURE_DETECTION_END:
                return "TYPE_GESTURE_DETECTION_END";
            case AccessibilityEvent.TYPE_TOUCH_INTERACTION_START:
                return "TYPE_TOUCH_INTERACTION_START";
            case AccessibilityEvent.TYPE_TOUCH_INTERACTION_END:
                return "TYPE_TOUCH_INTERACTION_END";
                
        }
        return "default";
    }

    private String getEventText(AccessibilityEvent event) {
        StringBuilder sb = new StringBuilder();
        for (CharSequence s : event.getText()) {
            sb.append(s);
        }
        return sb.toString();
    }
 
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {

    	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    	Date current = new Date();
    	//Log.v("eeeeee", "if前");
    	if(getEventType(event).equals("TYPE_VIEW_CLICKED"))
    	{
    		//把句子POST到幫忙段詞的網站
    		Log.v("eee", "原本:"+getEventText(event));
    		new MyThread(getEventText(event),sdf.format(current)).start();
    	
    	}
    	
    	else if(getEventType(event).equals("TYPE_VIEW_TEXT_SELECTION_CHANGED"))
    	{
    		//判斷打字在後送出會清空(ex:LINE)
    		if(getEventText(event).length()==0)
    		{
    			new MyThread(getEventText(event),sdf.format(current)).start();
	    	}

    		tmp = getEventText(event);
    	}
    	int k = 0;
    	rankstr="";
    	Cursor cursor = getfavor();
    	while(cursor.moveToNext())
    	{
    		rankstr = rankstr+"第"+(k+1)+"名:"+cursor.getString(1)+"\t\t";
    		if(k%2==1)
    			rankstr = rankstr+"\n";
    		k++;
    	}
    	cursor.close();
    	
    	if(!getEventType(event).equals("TYPE_WINDOW_CONTENT_CHANGED"))
    		Log.v(TAG, String.format(
                "onAccessibilityEvent: [type] %s [class] %s [package] %s [time] %s [text] %s",
                getEventType(event), event.getClassName(), event.getPackageName(),
                event.getEventTime(), getEventText(event)));
    }
 
    @Override
    public void onInterrupt() {
       // Log.v(TAG, "onInterrupt");
    }
    
    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        //Log.v(TAG, "onServiceConnected");
        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        info.flags = AccessibilityServiceInfo.DEFAULT;
        info.eventTypes = AccessibilityEvent.TYPES_ALL_MASK;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
        dbhelper = new DBHelper(this); 
        
        setServiceInfo(info);
    }
    
    class MyThread extends Thread {

        private String Text;
        private String currentTime;
        public MyThread(String Text,String currentTime) {
            this.Text = Text;
            this.currentTime = currentTime;
        }

        @Override
        public void run() {
        	FindKeyWord(Text,currentTime);
        }
    }
    
    private void FindKeyWord(String Text,String currentTime){
		if(Text==null||Text.length()<=1)
			return;
    	SQLiteDatabase db = dbhelper.getReadableDatabase();
    	Log.v("findyou", "第一個字"+Text.substring(0, 2));
		Cursor cursor = db.rawQuery("select * from dictionary where word like ? ", new String[]{Text.substring(0, 2)+"%"});
		
		int wordsize = 0;
		String insertword="";
		while (cursor.moveToNext()) {
			int id = cursor.getInt(0); //獲取第一列的值,第一列的索引從0開始
			String tmpword = cursor.getString(1);//獲取第二列的值
			Log.v("select", tmpword);
			if(Text.contains(tmpword)&&wordsize<tmpword.length())
			{
				Log.v("findyou", "這時word被取代囉"+tmpword);
				wordsize=tmpword.length();
				insertword = tmpword;
			}
		}
		if(!insertword.equals(""))
		{
			db.execSQL("insert into favorite(time, word) values('"+currentTime+"','" +insertword+"')");
			Text = Text.substring(wordsize);
			
		}
		else 
			Text = Text.substring(1);
		Log.v("findyou", "剩下的字:"+Text+"長度:"+Text.length());
		
		cursor.close();
		//db.close();
		FindKeyWord(Text,currentTime);
	}
    
    
    private Cursor getCursor(){
    	SQLiteDatabase db = dbhelper.getReadableDatabase();
    	String[] columns = {_ID, DWord };
    	
    	Cursor cursor = db.query("dictionary", columns, null, null, null, null, null);
    	//startManagingCursor(cursor);
    	
    	return cursor;
    }
    private Cursor getfavor(){
    	SQLiteDatabase db = dbhelper.getReadableDatabase();
    	Cursor cursor = db.rawQuery("SELECT COUNT(*) as sum, word FROM favorite GROUP BY word ORDER BY sum DESC LIMIT 10 ", null);
    	
    	return cursor;
    }
	
   
}