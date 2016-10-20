package ch.ethz.inf.vs.a2.fabischn.webservices;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class RESTServerActivity extends AppCompatActivity implements Button.OnClickListener{

    private static final String TAG = RESTServerActivity.class.getSimpleName();
    private static final String NETWORK_INTERFACE = "lo";
    private RESTService service;

    private NetworkInterface mNetworkInterface;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restserver);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Enumeration<NetworkInterface> interfaces = null;
        try {
            interfaces = NetworkInterface.getNetworkInterfaces();
        } catch(SocketException e){
            Log.e(TAG, "Exploded trying to get network interfaces", e);
        }
        // TODO make interface selectable and pass it on to service
        NetworkInterface netif = null;
        mNetworkInterface = null;
        if (interfaces != null) {

            while(interfaces.hasMoreElements()){
                netif = interfaces.nextElement();
                if (netif.getName().equals(NETWORK_INTERFACE)){
                    mNetworkInterface = netif;
                }
            }
        }

    }

    @Override
    public void onClick(View v) {
        // TODO start and stop service
    }
}
