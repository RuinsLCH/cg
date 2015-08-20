package com.example.google.playservices.placepicker;

import android.content.Intent;
import android.widget.RemoteViewsService;

/**
 * Created by awin on 15/7/15.
 */
public class WidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return(new WidgetViewFactory(this.getApplicationContext(),
                intent));
    }
}