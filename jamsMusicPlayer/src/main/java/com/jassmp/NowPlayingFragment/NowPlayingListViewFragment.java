package com.jassmp.NowPlayingFragment;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
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
import android.widget.TextView;

import com.jassmp.Dao.SongDao;
import com.jassmp.GuiHelper.UIElementsHelper;
import com.jassmp.JassMpDb.SongTableAccessor;
import com.jassmp.Playback.PlayPositionState;
import com.jassmp.Playback.Playback;
import com.jassmp.Playback.PlaybackStateListener;
import com.jassmp.Playback.PlayerState;
import com.jassmp.Playback.QueueState;
import com.jassmp.Playback.RepeatMode;
import com.jassmp.Preferences.Preferences;
import com.jassmp.R;
import com.jassmp.Utils.Common;
import com.mobeta.android.dslv.DragSortListView;
import com.mobeta.android.dslv.SimpleFloatViewManager;

import java.util.concurrent.TimeUnit;

public class NowPlayingListViewFragment extends Fragment implements PlaybackStateListener {

    //
    // private members

    private       Context     mContext     = null;
    private       Playback    mPlayback    = null;
    private       Preferences mPreferences = null;
    private final Object      mLock        = new Object();

    private View        mRootView     = null;
    private RelativeLayout mMiniPlayerLayout    = null;
    private ImageView      mMiniPlayerAlbumArt  = null;
    private RelativeLayout mPlayPauseBackground = null;
    private ImageButton    mPlayPauseButton     = null;
    private ImageButton    mNextButton          = null;
    private ImageButton    mPreviousButton      = null;
    private ImageButton mRepeatButton = null;
    private TextView    mPosition     = null;
    private TextView       mTitleText           = null;
    private TextView       mSubText             = null;

    private DragSortListView              mListView        = null;
    private NowPlayingListViewItemAdapter mListViewAdapter = null;
    private TextView                      mEmptyInfoText   = null;

