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
package com.jassmp.Helpers;

import android.content.ContentUris;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;

import com.jassmp.Dao.SongDao;
import com.jassmp.JassMpDb.DBAccessHelper;
import com.jassmp.Utils.Common;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.squareup.picasso.Transformation;

/**
 * Helper class for the current song.
 *
 * @author Saravan Pantham
 */
public class SongHelper {

    public static final String LOCAL = "local";

    private SongHelper mSongHelper;
    private Common     mApp;
    private boolean mIsCurrentSong    = false;
    private boolean mIsAlbumArtLoaded = false;

    //Song parameters.
    private String mTitle;
    private String mArtist;
    private String mAlbum;
    private String mDuration;
    private String mFilePath;
    private String mGenre;
    private String mId;
    private String mAlbumArtPath;
    private long   mSavedPosition;
    private Bitmap mAlbumArt;

    private AlbumArtLoadedListener mAlbumArtLoadedListener;

    /**
     * Interface that provides callbacks to the provided listener
     * once the song's album art has been loaded.
     */
    public interface AlbumArtLoadedListener {

        /**
         * Called once the album art bitmap is ready for use.
         */
        public void albumArtLoaded();
    }

    /**
     * Moves the specified cursor to the specified index and populates this
     * helper object with new song data.
     *
     * @param context             Context used to get a new Common object.
     * @param index               The index of the song.
     * @param albumArtTransformer The transformer to apply to the album art bitmap;
     */
    public void populateSongData( Context context, int index, Transformation albumArtTransformer ) {

        mSongHelper = this;
        mApp = (Common) context.getApplicationContext();

        if( mApp.isServiceRunning() ) {
            mApp.getAudioPlaybackService()
                .getCursor()
                .moveToPosition( mApp.getAudioPlaybackService().getPlaybackIndecesList().get( index ) );

            this.setId( mApp.getAudioPlaybackService().getCursor().getString( getIdColumnIndex() ) );
            this.setTitle( mApp.getAudioPlaybackService().getCursor().getString( getTitleColumnIndex() ) );
            this.setAlbum( mApp.getAudioPlaybackService().getCursor().getString( getAlbumColumnIndex() ) );
            this.setArtist( mApp.getAudioPlaybackService().getCursor().getString( getArtistColumnIndex() ) );
            this.setGenre( determineGenreName() );
            this.setDuration( determineDuration() );

            this.setFilePath( mApp.getAudioPlaybackService().getCursor().getString( getFilePathColumnIndex() ) );
            this.setAlbumArtPath( determineAlbumArtPath() );
            this.setSavedPosition( determineSavedPosition() );

            mApp.getPicasso().load( getAlbumArtPath() ).transform( albumArtTransformer ).into( imageLoadingTarget );

        }

    }

    /**
     * Moves the specified cursor to the specified index and populates this
     * helper object with new song data.
     *
     * @param context Context used to get a new Common object.
     * @param index   The index of the song.
     */
    public void populateSongData( Context context, int index ) {

        mSongHelper = this;
        mApp = (Common) context.getApplicationContext();

        if( mApp.isServiceRunning() ) {
            mApp.getAudioPlaybackService()
                .getCursor()
                .moveToPosition( mApp.getAudioPlaybackService().getPlaybackIndecesList().get( index ) );

            this.setId( mApp.getAudioPlaybackService().getCursor().getString( getIdColumnIndex() ) );
            this.setTitle( mApp.getAudioPlaybackService().getCursor().getString( getTitleColumnIndex() ) );
            this.setAlbum( mApp.getAudioPlaybackService().getCursor().getString( getAlbumColumnIndex() ) );
            this.setArtist( mApp.getAudioPlaybackService().getCursor().getString( getArtistColumnIndex() ) );
            this.setGenre( determineGenreName() );
            this.setDuration( determineDuration() );

            this.setFilePath( mApp.getAudioPlaybackService().getCursor().getString( getFilePathColumnIndex() ) );
            this.setAlbumArtPath( determineAlbumArtPath() );
            this.setSavedPosition( determineSavedPosition() );

            mApp.getPicasso().load( getAlbumArtPath() ).into( imageLoadingTarget );

        }

    }

