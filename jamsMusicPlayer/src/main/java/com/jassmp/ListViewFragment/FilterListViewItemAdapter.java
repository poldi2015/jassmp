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
package com.jassmp.ListViewFragment;

import android.content.Context;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.andraskindler.quickscroll.Scrollable;
import com.jassmp.Dao.FilterCursorAdapter;
import com.jassmp.Dao.FilterDao;
import com.jassmp.Helpers.UIElementsHelper;
import com.jassmp.ImageTransformers.PicassoCircularTransformer;
import com.jassmp.MainActivity.MainActivity;
import com.jassmp.R;
import com.jassmp.Utils.Common;

public class FilterListViewItemAdapter extends SimpleCursorAdapter implements Scrollable {

    //
    // defines

    public static final String INVALID_POSITION = " N/A ";

    //
    // private members

    private       FilterCursorAdapter     mCursorAdapter;
    private final MainActivity.FragmentId mFragmentId;
    public        Holder                  mHolder;
    private int mNumberOfEntries = 0;

    public FilterListViewItemAdapter( final Context context, final FilterCursorAdapter cursorAdapter, final MainActivity.FragmentId fragmentId ) {
        super( context, -1, cursorAdapter.getCursor(), new String[]{ }, new int[]{ }, 0 );
        mCursorAdapter = cursorAdapter;
        mNumberOfEntries = cursorAdapter.getCount();
        mContext = context;
        mFragmentId = fragmentId;
    }

