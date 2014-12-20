package edu.uml.android.serviceexample;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


public class Sending_Location extends Activity {

    private Intent locSenderIntent,locReceiverIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_sending__location);
        locSenderIntent=new Intent(getApplicationContext(), LocUpdateService.class);
        locReceiverIntent=new Intent(getApplicationContext(), LocUpdateReceiverService.class);

        /* Here on Click of the Button it will check that if that is sending the updates than again clicking on it will stop the location update.
        and if the location update has not been started yet than you can start now.
         */
        findViewById(R.id.send_location_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if((((Button)findViewById(R.id.send_location_button)).getText().toString().equals("Send"))&&(!((TextView)findViewById(R.id.observer_id)).getText().toString().equals(""))) {

                    ApplicationData.OBSERVER_ID=((TextView)findViewById(R.id.observer_id)).getText().toString();
                    new UpdateConnection("http://71.232.133.210/add_user_connection.php?Sender="+ApplicationData.USER_ID+"&Observer="+ApplicationData.OBSERVER_ID).start();
                    stopService(locReceiverIntent);
                    ApplicationData.SENDING_LOCATION_UPDATE = true;
                    startService(locSenderIntent);

                    startService(new Intent(getApplicationContext(), TapDetectionService.class));
                    if(ApplicationData.SENDING_LOCATION_UPDATE) {
                        ((Button) findViewById(R.id.send_location_button)).setText("Stop Location Update");


                    }

                }
                else {
                    ApplicationData.SENDING_LOCATION_UPDATE=false;
                    ((Button)findViewById(R.id.send_location_button)).setText("Send");
                    stopService(locSenderIntent);
                    stopService(new Intent(getApplicationContext(), TapDetectionService.class));
                    new UpdateConnection("http://71.232.133.210/delete_user_connection.php?Sender="+ApplicationData.USER_ID+"&Observer="+ApplicationData.OBSERVER_ID).start();
                }

            }
        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.sending__location, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(ApplicationData.SENDING_LOCATION_UPDATE) {
            ((Button) findViewById(R.id.start)).setText("Stop Location Update");

        }

    }


    }





