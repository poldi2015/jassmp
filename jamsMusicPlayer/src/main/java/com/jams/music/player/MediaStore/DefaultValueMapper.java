package com.jams.music.player.MediaStore;

import android.database.Cursor;

import com.jams.music.player.DBHelpers.Column;

public class DefaultValueMapper {
    public String mediaKey;
    public Column column;

    public DefaultValueMapper( final Column column, final String mediaKey ) {
        this.mediaKey = mediaKey;
        this.column = column;
    }

    public Object map( final MediaStoreSongIterator iterator, final Cursor cursor, String value ) {
        if( value == null ) {
            return null;
        }
        value = value.trim();
        if( "".equals( value ) ) {
            return null;
        }
        return value;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "( column=" + column.toString() + ", mediaKey=" + mediaKey + " )";
    }
}
