package ch.ethz.inf.vs.a2.fabischn.webservices.sensor;

import android.util.Log;

import org.ksoap2.serialization.SoapObject;

/**
 * Created by fabian on 16.10.16.
 */

public class SoapSensor extends AbstractSensor {

    private final static String TAG = SoapSensor.class.getSimpleName();
    private final static String TAG_SOAPOBJECT = "SoapObject";

    @Override
    public String executeRequest() throws Exception {
        // TODO continue with that SOAP stuff
        SoapObject soap = new SoapObject();
        soap.addProperty("bla", 1.0);
        soap.addProperty("test", 20.0);
        Log.d(TAG_SOAPOBJECT, soap.toString());
        return "blubb";
    }

    @Override
    public double parseResponse(String response) {
        return 0;
    }
}
