package ch.ethz.inf.vs.a2.fabischn.webservices;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import ch.ethz.inf.vs.a2.fabischn.webservices.sensor.SensorListener;

public class SOAPClientActivity extends AppCompatActivity implements SensorListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_soapclient);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onReceiveSensorValue(double value) {

    }

    @Override
    public void onReceiveMessage(String message) {

    }
}
