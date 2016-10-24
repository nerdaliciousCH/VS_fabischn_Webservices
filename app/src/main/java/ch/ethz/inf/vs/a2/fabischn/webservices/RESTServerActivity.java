package ch.ethz.inf.vs.a2.fabischn.webservices;

import android.app.ActivityManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.Enumeration;

public class RESTServerActivity extends AppCompatActivity implements Button.OnClickListener{

    private static final String TAG = RESTServerActivity.class.getSimpleName();
//    private static final String NETWORK_INTERFACE = "lo"; // loopback device for testing since there is no wlan0 in emulator
    private static final String NETWORK_INTERFACE = "wlan0";
    private RESTService service;

    private TextView textViewIP;
    private TextView textViewPort;
    private TextView textViewStatus;

    private Button btnToggleServer;

    private NetworkInterface mNetworkInterface;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restserver);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        service = new RESTService();

        textViewIP = (TextView) findViewById(R.id.text_restserver_ip);
        textViewPort = (TextView) findViewById(R.id.text_restserver_port);
        textViewStatus = (TextView) findViewById(R.id.text_restserver_status);

        btnToggleServer = (Button) findViewById(R.id.btn_toggle_server);

        Enumeration<NetworkInterface> interfaces = null;
        try {
            interfaces = NetworkInterface.getNetworkInterfaces();
        } catch(SocketException e){
            Log.e(TAG, "Exploded trying to get network interfaces", e);
            finish();
        }
        // TODO make interface selectable by the user and pass it on to service. ListView was setup for this purpose but commented out because it has no priority
        NetworkInterface netif;
        mNetworkInterface = null;
        if (interfaces != null) {
            while(interfaces.hasMoreElements()){
                netif = interfaces.nextElement();
                if (netif.getName().equals(NETWORK_INTERFACE)){
                    Log.d(TAG,"Found " + NETWORK_INTERFACE);
                    mNetworkInterface = netif;
                }
            }
        }
        if(mNetworkInterface == null){
            textViewStatus.setText(getString(R.string.no_networkint));
        }
    }

    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.btn_toggle_server){
            if (service != null){
                Log.d(TAG, service.toString());
                Intent intent = new Intent(this, RESTService.class);
                if (isServiceRunning(RESTService.class, this)){
                    stopService(intent);
                    btnToggleServer.setText(getString(R.string.enable_server));
                    textViewStatus.setText(getString(R.string.server_down));
                    textViewIP.setText(getString(R.string.no_ip));
                } else {
                    // TODO pending intent to get ip and port?
                    startService(new Intent(this, RESTService.class));
                    btnToggleServer.setText(getString(R.string.disable_server));
                    textViewStatus.setText(getString(R.string.server_up));

                    // get ip address and set field
                    for(InetAddress address : Collections.list(mNetworkInterface.getInetAddresses())) {
                        String ipAddress = address.getHostAddress();
                        // maybe checks needed
                        textViewIP.setText(ipAddress);
                    }
                    // TODO make port a parameter chosen by user that gets passed to the Service
                    textViewPort.setText("8088");
                }
            } else {
                Log.e(TAG, "Service object was null");
            }
        }
    }

    // fabischn: from http://stackoverflow.com/questions/17588910/check-if-service-is-running-on-android
    private boolean isServiceRunning(Class<?> serviceClass,Context context){
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    // TODO delete
    private void findAndKillService(){
        // http://s2.quickmeme.com/img/a7/a772f62f11a0d1e1521263cf45955e7dd485d8d147bd4b0615de9143fe55f96a.jpg

    }
}
