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

    public Context getContext() {
        return mContext;
    }

    public SQLiteDatabase getDatabase() {
        return getWritableDatabase();
    }

    @Override
    public void onCreate( final SQLiteDatabase db ) {
        getFolderTableAccessor().onCreate( db );
        getSongTableAccessor().onCreate( db );
        for( final FilterTablesAccessor.Filter filter : FilterTablesAccessor.Filter.values() ) {
            getFilterTablesAccessor( filter ).onCreate( db );
        }
    }


    @Override
    public void onUpgrade( final SQLiteDatabase db, final int oldVersion, final int newVersion ) {
        if( oldVersion != newVersion ) {
            getFolderTableAccessor().onUpgrade( db, oldVersion, newVersion );
            getSongTableAccessor().onUpgrade( db, oldVersion, newVersion );
            for( final FilterTablesAccessor.Filter filter : FilterTablesAccessor.Filter.values() ) {
                getFilterTablesAccessor( filter ).onUpgrade( db, oldVersion, newVersion );
            }
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
        updateFilterCreationCache( song );
    }

    public void commit() {
        getFolderTableAccessor().commit();
        getSongTableAccessor().commit();
        for( final FilterTablesAccessor.Filter filter : FilterTablesAccessor.Filter.values() ) {
            updateFilterTable( getFilterTablesAccessor( filter ) );
        }
        mFilterCreationCache.clear();
    }

    private void updateFilterCreationCache( final Song song ) {
        for( final FilterTablesAccessor.Filter filter : FilterTablesAccessor.Filter.values() ) {
            Map<String, Integer> filterToSongCount = mFilterCreationCache.get( filter );
            if( filterToSongCount == null ) {
                filterToSongCount = new HashMap<String, Integer>();
            }
            // Rating is an integer whereas the others are all strings
            final Object entryKey = song.getValue( filter.filterColumnInSongTable );
            final Integer songCount = filterToSongCount.get( entryKey.toString() );
            filterToSongCount.put( entryKey.toString(), songCount != null ? ( songCount + 1 ) : 1 );
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
