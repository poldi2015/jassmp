package com.jams.music.player.Utils;

import android.util.Log;

import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.mp3.MP3File;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;

import java.io.File;
import java.io.IOException;

/**
 * Used to access MP3 ID3 tags.
 * <p/>
 * Created by haberstu on 13.01.2015.
 */
public class AudioFileReader {

    private File mAudioFile;
    private              Tag   mTag         = null;
    private static final int[] RATE_MAPPING = { 0, 31, 95, 159, 223, 255 };

    public AudioFileReader( final File audioFile ) {
        mAudioFile = audioFile;
    }

    /**
     * @return 5-star rating (0-5)
     */
    public int getRating() {
        final int rating = getIntTag( FieldKey.RATING );
        if( rating > 0 ) {
            for( int i = 0; i < RATE_MAPPING.length; i++ ) {
                if( rating <= RATE_MAPPING[ i ] ) {
                    return i;
                }
            }
        }
        return 0;
    }

    /**
     * @return beats per minute value or 0
     */
    public int getBPM() {
        final int bpm = getIntTag( FieldKey.BPM );
        if( bpm > 0 ) {
            return bpm > 0 ? bpm : 0;
        } else {
            return 0;
        }
    }

    /**
     * Used internally to get the tag.
     *
     * @return The tag or null if file cannot be read or has not ID3v2 tag
     */
    private Tag getTag() {
        if( mTag != null ) {
            return mTag;
        }
        if( mAudioFile == null ) {
            return null;
        }
        final MP3File mp3File;
        try {
            mp3File = (MP3File) AudioFileIO.read( mAudioFile );
            if( mp3File == null ) {
                throw new IOException( "Cannot open file " + mAudioFile.getAbsolutePath() );
            }
        } catch( Exception e ) {
            Log.w( this.getClass().getName(), e.getMessage() );
            mAudioFile = null;
            return null;
        }

        if( !mp3File.hasID3v2Tag() ) {
            mAudioFile = null;
            return null;
        }

        return mTag = mp3File.getTag();
    }

    /**
     * Get integer value field.
     *
     * @param key the field key
     * @return The value or -1
     */
    private int getIntTag( final FieldKey key ) {
        final Tag tag = getTag();
        if( tag == null ) {
            return -1;
        }
        final String first = tag.getFirst( key );
        try {
            return first != null && !"".equals( first ) ? Integer.parseInt( first ) : 0;
        } catch( NumberFormatException e ) {
            return -1;
        }
    }
}
