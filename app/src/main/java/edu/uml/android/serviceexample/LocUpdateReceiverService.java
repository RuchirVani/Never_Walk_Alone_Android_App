package edu.uml.android.serviceexample;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.location.Criteria;
import android.location.LocationManager;
import android.os.Handler;
import android.os.IBinder;
import android.widget.Toast;


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

/**
 * Created by takwale on 06/12/14.
 */
public class LocUpdateReceiverService extends Service {

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

        startTimer();                                         //This method is used for calling server request periodically on certain fix time.
        return super.onStartCommand(intent, flags, startId);


    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        timerTask.cancel();
    }



    /*
    We have created this Thread to fetch data from server as this task may take some long time and block main Thread too.
    So to avoid this this Thread has been created.
     */
        private class DownLoadLocationUpdates extends Thread{

            @Override
            public void run() {

                try {
                    //To store the result of server response
                    String result="";

                    String sendLocURL = "http://71.232.133.210/chat_recv_ajax.php?"+ApplicationData.USER_ID;
                    HttpClient httpclient = new DefaultHttpClient();

                    HttpResponse httpResponse = httpclient.execute(new HttpGet(sendLocURL));

                    // receive response as inputStream
                    inputStream = httpResponse.getEntity().getContent();

                    if(inputStream != null)
                        result=convertInputStreamToString(inputStream); // convert inputstream to string

                    else
                        result = "Did not work!";

                    /*
                      Now here to update the notifications for the location upadates, We check used the flag name "PANIC_NOTOFIED" and push notification accordingly.
                      */

                    //This condition checks that if the user has notified with the panic and other user has cancelled it than notification should be removed.
                    if ((result.split(" ")[2].equals("0")) && ApplicationData.PANIC_NOTIFIED)
                    {
                        pushNotification(0);
                        ApplicationData.PANIC_NOTIFIED=false;

                    }

                    //This condition checks that if user has not notified with panic than he/she should be notified.
                    if ((result.split(" ")[2].equals("1")) && !ApplicationData.PANIC_NOTIFIED)
                    {
                        pushNotification(1);
                        ApplicationData.PANIC_NOTIFIED=true;
                    }

                } catch (Exception e) {
                    //This to check if no Internet connectiviety than user can not connect to the server and can not make request.
                    Toast.makeText(getApplicationContext(),"No internet connectivity",Toast.LENGTH_SHORT).show();
                }

            }
        }

    public void startTimer() {
        final Handler handler = new Handler();
        Timer ourtimer = new Timer();
        timerTask = new TimerTask() {
            public void run() {
                handler.post(new Runnable() {
                    public void run() {

                        new DownLoadLocationUpdates().start();  //This the thread which collects the location update from server.


                    }
                });
            }
        };

        ourtimer.schedule(timerTask, 0, 10000);



    }

    /*
    This method is to put various kind of notifications on the application.
     */
    public void pushNotification(int choice)
    {
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

    if(choice==1)     // This for the Panic update
    {
        /*
        This notification will open the MAP for seeing the location updates of the sender
         */
        Intent intent = new Intent(getBaseContext(),ShowMap.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, 0);
        Notification n  = new Notification.Builder(this)
                .setContentTitle("Time to Check")
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentIntent(pIntent)
                .setDefaults(Notification.DEFAULT_SOUND).build();

        notificationManager.notify(0, n);


    }
    if(choice==0) notificationManager.cancel(0); // Panic has been canceled.


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



}


