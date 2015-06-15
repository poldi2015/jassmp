package com.jassmp.Playback;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import java.util.List;

public class Playback {

    //
    // defines

    public enum Action {
        STATUS, ADD_SONGS_AFTER_CURRENT, ADD_SONGS_AT_END, MOVE_SONG, DELETE_SONGS, CLEAR_SONGS, PLAY, PLAY_PAUSE,
        PAUSE,
        NEXT,
        PREVIOUS,
        PLAY_INDEX, SET_REPEAT, PLAY_POSITION
    }

    public static final String EXTRA_SONGS        = "songs";
    public static final String EXTRA_FROM_INDEX   = "fromIndex";
    public static final String EXTRA_TO_INDEX     = "toIndex";
    public static final String EXTRA_INDEX        = "index";
    public static final String EXTRA_CURRENT_SONG = "currentSong";
    public static final String EXTRA_POSITION     = "position";
    public static final String EXTRA_INDICES      = "indices";
    public static final String EXTRA_REPEAT_MODE  = "RepeatMode";


    //
    // private members

    private final Context               mContext;
    private final PlaybackStateReceiver mPlaybackStateReceiver;

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

    public void status() {
        final Intent intent = new Intent( Action.STATUS.name() );
        mContext.startService( intent );
    }

    public void addSongsAfterCurrent( final List<String> keys ) {
        final Intent intent = new Intent( Action.ADD_SONGS_AFTER_CURRENT.name() );
        intent.putExtra( EXTRA_SONGS, keys.toArray( new String[ keys.size() ] ) );
        mContext.startService( intent );
    }

    public void addSongsAtEnd( final List<String> keys ) {
        final Intent intent = new Intent( Action.ADD_SONGS_AT_END.name() );
        intent.putExtra( EXTRA_SONGS, keys.toArray( new String[ keys.size() ] ) );
        mContext.startService( intent );
    }

    public void moveSong( final int fromIndex, final int toIndex ) {
        if( fromIndex == toIndex ) {
            return;
        }
        final Intent intent = new Intent( Action.MOVE_SONG.name() );
        intent.putExtra( EXTRA_FROM_INDEX, fromIndex );
        intent.putExtra( EXTRA_TO_INDEX, toIndex );
        mContext.startService( intent );
    }

    public void deleteSongs( final int[] indices ) {
        final Intent intent = new Intent( Action.DELETE_SONGS.name() );
        intent.putExtra( EXTRA_INDICES, indices );
        mContext.startService( intent );
    }

    public void clearSongs() {
        final Intent intent = new Intent( Action.CLEAR_SONGS.name() );
        mContext.startService( intent );
    }

    public void play() {
        final Intent intent = new Intent( Action.PLAY.name() );
        mContext.startService( intent );
    }

    public void pause() {
        final Intent intent = new Intent( Action.PAUSE.name() );
        mContext.startService( intent );
    }

    public void playPause() {
        final Intent intent = new Intent( Action.PLAY_PAUSE.name() );
        mContext.startService( intent );
    }

    public void setPlayPosition( final int position ) {
        final Intent intent = new Intent( Action.PLAY_POSITION.name() );
        intent.putExtra( EXTRA_POSITION, position );
        mContext.startService( intent );
    }

    public void next() {
        final Intent intent = new Intent( Action.NEXT.name() );
        mContext.startService( intent );
    }

    public void previous() {
        final Intent intent = new Intent( Action.PREVIOUS.name() );
        mContext.startService( intent );
    }

    public void play( final int index ) {
        final Intent intent = new Intent( Action.PLAY_INDEX.name() );
        intent.putExtra( EXTRA_INDEX, index );
        mContext.startService( intent );
    }

    public void setRepeat( final PlaybackService.RepeatMode repeatMode ) {
        final Intent intent = new Intent( Action.SET_REPEAT.name() );
        intent.putExtra( EXTRA_REPEAT_MODE, repeatMode.name() );
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
            mContext.registerReceiver( this, new IntentFilter( PlayerState.ACTION ) );
        }

        @Override
        public void onReceive( final Context context, final Intent intent ) {
            mListener.playStateChanged( new PlayerState( intent ) );
        }

        public void destroy() {
            mContext.unregisterReceiver( this );
        }
    }

}
