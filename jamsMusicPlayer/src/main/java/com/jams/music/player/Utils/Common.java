/*
 * Copyright (C) 2014 Saravan Pantham
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jams.music.player.Utils;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.multidex.MultiDexApplication;
import android.support.v4.content.LocalBroadcastManager;
import android.util.DisplayMetrics;

import com.jams.music.player.DBHelpers.DBAccessHelper;
import com.jams.music.player.Helpers.UIElementsHelper;
import com.jams.music.player.NowPlayingActivity.NowPlayingActivity;
import com.jams.music.player.PlaybackKickstarter.PlaybackKickstarter;
import com.jams.music.player.R;
import com.jams.music.player.Services.AudioPlaybackService;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Singleton class that provides access to common objects
 * and methods used in the application.
 *
 * @author Saravan Pantham
 */
public class Common extends MultiDexApplication {

    //Context.
    private Context mContext;

    //Service reference and flags.
    private AudioPlaybackService mService;
    private boolean mIsServiceRunning = false;

    //Playback kickstarter object.
    private PlaybackKickstarter mPlaybackKickstarter;

    //NowPlayingActivity reference.
    private NowPlayingActivity mNowPlayingActivity;

    //SharedPreferences.
    private static SharedPreferences mSharedPreferences;

    //Picasso instance.
    private Picasso mPicasso;

    //Indicates if the library is currently being built.
    private boolean mIsBuildingLibrary = false;
    private boolean mIsScanFinished    = false;

    //ImageLoader/ImageLoaderConfiguration objects for ListViews and GridViews.
    private ImageLoader              mImageLoader;
    private ImageLoaderConfiguration mImageLoaderConfiguration;

    //Image display options.
    private DisplayImageOptions mDisplayImageOptions;

    //Broadcast elements.
    private LocalBroadcastManager mLocalBroadcastManager;
    public static final String UPDATE_UI_BROADCAST = "com.jams.music.player.NEW_SONG_UPDATE_UI";

    //Update UI broadcast flags.
    public static final String SHOW_AUDIOBOOK_TOAST      = "AudiobookToast";
    public static final String UPDATE_SEEKBAR_DURATION   = "UpdateSeekbarDuration";
    public static final String UPDATE_PAGER_POSTIION     = "UpdatePagerPosition";
    public static final String UPDATE_PLAYBACK_CONTROLS  = "UpdatePlabackControls";
    public static final String SERVICE_STOPPING          = "ServiceStopping";
    public static final String SHOW_STREAMING_BAR        = "ShowStreamingBar";
    public static final String HIDE_STREAMING_BAR        = "HideStreamingBar";
    public static final String UPDATE_BUFFERING_PROGRESS = "UpdateBufferingProgress";
    public static final String INIT_PAGER                = "InitPager";
    public static final String NEW_QUEUE_ORDER           = "NewQueueOrder";
    public static final String UPDATE_EQ_FRAGMENT        = "UpdateEQFragment";

    //Contants for identifying each fragment/activity.
    public static final String FRAGMENT_ID      = "FragmentId";
    public static final int    ARTISTS_FRAGMENT = 0;
    public static final int    ALBUMS_FRAGMENT  = 2;
    public static final int    SONGS_FRAGMENT   = 3;
    public static final int    GENRES_FRAGMENT  = 5;

    //Device orientation constants.
    public static final int ORIENTATION_PORTRAIT  = 0;
    public static final int ORIENTATION_LANDSCAPE = 1;

    //Device screen size/orientation identifiers.
    public static final String REGULAR                  = "regular";
    public static final String SMALL_TABLET             = "small_tablet";
    public static final String LARGE_TABLET             = "large_tablet";
    public static final String XLARGE_TABLET            = "xlarge_tablet";
    public static final int    REGULAR_SCREEN_PORTRAIT  = 0;
    public static final int    REGULAR_SCREEN_LANDSCAPE = 1;
    public static final int    SMALL_TABLET_PORTRAIT    = 2;
    public static final int    SMALL_TABLET_LANDSCAPE   = 3;
    public static final int    LARGE_TABLET_PORTRAIT    = 4;
    public static final int    LARGE_TABLET_LANDSCAPE   = 5;
    public static final int    XLARGE_TABLET_PORTRAIT   = 6;
    public static final int    XLARGE_TABLET_LANDSCAPE  = 7;

