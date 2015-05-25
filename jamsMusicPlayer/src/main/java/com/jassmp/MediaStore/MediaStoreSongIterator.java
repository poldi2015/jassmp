/*
 * Copyright (C) 2014 Saravan Pantham
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jassmp.MediaStore;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import com.jassmp.Dao.Column;
import com.jassmp.Dao.SongDao;
import com.jassmp.Utils.AudioFileReader;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MediaStoreSongIterator {

    public static final List<DefaultValueMapper> COLUMN_MEDIA_MAPPER = Collections.unmodifiableList(
            new ArrayList<DefaultValueMapper>() {
                private List<String> mProjection = new ArrayList<String>();

                {
                    add( SongDao.COLUMN_SONG_TITLE, MediaStore.Audio.Media.TITLE );
                    add( SongDao.COLUMN_SONG_ARTIST, MediaStore.Audio.Media.ARTIST );
                    add( SongDao.COLUMN_SONG_ALBUM, MediaStore.Audio.Media.ALBUM );
                    add( SongDao.COLUMN_SONG_DURATION, MediaStore.Audio.Media.DURATION );
                    add( new TrackValueMapper( SongDao.COLUMN_SONG_TRACK_NUMBER, MediaStore.Audio.Media.TRACK ) );
                    add( SongDao.COLUMN_SONG_YEAR, MediaStore.Audio.Media.YEAR );
                    add( SongDao.COLUMN_ADDED_TIMESTAMP, MediaStore.Audio.Media.DATE_ADDED );
                    add( SongDao.COLUMN_SONG_FILE_PATH, MediaStore.Audio.Media.DATA );
                    add( new BpmValueMapper( SongDao.COLUMN_SONG_BPM, MediaStore.Audio.Media.DATA ) );
                    add( new RatingValueMapper( SongDao.COLUMN_SONG_RATING, MediaStore.Audio.Media.DATA ) );
                    add( new GenreValueMapper( SongDao.COLUMN_SONG_GENRE ) );
                    add( new ArtValueMapper( SongDao.COLUMN_SONG_ALBUM_ART_PATH, MediaStore.Audio.Media.ALBUM_ID ) );

                    buildProjection();
                }

                private void add( Column column, String mediaKey ) {
                    add( new DefaultValueMapper( column, mediaKey ) );
                }

                @Override
                public boolean add( final DefaultValueMapper mapper ) {
                    if( mapper.mediaKey != null && !mProjection.contains( mapper.mediaKey ) ) {
                        mProjection.add( mapper.mediaKey );
                    }
                    return super.add( mapper );
                }

                private void buildProjection() {
                    mProjection.add( MediaStore.Audio.Media._ID );
                    ID_INDEX = mProjection.size() - 1;
                    PROJECTION = mProjection.toArray( new String[ mProjection.size() ] );
                    mProjection = null;
                }
            } );

    public static  int      ID_INDEX;
    private static String[] PROJECTION;

    //
    // private members

    private final Context mContext;
    private final Cursor  mSongsCursor;
    private AudioFileReader mCachedAudioFileReader     = null;
    private String          mCachedAudioFileReaderPath = null;

    public MediaStoreSongIterator( final Context context, final LinkedHashMap<String, Boolean> folderFilter ) {
        mContext = context;
        final String selection = buildMusicFoldersSelection( folderFilter );
        mSongsCursor = getSongCursor( mContext, selection, PROJECTION, null );
        if( mSongsCursor == null ) {
            return;
        }
        if( !mSongsCursor.moveToFirst() ) {
            mSongsCursor.close();
        }
    }

    public Context getContext() {
        return mContext;
    }

    public int getNumberOfSongs() {
        return mSongsCursor.getCount();
    }

    public SongDao getNext() {
        if( mSongsCursor == null || mSongsCursor.isClosed() ) {
            // After end
            return null;
        }

        // Process song
        final Map<Column, Object> values = new HashMap<Column, Object>();
        int index = 0;
        String lastMediaKey = null;
        String value = null;
        for( final DefaultValueMapper mediaValueMapper : COLUMN_MEDIA_MAPPER ) {
            if( mediaValueMapper.mediaKey != null && !mediaValueMapper.mediaKey.equals( lastMediaKey ) ) {
                // New value
                value = mSongsCursor.getString( index++ );
            } else if( mediaValueMapper.mediaKey == null ) {
                // No value
                value = null;
            } // else reuse last value
            values.put( mediaValueMapper.column, mediaValueMapper.map( this, mSongsCursor, value ) );
            lastMediaKey = mediaValueMapper.mediaKey;
        }

        // Move to next
        if( !mSongsCursor.moveToNext() ) {
            mCachedAudioFileReader = null;
            mCachedAudioFileReaderPath = null;
            mSongsCursor.close();
        }

        return new SongDao( values );
    }

    private Cursor getSongCursor( Context context, String selection, String[] projection, String sortOrder ) {
        ContentResolver contentResolver = context.getContentResolver();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        if( selection == null ) {
            selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0";
        }

        return contentResolver.query( uri, projection, selection, null, sortOrder );
    }

    /**
     * Constructs the selection string for limiting the MediaStore
     * query to specific music folders.
     */
    private String buildMusicFoldersSelection( final LinkedHashMap<String, Boolean> entries ) {
        if( entries == null ) {
            return null;
        }
        String mediaStoreSelection = MediaStore.Audio.Media.IS_MUSIC + "!=0 AND (";
        boolean first = true;

        for( final Map.Entry<String, Boolean> entry : entries.entrySet() ) {
            final String path = entry.getKey();
            final boolean include = entry.getValue();
            //Set the correct LIKE clause.
            String likeClause = include ? " LIKE " : " NOT LIKE ";
            if( !first ) {
                mediaStoreSelection += ( include ? " OR " : " AND " );
            } else {
                first = false;
            }

            mediaStoreSelection += MediaStore.Audio.Media.DATA + likeClause + "'%" + path + "/%'";
        }


        //Append the closing parentheses.
        mediaStoreSelection += ")";

        return mediaStoreSelection;
    }

    public AudioFileReader getAudioFileReader( final String songFilePath ) {
        if( songFilePath == null ) {
            return null;
        }
        if( songFilePath.equals( mCachedAudioFileReaderPath ) ) {
            return mCachedAudioFileReader;
        }
        return mCachedAudioFileReader = new AudioFileReader( new File( mCachedAudioFileReaderPath = songFilePath ) );
    }

}
