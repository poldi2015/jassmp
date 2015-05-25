package com.jassmp.Dao;

import android.database.Cursor;

import java.util.Map;

/**
 * This class is used to create a {@link SongDao}s from a {@link android.database.Cursor}.
 */
public class SongCursorAdapter extends AbstractCursorAdapter {

    private int     mCurrentPosition = -1;
    private SongDao mCurrentDao      = null;

    /**
     * Used if there is a on-by-one mapping between the column names of the SongDao and the cursor.
     */
    public SongCursorAdapter( final Cursor cursor, final Column[] columns ) {
        super( cursor, columns );
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
        mCurrentPosition = -1;
        mCurrentDao = null;
    }

    /**
     * Changes the position and returns Dao.
     *
     * @return {@link #getDaoFromCursor()}
     */
    public SongDao getDaoFromCursor( final int position ) {
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
    public SongDao getDaoFromCursor() {
        if( !isValidPosition() ) {
            return null;
        }
        final int position = getPosition();
        if( mCurrentDao != null && position == mCurrentPosition ) {
            return mCurrentDao;
        }
        final Map<Column, Object> values = getCurrentValuesFromCursor();
        if( values == null ) {
            return null;
        }
        mCurrentPosition = position;
        mCurrentDao = new SongDao( values );
        return mCurrentDao;
    }

}
