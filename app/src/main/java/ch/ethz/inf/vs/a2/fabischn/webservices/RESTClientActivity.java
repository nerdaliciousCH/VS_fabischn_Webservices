package ch.ethz.inf.vs.a2.fabischn.webservices;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.IOException;

import ch.ethz.inf.vs.a2.fabischn.webservices.sensor.RawHttpSensor;
import ch.ethz.inf.vs.a2.fabischn.webservices.sensor.SensorListener;

public class RESTClientActivity extends AppCompatActivity implements SensorListener, Button.OnClickListener{

    private static final String TAG = RESTClientActivity.class.getSimpleName();
    private RawHttpSensor rawHttpSensor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restclient);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        rawHttpSensor = new RawHttpSensor();
        rawHttpSensor.registerListener(this);
    }

    @Override
    public void onReceiveSensorValue(double value) {

    }

    @Override
    public void onReceiveMessage(String message) {

    }

    @Override
    public void onClick(View v) {
        Log.d(TAG, "onClick");
        if (v.getId() == R.id.btn_rest_request){
            try{
                rawHttpSensor.executeRequest();
            } catch (IOException e){
                Log.e(TAG, "Something went wrong trying to send the request:", e);
            }
        }
    }
}
