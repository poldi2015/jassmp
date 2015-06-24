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
    public static final String EXTRA_IS_FIRST_SONG = "isFirstSong";
    public static final String EXTRA_IS_LAST_SONG  = "isLastSong";
    public static final String EXTRA_REPEAT_MODE   = "repeatMode";

    //
    // private members

    private final State      mState;
    private final int        mCurrentIndex;
    private final int        mPlayPosition;
    private final boolean    mIsFirstSong;
    private final boolean    mIsLastSong;
    private final RepeatMode mRepeatMode;
    private Intent mIntent = null;

    public PlayerState( final Intent intent ) {
        assert ACTION.equals( intent.getAction() );
        mIntent = intent;
        mCurrentIndex = intent.getIntExtra( EXTRA_CURRENT_INDEX, 0 );
        mState = State.valueOf( intent.getStringExtra( EXTRA_PLAY_STATE ) );
        mPlayPosition = intent.getIntExtra( EXTRA_PLAY_POSITION, 0 );
        mIsFirstSong = intent.getBooleanExtra( EXTRA_IS_FIRST_SONG, false );
        mIsLastSong = intent.getBooleanExtra( EXTRA_IS_LAST_SONG, false );
        mRepeatMode = RepeatMode.valueOf( intent.getStringExtra( EXTRA_REPEAT_MODE ) );
    }

    public PlayerState( final int currentIndex, final boolean isFirstSong, final boolean isLastSong,
                        final State state, final RepeatMode repeatMode, final int playPosition ) {
        mCurrentIndex = currentIndex;
        mState = state;
        mPlayPosition = playPosition;
        mIsFirstSong = isFirstSong;
        mIsLastSong = isLastSong;
        mRepeatMode = repeatMode;
    }

    public Intent getIntent() {
        if( mIntent == null ) {
            mIntent = new Intent( ACTION );
            mIntent.putExtra( EXTRA_PLAY_STATE, mState.name() );
            mIntent.putExtra( EXTRA_CURRENT_INDEX, mCurrentIndex );
            mIntent.putExtra( EXTRA_PLAY_POSITION, mPlayPosition );
            mIntent.putExtra( EXTRA_IS_FIRST_SONG, mIsFirstSong );
            mIntent.putExtra( EXTRA_IS_LAST_SONG, mIsLastSong );
            mIntent.putExtra( EXTRA_REPEAT_MODE, mRepeatMode.name() );
        }

        return mIntent;
    }

    public State getState() {
        return mState;
    }

    public RepeatMode getRepeatMode() {
        return mRepeatMode;
    }

    public int getCurrentIndex() {
        return mCurrentIndex;
    }

    public int getPlayPosition() {
        return mPlayPosition;
    }

    public boolean isFirstSong() {
        return mIsFirstSong;
    }

    public boolean isLastSong() {
        return mIsLastSong;
    }
}
