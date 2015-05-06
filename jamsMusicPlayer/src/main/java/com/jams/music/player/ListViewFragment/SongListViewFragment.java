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
package com.jams.music.player.ListViewFragment;

import android.content.Context;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.andraskindler.quickscroll.QuickScroll;
import com.jams.music.player.DBHelpers.DBAccessHelper;
import com.jams.music.player.DBHelpers.OrderDirection;
import com.jams.music.player.Dialogs.OrderDialog;
import com.jams.music.player.Helpers.PauseOnScrollHelper;
import com.jams.music.player.Helpers.TypefaceHelper;
import com.jams.music.player.Helpers.UIElementsHelper;
import com.jams.music.player.MainActivity.MainActivity;
import com.jams.music.player.R;
import com.jams.music.player.Utils.Common;
import com.jams.music.player.Utils.SynchronizedAsyncTask;

/**
 * Generic, multipurpose ListView fragment.
 *
 * @author Saravan Pantham
 */
public class SongListViewFragment extends Fragment implements OrderDialog.OrderDialogListener {

    private static final SparseArray<String> ITEM_TO_ORDER_BY;

    static {
        ITEM_TO_ORDER_BY = new SparseArray<String>();
        ITEM_TO_ORDER_BY.put( R.id.song_list_sort_name, DBAccessHelper.SONG_TITLE );
        ITEM_TO_ORDER_BY.put( R.id.song_list_sort_rating, DBAccessHelper.SONG_RATING );
        ITEM_TO_ORDER_BY.put( R.id.song_list_sort_bpm, DBAccessHelper.SONG_BPM );
        ITEM_TO_ORDER_BY.put( R.id.song_list_sort_artist, DBAccessHelper.SONG_ARTIST );
        ITEM_TO_ORDER_BY.put( R.id.song_list_sort_duration, DBAccessHelper.SONG_DURATION );
    }

    private Context mContext  = null;
    private Common  mApp      = null;
    private View    mRootView = null;

    private SynchronizedAsyncTask mAsyncExecutorTask = null;

    private QuickScroll mQuickScroll = null;
    private ListView    mListView    = null;

