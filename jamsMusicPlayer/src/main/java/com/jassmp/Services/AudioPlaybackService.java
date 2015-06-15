package com.jassmp.Services;

public class AudioPlaybackService /*extends Service*/ {

    //    //
    //    // private members
    //
    //    //Context and Intent.
    //    private Context mContext;
    //    private Service mService;
    //
    //    //Global Objects Provider.
    //    private Common mApp;
    //
    //    //PrepareServiceListener instance.
    //    private PrepareServiceListener mPrepareServiceListener;
    //
    //    //MediaPlayer objects and flags.
    //    private MediaPlayer mMediaPlayer;
    //    private MediaPlayer mMediaPlayer2;
    //    private int     mCurrentMediaPlayer = 1;
    //    private boolean mFirstRun           = true;
    //
    //    //AudioManager.
    //    private AudioManager       mAudioManager;
    //    private AudioManagerHelper mAudioManagerHelper;
    //
    //    //Flags that indicate whether the mediaPlayers have been initialized.
    //    private boolean mMediaPlayerPrepared  = false;
    //    private boolean mMediaPlayer2Prepared = false;
    //
    //    //Cursor object(s) that will guide the rest of this queue.
    //    private Cursor      mCursor;
    //    private MergeCursor mMergeCursor;
    //
    //    //Holds the indeces of the current cursor, in the order that they'll be played.
    //    private ArrayList<Integer> mPlaybackIndecesList = new ArrayList<Integer>();
    //
    //    //Holds the indeces of songs that were unplayable.
    //    private ArrayList<Integer> mFailedIndecesList = new ArrayList<Integer>();
    //
    //    //Song data helpers for each MediaPlayer object.
    //    private SongHelper mMediaPlayerSongHelper;
    //    private SongHelper mMediaPlayer2SongHelper;
    //
    //    //Pointer variable.
    //    private int mCurrentSongIndex;
    //
    //    //Notification elements.
    //    private NotificationCompat.Builder mNotificationBuilder;
    //    public static final int mNotificationId = 1080;
    //    //NOTE: Using 0 as a notification ID causes Android to ignore the notification call.
    //
    //    //Custom actions for media player controls via the notification bar.
    //    public static final String LAUNCH_NOW_PLAYING_ACTION = "com.jassmp.LAUNCH_NOW_PLAYING_ACTION";
    //    public static final String PREVIOUS_ACTION           = "com.jassmp.PREVIOUS_ACTION";
    //    public static final String PLAY_PAUSE_ACTION         = "com.jassmp.PLAY_PAUSE_ACTION";
    //    public static final String NEXT_ACTION               = "com.jassmp.NEXT_ACTION";
    //    public static final String STOP_SERVICE              = "com.jassmp.STOP_SERVICE";
    //
    //    //Indicates if an enqueue/queue reordering operation was performed on the original queue.
    //    private boolean mEnqueuePerformed = false;
    //
    //    //Handler object.
    //    private Handler mHandler;
    //
    //    //Volume variables that handle the crossfade effect.
    //    private float mFadeOutVolume = 1.0f;
    //    private float mFadeInVolume  = 0.0f;
    //
    //    //Headset plug receiver.
    //    private HeadsetPlugBroadcastReceiver mHeadsetPlugReceiver;
    //
    //    //Crossfade.
    //    private int mCrossfadeDuration;
    //
    //    //A-B Repeat variables.
    //    private int mRepeatSongRangePointA = 0;
    //    private int mRepeatSongRangePointB = 0;
    //
    //    //Indicates if the user changed the track manually.
    //    private boolean mTrackChangedByUser = false;
    //
    //    //RemoteControlClient for use with remote controls and ICS+ lockscreen controls.
    //    private RemoteControlClientCompat mRemoteControlClientCompat;
    //    private ComponentName             mMediaButtonReceiverComponent;
    //
    //    //Enqueue reorder scalar.
    //    private int mEnqueueReorderScalar = 0;
    //
    //    /**
    //     * Initializes the list of pointers to each cursor row.
    //     */
    //    private void initPlaybackIndecesList( boolean playAll ) {
    //        if( getCursor() != null && getPlaybackIndecesList() != null ) {
    //            getPlaybackIndecesList().clear();
    //            for( int i = 0; i < getCursor().getCount(); i++ ) {
    //                getPlaybackIndecesList().add( i );
    //            }
    //
    //            if( isShuffleOn() && !playAll ) {
    //                //Build a new list that doesn't include the current song index.
    //                ArrayList<Integer> newList = new ArrayList<Integer>( getPlaybackIndecesList() );
    //                newList.remove( getCurrentSongIndex() );
    //
    //                //Shuffle the new list.
    //                Collections.shuffle( newList, new Random( System.nanoTime() ) );
    //
    //                //Plug in the current song index back into the new list.
    //                newList.add( getCurrentSongIndex(), getCurrentSongIndex() );
    //                mPlaybackIndecesList = newList;
    //
    //            } else if( isShuffleOn() && playAll ) {
    //                //Shuffle all elements.
    //                Collections.shuffle( getPlaybackIndecesList(), new Random( System.nanoTime() ) );
    //            }
    //
    //        } else {
    //            stopSelf();
    //        }
    //
    //    }
    //
    //    /**
    //     * Called once mMediaPlayer is prepared.
    //     */
    //    public OnPreparedListener mediaPlayerPrepared = new OnPreparedListener() {
    //
    //        @Override
    //        public void onPrepared( MediaPlayer mediaPlayer ) {
    //
    //            //Update the prepared flag.
    //            setIsMediaPlayerPrepared( true );
    //
    //            //Set the completion listener for mMediaPlayer.
    //            getMediaPlayer().setOnCompletionListener( onMediaPlayerCompleted );
    //
    //            //Check to make sure we have AudioFocus.
    //            if( checkAndRequestAudioFocus() == true ) {
    //
    //                //Check if the the user saved the track's last playback position.
    //                if( getMediaPlayerSongHelper().getSavedPosition() != -1 ) {
    //                    //Seek to the saved track position.
    //                    mMediaPlayer.seekTo( (int) getMediaPlayerSongHelper().getSavedPosition() );
    //                    mApp.broadcastUpdateUICommand( new String[]{ Common.SHOW_AUDIOBOOK_TOAST },
    //                                                   new String[]{ "" + getMediaPlayerSongHelper()
    // .getSavedPosition() } );
    //
    //                }
    //
    //                //This is the first time mMediaPlayer has been prepared, so start it immediately.
    //                if( mFirstRun ) {
    //                    startMediaPlayer();
    //                    mFirstRun = false;
    //                }
    //
    //            } else {
    //                return;
    //            }
    //
    //        }
    //
    //    };
    //
    //    /**
    //     * Called once mMediaPlayer2 is prepared.
    //     */
    //    public OnPreparedListener mediaPlayer2Prepared = new OnPreparedListener() {
    //
    //        @Override
    //        public void onPrepared( MediaPlayer mediaPlayer ) {
    //
    //            //Update the prepared flag.
    //            setIsMediaPlayer2Prepared( true );
    //
    //            //Set the completion listener for mMediaPlayer2.
    //            getMediaPlayer2().setOnCompletionListener( onMediaPlayer2Completed );
    //
    //            //Check to make sure we have AudioFocus.
    //            if( checkAndRequestAudioFocus() == true ) {
    //
    //                //Check if the the user saved the track's last playback position.
    //                if( getMediaPlayer2SongHelper().getSavedPosition() != -1 ) {
    //                    //Seek to the saved track position.
    //                    mMediaPlayer2.seekTo( (int) getMediaPlayer2SongHelper().getSavedPosition() );
    //                    mApp.broadcastUpdateUICommand( new String[]{ Common.SHOW_AUDIOBOOK_TOAST }, new String[]{
    //                            "" + getMediaPlayer2SongHelper().getSavedPosition() } );
    //
    //                }
    //
    //            } else {
    //                return;
    //            }
    //
    //        }
    //
    //    };
    //
    //    /**
    //     * Completion listener for mMediaPlayer.
    //     */
    //    private OnCompletionListener onMediaPlayerCompleted = new OnCompletionListener() {
    //
    //        @Override
    //        public void onCompletion( MediaPlayer mp ) {
    //
    //            //Remove the crossfade playback.
    //            mHandler.removeCallbacks( startCrossFadeRunnable );
    //            mHandler.removeCallbacks( crossFadeRunnable );
    //
    //            //Set the track position handler (notifies the handler when the track should start being faded).
    //            if( mHandler != null && mApp.isCrossfadeEnabled() ) {
    //                mHandler.post( startCrossFadeRunnable );
    //            }
    //
    //            //Reset the fadeVolume variables.
    //            mFadeInVolume = 0.0f;
    //            mFadeOutVolume = 1.0f;
    //
    //            //Reset the volumes for both mediaPlayers.
    //            getMediaPlayer().setVolume( 1.0f, 1.0f );
    //            getMediaPlayer2().setVolume( 1.0f, 1.0f );
    //
    //            try {
    //                if( isAtEndOfQueue() && getRepeatMode() != Common.REPEAT_PLAYLIST ) {
    //                    stopSelf();
    //                } else if( isMediaPlayer2Prepared() ) {
    //                    startMediaPlayer2();
    //                } else {
    //                    //Check every 100ms if mMediaPlayer2 is prepared.
    //                    mHandler.post( startMediaPlayer2IfPrepared );
    //                }
    //
    //            } catch( IllegalStateException e ) {
    //                //mMediaPlayer2 isn't prepared yet.
    //                mHandler.post( startMediaPlayer2IfPrepared );
    //            }
    //
    //        }
    //
    //    };
    //
    //    /**
    //     * Completion listener for mMediaPlayer2.
    //     */
    //    private OnCompletionListener onMediaPlayer2Completed = new OnCompletionListener() {
    //
    //        @Override
    //        public void onCompletion( MediaPlayer mp ) {
    //
    //            //Remove the crossfade playback.
    //            mHandler.removeCallbacks( startCrossFadeRunnable );
    //            mHandler.removeCallbacks( crossFadeRunnable );
    //
    //            //Set the track position handler (notifies the handler when the track should start being faded).
    //            if( mHandler != null && mApp.isCrossfadeEnabled() ) {
    //                mHandler.post( startCrossFadeRunnable );
    //            }
    //
    //            //Reset the fadeVolume variables.
    //            mFadeInVolume = 0.0f;
    //            mFadeOutVolume = 1.0f;
    //
    //            //Reset the volumes for both mediaPlayers.
    //            getMediaPlayer().setVolume( 1.0f, 1.0f );
    //            getMediaPlayer2().setVolume( 1.0f, 1.0f );
    //
    //            try {
    //                if( isAtEndOfQueue() && getRepeatMode() != Common.REPEAT_PLAYLIST ) {
    //                    stopSelf();
    //                } else if( isMediaPlayerPrepared() ) {
    //                    startMediaPlayer();
    //                } else {
    //                    //Check every 100ms if mMediaPlayer is prepared.
    //                    mHandler.post( startMediaPlayerIfPrepared );
    //                }
    //
    //            } catch( IllegalStateException e ) {
    //                //mMediaPlayer isn't prepared yet.
    //                mHandler.post( startMediaPlayerIfPrepared );
    //            }
    //
    //        }
    //
    //    };
    //
    //    /**
    //     * Buffering listener.
    //     */
    //    public OnBufferingUpdateListener bufferingListener = new OnBufferingUpdateListener() {
    //
    //        @Override
    //        public void onBufferingUpdate( MediaPlayer mp, int percent ) {
    //
    //            if( mApp.getSharedPreferences().getBoolean( "NOW_PLAYING_ACTIVE", false ) == true ) {
    //
    //                if( mp == getCurrentMediaPlayer() ) {
    //                    float max = mp.getDuration() / 1000;
    //                    float maxDividedByHundred = max / 100;
    //                    mApp.broadcastUpdateUICommand( new String[]{ Common.UPDATE_BUFFERING_PROGRESS },
    //                                                   new String[]{ "" + (int) ( percent * maxDividedByHundred ) } );
    //                }
    //
    //            }
    //
    //        }
    //
    //    };
    //
    //    /**
    //     * Error listener for mMediaPlayer.
    //     */
    //    public OnErrorListener onErrorListener = new OnErrorListener() {
    //
    //        @Override
    //        public boolean onError( MediaPlayer mMediaPlayer, int what, int extra ) {
    //            /* This error listener might seem like it's not doing anything.
    //             * However, removing this will cause the mMediaPlayer object to go crazy
    //			 * and skip around. The key here is to make this method return true. This
    //			 * notifies the mMediaPlayer object that we've handled all errors and that
    //			 * it shouldn't do anything else to try and remedy the situation.
    //			 *
    //			 * TL;DR: Don't touch this interface. Ever.
    //			 */
    //            return true;
    //        }
    //
    //    };
    //
    //    /**
    //     * Starts mMediaPlayer if it is prepared and ready for playback.
    //     * Otherwise, continues checking every 100ms if mMediaPlayer is prepared.
    //     */
    //    private Runnable startMediaPlayerIfPrepared = new Runnable() {
    //
    //        @Override
    //        public void run() {
    //            if( isMediaPlayerPrepared() ) {
    //                startMediaPlayer();
    //            } else {
    //                mHandler.postDelayed( this, 100 );
    //            }
    //
    //
    //        }
    //
    //    };
    //
    //    /**
    //     * Starts mMediaPlayer if it is prepared and ready for playback.
    //     * Otherwise, continues checking every 100ms if mMediaPlayer2 is prepared.
    //     */
    //    private Runnable startMediaPlayer2IfPrepared = new Runnable() {
    //
    //        @Override
    //        public void run() {
    //            if( isMediaPlayer2Prepared() ) {
    //                startMediaPlayer2();
    //            } else {
    //                mHandler.postDelayed( this, 100 );
    //            }
    //
    //
    //        }
    //
    //    };
    //
    //    /**
    //     * First runnable that handles the cross fade operation between two tracks.
    //     */
    //    public Runnable startCrossFadeRunnable = new Runnable() {
    //
    //        @Override
    //        public void run() {
    //
    //            //Check if we're in the last part of the current song.
    //            try {
    //                if( getCurrentMediaPlayer().isPlaying() ) {
    //
    //                    int currentTrackDuration = getCurrentMediaPlayer().getDuration();
    //                    int currentTrackFadePosition = currentTrackDuration - ( mCrossfadeDuration * 1000 );
    //                    if( getCurrentMediaPlayer().getCurrentPosition() >= currentTrackFadePosition ) {
    //                        //Launch the next runnable that will handle the cross fade effect.
    //                        mHandler.postDelayed( crossFadeRunnable, 100 );
    //
    //                    } else {
    //                        mHandler.postDelayed( startCrossFadeRunnable, 1000 );
    //                    }
    //
    //                } else {
    //                    mHandler.postDelayed( startCrossFadeRunnable, 1000 );
    //                }
    //
    //            } catch( Exception e ) {
    //                e.printStackTrace();
    //            }
    //
    //        }
    //
    //    };
    //
    //    /**
    //     * Crossfade runnable.
    //     */
    //    public Runnable crossFadeRunnable = new Runnable() {
    //
    //        @Override
    //        public void run() {
    //            try {
    //
    //                //Do not crossfade if the current song is set to repeat itself.
    //                if( getRepeatMode() != Common.REPEAT_SONG ) {
    //
    //                    //Do not crossfade if this is the last track in the queue.
    //                    if( getCursor().getCount() > ( mCurrentSongIndex + 1 ) ) {
    //
    //                        //Set the next mMediaPlayer's volume and raise it incrementally.
    //                        if( getCurrentMediaPlayer() == getMediaPlayer() ) {
    //
    //                            getMediaPlayer2().setVolume( mFadeInVolume, mFadeInVolume );
    //                            getMediaPlayer().setVolume( mFadeOutVolume, mFadeOutVolume );
    //
    //                            //If the mMediaPlayer is already playing or it hasn't been prepared yet,
    //                            // we can't use crossfade.
    //                            if( !getMediaPlayer2().isPlaying() ) {
    //
    //                                if( mMediaPlayer2Prepared == true ) {
    //
    //                                    if( checkAndRequestAudioFocus() == true ) {
    //
    //                                        //Check if the the user requested to save the track's last playback
    // position.
    //                                        if( getMediaPlayer2SongHelper().getSavedPosition() != -1 ) {
    //                                            //Seek to the saved track position.
    //                                            getMediaPlayer2().seekTo(
    //                                                    (int) getMediaPlayer2SongHelper().getSavedPosition() );
    //                                            mApp.broadcastUpdateUICommand( new String[]{ Common
    // .SHOW_AUDIOBOOK_TOAST },
    //                                                                           new String[]{ ""
    //                                                                                         +
    // getMediaPlayer2SongHelper
    //                                                                                   ().getSavedPosition() } );
    //
    //                                        }
    //
    //                                        getMediaPlayer2().start();
    //                                    } else {
    //                                        return;
    //                                    }
    //
    //                                }
    //
    //                            }
    //
    //                        } else {
    //
    //                            getMediaPlayer().setVolume( mFadeInVolume, mFadeInVolume );
    //                            getMediaPlayer2().setVolume( mFadeOutVolume, mFadeOutVolume );
    //
    //                            //If the mMediaPlayer is already playing or it hasn't been prepared yet,
    //                            // we can't use crossfade.
    //                            if( !getMediaPlayer().isPlaying() ) {
    //
    //                                if( mMediaPlayerPrepared == true ) {
    //
    //                                    if( checkAndRequestAudioFocus() == true ) {
    //
    //                                        //Check if the the user requested to save the track's last playback
    // position.
    //                                        if( getMediaPlayerSongHelper().getSavedPosition() != -1 ) {
    //                                            //Seek to the saved track position.
    //                                            getMediaPlayer().seekTo(
    //                                                    (int) getMediaPlayerSongHelper().getSavedPosition() );
    //                                            mApp.broadcastUpdateUICommand( new String[]{ Common
    // .SHOW_AUDIOBOOK_TOAST },
    //                                                                           new String[]{ "" +
    // getMediaPlayerSongHelper()
    //                                                                                   .getSavedPosition() } );
    //
    //                                        }
    //
    //                                        getMediaPlayer().start();
    //                                    } else {
    //                                        return;
    //                                    }
    //
    //                                }
    //
    //                            }
    //
    //                        }
    //
    //                        mFadeInVolume = mFadeInVolume + (float) ( 1.0f / ( ( (float) mCrossfadeDuration ) * 10
    // .0f ) );
    //                        mFadeOutVolume = mFadeOutVolume - (float) ( 1.0f / ( ( (float) mCrossfadeDuration ) *
    // 10.0f ) );
    //
    //                        mHandler.postDelayed( crossFadeRunnable, 100 );
    //                    }
    //
    //                }
    //
    //            } catch( Exception e ) {
    //                e.printStackTrace();
    //            }
    //
    //        }
    //
    //    };
    //
    //    /**
    //     * Grabs the song parameters at the specified index, retrieves its
    //     * data source, and beings to asynchronously prepare mMediaPlayer.
    //     * Once mMediaPlayer is prepared, mediaPlayerPrepared is called.
    //     *
    //     * @return True if the method completed with no exceptions. False, otherwise.
    //     */
    //    public boolean prepareMediaPlayer( int songIndex ) {
    //
    //        try {
    //
    //            //Stop here if we're at the end of the queue.
    //            if( songIndex == -1 ) {
    //                return true;
    //            }
    //
    //            //Reset mMediaPlayer to it's uninitialized state.
    //            getMediaPlayer().reset();
    //
    //            //Loop the player if the repeat mode is set to repeat the current song.
    //            if( getRepeatMode() == Common.REPEAT_SONG ) {
    //                getMediaPlayer().setLooping( true );
    //            }
    //
    //            //Set mMediaPlayer's song data.
    //            SongHelper songHelper = new SongHelper();
    //            if( mFirstRun ) {
    //                /*
    //                 * We're not preloading the next song (mMediaPlayer2 is not
    //	    		 * playing right now). mMediaPlayer's song is pointed at
    //	    		 * by mCurrentSongIndex.
    //	    		 */
    //                songHelper.populateSongData( mContext, songIndex );
    //                setMediaPlayerSongHelper( songHelper );
    //
    //                //Set this service as a foreground service.
    //                startForeground( mNotificationId, buildNotification( songHelper ) );
    //
    //            } else {
    //                songHelper.populateSongData( mContext, songIndex );
    //                setMediaPlayerSongHelper( songHelper );
    //            }
    //
    //    		/*
    //    		 * Set the data source for mMediaPlayer and start preparing it
    //    		 * asynchronously.
    //    		 */
    //            getMediaPlayer().setDataSource( mContext, getSongDataSource( getMediaPlayerSongHelper() ) );
    //            getMediaPlayer().setOnPreparedListener( mediaPlayerPrepared );
    //            getMediaPlayer().setOnErrorListener( onErrorListener );
    //            getMediaPlayer().prepareAsync();
    //
    //        } catch( Exception e ) {
    //            Log.e( "DEBUG", "MESSAGE", e );
    //            e.printStackTrace();
    //
    //            //Display an error toast to the user.
    //            showErrorToast();
    //
    //            //Add the current song index to the list of failed indeces.
    //            getFailedIndecesList().add( songIndex );
    //
    //            //Start preparing the next song.
    //            if( !isAtEndOfQueue() || mFirstRun ) {
    //                prepareMediaPlayer( songIndex + 1 );
    //            } else {
    //                return false;
    //            }
    //
    //            return false;
    //        }
    //
    //        return true;
    //    }
    //
    //    /**
    //     * Grabs the song parameters at the specified index, retrieves its
    //     * data source, and beings to asynchronously prepare mMediaPlayer2.
    //     * Once mMediaPlayer2 is prepared, mediaPlayer2Prepared is called.
    //     *
    //     * @return True if the method completed with no exceptions. False, otherwise.
    //     */
    //    public boolean prepareMediaPlayer2( int songIndex ) {
    //
    //        try {
    //
    //            //Stop here if we're at the end of the queue.
    //            if( songIndex == -1 ) {
    //                return true;
    //            }
    //
    //            //Reset mMediaPlayer2 to its uninitialized state.
    //            getMediaPlayer2().reset();
    //
    //            //Loop the player if the repeat mode is set to repeat the current song.
    //            if( getRepeatMode() == Common.REPEAT_SONG ) {
    //                getMediaPlayer2().setLooping( true );
    //            }
    //
    //            //Set mMediaPlayer2's song data.
    //            SongHelper songHelper = new SongHelper();
    //            songHelper.populateSongData( mContext, songIndex );
    //            setMediaPlayer2SongHelper( songHelper );
    //
    //    		/*
    //    		 * Set the data source for mMediaPlayer and start preparing it
    //    		 * asynchronously.
    //    		 */
    //            getMediaPlayer2().setDataSource( mContext, getSongDataSource( getMediaPlayer2SongHelper() ) );
    //            getMediaPlayer2().setOnPreparedListener( mediaPlayer2Prepared );
    //            getMediaPlayer2().setOnErrorListener( onErrorListener );
    //            getMediaPlayer2().prepareAsync();
    //
    //        } catch( Exception e ) {
    //            e.printStackTrace();
    //
    //            //Display an error toast to the user.
    //            showErrorToast();
    //
    //            //Add the current song index to the list of failed indeces.
    //            getFailedIndecesList().add( songIndex );
    //
    //            //Start preparing the next song.
    //            if( !isAtEndOfQueue() ) {
    //                prepareMediaPlayer2( songIndex + 1 );
    //            } else {
    //                return false;
    //            }
    //
    //            return false;
    //        }
    //
    //        return true;
    //    }
    //
    //    /**
    //     * Returns the Uri of a song's data source.
    //     * If the song is a local file, its file path is
    //     * returned. If the song is from GMusic, its local
    //     * copy path is returned (if it exists). If no local
    //     * copy exists, the song's remote URL is requested
    //     * from Google's servers and a temporary placeholder
    //     * (URI_BEING_LOADED) is returned.
    //     */
    //    private Uri getSongDataSource( SongHelper songHelper ) {
    //        return Uri.parse( songHelper.getFilePath() );
    //    }
    //
    //    /**
    //     * Updates all open homescreen/lockscreen widgets.
    //     */
    //    public void updateWidgets() {
    //        try {
    //            //Fire a broadcast message to the widget(s) to update them.
    //            Intent smallWidgetIntent = new Intent( mContext, SmallWidgetProvider.class );
    //            smallWidgetIntent.setAction( "android.appwidget.action.APPWIDGET_UPDATE" );
    //            int smallWidgetIds[] = AppWidgetManager.getInstance( mContext )
    //                                                   .getAppWidgetIds(
    //                                                           new ComponentName( mContext,
    // SmallWidgetProvider.class ) );
    //            smallWidgetIntent.putExtra( AppWidgetManager.EXTRA_APPWIDGET_IDS, smallWidgetIds );
    //            mContext.sendBroadcast( smallWidgetIntent );
    //
    //            Intent largeWidgetIntent = new Intent( mContext, LargeWidgetProvider.class );
    //            largeWidgetIntent.setAction( "android.appwidget.action.APPWIDGET_UPDATE" );
    //            int largeWidgetIds[] = AppWidgetManager.getInstance( mContext )
    //                                                   .getAppWidgetIds(
    //                                                           new ComponentName( mContext,
    // LargeWidgetProvider.class ) );
    //            largeWidgetIntent.putExtra( AppWidgetManager.EXTRA_APPWIDGET_IDS, largeWidgetIds );
    //            mContext.sendBroadcast( largeWidgetIntent );
    //
    //            Intent blurredWidgetIntent = new Intent( mContext, BlurredWidgetProvider.class );
    //            blurredWidgetIntent.setAction( "android.appwidget.action.APPWIDGET_UPDATE" );
    //            int blurredWidgetIds[] = AppWidgetManager.getInstance( mContext )
    //                                                     .getAppWidgetIds( new ComponentName( mContext,
    //                                                                                          BlurredWidgetProvider
    // .class
    //                                                     ) );
    //            blurredWidgetIntent.putExtra( AppWidgetManager.EXTRA_APPWIDGET_IDS, blurredWidgetIds );
    //            mContext.sendBroadcast( blurredWidgetIntent );
    //
    //            Intent albumArtWidgetIntent = new Intent( mContext, AlbumArtWidgetProvider.class );
    //            albumArtWidgetIntent.setAction( "android.appwidget.action.APPWIDGET_UPDATE" );
    //            int albumArtWidgetIds[] = AppWidgetManager.getInstance( mContext )
    //                                                      .getAppWidgetIds(
    //                                                              new ComponentName( mContext, AlbumArtWidgetProvider
    //                                                                      .class ) );
    //            albumArtWidgetIntent.putExtra( AppWidgetManager.EXTRA_APPWIDGET_IDS, albumArtWidgetIds );
    //            mContext.sendBroadcast( albumArtWidgetIntent );
    //
    //        } catch( Exception e ) {
    //            e.printStackTrace();
    //        }
    //
    //    }
    //
    //    /**
    //     * Fix for KitKat error where the service is killed as soon
    //     * as the app is swiped away from the Recents menu.
    //     */
    //    @Override
    //    public void onTaskRemoved( Intent rootIntent ) {
    //        Intent intent = new Intent( this, KitKatFixActivity.class );
    //        intent.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
    //        startActivity( intent );
    //
    //    }
    //
    //    /**
    //     * Displays an error toast.
    //     */
    //    private void showErrorToast() {
    //        Toast.makeText( mContext, R.string.song_failed_to_load, Toast.LENGTH_SHORT ).show();
    //    }
    //
    //    /**
    //     * Registers the headset plug receiver.
    //     */
    //    public void registerHeadsetPlugReceiver() {
    //        //Register the headset plug receiver.
    //        if( mApp.getSharedPreferences().getString( "UNPLUG_ACTION",
    // "DO_NOTHING" ).equals( "PAUSE_MUSIC_PLAYBACK" ) ) {
    //            IntentFilter filter = new IntentFilter( Intent.ACTION_HEADSET_PLUG );
    //            mHeadsetPlugReceiver = new HeadsetPlugBroadcastReceiver();
    //            mService.registerReceiver( mHeadsetPlugReceiver, filter );
    //        }
    //
    //    }
    //
    //    /**
    //     * Starts playing mMediaPlayer and sends out the update UI broadcast,
    //     * and updates the notification and any open widgets.
    //     * <p/>
    //     * Do NOT call this method before mMediaPlayer has been prepared.
    //     */
    //    private void startMediaPlayer() throws IllegalStateException {
    //        //Aaaaand let the show begin!
    //        setCurrentMediaPlayer( 1 );
    //        getMediaPlayer().start();
    //
    //        //Set the new value for mCurrentSongIndex.
    //        if( mFirstRun == false ) {
    //            do {
    //                setCurrentSongIndex( determineNextSongIndex() );
    //            } while( getFailedIndecesList().contains( getCurrentSongIndex() ) );
    //
    //            getFailedIndecesList().clear();
    //
    //        } else {
    //            while( getFailedIndecesList().contains( getCurrentSongIndex() ) ) {
    //                setCurrentSongIndex( determineNextSongIndex() );
    //            }
    //
    //            //Initialize the crossfade runnable.
    //            if( mHandler != null && mApp.isCrossfadeEnabled() ) {
    //                mHandler.post( startCrossFadeRunnable );
    //            }
    //
    //        }
    //
    //
    //        //Update the UI.
    //        String[] updateFlags = new String[]{ Common.UPDATE_PAGER_POSTIION, Common.UPDATE_PLAYBACK_CONTROLS,
    //                                             Common.HIDE_STREAMING_BAR, Common.UPDATE_SEEKBAR_DURATION,
    //                                             Common.UPDATE_EQ_FRAGMENT };
    //
    //        String[] flagValues = new String[]{ getCurrentSongIndex() + "", "", "",
    // getMediaPlayer().getDuration() + "",
    //                                            "" };
    //
    //        mApp.broadcastUpdateUICommand( updateFlags, flagValues );
    //        setCurrentSong( getCurrentSong() );
    //
    //        //Start preparing the next song.
    //        prepareMediaPlayer2( determineNextSongIndex() );
    //    }
    //
    //    /**
    //     * Starts playing mMediaPlayer2, sends out the update UI broadcast,
    //     * and updates the notification and any open widgets.
    //     * <p/>
    //     * Do NOT call this method before mMediaPlayer2 has been prepared.
    //     */
    //    private void startMediaPlayer2() throws IllegalStateException {
    //        //Aaaaaand let the show begin!
    //        setCurrentMediaPlayer( 2 );
    //        getMediaPlayer2().start();
    //
    //        //Set the new value for mCurrentSongIndex.
    //        do {
    //            setCurrentSongIndex( determineNextSongIndex() );
    //        } while( getFailedIndecesList().contains( getCurrentSongIndex() ) );
    //
    //        getFailedIndecesList().clear();
    //
    //        //Update the UI.
    //        String[] updateFlags = new String[]{ Common.UPDATE_PAGER_POSTIION, Common.UPDATE_PLAYBACK_CONTROLS,
    //                                             Common.HIDE_STREAMING_BAR, Common.UPDATE_SEEKBAR_DURATION,
    //                                             Common.UPDATE_EQ_FRAGMENT };
    //
    //        String[] flagValues = new String[]{ getCurrentSongIndex() + "", "", "",
    // getMediaPlayer2().getDuration() + "",
    //                                            "" };
    //
    //        mApp.broadcastUpdateUICommand( updateFlags, flagValues );
    //        setCurrentSong( getCurrentSong() );
    //
    //        //Start preparing the next song.
    //        prepareMediaPlayer( determineNextSongIndex() );
    //    }
    //
    //    /**
    //     * Starts/resumes the current media player. Returns true if
    //     * the operation succeeded. False, otherwise.
    //     */
    //    public boolean startPlayback() {
    //
    //        try {
    //            //Check to make sure we have audio focus.
    //            if( checkAndRequestAudioFocus() ) {
    //                getCurrentMediaPlayer().start();
    //
    //                //Update the UI and scrobbler.
    //                String[] updateFlags = new String[]{ Common.UPDATE_PLAYBACK_CONTROLS };
    //                String[] flagValues = new String[]{ "" };
    //
    //                mApp.broadcastUpdateUICommand( updateFlags, flagValues );
    //                updateNotification( mApp.getAudioPlaybackService().getCurrentSong() );
    //                updateWidgets();
    //            } else {
    //                return false;
    //            }
    //
    //        } catch( Exception e ) {
    //            e.printStackTrace();
    //            return false;
    //        }
    //
    //        return true;
    //    }
    //
    //    /**
    //     * Pauses the current media player. Returns true if
    //     * the operation succeeded. False, otherwise.
    //     */
    //    public boolean pausePlayback() {
    //
    //        try {
    //            getCurrentMediaPlayer().pause();
    //
    //            //Update the UI and scrobbler.
    //            String[] updateFlags = new String[]{ Common.UPDATE_PLAYBACK_CONTROLS };
    //            String[] flagValues = new String[]{ "" };
    //
    //            mApp.broadcastUpdateUICommand( updateFlags, flagValues );
    //            updateNotification( mApp.getAudioPlaybackService().getCurrentSong() );
    //            updateWidgets();
    //
    //        } catch( Exception e ) {
    //            e.printStackTrace();
    //            return false;
    //        }
    //
    //        return true;
    //    }
    //
    //    /**
    //     * Skips to the next track (if there is one) and starts
    //     * playing it. Returns true if the operation succeeded.
    //     * False, otherwise.
    //     */
    //    public boolean skipToNextTrack() {
    //        try {
    //            //Reset both MediaPlayer objects.
    //            getMediaPlayer().reset();
    //            getMediaPlayer2().reset();
    //            clearCrossfadeCallbacks();
    //
    //            //Loop the players if the repeat mode is set to repeat the current song.
    //            if( getRepeatMode() == Common.REPEAT_SONG ) {
    //                getMediaPlayer().setLooping( true );
    //                getMediaPlayer2().setLooping( true );
    //            }
    //
    //            //Remove crossfade runnables and reset all volume levels.
    //            getHandler().removeCallbacks( crossFadeRunnable );
    //            getMediaPlayer().setVolume( 1.0f, 1.0f );
    //            getMediaPlayer2().setVolume( 1.0f, 1.0f );
    //
    //            //Increment the song index.
    //            incrementCurrentSongIndex();
    //
    //            //Update the UI.
    //            String[] updateFlags = new String[]{ Common.UPDATE_PAGER_POSTIION };
    //            String[] flagValues = new String[]{ getCurrentSongIndex() + "" };
    //            mApp.broadcastUpdateUICommand( updateFlags, flagValues );
    //
    //            //Start the playback process.
    //            mFirstRun = true;
    //            prepareMediaPlayer( getCurrentSongIndex() );
    //
    //        } catch( Exception e ) {
    //            e.printStackTrace();
    //            return false;
    //        }
    //
    //        return true;
    //    }
    //
    //    /**
    //     * Skips to the previous track (if there is one) and starts
    //     * playing it. Returns true if the operation succeeded.
    //     * False, otherwise.
    //     */
    //    public boolean skipToPreviousTrack() {
    //
    //        /*
    //         * If the current track is not within the first three seconds,
    //         * reset it. If it IS within the first three seconds, skip to the
    //         * previous track.
    //         */
    //        try {
    //            if( getCurrentMediaPlayer().getCurrentPosition() > 3000 ) {
    //                getCurrentMediaPlayer().seekTo( 0 );
    //                return true;
    //            }
    //
    //        } catch( Exception e ) {
    //            e.printStackTrace();
    //            return false;
    //        }
    //
    //        try {
    //            //Reset both MediaPlayer objects.
    //            getMediaPlayer().reset();
    //            getMediaPlayer2().reset();
    //            clearCrossfadeCallbacks();
    //
    //            //Loop the players if the repeat mode is set to repeat the current song.
    //            if( getRepeatMode() == Common.REPEAT_SONG ) {
    //                getMediaPlayer().setLooping( true );
    //                getMediaPlayer2().setLooping( true );
    //            }
    //
    //            //Remove crossfade runnables and reset all volume levels.
    //            getHandler().removeCallbacks( crossFadeRunnable );
    //            getMediaPlayer().setVolume( 1.0f, 1.0f );
    //            getMediaPlayer2().setVolume( 1.0f, 1.0f );
    //
    //            //Decrement the song index.
    //            decrementCurrentSongIndex();
    //
    //            //Update the UI.
    //            String[] updateFlags = new String[]{ Common.UPDATE_PAGER_POSTIION };
    //            String[] flagValues = new String[]{ getCurrentSongIndex() + "" };
    //            mApp.broadcastUpdateUICommand( updateFlags, flagValues );
    //
    //            //Start the playback process.
    //            mFirstRun = true;
    //            prepareMediaPlayer( getCurrentSongIndex() );
    //
    //        } catch( Exception e ) {
    //            e.printStackTrace();
    //            return false;
    //        }
    //
    //        return true;
    //    }
    //
    //    /**
    //     * Skips to the specified track index (if there is one) and starts
    //     * playing it. Returns true if the operation succeeded.
    //     * False, otherwise.
    //     */
    //    public boolean skipToTrack( int trackIndex ) {
    //        try {
    //            //Reset both MediaPlayer objects.
    //            getMediaPlayer().reset();
    //            getMediaPlayer2().reset();
    //            clearCrossfadeCallbacks();
    //
    //            //Loop the players if the repeat mode is set to repeat the current song.
    //            if( getRepeatMode() == Common.REPEAT_SONG ) {
    //                getMediaPlayer().setLooping( true );
    //                getMediaPlayer2().setLooping( true );
    //            }
    //
    //            //Remove crossfade runnables and reset all volume levels.
    //            getHandler().removeCallbacks( crossFadeRunnable );
    //            getMediaPlayer().setVolume( 1.0f, 1.0f );
    //            getMediaPlayer2().setVolume( 1.0f, 1.0f );
    //
    //            //Update the song index.
    //            setCurrentSongIndex( trackIndex );
    //
    //            //Update the UI.
    //            String[] updateFlags = new String[]{ Common.UPDATE_PAGER_POSTIION };
    //            String[] flagValues = new String[]{ getCurrentSongIndex() + "" };
    //            mApp.broadcastUpdateUICommand( updateFlags, flagValues );
    //
    //            //Start the playback process.
    //            mFirstRun = true;
    //            prepareMediaPlayer( trackIndex );
    //
    //        } catch( Exception e ) {
    //            e.printStackTrace();
    //            return false;
    //        }
    //
    //        return true;
    //    }
    //
    //    /**
    //     * Toggles the playback state between playing and paused and
    //     * returns whether the current media player is now playing
    //     * music or not.
    //     */
    //    public boolean togglePlaybackState() {
    //        if( isPlayingMusic() ) {
    //            pausePlayback();
    //        } else {
    //            startPlayback();
    //        }
    //
    //        return isPlayingMusic();
    //    }
    //
    //    /**
    //     * Checks which MediaPlayer object is currently in use, and
    //     * starts preparing the other one.
    //     */
    //    public void prepareAlternateMediaPlayer() {
    //        if( mCurrentMediaPlayer == 1 ) {
    //            prepareMediaPlayer2( determineNextSongIndex() );
    //        } else {
    //            prepareMediaPlayer( determineNextSongIndex() );
    //        }
    //
    //    }
    //
    //    /**
    //     * Toggles shuffle mode and returns whether shuffle is now on or off.
    //     */
    //    public boolean toggleShuffleMode() {
    //        if( isShuffleOn() ) {
    //            //Set shuffle off.
    //            mApp.getSharedPreferences().edit().putBoolean( Common.SHUFFLE_ON, false ).commit();
    //
    //            //Save the element at the current index.
    //            int currentElement = getPlaybackIndecesList().get( getCurrentSongIndex() );
    //
    //            //Reset the cursor pointers list.
    //            Collections.sort( getPlaybackIndecesList() );
    //
    //            //Reset the current index to the index of the old element.
    //            setCurrentSongIndex( getPlaybackIndecesList().indexOf( currentElement ) );
    //
    //
    //        } else {
    //            //Set shuffle on.
    //            mApp.getSharedPreferences().edit().putBoolean( Common.SHUFFLE_ON, true ).commit();
    //
    //            //Build a new list that doesn't include the current song index.
    //            ArrayList<Integer> newList = new ArrayList<Integer>( getPlaybackIndecesList() );
    //            newList.remove( getCurrentSongIndex() );
    //
    //            //Shuffle the new list.
    //            Collections.shuffle( newList, new Random( System.nanoTime() ) );
    //
    //            //Plug in the current song index back into the new list.
    //            newList.add( getCurrentSongIndex(), getCurrentSongIndex() );
    //            mPlaybackIndecesList = newList;
    //
    //            //Collections.shuffle(getPlaybackIndecesList().subList(0, getCurrentSongIndex()));
    //            //Collections.shuffle(getPlaybackIndecesList().subList(getCurrentSongIndex()+1,
    //            // getPlaybackIndecesList().size()));
    //
    //        }
    //
    //    	/* Since the queue changed, we're gonna have to update the
    //    	 * next MediaPlayer object with the new song info.
    //    	 */
    //        prepareAlternateMediaPlayer();
    //
    //        //Update all UI elements with the new queue order.
    //        mApp.broadcastUpdateUICommand( new String[]{ Common.NEW_QUEUE_ORDER }, new String[]{ "" } );
    //        return isShuffleOn();
    //    }
    //
    //    /**
    //     * Applies the specified repeat mode.
    //     */
    //    public void setRepeatMode( int repeatMode ) {
    //        if( repeatMode == Common.REPEAT_OFF || repeatMode == Common.REPEAT_PLAYLIST ||
    //            repeatMode == Common.REPEAT_SONG || repeatMode == Common.A_B_REPEAT ) {
    //            //Save the repeat mode.
    //            mApp.getSharedPreferences().edit().putInt( Common.REPEAT_MODE, repeatMode ).commit();
    //        } else {
    //            //Just in case a bogus value is passed in.
    //            mApp.getSharedPreferences().edit().putInt( Common.REPEAT_MODE, Common.REPEAT_OFF ).commit();
    //        }
    //
    //    	/*
    //    	 * Set the both MediaPlayer objects to loop if the repeat mode
    //    	 * is Common.REPEAT_SONG.
    //    	 */
    //        try {
    //            if( repeatMode == Common.REPEAT_SONG ) {
    //                getMediaPlayer().setLooping( true );
    //                getMediaPlayer2().setLooping( true );
    //            } else {
    //                getMediaPlayer().setLooping( false );
    //                getMediaPlayer2().setLooping( false );
    //            }
    //
    //            //Prepare the appropriate next song.
    //            prepareAlternateMediaPlayer();
    //
    //        } catch( Exception e ) {
    //            e.printStackTrace();
    //        }
    //
    //        /*
    //         * Remove the crossfade callbacks and reinitalize them
    //         * only if the user didn't select A-B repeat.
    //         */
    //        clearCrossfadeCallbacks();
    //
    //        if( repeatMode != Common.A_B_REPEAT ) {
    //            if( mHandler != null && mApp.isCrossfadeEnabled() ) {
    //                mHandler.post( startCrossFadeRunnable );
    //            }
    //        }
    //
    //    }
    //
    //    /**
    //     * Returns the current active MediaPlayer object.
    //     */
    //    public MediaPlayer getCurrentMediaPlayer() {
    //        if( mCurrentMediaPlayer == 1 ) {
    //            return mMediaPlayer;
    //        } else {
    //            return mMediaPlayer2;
    //        }
    //    }
    //
    //    /**
    //     * Returns the primary MediaPlayer object. Don't
    //     * use this method directly unless you have a good
    //     * reason to explicitly call mMediaPlayer. Use
    //     * getCurrentMediaPlayer() whenever possible.
    //     */
    //    public MediaPlayer getMediaPlayer() {
    //        return mMediaPlayer;
    //    }
    //
    //    /**
    //     * Returns the secondary MediaPlayer object. Don't
    //     * use this method directly unless you have a good
    //     * reason to explicitly call mMediaPlayer2. Use
    //     * getCurrentMediaPlayer() whenever possible.
    //     */
    //    public MediaPlayer getMediaPlayer2() {
    //        return mMediaPlayer2;
    //    }
    //
    //    /**
    //     * Indicates if mMediaPlayer is prepared and
    //     * ready for playback.
    //     */
    //    public boolean isMediaPlayerPrepared() {
    //        return mMediaPlayerPrepared;
    //    }
    //
    //    /**
    //     * Indicates if mMediaPlayer2 is prepared and
    //     * ready for playback.
    //     */
    //    public boolean isMediaPlayer2Prepared() {
    //        return mMediaPlayer2Prepared;
    //    }
    //
    //    /**
    //     * Indicates if music is currently playing.
    //     */
    //    public boolean isPlayingMusic() {
    //        try {
    //            if( getCurrentMediaPlayer().isPlaying() ) {
    //                return true;
    //            } else {
    //                return false;
    //            }
    //
    //        } catch( Exception e ) {
    //            e.printStackTrace();
    //            return false;
    //        }
    //
    //    }
    //
    //    /**
    //     * Returns an instance of SongHelper. This
    //     * object can be used to pull details about
    //     * the current song.
    //     */
    //    public SongHelper getCurrentSong() {
    //        if( getCurrentMediaPlayer() == mMediaPlayer ) {
    //            return mMediaPlayerSongHelper;
    //        } else {
    //            return mMediaPlayer2SongHelper;
    //        }
    //
    //    }
    //
    //    /**
    //     * Removes all crossfade callbacks on the current
    //     * Handler object. Also resets the volumes of the
    //     * MediaPlayer objects to 1.0f.
    //     */
    //    private void clearCrossfadeCallbacks() {
    //        if( mHandler == null ) {
    //            return;
    //        }
    //
    //        mHandler.removeCallbacks( startCrossFadeRunnable );
    //        mHandler.removeCallbacks( crossFadeRunnable );
    //
    //        try {
    //            getMediaPlayer().setVolume( 1.0f, 1.0f );
    //            getMediaPlayer2().setVolume( 1.0f, 1.0f );
    //        } catch( IllegalStateException e ) {
    //            e.printStackTrace();
    //        }
    //
    //    }
    //
    //    /**
    //     * Returns the current repeat mode. The repeat mode
    //     * is determined based on the value that is saved in
    //     * SharedPreferences.
    //     */
    //    public int getRepeatMode() {
    //        return mApp.getSharedPreferences().getInt( Common.REPEAT_MODE, Common.REPEAT_OFF );
    //    }
    //
    //    /**
    //     * Indicates if shuffle mode is turned on or off.
    //     */
    //    public boolean isShuffleOn() {
    //        return mApp.getSharedPreferences().getBoolean( Common.SHUFFLE_ON, false );
    //    }
    //
    //    /**
    //     * (non-Javadoc)
    //     *
    //     * @see android.app.Service#onDestroy()
    //     */
    //    @Override
    //    public void onDestroy() {
    //
    //        //Notify the UI that the service is about to stop.
    //        mApp.broadcastUpdateUICommand( new String[]{ Common.SERVICE_STOPPING }, new String[]{ "" } );
    //
    //        //Fire a broadcast message to the widget(s) to update them.
    //        updateWidgets();
    //
    //        //Save the last track's info within the current queue.
    //        try {
    //            mApp.getSharedPreferences()
    //                .edit()
    //                .putLong( "LAST_SONG_TRACK_POSITION", getCurrentMediaPlayer().getCurrentPosition() );
    //        } catch( Exception e ) {
    //            e.printStackTrace();
    //            mApp.getSharedPreferences().edit().putLong( "LAST_SONG_TRACK_POSITION", 0 );
    //        }
    //
    //        //If the current song is repeating a specific range, reset the repeat option.
    //        if( getRepeatMode() == Common.REPEAT_SONG ) {
    //            setRepeatMode( Common.REPEAT_OFF );
    //        }
    //
    //        mFadeInVolume = 0.0f;
    //        mFadeOutVolume = 1.0f;
    //
    //        //Unregister the headset plug receiver and RemoteControlClient.
    //        try {
    //            RemoteControlHelper.unregisterRemoteControlClient( mAudioManager, mRemoteControlClientCompat );
    //            unregisterReceiver( mHeadsetPlugReceiver );
    //        } catch( Exception e ) {
    //            //Just null out the receiver if it hasn't been registered yet.
    //            mHeadsetPlugReceiver = null;
    //        }
    //
    //        //Remove the notification.
    //        NotificationManager notificationManager = (NotificationManager) this.getSystemService(
    // NOTIFICATION_SERVICE );
    //        notificationManager.cancel( mNotificationId );
    //
    //        if( mMediaPlayer != null ) {
    //            mMediaPlayer.release();
    //        }
    //
    //        if( mMediaPlayer2 != null ) {
    //            getMediaPlayer2().release();
    //        }
    //
    //        mMediaPlayer = null;
    //        mMediaPlayer2 = null;
    //
    //        //Close the cursor(s).
    //        try {
    //            getCursor().close();
    //            setCursor( null );
    //        } catch( Exception e ) {
    //            e.printStackTrace();
    //        }
    //
    //        /*
    //         * If A-B repeat is enabled, disable it to prevent the
    //         * next service instance from repeating the same section
    //         * over and over on the new track.
    //         */
    //        if( getRepeatMode() == Common.A_B_REPEAT ) {
    //            setRepeatMode( Common.REPEAT_OFF );
    //        }
    //
    //        //Remove audio focus and unregister the audio buttons receiver.
    //        mAudioManagerHelper.setHasAudioFocus( false );
    //        mAudioManager.abandonAudioFocus( audioFocusChangeListener );
    //        mAudioManager.unregisterMediaButtonEventReceiver(
    //                new ComponentName( getPackageName(), HeadsetButtonsReceiver.class.getName() ) );
    //        mAudioManager = null;
    //        mMediaButtonReceiverComponent = null;
    //        mRemoteControlClientCompat = null;
    //
    //        //Nullify the service object.
    //        mApp.setService( null );
    //        mApp.setIsServiceRunning( false );
    //        mApp = null;
    //
    //    }
    //
    //    /**
    //     * Interface implementation to listen for service cursor events.
    //     */
    //    public BuildCursorListener buildCursorListener = new BuildCursorListener() {
    //
    //        @Override
    //        public void onServiceCursorReady( Cursor cursor, int currentSongIndex, boolean playAll ) {
    //
    //            if( cursor.getCount() == 0 ) {
    //                Toast.makeText( mContext, R.string.no_audio_files_found, Toast.LENGTH_SHORT ).show();
    //                if( mApp.getNowPlayingActivity() != null ) {
    //                    mApp.getNowPlayingActivity().finish();
    //                }
    //
    //                return;
    //            }
    //
    //            setCursor( cursor );
    //            setCurrentSongIndex( currentSongIndex );
    //            getFailedIndecesList().clear();
    //            initPlaybackIndecesList( playAll );
    //            mFirstRun = true;
    //            prepareMediaPlayer( currentSongIndex );
    //
    //            //Notify NowPlayingActivity to initialize its ViewPager.
    //            mApp.broadcastUpdateUICommand( new String[]{ Common.INIT_PAGER }, new String[]{ "" } );
    //
    //        }
    //
    //        @Override
    //        public void onServiceCursorFailed( String exceptionMessage ) {
    //            //We don't have a valid cursor, so stop the service.
    //            Log.e( "SERVICE CURSOR EXCEPTION", "onServiceCursorFailed(): " + exceptionMessage );
    //            Toast.makeText( mContext, R.string.unable_to_start_playback, Toast.LENGTH_SHORT ).show();
    //            stopSelf();
    //
    //        }
    //
    //        @Override
    //        public void onServiceCursorUpdated( Cursor cursor ) {
    //            //Make sure the new cursor and the old cursor are the same size.
    //            if( getCursor().getCount() == cursor.getCount() ) {
    //                setCursor( cursor );
    //            }
    //
    //        }
    //
    //    };

}
