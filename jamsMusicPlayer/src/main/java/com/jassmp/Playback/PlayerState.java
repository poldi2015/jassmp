package com.jassmp.Playback;

import android.content.Intent;

public class PlayerState {

    //
    // defines

    public enum State {PAUSED, PLAYING}

    public static final String ACTION              = "PlayerState";
    public static final String EXTRA_PLAY_STATE    = "playState";
    public static final String EXTRA_CURRENT_INDEX = "currentIndex";
    public static final String EXTRA_PLAY_POSITION = "playPosition";

    //
    // private members

    private final State mState;
    private final int   mCurrentIndex;
    private final int   mPlayPosition;
    private Intent mIntent = null;

    public PlayerState( final Intent intent ) {
        assert ACTION.equals( intent.getAction() );
        mIntent = intent;
        mCurrentIndex = intent.getIntExtra( EXTRA_CURRENT_INDEX, 0 );
        mState = State.valueOf( intent.getStringExtra( EXTRA_PLAY_STATE ) );
        mPlayPosition = intent.getIntExtra( EXTRA_PLAY_POSITION, 0 );
    }

    public PlayerState( final int currentIndex, final State state, final int playPosition ) {
        mCurrentIndex = currentIndex;
        mState = state;
        mPlayPosition = playPosition;
    }

    public Intent getIntent() {
        if( mIntent == null ) {
            mIntent = new Intent( ACTION );
            mIntent.putExtra( EXTRA_PLAY_STATE, mState.name() );
            mIntent.putExtra( EXTRA_CURRENT_INDEX, mCurrentIndex );
            mIntent.putExtra( EXTRA_PLAY_POSITION, mPlayPosition );
        }

        return mIntent;
    }

    public State getState() {
        return mState;
    }

    public int getCurrentIndex() {
        return mCurrentIndex;
    }

    public int getPlayPosition() {
        return mPlayPosition;
    }

}
