/*
* Copyright 2015 The Android Open Source Project
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


import android.app.Activity;
import android.app.Application;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;

import com.example.google.playservices.placepicker.cardstream.Card;
import com.example.google.playservices.placepicker.cardstream.CardStream;
import com.example.google.playservices.placepicker.cardstream.CardStreamFragment;
import com.example.google.playservices.placepicker.cardstream.OnCardClickListener;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Sample demonstrating the use of {@link PlacePicker}.
 * This sample shows the construction of an {@link Intent} to open the PlacePicker from the
 * Google Places API for Android and select a {@link Place}.
 *
 * This sample uses the CardStream sample template to create the UI for this demo, which is not
 * required to use the PlacePicker API. (Please see the Readme-CardStream.txt file for details.)
 *
 * @see com.google.android.gms.location.places.ui.PlacePicker.IntentBuilder
 * @see com.google.android.gms.location.places.ui.PlacePicker
 * @see com.google.android.gms.location.places.Place
 */
public class PlacePickerFragment extends Fragment implements OnCardClickListener {

    private static final String TAG = "NCHU POI";

    private CardStreamFragment mCards = null;

    // Buffer used to display list of place types for a place
    private final StringBuffer mPlaceTypeDisplayBuffer = new StringBuffer();

    // Tags for cards
    private static final String CARD_INTRO = "INTRO";
    private static final String CARD_PICKER = "PICKER";
    private static final String CARD_DETAIL = "DETAIL";

    /**
     * Action to launch the PlacePicker from a card. Identifies the card action.
     */
    private static final int ACTION_PICK_PLACE = 1;

    /**
     * Request code passed to the PlacePicker intent to identify its result when it returns.
     */
    private static final int REQUEST_PLACE_PICKER = 1;

