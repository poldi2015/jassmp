package com.jassmp.JassMpDb;

import android.content.Context;

import com.jassmp.Dao.Column;
import com.jassmp.Dao.FilterDao;
import com.jassmp.Dao.SongDao;

public class GenreFilterTableAccessor extends AbstractFilterTableAccessor {

    //
    // define

    public static final String   TABLE_NAME              = TABLE_NAME_PREFIX + "Genre";
    public static final Column   COLUMN_SONG_FILTER_NAME = SongDao.COLUMN_SONG_GENRE;
    public static final Column   COLUMN_SONG_FILTER_ID   = SongDao.COLUMN_SONG_GENRE_ID;
    public static final Column[] COLUMNS                 = { FilterDao.COLUMN_ID, FilterDao.COLUMN_NAME,
                                                             FilterDao.COLUMN_COUNT, FilterDao.COLUMN_SELECTED };

    //
    // private members

    private static GenreFilterTableAccessor sInstance = null;

    public GenreFilterTableAccessor( final Context context ) {
        super( context, TABLE_NAME, COLUMN_SONG_FILTER_NAME, COLUMN_SONG_FILTER_ID, COLUMNS );
    }

    public static synchronized GenreFilterTableAccessor getInstance( final Context context ) {
        if( sInstance == null ) {
            sInstance = new GenreFilterTableAccessor( context.getApplicationContext() );
        }
        return sInstance;
    }
}
