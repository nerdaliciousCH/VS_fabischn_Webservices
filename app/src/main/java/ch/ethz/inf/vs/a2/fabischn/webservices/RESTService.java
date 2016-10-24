package ch.ethz.inf.vs.a2.fabischn.webservices;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.os.Vibrator;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.Buffer;
import java.util.Arrays;
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
    private static final int PATTERN_SIZE = 20;

    private RESTServer mRestServer;
    private Thread mRestServerThread;

    private SensorManager mSensorManager;
    private Sensor mSensorGravity;
    private Sensor mSensorAcceleration;

    private static Vibrator mVibrator;
    private static MediaPlayer mMediaPlayer;


    private static float[] mLastGravity = new float[VEC_SIZE];
    private static float[] mLastAcceleration = new float[VEC_SIZE];
    // TODO change this
    private static long[] mVibratorPattern = {0,100,100,0,0,0,100,0,0,0,0,0,0,0,0,0,0,0,0,0,0,100};

    private static Object mLockGravity = new Object();
    private static Object mLockAcceleration = new Object();
    private static Object mLockVibratorPattern = new Object();


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
        mVibrator.cancel();
        mMediaPlayer.release();
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

        mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        // Assuming not too many concurrent requests
        mMediaPlayer = MediaPlayer.create(this, R.raw.sound);

        // Run on non-main thread, otherwise NetworkOnMainThread exception
        Thread initThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mRestServer = new RESTServer(TCP_PORT, 10, getApplicationContext());
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

    public static void vibrate(){
        long[] pattern = getVibrationPattern();
        synchronized (mVibrator) {
            mVibrator.vibrate(pattern, -1);
        }
    }

    public static void playSound(){
        synchronized (mMediaPlayer) {
            if (!mMediaPlayer.isPlaying()) {
                mMediaPlayer.start();
            }
        }
    }

    public static void setVibratorPattern(final long[] newPattern){
        if(newPattern != null){
            synchronized (mLockVibratorPattern){
                mVibratorPattern = newPattern.clone();
            }
        }
        else{
            // keep old pattern if newPattern is null
        }
    }

    public static long[] getVibrationPattern(){
        synchronized (mLockVibratorPattern){
            return mVibratorPattern.clone();
        }
    }

    public static float[] getGravity() {
        synchronized (mLockGravity) {
            return mLastGravity.clone();
        }
    }

    public static float[] getAcceleration(){
        synchronized (mLockAcceleration){
            return mLastAcceleration.clone();
        }
    }
}


// fabischn: https://developer.android.com/reference/java/util/concurrent/ExecutorService.html
// The server
class RESTServer implements Runnable {

    private static final String TAG = RESTServer.class.getSimpleName();

    private final ServerSocket serverSocket;
    private final ExecutorService pool;
    private Context context;

    AtomicBoolean acceptConnections;

    public RESTServer(int port, int poolSize, Context context)
            throws IOException {
        acceptConnections = new AtomicBoolean(true);
        serverSocket = new ServerSocket();
        serverSocket.setReuseAddress(true);
        serverSocket.bind(new InetSocketAddress(port),poolSize);
        pool = Executors.newFixedThreadPool(poolSize);
        this.context = context;
    }

    public void run() { // run the service
            while (acceptConnections.get()) {
                try {
                    pool.execute(new RESTRequestHandler(serverSocket.accept(), context));
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
    private Context context;
    RESTRequestHandler(Socket socket, Context context) {
        this.socket = socket;
        this.context = context;
    }
    public void run() {
        // read and service request on socket
        String lineBuffer;
        StringBuffer outBuffer;
        BufferedReader htmlReader;
        PrintWriter out;
        try {
            HttpParser content = new HttpParser(socket.getInputStream());
            int returnCode = content.parseRequest();
            String requestURL = content.getRequestURL();
            String acceptedResource = content.getHeader("accept");
            String resourceLocation;
            String sensorValue = "no value";
            boolean requestedSensor = false;

            out = new PrintWriter(socket.getOutputStream(), true);

            // case distinction by accept header field
            if(acceptedResource.contains("html")){
                Log.d(TAG,"He wants an HTML");
                switch (requestURL){
                    case "/":
                        resourceLocation = "index.html";
                        break;
                    case "/sensors.html":
                        resourceLocation = "sensors.html";
                        break;
                    case "/actuators.html":
                        resourceLocation = "actuators.html";
                        break;
                    case "/vibration.html":
                        resourceLocation = "vibration.html";
                        RESTService.vibrate();
                        break;
                    case "/sound.html":
                        resourceLocation = "sound.html";
                        RESTService.playSound();
                        break;
                    case "/gravity.html":
                        resourceLocation = "gravity.html";
                        requestedSensor = true;
                        sensorValue = Arrays.toString(RESTService.getGravity());
                        break;
                    case "/acceleration.html":
                        resourceLocation = "acceleration.html";
                        requestedSensor = true;
                        sensorValue = Arrays.toString(RESTService.getAcceleration());
                        break;
                    default:
                        resourceLocation = "noresource.html";
                        break;
                }
            } else if(acceptedResource.contains("json")) {
                resourceLocation = "notimplemented.json";

            } else if (acceptedResource.contains("xml")) {
                resourceLocation = "notimplemented.xml";
            } else {
                resourceLocation = "notsupported.html";
            }

            htmlReader = new BufferedReader(new InputStreamReader(context.getAssets().open(resourceLocation)));
            outBuffer = new StringBuffer();
            while((lineBuffer = htmlReader.readLine()) != null){
                outBuffer.append(lineBuffer);
            }
            if (outBuffer != null) {
                if(requestedSensor){
                    outBuffer.insert(outBuffer.indexOf(":")+1, sensorValue);
                }
                out.write(outBuffer.toString());
            }else{
                out.write("nothing");
            }
            out.flush();
        } catch (IOException e){
            Log.e(TAG, "Exploded trying to do some IO",e);
        }finally {

        }
        try {
            socket.close();
        } catch (IOException e) {
            Log.e(TAG, "Exploded trying to close socket", e);
        }
    }
}
