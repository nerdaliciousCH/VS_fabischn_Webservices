package ch.ethz.inf.vs.a2.fabischn.webservices;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;


// fabischn: Mostly from https://developer.android.com/guide/components/services.html#Basics
// and https://developer.android.com/reference/java/util/concurrent/ExecutorService.html

public class RESTService extends Service implements SensorEventListener {

    private static final String TAG = RESTService.class.getSimpleName();

    private static final int TCP_PORT = 8088;
    private static final int VEC_SIZE = 3;

    private RESTServer mRestServer;
    private Thread mRestServerThread;

    private SensorManager mSensorManager;
    private Sensor mSensorGravity;
    private Sensor mSensorAcceleration;


    private static float[] mLastGravity = new float[VEC_SIZE];
    private static float[] mLastAcceleration = new float[VEC_SIZE];
    private static Object mLockGravity = new Object();
    private static Object mLockAcceleration = new Object();


//  Caution: A service runs in the main thread of its hosting process—the service does not create
// its own thread and does not run in a separate process (unless you specify otherwise). This means
// that, if your service is going to do any CPU intensive work or blocking operations
// (such as MP3 playback or networking), you should
// create a new thread within the service to do that work. By
// using a separate thread, you will reduce the risk of Application Not Responding (ANR)
// errors and the application's main thread can remain dedicated
// to user interaction with your activities.


    public RESTService() {

    }

    @Override
    public void onCreate() {
        super.onCreate();
        // Remark: This is once on creation and this will be executed before onStartCommand() or onBind()


    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mRestServerThread != null) {
            mRestServer.stopAcceptingConnections();
        }
        mSensorManager.unregisterListener(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // Remark: this will be called when bindService() is called
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Remark: startId can be used with stopSelf(startId) to manage concurrent start and stops
        // Remark: will be called when startService() is called

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        mSensorGravity = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        mSensorAcceleration = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);


        // Run on non-main thread, otherwise NetworkOnMainThread exception
        Thread initThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mRestServer = new RESTServer(TCP_PORT, 10);
                    mRestServerThread = new Thread(mRestServer);
                } catch (IOException e) {
                    Log.e(TAG, "Exploded trying to fire up server", e);
                }
            }
        });

        initThread.start();
        try {
            initThread.join();
        } catch (InterruptedException e) {
            Log.e(TAG, "Got interrupted while initializing thread was running", e);
            stopSelf(startId);
        }

        if (mRestServerThread != null) {
            mSensorManager.registerListener(this, mSensorGravity, SensorManager.SENSOR_DELAY_UI);
            mSensorManager.registerListener(this, mSensorAcceleration, SensorManager.SENSOR_DELAY_UI);
            mRestServerThread.start();
        } else {
            Log.e(TAG, "Couldn't setup threads and server sockets");
            stopSelf(startId);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        switch (event.sensor.getType()) {
            case Sensor.TYPE_GRAVITY:
                synchronized (mLockGravity) {
                    mLastGravity[0] = event.values[0];
                    mLastGravity[1] = event.values[1];
                    mLastGravity[2] = event.values[2];
                }
                return;

            case Sensor.TYPE_LINEAR_ACCELERATION:
                synchronized (mLockAcceleration) {
                    mLastAcceleration[0] = event.values[0];
                    mLastAcceleration[1] = event.values[1];
                    mLastAcceleration[2] = event.values[2];
                }
                return;
            default:
                return;

        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public static float[] getGravity() {
        synchronized (mLockGravity) {
//            return float[3] values = {mLastGravity[0],mLastGravity[1],mLastGravity[2]};
        }
        return null;
    }
}


// fabischn: https://developer.android.com/reference/java/util/concurrent/ExecutorService.html
// The server
class RESTServer implements Runnable {

    private static final String TAG = RESTServer.class.getSimpleName();

    private final ServerSocket serverSocket;
    private final ExecutorService pool;

    AtomicBoolean acceptConnections;

    public RESTServer(int port, int poolSize)
            throws IOException {
        acceptConnections = new AtomicBoolean(true);
        serverSocket = new ServerSocket();
        serverSocket.setReuseAddress(true);
        serverSocket.bind(new InetSocketAddress(port),poolSize);
        pool = Executors.newFixedThreadPool(poolSize);
    }

    public void run() { // run the service
            while (acceptConnections.get()) {
                try {
                    pool.execute(new RESTRequestHandler(serverSocket.accept()));
                }catch (IOException e) {
                    Log.e(TAG, "Couldn't handle request", e);
                }
            }
            shutdownThreadPool();
        }

    public void stopAcceptingConnections(){
        acceptConnections.set(false);
        try {
            serverSocket.close();
        } catch (IOException e) {
            Log.e(TAG, "Couldn't close server socket", e);
        }
    }

    void shutdownThreadPool() {
        pool.shutdown(); // Disable new tasks from being submitted
        try {
            // Wait a while for existing tasks to terminate
            if (!pool.awaitTermination(5, TimeUnit.SECONDS)) {
                pool.shutdownNow(); // Cancel currently executing tasks
                // Wait a while for tasks to respond to being cancelled
                if (!pool.awaitTermination(5, TimeUnit.SECONDS))
                    Log.e(TAG, "Pool did not terminate");
            }
        } catch (InterruptedException ie) {
            // (Re-)Cancel if current thread also interrupted
            pool.shutdownNow();
            // Preserve interrupt status
            Thread.currentThread().interrupt();
        }
    }

}

// fabischn: https://developer.android.com/reference/java/util/concurrent/ExecutorService.html
// Handles a request
class RESTRequestHandler implements Runnable {

    private static final String TAG = RESTRequestHandler.class.getSimpleName();

    private final Socket socket;
    RESTRequestHandler(Socket socket) {
        this.socket = socket;
        Log.d(TAG, "Request handler instantiated for new client request: " + socket.toString());
    }
    public void run() {

        // read and service request on socket
        BufferedReader in = null;
        try {
            // in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            // if (in != null){
            //    Log.d(TAG,"We read following first line: " + in.readLine());
            // }
            // fdaniel: parsing with lib
            // TODO close socket after response
            HttpParser content = new HttpParser(socket.getInputStream());
            int returnCode = content.parseRequest();
            String version = content.getVersion();
            String requestURL = content.getRequestURL();
            String acceptedResource = content.getHeader("accept");

            // case distinction by accept header field
            if(acceptedResource.contains("html")){

            } else if(acceptedResource.contains("json")) {

            } else if (acceptedResource.contains("xml")) {

            } else {
                // no data for you sir
            }
            Log.d(TAG, acceptedResource);

          //  content.closeReader();

        } catch (IOException e){
            Log.e(TAG, "Exploded trying to read from socket's input stream",e);
        }finally {
//            try {
//                if(in != null) {
//                    in.close();
//                }
//            } catch (IOException e) {
//                Log.e(TAG, "Couldn't close BufferedReader and input streams");
//            }
        }

        PrintWriter out = null;
        try{
            out = new PrintWriter(socket.getOutputStream(), true);
            out.write("blubb");
            out.flush();
        } catch (IOException e) {
            Log.e(TAG, "Couldn't instantiate PrintWriter", e);
        }finally {
            if (out != null){
                out.close();
            }
        }
    }
}
