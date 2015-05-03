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
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.andraskindler.quickscroll.QuickScroll;
import com.jams.music.player.Dialogs.FilterDialog;
import com.jams.music.player.MainActivity.MainActivity;
import com.jams.music.player.R;
import com.jams.music.player.Utils.Common;
import com.jams.music.player.Utils.SynchronizedAsyncTask;

/**
 * Generic, multipurpose ListView fragment.
 *
 * @author Saravan Pantham
 */
public class FilterListViewFragment extends Fragment implements FilterDialog.FilterDialogListener {

    private Context         mContext         = null;
    private Common          mApp             = null;
    private View            mRootView        = null;
    private ViewStyleHelper mViewStyleHelper = null;

    private int    mFragmentId    = -1;
    private String mFragmentTitle = null;

    private SynchronizedAsyncTask mAsyncExecutorTask = null;

    private QuickScroll mQuickScroll = null;
    private ListView    mListView    = null;

    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState ) {

        mContext = getActivity().getApplicationContext();
        mApp = (Common) mContext;
        mAsyncExecutorTask = new SynchronizedAsyncTask();

        //Grab the fragment. This will determine which data to load into the cursor.
        mFragmentId = getArguments().getInt( Common.FRAGMENT_ID );
        mFragmentTitle = getArguments().getString( MainActivity.FRAGMENT_HEADER );

        mRootView = inflater.inflate( R.layout.fragment_list_view, container, false );
        mQuickScroll = (QuickScroll) mRootView.findViewById( R.id.quickscroll );
        mListView = (ListView) mRootView.findViewById( R.id.generalListView );
        final TextView mEmptyTextView = (TextView) mRootView.findViewById( R.id.empty_view_text );

        mViewStyleHelper = new ViewStyleHelper( mApp, mContext );
        mViewStyleHelper.styleRootView( mRootView );
        mViewStyleHelper.styleListView( mQuickScroll, mListView );
        mViewStyleHelper.styleEmptyView( mEmptyTextView );

        reloadDatabase( 400 );

        return mRootView;
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
        onItemClickListener = null;
        mListView = null;
        mListView = null;
        mContext = null;
        mViewStyleHelper = null;

        super.onDestroyView();
    }


    /**
     * Loads or reloads the database and refreshes the views if necessary.
     *
     * @param delay delay in millis
     */
    public void reloadDatabase( final int delay ) {
        if( mAsyncExecutorTask.isDisposed() ) {
            mAsyncExecutorTask = new SynchronizedAsyncTask();
        }
        mAsyncExecutorTask.execute( delay, new AsyncRunQuery() );
    }

    @Override
    public boolean onOptionsItemSelected( MenuItem item ) {
        switch( item.getItemId() ) {
            case R.id.action_filter:
                new FilterDialog( this ).show( getFragmentManager(), "FilterDialog" );
                break;
            default:
                break;

        }
        return super.onOptionsItemSelected( item );
    }

    @Override
    public void onFilterDialogClick( int which ) {
        ( (MainActivity) getActivity() ).switchContent( FilterDialog.FRAGMENT_IDS[ which ] );
    }

    /**
     * Item click listener for the ListView.
     */
    private OnItemClickListener onItemClickListener = new OnItemClickListener() {

        @Override
        public void onItemClick( AdapterView<?> arg0, View view, int index, long id ) {
        }

    };

    /**
     * Runs the correct DB query based on the passed in fragment id and
     * displays the ListView.
     */
    public class AsyncRunQuery extends SynchronizedAsyncTask.Executor {

        private FilterListViewItemAdapter mListViewAdapter = null;
        private Cursor                    mCursor          = null;

        public AsyncRunQuery() {
            super();
        }

        @Override
        public void doInBackground() {
            mCursor = mApp.getDBAccessHelper().getFragmentCursor( mContext, "", mFragmentId );
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

        private void createList() {
            mListViewAdapter = new FilterListViewItemAdapter( mContext, mCursor, mFragmentId );
            mListView.setAdapter( mListViewAdapter );
            mListView.setOnItemClickListener( onItemClickListener );
            mListView.setCacheColorHint( 0 );
            mViewStyleHelper.styleAndInitQuickScroll( mQuickScroll, mListView, mListViewAdapter );
            mListView.setChoiceMode( ListView.CHOICE_MODE_MULTIPLE );
            mListView.setItemsCanFocus( false );
            mViewStyleHelper.animateFloatInFromBottom( mQuickScroll, mListView );
        }

        private void reloadList() {
            mListViewAdapter.swapCursor( mCursor );
            mViewStyleHelper.animateFloatInFromBottom( mQuickScroll, mListView );
        }
    }

}