    private String mQuerySelection = "";


    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState ) {
        setHasOptionsMenu( true );
        mRootView = inflater.inflate( R.layout.fragment_list_view, container, false );
        mContext = getActivity().getApplicationContext();
        mApp = (Common) mContext;
        mAsyncExecutorTask = new SynchronizedAsyncTask();

        //Set the background. We're using getGridViewBackground() since the list doesn't have
        // card items.
        mRootView.setBackgroundColor( UIElementsHelper.getGridViewBackground( mContext ) );

        // Init list view
        mQuickScroll = (QuickScroll) mRootView.findViewById( R.id.quickscroll );
        mListView = (ListView) mRootView.findViewById( R.id.generalListView );
        mListView.setVerticalScrollBarEnabled( false );

        //Apply the ListViews' dividers.
        if( mApp.getCurrentTheme() == Common.DARK_THEME ) {
            mListView.setDivider( mContext.getResources().getDrawable( R.drawable.icon_list_divider ) );
        } else {
            mListView.setDivider( mContext.getResources().getDrawable( R.drawable.icon_list_divider_light ) );
        }
        mListView.setDividerHeight( 1 );

        //KitKat translucent navigation/status bar.
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
            mQuickScroll.setPadding( 0, topPadding, 0, navigationBarHeight );
        }

        //Set the empty views.
        final TextView mEmptyTextView = (TextView) mRootView.findViewById( R.id.empty_view_text );
        mEmptyTextView.setTypeface( TypefaceHelper.getTypeface( mContext, "Roboto-Light" ) );
        mEmptyTextView.setPaintFlags(
                mEmptyTextView.getPaintFlags() | Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG );

        //Create a set of options to optimize the bitmap memory usage.
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        options.inJustDecodeBounds = false;
        options.inPurgeable = true;

        reloadDatabase( 400, null );

        return mRootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        //Set the ActionBar title.
        if( getActivity().getActionBar() != null ) {
            getActivity().getActionBar().setTitle( mContext.getResources().getString( R.string.songs ) );
        }

    }

    @Override
    public void onDestroyView() {
        mAsyncExecutorTask.dispose();

        mRootView = null;
        onItemClickListener = null;
        mListView = null;
        mListView = null;
        mContext = null;

        super.onDestroyView();
    }


    /**
     * Loads or reloads the database and refreshes the views if necessary.
     *
     * @param delay   delay in millis
     * @param orderBy column to order with
     */
    public void reloadDatabase( final int delay, final String orderBy ) {
        if( mAsyncExecutorTask.isDisposed() ) {
            mAsyncExecutorTask = new SynchronizedAsyncTask();
        }
        mAsyncExecutorTask.execute( delay, new AsyncRunQuery( orderBy, null ) );
    }


    @Override
    public boolean onOptionsItemSelected( MenuItem item ) {
        switch( item.getItemId() ) {
            case R.id.action_order:
                new OrderDialog( this ).show( getFragmentManager(), "OrderDialog" );
                break;
        }
        return super.onOptionsItemSelected( item );
    }

    @Override
    public void onOrderDialogClick( int which ) {
        final String orderBy = ITEM_TO_ORDER_BY.valueAt( which );
        reloadDatabase( 0, orderBy );
    }

    @SuppressWarnings("unused")
    private void setSortOrderIcon( final MenuItem item, final String orderBy ) {
        //        if( orderBy == null ) return;
        //        final DBAccessHelper.OrderDirection orderDirection = getOrderDirectionFromPreferences(
        // orderBy );
        //        switch( orderDirection ) {
        //            case ASC:
        //                item.setIcon( R.drawable.down );
        //                break;
        //            case DESC:
        //                item.setIcon( R.drawable.down );
        //                break;
        //        }
    }

    /**
     * Item click listener for the ListView.
     */
    private OnItemClickListener onItemClickListener = new OnItemClickListener() {

        @Override
        public void onItemClick( AdapterView<?> arg0, View view, int index, long id ) {
            mApp.getPlaybackKickstarter()
                .initPlayback( mContext, mQuerySelection, MainActivity.FragmentId.SONGS, index, true, false );
        }

    };

    /**
     * @return current sort order from the preferences
     */
    private String getOrderByFromPreferences() {
        return mApp.getSharedPreferences().getString( Common.SORT_COLUMN, DBAccessHelper.SONG_TITLE );
    }

    /**
     * Save sort order to preferences
     *
     * @param orderBy the new sort order
     */
    private void setOrderByInPreferences( final String orderBy ) {
        mApp.getSharedPreferences().edit().putString( Common.SORT_COLUMN, orderBy ).apply();
    }

    /**
     * Get the currently set orderDirection from the preferences.
     *
     * @param orderBy the order column or null to take the current from the preferences
     * @return the direction
     */
    private OrderDirection getOrderDirectionFromPreferences( String orderBy ) {
        if( orderBy == null ) {
            orderBy = mApp.getSharedPreferences().getString( Common.SORT_COLUMN, DBAccessHelper.SONG_TITLE );
        }

        return OrderDirection.valueOf( mApp.getSharedPreferences()
                                           .getString( Common.SORT_DIRECTION_COLUMN + "_" + orderBy,
                                                       OrderDirection.ASC.name() ) );
    }

    private void setOrderDirectionInPreferences( final String orderBy, final OrderDirection orderDirection ) {
        mApp.getSharedPreferences()
            .edit()
            .putString( Common.SORT_DIRECTION_COLUMN + "_" + orderBy, orderDirection.name() )
            .apply();
    }

    /**
     * Runs the correct DB query based on the passed in fragment id and
     * displays the ListView.
     */
    public class AsyncRunQuery extends SynchronizedAsyncTask.Executor {

        private SongListViewItemAdapter mListViewAdapter = null;
        private Cursor                  mCursor          = null;

        private String         mOrderBy        = DBAccessHelper.SONG_TITLE;
        private OrderDirection mOrderDirection = OrderDirection.ASC;

        public AsyncRunQuery( String orderBy, OrderDirection orderDirection ) {
            super();

            if( orderBy == null ) {
                orderBy = getOrderByFromPreferences();
            } else {
                setOrderByInPreferences( orderBy );
            }
            if( orderDirection == null ) {
                orderDirection = getOrderDirectionFromPreferences( orderBy );
            } else {
                setOrderDirectionInPreferences( orderBy, orderDirection );
            }

            mOrderBy = orderBy;
            mOrderDirection = orderDirection;

            // Reverse order Direction
            setOrderDirectionInPreferences( orderBy, orderDirection == OrderDirection.ASC ? OrderDirection.DESC
                                                                                          : OrderDirection.ASC );
        }

        @Override
        public void doInBackground() {
            mCursor = mApp.getDBAccessHelper()
                          .getFragmentCursor( mContext, mQuerySelection, MainActivity.FragmentId.SONGS, mOrderBy,
                                              mOrderDirection );
        }

        @Override
        public void cancel() {
            if( mCursor != null && !mCursor.isClosed() ) {
                if( mListViewAdapter != null ) {
                    mListViewAdapter.changeCursor( null );
                }
                mCursor.close();
            }
        }

        @Override
        public void onPostExecute() {
            if( mListViewAdapter == null ) {
                createList();
            } else {
                reloadList();
            }
        }

        private Animation createAnimation() {
            TranslateAnimation animation = new TranslateAnimation( Animation.RELATIVE_TO_SELF, 0.0f,
                                                                   Animation.RELATIVE_TO_SELF, 0.0f,
                                                                   Animation.RELATIVE_TO_SELF, 2.0f,
                                                                   Animation.RELATIVE_TO_SELF, 0.0f );

            animation.setDuration( 300 );
            animation.setInterpolator( new AccelerateDecelerateInterpolator() );

            animation.setAnimationListener( new AnimationListener() {

                @Override
                public void onAnimationEnd( Animation arg0 ) {
                    mQuickScroll.setVisibility( View.VISIBLE );

                }

                @Override
                public void onAnimationRepeat( Animation arg0 ) {
                }

                @Override
                public void onAnimationStart( Animation arg0 ) {
                    mListView.setVisibility( View.VISIBLE );

                }

            } );

            return animation;
        }

        private void createList() {
            mListViewAdapter = new SongListViewItemAdapter( mContext, mCursor, SongListViewFragment.this );
            mListView.setAdapter( mListViewAdapter );
            mListView.setOnItemClickListener( onItemClickListener );

            //Init the quick scroll widget.
            mQuickScroll.init( QuickScroll.TYPE_INDICATOR_WITH_HANDLE, mListView, mListViewAdapter,
                               QuickScroll.STYLE_HOLO );

            int[] quickScrollColors = UIElementsHelper.getQuickScrollColors( mContext );
            PauseOnScrollHelper scrollListener = new PauseOnScrollHelper( mApp.getPicasso(), null, true, true );

            mQuickScroll.setOnScrollListener( scrollListener );
            mQuickScroll.setPicassoInstance( mApp.getPicasso() );
            mQuickScroll.setHandlebarColor( quickScrollColors[ 0 ], quickScrollColors[ 0 ], quickScrollColors[ 1 ] );
            mQuickScroll.setIndicatorColor( quickScrollColors[ 1 ], quickScrollColors[ 0 ], quickScrollColors[ 2 ] );
            mQuickScroll.setTextSize( TypedValue.COMPLEX_UNIT_DIP, 48 );


            mListView.startAnimation( createAnimation() );
        }

        private void reloadList() {
            mListViewAdapter.swapCursor( mCursor );
            mListView.startAnimation( createAnimation() );
        }

    }

}
