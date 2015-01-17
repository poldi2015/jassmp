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
import android.support.v4.app.FragmentTransaction;
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
import android.widget.Toast;

import com.andraskindler.quickscroll.Scrollable;
import com.jams.music.player.AsyncTasks.AsyncAddToQueueTask;
import com.jams.music.player.DBHelpers.DBAccessHelper;
import com.jams.music.player.Dialogs.AddToPlaylistDialog;
import com.jams.music.player.Dialogs.CautionEditArtistsDialog;
import com.jams.music.player.Dialogs.ID3sArtistEditorDialog;
import com.jams.music.player.Helpers.TypefaceHelper;
import com.jams.music.player.Helpers.UIElementsHelper;
import com.jams.music.player.ImageTransformers.PicassoCircularTransformer;
import com.jams.music.player.R;
import com.jams.music.player.Utils.Common;
import com.squareup.picasso.RequestCreator;

/**
 * Generic ListView adapter for ListViewFragment.
 *
 * @author Saravan Pantham
 */
public class SongListViewCardsAdapter extends SimpleCursorAdapter implements Scrollable {

    //
    // private members

    private Context mContext;
    private Common mApp;
    private ListViewFragment mListViewFragment;
    public static Holder mHolder = null;
    private String mName = "";

    public SongListViewCardsAdapter( Context context, ListViewFragment listViewFragment ) {
        super( context, -1, listViewFragment.getCursor(), new String[]{ }, new int[]{ }, 0 );
        mContext = context;
        mListViewFragment = listViewFragment;
        mApp = (Common) mContext.getApplicationContext();
    }

    /**
     * Quick scroll indicator implementation.
     */
    @Override
    public String getIndicatorForPosition( int childPosition, int groupPosition ) {
        Cursor c = (Cursor) getItem( childPosition );
        String title = c.getString( c.getColumnIndex( Items.TITLE.getDbColumn() ) );
        if( title != null && title.length() > 1 )
            return "  " + title.substring( 0, 1 ).toUpperCase() + "  ";
        else
            return "  N/A  ";
    }

    /**
     * Returns the current position of the top view in the list/grid.
     */
    @Override
    public int getScrollPosition( int childPosition, int groupPosition ) {
        return childPosition;
    }

    /**
     * Returns the individual row/child in the list/grid.
     */
    @Override
    public View getView( int position, View convertView, ViewGroup parent ) {
        Cursor c = (Cursor) getItem( position );

        if( convertView == null ) {
            convertView = LayoutInflater.from( mContext )
                    .inflate( R.layout.songlist_view_item, parent, false );

            mHolder = new Holder( convertView, mContext );
            mHolder.setCoverImage(
                    (ImageView) convertView.findViewById( R.id.songListViewCoverIcon ) );
            mHolder.setTitleTextView(
                    (TextView) convertView.findViewById( R.id.songListViewTitleText ) );
            mHolder.setArtistTextView(
                    (TextView) convertView.findViewById( R.id.songListViewArtistText ) );
            mHolder.setDurationTextView(
                    (TextView) convertView.findViewById( R.id.songListViewRightDurationText ) );
            mHolder.setBpmTextView(
                    (TextView) convertView.findViewById( R.id.songListViewRightBPMText ) );
            mHolder.setRatingIcon(
                    (ImageView) convertView.findViewById( R.id.songListViewRatingIcon ) );
            mHolder.setActionIcon(
                    (ImageButton) convertView.findViewById( R.id.songListViewActions ) );

            mHolder.titleTextView
                    .setTextColor( UIElementsHelper.getThemeBasedTextColor( mContext ) );
            mHolder.artistTextView.setTextColor( UIElementsHelper.getSmallTextColor( mContext ) );
            mHolder.durationTextView
                    .setTextColor( UIElementsHelper.getSmallTextColor( mContext ) );
            mHolder.coverImage.setImageResource(
                    UIElementsHelper.getEmptyCircularColorPatch( mContext ) );

            mHolder.titleTextView
                    .setTypeface( TypefaceHelper.getTypeface( mContext, "Roboto-Regular" ) );
            mHolder.artistTextView.setTypeface(
                    TypefaceHelper.getTypeface( mContext, "Roboto-Regular" ) );
            mHolder.durationTextView.setTypeface(
                    TypefaceHelper.getTypeface( mContext, "Roboto-Regular" ) );
            mHolder.bpmTextView.setTypeface(
                    TypefaceHelper.getTypeface( mContext, "Roboto-Regular" ) );

            mHolder.actionIcon.setImageResource(
                    UIElementsHelper.getIcon( mContext, "ic_action_overflow" ) );
            mHolder.actionIcon.setOnClickListener( overflowClickListener );
            mHolder.actionIcon.setFocusable( false );
            mHolder.actionIcon.setFocusableInTouchMode( false );
        } else {
            mHolder = (Holder) convertView.getTag();
        }

        //Retrieve data from the cursor.
        mHolder.setTitle( c.getString( c.getColumnIndex( Items.TITLE.getDbColumn() ) ) );
        mHolder.setArtist( c.getString( c.getColumnIndex( Items.ARTIST.getDbColumn() ) ) );
        mHolder.setSource( c.getString( c.getColumnIndex( Items.SOURCE.getDbColumn() ) ) );
        mHolder.setFilePath( c.getString( c.getColumnIndex( Items.FILE_PATH.getDbColumn() ) ) );
        mHolder.setCoverPath(
                c.getString( c.getColumnIndex( Items.COVER_PATH.getDbColumn() ) ) );
        mHolder.setDuration( c.getString( c.getColumnIndex( Items.DURATION.getDbColumn() ) ) );
        mHolder.setRating( c.getInt( c.getColumnIndex( Items.RATING.getDbColumn() ) ) );
        mHolder.setBpm( c.getInt( c.getColumnIndex( Items.BPM.getDbColumn() ) ) );

        //Load the album art.
        mApp.getPicasso().load( mHolder.coverPath )
                .transform( new PicassoCircularTransformer() )
                .placeholder( UIElementsHelper.getEmptyCircularColorPatch( mContext ) )
                .resizeDimen( R.dimen.song_list_view_album_icon_width,
                        R.dimen.song_list_view_icon_height )
                .into( mHolder.coverImage );

        return convertView;
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
                case R.id.edit_artist_tags:
                    //Edit Artist Tags.
                    if( mApp.getSharedPreferences()
                            .getBoolean( "SHOW_ARTIST_EDIT_CAUTION", true ) ) {
                        FragmentTransaction transaction =
                                mListViewFragment.getFragmentManager().beginTransaction();
                        Bundle bundle = new Bundle();
                        bundle.putString( "EDIT_TYPE", "ARTIST" );
                        bundle.putString( "ARTIST", mName );
                        CautionEditArtistsDialog dialog = new CautionEditArtistsDialog();
                        dialog.setArguments( bundle );
                        dialog.show( transaction, "cautionArtistsDialog" );
                    } else {
                        FragmentTransaction ft =
                                mListViewFragment.getFragmentManager().beginTransaction();
                        Bundle bundle = new Bundle();
                        bundle.putString( "EDIT_TYPE", "ARTIST" );
                        bundle.putString( "ARTIST", mName );
                        ID3sArtistEditorDialog dialog = new ID3sArtistEditorDialog();
                        dialog.setArguments( bundle );
                        dialog.show( ft, "id3ArtistEditorDialog" );
                    }
                    break;
                case R.id.add_to_queue:
                    //Add to Queue.
                    AsyncAddToQueueTask task = new AsyncAddToQueueTask( mContext,
                            mListViewFragment,
                            "ARTIST",
                            mName,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null );
                    task.execute();
                    break;
                case R.id.play_next:
                    AsyncAddToQueueTask playNextTask = new AsyncAddToQueueTask( mContext,
                            mListViewFragment,
                            "ARTIST",
                            mName,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null );
                    playNextTask.execute( true );
                    break;
                case R.id.add_to_playlist:
                    //Add to Playlist
                    FragmentTransaction ft =
                            mListViewFragment.getFragmentManager().beginTransaction();
                    AddToPlaylistDialog dialog = new AddToPlaylistDialog();
                    Bundle bundle = new Bundle();
                    bundle.putString( "ADD_TYPE", "ARTIST" );
                    bundle.putString( "ARTIST", mName );
                    dialog.setArguments( bundle );
                    dialog.show( ft, "AddToPlaylistDialog" );
                    break;
                case R.id.blacklist_artist:
                    //Blacklist Artist
                    mApp.getDBAccessHelper().setBlacklistForArtist( mName, true );
                    Toast.makeText( mContext, R.string.artist_blacklisted, Toast.LENGTH_SHORT )
                            .show();

                    //Update the ListView.
                    mListViewFragment.mHandler.post( mListViewFragment.queryRunnable );
                    mListViewFragment.getListViewAdapter().notifyDataSetChanged();

                    break;

            }