    @Override
    public synchronized View getView( final int position, View convertView, ViewGroup parent ) {
        mCursorAdapter.setPosition( position );
        mHolder = Holder.getHolder( (Common) mContext, convertView, mFragmentId, mNumberOfEntries );
        convertView = mHolder.init( parent );
        mHolder.loadCursor( mCursorAdapter );
        mHolder.getItemView().setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick( final View view ) {
                view.post( new Runnable() {
                    @Override
                    public void run() {
                        toggleSelectedState( position );
                    }
                } );
            }
        } );

        return convertView;
    }

    private void toggleSelectedState( final int position ) {
        final FilterDao filterDao = mCursorAdapter.getDaoFromCursor( position );
        final boolean currentSelection = filterDao.isSelected();
        final int numberOfSelectedEntries = mCursorAdapter.getNumberOfSelectedEntries();
        if( numberOfSelectedEntries == mNumberOfEntries ) {
            // All entries are currently selected, view shows no selection => switch to only current selected
            mCursorAdapter.setSelectedAll( false );
            mCursorAdapter.setSelected( true );
        } else if( numberOfSelectedEntries == 1 && currentSelection ) {
            // last selected item get deselected => select all
            mCursorAdapter.setSelectedAll( true );
        } else {
            mCursorAdapter.setSelected( !currentSelection );
        }
        notifyDataSetChanged();
    }


    @Override
    public synchronized String getIndicatorForPosition( int childPosition, int groupPosition ) {
        if( !isValid() ) {
            return INVALID_POSITION;
        }
        final FilterDao dao = mCursorAdapter.getDaoFromCursor( childPosition );
        if( dao == null ) {
            return INVALID_POSITION;
        }
        final String title = dao.getName();
        if( title != null && title.length() > 1 ) {
            return "  " + title.substring( 0, 1 ).toUpperCase() + "  ";
        } else {
            return INVALID_POSITION;
        }
    }

    /**
     * Returns the current position of the top view in the list/grid.
     */
    @Override
    public int getScrollPosition( int childPosition, int groupPosition ) {
        return childPosition;
    }

    public synchronized boolean isValid() {
        return mCursorAdapter != null && mCursorAdapter.isValidPosition();
    }

    public synchronized void swapCursorAdapter( final FilterCursorAdapter cursorAdapter ) {
        if( cursorAdapter == null ) {
            mNumberOfEntries = 0;
            if( mCursorAdapter != null ) {
                mCursorAdapter.close();
            }
            mCursorAdapter = null;
            changeCursor( null );
        } else if( cursorAdapter != mCursorAdapter ) {
            mNumberOfEntries = cursorAdapter.getCount();
            if( mCursorAdapter != null ) {
                mCursorAdapter.close();
            }
            mCursorAdapter = cursorAdapter;
            changeCursor( cursorAdapter.getCursor() );
        }
    }

    private static class AlbumHolder extends Holder {

        private ImageView mCoverImage   = null;
        private TextView  mTitleText    = null;
        private TextView  mSubTitleText = null;

        public AlbumHolder( final Common app, final int numberOfListItems ) {
            super( app, R.layout.filter_album_view_item, numberOfListItems );
        }

        @Override
        public View init( final ViewGroup listView ) {
            if( isInitialized() ) {
                return getItemView();
            }
            final View itemView = super.init( listView );

            mCoverImage = (ImageView) findViewByIdAndTag( itemView, R.id.listViewCoverIcon );
            getViewStyleHelper().styleCoverImage( mCoverImage );
            mTitleText = (TextView) findViewByIdAndTag( itemView, R.id.listViewTitleText );
            getViewStyleHelper().styleTileText( mTitleText );
            mSubTitleText = (TextView) findViewByIdAndTag( itemView, R.id.listViewSubTitleText );
            getViewStyleHelper().styleSubTitleText( mSubTitleText );

            return itemView;
        }

        @Override
        public void loadCursor( final FilterCursorAdapter cursorAdapter ) {
            if( !isInitialized() ) {
                return;
            }
            final FilterDao dao = cursorAdapter.getDaoFromCursor();
            loadText( mTitleText, dao.getName() );
            loadText( mSubTitleText, dao.getArtist() );

            loadCoverImage( dao.getArtPath(), mCoverImage );
            super.loadCursor( cursorAdapter );
        }
    }

    private static class ArtistHolder extends Holder {

        private ImageView mCoverImage = null;
        private TextView  mTitleText  = null;

        public ArtistHolder( final Common app, final int numberOfListItems ) {
            super( app, R.layout.filter_artist_view_item, numberOfListItems );
        }

        @Override
        public View init( final ViewGroup listView ) {
            if( isInitialized() ) {
                return getItemView();
            }
            final View itemView = super.init( listView );

            mCoverImage = (ImageView) findViewByIdAndTag( itemView, R.id.listViewCoverIcon );
            getViewStyleHelper().styleCoverImage( mCoverImage );
            mTitleText = (TextView) findViewByIdAndTag( itemView, R.id.listViewTitleText );
            getViewStyleHelper().styleTileText( mTitleText );

            return itemView;
        }

        @Override
        public void loadCursor( final FilterCursorAdapter cursorAdapter ) {
            if( !isInitialized() ) {
                return;
            }

            final FilterDao dao = cursorAdapter.getDaoFromCursor();
            loadText( mTitleText, dao.getName() );
            loadCoverImage( dao.getArtPath(), mCoverImage );
            super.loadCursor( cursorAdapter );
        }
    }

    private static class GenreHolder extends Holder {

        private TextView mTitleText = null;

        public GenreHolder( final Common app, final int numberOfListItems ) {
            super( app, R.layout.filter_genre_view_item, numberOfListItems );
        }

        @Override
        public View init( final ViewGroup listView ) {
            if( isInitialized() ) {
                return getItemView();
            }
            final View itemView = super.init( listView );

            mTitleText = (TextView) findViewByIdAndTag( itemView, R.id.listViewTitleText );
            getViewStyleHelper().styleTileText( mTitleText );

            return itemView;
        }

        @Override
        public boolean isInitialized() {
            return super.isInitialized();
        }

        @Override
        public void loadCursor( final FilterCursorAdapter cursorAdapter ) {
            if( !isInitialized() ) {
                return;
            }
            loadText( mTitleText, cursorAdapter.getDaoFromCursor().getName() );
            super.loadCursor( cursorAdapter );
        }
    }

    public abstract static class Holder {

        private       ViewStyleHelper mViewStyleHelper;
        private final int             mListViewId;
        private final int             mNumberOfListItems;
        private final Common          mApp;
        private View mItemView = null;

        public Holder( final Common app, final int listViewId, final int numberOfListItems ) {
            mApp = app;
            mListViewId = listViewId;
            mNumberOfListItems = numberOfListItems;
        }

        public static Holder getHolder( final Common app, final View itemView, final MainActivity.FragmentId fragmentId, final int numberOfListItems ) {
            Holder holder = itemView != null ? (Holder) itemView.getTag() : null;
            if( holder == null ) {
                switch( fragmentId ) {
                    case ALBUMS:
                        holder = new AlbumHolder( app, numberOfListItems );
                        break;
                    case ARTISTS:
                        holder = new ArtistHolder( app, numberOfListItems );
                        break;
                    case GENRES:
                        holder = new GenreHolder( app, numberOfListItems );
                        break;
                }
            }

            return holder;
        }

        public View init( final ViewGroup listView ) {
            if( mItemView == null ) {
                mItemView = LayoutInflater.from( mApp ).inflate( mListViewId, listView, false );
                mItemView.setTag( this );
            }

            return mItemView;
        }

        public boolean isInitialized() {
            return mItemView != null;
        }

        protected Common getApp() {
            return mApp;
        }

        public View getItemView() {
            return mItemView;
        }

        public ViewStyleHelper getViewStyleHelper() {
            if( mViewStyleHelper == null ) {
                mViewStyleHelper = new ViewStyleHelper( mApp, mApp.getApplicationContext() );
            }
            return mViewStyleHelper;
        }

        public void loadCursor( FilterCursorAdapter cursorAdapter ) {
            if( !isInitialized() ) {
                return;
            }
            if( cursorAdapter.getNumberOfSelectedEntries() == mNumberOfListItems ) {
                mItemView.post( new Runnable() {
                    @Override
                    public void run() {
                        mItemView.setSelected( false );
                    }
                } );
            } else {
                final boolean selected = cursorAdapter.getDaoFromCursor().isSelected();
                mItemView.post( new Runnable() {
                    @Override
                    public void run() {
                        mItemView.setSelected( selected );
                    }
                } );
            }
        }

        public View findViewByIdAndTag( final View itemView, final int id ) {
            final View view = itemView.findViewById( id );
            view.setTag( this );

            return view;
        }

        protected void loadText( final TextView textView, final String text ) {
            textView.setText( text );
        }

        protected void loadCoverImage( final String coverPath, final ImageView coverImage ) {
            if( coverPath == null ) {
                return;
            }
            getApp().getPicasso()
                    .load( coverPath )
                    .transform( new PicassoCircularTransformer() )
                    .placeholder( UIElementsHelper.getEmptyCircularColorPatch( getApp() ) )
                    .resizeDimen( R.dimen.list_view_album_icon_width, R.dimen.list_view_icon_height )
                    .into( coverImage );

        }
    }
}
