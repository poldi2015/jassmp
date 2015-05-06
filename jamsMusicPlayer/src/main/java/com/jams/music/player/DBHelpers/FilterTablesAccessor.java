package com.jams.music.player.DBHelpers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by poldi on 01.05.15.
 */
public class FilterTablesAccessor extends AbstractTableAccessor {

    //
    // defines

    public static enum Filter {
        GENRE( Song.COLUMN_SONG_GENRE ), ARTIST( Song.COLUMN_SONG_ARTIST ), ALBUM( Song.COLUMN_SONG_ALBUM ), RATING(
                Song.COLUMN_SONG_RATING );

        public final Column songTableColumn;

        private Filter( final Column songTableColumn ) {
            this.songTableColumn = songTableColumn;
        }
    }

    public static final String DB_NAME_PREFIX = "Filter";

    public static final Column COLUMN_NAME       = new Column( "name", ColumnFlag.UNIQUE );
    public static final Column COLUMN_KEY        = new Column( "key", ColumnFlag.UNIQUE );
    public static final Column COLUMN_SELECTED   = new Column( "selected", ColumnType.INTEGER, "1" );
    public static final Column COLUMN_SONG_COUNT = new Column( "song_count", ColumnType.INTEGER, "0" );

    public static final Column[] COLUMNS = { COLUMN_ID, COLUMN_KEY, COLUMN_NAME, COLUMN_SELECTED, };

    //
    // private members

    private final Filter mFilter;
    private static final Map<Filter, FilterTablesAccessor> sInstances = new HashMap<Filter, FilterTablesAccessor>();

    private FilterTablesAccessor( final Context context, final Filter filter ) {
        super( DB_NAME_PREFIX + filter.name(), context );
        mFilter = filter;
    }

    public static synchronized FilterTablesAccessor getInstance( final Context context, final Filter filter ) {
        FilterTablesAccessor helper = sInstances.get( filter );
        if( helper == null ) {
            helper = new FilterTablesAccessor( context, filter );
            sInstances.put( filter, helper );
        }

        return helper;
    }

    @Override
    protected Column[] getTableColumns() {
        return COLUMNS;
    }

    public Filter getFilter() {
        return mFilter;
    }

    public Set<String> getNames() {
        final Cursor cursor = queryEntries( new QueryBuilder().addResultColumn( COLUMN_NAME ) );
        final Set<String> names = new HashSet<String>();
        try {
            if( resetToFirst( cursor ) ) {
                do {
                    names.add( cursor.getString( 0 ) );
                } while( cursor.moveToNext() );
            }
        } finally {
            if( cursor != null ) {
                cursor.close();
            }
        }

        return names;
    }

    public void addName( final String name, final int songCount ) {
        final ContentValues values = new ContentValues();
        values.put( COLUMN_NAME.name, name );
        values.put( COLUMN_SELECTED.name, 1 );
        values.put( COLUMN_SONG_COUNT.name, songCount );
        replaceEntry( values );
    }

    public void removeName( final String name ) {
        removeEntry( COLUMN_NAME, name );
    }

    public boolean hasName( final String name ) {
        return hasEntries( COLUMN_NAME.name + " = '" + name + "'" );
    }

    public void setSelected( final String name, final boolean selected ) {
        if( !hasName( name ) ) {
            return;
        }
    }

    public void setSelectedAll() {
        final ContentValues values = new ContentValues();
        values.put( COLUMN_SELECTED.name, true );
        updateEntry( values, null );
    }

    public void setSelectedNone() {
        final ContentValues values = new ContentValues();
        values.put( COLUMN_SELECTED.name, false );
        updateEntry( values, null );
    }

    public boolean isSelected( final String name ) {
        return hasEntries( COLUMN_NAME.name + " = '" + name + "' AND " + COLUMN_SELECTED.name );
    }

    public int getSongCount( final String name ) {
        final Cursor cursor = queryEntries(
                new QueryBuilder().addResultColumn( COLUMN_SONG_COUNT ).setWhereWhereExpr( COLUMN_NAME.name +
                                                                                           " = " + name ) );
        int songCount;
        try {
            if( resetToFirst( cursor ) ) {
                songCount = cursor.getInt( 0 );
            } else {
                songCount = 0;
            }
        } finally {
            if( cursor != null ) {
                cursor.close();
            }
        }

        return songCount;
    }

}
