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
package com.jams.music.player.LauncherActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.jams.music.player.Helpers.TypefaceHelper;
import com.jams.music.player.MainActivity.MainActivity;
import com.jams.music.player.R;
import com.jams.music.player.Services.BuildMusicLibraryService;
import com.jams.music.player.Utils.Common;
import com.jams.music.player.WelcomeActivity.WelcomeActivity;

import java.lang.reflect.Method;

public class LauncherActivity extends FragmentActivity {

    public  Context  mContext;
    private Common   mApp;
    public  Activity mActivity;

    public static TextView       buildingLibraryMainText;
    public static TextView       buildingLibraryInfoText;
    private       RelativeLayout buildingLibraryLayout;
    private       Handler        mHandler;

    @SuppressLint( "NewApi" )
    @Override
    public void onCreate( Bundle savedInstanceState ) {

        setTheme( R.style.AppThemeNoActionBar );
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_launcher );

        mContext = this;
        mActivity = this;
        mApp = (Common) mContext.getApplicationContext();
        mHandler = new Handler();

        //Increment the start count. This value will be used to determine when the library should be rescanned.
        int startCount = mApp.getSharedPreferences().getInt( "START_COUNT", 1 );
        mApp.getSharedPreferences().edit().putInt( "START_COUNT", startCount + 1 ).commit();

        //Save the dimensions of the layout for later use on KitKat devices.
        final RelativeLayout launcherRootView = (RelativeLayout) findViewById( R.id.launcher_root_view );
        launcherRootView.getViewTreeObserver().addOnGlobalLayoutListener( new OnGlobalLayoutListener() {

            @Override
            public void onGlobalLayout() {

                try {

                    int screenDimens[];
                    int screenHeight;
                    if( Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1 ) {
                        //API levels 14, 15 and 16.
                        screenDimens = getTrueDeviceResolution();
                        screenHeight = screenDimens[ 1 ];
                    } else {
                        //API levels 17+.
                        Display display = getWindowManager().getDefaultDisplay();
                        DisplayMetrics metrics = new DisplayMetrics();
                        display.getRealMetrics( metrics );
                        screenHeight = metrics.heightPixels;
                    }

                    int layoutHeight = launcherRootView.getHeight();
                    int layoutWidth = launcherRootView.getWidth();

                    int extraHeight = screenHeight - layoutHeight;

                    mApp.getSharedPreferences().edit().putInt( "KITKAT_HEIGHT", layoutHeight ).commit();
                    mApp.getSharedPreferences().edit().putInt( "KITKAT_WIDTH", layoutWidth ).commit();
                    mApp.getSharedPreferences()
                        .edit()
                        .putInt( "KITKAT_HEIGHT_LAND", layoutWidth - extraHeight )
                        .commit();
                    mApp.getSharedPreferences().edit().putInt( "KITKAT_WIDTH_LAND", screenHeight ).commit();

                } catch( Exception e ) {
                    e.printStackTrace();
                }

            }

        } );

        //Build the music library based on the user's scan frequency preferences.
        int scanFrequency = mApp.getSharedPreferences().getInt( "SCAN_FREQUENCY", 5 );
        int updatedStartCount = mApp.getSharedPreferences().getInt( "START_COUNT", 1 );

