package com.jassmp.Dao;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;

import com.jassmp.ImageTransformers.PicassoMirrorReflectionTransformer;
import com.jassmp.JassMpDb.SongTableAccessor;
import com.jassmp.R;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;

public class SongDao {

    public static final String KEY                 = "key";
    public static final String SONG_TITLE          = "title";
    public static final String SONG_ARTIST         = "artist";
    public static final String SONG_ALBUM          = "album";
    public static final String SONG_DURATION       = "duration";
    public static final String SAVED_POSITION      = "saved_position";
    public static final String SONG_FILE_PATH      = "file_path";
    public static final String SONG_TRACK_NUMBER   = "track_number";
    public static final String SONG_GENRE          = "genre";
    public static final String SONG_PLAY_COUNT     = "play_count";
    public static final String SONG_YEAR           = "year";
    public static final String SONG_RATING         = "rating";
    public static final String ADDED_TIMESTAMP     = "added_timestamp";
    public static final String SONG_BPM            = "bpm";
    public static final String SONG_ALBUM_ART_PATH = "album_art_path";
    public static final String SONG_GENRE_ID       = "genreId";
    public static final String SONG_ARTIST_ID      = "artistId";
    public static final String SONG_ALBUM_ID       = "albumId";
    public static final String SONG_RATING_ID      = "ratingId";

    public static final Column COLUMN_ID                  = SongTableAccessor.COLUMN_ID;
    public static final Column COLUMN_KEY                 = new Column( KEY, Column.ColumnFlag.UNIQUE );
    public static final Column COLUMN_SONG_TITLE          = new Column( SONG_TITLE );
    public static final Column COLUMN_SONG_ARTIST         = new Column( SONG_ARTIST, R.string.unknown_artist );
    public static final Column COLUMN_SONG_ALBUM          = new Column( SONG_ALBUM, R.string.unknown_album );
    public static final Column COLUMN_SONG_DURATION       = new Column( SONG_DURATION, Column.ColumnType.INTEGER, "0" );
    public static final Column COLUMN_SONG_FILE_PATH      = new Column( SONG_FILE_PATH );
    public static final Column COLUMN_SONG_TRACK_NUMBER   = new Column( SONG_TRACK_NUMBER, Column.ColumnType.INTEGER,
                                                                        "0" );
    public static final Column COLUMN_SONG_GENRE          = new Column( SONG_GENRE, R.string.unknown_genre );
    public static final Column COLUMN_SONG_PLAY_COUNT     = new Column( SONG_PLAY_COUNT, Column.ColumnType.INTEGER,
                                                                        "0" );
    public static final Column COLUMN_SONG_SAVED_POSITION = new Column( SAVED_POSITION, Column.ColumnType.INTEGER,
                                                                        "0" );
    public static final Column COLUMN_SONG_YEAR           = new Column( SONG_YEAR, "" );
    public static final Column COLUMN_SONG_RATING         = new Column( SONG_RATING, Column.ColumnType.INTEGER, "0" );
    public static final Column COLUMN_ADDED_TIMESTAMP     = new Column( ADDED_TIMESTAMP );
    public static final Column COLUMN_SONG_BPM            = new Column( SONG_BPM, Column.ColumnType.INTEGER, "0" );
    public static final Column COLUMN_SONG_ALBUM_ART_PATH = new Column( SONG_ALBUM_ART_PATH );
    public static final Column COLUMN_SONG_GENRE_ID       = new Column( SONG_GENRE_ID, Column.ColumnType.INTEGER,
                                                                        "-1" );
    public static final Column COLUMN_SONG_ARTIST_ID      = new Column( SONG_ARTIST_ID, Column.ColumnType.INTEGER,
                                                                        "-1" );
    public static final Column COLUMN_SONG_ALBUM_ID       = new Column( SONG_ALBUM_ID, Column.ColumnType.INTEGER,
                                                                        "-1" );
    public static final Column COLUMN_SONG_RATING_ID      = new Column( SONG_RATING_ID, Column.ColumnType.INTEGER,
                                                                        "-1" );

