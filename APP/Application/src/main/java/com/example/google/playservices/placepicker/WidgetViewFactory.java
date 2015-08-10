package com.example.google.playservices.placepicker;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;


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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


import se.walkercrou.places.GooglePlaces;
import se.walkercrou.places.GooglePlacesInterface;
import se.walkercrou.places.Photo;
import se.walkercrou.places.Place;

/**
 * Created by awin on 15/7/15.
 */
public class WidgetViewFactory implements RemoteViewsService.RemoteViewsFactory {

    private Context ctxt=null;
    private int appWidgetId;
    static double[] place;
    private static final int TYPE_MAX_COUNT =  2;
    static List<String> placereview = new ArrayList<String>();
    AppWidgetManager appWidgetManager;
    static ArrayList<String> PlaceDetail = new ArrayList<>();
    static List<Bitmap> placephoto= new ArrayList<Bitmap>();
    static ArrayList<String> distance= new ArrayList<String>();
    Intent myintent;
    //Map<String, Bitmap> list_contain = new HashMap<String, Bitmap>();;
    //String api_key="AIzaSyDmn3xZuAYmcx1FtdNjvtlUc17314q2YEc";
    ///String api_key="AIzaSyA0ZkS5y3B_SE9kWj-jUekIib49yN11Xwo";
    //String api_key="AIzaSyDQQD7UzswrQirACXhD5y5ALogU-19hEp4";
    public WidgetViewFactory(Context ctxt, Intent intent) {
        this.ctxt=ctxt;
        myintent=intent;
        appWidgetManager = AppWidgetManager.getInstance(ctxt);
        appWidgetId=myintent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);
        try {
            place = myintent.getDoubleArrayExtra("geoplace");

            getPlaceTask getplaceTask = new getPlaceTask();
            getplaceTask.execute(place[0], place[1], 500.0);


        }catch (android.os.NetworkOnMainThreadException e ){

        }
    }

    @Override
    public void onCreate() {
        // no-op

    }

    @Override
    public void onDestroy() {
        // no-op
    }

    @Override
    public int getCount() {
        return(PlaceDetail.size()+1);
    }



    @Override
    public RemoteViews getViewAt(int position) {
        if(position == 0){
            RemoteViews row = new RemoteViews(ctxt.getPackageName(),
                    R.layout.weather_item);
            Intent i = new Intent();
            Bundle extras = new Bundle();
            extras.putString(AppWidget.EXTRA_WORD, String.valueOf("weather"));
            i.putExtras(extras);
            row.setOnClickFillInIntent(R.id.weather_relativeLayout, i);

            return (row);

        }else {
            RemoteViews row = new RemoteViews(ctxt.getPackageName(),
                    R.layout.row);

            row.setTextViewText(R.id.position_name, String.valueOf(PlaceDetail.get(position - 1)));
            System.out.println("Title:" + String.valueOf(PlaceDetail.get(position - 1)));

            if(placephoto.get(position - 1)==null)
                row.setViewVisibility(R.id.imgViewLogo, View.GONE);
            else
                row.setImageViewBitmap(R.id.imgViewLogo, placephoto.get(position - 1));

            row.setTextViewText(R.id.distance, String.valueOf("距離："+distance.get(position - 1)+"公里"));
            System.out.println("Distance:" + String.valueOf("距離：" + distance.get(position - 1) + "公里"));

            if(String.valueOf(placereview.get(position - 1)).equals("未提供")||String.valueOf(placereview.get(position - 1)).equals("無資料")) {
                row.setTextColor(R.id.review, Color.parseColor("#F36D70"));
                row.setTextViewText(R.id.review, String.valueOf("電話：" + placereview.get(position - 1)));
                System.out.println("Tel:" + String.valueOf("電話：" + placereview.get(position - 1)));

            } else {
                row.setTextColor(R.id.review, Color.parseColor("#0aba89"));
                row.setTextViewText(R.id.review, String.valueOf("時間：" + placereview.get(position - 1)));
                System.out.println("Time:" + String.valueOf("時間：" + placereview.get(position - 1)));

            }
            Intent i = new Intent();
            Bundle extras = new Bundle();
            extras.putString(AppWidget.EXTRA_WORD, String.valueOf(PlaceDetail.get(position-1)));
            i.putExtras(extras);
            row.setOnClickFillInIntent(R.id.place_relativeLayout, i);

            return (row);
        }
    }

    @Override
    public RemoteViews getLoadingView() {

        return(null);
    }

    @Override
    public int getViewTypeCount() {
        return(TYPE_MAX_COUNT);
    }

    @Override
    public long getItemId(int position) {
        return(position);
    }


    @Override
    public boolean hasStableIds() {
        return(true);
    }

    @Override
    public void onDataSetChanged() {

    }
    public Bitmap resizebitmap(Bitmap bm){
        int width = bm.getWidth();

        int height = bm.getHeight();

        int newWidth = 380;

        int newHeight = 380;

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
            System.out.println("Query URL:"+hostURL);
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


    public void getDetail(List<String> placename, JSONArray array) throws JSONException {
        for(int a=0; a<array.length(); a++){
            placename.add(array.getJSONObject(a).get("title").toString());
            distance.add(array.getJSONObject(a).get("distance").toString());
            if(array.getJSONObject(a).isNull("time")){
                placereview.add(array.getJSONObject(a).get("tel").toString());
            }else{
                placereview.add(array.getJSONObject(a).get("time").toString());
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
                    }else{placename.add(null);}
                }
            }
        }



    class getPlaceTask extends AsyncTask<Double, Integer,  Map> {
        List<String> placename = new ArrayList<String>();
        Map map=new HashMap();
        @Override
        protected  Map doInBackground(Double... position) {
            // TODO Auto-generated method stub
            Double lat = position[0], lng = position[1], radius = position[2];

            //list_contain = new HashMap<String, Bitmap>();;
            /*
            GooglePlaces client = new GooglePlaces(api_key);
            client.setDebugModeEnabled(true);
            List<Place> places = client.getNearbyPlaces(lat, lng, 1500, GooglePlacesInterface.MAXIMUM_RESULTS);
            if (places.size() >0) {
                for (Place empireStateBuilding : places) {

                    Place detailedEmpireStateBuilding = empireStateBuilding.getDetails();
                    List<Photo> photos = detailedEmpireStateBuilding.getPhotos();// sends a GET request for more details
                    if (!photos.isEmpty()) {
                        Photo photo = photos.get(0);
                        InputStream stream = photo.download(350, 350).getInputStream();
                        Bitmap bitmap = BitmapFactory.decodeStream(stream);
                        if (bitmap.getWidth() != 350 || bitmap.getHeight() != 350) {
                            bitmap = resizebitmap(bitmap);

                        }
                        placephoto.add(bitmap);

                        // Just an example of the amount of information at your disposal:
                        System.out.println("Name: " + detailedEmpireStateBuilding.getName());
                        placename.add(detailedEmpireStateBuilding.getName());

                        System.out.println("Phone: " + detailedEmpireStateBuilding.getPhoneNumber());
                        System.out.println("International Phone: " + empireStateBuilding.getInternationalPhoneNumber());
                        System.out.println("Website: " + detailedEmpireStateBuilding.getWebsite());
                        System.out.println("PlaceId: " + detailedEmpireStateBuilding.getPlaceId());
                        System.out.println("Always Opened: " + detailedEmpireStateBuilding.isAlwaysOpened());
                        System.out.println("Status: " + detailedEmpireStateBuilding.getStatus());
                        System.out.println("Google Place URL: " + detailedEmpireStateBuilding.getGoogleUrl());
                        System.out.println("Price: " + detailedEmpireStateBuilding.getPrice());
                        System.out.println("Address: " + detailedEmpireStateBuilding.getAddress());
                        System.out.println("Vicinity: " + detailedEmpireStateBuilding.getVicinity());
                        System.out.println("Reviews: " + detailedEmpireStateBuilding.getReviews().size());
                        placereview.add(detailedEmpireStateBuilding.getReviews().size() + "");
                        System.out.println("Hours:\n " + detailedEmpireStateBuilding.getHours());

                    }
                }
            }
            */
            placereview = new ArrayList<String>();
            placephoto = new ArrayList<>();
            //map= HTTPGetQuery("http://140.120.13.89:8080/transaction.jsp?px="+place[1]+"&py="+place[0]);
            map= HTTPGetQuery("http://140.120.13.89:8080/transaction.jsp?px=24.111&py=120.658");
            try {
                JSONObject placedata = new JSONObject(map.get("content").toString());
                System.out.println("12311"+map.get("content").toString());
                JSONArray showaction = placedata.getJSONArray("showaction");
                JSONArray restaurant = placedata.getJSONArray("restaurant");
                JSONArray hotel = placedata.getJSONArray("hotel");
                JSONArray scenic = placedata.getJSONArray("scenic");
                if(showaction.length()>0)
                    getDetail(placename,showaction);
                if(restaurant.length()>0)
                    getDetail(placename,restaurant);
                if(hotel.length()>0)
                    getDetail(placename,hotel);
                if(scenic.length()>0)
                    getDetail(placename,scenic);




            } catch (JSONException e) {
                e.printStackTrace();
            }
            return map;

        }

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();

            RemoteViews temp = new RemoteViews(ctxt.getPackageName(), R.layout.app_widget);
            temp.setViewVisibility(R.id.progress_bar, View.VISIBLE);
            appWidgetManager.updateAppWidget(appWidgetId, temp);



        }


        @Override
        protected void onProgressUpdate(Integer... values) {
            // TODO Auto-generated method stub
            super.onProgressUpdate(values);




        }

        @Override
        protected void onPostExecute( Map map) {
            // TODO Auto-generated method stub
            super.onPostExecute(map);
            int len = placename.size();
            for(int i=0; i<len; i++) {
                PlaceDetail.add(placename.get(i));

            }
            RemoteViews temp = new RemoteViews(ctxt.getPackageName(), R.layout.app_widget);
            temp.setViewVisibility(R.id.progress_bar, View.GONE);
            appWidgetManager.updateAppWidget(appWidgetId, temp);
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.words);


        }

        @Override
        protected void onCancelled() {
            // TODO Auto-generated method stub
            super.onCancelled();

        }

    }
}
