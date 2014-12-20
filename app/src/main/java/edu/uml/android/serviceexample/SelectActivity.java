package edu.uml.android.serviceexample;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;


public class SelectActivity extends Activity {

    private InputStream inputStream;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select);

        /*
        To check that does this id have any request for the seeing location updates?
        This cant be done on main Thread as it has the server included in it so
        created AsyncTask to get that details and push notification for that.
        */

        class MarkerUpdateTask1 extends AsyncTask<String, Void, String> {


            protected String doInBackground(String... urls) {
                publishProgress();
                return GET(urls[0]);
            }
            @Override
            protected void onProgressUpdate(Void... values) {
                super.onProgressUpdate(values);

            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);

                // here check the response that it doesn't have any exception from server so that query worked fine and get the row for that
                if (!s.split(" ")[0].equals("<br"))
                    pushNotification(1);
            }
        }

        new MarkerUpdateTask1().execute("http://71.232.133.210/check_Connected_users.php?Username="+ApplicationData.USER_ID);


            }


    /*
    Same Notification Config as the LocUpdateReceiverService
     */
    public void pushNotification(int choice)
    {
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        if(choice==1)
        {

            Intent intent = new Intent(getBaseContext(),ShowMap.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, 0);
            Log.i("inside","pushnotification");
                     Notification n  = new Notification.Builder(this)
                    .setContentTitle("Click to see map")
                    .setSmallIcon(R.drawable.ic_launcher)
                    .setContentIntent(pIntent)
                    .setDefaults(Notification.DEFAULT_SOUND).build();




            notificationManager.notify(0, n);


        }
        if(choice==0) notificationManager.cancel(0);


    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.select, menu);
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

    @Override
    protected void onResume() {
        super.onResume();
        /*
         This is the implementation of Listener for the Button.
         */

        findViewById(R.id.Watch_Me).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),Sending_Location.class));
            }
        });
        findViewById(R.id.will_watch_you).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),ShowMap.class));
            }
        });

    }
    private static String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while((line = bufferedReader.readLine()) != null)
            result += line;

        inputStream.close();
        return result;


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
            Toast.makeText(getApplicationContext(), "No internet connectivity", Toast.LENGTH_SHORT).show();
                    }

        return result;
    }

}