    public static final Column[] COLUMNS = { COLUMN_ID, COLUMN_KEY, COLUMN_SONG_TITLE, COLUMN_SONG_ARTIST,
                                             COLUMN_SONG_ALBUM, COLUMN_SONG_DURATION, COLUMN_SONG_FILE_PATH,
                                             COLUMN_SONG_TRACK_NUMBER, COLUMN_SONG_GENRE, COLUMN_SONG_PLAY_COUNT,
                                             COLUMN_SONG_SAVED_POSITION, COLUMN_SONG_YEAR, COLUMN_SONG_RATING,
                                             COLUMN_ADDED_TIMESTAMP, COLUMN_SONG_BPM, COLUMN_SONG_ALBUM_ART_PATH,
                                             COLUMN_SONG_GENRE_ID, COLUMN_SONG_ARTIST_ID, COLUMN_SONG_ALBUM_ID,
                                             COLUMN_SONG_RATING_ID };

    private static final String HASH_PADDING = "000000000000000000000000000000";

    private final Map<Column, Object> mValues;
    private Bitmap mAlbumArt = null;

    public SongDao( final Map<Column, Object> values ) {
        mValues = new HashMap<Column, Object>( COLUMNS.length );
        for( final Column column : COLUMNS ) {
            if( !COLUMN_ID.equals( column ) ) {
                mValues.put( column, convertValue( column, values.get( column ) ) );
            }
        }
        mValues.put( COLUMN_KEY, generateKey( mValues.get( COLUMN_SONG_FILE_PATH ) ) );
    }

    public int getId() {
        return (Integer) mValues.get( COLUMN_ID );
    }

    public String getKey() {
        return (String) mValues.get( COLUMN_KEY );
    }

    public String getTitle() {
        return (String) mValues.get( COLUMN_SONG_TITLE );
    }

    public String getArtist() {
        return (String) mValues.get( COLUMN_SONG_ARTIST );
    }

    public String getAlbum() {
        return (String) mValues.get( COLUMN_SONG_ALBUM );
    }

    public int getDuration() {
        return (Integer) mValues.get( COLUMN_SONG_DURATION );
    }

    public String getFilePath() {
        return (String) mValues.get( COLUMN_SONG_FILE_PATH );
    }

    public Uri getFileUri() {
        return Uri.parse( (String) mValues.get( COLUMN_SONG_FILE_PATH ) );
    }

    public int getTrackNumber() {
        return (Integer) mValues.get( COLUMN_SONG_TRACK_NUMBER );
    }

    public String getGenre() {
        return (String) mValues.get( COLUMN_SONG_GENRE );
    }

    public int getPlayCount() {
        return (Integer) mValues.get( COLUMN_SONG_PLAY_COUNT );
    }

    public int getSavedPosition() {
        return (Integer) mValues.get( COLUMN_SONG_SAVED_POSITION );
    }

    public String getYear() {
        return (String) mValues.get( COLUMN_SONG_YEAR );
    }

    public int getRating() {
        return (Integer) mValues.get( COLUMN_SONG_RATING );
    }

    public int getAddedTimestamp() {
        return (Integer) mValues.get( COLUMN_ADDED_TIMESTAMP );
    }

    public int getBpm() {
        return (Integer) mValues.get( COLUMN_SONG_BPM );
    }

    public String getAlbumArtPath() {
        return (String) mValues.get( COLUMN_SONG_ALBUM_ART_PATH );
    }

    public int getGenreId() {
        return (Integer) mValues.get( COLUMN_SONG_GENRE_ID );
    }

    public int getArtistId() {
        return (Integer) mValues.get( COLUMN_SONG_ARTIST_ID );
    }

    public int getAlbumId() {
        return (Integer) mValues.get( COLUMN_SONG_ALBUM_ID );
    }

