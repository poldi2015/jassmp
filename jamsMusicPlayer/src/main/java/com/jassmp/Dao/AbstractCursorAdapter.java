package com.jassmp.Dao;

import android.database.Cursor;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractCursorAdapter {

    private Cursor   mCursor;
    private Column[] mColumnByIndex;
    private int[]    mTypeByIndex;

    /**
     * Used if there is a on-by-one mapping between the column names of the SongDao and the cursor.
     */
    public AbstractCursorAdapter( final Cursor cursor, final Column[] columns ) {
        mCursor = cursor;
        if( mCursor.getCount() > 0 ) {
            mCursor.moveToFirst();
            mColumnByIndex = new Column[ cursor.getColumnCount() ];
            mTypeByIndex = new int[ cursor.getColumnCount() ];
            for( final Column column : columns ) {
                final int columnIndex = cursor.getColumnIndex( column.name );
                mColumnByIndex[ columnIndex ] = column;
                mTypeByIndex[ columnIndex ] = cursor.getType( columnIndex );
            }
        }
    }

    public Cursor getCursor() {
        return mCursor;
    }

    public boolean isClosed() {
        return mCursor == null || mCursor.isClosed();
    }

    /**
     * Close the adapter.
     * <p/>
     * Must be called after using.
     */
    public void close() {
        if( mCursor != null && !mCursor.isClosed() ) {
            try {
                mCursor.close();
            } catch( Exception e ) {
                // Do nothing
            }
            mCursor = null;
            mColumnByIndex = null;
            mTypeByIndex = null;
        }
    }

    @Override
    public void finalize() throws Throwable {
        close();
        super.finalize();
    }

    public boolean setPosition( final int position ) {
        return !isClosed() && mCursor.moveToPosition( position );
    }

    public int getPosition() {
        if( isClosed() ) {
            return -1;
        }
        return mCursor.getPosition();
    }

    public int getCount() {
        if( isClosed() ) {
            return -1;
        }
        return mCursor.getCount();
    }

    public boolean isValidPosition() {
        return getCount() > 0 && !mCursor.isBeforeFirst() && !mCursor.isAfterLast();
    }

    /**
     * Get the values of the current cursor position mapped to the columns handed in by the constructor using the
     * datatypes given by the database.
     */
    protected Map<Column, Object> getCurrentValuesFromCursor() {
        if( !isValidPosition() ) {
            return null;
        }
        final Cursor cursor = getCursor();
        final Map<Column, Object> values = new HashMap<Column, Object>();
        for( int i = 0; i < mColumnByIndex.length; i++ ) {
            if( mColumnByIndex[ i ] == null ) {
                continue;
            }
            switch( mTypeByIndex[ i ] ) {
                case Cursor.FIELD_TYPE_STRING:
                    values.put( mColumnByIndex[ i ], cursor.getString( i ) );
                    break;
                case Cursor.FIELD_TYPE_INTEGER:
                    values.put( mColumnByIndex[ i ], cursor.getInt( i ) );
                    break;
                case Cursor.FIELD_TYPE_FLOAT:
                    values.put( mColumnByIndex[ i ], cursor.getFloat( i ) );
                    break;

            }
        }
        return values;
    }

}
