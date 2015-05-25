package com.jassmp.Dao;

import android.database.Cursor;

import java.util.Map;

/**
 * This class is used to create a {@link FilterDao}s from a {@link android.database.Cursor}.
 */

public abstract class FilterCursorAdapter extends AbstractCursorAdapter {

    private FilterDao[] mDaoCache = null;

    /**
     * Used if there is a on-by-one mapping between the column names of the SongDao and the cursor.
     */
    public FilterCursorAdapter( final Cursor cursor, final Column[] columns ) {
        super( cursor, columns );
        mDaoCache = new FilterDao[ getCount() ];
    }

    /**
     * Close the adapter.
     * <p/>
     * Must be called after using.
     */
    public void close() {
        if( isClosed() ) {
            return;
        }
        super.close();
        mDaoCache = null;
    }

    /**
     * Changes the position and returns Dao.
     *
     * @return {@link #getDaoFromCursor()}
     */
    public FilterDao getDaoFromCursor( final int position ) {
        if( !setPosition( position ) ) {
            return null;
        }
        return getDaoFromCursor();
    }

    /**
     * Creates the Dao.
     * <p/>
     * Override to create a customized Dao when not using the JassMpDb.
     *
     * @return the Dao or null if cursor is not on a valid row
     */
    public FilterDao getDaoFromCursor() {
        if( !isValidPosition() ) {
            return null;
        }
        final int position = getPosition();
        if( mDaoCache[ position ] != null ) {
            return mDaoCache[ position ];
        }
        final Map<Column, Object> values = getCurrentValuesFromCursor();
        if( values == null ) {
            return null;
        }
        return mDaoCache[ position ] = new FilterDao( values );
    }

    public void setSelected( final boolean selected ) {
        final int position = getPosition();
        if( mDaoCache[ position ] != null ) {
            mDaoCache[ position ].setSelected( selected );
        }
        updateSelected( selected );
    }

    public void setSelectedAll( final boolean selected ) {
        for( final FilterDao dao : mDaoCache ) {
            if( dao != null ) {
                dao.setSelected( selected );
            }
        }
        updateSelectedAll( selected );
    }

    public abstract void updateSelected( final boolean selected );

    public abstract void updateSelectedAll( final boolean selected );

    public abstract int getNumberOfSelectedEntries();

}
