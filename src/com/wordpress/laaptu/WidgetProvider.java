package com.wordpress.laaptu;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;


import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.ListView;
import android.widget.RemoteViews;


//import¬Ù²¤   
public class WidgetProvider extends AppWidgetProvider { 
 
Context context_main ; 
AppWidgetManager app_manager; 
int []appWidgetId;

@Override 
public void onUpdate(Context context, AppWidgetManager appWidgetManager, 
  int[] appWidgetIds) { 
 super.onUpdate(context, appWidgetManager, appWidgetIds); 
  
 app_manager = appWidgetManager; 
 context_main = context; 
 appWidgetId = appWidgetIds; 
 
  
    Thread thread = new Thread(new update_thread()); 
    thread.start(); 

} 
 
public class update_thread implements Runnable{ 



@Override 
 public void run() { 
  while(true)
  { 
    RemoteViews updateViews = new RemoteViews(context_main.getPackageName(), R.layout.widget_layout);
    //updateViews.setRemoteAdapter(appWidgetId, R.id.listA,intent);
    updateViews.setTextViewText(R.id.rank, Access.rankstr);
    app_manager.updateAppWidget(appWidgetId, updateViews);
    try {
		Thread.sleep(600000);
	} catch (InterruptedException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
  }       
 }      
	}

@Override
public void onReceive(Context context, Intent intent) {
    // TODO Auto-generated method stub
    super.onReceive(context, intent);

}
protected PendingIntent getPendingSelfIntent(Context context, String string) {
	// TODO Auto-generated method stub
	Intent intent = new Intent(context, getClass());
    intent.setAction(string);
    return PendingIntent.getBroadcast(context, 0, intent, 0);
}



}    




