package com.example.google.playservices.placepicker;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;

import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;


import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationServices;


/**
 * Implementation of App Widget functionality.
 */
public class AppWidget extends AppWidgetProvider implements
        ConnectionCallbacks, OnConnectionFailedListener {
    public static String EXTRA_WORD =
            "com.example.google.playservices.placepicker.WORD";
    protected static final String TAG = "basic-location-sample";

    private static double TEST_PLACE_LAT, TEST_PLACE_LNG;
    double[] place;
    protected GoogleApiClient mGoogleApiClient;

    protected Location mLastLocation;
    public Context ttct;
    int[] appWidgetId;
    AppWidgetManager appWidgetManag;
    RemoteViews widget;
    @Override
    public void onUpdate(Context ctxt, AppWidgetManager appWidgetManager,
                         int[] appWidgetIds) {
        ttct = ctxt;
        appWidgetId = appWidgetIds;
        appWidgetManag = appWidgetManager;
        widget = new RemoteViews(ctxt.getPackageName(), R.layout.app_widget);
        buildGoogleApiClient();
        mGoogleApiClient.connect();





    }
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this.ttct)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }
    @Override
    public void onConnected(Bundle connectionHint) {
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            TEST_PLACE_LAT = mLastLocation.getLatitude();
            TEST_PLACE_LNG = mLastLocation.getLongitude();
            System.out.println("777777" + TEST_PLACE_LAT + "   " + TEST_PLACE_LNG);
            place = new double[]{TEST_PLACE_LAT, TEST_PLACE_LNG};
            for (int i = 0; i < appWidgetId.length; i++) {
            Intent svcIntent = new Intent(ttct, WidgetService.class);
            svcIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId[i]);
            svcIntent.putExtra("geoplace", place);
                 svcIntent.setData(Uri.parse(svcIntent.toUri(Intent.URI_INTENT_SCHEME)));



                widget.setRemoteAdapter(appWidgetId[i], R.id.words,
                        svcIntent);

                Intent clickIntent = new Intent(ttct, MainActivity.class);
                PendingIntent clickPI = PendingIntent
                        .getActivity(ttct, 0,
                                clickIntent,
                                PendingIntent.FLAG_UPDATE_CURRENT);

                 widget.setPendingIntentTemplate(R.id.words, clickPI);

                 appWidgetManag.updateAppWidget(appWidgetId[i], widget);
            }
            super.onUpdate(ttct, appWidgetManag, appWidgetId);


        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Connection suspended");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode()");
        mGoogleApiClient.connect();

    }
}






