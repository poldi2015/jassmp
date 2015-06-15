package com.jassmp.Playback;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.PowerManager;

import com.jassmp.Dao.SongDao;

import java.io.IOException;

public class PlaybackMediaPlayer {

    //
    // defines

    public enum State {
        UNINITIALIZED, STOPPED, PAUSED, PLAYING
    }

    public static final int   FADE_STEP_DELAY_MS            = 100;
    public static final int   SONG_STATE_UPDATE_INTERVAL_MS = 300;
    public static final float MAX_VOLUME                    = 1.0f;
    public static final float MIN_VOLUME                    = 0.0f;


    //
    // private members

    private final Context                   mContext;
    private final AudioManagement           mAudioManagement;
    private final MediaPlayer               mMediaPlayer;
    private final MediaPlayerStatusListener mListener;
    private final Handler mHandler = new Handler();

    private int     mChangedCrossFadeDuration = -1;
    private int     mCrossFadeDuration        = 0;
    private boolean mLoopSong                 = false;
    private State   mState                    = State.UNINITIALIZED;

    public PlaybackMediaPlayer( final Context context, final AudioManagement audioManagement,
                                final MediaPlayerStatusListener listener ) {
        mContext = context;
        mAudioManagement = audioManagement;
        mListener = listener;
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.reset();
        mMediaPlayer.setWakeMode( mContext, PowerManager.PARTIAL_WAKE_LOCK );
        mMediaPlayer.setAudioStreamType( AudioManager.STREAM_MUSIC );
        mMediaPlayer.setOnErrorListener( new MediaPlayer.OnErrorListener() {

            @Override
            public boolean onError( MediaPlayer mMediaPlayer, int what, int extra ) {
                return true;
            }

        } );
        mState = State.STOPPED;
    }


    //
    // Lifecycle

    public synchronized boolean isInitialized() {
        return mState != State.UNINITIALIZED;
    }

    public synchronized State getState() {
        return mState;
    }

    public synchronized void destroy() {
        if( getState() == State.UNINITIALIZED ) {
            return;
        }
        mMediaPlayer.release();
        updateState( State.UNINITIALIZED );
    }


    //
    // Playback functions

    public void play() {
        switch( getState() ) {
            case PLAYING:
                if( !mAudioManagement.requestAudioFocus() ) {
                    return;
                }
                break;
            case PAUSED:
                if( !mAudioManagement.requestAudioFocus() ) {
                    return;
                }
                mMediaPlayer.start();
                updateState( State.PLAYING );
                break;
        }
    }

    public void pause() {
        switch( getState() ) {
            case PLAYING:
                mMediaPlayer.pause();
                updateState( State.PAUSED );
                break;
        }
    }

    public void stop() {
        switch( getState() ) {
            case PAUSED:
            case PLAYING:
                mMediaPlayer.stop();
                updateState( State.STOPPED );
                break;
        }
    }

    public void play( final SongDao song, final int position ) {
        if( !isInitialized() ) {
            return;
        }
        handleStart( song, position );
    }

    public void setRepeatMode( final PlaybackService.RepeatMode repeatMode ) {
        if( !isInitialized() ) {
            return;
        }
        mLoopSong = repeatMode == PlaybackService.RepeatMode.SONG;
    }

    public void setCrossFadingDuration( final int crossFadeDuration ) {
        mChangedCrossFadeDuration = crossFadeDuration;
    }

    public int getPosition() {
        switch( getState() ) {
            case PAUSED:
            case PLAYING:
                return mMediaPlayer.getCurrentPosition();
        }
        return 0;
    }

    //
    // event handler

    private void handleStart( final SongDao song, final int position ) {
        mMediaPlayer.reset();
        if( getState() != State.PLAYING ) {
            mListener.preparePlaying();
        }
        try {
            mMediaPlayer.setDataSource( mContext, song.getFileUri() );
            mMediaPlayer.prepare();
        } catch( final IOException e ) {
            reset();
            mListener.songFailed( song, e );
            return;
        }
        mMediaPlayer.setLooping( mLoopSong );

        if( !mAudioManagement.requestAudioFocus() ) {
            reset();
            return;
        }

        if( position >= 0 ) {
            mMediaPlayer.seekTo( position );
        }

        mListener.songStarting( song, position );
        updateState( State.PLAYING );
        new SongStateListener( song );
        if( position == 0 ) {
            updateCrossFadeDuration();
            startFading( true );
        } else {
            mMediaPlayer.setVolume( MAX_VOLUME, MAX_VOLUME );
        }
        mMediaPlayer.start();
    }

