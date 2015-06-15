package com.jassmp.Playback;

import android.content.Intent;

import java.util.List;

public class QueueState {

    //
    // defines

    public static final String ACTION              = "QueueState";
    public static final String EXTRA_QUEUE         = "queue";
    public static final String EXTRA_CURRENT_INDEX = "currentIndex";

    //
    // private members

    private final List<String> mQueue;
    private final int          mCurrentIndex;
    private Intent mIntent = null;

    public QueueState( final Intent intent ) {
        assert ACTION.equals( intent.getAction() );
        mIntent = intent;
        mQueue = intent.getStringArrayListExtra( EXTRA_QUEUE );
        mCurrentIndex = intent.getIntExtra( EXTRA_CURRENT_INDEX, 0 );
    }

    public QueueState( final List<String> queue, final int currentIndex ) {
        mQueue = queue;
        mCurrentIndex = currentIndex;
    }

    public Intent getIntent() {
        if( mIntent == null ) {
            mIntent = new Intent( ACTION );
            mIntent.putExtra( EXTRA_QUEUE, mQueue.toArray( new String[ mQueue.size() ] ) );
            mIntent.putExtra( EXTRA_CURRENT_INDEX, mCurrentIndex );
        }

        return mIntent;
    }

    public List<String> getQueue() {
        return mQueue;
    }

    public int getCurrentIndex() {
        return mCurrentIndex;
    }

}
