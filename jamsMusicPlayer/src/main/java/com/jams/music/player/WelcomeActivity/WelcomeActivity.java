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
package com.jams.music.player.WelcomeActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;

import com.jams.music.player.AsyncTasks.AsyncSaveMusicFoldersTask;
import com.jams.music.player.R;
import com.jams.music.player.Services.BuildMusicLibraryService;
import com.viewpagerindicator.LinePageIndicator;

public class WelcomeActivity extends FragmentActivity {

    //
    // defines

    public static final String INTENT_REFRESH_MUSIC_LIBRARY = "REFRESH_MUSIC_LIBRARY";
    public static final int    FRAGMENT_WELCOME             = 0;
    public static final int    FRAGMENT_MUSIC_FOLDERS       = 1;
    public static final int    FRAGMENT_ALBUM_ART           = 2;
    public static final int    FRAGMENT_READ_TO_SCAN        = 3;
    public static final int    FRAGMENT_BUILD_LIBRARY       = 4;

    //
    // private members

    private Context           mContext;
    private ViewPager         mPager;
    private LinePageIndicator mIndicator;

    private       MusicFoldersFragment            mMusicFoldersFragment;
    public static BuildingLibraryProgressFragment mBuildingLibraryProgressFragment;

    @Override
    public void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        mContext = this;
        overridePendingTransition( R.anim.fade_in, R.anim.fade_out );

        setContentView( R.layout.activity_welcome );
        setTheme( R.style.AppThemeLight );

        if( getActionBar() != null ) {
            getActionBar().hide();
        }

        mPager = (ViewPager) findViewById( R.id.welcome_pager );
        mPager.setAdapter( new WelcomePagerAdapter( getSupportFragmentManager() ) );
        mPager.setOffscreenPageLimit( 6 );

        final float density = getResources().getDisplayMetrics().density;
        mIndicator = (LinePageIndicator) findViewById( R.id.indicator );
        mIndicator.setViewPager( mPager );
        mIndicator.setSelectedColor( 0x880099CC );
        mIndicator.setUnselectedColor( 0xFF4F4F4F );
        mIndicator.setStrokeWidth( 2 * density );
        mIndicator.setLineWidth( 30 * density );
        mIndicator.setOnPageChangeListener( mPageChangeListener );

        //Check if the library needs to be rebuilt and this isn't the first run.
        if( getIntent().hasExtra( INTENT_REFRESH_MUSIC_LIBRARY ) ) {
            showBuildingLibraryProgress();
        }
    }

    /**
     * Page scroll listener.
     */
    private final OnPageChangeListener mPageChangeListener = new OnPageChangeListener() {

        @Override
        public void onPageScrollStateChanged( int scrollState ) {
        }

        @Override
        public void onPageScrolled( int position, float positionOffset, int positionOffsetPixels ) {
        }

        @Override
        public void onPageSelected( int page ) {
            switch( page ) {
                case FRAGMENT_WELCOME:
                case FRAGMENT_ALBUM_ART:
                    // If the user swiped away from the music folders selection fragment,
                    // save the music folders to the database.
                    new AsyncSaveMusicFoldersTask( mContext.getApplicationContext(),
                                                   mMusicFoldersFragment.getMusicFoldersSelectionFragment()
                                                                        .getMusicFolders() ).execute();

                    break;
                case FRAGMENT_BUILD_LIBRARY:
                    // If the user swiped away from the music folders selection fragment,
                    // save the music folders tothe database.
                    showBuildingLibraryProgress();
                    break;
            }
        }

    };

    private void showBuildingLibraryProgress() {
        //Disables swiping events on the pager.
        mPager.setCurrentItem( FRAGMENT_BUILD_LIBRARY );
        mPager.setOnTouchListener( new OnTouchListener() {

            @Override
            public boolean onTouch( View arg0, MotionEvent arg1 ) {
                return true;
            }

        } );

        //Fade out the ViewPager indicator.
        final Animation fadeOutAnim = AnimationUtils.loadAnimation( mContext, R.anim.fade_out );
        fadeOutAnim.setDuration( 600 );
        fadeOutAnim.setAnimationListener( new AnimationListener() {

            @Override
            public void onAnimationEnd( Animation arg0 ) {
                mIndicator.setVisibility( View.INVISIBLE );
                final Intent intent = new Intent( mContext, BuildMusicLibraryService.class );
                startService( intent );
            }

            @Override
            public void onAnimationRepeat( Animation arg0 ) {
            }

            @Override
            public void onAnimationStart( Animation arg0 ) {
            }

        } );
        mIndicator.startAnimation( fadeOutAnim );
    }

    class WelcomePagerAdapter extends FragmentStatePagerAdapter {

        public WelcomePagerAdapter( FragmentManager fm ) {
            super( fm );
        }

        @Override
        public Fragment getItem( int position ) {
            switch( position ) {
                case FRAGMENT_WELCOME:
                    return new WelcomeFragment();
                case FRAGMENT_MUSIC_FOLDERS:
                    mMusicFoldersFragment = new MusicFoldersFragment();
                    return mMusicFoldersFragment;
                case FRAGMENT_ALBUM_ART:
                    return new AlbumArtFragment();
                case FRAGMENT_READ_TO_SCAN:
                    return new ReadyToScanFragment();
                case FRAGMENT_BUILD_LIBRARY:
                    mBuildingLibraryProgressFragment = new BuildingLibraryProgressFragment();
                    return mBuildingLibraryProgressFragment;
                default:
                    return null;
            }

        }

        @Override
        public int getCount() {
            return 5;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        finish();
    }

}