    //Miscellaneous flags/identifiers.
    public static final String SONG_ID       = "SongId";
    public static final String SONG_TITLE    = "SongTitle";
    public static final String SONG_ALBUM    = "SongAlbum";
    public static final String SONG_ARTIST   = "SongArtist";
    public static final String ALBUM_ART     = "AlbumArt";
    public static final String CURRENT_THEME = "CurrentTheme";
    public static final int    DARK_THEME    = 0;
    public static final int    LIGHT_THEME   = 1;

    //SharedPreferences keys.
    public static final String CROSSFADE_ENABLED        = "CrossfadeEnabled";
    public static final String CROSSFADE_DURATION       = "CrossfadeDuration";
    public static final String REPEAT_MODE              = "RepeatMode";
    public static final String MUSIC_PLAYING            = "MusicPlaying";
    public static final String SERVICE_RUNNING          = "ServiceRunning";
    public static final String CURRENT_LIBRARY          = "CurrentLibrary";
    public static final String CURRENT_LIBRARY_POSITION = "CurrentLibraryPosition";
    public static final String SHUFFLE_ON               = "ShuffleOn";
    public static final String FIRST_RUN                = "FirstRun";
    public static final String STARTUP_BROWSER          = "StartupBrowser";
    public static final String SHOW_LOCKSCREEN_CONTROLS = "ShowLockscreenControls";
    public static final String ARTISTS_LAYOUT           = "ArtistsLayout";
    public static final String ALBUM_ARTISTS_LAYOUT     = "AlbumArtistsLayout";
    public static final String ALBUMS_LAYOUT            = "AlbumsLayout";
    public static final String PLAYLISTS_LAYOUT         = "PlaylistsLayout";
    public static final String GENRES_LAYOUT            = "GenresLayout";
    public static final String FOLDERS_LAYOUT           = "FoldersLayout";

    //Repeat mode constants.
    public static final int REPEAT_OFF      = 0;
    public static final int REPEAT_PLAYLIST = 1;
    public static final int REPEAT_SONG     = 2;
    public static final int A_B_REPEAT      = 3;

    // Sorting
    public static final String SORT_COLUMN           = "SortColumn";
    public static final String SORT_DIRECTION_COLUMN = "SortDirectionColumn";

    @Override
    public void onCreate() {
        super.onCreate();

        //Application context.
        mContext = getApplicationContext();

        //SharedPreferences.
        mSharedPreferences = this.getSharedPreferences( "com.jams.music.player", Context.MODE_PRIVATE );

        //Playback kickstarter.
        mPlaybackKickstarter = new PlaybackKickstarter( this.getApplicationContext() );

        //Picasso.
        mPicasso = new Picasso.Builder( mContext ).build();

        //ImageLoader.
        mImageLoader = ImageLoader.getInstance();
        mImageLoaderConfiguration = new ImageLoaderConfiguration.Builder( getApplicationContext() ).memoryCache(
                new WeakMemoryCache() )
                                                                                                   .memoryCacheSizePercentage(
                                                                                                           13 )
                                                                                                   .imageDownloader(
                                                                                                           new ByteArrayUniversalImageLoader(
                                                                                                                   mContext ) )
                                                                                                   .build();
        mImageLoader.init( mImageLoaderConfiguration );

        //Init DisplayImageOptions.
        initDisplayImageOptions();
    }

    /**
     * Initializes a DisplayImageOptions object. The drawable shown
     * while an image is loading is based on the current theme.
     */
    public void initDisplayImageOptions() {

        //Create a set of options to optimize the bitmap memory usage.
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        options.inJustDecodeBounds = false;
        options.inPurgeable = true;

        int emptyColorPatch = UIElementsHelper.getEmptyColorPatch( this );
        mDisplayImageOptions = null;
        mDisplayImageOptions = new DisplayImageOptions.Builder().showImageForEmptyUri( emptyColorPatch )
                                                                .showImageOnFail( emptyColorPatch )
                                                                .showImageOnLoading( emptyColorPatch )
                                                                .cacheInMemory( true )
                                                                .cacheOnDisc( true )
                                                                .decodingOptions( options )
                                                                .imageScaleType( ImageScaleType.IN_SAMPLE_POWER_OF_2 )
                                                                .bitmapConfig( Bitmap.Config.ARGB_4444 )
                                                                .delayBeforeLoading( 400 )
                                                                .displayer( new FadeInBitmapDisplayer( 200 ) )
                                                                .build();

    }

