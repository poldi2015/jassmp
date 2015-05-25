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
package com.jassmp.JassMpDb;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.jassmp.Dao.SongDao;
import com.jassmp.MainActivity.MainActivity;

/**
 * SQLite database implementation. Note that this class
 * only contains methods that access Jams' private
 * database. For methods that access Android's
 * MediaStore database, see MediaStoreAccessHelper.
 *
 * @author Saravan Pantham
 */
public class DBAccessHelper {

    //Database instance. Will last for the lifetime of the application.
    private static DBAccessHelper sInstance;

    //Writable database instance.
    private SQLiteDatabase mDatabase;

    //Common fields.
    public static final String _ID = "_id";

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
    public static final String SONG_ALBUM_ART_PATH = "album_art_path";

    private final Context mContext;

    public DBAccessHelper( Context context ) {
        mContext = context;
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
        return DatabaseAccessor.getInstance( mContext ).getDatabase();
    }

    /**
     * Returns the playback cursor based on the specified query selection.
     */
    public Cursor getPlaybackCursor( Context context, String querySelection, MainActivity.FragmentId fragmentId ) {
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
    private Cursor getAllSongsSearchable( String selection, String orderBy ) {
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
     * Saves the last playback position for the specified song.
     */
    public void setLastPlaybackPosition( String songId, long lastPlaybackPosition ) {
        if( songId != null ) {
            songId = songId.replace( "'", "''" );
        } else {
            return;
        }

        String where = SongDao.COLUMN_ID.name + "=" + "'" + songId + "'";
        ContentValues values = new ContentValues();
        values.put( SAVED_POSITION, lastPlaybackPosition );

        getDatabase().update( MUSIC_LIBRARY_TABLE, values, where, null );

    }

}
