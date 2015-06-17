package com.jassmp.Playback;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;

/**
 * {@link android.app.IntentService} that does not kill itself with each request.
 */
public abstract class PersistentService extends Service {

    //
    // private members

    private volatile Looper         mServiceLooper;
    private volatile ServiceHandler mServiceHandler;
    private final    String         mName;

    private final class ServiceHandler extends Handler {
        public ServiceHandler( Looper looper ) {
            super( looper );
        }

        @Override
        public void handleMessage( Message msg ) {
            onHandleIntent( (Intent) msg.obj );
        }
    }

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public PersistentService( String name ) {
        super();
        mName = name;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        HandlerThread thread = new HandlerThread( "PersistentService[" + mName + "]" );
        thread.start();

        mServiceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler( mServiceLooper );
    }

    /**
     * You should not override this method for your IntentService. Instead,
     * override {@link #onHandleIntent}, which the system calls when the IntentService
     * receives a start request.
     *
     * @see android.app.Service#onStartCommand
     */
    @Override
    public int onStartCommand( Intent intent, int flags, int startId ) {
        Message msg = mServiceHandler.obtainMessage();
        msg.arg1 = startId;
        msg.obj = intent;
        mServiceHandler.sendMessage( msg );
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        mServiceLooper.quit();
    }

    /**
     * Unless you provide binding for your service, you don't need to implement this
     * method, because the default implementation returns null.
     *
     * @see android.app.Service#onBind
     */
    @Override
    public IBinder onBind( Intent intent ) {
        return null;
    }

    /**
     * This method is invoked on the worker thread with a request to process.
     * Only one Intent is processed at a time, but the processing happens on a
     * worker thread that runs independently from other application logic.
     * So, if this code takes a long time, it will hold up other requests to
     * the same IntentService, but it will not hold up anything else.
     * When all requests have been handled, the IntentService stops itself,
     * so you should not call {@link #stopSelf}.
     *
     * @param intent The value passed to {@link
     *               android.content.Context#startService(Intent)}.
     */
    protected abstract void onHandleIntent( Intent intent );

}
