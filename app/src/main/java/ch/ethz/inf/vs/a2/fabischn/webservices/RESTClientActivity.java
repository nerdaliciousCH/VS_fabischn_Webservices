package ch.ethz.inf.vs.a2.fabischn.webservices;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import ch.ethz.inf.vs.a2.fabischn.webservices.sensor.JsonSensor;
import ch.ethz.inf.vs.a2.fabischn.webservices.sensor.RawHttpSensor;
import ch.ethz.inf.vs.a2.fabischn.webservices.sensor.SensorListener;
import ch.ethz.inf.vs.a2.fabischn.webservices.sensor.TextSensor;

public class RESTClientActivity extends AppCompatActivity implements SensorListener, Button.OnClickListener{

    private static final String TAG = RESTClientActivity.class.getSimpleName();
    private RawHttpSensor rawHttpSensor;
    private TextSensor textSensor;
    private JsonSensor jsonSensor;
    private TextView textView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restclient);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        textView = (TextView) findViewById(R.id.text_restclient_data);

        rawHttpSensor = new RawHttpSensor();
        textSensor = new TextSensor();
        jsonSensor = new JsonSensor();
        rawHttpSensor.registerListener(this);
        textSensor.registerListener(this);
        jsonSensor.registerListener(this);
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
        switch(v.getId()) {
            case R.id.btn_rest_request_raw:
                rawHttpSensor.getTemperature();
                return;
            case R.id.btn_rest_request_plain:
                textSensor.getTemperature();
                return;
            case R.id.btn_rest_request_json:
                jsonSensor.getTemperature();
                return;
            default:
                return;
        }
    }
}
