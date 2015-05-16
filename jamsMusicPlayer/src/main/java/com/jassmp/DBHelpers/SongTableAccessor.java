package com.jassmp.DBHelpers;

import android.content.Context;

public class SongTableAccessor extends AbstractTableAccessor {

    //
    // defines

    public static final String TABLE_NAME = "MusicLibraryTable";


    //
    // private members

    private static SongTableAccessor sInstance = null;

    public SongTableAccessor( Context context ) {
        super( TABLE_NAME, context );
    }

    public static synchronized SongTableAccessor getInstance( Context context ) {
        if( sInstance == null ) {
            sInstance = new SongTableAccessor( context.getApplicationContext() );
        }

        return sInstance;
    }

    @Override
    protected Column[] getTableColumns() {
        return Song.COLUMNS;
    }

    public void updateSong( final Song song ) {
        replaceEntry( song.getContentValues() );
    }

}
