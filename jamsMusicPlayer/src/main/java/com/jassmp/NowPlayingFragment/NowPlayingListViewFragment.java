package com.jassmp.NowPlayingFragment;

import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.jassmp.Dao.SongDao;
import com.jassmp.GuiHelper.UIElementsHelper;
import com.jassmp.JassMpDb.SongTableAccessor;
import com.jassmp.Playback.PlayPositionState;
import com.jassmp.Playback.Playback;
import com.jassmp.Playback.PlaybackStateListener;
import com.jassmp.Playback.PlayerState;
import com.jassmp.Playback.QueueState;
import com.jassmp.Preferences.Preferences;
import com.jassmp.R;
import com.jassmp.Utils.Common;
import com.mobeta.android.dslv.DragSortListView;
import com.mobeta.android.dslv.SimpleFloatViewManager;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class NowPlayingListViewFragment extends Fragment implements PlaybackStateListener {
    public static final String SEEK_BAR_PROGRESS_PROPERTY_NAME = "progress";
    public static final int    SEEKBAR_SMOOTH_SCROLL_DURATION  = 200;

    //
    // private members

    private       Context     mContext     = null;
    private       Playback    mPlayback    = null;
    private       Preferences mPreferences = null;
    private final Object      mLock        = new Object();

    // TODO: Refactor player into own class
    // TODO: Fadin in player
    private View           mRootView             = null;
    private RelativeLayout mMiniPlayerLayout     = null;
    private View           mMiniPlayerSongLayout = null;
    private ImageView      mMiniPlayerAlbumArt   = null;
    private RelativeLayout mPlayPauseBackground  = null;
    private ImageButton    mPlayPauseButton      = null;
    private ImageButton    mNextButton           = null;
    private ImageButton    mPreviousButton       = null;
    private ImageButton    mRepeatButton         = null;
    private TextView       mPosition             = null;
    private TextView       mTitleText            = null;
    private TextView       mSubText              = null;
    private SeekBar        mSeekBar              = null;


    private DragSortListView              mListView        = null;
    private NowPlayingListViewItemAdapter mListViewAdapter = null;
    private TextView                      mEmptyInfoText   = null;

    private boolean mIsPlaying       = false;
    private boolean mShowElapsedTime = true;
    private boolean mIsSeeking       = false;


    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState ) {
        synchronized( mLock ) {
            mContext = getActivity().getApplicationContext();
            mPreferences = new Preferences( mContext );
            mPlayback = new Playback( mContext, this );
            mShowElapsedTime = mPreferences.getShowElapsedTime();

            mRootView = inflater.inflate( R.layout.fragment_now_playing, container, false );
            mRootView.setBackgroundColor( UIElementsHelper.getGridViewBackground( mContext ) );
            setHasOptionsMenu( true );

            initMiniPlayer( mRootView );
            initListViewer( mRootView );
            reloadPlayState();

            return mRootView;
        }
    }

    private void initMiniPlayer( final View rootView ) {
        mMiniPlayerLayout = (RelativeLayout) rootView.findViewById( R.id.player_layout );
        mMiniPlayerSongLayout = rootView.findViewById( R.id.player_songLayout );
        mMiniPlayerAlbumArt = (ImageView) rootView.findViewById( R.id.player_albumArt );
        mPosition = (TextView) rootView.findViewById( R.id.player_position );
        mPlayPauseBackground = (RelativeLayout) rootView.findViewById( R.id.player_playPause_layout );
        mPlayPauseButton = (ImageButton) rootView.findViewById( R.id.player_playPause );
        mNextButton = (ImageButton) rootView.findViewById( R.id.player_nextSong );
        mPreviousButton = (ImageButton) rootView.findViewById( R.id.player_previousSong );
        mRepeatButton = (ImageButton) rootView.findViewById( R.id.player_repeat );
        mTitleText = (TextView) rootView.findViewById( R.id.player_song );
        mSubText = (TextView) rootView.findViewById( R.id.player_albumAndArtist );
        mSeekBar = (SeekBar) rootView.findViewById( R.id.player_seekBar );

        mPlayPauseBackground.setBackgroundResource( UIElementsHelper.getShadowedCircle( mContext ) );
        mTitleText.setTypeface( UIElementsHelper.getRegularTypeface( mContext ) );
        mSubText.setTypeface( UIElementsHelper.getRegularTypeface( mContext ) );
        mSeekBar.setThumb( getResources().getDrawable( R.drawable.transparent_drawable ) );

        mPosition.setOnClickListener( mPositionClickListener );
        mMiniPlayerSongLayout.setOnClickListener( mSongClickListener );
        mPlayPauseBackground.setOnClickListener( mPlayPauseClickListener );
        mPlayPauseButton.setOnClickListener( mPlayPauseClickListener );
        mNextButton.setOnClickListener( mNextClickListener );
        mPreviousButton.setOnClickListener( mPreviousClickListener );
        mRepeatButton.setOnClickListener( mRepeatClickListener );
        mSeekBar.setOnSeekBarChangeListener( mSeekBarChangeListener );
    }

    private void initListViewer( final View rootView ) {
        mListView = (DragSortListView) rootView.findViewById( R.id.now_playing_list_view );
        mEmptyInfoText = (TextView) rootView.findViewById( R.id.now_playing_empty_text );

        mListView.requestFocusFromTouch();
        mListView.setVerticalScrollBarEnabled( false );
        //Apply the ListViews' dividers.
        switch( mPreferences.getCurrentTheme() ) {
            case DARK:
                mListView.setDivider( mContext.getResources().getDrawable( R.drawable.icon_list_divider ) );
                break;
            case LIGHT:
                mListView.setDivider( mContext.getResources().getDrawable( R.drawable.icon_list_divider_light ) );
                break;
        }
        mListView.setDividerHeight( 1 );
        mListView.setFastScrollEnabled( true );

        if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT ) {
            int topPadding = Common.getStatusBarHeight( mContext );

            //Calculate navigation bar height.
            int navigationBarHeight = 0;
            int resourceId = getResources().getIdentifier( "navigation_bar_height", "dimen", "android" );
            if( resourceId > 0 ) {
                navigationBarHeight = getResources().getDimensionPixelSize( resourceId );
            }

            mListView.setClipToPadding( false );
            mListView.setPadding( 0, topPadding, 0, navigationBarHeight );
        }

        mListView.setOnItemClickListener( mOnItemClickListener );
        mListView.setDropListener( mOnDropListener );
        mListView.setRemoveListener( mOnRemoveListener );

        SimpleFloatViewManager simpleFloatViewManager = new SimpleFloatViewManager( mListView );
        simpleFloatViewManager.setBackgroundColor( Color.TRANSPARENT );
        mListView.setFloatViewManager( simpleFloatViewManager );

        mListViewAdapter = new NowPlayingListViewItemAdapter( mContext, new ArrayList<String>(), -1 );
        mListView.setAdapter( mListViewAdapter );

        mEmptyInfoText.setTypeface( UIElementsHelper.getRegularTypeface( mContext ) );
        mEmptyInfoText.setPaintFlags(
                mEmptyInfoText.getPaintFlags() | Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG );

        toggleEmptyPresentation( true );
    }


    @Override
    public void onResume() {
        super.onResume();
        synchronized( mLock ) {
            if( !isInitialized() ) {
                return;
            }
            if( getActivity().getActionBar() != null ) {
                getActivity().getActionBar().setTitle( mContext.getResources().getString( R.string.now_playing ) );
            }
            mPlayPauseBackground.setBackgroundResource( UIElementsHelper.getShadowedCircle( mContext ) );
            reloadPlayState();
        }
    }

    @Override
    public void onDestroyView() {
        synchronized( mLock ) {
            mContext = null;
            mListViewAdapter = null;
            mPlayback.destroy();
            super.onDestroyView();
        }
    }

    private boolean isInitialized() {
        synchronized( mLock ) {
            return mContext != null;
        }
    }

    @Override
    public void onPlayStateChanged( final PlayerState state ) {
        updateMiniPlayerState( state );
        updateListAdapterIndex( state );
    }

    @Override
    public void onQueueChanged( final QueueState state ) {
        updateListAdapterQueue( state );
    }

    @Override
    public void onPlayPositionChanged( final PlayPositionState state ) {
        updateMiniPlayerPosition( state );
    }

    private void reloadPlayState() {
        synchronized( mLock ) {
            synchronized( mLock ) {
                if( !isInitialized() ) {
                    return;
                }
                mPlayback.status();
            }
        }
    }

    public void toggleEmptyPresentation( final boolean queueIsEmpty ) {
        if( queueIsEmpty ) {
            mRootView.post( new Runnable() {
                @Override
                public void run() {
                    mMiniPlayerLayout.setVisibility( View.GONE );
                    mListView.setVisibility( View.INVISIBLE );
                    mEmptyInfoText.setVisibility( View.VISIBLE );
                }
            } );
        } else {
            mRootView.post( new Runnable() {
                @Override
                public void run() {
                    mEmptyInfoText.setVisibility( View.INVISIBLE );
                    mListView.setVisibility( View.VISIBLE );
                }
            } );

        }
    }

    private void updateMiniPlayerState( final PlayerState playerState ) {
        synchronized( mLock ) {
            if( !isInitialized() ) {
                return;
            }
            mRootView.post( new Runnable() {
                @Override
                public void run() {
                    switch( playerState.getState() ) {
                        case PAUSED:
                            animatePauseToPlay();
                            break;
                        case PLAYING:
                            animatePlayToPause();
                            break;
                    }
                    mMiniPlayerLayout.setVisibility( View.VISIBLE );
                    final String key = mListViewAdapter.getItem( playerState.getCurrentIndex() );
                    final SongDao song = key != null ? SongTableAccessor.getInstance( mContext ).getSong( key ) : null;
                    final String title = song != null ? song.getTitle() : "";
                    final String subTitle = song != null ? song.getAlbum() + " - " + song.getArtist() : "";
                    mTitleText.setText( title );
                    mSubText.setText( subTitle );
                    updateRepeatMode( playerState );
                    mSeekBar.setMax( song != null ? song.getDuration() : 0 );
                    updateMiniPlayerPosition(
                            new PlayPositionState( playerState.getCurrentIndex(), playerState.getPlayPosition() ) );
                    if( song != null ) {
                        song.loadAlbumArt( mContext, new SongDao.AlbumArtLoadedListener() {
                            @Override
                            public void artLoaded( final Bitmap bitmap ) {
                                synchronized( mLock ) {
                                    if( !isInitialized() ) {
                                        return;
                                    }
                                    mMiniPlayerAlbumArt.setImageBitmap( bitmap );
                                }
                            }
                        } );
                    }
                }
            } );
        }
    }

    private void updateMiniPlayerPosition( final PlayPositionState playPositionState ) {
        synchronized( mLock ) {
            if( !isInitialized() ) {
                return;
            }
            if( mIsSeeking ) {
                return;
            }
            mMiniPlayerLayout.setVisibility( View.VISIBLE );
            updatePositionIndicator( playPositionState );
            updateSeekBarSmoothly( playPositionState );

        }
    }

    private void updatePositionIndicator( final int position, final int duration ) {
        synchronized( mLock ) {
            if( !isInitialized() ) {
                return;
            }
            mPosition.setText( formatPositionForIndicator( mShowElapsedTime ? position : ( duration - position ) ) );
        }
    }

    private void updatePositionIndicator( final PlayPositionState playPositionState ) {
        synchronized( mLock ) {
            if( !isInitialized() ) {
                return;
            }
            mPosition.setText( formatPositionForIndicator( getIndicatorPosition( playPositionState ) ) );
        }
    }

    private int getIndicatorPosition( final PlayPositionState playPositionState ) {
        int position = playPositionState.getPlayPosition();
        if( !mShowElapsedTime ) {
            final String key = mListViewAdapter.getItem( playPositionState.getCurrentIndex() );
            final SongDao song = key != null ? SongTableAccessor.getInstance( mContext ).getSong( key ) : null;
            if( song != null ) {
                position = song.getDuration() - position;
            } else {
                position = 0;
            }
        }

        return position;
    }

    private String formatPositionForIndicator( final int position ) {
        return String.format( mShowElapsedTime ? "%d:%02d" : "-%d:%02d", TimeUnit.MILLISECONDS.toMinutes( position ),
                              TimeUnit.MILLISECONDS.toSeconds( position ) - TimeUnit.MINUTES.toSeconds(
                                      TimeUnit.MILLISECONDS.toMinutes( position ) ) );
    }

    private void updateSeekBarSmoothly( final PlayPositionState playPositionState ) {
        synchronized( mLock ) {
            if( !isInitialized() ) {
                return;
            }

            int position = playPositionState.getPlayPosition();
            final ObjectAnimator animation = ObjectAnimator.ofInt( mSeekBar, SEEK_BAR_PROGRESS_PROPERTY_NAME,
                                                                   position );
            animation.setDuration( SEEKBAR_SMOOTH_SCROLL_DURATION );
            animation.setInterpolator( new DecelerateInterpolator() );
            animation.start();
        }
    }


    private void updateListAdapterQueue( final QueueState queueState ) {
        synchronized( mLock ) {
            if( !isInitialized() ) {
                return;
            }
            mListViewAdapter.swapItems( queueState.getQueue(), queueState.getCurrentIndex() );
            toggleEmptyPresentation( queueState.getQueue().size() == 0 );
        }

    }

    private void updateRepeatMode( final PlayerState playerState ) {
        switch( playerState.getRepeatMode() ) {
            case NONE:
                mRepeatButton.setImageResource( UIElementsHelper.getIcon( mContext, "repeat" ) );
                break;
            case ALL:
                mRepeatButton.setImageResource( R.drawable.repeat_highlighted );
                break;
            case SONG:
                mRepeatButton.setImageResource( R.drawable.repeat_song );
                break;
        }
    }


    private void updateListAdapterIndex( final PlayerState state ) {
        synchronized( mLock ) {
            if( !isInitialized() ) {
                return;
            }
            if( mListViewAdapter == null ) {
                return;
            }
            mListViewAdapter.setCurrentIndex( state.getCurrentIndex() );
            mListView.smoothScrollToPosition( state.getCurrentIndex() );
        }
    }

    private final SeekBar.OnSeekBarChangeListener mSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {

        @Override
        public void onProgressChanged( final SeekBar seekBar, final int seekBarPosition, final boolean changedByUser ) {
            if( changedByUser ) {
                updatePositionIndicator( seekBarPosition, seekBar.getMax() );
            }
        }

        @Override
        public void onStartTrackingTouch( SeekBar seekBar ) {
            synchronized( mLock ) {
                mIsSeeking = true;
            }
        }

        @Override
        public void onStopTrackingTouch( SeekBar seekBar ) {
            synchronized( mLock ) {
                mPlayback.setPlayPosition( seekBar.getProgress() );
                // TODO: set mIsSeeking to false after setPlayPosition is finished
                mIsSeeking = false;
            }

        }

    };

    private final View.OnClickListener mPositionClickListener = new View.OnClickListener() {

        @Override
        public void onClick( View view ) {
            mShowElapsedTime = !mShowElapsedTime;
            mPreferences.setShowElapsedTime( mShowElapsedTime );
        }

    };

    private final View.OnClickListener mSongClickListener = new View.OnClickListener() {
        @Override
        public void onClick( final View v ) {
            scrollToCurrentIndex();
        }
    };

    private final View.OnClickListener mPlayPauseClickListener = new View.OnClickListener() {

        @Override
        public void onClick( View view ) {
            view.performHapticFeedback( HapticFeedbackConstants.VIRTUAL_KEY );
            mPlayback.playPause();
        }

    };

    private final View.OnClickListener mPreviousClickListener = new View.OnClickListener() {

        @Override
        public void onClick( View arg0 ) {
            mPlayback.previous();
        }

    };

    private final View.OnClickListener mNextClickListener = new View.OnClickListener() {

        @Override
        public void onClick( View arg0 ) {
            mPlayback.next();

        }

    };

    private final View.OnClickListener mRepeatClickListener = new View.OnClickListener() {

        @Override
        public void onClick( View arg0 ) {
            mPlayback.toggleRepeat();
        }

    };

    private final AdapterView.OnItemClickListener mOnItemClickListener = new AdapterView.OnItemClickListener() {

        @Override
        public void onItemClick( AdapterView<?> parent, View view, int position, long id ) {
            mPlayback.play( position );
        }

    };

    private final DragSortListView.DropListener mOnDropListener = new DragSortListView.DropListener() {

        @Override
        public void drop( int from, int to ) {
            // TODO: Fix flickering
            mPlayback.moveSong( from, to );
        }
    };

    private final DragSortListView.RemoveListener mOnRemoveListener = new DragSortListView.RemoveListener() {

        @Override
        public void remove( int which ) {
            final int[] indices = new int[]{ which };
            mPlayback.deleteSongs( indices );
        }
    };

    private void scrollToCurrentIndex() {
        synchronized( mLock ) {
            if( !isInitialized() ) {
                return;
            }
            mListView.smoothScrollToPosition( mListViewAdapter.getCurrentIndex() );
        }
    }

    private void animatePlayToPause() {
        if( !mIsPlaying ) {
            return;
        }

        //Fade out the play button.
        final ScaleAnimation scaleOut = new ScaleAnimation( 1.0f, 0.0f, 1.0f, 0.0f, mPlayPauseButton.getWidth() / 2,
                                                            mPlayPauseButton.getHeight() / 2 );
        scaleOut.setDuration( 150 );
        scaleOut.setInterpolator( new AccelerateInterpolator() );


        //Scale in the pause button.
        final ScaleAnimation scaleIn = new ScaleAnimation( 0.0f, 1.0f, 0.0f, 1.0f, mPlayPauseButton.getWidth() / 2,
                                                           mPlayPauseButton.getHeight() / 2 );
        scaleIn.setDuration( 150 );
        scaleIn.setInterpolator( new DecelerateInterpolator() );

        scaleOut.setAnimationListener( new Animation.AnimationListener() {

            @Override
            public void onAnimationStart( Animation animation ) {

            }

            @Override
            public void onAnimationEnd( Animation animation ) {
                mPlayPauseButton.setImageResource( R.drawable.pause_light );
                mPlayPauseButton.setPadding( 0, 0, 0, 0 );
                mPlayPauseButton.startAnimation( scaleIn );
            }

            @Override
            public void onAnimationRepeat( Animation animation ) {

            }

        } );

        scaleIn.setAnimationListener( new Animation.AnimationListener() {

            @Override
            public void onAnimationStart( Animation animation ) {

            }

            @Override
            public void onAnimationEnd( Animation animation ) {
                mPlayPauseButton.setScaleX( 1.0f );
                mPlayPauseButton.setScaleY( 1.0f );
                mIsPlaying = false;
            }

            @Override
            public void onAnimationRepeat( Animation animation ) {

            }

        } );

        mPlayPauseButton.startAnimation( scaleOut );
    }

    private void animatePauseToPlay() {
        if( mIsPlaying ) {
            return;
        }

        //Scale out the pause button.
        final ScaleAnimation scaleOut = new ScaleAnimation( 1.0f, 0.0f, 1.0f, 0.0f, mPlayPauseButton.getWidth() / 2,
                                                            mPlayPauseButton.getHeight() / 2 );
        scaleOut.setDuration( 150 );
        scaleOut.setInterpolator( new AccelerateInterpolator() );


        //Scale in the play button.
        final ScaleAnimation scaleIn = new ScaleAnimation( 0.0f, 1.0f, 0.0f, 1.0f, mPlayPauseButton.getWidth() / 2,
                                                           mPlayPauseButton.getHeight() / 2 );
        scaleIn.setDuration( 150 );
        scaleIn.setInterpolator( new DecelerateInterpolator() );

        scaleOut.setAnimationListener( new Animation.AnimationListener() {

            @Override
            public void onAnimationStart( Animation animation ) {

            }

            @Override
            public void onAnimationEnd( Animation animation ) {
                mPlayPauseButton.setImageResource( R.drawable.play_light );
                mPlayPauseButton.setPadding( 0, 0, -5, 0 );
                mPlayPauseButton.startAnimation( scaleIn );
            }

            @Override
            public void onAnimationRepeat( Animation animation ) {

            }

        } );

        scaleIn.setAnimationListener( new Animation.AnimationListener() {

            @Override
            public void onAnimationStart( Animation animation ) {

            }

            @Override
            public void onAnimationEnd( Animation animation ) {
                mPlayPauseButton.setScaleX( 1.0f );
                mPlayPauseButton.setScaleY( 1.0f );
                mIsPlaying = true;
            }

            @Override
            public void onAnimationRepeat( Animation animation ) {

            }

        } );

        mPlayPauseButton.startAnimation( scaleOut );
    }

    @Override
    public void onCreateOptionsMenu( final Menu menu, final MenuInflater inflater ) {
        inflater.inflate( R.menu.now_playing_fragment_menu, menu );
        super.onCreateOptionsMenu( menu, inflater );
    }

    @Override
    public boolean onOptionsItemSelected( MenuItem item ) {
        switch( item.getItemId() ) {
            case R.id.action_clear_all:
                handleClearAll();
                break;
        }
        return super.onOptionsItemSelected( item );
    }

    private void handleClearAll() {
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder( getActivity() );
        alertDialogBuilder.setTitle( mContext.getString( R.string.now_playing_clear_title ) )
                          .setMessage( mContext.getString( R.string.now_playing_clear_message ) );
        alertDialogBuilder.setNegativeButton( R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick( final DialogInterface dialog, final int which ) {
                dialog.dismiss();
            }
        } );
        alertDialogBuilder.setPositiveButton( R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick( final DialogInterface dialog, final int which ) {
                mPlayback.clearSongs();
                dialog.dismiss();
            }
        } );
        alertDialogBuilder.create().show();
    }

}
