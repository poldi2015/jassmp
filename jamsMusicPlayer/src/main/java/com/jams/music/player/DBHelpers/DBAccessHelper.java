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
package com.jams.music.player.DBHelpers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.jams.music.player.MainActivity.MainActivity;
import com.jams.music.player.Utils.Common;

/**
 * SQLite database implementation. Note that this class
 * only contains methods that access Jams' private
 * database. For methods that access Android's
 * MediaStore database, see MediaStoreAccessHelper.
 *
 * @author Saravan Pantham
 */
public class DBAccessHelper extends SQLiteOpenHelper {

    //Database instance. Will last for the lifetime of the application.
    private static DBAccessHelper sInstance;

    //Writable database instance.
    private SQLiteDatabase mDatabase;

    //Commmon utils object.
    private Common mApp;

    //Common fields.
    public static final String _ID     = "_id";
    public static final String SONG_ID = "song_id";

    //Music library table.
    public static final String MUSIC_LIBRARY_TABLE = "MusicLibraryTable";
    public static final String SONG_TITLE          = "title";
    public static final String SONG_ARTIST         = "artist";
    public static final String SONG_ALBUM          = "album";
    public static final String SONG_DURATION       = "duration";
    public static final String SAVED_POSITION      = "saved_position";
    public static final String SONG_FILE_PATH      = "file_path";
    public static final String SONG_TRACK_NUMBER   = "track_number";
    public static final String SONG_GENRE          = "genre";
    public static final String SONG_PLAY_COUNT     = "play_count";
    public static final String SONG_YEAR           = "year";
    public static final String SONG_RATING         = "rating";
    public static final String ADDED_TIMESTAMP     = "added_timestamp";
    public static final String SONG_BPM            = "bpm";
    public static final String SONG_ALBUM_ART_PATH = "album_art_path";

    public DBAccessHelper( Context context ) {
        super( context, DatabaseAccessor.DATABASE_NAME, null, DatabaseAccessor.DATABASE_VERSION );
        mApp = (Common) context.getApplicationContext();

    }

    /**
     * Returns a singleton instance for the database.
     *
     * @param context
     * @return
     */
    public static synchronized DBAccessHelper getInstance( Context context ) {
        if( sInstance == null ) {
            sInstance = new DBAccessHelper( context.getApplicationContext() );
        }

        return sInstance;
    }

    /**
     * Returns a writable instance of the database. Provides an additional
     * null check for additional stability.
     */
    private synchronized SQLiteDatabase getDatabase() {
        if( mDatabase == null ) {
            mDatabase = getWritableDatabase();
        }

        return mDatabase;
    }

    @Override
    public void onCreate( SQLiteDatabase db ) {
    }

    @Override
    public void onUpgrade( final SQLiteDatabase db, final int oldVersion, final int newVersion ) {
    }

    @Override
    protected void finalize() {
        try {
            getDatabase().close();
        } catch( Exception e ) {
            e.printStackTrace();
        }

    }

    /**
     * ********************************************************
     * MUSIC LIBRARY TABLE METHODS.
     * *********************************************************
     */

    public Cursor getFragmentCursor( Context context, String querySelection, MainActivity.FragmentId fragmentId ) {
        return getFragmentCursor( context, querySelection, fragmentId, SONG_TITLE, OrderDirection.ASC );
    }

    /**
     * Returns the cursor based on the specified fragment.
     */
    public Cursor getFragmentCursor( Context context, String querySelection, MainActivity.FragmentId fragmentId, String orderBy, OrderDirection orderDirection ) {
        return getFragmentCursorHelper( querySelection, fragmentId, orderBy, orderDirection );
    }

    /**
     * Helper method for getFragmentCursor(). Returns the correct
     * cursor retrieval method for the specified fragment.
     */
    private Cursor getFragmentCursorHelper( String querySelection, MainActivity.FragmentId fragmentId, String orderBy, OrderDirection orderDirection ) {
        switch( fragmentId ) {
            case ARTISTS:
                return getAllUniqueArtists( querySelection );
            case ALBUMS:
                return getAllUniqueAlbums( querySelection );
            case SONGS:
                if( orderBy != null ) {
                    orderBy = orderBy + " " + orderDirection.name();
                }
                return getAllSongsSearchable( querySelection, orderBy );
            case GENRES:
                return getAllUniqueGenres( querySelection );
            default:
                return null;
        }

    }

    /**
     * Returns the playback cursor based on the specified query selection.
     */
    public Cursor getPlaybackCursor( Context context, String querySelection, MainActivity.FragmentId fragmentId ) {
        return getPlaybackCursorHelper( querySelection, fragmentId );
    }

    /**
     * Helper method for getPlaybackCursor(). Returns the correct
     * cursor retrieval method for the specified playback/fragment route.
     */
    private Cursor getPlaybackCursorHelper( String querySelection, MainActivity.FragmentId fragmentId ) {
        String orderBy = null;
        switch( fragmentId ) {
            case ARTISTS:
            case ALBUMS:
            case GENRES:
                orderBy = SONG_TRACK_NUMBER + "*1 ASC";
                break;
            case SONGS:
                orderBy = SONG_TITLE + " ASC";
                break;
        }

        return getAllSongsSearchable( querySelection, orderBy );
    }

    /**
     * Returns a selection cursor of all songs in the database.
     * This method can also be used to search all songs if a
     * valid selection parameter is passed.
     */
    public Cursor getAllSongsSearchable( String selection, String orderBy ) {
        if( selection != null && !"".equals( selection ) ) {
            selection = " WHERE " + selection;
        } else {
            selection = "";
        }
        if( orderBy != null && !"".equals( orderBy ) ) {
            orderBy = " ORDER BY " + orderBy;
        } else {
            orderBy = "";
        }
        String selectQuery = "SELECT  * FROM " + MUSIC_LIBRARY_TABLE + selection + orderBy;

        return getDatabase().rawQuery( selectQuery, null );
    }

