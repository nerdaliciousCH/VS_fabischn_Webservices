package ch.ethz.inf.vs.a2.fabischn.webservices.sensor;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import java.io.PrintWriter;
import java.net.Socket;

import ch.ethz.inf.vs.a2.fabischn.webservices.HttpRawRequestImpl;
import ch.ethz.inf.vs.a2.fabischn.webservices.http.RemoteServerConfiguration;

/**
 * Created by fabian on 16.10.16.
 */

public class RawHttpSensor extends AbstractSensor {

    private static final String TAG = RawHttpSensor.class.getSimpleName();
    private HttpRawRequestImpl httpRawRequest;

    public RawHttpSensor(){
        httpRawRequest = new HttpRawRequestImpl();
    }

    @Override
    public String executeRequest() throws IOException{
        // http://stackoverflow.com/questions/10673684/send-http-request-manually-via-socket
        Socket socket = null;
        BufferedReader bufferedReader = null;
        String response = null;
        try{
         socket = new Socket(RemoteServerConfiguration.HOST, RemoteServerConfiguration.REST_PORT);
        PrintWriter printWriter = new PrintWriter(socket.getOutputStream());
        printWriter.write(httpRawRequest.generateRequest(RemoteServerConfiguration.HOST, RemoteServerConfiguration.REST_PORT, "/sunspots/Spot1/sensors/temperature").toCharArray());
        printWriter.flush();
            bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }
            response = stringBuilder.toString();
            Log.d(TAG, response);
        } catch(IOException e) {
            throw e;

        } finally{
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (socket != null) {
                socket.close();
            }
        }
        // might be null
        return response;
    }

    @Override
    public double parseResponse(String response) {
        if (response != null){

            return 1;
        } else {
            return 0;
        }
    }
}