            return false;
        }

    };

    public enum Items {
        TITLE( DBAccessHelper.SONG_TITLE ),
        ARTIST( DBAccessHelper.SONG_ARTIST ),
        DURATION( DBAccessHelper.SONG_DURATION ),
        BPM( DBAccessHelper.SONG_BPM ),
        RATING( DBAccessHelper.SONG_RATING ),
        SOURCE( DBAccessHelper.SONG_SOURCE ),
        FILE_PATH( DBAccessHelper.SONG_FILE_PATH ),
        COVER_PATH( DBAccessHelper.SONG_ALBUM_ART_PATH );

        private final String mDbColumn;

        private Items( final String dbColumn ) {
            mDbColumn = dbColumn;
        }

        public String getDbColumn() {
            return mDbColumn;
        }
    }

    /**
     * Holder subclass for ListViewCardsAdapter.
     *
     * @author Saravan Pantham
     */
    public static class Holder {

        //
        // defines

        private final int[] RATING_MAP = { R.drawable.song_rating_0,
                R.drawable.song_rating_1,
                R.drawable.song_rating_2,
                R.drawable.song_rating_3,
                R.drawable.song_rating_4,
                R.drawable.song_rating_5 };
        //
        // public members

        public ImageView coverImage;
        public TextView titleTextView;
        public String title;
        public TextView artistTextView;
        public String artist;
        public TextView durationTextView;
        public String duration;
        public TextView bpmTextView;
        public int bpm;
        public ImageView ratingIcon;
        public int rating;
        public ImageButton actionIcon;
        public String source;
        public String filePath;
        public String coverPath;

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

        public void setDuration( String duration ) {
            this.duration = duration;
            durationTextView.setText( duration );
        }

        public void setBpm( int bpm ) {
            this.bpm = bpm;
            bpmTextView.setText( Integer.toString( bpm ) + " bpm" );
        }

        public void setRating( int rating ) {
            this.rating = rating;
            setRatingBitmap( rating );
        }

        public void setSource( String source ) {
            this.source = source;
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
                picasso = mRatingBitmaps[ id ] =
                        ( (Common) mContext.getApplicationContext() ).getPicasso()
                                .load( RATING_MAP[ id ] ).resizeDimen(
                                R.dimen.song_list_view_rating_icon_width,
                                R.dimen.song_list_view_icon_height );
                picasso.fetch();
            }
            picasso.into( ratingIcon );
        }

    }

}
