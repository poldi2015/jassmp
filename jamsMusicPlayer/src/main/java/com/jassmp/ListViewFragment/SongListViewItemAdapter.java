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
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.TextView;

import com.andraskindler.quickscroll.Scrollable;
import com.jassmp.AsyncTasks.AsyncAddToQueueTask;
import com.jassmp.Dao.SongCursorAdapter;
import com.jassmp.Dao.SongDao;
import com.jassmp.Helpers.TypefaceHelper;
import com.jassmp.Helpers.UIElementsHelper;
import com.jassmp.ImageTransformers.PicassoCircularTransformer;
import com.jassmp.R;
import com.jassmp.Utils.Common;
import com.squareup.picasso.RequestCreator;

/**
 * Shows the Library
 */
public class SongListViewItemAdapter extends SimpleCursorAdapter implements Scrollable {

    //
    // defines

    public static final String INVALID_POSITION = " N/A ";

    //
    // private members

    private final Context              mContext;
    private       SongCursorAdapter    mCursorAdapter;
    private final Common               mApp;
    private       SongListViewFragment mListViewFragment;
    public static Holder mHolder = null;
    private       String mName   = "";

    public SongListViewItemAdapter( final Context context, final SongCursorAdapter cursorAdapter, final SongListViewFragment listViewFragment ) {
        super( context, -1, cursorAdapter.getCursor(), new String[]{ }, new int[]{ }, 0 );
        mCursorAdapter = cursorAdapter;
        mContext = context;
        mListViewFragment = listViewFragment;
        mApp = (Common) mContext.getApplicationContext();
    }

    @Override
    public synchronized View getView( int position, View convertView, ViewGroup parent ) {
        if( convertView == null ) {
            convertView = LayoutInflater.from( mContext ).inflate( R.layout.songlist_view_item, parent, false );

            mHolder = new Holder( convertView, mContext );
            mHolder.setCoverImage( (ImageView) convertView.findViewById( R.id.songListViewCoverIcon ) );
            mHolder.setTitleTextView( (TextView) convertView.findViewById( R.id.songListViewTitleText ) );
            mHolder.setArtistTextView( (TextView) convertView.findViewById( R.id.songListViewArtistText ) );
            mHolder.setDurationTextView( (TextView) convertView.findViewById( R.id.songListViewRightDurationText ) );
            mHolder.setBpmTextView( (TextView) convertView.findViewById( R.id.songListViewRightBPMText ) );
            mHolder.setRatingIcon( (ImageView) convertView.findViewById( R.id.songListViewRatingIcon ) );
            mHolder.setActionIcon( (ImageButton) convertView.findViewById( R.id.songListViewActions ) );

            mHolder.titleTextView.setTextColor( UIElementsHelper.getThemeBasedTextColor( mContext ) );
            mHolder.artistTextView.setTextColor( UIElementsHelper.getSmallTextColor( mContext ) );
            mHolder.durationTextView.setTextColor( UIElementsHelper.getSmallTextColor( mContext ) );
            mHolder.coverImage.setImageResource( UIElementsHelper.getEmptyCircularColorPatch( mContext ) );

            mHolder.titleTextView.setTypeface( TypefaceHelper.getTypeface( mContext, "Roboto-Regular" ) );
            mHolder.artistTextView.setTypeface( TypefaceHelper.getTypeface( mContext, "Roboto-Regular" ) );
            mHolder.durationTextView.setTypeface( TypefaceHelper.getTypeface( mContext, "Roboto-Regular" ) );
            mHolder.bpmTextView.setTypeface( TypefaceHelper.getTypeface( mContext, "Roboto-Regular" ) );

            mHolder.actionIcon.setImageResource( UIElementsHelper.getIcon( mContext, "ic_action_overflow" ) );
            mHolder.actionIcon.setOnClickListener( overflowClickListener );
            mHolder.actionIcon.setFocusable( false );
            mHolder.actionIcon.setFocusableInTouchMode( false );
        } else {
            mHolder = (Holder) convertView.getTag();
        }

        if( isValid() ) {
            final SongDao songDao = mCursorAdapter.getDaoFromCursor( position );
            //Retrieve data from the cursor.
            mHolder.setTitle( songDao.getTitle() );
            mHolder.setArtist( songDao.getArtist() );
            mHolder.setFilePath( songDao.getFilePath() );
            mHolder.setCoverPath( songDao.getAlbumArtPath() );
            mHolder.setDuration( songDao.getDuration() );
            mHolder.setRating( songDao.getRating() );
            mHolder.setBpm( songDao.getBpm() );
        }

        //Load the album art.
        mApp.getPicasso()
            .load( mHolder.coverPath )
            .transform( new PicassoCircularTransformer() )
            .placeholder( UIElementsHelper.getEmptyCircularColorPatch( mContext ) )
            .resizeDimen( R.dimen.list_view_album_icon_width, R.dimen.list_view_icon_height )
            .into( mHolder.coverImage );

        return convertView;
    }

