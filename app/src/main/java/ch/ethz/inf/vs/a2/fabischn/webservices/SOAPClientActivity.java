package ch.ethz.inf.vs.a2.fabischn.webservices;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Xml;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import ch.ethz.inf.vs.a2.fabischn.webservices.sensor.SensorListener;
import ch.ethz.inf.vs.a2.fabischn.webservices.sensor.XmlSensor;

public class SOAPClientActivity extends AppCompatActivity implements SensorListener, Button.OnClickListener{

    private final static String TAG = SOAPClientActivity.class.getSimpleName();
    private XmlSensor xmlSensor;
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_soapclient);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        textView = (TextView) findViewById(R.id.text_soapclient_data);

        xmlSensor = new XmlSensor();
        xmlSensor.registerListener(this);
    }

    @Override
    public void onReceiveSensorValue(final double value) {
        runOnUiThread(
                new Runnable() {
                    @Override
                    public void run() {
                        textView.setText(Double.toString(value));
                    }
                }
        );
    }

    @Override
    public void onReceiveMessage(String message) {
        Log.e(TAG, message);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_soap_request:
                xmlSensor.getTemperature();
                return;
        }
    }
}