    /**
     * Moves the specified cursor to the specified index and populates this
     * helper object with new song data. Note that this method only laods
     * the song's title and artist. All other fields are set to null. To
     * retrieve all song data, see populateSongData().
     *
     * @param context Context used to get a new Common object.
     * @param index   The index of the song.
     */
    public void populateBasicSongData( Context context, int index ) {

        mSongHelper = this;
        mApp = (Common) context.getApplicationContext();

        if( mApp.isServiceRunning() ) {
            mApp.getAudioPlaybackService()
                .getCursor()
                .moveToPosition( mApp.getAudioPlaybackService().getPlaybackIndecesList().get( index ) );

            this.setId( mApp.getAudioPlaybackService().getCursor().getString( getIdColumnIndex() ) );
            this.setTitle( mApp.getAudioPlaybackService().getCursor().getString( getTitleColumnIndex() ) );
            this.setAlbum( mApp.getAudioPlaybackService().getCursor().getString( getAlbumColumnIndex() ) );
            this.setArtist( mApp.getAudioPlaybackService().getCursor().getString( getArtistColumnIndex() ) );
            this.setGenre( determineGenreName() );
            this.setDuration( determineDuration() );

            this.setFilePath( mApp.getAudioPlaybackService().getCursor().getString( getFilePathColumnIndex() ) );
            this.setAlbumArtPath( determineAlbumArtPath() );
            this.setSavedPosition( determineSavedPosition() );

        }

    }

    /**
     * Sets this helper object as the current song. This method
     * will check if the song's album art has already been loaded.
     * If so, the updateNotification() and updateWidget() methods
     * will be called. If not, they'll be called as soon as the
     * album art is loaded.
     */
    public void setIsCurrentSong() {
        mIsCurrentSong = true;
        //The album art has already been loaded.
        if( mIsAlbumArtLoaded ) {
            mApp.getAudioPlaybackService().updateNotification( this );
            mApp.getAudioPlaybackService().updateWidgets();
        }
            /*
            * else {
             * The album art isn't ready yet. The listener will call
			 * the updateNotification() and updateWidgets() methods.
			 * }
			 */

    }

    /**
     * Image loading listener to store the current song's album art.
     */
    Target imageLoadingTarget = new Target() {

        @Override
        public void onBitmapLoaded( Bitmap bitmap, Picasso.LoadedFrom from ) {
            mIsAlbumArtLoaded = true;
            setAlbumArt( bitmap );
            if( getAlbumArtLoadedListener() != null ) {
                getAlbumArtLoadedListener().albumArtLoaded();
            }

            if( mIsCurrentSong ) {
                mApp.getAudioPlaybackService().updateNotification( mSongHelper );
                mApp.getAudioPlaybackService().updateWidgets();

            }

        }

        @Override
        public void onBitmapFailed( Drawable errorDrawable ) {
            setAlbumArt( null );
            onBitmapLoaded( mAlbumArt, null );

        }

        @Override
        public void onPrepareLoad( Drawable placeHolderDrawable ) {
            mIsAlbumArtLoaded = false;

        }

    };

    private int getIdColumnIndex() {
        return mApp.getAudioPlaybackService().getCursor().getColumnIndex( SongDao.COLUMN_ID.name );
    }

