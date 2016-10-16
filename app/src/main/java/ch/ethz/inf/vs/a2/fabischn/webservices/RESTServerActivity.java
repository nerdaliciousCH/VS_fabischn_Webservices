package ch.ethz.inf.vs.a2.fabischn.webservices;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class RESTServerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restserver);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
}
