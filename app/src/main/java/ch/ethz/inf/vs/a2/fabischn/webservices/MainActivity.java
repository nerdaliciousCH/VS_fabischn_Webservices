package ch.ethz.inf.vs.a2.fabischn.webservices;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity implements Button.OnClickListener{

    // TODO make sure we have internet
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    private void startRESTClientActivity(){
        // task 1
        Intent intent = new Intent(this, RESTClientActivity.class);
        startActivity(intent);
    }

    private void startSOAPClientActivity(){
        // task 2
        Intent intent = new Intent(this, SOAPClientActivity.class);
        startActivity(intent);

    }

    private void startRESTServerActivity(){
        // task 3
        Intent intent = new Intent(this, RESTServerActivity.class);
        startActivity(intent);
    }

    @Override
    public void onClick(View v) {
        if (v instanceof Button){
            Button btn = (Button) v;
            int btnID = btn.getId();
            switch(btnID){
                case R.id.btn_rest_client:
                    startRESTClientActivity();
                    return;
                case R.id.btn_soap_client:
                    startSOAPClientActivity();
                    return;
                case R.id.btn_rest_server:
                    startRESTServerActivity();
                    return;
                default:
                    return;
            }
        }
    }
}