    /**
     * Returns a cursor of songs sorted by their track number. Used for
     */

    /**
     * Returns a selection cursor of all unique artists.
     */
    public Cursor getAllUniqueArtists( String selection ) {
        if( selection != null && !"".equals( selection ) ) {
            selection = " WHERE " + selection;
        } else {
            selection = "";
        }
        String selectDistinctQuery = "SELECT DISTINCT(" + SONG_ARTIST + "), " + _ID + ", " + SONG_FILE_PATH + ", "
                                     + SONG_ALBUM_ART_PATH + ", " + SONG_DURATION + " FROM " + MUSIC_LIBRARY_TABLE +
                                     selection + " GROUP BY " + SONG_ARTIST + " ORDER BY " + SONG_ARTIST + " ASC";

        return getDatabase().rawQuery( selectDistinctQuery, null );

    }

    /**
     * Returns a selection cursor of all unique albums.
     */
    public Cursor getAllUniqueAlbums( String selection ) {
        if( selection != null && !"".equals( selection ) ) {
            selection = " WHERE " + selection;
        } else {
            selection = "";
        }
        String selectDistinctQuery = "SELECT DISTINCT(" + SONG_ALBUM + "), " +
                                     _ID + ", " + SONG_ARTIST + ", " + SONG_FILE_PATH + ", " +
                                     SONG_ALBUM_ART_PATH + ", " +
                                     SONG_DURATION +
                                     " FROM " + MUSIC_LIBRARY_TABLE + selection + " GROUP BY " +
                                     SONG_ALBUM + " ORDER BY " + SONG_ALBUM + " ASC";


        return getDatabase().rawQuery( selectDistinctQuery, null );

    }

    /**
     * Returns a selection cursor of all unique genres.
     */
    public Cursor getAllUniqueGenres( String selection ) {
        if( selection != null && !"".equals( selection ) ) {
            selection = " WHERE " + selection;
        } else {
            selection = "";
        }
        String selectDistinctQuery = "SELECT DISTINCT(" + SONG_GENRE + "), " +
                                     _ID + ", " + SONG_FILE_PATH + ", " + SONG_ALBUM_ART_PATH + ", " + SONG_DURATION
                                     + " FROM " +
                                     MUSIC_LIBRARY_TABLE + selection + " GROUP BY " +
                                     SONG_GENRE + " ORDER BY " + SONG_GENRE + " ASC";


        return getDatabase().rawQuery( selectDistinctQuery, null );

    }

    /**
     * Returns the number of songs in the specified genre.
     */
    public int getGenreSongCount( String genreName ) {
        String selection = SONG_GENRE + "=" + "'" + genreName.replace( "'", "''" ) + "'";
        Cursor cursor = getDatabase().query( MUSIC_LIBRARY_TABLE, null, selection, null, null, null, null );

        int songCount = cursor.getCount();
        cursor.close();
        return songCount;

    }

    /**
     * Saves the last playback position for the specified song.
     */
    public void setLastPlaybackPosition( String songId, long lastPlaybackPosition ) {
        if( songId != null ) {
            songId = songId.replace( "'", "''" );
        } else {
            return;
        }

        String where = SONG_ID + "=" + "'" + songId + "'";
        ContentValues values = new ContentValues();
        values.put( SAVED_POSITION, lastPlaybackPosition );

        getDatabase().update( MUSIC_LIBRARY_TABLE, values, where, null );

    }

    /**
     * Returns the album art path of the specified song.
     */
    public String getAlbumArtBySongId( String songId ) {
        String where = SONG_ID + "=" + "'" + songId + "'";
        Cursor cursor = getDatabase().query( MUSIC_LIBRARY_TABLE, new String[]{ _ID, SONG_ALBUM_ART_PATH }, where, null,
                                             null, null, null );

        if( cursor != null ) {
            cursor.moveToFirst();
            String albumArtPath = cursor.getString( cursor.getColumnIndex( SONG_ALBUM_ART_PATH ) );
            cursor.close();
            return albumArtPath;
        } else {
            return null;
        }

    }

    /**
     * Returns a cursor of all the songs in the current table.
     */
    public Cursor getAllSongs() {
        String selectQuery = "SELECT  * FROM " + MUSIC_LIBRARY_TABLE + " ORDER BY " + SONG_TITLE + " ASC";

        return getDatabase().rawQuery( selectQuery, null );

    }

    /**
     * Returns a cursor with the specified song.
     */
    public Cursor getSongById( String songID ) {
        String selection = SONG_ID + "=" + "'" + songID + "'";
        return getDatabase().query( MUSIC_LIBRARY_TABLE, null, selection, null, null, null, null );

    }


    /**
     * Returns the rating for the specified song.
     */
    public int getSongRating( String songId ) {
        String where = SONG_ID + "=" + "'" + songId + "'";
        Cursor cursor = getDatabase().query( MUSIC_LIBRARY_TABLE, new String[]{ _ID, SONG_RATING }, where, null, null,
                                             null, null );

        int songRating = 0;
        if( cursor != null ) {
            songRating = cursor.getInt( cursor.getColumnIndex( SONG_RATING ) );
            cursor.close();
        }

        return songRating;

    }

    /**
     * Sets the rating for the specified song.
     */
    public void setSongRating( String songId, int rating ) {
        String where = SONG_ID + "=" + "'" + songId + "'";
        ContentValues values = new ContentValues();
        values.put( SONG_RATING, rating );
        getDatabase().update( MUSIC_LIBRARY_TABLE, values, where, null );

    }


}
