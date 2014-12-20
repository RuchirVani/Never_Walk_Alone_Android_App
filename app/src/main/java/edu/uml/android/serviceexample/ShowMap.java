package edu.uml.android.serviceexample;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Timer;
import java.util.TimerTask;


public class ShowMap extends Activity {

    Intent locServIntent;
    private GoogleMap map;
    final LatLng olsen_hall = new LatLng(42.654584, -71.326950);
    private Marker marker;
    private TimerTask timerTask;
    private ProgressDialog dailog;
    private LatLng latlng;
    double panic;
    int count=0;
    MarkerOptions markeroptions;
    private double lastPanic=0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_activity5);
        locServIntent=new Intent(getApplicationContext(), LocUpdateReceiverService.class);
        dailog =ProgressDialog.show(ShowMap.this,"","Loading",true);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.my_activity5, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    protected void onResume() {
        super.onResume();
         /*
            This method to call location updates on specific period of time
        */
        startTimer();

        /*Here count is little tricky part-- as We planned to change the marker color on Panic we need to add this counter.
         because you can not update the marker's property once it has been created so we need to create new marker object on each panic change.
         but you can not simply increase the counter as it will go out of bound and application might crash.
         So here we increase the counter when it is zero. and uptil 2 only. let us see why.
         count =0 -- that means that location update has been not call yet so you can not put any marker on the map.
         count =1 -- That marker needs to be create or needs to be updated.
         count =2 -- You need to call Animate marker to animate the marker on the map.

         */

        if (count==1) {
            map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
            if(ApplicationData.PANIC_CHANGE==true&&marker!=null)
            {
                marker.remove();
            }
            markeroptions = new MarkerOptions();
            markeroptions.position(latlng);
            if(panic==1)
            {
                markeroptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.reddot));

            }
            else {
                markeroptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.greendot));
            }

            marker = map.addMarker(markeroptions);
            CameraUpdate update = CameraUpdateFactory.newLatLngZoom(latlng, 17);
            map.animateCamera(update);
        }

    }

    public void startTimer() {
        stopService(locServIntent);
        final Handler handler = new Handler();
        Timer ourtimer = new Timer();
        timerTask = new TimerTask() {
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                     new MarkerUpdateTask().execute("http://71.232.133.210/chat_recv_ajax.php?Username="+ApplicationData.USER_ID);
                    }
                });
            }};

        ourtimer.schedule(timerTask, 0, 3000);

    }

    /*
    Before updating the marker we need to do some calculation to update the marker.
    So for that we need to interact with the main thread with from the supporting thread.
    So we have used the Asynctask to update marker on screen.
     */
    class MarkerUpdateTask extends AsyncTask<String, Void, String> {

        protected String doInBackground(String... urls) {
            publishProgress();
            // This get method send the request and get response from the server. This is the time consuming process so we do it in background.
            return GET(urls[0]);
        }


        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);

        }

        protected void onPostExecute(String result) {


            /*
            To understand this read the comments of line81 in ShowMap.java
            */
            if(count==0||count==1)
                    count++;

                dailog.cancel();

                /*
                Get the values from the result we got.
                */
                double latitude=Double.parseDouble(result.split(" ")[1]);
                double longitude=Double.parseDouble(result.split(" ")[0]);

                /*
                As I discussed in LocUpadateService also this out of range coordinate so it can be used to detect the end of sending!!
                 */
                if(latitude==200&&longitude==200)
                {
                       Toast.makeText(getApplicationContext(),"Thanks!! I reached safely!!",Toast.LENGTH_SHORT).show();

                }
                panic=Double.parseDouble(result.split(" ")[2]);
                latlng=new LatLng(longitude,latitude);

                if(panic!=lastPanic)
                {
                    ApplicationData.PANIC_CHANGE=true;
                    count=1;
                }

                if(count==1)
                {
                    onResume();
                }

                lastPanic=panic;

                if(count>0)
                changeMarkerPosition(latlng);

        }


    }
    /*
    This is for the animation of the Marker on the screen.( On changing the marker position it will move smoothly by calculating the cure of that path.
    */
    private void changeMarkerPosition(LatLng latlng) {
        MarkerAnimation.animateMarkerToGB(marker,latlng,new LatLngInterpolator.Linear());

    }

    public  String GET(String url){
        InputStream inputStream = null;
        String result = "";
        try {

            HttpClient httpclient = new DefaultHttpClient();
            HttpResponse httpResponse = httpclient.execute(new HttpGet(url));
            inputStream = httpResponse.getEntity().getContent();
            if(inputStream != null)
                result = convertInputStreamToString(inputStream);
            else
                result = "Did not work!";


        } catch (Exception e) {
            Toast.makeText(getApplicationContext(),"No internet connectivity",Toast.LENGTH_SHORT).show();
        }

        return result;
    }
    /*
    This method has been created to convert the InputStream object to String so it can be parse. And can get the response
     */
    private static String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while((line = bufferedReader.readLine()) != null)
            result += line;

        inputStream.close();
        return result;

    }
    protected void onPause()
    {
        super.onPause();
        startService(locServIntent);
        timerTask.cancel();


    }

}
