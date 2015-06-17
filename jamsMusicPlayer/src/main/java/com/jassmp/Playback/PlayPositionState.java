package com.jassmp.Playback;

import android.content.Intent;

public class PlayPositionState {

    //
    // defines

    public static final String ACTION              = "PlayerPositionState";
    public static final String EXTRA_PLAY_POSITION = "playPosition";
    public static final String EXTRA_CURRENT_INDEX = "currentIndex";

    //
    // private members

    private final int mCurrentIndex;
    private final int mPlayPosition;
    private Intent mIntent = null;

    public PlayPositionState( final Intent intent ) {
        assert ACTION.equals( intent.getAction() );
        mIntent = intent;
        mCurrentIndex = intent.getIntExtra( EXTRA_CURRENT_INDEX, 0 );
        mPlayPosition = intent.getIntExtra( EXTRA_PLAY_POSITION, 0 );
    }

    public PlayPositionState( final int currentIndex, final int playPosition ) {
        mCurrentIndex = currentIndex;
        mPlayPosition = playPosition;
    }

    public Intent getIntent() {
        if( mIntent == null ) {
            mIntent = new Intent( ACTION );
            mIntent.putExtra( EXTRA_CURRENT_INDEX, mCurrentIndex );
            mIntent.putExtra( EXTRA_PLAY_POSITION, mPlayPosition );
        }

        return mIntent;
    }

    public int getCurrentIndex() {
        return mCurrentIndex;
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
