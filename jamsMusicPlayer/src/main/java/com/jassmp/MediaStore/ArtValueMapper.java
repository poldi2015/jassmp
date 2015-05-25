package com.jassmp.MediaStore;

import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;

import com.jassmp.Dao.Column;

public class ArtValueMapper extends DefaultValueMapper {

    //
    // defines

    private static final Uri ART_CONTENT_URI = Uri.parse( "content://media/external/audio/albumart" );

    public ArtValueMapper( final Column column, final String albumId ) {
        super( column, albumId );
    }

    @Override
    public Object map( final MediaStoreSongIterator iterator, final Cursor cursor, final String albumId ) {
        return ContentUris.withAppendedId( ART_CONTENT_URI, Long.parseLong( albumId ) );
    }
}
