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
public class FilterDBsAccessor extends AbstractDBAccessor {

    //
    // defines


    public static final String DB_NAME_PREFIX = "Filter";

    public static final Column COLUMN_NAME       = new Column( "name", ColumnFlag.PRIMARY_KEY );
    public static final Column COLUMN_SELECTED   = new Column( "selected", ColumnType.INTEGER, "1" );
    public static final Column COLUMN_SONG_COUNT = new Column( "song_count", ColumnType.INTEGER, "0" );

    public static final Column[] COLUMNS = { COLUMN_NAME, COLUMN_SELECTED, };

    //
    // private members

    private static final Map<String, FilterDBsAccessor> sInstances = new HashMap<String, FilterDBsAccessor>();

    private FilterDBsAccessor( final Context context, final String filter ) {
        super( DB_NAME_PREFIX + filter, context );
    }

    public static synchronized FilterDBsAccessor getInstance( final Context context, final String filter ) {
        FilterDBsAccessor helper = sInstances.get( filter );
        if( helper == null ) {
            helper = new FilterDBsAccessor( context, filter );
            sInstances.put( filter, helper );
        }

        return helper;
    }

    @Override
    protected Column[] getTableColumns() {
        return COLUMNS;
    }

    @Override
    protected Column[] getAdditionalColumns( int version ) {
        return new Column[ 0 ];
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
        insertEntry( values );
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
