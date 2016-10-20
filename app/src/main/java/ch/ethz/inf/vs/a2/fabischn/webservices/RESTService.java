package ch.ethz.inf.vs.a2.fabischn.webservices;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;
import java.net.ServerSocket;


// fabischn: Mostly from https://developer.android.com/guide/components/services.html#Basics

public class RESTService extends Service {

    private static final String TAG = RESTService.class.getSimpleName();
    private static final int TCP_PORT = 8088;

    private ServerSocket sock;

//  Caution: A service runs in the main thread of its hosting process—the service does not create
// its own thread and does not run in a separate process (unless you specify otherwise). This means
// that, if your service is going to do any CPU intensive work or blocking operations
// (such as MP3 playback or networking), you should
// create a new thread within the service to do that work. By
// using a separate thread, you will reduce the risk of Application Not Responding (ANR)
// errors and the application's main thread can remain dedicated
// to user interaction with your activities.


    public RESTService() {

        Log.d(TAG, "Constructor");
        //TODO  get intent or whatever to get interface string
        sock = null;
        try {
            sock = new ServerSocket(TCP_PORT);
        } catch (IOException e){
            Log.e(TAG, "Exploded trying to setup a socket", e);
        }
        if (sock != null){

        }else{
            // TODO shutdown
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");
        // Remark: This is once on creation and this will be executed before onStartCommand() or onBind()
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        // TODO free all resources?
    }



    @Override
    public IBinder onBind(Intent intent) {
        // Remark: this will be called when bindService() is called
        // TODO: Return the communication channel to the service. Return null if no binding used
        Log.d(TAG, "onBind");
//        throw new UnsupportedOperationException("Not yet implemented");
        return null;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // RemarkP startId can be used with stopSelf(startId) to manage concurrent start and stops
        // Remark: will be called when startService() is called
        // Remark: Should call stopSelf() or stopService() if work is done. Not for us, this is done by UI button click
        String netif = intent.getStringExtra("interface");
        Log.d(TAG, "onStartCommand");
        // TODO return START_STICKY?
        return super.onStartCommand(intent, flags, startId);
    }
}
