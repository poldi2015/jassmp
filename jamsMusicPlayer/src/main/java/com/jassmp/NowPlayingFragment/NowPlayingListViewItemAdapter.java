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
package com.jassmp.NowPlayingFragment;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.jassmp.Dao.SongDao;
import com.jassmp.GuiHelper.TypefaceHelper;
import com.jassmp.GuiHelper.UIElementsHelper;
import com.jassmp.ImageTransformers.PicassoCircularTransformer;
import com.jassmp.JassMpDb.SongTableAccessor;
import com.jassmp.R;
import com.jassmp.Utils.Common;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class NowPlayingListViewItemAdapter extends ArrayAdapter<String> {

    //
    // private members

    public static Holder mHolder = null;

    private final Context           mContext;
    private final SongTableAccessor mSongTableAccessor;
    private int mCurrentIndex = -1;

    public NowPlayingListViewItemAdapter( final Context context, final List<String> queuedKeys,
                                          final int currentIndex ) {
        super( context, R.layout.now_playing_list_item, queuedKeys );
        mContext = context;
        mSongTableAccessor = SongTableAccessor.getInstance( context );
        mCurrentIndex = currentIndex;
    }

    @Override
    public synchronized View getView( int position, View convertView, ViewGroup parent ) {
        if( convertView == null ) {
            convertView = LayoutInflater.from( mContext ).inflate( R.layout.now_playing_list_item, parent, false );
            mHolder = new Holder( convertView, mContext );
            mHolder.setCoverImageView( (ImageView) convertView.findViewById( R.id.now_playing_cover ) );
            mHolder.setTitleTextView( (TextView) convertView.findViewById( R.id.now_playing_title ) );
            mHolder.setArtistTextView( (TextView) convertView.findViewById( R.id.now_playing_artist ) );
            mHolder.setDurationTextView( (TextView) convertView.findViewById( R.id.now_playing_duration ) );
            mHolder.setBpmTextView( (TextView) convertView.findViewById( R.id.now_playing_bpm ) );
            mHolder.setRatingIcon( (ImageView) convertView.findViewById( R.id.now_playing_rating ) );
        } else {
            mHolder = (Holder) convertView.getTag();
        }

        if( isValid() ) {
            final String key = getItem( position );
            final SongDao songDao = key != null ? mSongTableAccessor.getSong( key ) : null;
            if( songDao != null ) {
                //Retrieve data from the cursor.
                mHolder.setTitle( songDao.getTitle() );
                mHolder.setArtist( songDao.getArtist() );
                mHolder.setCoverPath( songDao.getAlbumArtPath() );
                mHolder.setDuration( songDao.getDuration() );
                mHolder.setRating( songDao.getRating() );
                mHolder.setBpm( songDao.getBpm() );
                mHolder.setIsCurrentIndex( position == mCurrentIndex );
            } else {
                mHolder.clear();
            }
        }

        return convertView;
    }

    @Override
    public String getItem( final int position ) {
        if( position < 0 || position >= getCount() ) {
            return null;
        }
        return super.getItem( position );
    }

    public void setCurrentIndex( final int currentIndex ) {
        mCurrentIndex = currentIndex;
        notifyDataSetChanged();
    }

    public synchronized boolean isValid() {
        return getCount() > 0;
    }

    public synchronized void swapItems( final List<String> queueItems, final int currentIndex ) {
        setNotifyOnChange( false );
        clear();
        if( queueItems != null ) {
            addAll( queueItems );
        }
        setCurrentIndex( currentIndex );
    }


    public static class Holder {

        //
        // defines

        private final int[] RATING_MAP = { R.drawable.song_rating_0, R.drawable.song_rating_1, R.drawable.song_rating_2,
                                           R.drawable.song_rating_3, R.drawable.song_rating_4,
                                           R.drawable.song_rating_5 };
        //
        // public members

        private ImageView mCoverImage;
        private TextView  mTitleTextView;
        private TextView  mArtistTextView;
        private TextView  mDurationTextView;
        private TextView  mBpmTextView;
        private ImageView mRatingIcon;

        private       Context mContext;
        private final Picasso mPicasso;
        private final RequestCreator[] mRatingBitmaps = new RequestCreator[ RATING_MAP.length ];


        public Holder( final View view, final Context context ) {
            view.setTag( this );
            mContext = context;
            mPicasso = new Picasso.Builder( context ).build();
        }

        public void clear() {
            setTitle( "" );
            setArtist( "" );
            setDuration( -1 );
            setBpm( -1 );
            setRating( 0 );
            setCoverPath( null );
            setCoverPath( null );
        }

        public void setIsCurrentIndex( final boolean currentIndex ) {
            final Typeface typeface;
            if( currentIndex ) {
                typeface = UIElementsHelper.getBoldTypeface( mContext );
                mTitleTextView.setTextColor( UIElementsHelper.getHighLightTextColor( mContext ) );
                mArtistTextView.setTextColor( UIElementsHelper.getHighLightSmallTextColor( mContext ) );
                mDurationTextView.setTextColor( UIElementsHelper.getHighLightSmallTextColor( mContext ) );
                mBpmTextView.setTextColor( UIElementsHelper.getHighLightSmallTextColor( mContext ) );
            } else {
                typeface = UIElementsHelper.getRegularTypeface( mContext );
                mTitleTextView.setTextColor( UIElementsHelper.getRegularTextColor( mContext ) );
                mArtistTextView.setTextColor( UIElementsHelper.getSmallTextColor( mContext ) );
                mDurationTextView.setTextColor( UIElementsHelper.getSmallTextColor( mContext ) );
                mBpmTextView.setTextColor( UIElementsHelper.getSmallTextColor( mContext ) );
            }
            mTitleTextView.setTypeface( typeface );
            mArtistTextView.setTypeface( typeface );
            mDurationTextView.setTypeface( typeface );
            mBpmTextView.setTypeface( typeface );
        }

        public void setTitle( String title ) {
            mTitleTextView.setText( title );
        }

        public void setArtist( String artist ) {
            mArtistTextView.setText( artist );
        }

        public void setDuration( final int millis ) {
            if( millis > 0 ) {
                final String duration = String.format( "%d:%02d", TimeUnit.MILLISECONDS.toMinutes( millis ),
                                                       TimeUnit.MILLISECONDS.toSeconds( millis )
                                                       - TimeUnit.MINUTES.toSeconds(
                                                               TimeUnit.MILLISECONDS.toMinutes( millis ) ) );
                mDurationTextView.setText( duration );
            } else {
                mDurationTextView.setText( "0:00" );
            }
        }

        public void setBpm( int bpm ) {
            mBpmTextView.setText( bpm >= 0 ? Integer.toString( bpm ) + " bpm" : "" );
        }

        public void setRating( int rating ) {
            setRatingBitmap( rating );
        }

        public void setCoverPath( String coverPath ) {
            loadCoverImage( coverPath );
        }

        public void setCoverImageView( ImageView coverImage ) {
            this.mCoverImage = coverImage;
            coverImage.setImageResource( UIElementsHelper.getEmptyCircularColorPatch( mContext ) );
            coverImage.setTag( this );
        }

        public void setTitleTextView( TextView titleTextView ) {
            this.mTitleTextView = titleTextView;
            titleTextView.setTextColor( UIElementsHelper.getRegularTextColor( mContext ) );
            titleTextView.setTypeface( TypefaceHelper.getTypeface( mContext, "Roboto-Regular" ) );
            titleTextView.setTag( this );
        }

        public void setArtistTextView( TextView artistTextView ) {
            this.mArtistTextView = artistTextView;
            artistTextView.setTextColor( UIElementsHelper.getSmallTextColor( mContext ) );
            artistTextView.setTypeface( TypefaceHelper.getTypeface( mContext, "Roboto-Regular" ) );
            artistTextView.setTag( this );
        }

        public void setDurationTextView( TextView durationTextView ) {
            this.mDurationTextView = durationTextView;
            durationTextView.setTextColor( UIElementsHelper.getSmallTextColor( mContext ) );
            durationTextView.setTypeface( TypefaceHelper.getTypeface( mContext, "Roboto-Regular" ) );
            durationTextView.setTag( this );
        }

        public void setBpmTextView( TextView bpmTextView ) {
            this.mBpmTextView = bpmTextView;
            bpmTextView.setTypeface( TypefaceHelper.getTypeface( mContext, "Roboto-Regular" ) );
            bpmTextView.setTag( this );
        }

        public void setRatingIcon( ImageView ratingIcon ) {
            this.mRatingIcon = ratingIcon;
            ratingIcon.setTag( this );
        }

        private void setRatingBitmap( final int id ) {
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
            picasso.into( mRatingIcon );
        }

        private void loadCoverImage( final String coverPath ) {
            if( coverPath == null ) {
                mCoverImage.setImageResource( UIElementsHelper.getEmptyCircularColorPatch( mContext ) );
                return;
            }
            mPicasso.load( coverPath )
                    .transform( new PicassoCircularTransformer() )
                    .placeholder( UIElementsHelper.getEmptyCircularColorPatch( mContext ) )
                    .resizeDimen( R.dimen.list_view_album_icon_width, R.dimen.list_view_icon_height )
                    .into( mCoverImage );
        }

    }

}