    private int getFilePathColumnIndex() {
        if( mApp.getAudioPlaybackService().getCursor().getColumnIndex( MediaStore.Audio.Media.IS_MUSIC ) == -1 ) {
            //We're dealing with Jams' internal DB schema.
            return mApp.getAudioPlaybackService().getCursor().getColumnIndex( DBAccessHelper.SONG_FILE_PATH );
        } else {
            String isMusicColName = MediaStore.Audio.Media.IS_MUSIC;
            int isMusicColumnIndex = mApp.getAudioPlaybackService().getCursor().getColumnIndex( isMusicColName );

            //Check if the current row is from Jams' internal DB schema or MediaStore.
            if( mApp.getAudioPlaybackService().getCursor().getString( isMusicColumnIndex ).isEmpty() )
            //We're dealing with Jams' internal DB schema.
            {
                return mApp.getAudioPlaybackService().getCursor().getColumnIndex( DBAccessHelper.SONG_FILE_PATH );
            } else
            //The current row is from MediaStore's DB schema.
            {
                return mApp.getAudioPlaybackService().getCursor().getColumnIndex( MediaStore.Audio.Media.DATA );
            }

        }

    }

    private int getTitleColumnIndex() {
        if( mApp.getAudioPlaybackService().getCursor().getColumnIndex( MediaStore.Audio.Media.IS_MUSIC ) == -1 ) {
            //We're dealing with Jams' internal DB schema.
            return mApp.getAudioPlaybackService().getCursor().getColumnIndex( DBAccessHelper.SONG_TITLE );
        } else {
            String isMusicColName = MediaStore.Audio.Media.IS_MUSIC;
            int isMusicColumnIndex = mApp.getAudioPlaybackService().getCursor().getColumnIndex( isMusicColName );

            //Check if the current row is from Jams' internal DB schema or MediaStore.
            if( mApp.getAudioPlaybackService().getCursor().getString( isMusicColumnIndex ).isEmpty() )
            //We're dealing with Jams' internal DB schema.
            {
                return mApp.getAudioPlaybackService().getCursor().getColumnIndex( DBAccessHelper.SONG_TITLE );
            } else
            //The current row is from MediaStore's DB schema.
            {
                return mApp.getAudioPlaybackService().getCursor().getColumnIndex( MediaStore.Audio.Media.TITLE );
            }
        }

    }

    private int getArtistColumnIndex() {
        if( mApp.getAudioPlaybackService().getCursor().getColumnIndex( MediaStore.Audio.Media.IS_MUSIC ) == -1 ) {
            //We're dealing with Jams' internal DB schema.
            return mApp.getAudioPlaybackService().getCursor().getColumnIndex( DBAccessHelper.SONG_ARTIST );
        } else {
            String isMusicColName = MediaStore.Audio.Media.IS_MUSIC;
            int isMusicColumnIndex = mApp.getAudioPlaybackService().getCursor().getColumnIndex( isMusicColName );

            //Check if the current row is from Jams' internal DB schema or MediaStore.
            if( mApp.getAudioPlaybackService().getCursor().getString( isMusicColumnIndex ).isEmpty() )
            //We're dealing with Jams' internal DB schema.
            {
                return mApp.getAudioPlaybackService().getCursor().getColumnIndex( DBAccessHelper.SONG_ARTIST );
            } else
            //The current row is from MediaStore's DB schema.
            {
                return mApp.getAudioPlaybackService().getCursor().getColumnIndex( MediaStore.Audio.Media.ARTIST );
            }

        }

    }

    private int getAlbumColumnIndex() {
        if( mApp.getAudioPlaybackService().getCursor().getColumnIndex( MediaStore.Audio.Media.IS_MUSIC ) == -1 ) {
            //We're dealing with Jams' internal DB schema.
            return mApp.getAudioPlaybackService().getCursor().getColumnIndex( DBAccessHelper.SONG_ALBUM );
        } else {
            String isMusicColName = MediaStore.Audio.Media.IS_MUSIC;
            int isMusicColumnIndex = mApp.getAudioPlaybackService().getCursor().getColumnIndex( isMusicColName );

            //Check if the current row is from Jams' internal DB schema or MediaStore.
            if( mApp.getAudioPlaybackService().getCursor().getString( isMusicColumnIndex ).isEmpty() )
            //We're dealing with Jams' internal DB schema.
            {
                return mApp.getAudioPlaybackService().getCursor().getColumnIndex( DBAccessHelper.SONG_ALBUM );
            } else
            //The current row is from MediaStore's DB schema.
            {
                return mApp.getAudioPlaybackService().getCursor().getColumnIndex( MediaStore.Audio.Media.ALBUM );
            }

        }

    }

