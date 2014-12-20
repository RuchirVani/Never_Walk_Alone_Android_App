package edu.uml.android.serviceexample;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.widget.Toast;

import java.util.LinkedList;

public class TapDetectionService extends Service {
    public TapDetectionService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
    public int onStartCommand(Intent intent, int flags, int startId) {
        /*
        Created Thread as checking the sensors data is time and memory consuming. so don't want to block the main Thread.
        */
        new CheckPanic().start();
        return super.onStartCommand(intent, flags, startId);
    }
    private class CheckPanic extends Thread implements SensorEventListener
    {

        private long lastUpdate;
        SensorManager sensorManager;
        Sensor accelerometer;
        int updatefrequency= SensorManager.SENSOR_DELAY_FASTEST;
        private LinkedList<Float> sensorEventList2= new LinkedList<Float>();
        private int strongThreshold=14;
        private int strongPositiveSample=0;
        private int strongNegativeSample=0;
        private int zMin,zMax=0;
        private int direction;
        private int prevDirection;
        private int dirChange;

        @Override
        public void run()
        {
            /*
            lastUpdate to get the last update and narrow the window. we have discussed the algorithm brifly in report
            */
            lastUpdate= System.currentTimeMillis();
            sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
            if (sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION) != null) {
                accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
                sensorManager.registerListener(this, accelerometer, updatefrequency);

            } else {
                Toast.makeText(getApplicationContext(), "No accelerometer", Toast.LENGTH_SHORT).show();
                 }

        }

        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            handleQueue(sensorEvent);


        }

        private void handleQueue(SensorEvent e) {

            if(sensorEventList2.size()==10)
            {
                float temp=sensorEventList2.remove();
                if(temp>strongThreshold)strongPositiveSample--;
                if(temp<(strongThreshold*-1))strongNegativeSample--;
            }

            sensorEventList2.add(e.values[2]);
            if(e.values[2]>strongThreshold)strongPositiveSample++;
            if(e.values[2]<(strongThreshold*-1))strongNegativeSample++;


            if(Math.abs(zMin - zMax)>10&& Math.abs(e.timestamp % 10000 - lastUpdate % 10000)>1)
            {

                zMin=0;
                zMax=0;

            }


            if(sensorEventList2.size()==10)
            {
                dirChange=0;
                for(int i=0;i<sensorEventList2.size()-1;i++)
                {
                    if(sensorEventList2.get(i+1)-sensorEventList2.get(i)>0)
                        direction=1;
                    else if(sensorEventList2.get(i+1)-sensorEventList2.get(i)<0)
                        direction=0;

                    if(prevDirection!=direction) dirChange++;
                    prevDirection=direction;

                }






            }

            if(dirChange>1 && (strongNegativeSample>1 && strongNegativeSample>1) )
            {
                ApplicationData.PANIC=1;
                panicSentNotification();

            }


        }

        /*
        Sent the Notification on detecting the Panic
        */
        private void panicSentNotification() {
             NotificationManager mNotificationManager = (NotificationManager)
                    getSystemService(NOTIFICATION_SERVICE);

            Intent dismissIntent = new Intent();
            PendingIntent piDismiss = PendingIntent.getBroadcast(getApplicationContext(), 0, dismissIntent, 0);
            Intent snoozeIntent = new Intent(getApplicationContext(), TapDetectionService.class);
            snoozeIntent.setAction("Snooze..");
            PendingIntent piSnooze = PendingIntent.getService(getApplicationContext(), 0, snoozeIntent, 0);
            Notification.Builder builder =
                    new Notification.Builder(getApplicationContext())
                            .setSmallIcon(R.drawable.common_signin_btn_icon_dark)
                            .setContentTitle("Panic Alarm Alert")
                            .setContentText("Click Dismiss to cancel Alert")
                            .setDefaults(Notification.DEFAULT_ALL)
                            .setStyle(new Notification.BigTextStyle()
                                    .bigText("Dismiss"))
                            .addAction (R.drawable.ic_stat_dismiss,              //For the Dismisss the false notification
                                    "Dismiss", piDismiss);
            mNotificationManager.notify(001, builder.build());

        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {

        }
    }
}
