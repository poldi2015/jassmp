package com.jassmp.Playback;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;

import com.jassmp.BroadcastReceivers.HeadsetButtonsReceiver;
import com.jassmp.Dao.SongDao;
import com.jassmp.Preferences.Preferences;

public class RemoteControlClient {

    //
    // private members

    private final ComponentName mComponentName;
    private final Context       mContext;
    private final AudioManager  mAudioManager;
    private RemoteControlClientCompat mLockScreenControl = null;

    public RemoteControlClient( final Context context, final AudioManager audioManager ) {
        mContext = context;
        mAudioManager = audioManager;
        mComponentName = initMediaButtonReceiver( context, mAudioManager );
        if( new Preferences( context ).getShowLockScreenControls() ) {
            initLockScreenControls( context, mAudioManager, mComponentName );
        }
    }

    private ComponentName initMediaButtonReceiver( final Context context, final AudioManager audioManager ) {
        final ComponentName componentName = new ComponentName( context.getPackageName(),
                                                               HeadsetButtonsReceiver.class.getName() );
        audioManager.registerMediaButtonEventReceiver( componentName );
        return componentName;
    }

    private void initLockScreenControls( final Context context, final AudioManager audioManager,
                                         final ComponentName componentName ) {
        final Intent remoteControlIntent = new Intent( Intent.ACTION_MEDIA_BUTTON );
        remoteControlIntent.setComponent( componentName );

        mLockScreenControl = new RemoteControlClientCompat( audioManager,
                                                            PendingIntent.getBroadcast( context, 0, remoteControlIntent,
                                                                                        0 ) );

        mLockScreenControl.setPlaybackState( android.media.RemoteControlClient.PLAYSTATE_PLAYING );
        mLockScreenControl.setTransportControlFlags( android.media.RemoteControlClient.FLAG_KEY_MEDIA_PLAY |
                                                     android.media.RemoteControlClient.FLAG_KEY_MEDIA_PAUSE |
                                                     android.media.RemoteControlClient.FLAG_KEY_MEDIA_NEXT |
                                                     android.media.RemoteControlClient.FLAG_KEY_MEDIA_STOP |
                                                     android.media.RemoteControlClient.FLAG_KEY_MEDIA_PREVIOUS );
    }

    public boolean hasLockScreenControls() {
        return mLockScreenControl != null;
    }

    public void update( final boolean isPlaying, final SongDao song ) {
        if( hasLockScreenControls() ) {
            try {
                //Update the remote controls
                mLockScreenControl.editMetadata( true )
                                  .putString( MediaMetadataRetriever.METADATA_KEY_ARTIST, song.getArtist() )
                                  .putString( MediaMetadataRetriever.METADATA_KEY_TITLE, song.getTitle() )
                                  .putString( MediaMetadataRetriever.METADATA_KEY_ALBUM, song.getAlbum() )
                                  .putLong( MediaMetadataRetriever.METADATA_KEY_DURATION, song.getDuration() )
                                  .apply();
                song.loadAlbumArt( mContext, mAlbumArtLoadedListener );

                mLockScreenControl.setPlaybackState( isPlaying ? android.media.RemoteControlClient.PLAYSTATE_PLAYING
                                                               : android.media.RemoteControlClient.PLAYSTATE_PAUSED );
            } catch( Exception e ) {
                // Do nothing
            }
        }
    }

    private final SongDao.AlbumArtLoadedListener mAlbumArtLoadedListener = new SongDao.AlbumArtLoadedListener() {
        @Override
        public void artLoaded( final Bitmap bitmap ) {
            if( hasLockScreenControls() ) {
                mLockScreenControl.editMetadata( true )
                                  .putBitmap( RemoteControlClientCompat.MetadataEditorCompat.METADATA_KEY_ARTWORK,
                                              bitmap )
                                  .apply();
            }
        }
    };

    public void destroy() {
        shutdownLockScreenControls();
        shutdownMediaButtonReceiver();
    }

    private void shutdownLockScreenControls() {
        try {
            if( hasLockScreenControls() ) {
                mLockScreenControl.destroy();
            }
        } catch( Exception e ) {
            // Do nothing
        } finally {
            mLockScreenControl = null;
        }
    }

    private void shutdownMediaButtonReceiver() {
        mAudioManager.unregisterMediaButtonEventReceiver( mComponentName );
    }
}
