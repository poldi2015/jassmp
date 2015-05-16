package com.jassmp.MediaStore;

import android.database.Cursor;

import com.jassmp.DBHelpers.Column;
import com.jassmp.Utils.AudioFileReader;

public class RatingValueMapper extends DefaultValueMapper {

    public RatingValueMapper( final Column column, final String mediaKey ) {
        super( column, mediaKey );
    }

    @Override
    public Object map( final MediaStoreSongIterator iterator, final Cursor cursor, final String path ) {
        final AudioFileReader audioFileReader = iterator.getAudioFileReader( path );
        if( audioFileReader == null ) {
            return null;
        }
        return new Integer( audioFileReader.getRating() );
    }

}
