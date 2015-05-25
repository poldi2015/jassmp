package com.jassmp.JassMpDb;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.jassmp.Dao.Column;
import com.jassmp.Dao.FilterCursorAdapter;
import com.jassmp.Dao.FilterDao;

public abstract class AbstractFilterTableAccessor extends AbstractTableAccessor {

    //
    // defines

    public static final String TABLE_NAME_PREFIX = "Filter";

    //
    // private members

    private final Column   mFilterNameColumnInSongTable;
    private final Column   mFilterIdColumnInSongTable;
    private final Column[] mFilterColumns;
    private int mNumberOfSelectedEntries = -1;

    protected AbstractFilterTableAccessor( final Context context, final String tableName, final Column columnSongFilterName, final Column columnSongFilterId, final Column[] filterColumns ) {
        super( tableName, context );
        mFilterNameColumnInSongTable = columnSongFilterName;
        mFilterIdColumnInSongTable = columnSongFilterId;
        mFilterColumns = filterColumns;
    }

    public static AbstractFilterTableAccessor[] getInstances( final Context context ) {
        return new AbstractFilterTableAccessor[]{ GenreFilterTableAccessor.getInstance( context ),
                                                  AlbumFilterTableAccessor.getInstance( context ),
                                                  ArtistFilterTableAccessor.getInstance( context ),
                                                  RatingFilterTableAccessor.getInstance( context ), };
    }

    @Override
    public Column[] getTableColumns() {
        return mFilterColumns;
    }

    public Column getFilterNameColumnInSongTable() {
        return mFilterNameColumnInSongTable;
    }

    public Column getFilterIdColumnInSongTable() {
        return mFilterIdColumnInSongTable;
    }

    @SuppressWarnings("unused")
    public FilterCursorAdapter getAllFilterCursorAdapter( final Column orderBy, final OrderDirection orderDirection ) {
        final QueryBuilder builder = new QueryBuilder().result( getTableColumns() ).order( orderBy, orderDirection );
        final Cursor cursor = queryEntries( builder );
        return new DbFilterCursorAdapter( this, cursor );
    }

    /**
     * Adds or replaces entry.
     */
    public synchronized void updateEntry( final FilterDao filterDao ) {
        replaceEntry( filterDao.getContentValues() );
        if( filterDao.getId() == -1 ) {
            filterDao.setId( getId( filterDao.getName() ) );
        }
    }

    public int getId( final String name ) {
        final QueryBuilder builder = new QueryBuilder().where( FilterDao.COLUMN_NAME ).whereEq().whereText( name );
        return queryId( builder );
    }

    public synchronized void setSelected( final String name, final boolean selected ) {
        mNumberOfSelectedEntries = -1;
        final ContentValues values = new ContentValues();
        values.put( FilterDao.COLUMN_SELECTED.name, selected );
        updateEntry( values, new UnqualifiedWhereBuilder().where( FilterDao.COLUMN_NAME ).whereEq().whereText( name ) );
        commit();
    }

    public synchronized void setSelectedAll( final boolean selected ) {
        mNumberOfSelectedEntries = -1;
        final ContentValues values = new ContentValues();
        values.put( FilterDao.COLUMN_SELECTED.name, selected );
        updateEntry( values, null );
        commit();
    }

    public synchronized int getNumberOfSelectedEntries() {
        if( mNumberOfSelectedEntries == -1 ) {
            mNumberOfSelectedEntries = getNumberOfEntries( new QueryBuilder().where( FilterDao.COLUMN_SELECTED ) );
        }
        return mNumberOfSelectedEntries;
    }

    private static class DbFilterCursorAdapter extends FilterCursorAdapter {

        //
        // private members

        private final AbstractFilterTableAccessor mAccessor;

        public DbFilterCursorAdapter( final AbstractFilterTableAccessor accessor, final Cursor cursor ) {
            super( cursor, accessor.getTableColumns() );
            mAccessor = accessor;
        }

        @Override
        public void updateSelected( final boolean selected ) {
            mAccessor.setSelected( getDaoFromCursor().getName(), selected );
            mAccessor.commit();
        }

        @Override
        public void updateSelectedAll( final boolean selected ) {
            mAccessor.setSelectedAll( selected );
            mAccessor.commit();
        }

        @Override
        public int getNumberOfSelectedEntries() {
            return mAccessor.getNumberOfSelectedEntries();
        }

    }

}
