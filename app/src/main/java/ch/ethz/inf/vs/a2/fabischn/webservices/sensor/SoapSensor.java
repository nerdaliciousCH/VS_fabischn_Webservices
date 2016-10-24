package ch.ethz.inf.vs.a2.fabischn.webservices.sensor;

import android.net.Uri;
import android.util.Log;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.SoapFault;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import ch.ethz.inf.vs.a2.fabischn.webservices.http.RemoteServerConfiguration;

/**
 * Created by fabian on 16.10.16.
 */

public class SoapSensor extends AbstractSensor {

    private final static String TAG = SoapSensor.class.getSimpleName();
    private final static String TAG_SOAPOBJECT = "SoapObject";
    private final static String SOAP_WEBSERVICE_PATH = "SunSPOTWebServices/SunSPOTWebservice";
    private final static String SOAP_NAMESPACE = "http://webservices.vslecture.vs.inf.ethz.ch/";
    private final static String SOAP_ACTION = "";
    private final static String SOAP_METHOD = "getSpot";
    private final static String spotID = "Spot3";
    private HttpTransportSE http;
    @Override
    public String executeRequest() throws Exception {

        String auth = RemoteServerConfiguration.HOST + ":" + RemoteServerConfiguration.SOAP_PORT;

        Uri.Builder builder = new Uri.Builder();
        builder.scheme("http")
                .encodedAuthority(auth)
                .appendEncodedPath(SOAP_WEBSERVICE_PATH);
        String url = builder.toString();

        http = new HttpTransportSE(url);
        SoapSerializationEnvelope envl = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envl.enc = "UTF-8";
        SoapObject soap = new SoapObject(SOAP_NAMESPACE,SOAP_METHOD); // need to namespace an path
        soap.addProperty("id", spotID);
        envl.setOutputSoapObject(soap);
        http.call(SOAP_ACTION, envl);
        if(envl.bodyIn instanceof SoapFault){
            SoapFault fault = (SoapFault) envl.bodyIn;
            Log.e(TAG_SOAPOBJECT, fault.toString());
        }
        if (envl.bodyIn instanceof SoapObject){
            SoapObject res = (SoapObject) envl.bodyIn;
            res = (SoapObject) res.getProperty("return");
            String response = res.getPropertyAsString("temperature");
            // TODO must HttpTransportSE be closed?
            return response;
        }
        return null;
    }

    @Override
    public double parseResponse(String response) {
        return Double.parseDouble(response);
    }
}