    private String determineGenreName() {
        if( mApp.getAudioPlaybackService().getCursor().getColumnIndex( MediaStore.Audio.Media.IS_MUSIC ) == -1 ) {
            //We're dealing with Jams' internal DB schema.
            int colIndex = mApp.getAudioPlaybackService().getCursor().getColumnIndex( DBAccessHelper.SONG_GENRE );
            return mApp.getAudioPlaybackService().getCursor().getString( colIndex );
        } else {
            String isMusicColName = MediaStore.Audio.Media.IS_MUSIC;
            int isMusicColumnIndex = mApp.getAudioPlaybackService().getCursor().getColumnIndex( isMusicColName );

            //Check if the current row is from Jams' internal DB schema or MediaStore.
            if( mApp.getAudioPlaybackService().getCursor().getString( isMusicColumnIndex ).isEmpty() ) {
                //We're dealing with Jams' internal DB schema.
                int colIndex = mApp.getAudioPlaybackService().getCursor().getColumnIndex( DBAccessHelper.SONG_GENRE );
                return mApp.getAudioPlaybackService().getCursor().getString( colIndex );

            } else {
                //The current row is from MediaStore's DB schema.
                return ""; //We're not using the genres field for now, so we'll leave it blank.

            }

        }

    }

    private String determineAlbumArtPath() {
        if( mApp.getAudioPlaybackService().getCursor().getColumnIndex( MediaStore.Audio.Media.IS_MUSIC ) == -1 ) {
            //We're dealing with Jams' internal DB schema.
            int colIndex = mApp.getAudioPlaybackService()
                               .getCursor()
                               .getColumnIndex( DBAccessHelper.SONG_ALBUM_ART_PATH );
            return mApp.getAudioPlaybackService().getCursor().getString( colIndex );
        } else {
            String isMusicColName = MediaStore.Audio.Media.IS_MUSIC;
            int isMusicColumnIndex = mApp.getAudioPlaybackService().getCursor().getColumnIndex( isMusicColName );

            //Check if the current row is from Jams' internal DB schema or MediaStore.
            if( mApp.getAudioPlaybackService().getCursor().getString( isMusicColumnIndex ).isEmpty() ) {
                //We're dealing with Jams' internal DB schema.
                int colIndex = mApp.getAudioPlaybackService()
                                   .getCursor()
                                   .getColumnIndex( DBAccessHelper.SONG_ALBUM_ART_PATH );
                return mApp.getAudioPlaybackService().getCursor().getString( colIndex );

            } else {
                //The current row is from MediaStore's DB schema.
                final Uri ART_CONTENT_URI = Uri.parse( "content://media/external/audio/albumart" );
                int albumIdColIndex = mApp.getAudioPlaybackService()
                                          .getCursor()
                                          .getColumnIndex( MediaStore.Audio.Media.ALBUM_ID );
                long albumId = mApp.getAudioPlaybackService().getCursor().getLong( albumIdColIndex );

                return ContentUris.withAppendedId( ART_CONTENT_URI, albumId ).toString();

            }

        }

    }

