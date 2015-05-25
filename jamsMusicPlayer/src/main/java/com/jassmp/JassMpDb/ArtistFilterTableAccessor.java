package com.jassmp.JassMpDb;

import android.content.Context;

import com.jassmp.Dao.Column;
import com.jassmp.Dao.FilterDao;
import com.jassmp.Dao.SongDao;

public class ArtistFilterTableAccessor extends AbstractFilterTableAccessor {

    //
    // define

    public static final String   TABLE_NAME              = TABLE_NAME_PREFIX + "Artist";
    public static final Column   COLUMN_SONG_FILTER_NAME = SongDao.COLUMN_SONG_ARTIST;
    public static final Column   COLUMN_SONG_FILTER_ID   = SongDao.COLUMN_SONG_ARTIST_ID;
    public static final Column[] COLUMNS                 = { FilterDao.COLUMN_ID, FilterDao.COLUMN_NAME,
                                                             FilterDao.COLUMN_COUNT, FilterDao.COLUMN_SELECTED,
                                                             FilterDao.COLUMN_ART_PATH };

    //
    // private members

    private static ArtistFilterTableAccessor sInstance = null;

    public ArtistFilterTableAccessor( final Context context ) {
        super( context, TABLE_NAME, COLUMN_SONG_FILTER_NAME, COLUMN_SONG_FILTER_ID, COLUMNS );
    }

    public static synchronized ArtistFilterTableAccessor getInstance( final Context context ) {
        if( sInstance == null ) {
            sInstance = new ArtistFilterTableAccessor( context.getApplicationContext() );
        }
        return sInstance;
    }

}
