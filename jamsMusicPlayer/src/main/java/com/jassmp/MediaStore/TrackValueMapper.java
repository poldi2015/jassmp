package com.jassmp.MediaStore;

import android.database.Cursor;

import com.jassmp.Dao.Column;

public class TrackValueMapper extends DefaultValueMapper {
    public TrackValueMapper( final Column column, final String mediaKey ) {
        super( column, mediaKey );
    }

    @Override
    public Object map( final MediaStoreSongIterator iterator, final Cursor cursor, String trackNumber ) {
        if( trackNumber != null ) {
            trackNumber = trackNumber.trim();
            if( trackNumber.contains( "/" ) ) {
                final int index = trackNumber.lastIndexOf( "/" );
                trackNumber = trackNumber.substring( 0, index );
            }

            try {
                if( Integer.parseInt( trackNumber ) <= 0 ) {
                    trackNumber = null;
                }
            } catch( Exception e ) {
                trackNumber = null;
            }

        }
        return trackNumber;
    }

}
