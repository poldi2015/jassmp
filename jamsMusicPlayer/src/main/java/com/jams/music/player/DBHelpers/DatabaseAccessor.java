package com.jams.music.player.DBHelpers;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public class DatabaseAccessor extends SQLiteOpenHelper {

    //
    // defines

    public static final String DATABASE_NAME    = "jassmp.db";
    public static final int    DATABASE_VERSION = 1;

    public static final String TAG = DatabaseAccessor.class.getSimpleName();

    //
    // private members

    private static DatabaseAccessor sInstance = null;

    private final Context mContext;

    private EnumMap<FilterTablesAccessor.Filter, Map<String, Integer>> mFilterCreationCache
            = new EnumMap<FilterTablesAccessor.Filter, Map<String, Integer>>( FilterTablesAccessor.Filter.class );

    protected DatabaseAccessor( final Context context ) {
        super( context, DatabaseAccessor.DATABASE_NAME, null, DatabaseAccessor.DATABASE_VERSION );
        mContext = context;
    }

    public static synchronized DatabaseAccessor getInstance( Context context ) {
        if( sInstance == null ) {
            sInstance = new DatabaseAccessor( context.getApplicationContext() );
        }

        return sInstance;
    }


    @Override
    public void onCreate( final SQLiteDatabase db ) {
    }


    @Override
    public void onUpgrade( final SQLiteDatabase db, final int oldVersion, final int newVersion ) {
        if( oldVersion != newVersion ) {
            onCreate( db );
        }
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            getWritableDatabase().close();
            super.finalize();
        } catch( Exception e ) {
            Log.e( TAG, "Failed to close database", e );
        }
    }

    public FilterTablesAccessor getFilterTablesAccessor( final FilterTablesAccessor.Filter filter ) {
        return FilterTablesAccessor.getInstance( mContext, filter );
    }

    public SongTableAccessor getSongTableAccessor() {
        return SongTableAccessor.getInstance( mContext );
    }

    public FolderTableAccessor getFolderTableAccessor() {
        return FolderTableAccessor.getInstance( mContext );
    }

    public void updateSong( final Song song ) {
        getSongTableAccessor().updateSong( song );
        updateFilterCache( song );
    }

    public void commit() {
        getFolderTableAccessor().commit();
        getSongTableAccessor().commit();
        for( final FilterTablesAccessor.Filter filter : FilterTablesAccessor.Filter.values() ) {
            updateFilterTable( getFilterTablesAccessor( filter ) );
        }
        mFilterCreationCache.clear();
    }

    private void updateFilterCache( final Song song ) {
        for( final FilterTablesAccessor.Filter filter : FilterTablesAccessor.Filter.values() ) {
            Map<String, Integer> filterToSongCount = mFilterCreationCache.get( filter );
            if( filterToSongCount == null ) {
                filterToSongCount = new HashMap<String, Integer>();
            }
            final String entryName = song.getValue( filter.songTableColumn );
            final Integer songCount = filterToSongCount.get( entryName );
            filterToSongCount.put( entryName, new Integer( songCount != null ? ( songCount.intValue() + 1 ) : 1 ) );
        }
    }

    private void updateFilterTable( final FilterTablesAccessor filterTablesAccessor ) {
        final Map<String, Integer> entryMap = mFilterCreationCache.get( filterTablesAccessor.getFilter() );
        if( entryMap != null ) {
            for( final Map.Entry<String, Integer> entry : entryMap.entrySet() ) {
                filterTablesAccessor.addName( entry.getKey(), entry.getValue() );
            }
        }
        filterTablesAccessor.commit();
    }

}