    @Override
    public synchronized String getIndicatorForPosition( int childPosition, int groupPosition ) {
        if( !isValid() ) {
            return INVALID_POSITION;
        }
        final SongDao dao = mCursorAdapter.getDaoFromCursor( childPosition );
        if( dao == null ) {
            return INVALID_POSITION;
        }
        final String title = dao.getTitle();
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

    public synchronized void swapCursorAdapter( final SongCursorAdapter cursorAdapter ) {
        if( cursorAdapter == null ) {
            if( mCursorAdapter != null ) {
                mCursorAdapter.close();
            }
            mCursorAdapter = null;
            changeCursor( null );
        } else if( cursorAdapter != mCursorAdapter ) {
            if( mCursorAdapter != null ) {
                mCursorAdapter.close();
            }
            mCursorAdapter = cursorAdapter;
            changeCursor( cursorAdapter.getCursor() );
        }
    }


    /**
     * Click listener for overflow button.
     */
    private OnClickListener overflowClickListener = new OnClickListener() {

        @Override
        public void onClick( View v ) {
            PopupMenu menu = new PopupMenu( mContext, v );
            menu.inflate( R.menu.artist_overflow_menu );
            menu.setOnMenuItemClickListener( popupMenuItemClickListener );
            mName = (String) v.getTag( R.string.artist );
            menu.show();

        }

    };

    /**
     * Menu item click listener for the pop up menu.
     */
    private OnMenuItemClickListener popupMenuItemClickListener = new OnMenuItemClickListener() {

        @Override
        public boolean onMenuItemClick( MenuItem item ) {

            switch( item.getItemId() ) {
                case R.id.add_to_queue:
                    //Add to Queue.
                    AsyncAddToQueueTask task = new AsyncAddToQueueTask( mContext, mListViewFragment, "ARTIST", null,
                                                                        mName, null, null );
                    task.execute();
                    break;
                case R.id.play_next:
                    AsyncAddToQueueTask playNextTask = new AsyncAddToQueueTask( mContext, mListViewFragment, "ARTIST",
                                                                                null, mName, null, null );
                    playNextTask.execute( true );
                    break;
            }

            return false;
        }

    };


    public static class Holder {

        //
        // defines

        private final int[] RATING_MAP = { R.drawable.song_rating_0, R.drawable.song_rating_1, R.drawable.song_rating_2,
                                           R.drawable.song_rating_3, R.drawable.song_rating_4,
                                           R.drawable.song_rating_5 };
        //
        // public members

        public ImageView   coverImage;
        public TextView    titleTextView;
        public String      title;
        public TextView    artistTextView;
        public String      artist;
        public TextView    durationTextView;
        public int         duration;
        public TextView    bpmTextView;
        public int         bpm;
        public ImageView   ratingIcon;
        public int         rating;
        public ImageButton actionIcon;
        public String      source;
        public String      filePath;
        public String      coverPath;

        private Context mContext;
        private final RequestCreator[] mRatingBitmaps = new RequestCreator[ RATING_MAP.length ];


        public Holder( final View view, final Context context ) {
            view.setTag( this );
            mContext = context;
        }

        public void setTitle( String title ) {
            this.title = title;
            titleTextView.setText( title );
        }

        public void setArtist( String artist ) {
            this.artist = artist;
            artistTextView.setText( artist );
        }

        public void setDuration( int duration ) {
            this.duration = duration;
            durationTextView.setText( Integer.toString( duration ) );
        }

        public void setBpm( int bpm ) {
            this.bpm = bpm;
            bpmTextView.setText( Integer.toString( bpm ) + " bpm" );
        }

        public void setRating( int rating ) {
            this.rating = rating;
            setRatingBitmap( rating );
        }

        public void setFilePath( String filePath ) {
            this.filePath = filePath;
        }

        public void setCoverPath( String coverPath ) {
            this.coverPath = coverPath;
        }

        public void setCoverImage( ImageView coverImage ) {
            this.coverImage = coverImage;
            coverImage.setTag( this );
        }

        public void setTitleTextView( TextView titleTextView ) {
            this.titleTextView = titleTextView;
            titleTextView.setTag( this );
        }

        public void setArtistTextView( TextView artistTextView ) {
            this.artistTextView = artistTextView;
            artistTextView.setTag( this );
        }

        public void setDurationTextView( TextView durationTextView ) {
            this.durationTextView = durationTextView;
            durationTextView.setTag( this );
        }

        public void setBpmTextView( TextView bpmTextView ) {
            this.bpmTextView = bpmTextView;
            bpmTextView.setTag( this );
        }

        public void setRatingIcon( ImageView ratingIcon ) {
            this.ratingIcon = ratingIcon;
            ratingIcon.setTag( this );
        }

        public void setActionIcon( ImageButton actionIcon ) {
            this.actionIcon = actionIcon;
            actionIcon.setTag( this );
        }

        public void setRatingBitmap( final int id ) {
            assert id >= 0 && id < RATING_MAP.length;
            RequestCreator picasso = mRatingBitmaps[ id ];
            if( picasso == null ) {
                picasso = mRatingBitmaps[ id ] = ( (Common) mContext.getApplicationContext() ).getPicasso()
                                                                                              .load( RATING_MAP[ id ] )
                                                                                              .resizeDimen(
                                                                                                      R.dimen.song_list_view_rating_icon_width,
                                                                                                      R.dimen.list_view_icon_height );
                picasso.fetch();
            }
            picasso.into( ratingIcon );
        }

    }

}