    public int getRatingId() {
        return (Integer) mValues.get( COLUMN_SONG_RATING_ID );
    }

    public boolean hasValue( final Column column ) {
        return mValues.containsKey( column );
    }

    public void putValue( final Column column, Object value ) {
        mValues.put( column, value );
    }

    public <T> T getValue( final Column column ) {
        return (T) mValues.get( column );
    }

    public ContentValues getContentValues() {
        final ContentValues values = new ContentValues();
        for( final Map.Entry<Column, Object> entry : mValues.entrySet() ) {
            final Object value = entry.getValue();
            if( value instanceof String ) {
                values.put( entry.getKey().name, (String) value );
            } else if( value instanceof Integer ) {
                values.put( entry.getKey().name, (Integer) value );
            } else if( value instanceof Float ) {
                values.put( entry.getKey().name, (Float) value );
            }
        }

        return values;
    }

    private Object convertValue( final Column column, final Object data ) {
        Object value = "";
        switch( column.type ) {
            case TEXT:
                value = data != null ? data.toString() : "";
                break;
            case INTEGER:
                if( data == null ) {
                    value = Integer.valueOf( 0 );
                } else if( data instanceof Long ) {
                    value = Integer.valueOf( ( (Long) data ).intValue() );
                } else if( data instanceof Integer ) {
                    value = data;
                } else if( data instanceof Float ) {
                    value = Integer.valueOf( ( (Float) data ).intValue() );
                } else if( data instanceof Double ) {
                    value = Integer.valueOf( ( (Double) data ).intValue() );
                } else {
                    value = Integer.valueOf( data.toString() );
                }
                break;
            case REAL:
                if( data == null ) {
                    value = Float.valueOf( 0 );
                } else if( data instanceof Long ) {
                    value = Float.valueOf( ( (Long) data ).floatValue() );
                } else if( data instanceof Integer ) {
                    value = Float.valueOf( ( (Integer) data ).intValue() );
                } else if( data instanceof Double ) {
                    value = Float.valueOf( ( (Double) data ).floatValue() );
                } else if( data instanceof Float ) {
                    value = data;
                } else {
                    value = Float.valueOf( data.toString() );
                }
        }

        return value;
    }

    private String generateKey( final Object data ) {
        try {
            final MessageDigest digest = MessageDigest.getInstance( "MD5" );
            digest.reset();
            digest.update( data.toString().getBytes() );
            final BigInteger bigInt = new BigInteger( 1, digest.digest() );
            String hashCode = bigInt.toString( 16 );
            int len = hashCode.length();
            if( len < 32 ) {
                hashCode = HASH_PADDING.substring( 0, 32 - len ) + hashCode;

            }
            return hashCode;
        } catch( Exception e ) {
            return HASH_PADDING;
        }
    }

    public void loadAlbumArt( final Context context, final AlbumArtLoadedListener listener ) {
        if( mAlbumArt != null ) {
            listener.artLoaded( mAlbumArt );
        } else {
            final Picasso picasso = new Picasso.Builder( context ).build();
            picasso.load( getAlbumArtPath() ).transform( new PicassoMirrorReflectionTransformer() ).into( new Target() {

                @Override
                public void onBitmapLoaded( Bitmap bitmap, Picasso.LoadedFrom from ) {
                    mAlbumArt = bitmap;
                    listener.artLoaded( mAlbumArt );
                }

                @Override
                public void onBitmapFailed( Drawable errorDrawable ) {
                    onBitmapLoaded( mAlbumArt, null );

                }

                @Override
                public void onPrepareLoad( Drawable placeHolderDrawable ) {
                }

            } );
        }
    }


    @Override
    public String toString() {
        return "SongDao{" +
               "id=" + mValues.get( COLUMN_ID ) +
               ",title=" + mValues.get( COLUMN_SONG_TITLE ) +
               '}';
    }

    public interface AlbumArtLoadedListener {

        public void artLoaded( Bitmap bitmap );

    }
}
