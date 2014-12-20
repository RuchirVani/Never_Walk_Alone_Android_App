package edu.uml.android.serviceexample;

import android.util.Log;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by Ruchir on 12/11/2014.
 */

    public class UpdateConnection extends Thread {
    String sendURL;
    private InputStream inputStream;
    String result = "";
    UpdateConnection(String Url) {
        sendURL = Url;
    }

    @Override
    public void run() {

        try {



            // create HttpClientloc
            HttpClient httpclient = new DefaultHttpClient();

            // make GET request to the given URL
            HttpResponse httpResponse = httpclient.execute(new HttpGet(sendURL));
            inputStream = httpResponse.getEntity().getContent();
            Log.i("Abc","inside Get method ");
            // convert inputstream to string

            if(inputStream != null)
                result=convertInputStreamToString(inputStream);
            else
                result = "Did not work!";

            Log.i("Abc",result);


        } catch (Exception e) {
            Log.i("Abc", "Exception" + e);
            //Log.d("InputStream", e.getLocalizedMessage());
            //Toast.makeText(getApplicationContext(), "No internet connectivity", Toast.LENGTH_SHORT).show();
        }

    }
    public static String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while((line = bufferedReader.readLine()) != null)
            result += line;

        inputStream.close();
        return result;

    }
    public static String resultFromRequest(String result)
    {
        return result;
    }
}
