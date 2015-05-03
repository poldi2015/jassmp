package com.jams.music.player.DBHelpers;

import android.content.Context;

/**
 * Created by poldi on 02.05.15.
 */
public class MusicLibraryAccessor extends AbstractDBAccessor {

    //
    // defines

    public static final String TABLE_NAME = "MusicLibraryTable";

    public static final String SONG_TITLE            = "title";
    public static final String SONG_ARTIST           = "artist";
    public static final String SONG_ALBUM            = "album";
    public static final String SONG_ALBUM_ARTIST     = "album_artist";
    public static final String SONG_DURATION         = "duration";
    public static final String SAVED_POSITION        = "saved_position";
    public static final String SONG_FILE_PATH        = "file_path";
    public static final String SONG_TRACK_NUMBER     = "track_number";
    public static final String SONG_GENRE            = "genre";
    public static final String SONG_PLAY_COUNT       = "play_count";
    public static final String SONG_YEAR             = "year";
    public static final String SONG_RATING           = "rating";
    public static final String BLACKLIST_STATUS      = "blacklist_status";
    public static final String ADDED_TIMESTAMP       = "added_timestamp";
    public static final String LAST_PLAYED_TIMESTAMP = "last_played_timestamp";
    public static final String SONG_BPM              = "bpm";
    public static final String SONG_ALBUM_ART_PATH   = "album_art_path";
    public static final String ARTIST_ART_LOCATION   = "artist_art_location";


    public static final Column COLUMN_SONG_TITLE            = new Column( SONG_TITLE );
    public static final Column COLUMN_SONG_ARTIST           = new Column( SONG_ARTIST );
    public static final Column COLUMN_SONG_ALBUM            = new Column( SONG_ALBUM );
    public static final Column COLUMN_SONG_ALBUM_ARTIST     = new Column( SONG_ALBUM_ARTIST );
    public static final Column COLUMN_SONG_DURATION         = new Column( SONG_DURATION, ColumnType.INTEGER, "0" );
    public static final Column COLUMN_SONG_FILE_PATH        = new Column( SONG_FILE_PATH );
    public static final Column COLUMN_SONG_TRACK_NUMBER     = new Column( SONG_TRACK_NUMBER, ColumnType.INTEGER, "0" );
    public static final Column COLUMN_SONG_GENRE            = new Column( SONG_GENRE );
    public static final Column COLUMN_SONG_PLAY_COUNT       = new Column( SONG_PLAY_COUNT, ColumnType.INTEGER, "0" );
    public static final Column COLUMN_SONG_SAVED_POSITION   = new Column( SAVED_POSITION, ColumnType.INTEGER, "0" );
    public static final Column COLUMN_SONG_YEAR             = new Column( SONG_YEAR );
    public static final Column COLUMN_SONG_RATING           = new Column( SONG_RATING, ColumnType.INTEGER, "0" );
    public static final Column COLUMN_BLACKLIST_STATUS      = new Column( BLACKLIST_STATUS );
    public static final Column COLUMN_ADDED_TIMESTAMP       = new Column( ADDED_TIMESTAMP );
    public static final Column COLUMN_SONG_BPM              = new Column( SONG_BPM );
    public static final Column COLUMN_LAST_PLAYED_TIMESTAMP = new Column( LAST_PLAYED_TIMESTAMP );
    public static final Column COLUMN_SONG_ALBUM_ART_PATH   = new Column( SONG_ALBUM_ART_PATH );
    public static final Column COLUMN_ARTIST_ART_LOCATION   = new Column( ARTIST_ART_LOCATION );

    public static final Column[] COLUMNS = { COLUMN_ID, COLUMN_SONG_TITLE, COLUMN_SONG_ARTIST, COLUMN_SONG_ALBUM,
                                             COLUMN_SONG_ALBUM_ARTIST, COLUMN_SONG_DURATION, COLUMN_SONG_FILE_PATH,
                                             COLUMN_SONG_TRACK_NUMBER, COLUMN_SONG_GENRE, COLUMN_SONG_PLAY_COUNT,
                                             COLUMN_SONG_SAVED_POSITION, COLUMN_SONG_YEAR, COLUMN_SONG_RATING,
                                             COLUMN_BLACKLIST_STATUS, COLUMN_ADDED_TIMESTAMP, COLUMN_SONG_BPM,
                                             COLUMN_LAST_PLAYED_TIMESTAMP, COLUMN_SONG_ALBUM_ART_PATH,
                                             COLUMN_ARTIST_ART_LOCATION };

    //
    // private members

    private static MusicLibraryAccessor sInstance = null;

    public MusicLibraryAccessor( Context context ) {
        super( TABLE_NAME, context );
    }

    public static synchronized MusicLibraryAccessor getInstance( Context context ) {
        if( sInstance == null ) {
            sInstance = new MusicLibraryAccessor( context.getApplicationContext() );
        }

        return sInstance;
    }

    @Override
    protected Column[] getTableColumns() {
        return COLUMNS;
    }

    @Override
    protected Column[] getAdditionalColumns( int version ) {
        return new Column[ 0 ];
    }

}
