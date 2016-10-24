package ch.ethz.inf.vs.a2.fabischn.webservices.sensor;

import android.text.Html;
import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

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
    public String executeRequest() throws Exception{
        // fabischn: How to use JAVA Socket API:
        // http://stackoverflow.com/questions/10673684/send-http-request-manually-via-socket
        Socket socket = null;
        PrintWriter printWriter = null;
        BufferedReader bufferedReader = null;
        String response = null;
        try{
         socket = new Socket(RemoteServerConfiguration.HOST, RemoteServerConfiguration.REST_PORT);
        printWriter = new PrintWriter(socket.getOutputStream());
        printWriter.write(httpRawRequest.generateRequest(RemoteServerConfiguration.HOST, RemoteServerConfiguration.REST_PORT, "/sunspots/Spot1/sensors/temperature").toCharArray());
        printWriter.flush();
        bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        StringBuffer buffer = new StringBuffer();
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            buffer.append(line);
        }
        response = buffer.toString();
        } catch(IOException e) {
            throw e;

        } finally{
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if(printWriter != null){
                printWriter.close();
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

            //fabischn: Using Jsoup library for HTML parsing
            Document html = Jsoup.parse(response);
            Elements elements = html.getElementsByClass("getterValue");
            Element element = elements.first();
            if (element != null) {
                return Double.parseDouble(element.html());
            } else{
                return Double.NEGATIVE_INFINITY;
            }
        } else {
            return Double.NEGATIVE_INFINITY;
        }
    }
}
