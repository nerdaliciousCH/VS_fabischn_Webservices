package ch.ethz.inf.vs.a2.fabischn.webservices.sensor;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import ch.ethz.inf.vs.a2.fabischn.webservices.http.RemoteServerConfiguration;

/**
 * Created by fabian on 16.10.16.
 */

public class JsonSensor extends AbstractSensor {

    private final static String TAG = JsonSensor.class.getSimpleName();

    @Override
    public String executeRequest() throws Exception {
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        URL url = new URL("http",
                RemoteServerConfiguration.HOST,
                RemoteServerConfiguration.REST_PORT,
                "/sunspots/Spot1/sensors/temperature");


        urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setRequestMethod("GET");
        urlConnection.setRequestProperty("Accept", "application/json");
        urlConnection.setRequestProperty("Connection", "close");

        urlConnection.connect();

        InputStream inputStream = urlConnection.getInputStream();
        StringBuffer buffer = new StringBuffer();
        reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        while ((line = reader.readLine()) != null) {
            buffer.append(line);
        }
        // TODO safety and sanity checks
        return buffer.toString();
    }

    @Override
    public double parseResponse(String response) {
        //fabischn: https://www.tutorialspoint.com/json/json_java_example.htm
        double temp = Double.NEGATIVE_INFINITY;
        try {
            JSONObject json = new JSONObject(response);
            temp = json.getDouble("value");
        } catch (JSONException e){
            Log.e(TAG, "Exploded while reading 'JSON'...", e);
        }
        return temp;
    }
}