    private boolean    mIsPlaying       = false;
    private boolean    mShowElapsedTime = true;
    private RepeatMode mRepeatMode      = RepeatMode.NONE;


    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState ) {
        mContext = getActivity().getApplicationContext();
        mPreferences = new Preferences( mContext );
        mPlayback = new Playback( mContext, this );
        mShowElapsedTime = mPreferences.getShowElapsedTime();

        mRootView = inflater.inflate( R.layout.fragment_now_playing, container, false );
        mRootView.setBackgroundColor( UIElementsHelper.getGridViewBackground( mContext ) );

        initMiniPlayer( mRootView );
        initListViewer( mRootView );
        reloadPlayState();

        return mRootView;
    }

    private void initMiniPlayer( final View rootView ) {
        mMiniPlayerLayout = (RelativeLayout) rootView.findViewById( R.id.player_layout );
        mMiniPlayerAlbumArt = (ImageView) rootView.findViewById( R.id.player_albumArt );
        mPosition = (TextView) rootView.findViewById( R.id.player_position );
        mPlayPauseBackground = (RelativeLayout) rootView.findViewById( R.id.player_playPause_layout );
        mPlayPauseButton = (ImageButton) rootView.findViewById( R.id.player_playPause );
        mNextButton = (ImageButton) rootView.findViewById( R.id.player_nextSong );
        mPreviousButton = (ImageButton) rootView.findViewById( R.id.player_previousSong );
        mRepeatButton = (ImageButton) rootView.findViewById( R.id.player_repeat );
        mTitleText = (TextView) rootView.findViewById( R.id.player_song );
        mSubText = (TextView) rootView.findViewById( R.id.player_albumAndArtist );

        mPlayPauseBackground.setBackgroundResource( UIElementsHelper.getShadowedCircle( mContext ) );
        mTitleText.setTypeface( UIElementsHelper.getRegularTypeface( mContext ) );
        mSubText.setTypeface( UIElementsHelper.getRegularTypeface( mContext ) );

        mPosition.setOnClickListener( mPositionClickListener );
        mPlayPauseBackground.setOnClickListener( mPlayPauseClickListener );
        mPlayPauseButton.setOnClickListener( mPlayPauseClickListener );
        mNextButton.setOnClickListener( mOnClickNextListener );
        mPreviousButton.setOnClickListener( mOnClickPreviousListener );
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

        mListView.setOnItemClickListener( mOnClick );
        mListView.setDropListener( mOnDrop );
        mListView.setRemoveListener( mOnRemove );

        SimpleFloatViewManager simpleFloatViewManager = new SimpleFloatViewManager( mListView );
        simpleFloatViewManager.setBackgroundColor( Color.TRANSPARENT );
        mListView.setFloatViewManager( simpleFloatViewManager );

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
        return mContext != null;
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
                    mPreviousButton.setEnabled( !playerState.isFirstSong() );
                    mNextButton.setEnabled( !playerState.isLastSong() );
                    updateRepeatMode( playerState );
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
            mMiniPlayerLayout.setVisibility( View.VISIBLE );
            final int millis = getMillis( playPositionState );
            final String position = String.format( mShowElapsedTime ? "%d:%02d" : "-%d:%02d",
                                                   TimeUnit.MILLISECONDS.toMinutes( millis ),
                                                   TimeUnit.MILLISECONDS.toSeconds( millis )
                                                   - TimeUnit.MINUTES.toSeconds(
                                                           TimeUnit.MILLISECONDS.toMinutes( millis ) ) );
            mPosition.setText( position );
        }
    }

    private int getMillis( final PlayPositionState playPositionState ) {
        int millis = playPositionState.getPlayPosition();
        if( !mShowElapsedTime ) {
            final String key = mListViewAdapter.getItem( playPositionState.getCurrentIndex() );
            final SongDao song = key != null ? SongTableAccessor.getInstance( mContext ).getSong( key ) : null;
            if( song != null ) {
                millis = song.getDuration() - millis;
            } else {
                millis = 0;
            }
        }

        return millis;
    }

    private void updateListAdapterQueue( final QueueState queueState ) {
        synchronized( mLock ) {
            if( !isInitialized() ) {
                return;
            }
            if( mListViewAdapter == null ) {
                mListViewAdapter = new NowPlayingListViewItemAdapter( mContext, queueState.getQueue(),
                                                                      queueState.getCurrentIndex() );
                mListView.setAdapter( mListViewAdapter );
            } else {
                mListViewAdapter.swapItems( queueState.getQueue() );
                mListViewAdapter.setCurrentIndex( queueState.getCurrentIndex() );
            }
            toggleEmptyPresentation( queueState.getQueue().size() == 0 );
        }

    }

    private void updateRepeatMode( final PlayerState playerState ) {
        mRepeatMode = playerState.getRepeatMode();
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
        }
    }

    private View.OnClickListener mPositionClickListener = new View.OnClickListener() {

        @Override
        public void onClick( View view ) {
            mShowElapsedTime = !mShowElapsedTime;
            mPreferences.setShowElapsedTime( mShowElapsedTime );
        }

    };

    private View.OnClickListener mPlayPauseClickListener = new View.OnClickListener() {

        @Override
        public void onClick( View view ) {
            view.performHapticFeedback( HapticFeedbackConstants.VIRTUAL_KEY );
            mPlayback.playPause();
        }

    };

    private View.OnClickListener mOnClickPreviousListener = new View.OnClickListener() {

        @Override
        public void onClick( View arg0 ) {
            mPlayback.previous();
        }

    };

    private View.OnClickListener mOnClickNextListener = new View.OnClickListener() {

        @Override
        public void onClick( View arg0 ) {
            mPlayback.next();

        }

    };

    private View.OnClickListener mOnCLickRepeatListener = new View.OnClickListener() {

        @Override
        public void onClick( View arg0 ) {
            mPlayback.next();

        }

    };

    private AdapterView.OnItemClickListener mOnClick = new AdapterView.OnItemClickListener() {

        @Override
        public void onItemClick( AdapterView<?> parent, View view, int position, long id ) {
            mPlayback.play( position );
        }

    };

    private DragSortListView.DropListener mOnDrop = new DragSortListView.DropListener() {

        @Override
        public void drop( int from, int to ) {
            mPlayback.moveSong( from, to );
        }
    };

    private DragSortListView.RemoveListener mOnRemove = new DragSortListView.RemoveListener() {

        @Override
        public void remove( int which ) {
            final int[] indices = new int[]{ which };
            mPlayback.deleteSongs( indices );
        }
    };

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


}
