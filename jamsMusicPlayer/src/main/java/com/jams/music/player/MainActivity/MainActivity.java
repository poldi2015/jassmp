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
package com.jams.music.player.MainActivity;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.DrawerLayout;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.jams.music.player.Drawers.NavigationDrawerFragment;
import com.jams.music.player.Drawers.QueueDrawerFragment;
import com.jams.music.player.Helpers.UIElementsHelper;
import com.jams.music.player.ListViewFragment.FilterListViewFragment;
import com.jams.music.player.ListViewFragment.SongListViewFragment;
import com.jams.music.player.R;
import com.jams.music.player.Utils.Common;

public class MainActivity extends FragmentActivity {

    public static enum FragmentId {
        NONE, SONGS, GENRES, ARTISTS, ALBUMS
    }

    //Context and Common object(s).
    private Context mContext;
    private Common  mApp;

    //UI elements.
    private FrameLayout           mDrawerParentLayout;
    private DrawerLayout          mDrawerLayout;
    private RelativeLayout        mNavDrawerLayout;
    private RelativeLayout        mCurrentQueueDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private QueueDrawerFragment   mQueueDrawerFragment;
    private Menu                  mMenu;

    private FragmentId mCurrentFragmentId = null;

    //Layout flags.
    public static final String CURRENT_FRAGMENT = "CurrentFragment";
    public static final String FRAGMENT_HEADER  = "FragmentHeader";

    @Override
    public void onCreate( Bundle savedInstanceState ) {
        //Context and Common object(s).
        mContext = getApplicationContext();
        mApp = (Common) getApplicationContext();

        //Set the theme and inflate the layout.
        setTheme();
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_main );

        //Init the UI elements.
        mDrawerParentLayout = (FrameLayout) findViewById( R.id.main_activity_root );
        mDrawerLayout = (DrawerLayout) findViewById( R.id.main_activity_drawer_root );
        mNavDrawerLayout = (RelativeLayout) findViewById( R.id.nav_drawer_container );
        mCurrentQueueDrawerLayout = (RelativeLayout) findViewById( R.id.current_queue_drawer_container );

        //Load the drawer fragments.
        loadDrawerFragments();

        //KitKat specific translucency.
        applyKitKatTranslucency();

        //Load the fragment.
        switchFragment( FragmentId.NONE, savedInstanceState );

        /**
         * Navigation drawer toggle.
         */
        mDrawerToggle = new ActionBarDrawerToggle( this, mDrawerLayout, R.drawable.ic_navigation_drawer, 0, 0 ) {

            @Override
            public void onDrawerClosed( View view ) {
                if( mQueueDrawerFragment != null && view == mCurrentQueueDrawerLayout ) {
                    mQueueDrawerFragment.setIsDrawerOpen( false );
                }

            }

            @Override
            public void onDrawerOpened( View view ) {
                if( mQueueDrawerFragment != null && view == mCurrentQueueDrawerLayout ) {
                    mQueueDrawerFragment.setIsDrawerOpen( true );
                }

            }

        };

        //Apply the drawer toggle to the DrawerLayout.
        mDrawerLayout.setDrawerListener( mDrawerToggle );
        final ActionBar actionBar = getActionBar();
        if( actionBar != null ) {
            actionBar.setDisplayHomeAsUpEnabled( true );
            actionBar.setDisplayShowHomeEnabled( true );
        }

