package edu.uml.android.serviceexample;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;
import java.io.InputStream;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by takwale on 06/12/14.
 */
public class LocUpdateService extends Service {

    private TimerTask timerTask;
    LocationManager locationManager;
    Criteria criteria;
    String provider;
    private InputStream inputStream;
    double mylng,mylat;



    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        /*
        setting up the best provider location Manager to get Locations as well as constructing the locationmanger.
         */
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        criteria=new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        provider = locationManager.getBestProvider(criteria, false);

        /*
                This the timer to call the sending updates periodically.
         */
        startTimer();

        return super.onStartCommand(intent, flags, startId);


    }

    @Override
    public void onDestroy() {
        /*
        We are using this code to convey that user reached safely, as we don't have any coordinate with longitude=200 and Latitude=200 on map.
        So receiver will get idea that sender has reached his/her destination safely.
         */

        String sendEndUrl = "http://71.232.133.210/chat_send_ajax.php?Latitude=200&Longitude=200&Username="+ApplicationData.OBSERVER_ID+"&Panic=0";
        new UploadLocationUpdates(sendEndUrl).start();
        super.onDestroy();
        timerTask.cancel();

    }

    /*
    This the Thread used for uploading the location update without interrupting the main Thread. As it might block UI thread because of server operations.
     */

    private class UploadLocationUpdates extends Thread{
    String sendLocURL;
    UploadLocationUpdates(String Url)
            {
                sendLocURL=Url;
            }

            @Override
            public void run() {

                try {
                    String result="";
                    HttpClient httpclient = new DefaultHttpClient();
                    HttpResponse httpResponse = httpclient.execute(new HttpGet(sendLocURL));

                    /* This is to store the response of the server*/
                    inputStream = httpResponse.getEntity().getContent();
                    if(inputStream != null)
                        result="Worked";   // server response is not null than query has been fired correctly.
                    else
                        result = "Did not work!";

                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(),"No internet connectivity",Toast.LENGTH_SHORT).show();
                }

            }
        }

    /*
     This method to call updates on specific period of time
    */
    public void startTimer() {
        final Handler handler = new Handler();
        Timer ourtimer = new Timer();
        timerTask = new TimerTask() {
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        if(locationManager.getLastKnownLocation(provider)==null)
                        {
                            /*
                                    This exception might be generate if location provider disappeared.( That means probably your devices is not able to get location)
                             */
                            Toast.makeText(getApplicationContext(),"Location Update is not active",Toast.LENGTH_SHORT).show();

                        }
                        else {
                            locationManager.getLastKnownLocation(provider).getLatitude();
                            inputStream = null;
                            String result = "";
                            mylat = locationManager.getLastKnownLocation(provider).getLatitude();
                            mylng = locationManager.getLastKnownLocation(provider).getLongitude();
                            String sendLocURL = "http://71.232.133.210//chat_send_ajax.php?Latitude=" + mylat + "&Longitude=" + mylng + "&Username="+ApplicationData.OBSERVER_ID+"&Panic=" + ApplicationData.PANIC;
                            // Start an Thread to deal with the server sending service as it should not affect main thread.
                            new UploadLocationUpdates(sendLocURL).start();
                        }
                    }
                });
            }
        };
        // time to call the perodic update. This is in milli seconds.
        ourtimer.schedule(timerTask, 0, 10000);
    }


}


