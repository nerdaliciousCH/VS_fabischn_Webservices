package ch.ethz.inf.vs.a2.fabischn.webservices;

import android.app.ActivityManager;
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

//    private ListView listViewInterfaces;

    private NetworkInterface mNetworkInterface;
    private InetAddress mNetworkInterfaceIP;
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

//        listViewInterfaces = (ListView) findViewById(R.id.listview_interfaces);
//        listViewInterfaces.setAdapter();

        Enumeration<NetworkInterface> interfaces = null;
        try {
            interfaces = NetworkInterface.getNetworkInterfaces();
        } catch(SocketException e){
            Log.e(TAG, "Exploded trying to get network interfaces", e);
        }
        // TODO make interface selectable and pass it on to service
        NetworkInterface netif;
        mNetworkInterface = null;
        if (interfaces != null) {
            while(interfaces.hasMoreElements()){
                netif = interfaces.nextElement();
                Log.d(TAG, "IF: " + netif.getName() + " IP: " + netif.getInetAddresses().nextElement().toString());
                if (netif.getName().equals(NETWORK_INTERFACE)){
                    Log.d(TAG,"Found " + NETWORK_INTERFACE);
                    mNetworkInterface = netif;
                }
            }
        }
        if(mNetworkInterface == null){
            // TODO abort
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
                    // TODO PendingIntent and getBroadcast to get (IP) and PORT?
                    intent.putExtra("interface", NETWORK_INTERFACE);
                    startService(new Intent(this, RESTService.class));
                    btnToggleServer.setText(getString(R.string.disable_server));
                    textViewStatus.setText(getString(R.string.server_up));

                    // get ip address and set field
                    for(InetAddress address : Collections.list(mNetworkInterface.getInetAddresses())) {
                        String ipAddress = address.getHostAddress();
                        // maybe checks needed
                        textViewIP.setText(ipAddress);
                    }
                    // TODO make port a parameter that gets passed to the Service
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
            Log.d(TAG, "Service running: " + service.service.getClassName());
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private void findAndKillService(){
        // http://s2.quickmeme.com/img/a7/a772f62f11a0d1e1521263cf45955e7dd485d8d147bd4b0615de9143fe55f96a.jpg
        // TODO find and kill
    }
}
