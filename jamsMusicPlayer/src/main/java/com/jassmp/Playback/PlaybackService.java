package com.jassmp.Playback;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

import com.jassmp.Dao.SongDao;
import com.jassmp.JassMpDb.SongTableAccessor;
import com.jassmp.Preferences.Preferences;
import com.jassmp.R;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class PlaybackService extends IntentService {

    //
    // defines

    public static final String NAME            = PlaybackService.class.getSimpleName();
    public static final int    NOTIFICATION_ID = 1080;

    public enum RepeatMode {NONE, ALL, SONG}

    //
    // private members

    private Context           mContext         = null;
    private Preferences       mPreferences     = null;
    private List<String>      mQueue           = null;
    private int               mCurrentIndex    = 0;
    private int               mCurrentPosition = 0;
    private PlayerState.State mPlayState       = PlayerState.State.PAUSED;
    private RepeatMode        mRepeatMode      = RepeatMode.NONE;

    private SongTableAccessor   mSongTableAccessor      = null;
    private AudioManagement     mAudioManagement        = null;
    private RemoteControlClient mRemoteControlClient    = null;
    private PlaybackMediaPlayer mCurrentMediaPlayer     = null;
    private PlaybackMediaPlayer mAlternativeMediaPlayer = null;

    private final Object mLock = new Object();

    // TODO: Preferences: Loop

    public PlaybackService() {
        super( NAME );
    }


    //
    // Service state

    private void init() {
        if( isInitialized() ) {
            // Already initialized
            if( !mCurrentMediaPlayer.isInitialized() ) {
                mCurrentMediaPlayer = new PlaybackMediaPlayer( mContext, mAudioManagement, mMediaPlayerListern );
            }
            if( !mAlternativeMediaPlayer.isInitialized() ) {
                mAlternativeMediaPlayer = new PlaybackMediaPlayer( mContext, mAudioManagement, mMediaPlayerListern );
            }
            updateRepeatMode( mRepeatMode );
        } else {
            // First time
            mContext = getApplicationContext();
            mSongTableAccessor = SongTableAccessor.getInstance( mContext );
            mPreferences = new Preferences( mContext );
            mQueue = mPreferences.getPlaybackQueue();
            mCurrentIndex = mPreferences.getPlaybackCurrentIndex();
            mCurrentPosition = mPreferences.getPlaybackPosition();
            mPlayState = PlayerState.State.PAUSED;
            updateRepeatMode( mPreferences.getRepeatMode() );

            mAudioManagement = new AudioManagement( mContext, mAudioFocusListener );
            mCurrentMediaPlayer = new PlaybackMediaPlayer( mContext, mAudioManagement, mMediaPlayerListern );
            mAlternativeMediaPlayer = new PlaybackMediaPlayer( mContext, mAudioManagement, mMediaPlayerListern );
            mAudioManagement.requestAudioFocus();
            mRemoteControlClient = new RemoteControlClient( mContext, mAudioManagement.getAudioManager() );
        }
        updateCrossFade( mPreferences.isCrossFadeEnabled(), mPreferences.getCrossFadeDuration() );
    }

    private boolean isInitialized() {
        return mContext != null;
    }

    private void destroy() {
        if( !isInitialized() ) {
            return;
        }
        mPreferences.setPlaybackQueue( mQueue );
        mPreferences.setPlaybackCurrentIndex( mCurrentIndex );
        mPreferences.setPlaybackPosition( getCurrentMediaPlayer().getPosition() );
        mPreferences.setRepeatMode( mRepeatMode );
        getCurrentMediaPlayer().destroy();
        getAlternativeMediaPlayer().destroy();

        mRemoteControlClient.destroy();
        mRemoteControlClient = null;
        mContext = null;
        mPreferences = null;
        mQueue = null;
        mCurrentIndex = 0;
        mSongTableAccessor = null;
    }


    //
    // Actions

    private void handleStatus() {
        publishQueueChanged();
        publishPlayerChanged();
    }

    private void handleAddSongsAfterCurrent( final Intent intent ) {
        final List<String> songKeys = intent.getStringArrayListExtra( Playback.EXTRA_SONGS );
        insertSongsAfterCurrent( songKeys );
        publishQueueChanged();
    }

    private void handleAddSongsAtEnd( final Intent intent ) {
        final List<String> songKeys = intent.getStringArrayListExtra( Playback.EXTRA_SONGS );
        addSongsAtEnd( songKeys );
        publishQueueChanged();
    }

    private void handleMoveSong( final Intent intent ) {
        final int fromIndex = intent.getIntExtra( Playback.EXTRA_FROM_INDEX, -1 );
        final int toIndex = intent.getIntExtra( Playback.EXTRA_TO_INDEX, -1 );
        moveSong( fromIndex, toIndex );
        publishQueueChanged();
    }

    private void handleDeleteSongs( final Intent intent ) {
        final int[] songIndices = intent.getIntArrayExtra( Playback.EXTRA_INDICES );
        deleteSongs( songIndices );
        for( final int index : songIndices ) {
            if( index == getCurrentIndex() ) {
                stop();
                return;
            }
        }
        publishQueueChanged();
    }

    private void handleClearSongs() {
        clearSongs();
        stop();
        publishQueueChanged();
    }

    private void handlePlay() {
        play();
    }

    private void handlePause() {
        pause();
    }

    private void handlePlayPause() {
        playPause();
    }

    private void handleSetPlayPosition( final Intent intent ) {
        final int position = intent.getIntExtra( Playback.EXTRA_POSITION, 0 );
        getCurrentMediaPlayer().play( getCurrentSong(), position );
    }

    private void handleNext() {
        if( nextSong() != -1 ) {
            stop();
            playNewSong();
        } else {
            stopSelf();
        }
    }

    private void handlePrevious() {
        // TODO Implement
    }

    private void handlePlayIndex( final Intent intent ) {
        final int index = intent.getIntExtra( Playback.EXTRA_INDEX, -1 );
        if( setCurrentIndex( index ) != -1 ) {
            stop();
            play();
        } else {
            stopSelf();
        }
    }

    private void handleSetRepeat( final Intent intent ) {
        RepeatMode repeatMode = RepeatMode.valueOf( intent.getStringExtra( Playback.EXTRA_REPEAT_MODE ) );
        updateRepeatMode( repeatMode );
    }

    //
    // Service interface

    @Override
    public synchronized void onHandleIntent( final Intent intent ) {
        init();
        final Playback.Action action = Playback.Action.valueOf( intent.getAction() );
        switch( action ) {
            case STATUS:
                handleStatus();
                break;
            case ADD_SONGS_AFTER_CURRENT:
                handleAddSongsAfterCurrent( intent );
                break;
            case ADD_SONGS_AT_END:
                handleAddSongsAtEnd( intent );
                break;
            case MOVE_SONG:
                handleMoveSong( intent );
                break;
            case DELETE_SONGS:
                handleDeleteSongs( intent );
                break;
            case CLEAR_SONGS:
                handleClearSongs();
                break;
            case PLAY:
                handlePlay();
                break;
            case PAUSE:
                handlePause();
                break;
            case PLAY_PAUSE:
                handlePlayPause();
                break;
            case PLAY_POSITION:
                handleSetPlayPosition( intent );
                break;
            case NEXT:
                handleNext();
                break;
            case PREVIOUS:
                handlePrevious();
                break;
            case PLAY_INDEX:
                handlePlayIndex( intent );
                break;
            case SET_REPEAT:
                handleSetRepeat( intent );
                break;
        }
    }

    @Override
    public synchronized void onDestroy() {
        destroy();
        super.onDestroy();
    }

    private void publishQueueChanged() {
        if( !isInitialized() ) {
            return;
        }
        final Context context;
        final Intent intent;
        synchronized( mLock ) {
            context = mContext;
            intent = new QueueState( mQueue, mCurrentIndex ).getIntent();
        }
        LocalBroadcastManager.getInstance( context ).sendBroadcast( intent );
    }

    private void publishPlayerChanged() {
        if( !isInitialized() ) {
            return;
        }
        final Context context;
        final Intent intent;
        synchronized( mLock ) {
            context = mContext;
            intent = new PlayerState( mCurrentIndex, mPlayState, mCurrentPosition ).getIntent();
        }
        LocalBroadcastManager.getInstance( context ).sendBroadcast( intent );
    }

    private void publishPositionChanged() {
        if( !isInitialized() ) {
            return;
        }
        final Context context;
        final Intent intent;
        synchronized( mLock ) {
            context = mContext;
            intent = new PlayPositionState( mCurrentPosition ).getIntent();
        }
        LocalBroadcastManager.getInstance( context ).sendBroadcast( intent );
    }


    //
    // Play Queue

    private int getCurrentIndex() {
        if( !isInitialized() ) {
            return -1;
        }
        synchronized( mLock ) {
            return mQueue.size() > 0 ? mCurrentIndex : 0;
        }
    }

    private SongDao getCurrentSong() {
        if( !isInitialized() ) {
            return null;
        }
        synchronized( mLock ) {
            if( mQueue.size() < 1 || mCurrentIndex < 0 || mCurrentIndex >= mQueue.size() ) {
                return null;
            }
            return mSongTableAccessor.getSong( mQueue.get( mCurrentIndex ) );
        }
    }

    private void insertSongsAfterCurrent( final List<String> songKeys ) {
        if( !isInitialized() ) {
            return;
        }
        synchronized( mLock ) {
            insertSongs( getCurrentIndex() + 1, songKeys );
        }
    }

    private void addSongsAtEnd( final List<String> songKeys ) {
        synchronized( mLock ) {
            insertSongs( mQueue.size(), songKeys );
        }
    }

    private void insertSongs( final int index, final List<String> songKeys ) {
        if( !isInitialized() ) {
            return;
        }
        synchronized( mLock ) {
            for( final String key : songKeys ) {
                if( index < mQueue.size() ) {
                    mQueue.add( index, key );
                } else {
                    mQueue.add( key );
                }
            }
        }
    }

    private boolean moveSong( final int fromIndex, int toIndex ) {
        if( !isInitialized() ) {
            return false;
        }
        synchronized( mLock ) {
            if( fromIndex < 0 || fromIndex >= mQueue.size() ) {
                return false;
            }
            final String key = mQueue.get( fromIndex );
            mQueue.remove( fromIndex );
            if( toIndex > fromIndex ) {
                toIndex--;
            }
            if( toIndex < mQueue.size() ) {
                mQueue.add( toIndex, key );
            } else {
                toIndex = mQueue.size();
                mQueue.add( key );
            }
            if( mCurrentIndex == fromIndex ) {
                mCurrentIndex = toIndex;
            }

            return true;
        }
    }

    private void deleteSongs( final int[] indices ) {
        if( !isInitialized() ) {
            return;
        }
        synchronized( mLock ) {
            Arrays.sort( indices );
            for( int i = indices.length - 1; i >= 0; i-- ) {
                if( indices[ i ] >= 0 && indices[ i ] < mQueue.size() ) {
                    mQueue.remove( indices[ i ] );
                }
            }
        }
    }

    private void clearSongs() {
        if( !isInitialized() ) {
            return;
        }
        synchronized( mLock ) {
            mQueue.clear();
        }
    }

    private int nextSong() {
        synchronized( mLock ) {
            if( mQueue.size() == 0 ) {
                mCurrentIndex = -1;
            } else if( mCurrentIndex < 0 ) {
                mCurrentIndex = 0;
            } else //noinspection StatementWithEmptyBody
                if( mRepeatMode == RepeatMode.SONG ) {
                    // Do nothing
                } else if( mCurrentIndex == ( mQueue.size() - 1 ) ) {
                    if( mRepeatMode == RepeatMode.ALL ) {
                        mCurrentIndex = 0;
                    } else {
                        mCurrentIndex = -1;
                    }
                } else {
                    mCurrentIndex++;
                }

            return mCurrentIndex;
        }
    }

    private int setCurrentIndex( final int index ) {
        synchronized( mLock ) {
            if( mQueue.size() == 0 ) {
                mCurrentIndex = -1;
            } else if( index < 0 || index >= mQueue.size() ) {
                mCurrentIndex = -1;
            } else {
                mCurrentIndex = index;
            }
            return mCurrentIndex;
        }
    }

    //
    // Player

    private void playNewSong() {
        final SongDao song = getCurrentSong();
        if( song != null ) {
            getCurrentMediaPlayer().play( song, 0 );
        }
    }

    private void play() {
        if( getCurrentMediaPlayer().getState() == PlaybackMediaPlayer.State.STOPPED ) {
            SongDao song = getCurrentSong();
            if( song == null ) {
                if( setCurrentIndex( 0 ) != -1 ) {
                    song = getCurrentSong();
                }
            }
            if( song != null ) {
                getCurrentMediaPlayer().play( song, mCurrentPosition );
            }
        } else {
            getCurrentMediaPlayer().play();
        }
        if( getAlternativeMediaPlayer().getState() == PlaybackMediaPlayer.State.PAUSED ) {
            getAlternativeMediaPlayer().play();
        }
    }

    private void pause() {
        getCurrentMediaPlayer().pause();
        getAlternativeMediaPlayer().pause();
    }

    private void playPause() {
        if( getCurrentMediaPlayer().getState() == PlaybackMediaPlayer.State.PLAYING ) {
            pause();
        } else {
            play();
        }
    }

    private void stop() {
        getCurrentMediaPlayer().stop();
        getAlternativeMediaPlayer().stop();
    }

    private void updateRepeatMode( final RepeatMode repeatMode ) {
        mRepeatMode = repeatMode;
        if( !isInitialized() ) {
            return;
        }
        mCurrentMediaPlayer.setRepeatMode( repeatMode );
        mAlternativeMediaPlayer.setRepeatMode( repeatMode );
    }

    private void updateCrossFade( final boolean crossFadeEnabled, int crossFadeDuration ) {
        crossFadeDuration = crossFadeEnabled ? crossFadeDuration : 0;
        if( !isInitialized() ) {
            return;
        }
        mCurrentMediaPlayer.setCrossFadingDuration( crossFadeDuration );
        mAlternativeMediaPlayer.setCrossFadingDuration( crossFadeDuration );
    }

    public void updateState() {
        synchronized( mLock ) {
            final PlayerState.State currentState =
                    getCurrentMediaPlayer().getState() == PlaybackMediaPlayer.State.PLAYING ? PlayerState.State.PLAYING
                                                                                            : PlayerState.State.PAUSED;
            final PlayerState.State alternateState =
                    getAlternativeMediaPlayer().getState() == PlaybackMediaPlayer.State.PLAYING
                    ? PlayerState.State.PLAYING : PlayerState.State.PAUSED;
            mPlayState = currentState == PlayerState.State.PLAYING || alternateState == PlayerState.State.PLAYING
                         ? PlayerState.State.PLAYING : PlayerState.State.PAUSED;

        }

    }

    private PlaybackMediaPlayer getCurrentMediaPlayer() {
        return mCurrentMediaPlayer;
    }

    private PlaybackMediaPlayer getAlternativeMediaPlayer() {
        return mAlternativeMediaPlayer;
    }

    private void swapMediaPlayer() {
        synchronized( mLock ) {
            final PlaybackMediaPlayer tempPlayer = mCurrentMediaPlayer;
            mCurrentMediaPlayer = mAlternativeMediaPlayer;
            mAlternativeMediaPlayer = tempPlayer;
        }
    }

    private final AudioManagement.AudioFocusListener mAudioFocusListener = new AudioManagement.AudioFocusListener() {

        @Override
        public void audioFocusGained() {
            // Do nothing
        }

        @Override
        public void audioFocusDucked() {
            // Do nothing, music is still playing
        }

        @Override
        public void audioFocusLost() {
            if( !isInitialized() ) {
                return;
            }
            getCurrentMediaPlayer().pause();
            // TODO: mNotificationManager.updateNotification( getCurrentSong() );
        }

        @Override
        public void audioFocusFailed() {
            Toast.makeText( mContext, R.string.close_other_audio_apps, Toast.LENGTH_LONG ).show();
        }
    };

    private final PlaybackMediaPlayer.MediaPlayerStatusListener mMediaPlayerListern
            = new PlaybackMediaPlayer.MediaPlayerStatusListener() {

        @Override
        public void preparePlaying() {
            startForeground( NOTIFICATION_ID, null );
        }

        @Override
        public void songFailed( final SongDao song, final IOException e ) {
            Toast.makeText( mContext, R.string.song_failed_to_load, Toast.LENGTH_SHORT ).show();
            // TODO: backlist song, skip to next song
        }

        @Override
        public void stateChanged( final PlaybackMediaPlayer.State state ) {
            updateState();
            publishPlayerChanged();
        }

        @Override
        public void songStarting( final SongDao song, final int position ) {
            // TODO: Show toast
            publishPlayerChanged();
        }

        @Override
        public void positionChanged( final SongDao song, final int position ) {
            publishPositionChanged();
        }

        @Override
        public void songFinished( final SongDao song ) {
            publishPlayerChanged();
            if( nextSong() != -1 ) {
                swapMediaPlayer();
                playNewSong();
            } else {
                stopSelf();
            }
        }

    };

}