    double[] place;
    List<String> placetel = new ArrayList<String>();
    List<String> placetime = new ArrayList<String>();
    ArrayList<String> PlaceDetail = new ArrayList<>();
    List<Bitmap> placephoto= new ArrayList<Bitmap>();
    ArrayList<String> distance= new ArrayList<String>();
    OnCardClickListener listener;
    ArrayList<String> description= new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        listener = this;



    }

    @Override
    public void onResume() {
        super.onResume();
        listener = this;
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            place = bundle.getDoubleArray("place");
        }
        CardStreamFragment stream = getCardStream();
        if (stream.getVisibleCardCount() < 1) {
            // No cards are visible, sample is started for the first time.
            // Prepare all cards and show the intro card.
            initialiseCards();

        }
        try {
            for(int i=0; i<stream.getVisibleCardCount(); ++i)
            getCardStream().hideCard(CARD_DETAIL + i);
            getPlaceTask getplaceTask = new getPlaceTask();
            getplaceTask.execute(place[0], place[1]);


        }catch (android.os.NetworkOnMainThreadException e ){

            System.out.print("456456"+e.toString());


        }
        // Check if cards are visible, at least the picker card is always shown.


    }

    @Override
    public void onCardClick(int cardActionId, String cardTag) {
        if (cardActionId == ACTION_PICK_PLACE) {
            // BEGIN_INCLUDE(intent)
            /* Use the PlacePicker Builder to construct an Intent.
            Note: This sample demonstrates a basic use case.
            The PlacePicker Builder supports additional properties such as search bounds.
             */
            try {
                PlacePicker.IntentBuilder intentBuilder = new PlacePicker.IntentBuilder();
                Intent intent = intentBuilder.build(getActivity());
                // Start the Intent by requesting a result, identified by a request code.
                startActivityForResult(intent, REQUEST_PLACE_PICKER);

                // Hide the pick option in the UI to prevent users from starting the picker
                // multiple times.
                showPickAction(false);

            } catch (GooglePlayServicesRepairableException e) {
                GooglePlayServicesUtil
                        .getErrorDialog(e.getConnectionStatusCode(), getActivity(), 0);
            } catch (GooglePlayServicesNotAvailableException e) {
                Toast.makeText(getActivity(), "Google Play Services is not available.",
                        Toast.LENGTH_LONG)
                        .show();
            }

            // END_INCLUDE(intent)
        }
        if(cardActionId==2){
            String info = description.get(Integer.parseInt(cardTag));
            new AlertDialogWrapper.Builder(this.getActivity())
                    .setTitle(R.string.info_title)
                    .setMessage(info)
                    .setNegativeButton(R.string.info_ckecked, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).show();

        }

    }

    /**
     * Extracts data from PlacePicker result.
     * This method is called when an Intent has been started by calling
     * {@link #startActivityForResult(android.content.Intent, int)}. The Intent for the
     * {@link com.google.android.gms.location.places.ui.PlacePicker} is started with
     * {@link #REQUEST_PLACE_PICKER} request code. When a result with this request code is received
     * in this method, its data is extracted by converting the Intent data to a {@link Place}
     * through the
     * {@link com.google.android.gms.location.places.ui.PlacePicker#getPlace(android.content.Intent,
     * android.content.Context)} call.
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // BEGIN_INCLUDE(activity_result)
        if (requestCode == REQUEST_PLACE_PICKER) {
            // This result is from the PlacePicker dialog.

            // Enable the picker option
            showPickAction(true);

            if (resultCode == Activity.RESULT_OK) {
                /* User has picked a place, extract data.
                   Data is extracted from the returned intent by retrieving a Place object from
                   the PlacePicker.
                 */
                final Place place = PlacePicker.getPlace(data, getActivity());

                /* A Place object contains details about that place, such as its name, address
                and phone number. Extract the name, address, phone number, place ID and place types.
                 */
                final CharSequence name = place.getName();
                final CharSequence address = place.getAddress();
                final CharSequence phone = place.getPhoneNumber();
                final String placeId = place.getId();
                String attribution = PlacePicker.getAttributions(data);
                if(attribution == null){
                    attribution = "";
                }

                // Update data on card.
                getCardStream().getCard(CARD_DETAIL)
                        .setTitle(name.toString())
                        .setDescription(getString(R.string.detail_text, placeId, address, phone,
                                attribution));

                // Print data to debug log
                Log.d(TAG, "Place selected: " + placeId + " (" + name.toString() + ")");

                // Show the card.
                getCardStream().showCard(CARD_DETAIL);

            } else {
                // User has not selected a place, hide the card.
                getCardStream().hideCard(CARD_DETAIL);
            }

        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
        // END_INCLUDE(activity_result)
    }

    /**
     * Initializes the picker and detail cards and adds them to the card stream.
     */
    private void initialiseCards() {

        // Add detail card.

        Card c = new Card.Builder(this, CARD_DETAIL)
                .setTitle("")
                .setDescription("")
                .build(getActivity());
        getCardStream().addCard(c, false);

        // Add and show introduction card.

        c = new Card.Builder(this, CARD_INTRO)
                .setTitle(getString(R.string.intro_title))
                .setDescription(getString(R.string.intro_message))
                .build(getActivity());
        getCardStream().addCard(c, true);



        c = new Card.Builder(listener, CARD_PICKER)
                .setTitle(getString(R.string.pick_title))
                .addAction(getString(R.string.pick_action), ACTION_PICK_PLACE, Card.ACTION_NEUTRAL)
                .setLayout(R.layout.card_google)
                .build(getActivity());
        getCardStream().addCard(c, false);


    }

    /**
     * Sets the visibility of the 'Pick Action' option on the 'Pick a place' card.
     * The action should be hidden when the PlacePicker Intent has been fired to prevent it from
     * being launched multiple times simultaneously.
     * @param show
     */
    private void showPickAction(boolean show){
        mCards.getCard(CARD_PICKER).setActionVisibility(ACTION_PICK_PLACE, show);
    }

    /**
     * Returns the CardStream.
     * @return
     */
    private CardStreamFragment getCardStream() {
        if (mCards == null) {
            mCards = ((CardStream) getActivity()).getCardStream();
        }
        return mCards;
    }
    public Bitmap resizebitmap(Bitmap bm){
        int width = bm.getWidth();

        int height = bm.getHeight();

        int newWidth = 350;

        int newHeight = 350;

        float scaleWidth = ((float) newWidth) / width;

        float scaleHeight = ((float) newHeight) / height;

        Matrix matrix = new Matrix();

        matrix.postScale(scaleWidth, scaleHeight);

        Bitmap newbm = Bitmap.createBitmap(bm, 0, 0, width, height, matrix,

                true);
        return newbm;
    }
    private Map<String, String> HTTPGetQuery(String hostURL) {
        // Declare a content string prepared for returning.
        String content = "";
        // Have an HTTP client to connect to the web service.
        HttpClient httpClient = new DefaultHttpClient();
        // Have an HTTP response container.
        HttpResponse httpResponse = null;
        // Have map container to store the information.
        Map<String, String> map = new HashMap<String, String>();

        // This try & catch is prepared for the IO exception in case.
        try {
            // Have a post method class with the query URL.
            HttpGet httpQuery = new HttpGet(hostURL);
            // The HTTP client do the query and have the string type response.
            httpResponse = httpClient.execute(httpQuery);

            // Read the HTTP headers and into content.
            //for (Header header : httpResponse.getAllHeaders()) {
            //     content += "\n" + header.toString();
            //}
            // Read the HTTP response content as an encoded string.
            content += EntityUtils.toString(httpResponse.getEntity());
        }
        // Catch the HTTP exception.
        catch(ClientProtocolException ex) {
            content = "ClientProtocolException:" + ex.getMessage();
        }
        // Catch the any IO exception.
        catch(IOException ex) {
            content = "IOException:" + ex.getMessage();
        }
        // The HTTP connection must be closed any way.
        finally    {
            httpClient.getConnectionManager().shutdown();
        }

        // Check the HTTP connection is executed or not.
        if (httpResponse != null) {
            // Put the status code with status key.
            map.put("status", Integer.toString(httpResponse.getStatusLine().getStatusCode()));
            // Put the response content with content key
            map.put("content", content);
        }
        else {
            // Put the dummy with status key.
            map.put("status", "");
            // Put the dummy with content key
            map.put("content", "");
        }

        // Return result.
        return map;
    }
    class getPlaceTask extends AsyncTask<Double, Integer,  Map> {
        List<String> placename = new ArrayList<String>();
        Map map = new HashMap();
        int total_place=0;
        @Override
        protected Map doInBackground(Double... position) {
            // TODO Auto-generated method stub

            map = HTTPGetQuery("http://140.120.13.89:8080/transaction.jsp?px=" + place[1] + "&py=" + place[0]);
            //map = HTTPGetQuery("http://140.120.13.89:8080/transaction.jsp?px=0.0&py=0.0");
            //map= HTTPGetQuery("http://140.120.13.89:8080/transaction.jsp?px=24.111&py=120.658");

            try {
                int progress=0;
                JSONObject placedata = new JSONObject(map.get("content").toString());
                JSONArray showaction = placedata.getJSONArray("showaction");
                JSONArray restaurant = placedata.getJSONArray("restaurant");
                JSONArray hotel = placedata.getJSONArray("hotel");
                JSONArray scenic = placedata.getJSONArray("scenic");
                total_place = showaction.length()+restaurant.length()+hotel.length()+scenic.length();
                if(showaction.length()>0) {
                    System.out.println("2222222" + showaction.length());
                    getDetail(placename, showaction, "showaction");
                    progress += showaction.length();
                    publishProgress((int) percent(progress, total_place));
                }
                if(restaurant.length()>0) {
                    System.out.println("1111111" + restaurant.length());
                    getDetail(placename, restaurant, "restaurant");
                    progress += showaction.length();

                    publishProgress((int) percent(progress, total_place));
                }
                if(scenic.length()>0) {
                    getDetail(placename, hotel, "hotel");
                    progress += showaction.length();
                    publishProgress((int) percent(progress, total_place));
                }
                if(hotel.length()>0) {
                    getDetail(placename, scenic, "scenic");
                    progress += showaction.length();
                    publishProgress((int) percent(progress, total_place));
                }

                publishProgress(-1);

            } catch (JSONException e) {
                e.printStackTrace();
            }
            return map;

        }

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
            placetime = new ArrayList<String>();
            placetel = new ArrayList<String>();
            placephoto = new ArrayList<>();
            PlaceDetail = new ArrayList<>();
            distance= new ArrayList<String>();

            Card c = new Card.Builder(listener, CARD_INTRO+"Loading")
                    .setProgressType(Card.PROGRESS_TYPE_NORMAL)
                    .setTitle("讀取中")
                    .setProgressMaxValue(100)
                    .setProgressLabel("讀取中")
                    .build(getActivity());
            getCardStream().addCard(c, true);
            getCardStream().showCard(CARD_INTRO + "Loading");


        }


        @Override
        protected void onProgressUpdate(Integer... values) {
            // TODO Auto-generated method stub
            super.onProgressUpdate(values);
            String info="";
            if(values[0]!=-1)
                info="讀取中..."+values[0]+"%";
            else
                info="目前無可推薦資訊";
            getCardStream().getCard(CARD_INTRO + "Loading")
                    .setActionAreaVisibility(isVisible())
                    .setProgressVisibility(isVisible())
                    .setProgress(50)
                    .setTitle(info)
                    .setProgressLabel("讀取中")
                    .setProgress(values[0]);
            System.out.println("456456" + getCardStream().getCard(CARD_INTRO + "Loading").getProgressType());


        }

        @Override
        protected void onPostExecute(Map map) {
            // TODO Auto-generated method stub
            super.onPostExecute(map);

            int len = placename.size();
            for (int i = 0; i < len; i++) {
                PlaceDetail.add(placename.get(i));
            }
            getCardStream().removeCard(CARD_INTRO + "Loading");

            getCardStream().showCard(CARD_PICKER, false);


            for (int i= 0; i<PlaceDetail.size(); i++) {
                final String name = PlaceDetail.get(i);
                final String phone = placetel.get(i);
                final String time = placetime.get(i);
                String place_distance = distance.get(i);
                Bitmap image = placephoto.get(i);

                getCardStream().removeCard(i+"");

                Card  d = new Card.Builder(listener, i+"")
                        .setTitle("")
                        .addAction("詳細資訊", 2, Card.ACTION_POSITIVE)
                        .setDescription("")
                        .setImage(image)
                        .build(getActivity());
                getCardStream().addCard(d, false);
                // Update data on card.
                getCardStream().getCard(i+"")
                    .setTitle(name)
                    .setDescription(getString(R.string.detail_text, time, place_distance, phone, ""));

                // Print data to debug log
                Log.d(TAG, "Place selected: "  + " (" + name.toString() + ")");

                // Show the card.
            getCardStream().showCard(i+"");

            }


        }

        @Override
        protected void onCancelled() {
            // TODO Auto-generated method stub
            super.onCancelled();

        }
    }
        public void getDetail(List<String> placename, JSONArray array, String placeClass) throws JSONException {
            for(int a=0; a<array.length(); a++){
                if(array.getJSONObject(a).get("title").toString().isEmpty()) {
                    placename.add(null);
                }else {
                    placename.add(array.getJSONObject(a).get("title").toString());
                }

                if(array.getJSONObject(a).get("distance").toString().isEmpty()) {
                    distance.add(null);
                }else{distance.add(array.getJSONObject(a).get("distance").toString());
                }
                if(array.getJSONObject(a).isNull("time")){
                    placetel.add(array.getJSONObject(a).get("tel").toString());
                    placetime.add("未提供");
                }else{
                    placetime.add(array.getJSONObject(a).get("time").toString());
                    placetel.add("未提供");
                }
                if(array.getJSONObject(a).get("description").toString().isEmpty()){
                    description.add("無額外資訊");
                }else{
                    description.add(array.getJSONObject(a).get("description").toString());

                }


                if(array.getJSONObject(a).get("picture").toString().isEmpty()){
                    placephoto.add(null);
                }else{

                    Bitmap bitmap = null;
                    try {
                        bitmap = BitmapFactory.decodeStream((InputStream) new URL(array.getJSONObject(a).get("picture").toString()).getContent());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if(bitmap!=null) {
                        if (bitmap.getWidth() != 350 || bitmap.getHeight() != 350) {
                            bitmap = resizebitmap(bitmap);

                        }
                        placephoto.add(bitmap);
                    }else{placephoto.add(null);}
                }
            }
        }
    private double percent(int numerator, int denominator) throws ArithmeticException {
        if(denominator == 0)
            throw new ArithmeticException("分母為 0 !!");
        else {
            double percent = (double)numerator/(double)denominator*10000;
            percent = Math.floor(percent + 0.5);

            return percent/100;
        }
    }


    }
