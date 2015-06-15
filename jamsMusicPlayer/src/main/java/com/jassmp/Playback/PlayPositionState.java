package com.jassmp.Playback;

import android.content.Intent;

public class PlayPositionState {

    //
    // defines

    public static final String ACTION              = "PlayerPositionState";
    public static final String EXTRA_PLAY_POSITION = "playPosition";

    //
    // private members

    private final int mPlayPosition;
    private Intent mIntent = null;

    public PlayPositionState( final Intent intent ) {
        assert ACTION.equals( intent.getAction() );
        mIntent = intent;
        mPlayPosition = intent.getIntExtra( EXTRA_PLAY_POSITION, 0 );
    }

    public PlayPositionState( final int playPosition ) {
        mPlayPosition = playPosition;
    }

    public Intent getIntent() {
        if( mIntent == null ) {
            mIntent = new Intent( ACTION );
            mIntent.putExtra( EXTRA_PLAY_POSITION, mPlayPosition );
        }

        return mIntent;
    }

    public int getPlayPosition() {
        return mPlayPosition;
    }

    @Override
    public String toString() {
        return "PlayPositionState{" +
               "mPlayPosition=" + mPlayPosition +
               '}';
    }
}
