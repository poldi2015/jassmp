package com.jams.music.player.MediaStore;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import com.jams.music.player.DBHelpers.Column;
import com.jams.music.player.R;

public class GenreValueMapper extends DefaultValueMapper {

    public GenreValueMapper( final Column column ) {
        super( column, null );
    }

    @Override
    public Object map( final MediaStoreSongIterator iterator, final Cursor cursor, final String value ) {
        return getGenre( iterator.getContext(), cursor.getInt( MediaStoreSongIterator.ID_INDEX ) );
    }

    private String getGenre( final Context context, final int songId ) {
        final Uri uri = MediaStore.Audio.Genres.getContentUriForAudioId( "external", songId );
        final Cursor cursor = context.getContentResolver()
                                     .query( uri, new String[]{ MediaStore.Audio.Genres.NAME }, null, null, null );
        String genre = null;
        try {
            if( cursor != null && cursor.moveToFirst() ) {
                genre = cursor.getString( 0 );
            }
        } finally {
            if( cursor != null ) {
                cursor.close();
            }
        }
        if( genre == null || genre.isEmpty() ||
            genre.equals( " " ) || genre.equals( "   " ) ||
            genre.equals( "    " ) ) {
            genre = context.getResources().getString( R.string.unknown_genre );
        }

        return genre;
    }

}