    /**
     * Sends out a local broadcast that notifies all receivers to update
     * their respective UI elements.
     */
    public void broadcastUpdateUICommand( String[] updateFlags, String[] flagValues ) {
        Intent intent = new Intent( UPDATE_UI_BROADCAST );
        for( int i = 0; i < updateFlags.length; i++ ) {
            intent.putExtra( updateFlags[ i ], flagValues[ i ] );
        }

        mLocalBroadcastManager = LocalBroadcastManager.getInstance( mContext );
        mLocalBroadcastManager.sendBroadcast( intent );

    }

    /**
     * Resamples a resource image to avoid OOM errors.
     *
     * @param resID     Resource ID of the image to be downsampled.
     * @param reqWidth  Width of output image.
     * @param reqHeight Height of output image.
     * @return A bitmap of the resampled image.
     */
    public Bitmap decodeSampledBitmapFromResource( int resID, int reqWidth, int reqHeight ) {

        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        options.inSampleSize = calculateInSampleSize( options, reqWidth, reqHeight );
        options.inJustDecodeBounds = false;
        options.inPurgeable = true;

        return BitmapFactory.decodeResource( mContext.getResources(), resID, options );
    }

    /**
     * Resamples the specified input image file to avoid OOM errors.
     *
     * @param inputFile Input file to be downsampled
     * @param reqWidth  Width of the output file.
     * @param reqHeight Height of the output file.
     * @return The downsampled bitmap.
     */
    public Bitmap decodeSampledBitmapFromFile( File inputFile, int reqWidth, int reqHeight ) {

        InputStream is = null;
        try {

            try {
                is = new FileInputStream( inputFile );
            } catch( Exception e ) {
                //Return a null bitmap if there's an error reading the file.
                return null;
            }

            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream( is, null, options );

            options.inSampleSize = calculateInSampleSize( options, reqWidth, reqHeight );
            options.inJustDecodeBounds = false;
            options.inPurgeable = true;

            try {
                is = new FileInputStream( inputFile );
            } catch( FileNotFoundException e ) {
                //Return a null bitmap if there's an error reading the file.
                return null;
            }

            return BitmapFactory.decodeStream( is, null, options );
        } finally {
            try {
                if( is != null ) {
                    is.close();
                }
            } catch( IOException e ) {
                e.printStackTrace();
            }

        }

    }

    /**
     * Calculates the sample size for the resampling process.
     *
     * @param options
     * @param reqWidth
     * @param reqHeight
     * @return The sample size.
     */
    private static int calculateInSampleSize( BitmapFactory.Options options, int reqWidth, int reqHeight ) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if( height > reqHeight || width > reqWidth ) {
            if( width > height ) {
                inSampleSize = Math.round( (float) height / (float) reqHeight );
            } else {
                inSampleSize = Math.round( (float) width / (float) reqWidth );
            }
        }

