package com.jassmp.JassMpDb;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;

import com.jassmp.BuildConfig;
import com.jassmp.Dao.FilterDao;
import com.jassmp.Dao.SongDao;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.util.Arrays;
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

    private Map<AbstractFilterTableAccessor, Map<String, FilterDao>> mFilterCreationCache
            = new HashMap<AbstractFilterTableAccessor, Map<String, FilterDao>>();

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

    public synchronized SQLiteDatabase getDatabase() {
        return getWritableDatabase();
    }

    @Override
    public void onCreate( final SQLiteDatabase db ) {
        getFolderTableAccessor().onCreate( db );
        getSongTableAccessor().onCreate( db );
        for( final AbstractFilterTableAccessor filter : getFilterTableAccessors() ) {
            filter.onCreate( db );
        }
    }


    @Override
    public void onUpgrade( final SQLiteDatabase db, final int oldVersion, final int newVersion ) {
        if( oldVersion != newVersion ) {
            getFolderTableAccessor().onUpgrade( db, oldVersion, newVersion );
            getSongTableAccessor().onUpgrade( db, oldVersion, newVersion );
            for( final AbstractFilterTableAccessor filter : getFilterTableAccessors() ) {
                filter.onUpgrade( db, oldVersion, newVersion );
            }
        }
    }

    @Override
    public void onOpen( final SQLiteDatabase db ) {
        super.onOpen( db );
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

    public AbstractFilterTableAccessor[] getFilterTableAccessors() {
        return AbstractFilterTableAccessor.getInstances( mContext );
    }

    public SongTableAccessor getSongTableAccessor() {
        return SongTableAccessor.getInstance( mContext );
    }

    public FolderTableAccessor getFolderTableAccessor() {
        return FolderTableAccessor.getInstance( mContext );
    }

    public void updateSong( final SongDao song ) {
        updateFilterAndSongDaoAndCreationCache( song );
        getSongTableAccessor().updateSong( song );
    }

    public void commit() {
        getFolderTableAccessor().commit();
        getSongTableAccessor().commit();
        updateSongCount();
        mFilterCreationCache.clear();
        debugDumpDatabase();
    }

    private void updateFilterAndSongDaoAndCreationCache( final SongDao song ) {
        for( final AbstractFilterTableAccessor filter : getFilterTableAccessors() ) {
            Map<String, FilterDao> filterEntries = mFilterCreationCache.get( filter );
            if( filterEntries == null ) {
                filterEntries = new HashMap<String, FilterDao>();
                mFilterCreationCache.put( filter, filterEntries );
            }
            // Rating is an integer whereas the others are all strings
            final Object entryKey = song.getValue( filter.getFilterNameColumnInSongTable() );
            FilterDao filterDao = filterEntries.get( entryKey.toString() );
            if( filterDao == null ) {
                filterDao = new FilterDao( song, filter.getFilterNameColumnInSongTable(),
                                           Arrays.asList( filter.getTableColumns() ) );
                filterEntries.put( entryKey.toString(), filterDao );
                filter.updateEntry( filterDao );
            } else {
                filterDao.setCount( filterDao.getCount() + 1 );
            }
            song.putValue( filter.getFilterIdColumnInSongTable(), filterDao.getId() );
        }
    }

    private void updateSongCount() {
        for( final AbstractFilterTableAccessor filter : getFilterTableAccessors() ) {
            final Map<String, FilterDao> entryMap = mFilterCreationCache.get( filter );
            if( entryMap != null ) {
                for( final FilterDao filterDao : entryMap.values() ) {
                    filter.updateEntry( filterDao );
                }
            }
            filter.commit();
        }
    }

    private void debugDumpDatabase() {
        if( BuildConfig.DEBUG ) {
            final File sd = Environment.getExternalStorageDirectory();
            final File dbFile = new File( getDatabase().getPath() );
            if( dbFile.canRead() && sd.canWrite() ) {
                copyFile( dbFile, new File( sd, dbFile.getName() ) );
            }
        }
    }

    private void copyFile( final File in, final File out ) {
        try {
            final FileChannel inChannel = new FileInputStream( in ).getChannel();
            final FileChannel outChannel = new FileOutputStream( out ).getChannel();
            outChannel.transferFrom( inChannel, 0, inChannel.size() );
            inChannel.close();
            outChannel.close();
        } catch( Exception e ) {

        }
    }


}
