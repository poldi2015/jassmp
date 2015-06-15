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
package com.jassmp.Utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.support.multidex.MultiDexApplication;
import android.support.v4.content.LocalBroadcastManager;

import com.jassmp.GuiHelper.UIElementsHelper;
import com.jassmp.R;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.squareup.picasso.Picasso;

/**
 * Singleton class that provides access to common objects
 * and methods used in the application.
 *
 * @author Saravan Pantham
 */
public class Common extends MultiDexApplication {

    //Picasso instance.
    private Picasso mPicasso;

    //Indicates if the library is currently being built.
    private boolean mIsBuildingLibrary = false;
    private boolean mIsScanFinished    = false;

    //ImageLoader/ImageLoaderConfiguration objects for ListViews and GridViews.
    private ImageLoader              mImageLoader;
    private ImageLoaderConfiguration mImageLoaderConfiguration;

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
    public static final String FRAGMENT_ID = "FragmentId";

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


    @Override
    public void onCreate() {
        super.onCreate();

        //Application context.
        final Context context = getApplicationContext();

        //Picasso.
        mPicasso = new Picasso.Builder( context ).build();

        //ImageLoader.
        mImageLoader = ImageLoader.getInstance();
        mImageLoaderConfiguration = new ImageLoaderConfiguration.Builder( getApplicationContext() ).memoryCache(
                new WeakMemoryCache() )
                                                                                                   .memoryCacheSizePercentage(
                                                                                                           13 )
                                                                                                   .imageDownloader(
                                                                                                           new ByteArrayUniversalImageLoader(

                                                                                                                   context ) )
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

    public Picasso getPicasso() {
        return mPicasso;
    }

    public boolean isBuildingLibrary() {
        return mIsBuildingLibrary;
    }

    public boolean isScanFinished() {
        return mIsScanFinished;
    }

    public ImageLoader getImageLoader() {
        return mImageLoader;
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

}
