package ch.ethz.inf.vs.a2.fabischn.webservices.sensor;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import ch.ethz.inf.vs.a2.fabischn.webservices.http.RemoteServerConfiguration;

/**
 * Created by fabian on 16.10.16.
 */

public class TextSensor extends AbstractSensor {

    private static final String TAG = TextSensor.class.getSimpleName();

    //fabischn: Boilerplate code for HttpURLConnections from
    // https://github.com/udacity/Sunshine-Version-2/blob/sunshine_master/app/src/main/java/com/example/android/sunshine/app/sync/SunshineSyncAdapter.java
    @Override
    public String executeRequest() throws Exception{
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        URL url = new URL("http",
                RemoteServerConfiguration.HOST,
                RemoteServerConfiguration.REST_PORT,
                "/sunspots/Spot1/sensors/temperature");


        urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setRequestMethod("GET");
        urlConnection.setRequestProperty("Accept", "text/plain");
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
        // TODO safety and sanity checks
        return Double.parseDouble(response);
    }
}
