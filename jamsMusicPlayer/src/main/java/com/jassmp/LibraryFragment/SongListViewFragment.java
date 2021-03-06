package com.jassmp.LibraryFragment;

import android.content.Context;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.widget.ListView;
import android.widget.TextView;

import com.andraskindler.quickscroll.QuickScroll;
import com.jassmp.Dao.Column;
import com.jassmp.Dao.SongCursorAdapter;
import com.jassmp.Dao.SongDao;
import com.jassmp.Dialogs.OrderDialog;
import com.jassmp.GuiHelper.TypefaceHelper;
import com.jassmp.GuiHelper.UIElementsHelper;
import com.jassmp.Helpers.PauseOnScrollHelper;
import com.jassmp.JassMpDb.OrderDirection;
import com.jassmp.JassMpDb.SongTableAccessor;
import com.jassmp.MainActivity.MainActivity;
import com.jassmp.Preferences.Preferences;
import com.jassmp.R;
import com.jassmp.Utils.Common;
import com.jassmp.Utils.SynchronizedAsyncTask;

public class SongListViewFragment extends Fragment implements OrderDialog.OrderDialogListener {

    private static final SparseArray<Column> ITEM_TO_ORDER_BY;

    static {
        ITEM_TO_ORDER_BY = new SparseArray<Column>();
        ITEM_TO_ORDER_BY.put( R.id.song_list_sort_name, SongDao.COLUMN_SONG_TITLE );
        ITEM_TO_ORDER_BY.put( R.id.song_list_sort_rating, SongDao.COLUMN_SONG_RATING );
        ITEM_TO_ORDER_BY.put( R.id.song_list_sort_bpm, SongDao.COLUMN_SONG_BPM );
        ITEM_TO_ORDER_BY.put( R.id.song_list_sort_artist, SongDao.COLUMN_SONG_ARTIST );
        ITEM_TO_ORDER_BY.put( R.id.song_list_sort_duration, SongDao.COLUMN_SONG_DURATION );
    }

    private Context mContext  = null;
    private Common  mApp      = null;
    private View    mRootView = null;

    private SynchronizedAsyncTask mAsyncExecutorTask = null;

    private QuickScroll mQuickScroll = null;
    private ListView    mListView    = null;

    private Preferences mPreferences = null;


    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState ) {
        setHasOptionsMenu( true );
        mRootView = inflater.inflate( R.layout.fragment_list_view, container, false );
        mContext = getActivity().getApplicationContext();
        mApp = (Common) mContext;
        mPreferences = new Preferences( mContext );
        mAsyncExecutorTask = new SynchronizedAsyncTask();

        //Set the background. We're using getGridViewBackground() since the list doesn't have
        // card items.
        mRootView.setBackgroundColor( UIElementsHelper.getGridViewBackground( mContext ) );


        // Init list view
        mQuickScroll = (QuickScroll) mRootView.findViewById( R.id.quickscroll );
        mListView = (ListView) mRootView.findViewById( R.id.generalListView );
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

        reloadDatabase( 100, null, null );

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
    public void reloadDatabase( final int delay, final Column orderBy, final OrderDirection orderDirection ) {
        if( mAsyncExecutorTask.isDisposed() ) {
            mAsyncExecutorTask = new SynchronizedAsyncTask();
        }
        mAsyncExecutorTask.execute( delay, new AsyncRunQuery( orderBy, orderDirection ) );
    }

    @Override
    public void onCreateOptionsMenu( final Menu menu, final MenuInflater inflater ) {
        inflater.inflate( R.menu.song_list_view_fragment_menu, menu );
        super.onCreateOptionsMenu( menu, inflater );
    }

    @Override
    public boolean onOptionsItemSelected( MenuItem item ) {
        switch( item.getItemId() ) {
            case R.id.action_order:
                final Column orderBy = getOrderByFromPreferences();
                new OrderDialog( this, 0 ).show( getFragmentManager(), "OrderDialog" );
                break;
        }
        return super.onOptionsItemSelected( item );
    }

    @Override
    public void onOrderDialogClick( final int orderItem, final OrderDirection orderDirection ) {
        final Column orderColumn = ITEM_TO_ORDER_BY.valueAt( orderItem );
        reloadDatabase( 0, orderColumn, orderDirection );
    }


    /**
     * @return current sort order from the preferences
     */
    private Column getOrderByFromPreferences() {
        final int index = mPreferences.getOrderByColumnIndex();
        if( index < 0 || index >= ITEM_TO_ORDER_BY.size() ) {
            return SongDao.COLUMN_SONG_TITLE;
        }
        return ITEM_TO_ORDER_BY.valueAt( index );
    }

    /**
     * Save sort order to preferences
     *
     * @param orderBy the new sort order
     */
    private void setOrderByInPreferences( final Column orderBy ) {
        final int index = ITEM_TO_ORDER_BY.indexOfValue( orderBy );
        mPreferences.setOrderByColumnIndex( index );
    }

    /**
     * Get the currently set orderDirection from the preferences.
     *
     * @param orderBy the order column or null to take the current from the preferences
     * @return the direction
     */
    private OrderDirection getOrderDirectionFromPreferences( Column orderBy ) {
        if( orderBy == null ) {
            orderBy = getOrderByFromPreferences();
        }

        return orderBy != null ? mPreferences.getSongListSortDirection( orderBy ) : OrderDirection.ASC;
    }

    private void setOrderDirectionInPreferences( final Column orderBy, final OrderDirection orderDirection ) {
        mPreferences.setSongListSortDirection( orderBy, orderDirection );
    }

    /**
     * Runs the correct DB query based on the passed in fragment id and
     * displays the ListView.
     */
    public class AsyncRunQuery extends SynchronizedAsyncTask.Executor {

        //
        // private members

        private SongListViewItemAdapter mListViewAdapter = null;
        private SongCursorAdapter mCursorAdapter = null;

        private Column         mOrderBy        = SongDao.COLUMN_SONG_TITLE;
        private OrderDirection mOrderDirection = OrderDirection.ASC;

        public AsyncRunQuery( Column orderBy, OrderDirection orderDirection ) {
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
        }

        @Override
        public void doInBackground() {
            mCursorAdapter = SongTableAccessor.getInstance( mApp )
                                              .getFilteredSongsCursorAdapter( mOrderBy, mOrderDirection );
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

        private Animation createSlideInAnimation() {
            TranslateAnimation animation = new TranslateAnimation( Animation.RELATIVE_TO_SELF, 0.0f,
                                                                   Animation.RELATIVE_TO_SELF, 0.0f,
                                                                   Animation.RELATIVE_TO_SELF, 2.0f,
                                                                   Animation.RELATIVE_TO_SELF, 0.0f );

            animation.setDuration( 400 );
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
            mListViewAdapter = new SongListViewItemAdapter( (MainActivity) getActivity(), mCursorAdapter );
            mListView.setAdapter( mListViewAdapter );

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


            mListView.startAnimation( createSlideInAnimation() );
        }

        private void reloadList() {
            mListViewAdapter.swapCursorAdapter( mCursorAdapter );
            mListView.startAnimation( createSlideInAnimation() );
        }

    }

}
