/*
* Copyright 2013 The Android Open Source Project
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package com.example.google.playservices.placepicker;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.widget.Toast;

import com.example.android.common.activities.SampleActivityBase;

import com.example.android.common.logger.Log;
import com.example.google.playservices.placepicker.cardstream.CardStream;
import com.example.google.playservices.placepicker.cardstream.CardStreamFragment;
import com.example.google.playservices.placepicker.cardstream.CardStreamState;
import com.example.google.playservices.placepicker.cardstream.OnCardClickListener;
import com.example.google.playservices.placepicker.cardstream.StreamRetentionFragment;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.GeoDataApi;
import com.google.android.gms.location.places.PlaceDetectionApi;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBuffer;
import com.google.android.gms.location.places.Places;

public class MainActivity extends SampleActivityBase implements CardStream,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    public static final String TAG = "MainActivity";
    public static final String FRAGTAG = "PlacePickerFragment";

    private CardStreamFragment mCardStreamFragment;
    private static double TEST_PLACE_LAT, TEST_PLACE_LNG;
    double[] place;
    protected GoogleApiClient mGoogleApiClient;

    protected Location mLastLocation;
    private StreamRetentionFragment mRetentionFragment;
    private static final String RETENTION_TAG = "retention";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*
        String word=getIntent().getStringExtra(AppWidget.EXTRA_WORD);
        if (word==null) {
            word="We did not get a word!";
        }
        Toast.makeText(this, word, Toast.LENGTH_LONG).show();
        */



    }
    @Override
    protected void onResume() {
        super.onResume();
        buildGoogleApiClient();
        mGoogleApiClient.connect();
    }
    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    public CardStreamFragment getCardStream() {
        if (mCardStreamFragment == null) {
            mCardStreamFragment = (CardStreamFragment)
                    getSupportFragmentManager().findFragmentById(R.id.fragment_cardstream);
        }
        return mCardStreamFragment;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        CardStreamState state = getCardStream().dumpState();
        mRetentionFragment.storeCardStream(state);
    }
    @Override
    public void onConnected(Bundle connectionHint) {
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            TEST_PLACE_LAT = mLastLocation.getLatitude();
            TEST_PLACE_LNG = mLastLocation.getLongitude();
            System.out.println("777777" + TEST_PLACE_LAT + "   " + TEST_PLACE_LNG);
            place = new double[]{TEST_PLACE_LAT, TEST_PLACE_LNG};
            FragmentManager fm = getSupportFragmentManager();

            PlacePickerFragment fragment =
                    (PlacePickerFragment) fm.findFragmentByTag(FRAGTAG);
            if (fragment == null) {
                FragmentTransaction transaction = fm.beginTransaction();
                fragment = new PlacePickerFragment();
                Bundle bundle = new Bundle();
                bundle.putDoubleArray("place", place);
                fragment.setArguments(bundle);
                transaction.add(fragment, FRAGTAG);

                transaction.commit();
            }

            // Use fragment as click listener for cards, but must implement correct interface
            if (!(fragment instanceof OnCardClickListener)){
                throw new ClassCastException("PlacePickerFragment must " +
                        "implement OnCardClickListener interface.");
            }
            OnCardClickListener clickListener = (OnCardClickListener) fm.findFragmentByTag(FRAGTAG);

            mRetentionFragment = (StreamRetentionFragment) fm.findFragmentByTag(RETENTION_TAG);
            if (mRetentionFragment == null) {
                mRetentionFragment = new StreamRetentionFragment();
                fm.beginTransaction().add(mRetentionFragment, RETENTION_TAG).commit();
            } else {
                // If the retention fragment already existed, we need to pull some state.
                // pull state out
                CardStreamState state = mRetentionFragment.getCardStream();

                // dump it in CardStreamFragment.
                mCardStreamFragment =
                        (CardStreamFragment) fm.findFragmentById(R.id.fragment_cardstream);
                mCardStreamFragment.restoreState(state, clickListener);
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        android.util.Log.i(TAG, "Connection suspended");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        android.util.Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode()");
        mGoogleApiClient.connect();

    }
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }
}
