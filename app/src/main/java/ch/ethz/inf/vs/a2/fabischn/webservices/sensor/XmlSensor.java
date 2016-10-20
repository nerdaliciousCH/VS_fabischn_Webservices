package ch.ethz.inf.vs.a2.fabischn.webservices.sensor;

import android.util.Log;

import org.ksoap2.serialization.SoapObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;

import ch.ethz.inf.vs.a2.fabischn.webservices.http.RemoteServerConfiguration;

/**
 * Created by fabian on 16.10.16.
 */

public class XmlSensor extends AbstractSensor {

    private static final String TAG = XmlSensor.class.getSimpleName();

    // TODO defensive programming
    @Override
    public String executeRequest() throws Exception {
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

                String msg = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><S:Envelope xmlns:S=\"http://schemas.xmlsoap.org/soap/envelope/\">\r\n" +
                "    <S:Header/>\r\n" +
                "    <S:Body>\r\n" +
                "        <ns2:getSpot xmlns:ns2=\"http://webservices.vslecture.vs.inf.ethz.ch/\">\r\n" +
                        " <id>Spot3</id>" +
                "        </ns2:getSpot>\r\n" +
                "    </S:Body>\r\n" +
                "</S:Envelope>";

        URL url = new URL("http",
                RemoteServerConfiguration.HOST,
                RemoteServerConfiguration.SOAP_PORT,
                "/SunSPOTWebServices/SunSPOTWebservice");


        // fabischn: Had to look up here for correct Content-Type specification
        // http://mashtips.com/call-soap-with-request-xml-and-get-response-xml-back/
        urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setDoOutput(true);
        urlConnection.setRequestProperty("Content-Type", "text/xml; charset=utf-8"); //fabischn: Took me like 2 hours to find out, that not specifying this field causes the 404s :(
        urlConnection.setRequestProperty("Accept", "application/xml");
        urlConnection.setRequestProperty("Connection", "close");
//        urlConnection.setRequestProperty("SOAPAction", "");
        DataOutputStream stream = new DataOutputStream(urlConnection.getOutputStream());

        stream.write(msg.getBytes());
//        stream.flush();
        stream.close();

        InputStream inputStream = urlConnection.getInputStream();
        StringBuffer buffer = new StringBuffer();
        reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        while ((line = reader.readLine()) != null) {
            buffer.append(line);
        }
        return buffer.toString();
    }

    @Override
    public double parseResponse(String response) {
        // fabischn:
        // https://developer.android.com/reference/org/xmlpull/v1/XmlPullParser.html
        final String TAG_XML = "XML";

        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser xpp = factory.newPullParser();
            xpp.setInput(new StringReader(response));
            int eventType = xpp.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG && xpp.getName().equals("temperature")){
                    xpp.next();
                    // TODO defensive programming ...
                    return Double.parseDouble(xpp.getText());
                }
                eventType = xpp.next();
            }
        } catch (XmlPullParserException e) {
            Log.e(TAG, "Exploded while using XmlPullParser", e);
        }
        catch (IOException e){
            Log.e(TAG, "Exploded while trying to get some more XML", e);
        }
        return Double.NEGATIVE_INFINITY;
    }
}
