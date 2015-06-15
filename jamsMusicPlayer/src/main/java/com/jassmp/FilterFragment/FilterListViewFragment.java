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
package com.jassmp.FilterFragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.andraskindler.quickscroll.QuickScroll;
import com.jassmp.Dao.FilterCursorAdapter;
import com.jassmp.Dao.FilterDao;
import com.jassmp.GuiHelper.ViewStyleHelper;
import com.jassmp.JassMpDb.AbstractFilterTableAccessor;
import com.jassmp.JassMpDb.AlbumFilterTableAccessor;
import com.jassmp.JassMpDb.ArtistFilterTableAccessor;
import com.jassmp.JassMpDb.GenreFilterTableAccessor;
import com.jassmp.JassMpDb.OrderDirection;
import com.jassmp.MainActivity.MainActivity;
import com.jassmp.R;
import com.jassmp.Utils.Common;
import com.jassmp.Utils.SynchronizedAsyncTask;

import java.util.EnumMap;

public class FilterListViewFragment extends Fragment {

    //
    // defines

    public static final EnumMap<MainActivity.FragmentId, AbstractFilterTableAccessor> FRAGMENT_TO_FILTER
            = new EnumMap<MainActivity.FragmentId, AbstractFilterTableAccessor>( MainActivity.FragmentId.class );

    //
    // private members

    private Context         mContext         = null;
    private Common          mApp             = null;
    private View            mRootView        = null;
    private ViewStyleHelper mViewStyleHelper = null;

    private MainActivity.FragmentId mFragmentId    = MainActivity.FragmentId.NONE;
    private String                  mFragmentTitle = null;

    private SynchronizedAsyncTask     mAsyncExecutorTask = null;
    private FilterListViewItemAdapter mListViewAdapter   = null;

    private QuickScroll mQuickScroll = null;
    private ListView    mListView    = null;


    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState ) {
        mContext = getActivity().getApplicationContext();
        mApp = (Common) mContext;
        mAsyncExecutorTask = new SynchronizedAsyncTask();
        initFragmentToFilter( mContext );

        //Grab the fragment. This will determine which data to load into the cursor.
        mFragmentId = (MainActivity.FragmentId) getArguments().getSerializable( Common.FRAGMENT_ID );
        mFragmentTitle = getArguments().getString( MainActivity.FRAGMENT_HEADER );

        mRootView = inflater.inflate( R.layout.fragment_list_view, container, false );
        mQuickScroll = (QuickScroll) mRootView.findViewById( R.id.quickscroll );
        mListView = (ListView) mRootView.findViewById( R.id.generalListView );
        mListView.requestFocusFromTouch();
        final TextView mEmptyTextView = (TextView) mRootView.findViewById( R.id.empty_view_text );

        mViewStyleHelper = new ViewStyleHelper( mApp, mContext );
        mViewStyleHelper.styleRootView( mRootView );
        mViewStyleHelper.styleListView( mQuickScroll, mListView );
        mViewStyleHelper.styleEmptyView( mEmptyTextView );

        setHasOptionsMenu( true );

        updateAndReloadDatabaseToList( null, 400 );

        return mRootView;
    }

    private void initFragmentToFilter( final Context context ) {
        FRAGMENT_TO_FILTER.put( MainActivity.FragmentId.GENRES, GenreFilterTableAccessor.getInstance( context ) );
        FRAGMENT_TO_FILTER.put( MainActivity.FragmentId.ARTISTS, ArtistFilterTableAccessor.getInstance( context ) );
        FRAGMENT_TO_FILTER.put( MainActivity.FragmentId.ALBUMS, AlbumFilterTableAccessor.getInstance( context ) );

    }

    @Override
    public void onResume() {
        super.onResume();

        //Set the ActionBar title.
        if( getActivity().getActionBar() != null ) {
            getActivity().getActionBar().setTitle( mFragmentTitle );
        }
    }

    @Override
    public void onDestroyView() {
        mAsyncExecutorTask.dispose();

        mRootView = null;
        mListView = null;
        mListView = null;
        mContext = null;
        mViewStyleHelper = null;

        super.onDestroyView();
    }


    /**
     * Loads or reloads the database and refreshes the views if necessary.
     *
     * @param delay     delay in millis
     * @param operation Optional operation to manipulate the database
     */
    public void updateAndReloadDatabaseToList( final Runnable operation, final int delay ) {
        if( mAsyncExecutorTask.isDisposed() ) {
            mAsyncExecutorTask = new SynchronizedAsyncTask();
        }
        mAsyncExecutorTask.execute( delay, new AsyncUpdateAndLoadDatabaseToList( operation ) );
    }

    @Override
    public void onCreateOptionsMenu( final Menu menu, final MenuInflater inflater ) {
        inflater.inflate( R.menu.filter_list_view_fragment_menu, menu );
        super.onCreateOptionsMenu( menu, inflater );
    }

    @Override
    public boolean onOptionsItemSelected( MenuItem item ) {
        switch( item.getItemId() ) {
            case R.id.action_clear_filter:
                clearAllFilters();
                break;
        }
        return super.onOptionsItemSelected( item );
    }

    private void clearAllFilters() {
        updateAndReloadDatabaseToList( new Runnable() {
            @Override
            public void run() {
                for( final AbstractFilterTableAccessor filterTableAccessor : FRAGMENT_TO_FILTER.values() ) {
                    filterTableAccessor.setSelectedAll( true );
                }
            }
        }, 0 );
        Toast.makeText( mContext, R.string.filters_clear_all, Toast.LENGTH_SHORT ).show();
    }

    /**
     * Runs the correct DB query based on the passed in fragment id and
     * displays the ListView.
     */
    private class AsyncUpdateAndLoadDatabaseToList extends SynchronizedAsyncTask.Executor {

        private FilterCursorAdapter mCursorAdapter = null;
        private final Runnable mOperation;

        public AsyncUpdateAndLoadDatabaseToList( final Runnable operation ) {
            super();
            mOperation = operation;
        }

        @Override
        public void doInBackground() {
            if( mOperation != null ) {
                mOperation.run();
            }
            mCursorAdapter = FRAGMENT_TO_FILTER.get( mFragmentId )
                                               .getAllFilterCursorAdapter( FilterDao.COLUMN_NAME, OrderDirection.ASC );
        }

        @Override
        public void cancel() {
            if( !mCursorAdapter.isClosed() ) {
                if( mListViewAdapter != null ) {
                    mListViewAdapter.swapCursorAdapter( null );
                }
                mCursorAdapter.close();
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

        private void createList() {
            mListViewAdapter = new FilterListViewItemAdapter( mContext, mCursorAdapter, mFragmentId );
            mListView.setAdapter( mListViewAdapter );
            mListView.setCacheColorHint( 0 );
            mViewStyleHelper.styleAndInitQuickScroll( mQuickScroll, mListView, mListViewAdapter );
            mListView.setChoiceMode( ListView.CHOICE_MODE_MULTIPLE );
            mListView.setItemsCanFocus( false );
            mViewStyleHelper.animateFloatInFromBottom( mQuickScroll, mListView );
        }

        private void reloadList() {
            mListViewAdapter.swapCursorAdapter( mCursorAdapter );
        }
    }

}