    private String determineDuration() {
        if( mApp.getAudioPlaybackService().getCursor().getColumnIndex( MediaStore.Audio.Media.IS_MUSIC ) == -1 ) {
            //We're dealing with Jams' internal DB schema.
            int colIndex = mApp.getAudioPlaybackService().getCursor().getColumnIndex( DBAccessHelper.SONG_DURATION );
            return mApp.getAudioPlaybackService().getCursor().getString( colIndex );
        } else {
            String isMusicColName = MediaStore.Audio.Media.IS_MUSIC;
            int isMusicColumnIndex = mApp.getAudioPlaybackService().getCursor().getColumnIndex( isMusicColName );

            //Check if the current row is from Jams' internal DB schema or MediaStore.
            if( mApp.getAudioPlaybackService().getCursor().getString( isMusicColumnIndex ).isEmpty() ) {
                //We're dealing with Jams' internal DB schema.
                int colIndex = mApp.getAudioPlaybackService()
                                   .getCursor()
                                   .getColumnIndex( DBAccessHelper.SONG_DURATION );
                return mApp.getAudioPlaybackService().getCursor().getString( colIndex );

            } else {
                //The current row is from MediaStore's DB schema.
                int durationColIndex = mApp.getAudioPlaybackService()
                                           .getCursor()
                                           .getColumnIndex( MediaStore.Audio.Media.DURATION );
                long duration = mApp.getAudioPlaybackService().getCursor().getLong( durationColIndex );

                return mApp.convertMillisToMinsSecs( duration );

            }

        }

    }


    private long determineSavedPosition() {
        if( mApp.getAudioPlaybackService().getCursor().getColumnIndex( MediaStore.Audio.Media.IS_MUSIC ) == -1 ) {
            //We're dealing with Jams' internal DB schema.
            int colIndex = mApp.getAudioPlaybackService().getCursor().getColumnIndex( DBAccessHelper.SAVED_POSITION );
            return mApp.getAudioPlaybackService().getCursor().getLong( colIndex );
        } else {
            String isMusicColName = MediaStore.Audio.Media.IS_MUSIC;
            int isMusicColumnIndex = mApp.getAudioPlaybackService().getCursor().getColumnIndex( isMusicColName );

            //Check if the current row is from Jams' internal DB schema or MediaStore.
            if( mApp.getAudioPlaybackService().getCursor().getString( isMusicColumnIndex ).isEmpty() ) {
                //We're dealing with Jams' internal DB schema.
                int colIndex = mApp.getAudioPlaybackService()
                                   .getCursor()
                                   .getColumnIndex( DBAccessHelper.SAVED_POSITION );
                return mApp.getAudioPlaybackService().getCursor().getLong( colIndex );

            } else {
                //The current row is from MediaStore's DB schema.
                return -1;

            }

        }

    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle( String title ) {
        mTitle = title;
    }

    public String getArtist() {
        return mArtist;
    }

    public void setArtist( String artist ) {
        mArtist = artist;
    }

    public String getAlbum() {
        return mAlbum;
    }

    public void setAlbum( String album ) {
        mAlbum = album;
    }

    public String getDuration() {
        return mDuration;
    }

    public void setDuration( String duration ) {
        mDuration = duration;
    }

    public String getFilePath() {
        return mFilePath;
    }

    public Bitmap getAlbumArt() {
        return mAlbumArt;
    }

    public void setFilePath( String filePath ) {
        mFilePath = filePath;
    }

    public String getGenre() {
        return mGenre;
    }

    public void setGenre( String genre ) {
        mGenre = genre;
    }

    public String getId() {
        return mId;
    }

    public void setId( String id ) {
        mId = id;
    }

    public String getAlbumArtPath() {
        return mAlbumArtPath;
    }

    public void setAlbumArtPath( String albumArtPath ) {
        mAlbumArtPath = albumArtPath;
    }

    public String getSource() {
        return LOCAL;
    }

    public void setAlbumArt( Bitmap albumArt ) {
        mAlbumArt = albumArt;
    }

    public void setSavedPosition( long savedPosition ) {
        mSavedPosition = savedPosition;
    }

    public long getSavedPosition() {
        return mSavedPosition;
    }

    public void setAlbumArtLoadedListener( AlbumArtLoadedListener listener ) {
        mAlbumArtLoadedListener = listener;
    }

    public AlbumArtLoadedListener getAlbumArtLoadedListener() {
        return mAlbumArtLoadedListener;
    }

}