    private void handleCurrentPosition( final SongDao song, final int position ) {
        if( getState() != State.PLAYING ) {
            return;
        }
        mListener.positionChanged( song, position );
    }

    /**
     * @return true if fading started
     */
    private boolean handleFading( final SongDao song, final int position, final int duration ) {
        if( getState() != State.PLAYING ) {
            return false;
        }
        final int crossFadeDuration = getCrossFadeDuration();
        if( crossFadeDuration > 0 && position + crossFadeDuration >= duration ) {
            mListener.songFinished( song );
            startFading( false );
            return true;
        } else {
            return false;
        }
    }

    private void handleSongFinished( final SongDao song ) {
        mMediaPlayer.setOnCompletionListener( null );
        if( getCrossFadeDuration() == 0 ) {
            // Already sent in handleFading
            mListener.songFinished( song );
        }
        updateState( State.STOPPED );
    }

    //
    // Helpers

    private void reset() {
        if( !isInitialized() ) {
            return;
        }
        mMediaPlayer.reset();
        mHandler.removeCallbacks( null );
        updateState( State.STOPPED );
    }

    private void startFading( boolean fadeIn ) {
        final int crossFadeDuration = getCrossFadeDuration();
        if( crossFadeDuration > 0 ) {
            final float fadeStep = MAX_VOLUME / ( crossFadeDuration / FADE_STEP_DELAY_MS );
            new FadeRunnable( MAX_VOLUME, fadeIn ? fadeStep : -fadeStep );
        } else {
            final float volume = fadeIn ? MAX_VOLUME : MIN_VOLUME;
            mMediaPlayer.setVolume( volume, volume );
        }
    }

    private void updateState( final State state ) {
        mState = state;
        mListener.stateChanged( state );
    }

    private void updateCrossFadeDuration() {
        if( mChangedCrossFadeDuration >= 0 ) {
            mCrossFadeDuration = mChangedCrossFadeDuration;
            mChangedCrossFadeDuration = -1;
        }
    }

    private int getCrossFadeDuration() {
        return mLoopSong ? mCrossFadeDuration : 0;
    }

    private class SongStateListener implements Runnable, MediaPlayer.OnCompletionListener {

        //
        // private members

        private final SongDao mSong;

        public SongStateListener( final SongDao song ) {
            mSong = song;
            mMediaPlayer.setOnCompletionListener( this );
            mHandler.postDelayed( this, SONG_STATE_UPDATE_INTERVAL_MS );
        }

        @Override
        public void run() {
            final int position = mMediaPlayer.getCurrentPosition();
            handleCurrentPosition( mSong, position );
            if( !handleFading( mSong, position, mMediaPlayer.getDuration() ) ) {
                mHandler.postDelayed( this, SONG_STATE_UPDATE_INTERVAL_MS );
            }
        }

        @Override
        public void onCompletion( final MediaPlayer mp ) {
            handleSongFinished( mSong );
        }

    }

    private class FadeRunnable implements Runnable {

        //
        // private members

        private final float mFadeStep;
        private       float mCurrentVolume;

        public FadeRunnable( final float fromVolume, float fadeStep ) {
            mFadeStep = fadeStep;
            mMediaPlayer.setVolume( fromVolume, fromVolume );
            mCurrentVolume = fromVolume;
            mHandler.postDelayed( this, FADE_STEP_DELAY_MS );
        }

        @Override
        public void run() {
            if( getState() != State.PLAYING ) {
                return;
            }
            mMediaPlayer.setVolume( mCurrentVolume, mCurrentVolume );
            mCurrentVolume += mFadeStep;
            mHandler.postDelayed( this, FADE_STEP_DELAY_MS );
        }
    }

    public interface MediaPlayerStatusListener {

        void preparePlaying();

        void songFailed( SongDao song, IOException e );

        void stateChanged( PlaybackMediaPlayer.State state );

        void songStarting( SongDao song, int position );

        void positionChanged( SongDao song, int position );

        void songFinished( SongDao song );
    }
}
