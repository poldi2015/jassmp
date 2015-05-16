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
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.andraskindler.quickscroll.Scrollable;
import com.jassmp.Helpers.UIElementsHelper;
import com.jassmp.ImageTransformers.PicassoCircularTransformer;
import com.jassmp.MainActivity.MainActivity;
import com.jassmp.R;
import com.jassmp.Utils.Common;

/**
 * Generic ListView adapter for ListViewFragment.
 *
 * @author Saravan Pantham
 */
public class FilterListViewItemAdapter extends SimpleCursorAdapter implements Scrollable {

    //
    // private members

    private final MainActivity.FragmentId mFragmentId;
    public        Holder                  mHolder;

    public FilterListViewItemAdapter( final Context context, final Cursor cursor, final MainActivity.FragmentId fragmentId ) {
        super( context, -1, cursor, new String[]{ }, new int[]{ }, 0 );
        mContext = context;
        mFragmentId = fragmentId;
    }

    /**
     * Returns the individual row/child in the list/grid.
     */
    @Override
    public View getView( int position, View convertView, ViewGroup parent ) {
        Cursor cursor = (Cursor) getItem( position );
        mHolder = Holder.getHolder( (Common) mContext, convertView, mFragmentId );
        convertView = mHolder.init( parent );
        mHolder.loadCursor( cursor );
        mHolder.getItemView().setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick( final View view ) {
                view.post( new Runnable() {
                    @Override
                    public void run() {
                        view.setSelected( !view.isSelected() );
                    }
                } );

            }
        } );

        return convertView;
    }


    /**
     * Quick scroll indicator implementation.
     */
    @Override
    public String getIndicatorForPosition( int childPosition, int groupPosition ) {
        if( mHolder == null ) {
            return "";
        }
        final String title = mHolder.getSortColumnValue( (Cursor) getItem( childPosition ) );
        if( title != null && !title.isEmpty() ) {
            return "  " + title.substring( 0, 1 ).toUpperCase() + "  ";
        } else {
            return "  N/A  ";
        }
    }

    /**
     * Returns the current position of the top view in the list/grid.
     */
    @Override
    public int getScrollPosition( int childPosition, int groupPosition ) {
        return childPosition;
    }


    private static class AlbumHolder extends Holder {

        private ImageView mCoverImage   = null;
        private TextView  mTitleText    = null;
        private TextView  mSubTitleText = null;

        public AlbumHolder( final Common app ) {
            super( app, R.layout.filter_album_view_item, DatabaseItem.ALBUM );
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
        public void loadCursor( final Cursor cursor ) {
            if( !isInitialized() ) {
                return;
            }
            loadText( mTitleText, cursor, DatabaseItem.ALBUM );
            loadText( mSubTitleText, cursor, DatabaseItem.ARTIST );

            loadCoverImage( cursor, DatabaseItem.COVER_PATH, mCoverImage );
        }
    }

    private static class ArtistHolder extends Holder {

        private ImageView mCoverImage = null;
        private TextView  mTitleText  = null;

        public ArtistHolder( final Common app ) {
            super( app, R.layout.filter_artist_view_item, DatabaseItem.ARTIST );
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
        public void loadCursor( final Cursor cursor ) {
            if( !isInitialized() ) {
                return;
            }
            loadText( mTitleText, cursor, DatabaseItem.ARTIST );
            loadCoverImage( cursor, DatabaseItem.COVER_PATH, mCoverImage );
        }
    }

    private static class GenreHolder extends Holder {

        private TextView mTitleText = null;

        public GenreHolder( final Common app ) {
            super( app, R.layout.filter_genre_view_item, DatabaseItem.GENRE );
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
        public void loadCursor( final Cursor cursor ) {
            if( !isInitialized() ) {
                return;
            }
            loadText( mTitleText, cursor, DatabaseItem.GENRE );
        }
    }

    public abstract static class Holder {

        private       ViewStyleHelper mViewStyleHelper;
        private final int             mListViewId;
        private final Common          mApp;
        private View mItemView = null;
        private DatabaseItem mSortColumnItem;

        public Holder( final Common app, final int listViewId, final DatabaseItem sortColumnItem ) {
            mApp = app;
            mListViewId = listViewId;
            mSortColumnItem = sortColumnItem;
        }

        public static Holder getHolder( final Common app, final View itemView, final MainActivity.FragmentId fragmentId ) {
            Holder holder = itemView != null ? (Holder) itemView.getTag() : null;
            if( holder == null ) {
                switch( fragmentId ) {
                    case ALBUMS:
                        holder = new AlbumHolder( app );
                        break;
                    case ARTISTS:
                        holder = new ArtistHolder( app );
                        break;
                    case GENRES:
                        holder = new GenreHolder( app );
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

        public String getSortColumnValue( final Cursor cursor ) {
            return cursor.getString( cursor.getColumnIndex( mSortColumnItem.getDbColumn() ) );
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

        public abstract void loadCursor( Cursor cursor );

        public View findViewByIdAndTag( final View itemView, final int id ) {
            final View view = itemView.findViewById( id );
            view.setTag( this );

            return view;
        }

        protected void loadText( final TextView textView, final Cursor cursor, final DatabaseItem item ) {
            textView.setText( cursor.getString( cursor.getColumnIndex( item.getDbColumn() ) ) );
        }

        protected void loadCoverImage( final Cursor cursor, final DatabaseItem coverItem, final ImageView coverImage ) {
            final String coverPath = cursor.getString( cursor.getColumnIndex( coverItem.getDbColumn() ) );
            getApp().getPicasso()
                    .load( coverPath )
                    .transform( new PicassoCircularTransformer() )
                    .placeholder( UIElementsHelper.getEmptyCircularColorPatch( getApp() ) )
                    .resizeDimen( R.dimen.list_view_album_icon_width, R.dimen.list_view_icon_height )
                    .into( coverImage );

        }
    }
}
