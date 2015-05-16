package com.jassmp.Utils;

import android.os.AsyncTask;

import java.util.concurrent.Semaphore;

/**
 * This AsyncTask allows to wait until the task has finished running.
 */
public class SynchronizedAsyncTask
        extends AsyncTask<SynchronizedAsyncTask.Executor, Void, SynchronizedAsyncTask.Executor> {

    private final Semaphore mDisposeSemaphore = new Semaphore( 1, true );
    private final Object    mLock             = new Object();

    public SynchronizedAsyncTask() {
    }

    public void execute( final int delayInMillis, final Executor executor ) {
        if( isDisposed() ) {
            throw new IllegalStateException( "Task is disposed" );
        }
        executor.setDelayInMillis( delayInMillis );
        super.execute( executor );
    }

    /**
     * Dispose the task.
     * <p/>
     * Blocks  until task has finished.
     */
    public void dispose() {
        synchronized( mLock ) {
            if( getStatus() == Status.FINISHED ) {
                // Ensure that we have the lock
                final Executor runner;
                try {
                    runner = get();
                    if( runner != null ) {
                        runner.cancel();
                    }
                } catch( Exception e ) {
                    // Ignore
                }
                acquire();
            } else {
                cancel( true );
                join();
            }
        }
    }

    public synchronized boolean isDisposed() {
        synchronized( mLock ) {
            return getStatus() == Status.FINISHED;
        }
    }

    //
    // internal interfaces

    @Override
    protected final void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected final void onProgressUpdate( Void... values ) {
        super.onProgressUpdate( values );
    }

    @Override
    protected final void onCancelled() {
        release();
    }

    @Override
    protected void onCancelled( SynchronizedAsyncTask.Executor runner ) {
        runner.cancel();
        release();
    }

    @Override
    protected final Executor doInBackground( final SynchronizedAsyncTask.Executor... runner ) {
        // Die if already disposed
        if( !acquire() ) {
            return null;
        }
        try {
            runner[ 0 ].execute();
        } finally {
            release();
        }
        return runner[ 0 ];
    }

    @Override
    protected final void onPostExecute( final SynchronizedAsyncTask.Executor result ) {
        // Die if already disposed
        if( !acquire() ) {
            return;
        }
        try {
            if( isCancelled() || result == null ) {
                return;
            }
            result.onPostExecute();
            super.onPostExecute( result );
        } finally {
            release();
        }
    }

    /**
     * Try to get the lock.
     *
     * @return true if was not locked and lock was qcquired
     */
    private boolean acquire() {
        return mDisposeSemaphore.tryAcquire();
    }

    /**
     * Releae the lock
     */
    private void release() {
        mDisposeSemaphore.release();
    }

    /**
     * Wait for the lock and acquire it.
     */
    private void join() {
        mDisposeSemaphore.acquireUninterruptibly();
    }


    /**
     * Implement this class to implement task body.
     */
    public static abstract class Executor {

        private long mDelayInMillis = 0;

        public void setDelayInMillis( final long delayInMillis ) {
            mDelayInMillis = delayInMillis;
        }

        private void execute() {
            if( mDelayInMillis > 0 ) {
                try {
                    Thread.sleep( mDelayInMillis );
                } catch( InterruptedException ignored ) {
                }
            }

            doInBackground();
        }

        public abstract void cancel();

        public abstract void doInBackground();

        public abstract void onPostExecute();

    }
}