        return inSampleSize;
    }

    /*
     * Returns the status bar height for the current layout configuration.
     */
    public static int getStatusBarHeight( Context context ) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier( "status_bar_height", "dimen", "android" );
        if( resourceId > 0 ) {
            result = context.getResources().getDimensionPixelSize( resourceId );
        }

        return result;
    }

    /*
     * Returns the navigation bar height for the current layout configuration.
     */
    public static int getNavigationBarHeight( Context context ) {
        Resources resources = context.getResources();
        int resourceId = resources.getIdentifier( "navigation_bar_height", "dimen", "android" );
        if( resourceId > 0 ) {
            return resources.getDimensionPixelSize( resourceId );
        }

        return 0;
    }

    /**
     * Converts dp unit to equivalent pixels, depending on device density.
     *
     * @param dp      A value in dp (density independent pixels) unit. Which we need to convert
     *                into pixels
     * @param context Context to get resources and device specific display metrics
     * @return A float value to represent px equivalent to dp depending on device density
     */
    public float convertDpToPixels( float dp, Context context ) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = dp * ( metrics.densityDpi / 160f );
        return px;
    }

    /**
     * Returns the orientation of the device.
     */
    public int getOrientation() {
        if( getResources().getDisplayMetrics().widthPixels > getResources().getDisplayMetrics().heightPixels ) {
            return ORIENTATION_LANDSCAPE;
        } else {
            return ORIENTATION_PORTRAIT;
        }

    }

    /**
     * Returns the current screen configuration of the device.
     */
    public int getDeviceScreenConfiguration() {
        String screenSize = getResources().getString( R.string.screen_size );
        boolean landscape = getResources().getBoolean( R.bool.landscape );

        if( screenSize.equals( REGULAR ) && !landscape ) {
            return REGULAR_SCREEN_PORTRAIT;
        } else if( screenSize.equals( REGULAR ) && landscape ) {
            return REGULAR_SCREEN_LANDSCAPE;
        } else if( screenSize.equals( SMALL_TABLET ) && !landscape ) {
            return SMALL_TABLET_PORTRAIT;
        } else if( screenSize.equals( SMALL_TABLET ) && landscape ) {
            return SMALL_TABLET_LANDSCAPE;
        } else if( screenSize.equals( LARGE_TABLET ) && !landscape ) {
            return LARGE_TABLET_PORTRAIT;
        } else if( screenSize.equals( LARGE_TABLET ) && landscape ) {
            return LARGE_TABLET_LANDSCAPE;
        } else if( screenSize.equals( XLARGE_TABLET ) && !landscape ) {
            return XLARGE_TABLET_PORTRAIT;
        } else if( screenSize.equals( XLARGE_TABLET ) && landscape ) {
            return XLARGE_TABLET_LANDSCAPE;
        } else {
            return REGULAR_SCREEN_PORTRAIT;
        }
    }

    public boolean isLandscape() {
        int screenConfig = getDeviceScreenConfiguration();
        if( screenConfig == SMALL_TABLET_LANDSCAPE ||
            screenConfig == LARGE_TABLET_LANDSCAPE ||
            screenConfig == XLARGE_TABLET_LANDSCAPE ) {
            return true;
        } else {
            return false;
        }
    }


    /**
     * Converts milliseconds to hh:mm:ss format.
     */
    public String convertMillisToMinsSecs( long milliseconds ) {

        int secondsValue = (int) ( milliseconds / 1000 ) % 60;
        int minutesValue = (int) ( ( milliseconds / ( 1000 * 60 ) ) % 60 );
        int hoursValue = (int) ( ( milliseconds / ( 1000 * 60 * 60 ) ) % 24 );

        String seconds = "";
        String minutes = "";
        String hours = "";

        if( secondsValue < 10 ) {
            seconds = "0" + secondsValue;
        } else {
            seconds = "" + secondsValue;
        }

        if( minutesValue < 10 ) {
            minutes = "0" + minutesValue;
        } else {
            minutes = "" + minutesValue;
        }

        if( hoursValue < 10 ) {
            hours = "0" + hoursValue;
        } else {
            hours = "" + hoursValue;
        }

        String output = "";
        if( hoursValue != 0 ) {
            output = hours + ":" + minutes + ":" + seconds;
        } else {
            output = minutes + ":" + seconds;
        }

        return output;
    }
    
    /*
     * Getter methods.
     */

    public DBAccessHelper getDBAccessHelper() {
        return DBAccessHelper.getInstance( mContext );
    }

    public SharedPreferences getSharedPreferences() {
        return mSharedPreferences;
    }

    public Picasso getPicasso() {
        return mPicasso;
    }

    public boolean isBuildingLibrary() {
        return mIsBuildingLibrary;
    }

    public boolean isScanFinished() {
        return mIsScanFinished;
    }

    public AudioPlaybackService getAudioPlaybackService() {
        return mService;
    }

    public NowPlayingActivity getNowPlayingActivity() {
        return mNowPlayingActivity;
    }

    public ImageLoader getImageLoader() {
        return mImageLoader;
    }

    public int getCurrentTheme() {
        return getSharedPreferences().getInt( CURRENT_THEME, DARK_THEME );
    }

    public boolean isServiceRunning() {
        return mIsServiceRunning;
    }

    public boolean isEqualizerEnabled() {
        return getSharedPreferences().getBoolean( "EQUALIZER_ENABLED", true );
    }

    public boolean isCrossfadeEnabled() {
        return getSharedPreferences().getBoolean( CROSSFADE_ENABLED, false );
    }

    public int getCrossfadeDuration() {
        return getSharedPreferences().getInt( CROSSFADE_DURATION, 5 );
    }

    public PlaybackKickstarter getPlaybackKickstarter() {
        return mPlaybackKickstarter;
    }

	/*
     * Setter methods.
	 */

    public void setIsBuildingLibrary( boolean isBuildingLibrary ) {
        mIsBuildingLibrary = isBuildingLibrary;
    }

    public void setIsScanFinished( boolean isScanFinished ) {
        mIsScanFinished = isScanFinished;
    }

    public void setService( AudioPlaybackService service ) {
        mService = service;
    }

    public void setNowPlayingActivity( NowPlayingActivity activity ) {
        mNowPlayingActivity = activity;
    }

    public void setIsServiceRunning( boolean running ) {
        mIsServiceRunning = running;
    }

}