        //Launch the appropriate activity based on the "FIRST RUN" flag.
        if( mApp.getSharedPreferences().getBoolean( Common.FIRST_RUN, true ) == true ) {

            //Send out a test broadcast to initialize the homescreen/lockscreen widgets.
            sendBroadcast( new Intent( Intent.ACTION_MAIN ).addCategory( Intent.CATEGORY_HOME ) );

            Intent intent = new Intent( this, WelcomeActivity.class );
            startActivity( intent );
            overridePendingTransition( R.anim.fade_in, R.anim.fade_out );

        } else if( mApp.isBuildingLibrary() ) {
            buildingLibraryMainText = (TextView) findViewById( R.id.building_music_library_text );
            buildingLibraryInfoText = (TextView) findViewById( R.id.building_music_library_info );
            buildingLibraryLayout = (RelativeLayout) findViewById( R.id.building_music_library_layout );

            buildingLibraryInfoText.setTypeface( TypefaceHelper.getTypeface( mContext, "RobotoCondensed-Light" ) );
            buildingLibraryInfoText.setPaintFlags( buildingLibraryInfoText.getPaintFlags() |
                                                   Paint.ANTI_ALIAS_FLAG |
                                                   Paint.SUBPIXEL_TEXT_FLAG );

            buildingLibraryMainText.setTypeface( TypefaceHelper.getTypeface( mContext, "RobotoCondensed-Light" ) );
            buildingLibraryMainText.setPaintFlags( buildingLibraryMainText.getPaintFlags() |
                                                   Paint.ANTI_ALIAS_FLAG |
                                                   Paint.SUBPIXEL_TEXT_FLAG );

            buildingLibraryMainText.setText( R.string.jams_is_building_library );
            buildingLibraryLayout.setVisibility( View.VISIBLE );

            //Initialize the runnable that will fire once the scan process is complete.
            mHandler.post( scanFinishedCheckerRunnable );

        } else if( mApp.getSharedPreferences().getBoolean( "RESCAN_ALBUM_ART", false ) == true ) {

            buildingLibraryMainText = (TextView) findViewById( R.id.building_music_library_text );
            buildingLibraryInfoText = (TextView) findViewById( R.id.building_music_library_info );
            buildingLibraryLayout = (RelativeLayout) findViewById( R.id.building_music_library_layout );

            buildingLibraryInfoText.setTypeface( TypefaceHelper.getTypeface( mContext, "RobotoCondensed-Light" ) );
            buildingLibraryInfoText.setPaintFlags( buildingLibraryInfoText.getPaintFlags() |
                                                   Paint.ANTI_ALIAS_FLAG |
                                                   Paint.SUBPIXEL_TEXT_FLAG );

            buildingLibraryMainText.setTypeface( TypefaceHelper.getTypeface( mContext, "RobotoCondensed-Light" ) );
            buildingLibraryMainText.setPaintFlags( buildingLibraryMainText.getPaintFlags() |
                                                   Paint.ANTI_ALIAS_FLAG |
                                                   Paint.SUBPIXEL_TEXT_FLAG );

            buildingLibraryMainText.setText( R.string.jams_is_caching_artwork );
            initScanProcess( 0 );

        } else if( ( mApp.getSharedPreferences().getBoolean( "REBUILD_LIBRARY", false ) == true ) ||
                   ( scanFrequency == 0 && mApp.isScanFinished() == false ) ||
                   ( scanFrequency == 1 && mApp.isScanFinished() == false && updatedStartCount % 3 == 0 ) ||
                   ( scanFrequency == 2 && mApp.isScanFinished() == false && updatedStartCount % 5 == 0 ) ||
                   ( scanFrequency == 3 && mApp.isScanFinished() == false && updatedStartCount % 10 == 0 ) ||
                   ( scanFrequency == 4 && mApp.isScanFinished() == false && updatedStartCount % 20 == 0 ) ) {

            buildingLibraryMainText = (TextView) findViewById( R.id.building_music_library_text );
            buildingLibraryInfoText = (TextView) findViewById( R.id.building_music_library_info );
            buildingLibraryLayout = (RelativeLayout) findViewById( R.id.building_music_library_layout );

            buildingLibraryInfoText.setTypeface( TypefaceHelper.getTypeface( mContext, "RobotoCondensed-Light" ) );
            buildingLibraryInfoText.setPaintFlags( buildingLibraryInfoText.getPaintFlags() |
                                                   Paint.ANTI_ALIAS_FLAG |
                                                   Paint.SUBPIXEL_TEXT_FLAG );

            buildingLibraryMainText.setTypeface( TypefaceHelper.getTypeface( mContext, "RobotoCondensed-Light" ) );
            buildingLibraryMainText.setPaintFlags( buildingLibraryMainText.getPaintFlags() |
                                                   Paint.ANTI_ALIAS_FLAG |
                                                   Paint.SUBPIXEL_TEXT_FLAG );

            initScanProcess( 1 );

        } else {

            launchMainActivity();
        }

    }

    private int[] getTrueDeviceResolution() {

        int[] resolution = new int[ 2 ];
        try {
            Display display = getWindowManager().getDefaultDisplay();

            Method mGetRawH = Display.class.getMethod( "getRawHeight" );
            Method mGetRawW = Display.class.getMethod( "getRawWidth" );

            int rawWidth = (Integer) mGetRawW.invoke( display );
            int rawHeight = (Integer) mGetRawH.invoke( display );

            resolution[ 0 ] = rawWidth;
            resolution[ 1 ] = rawHeight;

        } catch( Exception e ) {
            e.printStackTrace();
        }

        return resolution;
    }

    private void initScanProcess( int scanCode ) {

        //Start the service that will start scanning the user's library/caching album art.
        mApp.setIsBuildingLibrary( true );
        buildingLibraryLayout.setVisibility( View.VISIBLE );
        if( scanCode == 0 ) {
            Intent intent = new Intent( this, BuildMusicLibraryService.class );
            intent.putExtra( "SCAN_TYPE", "RESCAN_ALBUM_ART" );
            startService( intent );

            mApp.getSharedPreferences().edit().putBoolean( "RESCAN_ALBUM_ART", false ).commit();

        } else if( scanCode == 1 ) {
            Intent intent = new Intent( this, BuildMusicLibraryService.class );
            intent.putExtra( "SCAN_TYPE", "FULL_SCAN" );
            startService( intent );

            mApp.getSharedPreferences().edit().putBoolean( "REBUILD_LIBRARY", false ).commit();
        }

        //Initialize the runnable that will fire once the scan process is complete.
        mHandler.post( scanFinishedCheckerRunnable );

    }

    private Runnable scanFinishedCheckerRunnable = new Runnable() {

        @Override
        public void run() {

            if( mApp.isBuildingLibrary() == false ) {
                launchMainActivity();
            } else {
                mHandler.postDelayed( this, 100 );
            }

        }

    };


    private void launchMainActivity() {
        Intent intent = new Intent( mContext, MainActivity.class );
        int startupScreen = 3;//mApp.getSharedPreferences().getInt("STARTUP_SCREEN", 3);

        switch( startupScreen ) {
            case 0:
                intent.putExtra( "TARGET_FRAGMENT", "ARTISTS" );
                break;
            case 2:
                intent.putExtra( "TARGET_FRAGMENT", "ALBUMS" );
                break;
            case 3:
                intent.putExtra( "TARGET_FRAGMENT", "SONGS" );
                break;
            case 5:
                intent.putExtra( "TARGET_FRAGMENT", "GENRES" );
                break;
        }

        startActivity( intent );
        overridePendingTransition( R.anim.fade_in, R.anim.fade_out );
        finish();

    }

    @Override
    public void onPause() {
        super.onPause();
        finish();

    }
}