        //Check if this is the first time the app is being started.
        if( mApp.getSharedPreferences().getBoolean( Common.FIRST_RUN, true ) ) {
            showAlbumArtScanningDialog();
            mApp.getSharedPreferences().edit().putBoolean( Common.FIRST_RUN, false ).commit();
        }
    }

    @Override
    protected void onSaveInstanceState( final Bundle savedInstanceState ) {
        saveFragmentId( savedInstanceState, mCurrentFragmentId );
        super.onSaveInstanceState( savedInstanceState );
    }

    /**
     * Sets the entire activity-wide theme.
     */
    private void setTheme() {
        //Set the UI theme.
        if( mApp.getCurrentTheme() == Common.DARK_THEME ) {
            setTheme( R.style.AppTheme );
        } else {
            setTheme( R.style.AppThemeLight );
        }
    }

    /**
     * Apply KitKat specific translucency.
     */
    private void applyKitKatTranslucency() {

        //KitKat translucent navigation/status bar.
        if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT ) {

            //Set the window background.
            getWindow().setBackgroundDrawable( UIElementsHelper.getGeneralActionBarBackground( mContext ) );

            int topPadding = Common.getStatusBarHeight( mContext );
            if( mDrawerLayout != null ) {
                mDrawerLayout.setPadding( 0, topPadding, 0, 0 );
                mNavDrawerLayout.setPadding( 0, topPadding, 0, 0 );
                mCurrentQueueDrawerLayout.setPadding( 0, topPadding, 0, 0 );
            }

            //Calculate ActionBar and navigation bar height.
            TypedValue tv = new TypedValue();
            int actionBarHeight = 0;
            if( getTheme().resolveAttribute( android.R.attr.actionBarSize, tv, true ) ) {
                actionBarHeight = TypedValue.complexToDimensionPixelSize( tv.data, getResources().getDisplayMetrics() );
            }

            if( mDrawerParentLayout != null ) {
                mDrawerParentLayout.setPadding( 0, actionBarHeight, 0, 0 );
                mDrawerParentLayout.setClipToPadding( false );
            }

        }
    }

    public void switchFragment( final FragmentId fragmentId ) {
        switchFragment( fragmentId, null );
    }

    /**
     * Loads the specified fragment into the target layout.
     */
    public void switchFragment( FragmentId fragmentId, final Bundle bundle ) {
        if( fragmentId == null ) {
            fragmentId = FragmentId.NONE;
        }
        if( fragmentId == FragmentId.NONE ) {
            if( bundle != null ) {
                // From bundle

                fragmentId = loadFragmentId( bundle );
            } else if( getIntent() != null && getIntent().getExtras() != null ) {
                // from intent

                fragmentId = loadFragmentId( getIntent().getExtras() );
            } else {
                // default

                fragmentId = FragmentId.SONGS;
            }
        }
        if( fragmentId == mCurrentFragmentId ) {
            return;
        }

        // Update ActionBar
        final ActionBar actionBar = getActionBar();
        if( actionBar != null ) {
            getActionBar().setDisplayHomeAsUpEnabled( true );
            getActionBar().setDisplayShowHomeEnabled( true );
            getActionBar().setDisplayShowCustomEnabled( false );
        }

        mCurrentFragmentId = fragmentId;

        // Switch fragment
        getSupportFragmentManager().beginTransaction()
                                   .replace( R.id.mainActivityContainer, getLayoutFragment( mCurrentFragmentId ) )
                                   .commit();

        //Close the drawer(s).
        mDrawerLayout.closeDrawer( Gravity.START );

        // Invalidate Options
        invalidateOptionsMenu();
    }

    /**
     * Retrieves the correct fragment based on the saved layout preference.
     */
    private Fragment getLayoutFragment( final FragmentId fragmentId ) {
        Fragment fragment;
        Bundle bundle = new Bundle();

        switch( fragmentId ) {
            case ARTISTS:
                fragment = new FilterListViewFragment();
                bundle.putSerializable( Common.FRAGMENT_ID, fragmentId );
                bundle.putString( FRAGMENT_HEADER, mContext.getResources().getString( R.string.album_artists ) );
                break;
            case ALBUMS:
                fragment = new FilterListViewFragment();
                bundle.putSerializable( Common.FRAGMENT_ID, fragmentId );
                bundle.putString( FRAGMENT_HEADER, mContext.getResources().getString( R.string.albums ) );
                break;
            case GENRES:
                fragment = new FilterListViewFragment();
                bundle.putSerializable( Common.FRAGMENT_ID, fragmentId );
                bundle.putString( FRAGMENT_HEADER, mContext.getResources().getString( R.string.genres ) );
                break;
            case SONGS:
            default:
                fragment = new SongListViewFragment();
                break;
        }
        fragment.setArguments( bundle );

        return fragment;
    }

    /**
     * Loads the drawer fragments.
     */
    private void loadDrawerFragments() {
        //Load the navigation drawer.
        getSupportFragmentManager().beginTransaction()
                                   .replace( R.id.nav_drawer_container, new NavigationDrawerFragment() )
                                   .commit();

        //Load the current queue drawer.
        mQueueDrawerFragment = new QueueDrawerFragment();
        getSupportFragmentManager().beginTransaction()
                                   .replace( R.id.current_queue_drawer_container, mQueueDrawerFragment )
                                   .commit();
    }

    /**
     * Displays the message dialog for album art processing.
     */
    private void showAlbumArtScanningDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder( this );
        builder.setTitle( R.string.album_art );
        builder.setMessage( R.string.scanning_for_album_art_details );
        builder.setPositiveButton( R.string.got_it, new DialogInterface.OnClickListener() {

            @Override
            public void onClick( DialogInterface dialog, int which ) {
                dialog.dismiss();
            }

        } );

        builder.create().show();
    }

    /**
     * Inflates the generic MainActivity ActionBar layout.
     *
     * @param inflater The ActionBar's menu inflater.
     * @param menu     The ActionBar menu to work with.
     */
    private void showMainActivityActionItems( MenuInflater inflater, Menu menu ) {
        //Inflate the menu.
        getMenu().clear();
        inflater.inflate( R.menu.main_activity, menu );

        //Set the ActionBar background
        final ActionBar actionBar = getActionBar();
        if( actionBar != null ) {
            actionBar.setBackgroundDrawable( UIElementsHelper.getGeneralActionBarBackground( mContext ) );
            actionBar.setDisplayShowTitleEnabled( true );
            actionBar.setDisplayUseLogoEnabled( false );
            actionBar.setHomeButtonEnabled( true );
        }

        //Set the ActionBar text color.
        int actionBarTitleId = Resources.getSystem().getIdentifier( "action_bar_title", "id", "android" );
        if( actionBarTitleId > 0 ) {
            TextView title = (TextView) findViewById( actionBarTitleId );
            if( title != null ) {
                title.setTextColor( 0xFFFFFFFF );
            }

        }

    }

    /**
     * Initializes the ActionBar.
     */
    @Override
    public boolean onCreateOptionsMenu( Menu menu ) {
        mMenu = menu;
        showMainActivityActionItems( getMenuInflater(), menu );

        return super.onCreateOptionsMenu( menu );
    }

    /**
     * ActionBar item selection listener.
     */
    @Override
    public boolean onOptionsItemSelected( MenuItem item ) {

        if( mDrawerToggle.onOptionsItemSelected( item ) ) {
            return true;
        }

        switch( item.getItemId() ) {
            case R.id.action_search:
                //ArtistsFragment.showSearch();
                return true;
            case R.id.action_queue_drawer:
                if( mDrawerLayout != null && mCurrentQueueDrawerLayout != null ) {
                    if( mDrawerLayout.isDrawerOpen( mCurrentQueueDrawerLayout ) ) {
                        mDrawerLayout.closeDrawer( mCurrentQueueDrawerLayout );
                    } else {
                        mDrawerLayout.openDrawer( mCurrentQueueDrawerLayout );
                    }

                }
                return true;
            default:
                return super.onOptionsItemSelected( item );
        }

    }

    @Override
    protected void onPostCreate( Bundle savedInstanceState ) {
        super.onPostCreate( savedInstanceState );
        mDrawerToggle.syncState();
    }

    @Override
    public void onBackPressed() {

        if( mDrawerLayout.isDrawerOpen( Gravity.START ) ) { // Close left drawer if opened
            mDrawerLayout.closeDrawer( Gravity.START );
        } else {
            super.onBackPressed();
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        if( Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT ) {
            final ActionBar actionBar = getActionBar();
            if( actionBar != null ) {
                actionBar.setBackgroundDrawable( UIElementsHelper.getGeneralActionBarBackground( mContext ) );
            }
            getWindow().setBackgroundDrawable( UIElementsHelper.getGeneralActionBarBackground( mContext ) );
        }

    }

    public Menu getMenu() {
        return mMenu;
    }

    private FragmentId loadFragmentId( final Bundle bundle ) {
        return FragmentId.valueOf( bundle.getString( CURRENT_FRAGMENT, FragmentId.NONE.name() ) );
    }

    private void saveFragmentId( final Bundle bundle, FragmentId fragmentId ) {
        if( fragmentId == null ) {
            fragmentId = FragmentId.NONE;
        }
        bundle.putString( CURRENT_FRAGMENT, fragmentId.name() );
    }

    public FragmentId getCurrentFragmentId() {
        return mCurrentFragmentId;
    }
}
