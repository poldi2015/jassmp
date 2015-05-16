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
package com.jassmp.Drawers;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.jassmp.Helpers.UIElementsHelper;
import com.jassmp.MainActivity.MainActivity;
import com.jassmp.R;
import com.jassmp.SettingsActivity.SettingsActivity;
import com.jassmp.Utils.Common;

public class NavigationDrawerFragment extends Fragment {

    //
    // defines

    public static enum MenuItem {
        SONGS( 0, MainActivity.FragmentId.SONGS ),
        GENRES( 1, MainActivity.FragmentId.GENRES ),
        ARTISTS( 2, MainActivity.FragmentId.ARTISTS ),
        ALBUMS( 3, MainActivity.FragmentId.ALBUMS ),
        SETTINGS( 4, SettingsActivity.class );

        public final int                     position;
        public final MainActivity.FragmentId fragmentId;
        public final Class<?>                activity;

        public         String     title      = null;
        public static  String[]   titles     = null;
        private static MenuItem[] byPosition = null;

        private MenuItem( final int position, final MainActivity.FragmentId fragmentId ) {
            this( position, fragmentId, null );
        }

        private MenuItem( final int position, final Class<?> activity ) {
            this( position, MainActivity.FragmentId.NONE, activity );
        }

        private MenuItem( final int position, final MainActivity.FragmentId fragmentId, final Class<?> activity ) {
            this.position = position;
            this.fragmentId = fragmentId;
            this.activity = activity;
        }

        public static void init( final Activity activity ) {
            final MenuItem[] items = MenuItem.values();
            byPosition = new MenuItem[ items.length ];
            for( final MenuItem item : items ) {
                byPosition[ item.position ] = item;
            }

            titles = activity.getResources().getStringArray( R.array.sliding_menu_array );
            for( MenuItem item : MenuItem.values() ) {
                item.title = titles[ item.position ];
            }
        }

        public boolean isFragment() {
            return fragmentId != MainActivity.FragmentId.NONE;
        }

        public static MenuItem getByPosition( final int position ) {
            return byPosition[ position ];
        }
    }

    //
    // private members

    private ListView mmMenuItemsView;
    private Handler  mHandler;

    @SuppressWarnings("deprecation")
    @SuppressLint("NewApi")
    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState ) {
        final Context context = getActivity();
        mHandler = new Handler();

        MenuItem.init( getActivity() );

        final View rootView = inflater.inflate( R.layout.navigation_drawer_layout, null );
        rootView.setBackgroundColor( UIElementsHelper.getBackgroundColor( context ) );

        mmMenuItemsView = (ListView) rootView.findViewById( R.id.browsers_list_view );
        resetAdapter( getActivity() );
        mmMenuItemsView.setOnItemClickListener( mMenuItemsClickListener );
        //setListViewHeightBasedOnChildren( mmMenuItemsView );

        mmMenuItemsView.setDividerHeight( 0 );

        //KitKat translucent navigation/status bar.
        if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT ) {
            int navBarHeight = Common.getNavigationBarHeight( context );
            if( mmMenuItemsView != null ) {
                mmMenuItemsView.setPadding( 0, 0, 0, navBarHeight );
                mmMenuItemsView.setClipToPadding( false );
            }

        }


        return rootView;
    }

    private OnItemClickListener mMenuItemsClickListener = new OnItemClickListener() {

        @Override
        public void onItemClick( final AdapterView<?> adapterView, final View view, final int position, final long dbID ) {
            final MenuItem menuItem = MenuItem.values()[ position ];
            if( menuItem.isFragment() ) {
                ( (MainActivity) getActivity() ).switchFragment( menuItem.fragmentId );
            } else {
                Intent intent = new Intent( getActivity(), SettingsActivity.class );
                startActivity( intent );
            }

            resetAdapter( getActivity() );

            //Reset the ActionBar after 500ms.
            mHandler.postDelayed( new Runnable() {
                @Override
                public void run() {
                    getActivity().invalidateOptionsMenu();

                }
            }, 500 );

        }

    };

    public void resetAdapter( final Activity activity ) {
        MainActivity.FragmentId currentFragment = MainActivity.FragmentId.NONE;
        if( activity instanceof MainActivity ) {
            currentFragment = ( (MainActivity) activity ).getCurrentFragmentId();

        }
        mmMenuItemsView.setAdapter( new NavigationDrawerAdapter( activity, currentFragment ) );
    }


    /**
     * Clips ListViews to fit within the drawer's boundaries.
     */
    private void setListViewHeightBasedOnChildren( ListView listView ) {
        ListAdapter listAdapter = listView.getAdapter();
        if( listAdapter == null ) {
            // pre-condition
            return;
        }

        int totalHeight = 0;
        for( int i = 0; i < listAdapter.getCount(); i++ ) {
            View listItem = listAdapter.getView( i, null, listView );
            listItem.measure( 0, 0 );
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + ( listView.getDividerHeight() * ( listAdapter.getCount() - 1 ) );
        listView.setLayoutParams( params );
        listView.requestLayout();
    }

}
