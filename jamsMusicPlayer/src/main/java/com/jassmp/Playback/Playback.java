package com.jassmp.Playback;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;

import java.util.ArrayList;
import java.util.List;

public class Playback {

    //
    // defines

    public enum Action {
        STATUS, ADD_SONGS_AFTER_CURRENT, ADD_SONGS_AT_END, MOVE_SONG, DELETE_SONGS, CLEAR_SONGS, PLAY, PLAY_PAUSE,
        PAUSE,
        NEXT,
        PREVIOUS,
        PLAY_INDEX, TOGGLE_REPEAT, PLAY_POSITION
    }

    public static final String EXTRA_SONGS        = "songs";
    public static final String EXTRA_FROM_INDEX   = "fromIndex";
    public static final String EXTRA_TO_INDEX     = "toIndex";
    public static final String EXTRA_INDEX        = "index";
    public static final String EXTRA_CURRENT_SONG = "currentSong";
    public static final String EXTRA_POSITION     = "position";
    public static final String EXTRA_INDICES      = "indices";


    //
    // private members

    private final Context               mContext;
    private final PlaybackStateReceiver mPlaybackStateReceiver;

    public Playback( final Context context ) {
        this( context, null );
    }

    public Playback( final Context context, final PlaybackStateListener playbackStateListener ) {
        mContext = context;
        if( playbackStateListener != null ) {
            mPlaybackStateReceiver = new PlaybackStateReceiver( context, playbackStateListener );
        } else {
            mPlaybackStateReceiver = null;
        }
    }

    public void destroy() {
        if( mPlaybackStateReceiver != null ) {
            mPlaybackStateReceiver.destroy();
        }
    }

    private Intent createIntent( final Action action ) {
        return new Intent( mContext, PlaybackService.class ).setAction( action.name() )
                                                            .setClass( mContext, PlaybackService
                                                                    .class );
    }

    public void status() {
        mContext.startService( createIntent( Action.STATUS ) );
    }

    public void addSongsAfterCurrent( final List<String> keys ) {
        final Intent intent = createIntent( Action.ADD_SONGS_AFTER_CURRENT );
        intent.putStringArrayListExtra( EXTRA_SONGS, new ArrayList( keys ) );
        mContext.startService( intent );
    }

    public void addSongsAtEnd( final List<String> keys ) {
        final Intent intent = createIntent( Action.ADD_SONGS_AT_END );
        intent.putStringArrayListExtra( EXTRA_SONGS, new ArrayList( keys ) );
        mContext.startService( intent );
    }

    public void moveSong( final int fromIndex, final int toIndex ) {
        if( fromIndex == toIndex ) {
            return;
        }
        final Intent intent = createIntent( Action.MOVE_SONG );
        intent.putExtra( EXTRA_FROM_INDEX, fromIndex );
        intent.putExtra( EXTRA_TO_INDEX, toIndex );
        mContext.startService( intent );
    }

    public void deleteSongs( final int[] indices ) {
        final Intent intent = createIntent( Action.DELETE_SONGS );
        intent.putExtra( EXTRA_INDICES, indices );
        mContext.startService( intent );
    }

    public void clearSongs() {
        mContext.startService( createIntent( Action.CLEAR_SONGS ) );
    }

    public void play() {
        mContext.startService( createPlayIntent() );
    }

    protected Intent createPlayIntent() {
        return createIntent( Action.PLAY );
    }

    public void pause() {
        mContext.startService( createPauseIntent() );
    }

    protected Intent createPauseIntent() {
        return createIntent( Action.PAUSE );
    }

    public void playPause() {
        mContext.startService( createPlayPauseIntent() );
    }

    protected Intent createPlayPauseIntent() {
        return createIntent( Action.PLAY_PAUSE );
    }

    public void setPlayPosition( final int position ) {
        final Intent intent = createIntent( Action.PLAY_POSITION );
        intent.putExtra( EXTRA_POSITION, position );
        mContext.startService( intent );
    }

    public void next() {
        mContext.startService( createNextIntent() );
    }

    protected Intent createNextIntent() {
        return createIntent( Action.NEXT );
    }

    public void previous() {
        mContext.startService( createPreviousIntent() );
    }

    protected Intent createPreviousIntent() {
        return createIntent( Action.PREVIOUS );
    }

    public void play( final int index ) {
        final Intent intent = createIntent( Action.PLAY_INDEX );
        intent.putExtra( EXTRA_INDEX, index );
        mContext.startService( intent );
    }

    public void toggleRepeat() {
        final Intent intent = createIntent( Action.TOGGLE_REPEAT );
        mContext.startService( intent );
    }

    private static class PlaybackStateReceiver extends BroadcastReceiver {

        //
        // private

        private final Context               mContext;
        private final PlaybackStateListener mListener;

        public PlaybackStateReceiver( final Context context, final PlaybackStateListener listener ) {
            mContext = context;
            mListener = listener;
            final IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction( PlayerState.ACTION );
            intentFilter.addAction( QueueState.ACTION );
            intentFilter.addAction( PlayPositionState.ACTION );
            LocalBroadcastManager.getInstance( context ).registerReceiver( this, intentFilter );
        }

        @Override
        public void onReceive( final Context context, final Intent intent ) {
            if( PlayerState.ACTION.equals( intent.getAction() ) ) {
                mListener.onPlayStateChanged( new PlayerState( intent ) );
            } else if( QueueState.ACTION.equals( intent.getAction() ) ) {
                mListener.onQueueChanged( new QueueState( intent ) );
            } else if( PlayPositionState.ACTION.equals( intent.getAction() ) ) {
                mListener.onPlayPositionChanged( new PlayPositionState( intent ) );
            }
        }

        public void destroy() {
            LocalBroadcastManager.getInstance( mContext ).unregisterReceiver( this );
        }
    }

}
