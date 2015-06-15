package com.jassmp.JassMpDb;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.jassmp.Dao.Column;
import com.jassmp.Dao.FilterDao;
import com.jassmp.Dao.SongCursorAdapter;
import com.jassmp.Dao.SongDao;

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
    public Column[] getTableColumns() {
        return SongDao.COLUMNS;
    }

    public void updateSong( final SongDao song ) {
        replaceEntry( song.getContentValues() );
    }

    public SongDao getSong( final String key ) {
        final QueryBuilder builder = new QueryBuilder().where( SongDao.COLUMN_KEY ).whereEq().whereText( key );
        final SongCursorAdapter songCursorAdapter = getSongCursorAdapter( builder, null, null );
        if( songCursorAdapter.getCount() < 1 ) {
            return null;
        }
        final SongDao dao = songCursorAdapter.getDaoFromCursor( 0 );
        songCursorAdapter.close();

        return dao;
    }

    public SongCursorAdapter getAllSongsCursorAdapter( final Column orderBy, final OrderDirection orderDirection ) {
        return getSongCursorAdapter( null, orderBy, orderDirection );
    }

    public SongCursorAdapter getFilteredSongsCursorAdapter( final Column orderBy, final OrderDirection orderDirection
                                                          ) {
        final QueryBuilder builder = new QueryBuilder();
        for( final AbstractFilterTableAccessor filter : DatabaseAccessor.getInstance( getContext() )
                                                                        .getFilterTableAccessors() ) {
            appendFilterToWhere( builder, filter );
        }

        return getSongCursorAdapter( builder, orderBy, orderDirection );
    }

    public QueryBuilder appendFilterToWhere( final QueryBuilder builder, AbstractFilterTableAccessor filter ) {
        builder.table( filter.getTableName(), filter.getTableName() );
        builder.whereAnd();
        builder.where( filter.getFilterIdColumnInSongTable() )
               .whereEq()
               .where( filter.getTableName(), FilterDao.COLUMN_ID );
        builder.whereAnd();
        builder.where( filter.getTableName(), FilterDao.COLUMN_SELECTED );

        return builder;
    }

    private SongCursorAdapter getSongCursorAdapter( QueryBuilder builder, final Column orderBy,
                                                    final OrderDirection orderDirection ) {
        if( builder == null ) {
            builder = new QueryBuilder();
        }
        builder.order( orderBy, orderDirection );
        final Cursor cursor = queryEntries( builder );
        return new SongCursorAdapter( cursor, SongDao.COLUMNS );
    }

    public void setArtworkPath( final SongDao songDao, final String artworkPath ) {
        final ContentValues values = new ContentValues();
        values.put( DBAccessHelper.SONG_ALBUM_ART_PATH, artworkPath );

        beginNonExclusive();
        updateEntry( values,
                     new UnqualifiedWhereBuilder().where( SongDao.COLUMN_ID ).whereEq().whereText( songDao.getId() ) );
        yieldCommit();
    }

}
