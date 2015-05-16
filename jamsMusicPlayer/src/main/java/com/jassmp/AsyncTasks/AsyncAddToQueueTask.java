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
package com.jassmp.AsyncTasks;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.widget.Toast;

import com.jassmp.DBHelpers.DBAccessHelper;
import com.jassmp.NowPlayingActivity.NowPlayingActivity;
import com.jassmp.R;
import com.jassmp.Services.AudioPlaybackService;
import com.jassmp.Utils.Common;

public class AsyncAddToQueueTask extends AsyncTask<Boolean, Integer, Boolean> {

    private Context mContext;
    private Common  mApp;

    private String mArtistName;
    private String mAlbumName;
    private String mSongTitle;
    private String mGenreName;

    private Fragment mFragment;
    private Cursor   mCursor;
    private String   mEnqueueType;
    private int     originalPlaybackIndecesSize = 0;
    private boolean mPlayNext                   = false;
    private String  mPlayingNext                = "";

    public AsyncAddToQueueTask( Context context, Fragment fragment, String enqueueType, String genreName, String artistName, String albumName, String songTitle ) {

        mContext = context;
        mApp = (Common) mContext;

        mArtistName = artistName;
        mAlbumName = albumName;
        mSongTitle = songTitle;
        mGenreName = genreName;

        mFragment = fragment;
        mEnqueueType = enqueueType;

        if( mApp.getAudioPlaybackService().getPlaybackIndecesList() != null ) {
            originalPlaybackIndecesSize = mApp.getAudioPlaybackService().getPlaybackIndecesList().size();
        }

    }

    @Override
    protected Boolean doInBackground( Boolean... params ) {
        //Specifies if the user is trying to add song(s) to play next.
        if( params.length > 0 ) {
            mPlayNext = params[ 0 ];
        }


        //Escape any rogue apostrophes.
        if( mArtistName != null && mArtistName.contains( "'" ) ) {
            mArtistName = mArtistName.replace( "'", "''" );
        }

        if( mAlbumName != null && mAlbumName.contains( "'" ) ) {
            mAlbumName = mAlbumName.replace( "'", "''" );
        }

        if( mSongTitle != null && mSongTitle.contains( "'" ) ) {
            mSongTitle = mSongTitle.replace( "'", "''" );
        }

        if( mGenreName != null && mGenreName.contains( "''" ) ) {
            mGenreName = mGenreName.replace( "'", "''" );
        }

        //Fetch the cursor based on the type of set of songs that are being enqueued.
        assignCursor();

        //Check if the service is currently active.
        if( mApp.isServiceRunning() ) {

            if( mPlayNext ) {
                /* Loop through the mCursor of the songs that will be enqueued and add the
                 * loop's counter value to the size of the current mCursor. This will add
				 * the additional mCursor indeces of the new, merged mCursor to playbackIndecesList. 
				 * The new indeces must be placed after the current song's index.
				 */
                int playNextIndex = mApp.getAudioPlaybackService().getCurrentSongIndex() + 1;

                for( int i = 0; i < mCursor.getCount(); i++ ) {
                    mApp.getAudioPlaybackService()
                        .getPlaybackIndecesList()
                        .add( playNextIndex + i, mApp.getAudioPlaybackService().getCursor().getCount() + i );
                }

            } else {
                /* Loop through the mCursor of the songs that will be enqueued and add the
                 * loop's counter value to the size of the current mCursor. This will add
				 * the additional mCursor indeces of the new, merged mCursor to playbackIndecesList.
				 */
                for( int i = 0; i < mCursor.getCount(); i++ ) {
                    mApp.getAudioPlaybackService()
                        .getPlaybackIndecesList()
                        .add( mApp.getAudioPlaybackService().getCursor().getCount() + i );
                }

            }

            mApp.getAudioPlaybackService().enqueueCursor( mCursor, mPlayNext );

        } else {
            //The service doesn't seem to be running. We'll explicitly stop it, just in case, and then launch NowPlayingActivity.class.
            Intent serviceIntent = new Intent( mContext, AudioPlaybackService.class );
            mContext.stopService( serviceIntent );

            publishProgress( 0 );
        }

        publishProgress( 1 );

        return true;
    }

    //Retrieves and assigns the cursor based on the set of song(s) that are being enqueued.
    private void assignCursor() {

        DBAccessHelper dbHelper = new DBAccessHelper( mContext );

        if( mEnqueueType.equals( "SONG" ) ) {
            String selection = DBAccessHelper.SONG_ARTIST + "=" + "'" + mArtistName + "'" + " AND "
                               + DBAccessHelper.SONG_ALBUM + "=" + "'" + mAlbumName + "'" + " AND "
                               + DBAccessHelper.SONG_TITLE + "=" + "'" + mSongTitle + "'";

            mCursor = dbHelper.getReadableDatabase()
                              .query( DBAccessHelper.MUSIC_LIBRARY_TABLE, null, selection, null, null, null,
                                      DBAccessHelper.SONG_TITLE + " ASC" );

            mPlayingNext = mSongTitle;
        } else if( mEnqueueType.equals( "ARTIST" ) ) {
            String selection = DBAccessHelper.SONG_ARTIST + "=" + "'" + mArtistName + "'";


            mCursor = dbHelper.getReadableDatabase()
                              .query( DBAccessHelper.MUSIC_LIBRARY_TABLE, null, selection, null, null, null,
                                      DBAccessHelper.SONG_ALBUM + " ASC" + ", " + DBAccessHelper.SONG_TRACK_NUMBER
                                      + "*1 ASC" );

            mPlayingNext = mArtistName;
        } else if( mEnqueueType.equals( "ALBUM" ) ) {

            String selection = DBAccessHelper.SONG_ARTIST + "=" + "'" + mArtistName + "'" + " AND "
                               + DBAccessHelper.SONG_ALBUM + "=" + "'" + mAlbumName + "'";


            mCursor = dbHelper.getReadableDatabase()
                              .query( DBAccessHelper.MUSIC_LIBRARY_TABLE, null, selection, null, null, null,
                                      DBAccessHelper.SONG_TRACK_NUMBER + "*1 ASC" );

            mPlayingNext = mAlbumName;
        } else if( mEnqueueType.equals( "GENRE" ) ) {

            String selection = DBAccessHelper.SONG_GENRE + "=" + "'" + mGenreName + "'";

            mCursor = dbHelper.getReadableDatabase()
                              .query( DBAccessHelper.MUSIC_LIBRARY_TABLE, null, selection, null, null, null,
                                      DBAccessHelper.SONG_ALBUM + " ASC, " +
                                      DBAccessHelper.SONG_TRACK_NUMBER + "*1 ASC" );

            mPlayingNext = mGenreName;
        }
    }

    @Override
    protected void onProgressUpdate( Integer... values ) {
        super.onProgressUpdate( values );
        int value = values[ 0 ];

        switch( value ) {
            case 0:
                Intent intent = new Intent( mContext, NowPlayingActivity.class );

                //Get the parameters for the first song.
                if( mCursor.getCount() > 0 ) {
                    mCursor.moveToFirst();

                    if( mEnqueueType.equals( "ARTIST" ) ) {
                        intent.putExtra( "PLAY_ALL", "ARTIST" );
                        intent.putExtra( "CALLING_FRAGMENT", "ARTISTS_FLIPPED_FRAGMENT" );
                    } else if( mEnqueueType.equals( "ALBUM" ) ) {
                        intent.putExtra( "PLAY_ALL", "ALBUM" );
                        intent.putExtra( "CALLING_FRAGMENT", "ALBUMS_FLIPPED_FRAGMENT" );
                    } else if( mEnqueueType.equals( "GENRE" ) ) {
                        intent.putExtra( "PLAY_ALL", "GENRE" );
                        intent.putExtra( "CALLING_FRAGMENT", "GENRES_FLIPPED_FRAGMENT" );
                    } else if( mEnqueueType.equals( "SONG" ) ) {
                        intent.putExtra( "CALLING_FRAGMENT", "SONGS_FRAGMENT" );
                        intent.putExtra( "SEARCHED", true );
                    }

                    intent.putExtra( "SELECTED_SONG_DURATION",
                                     mCursor.getString( mCursor.getColumnIndex( DBAccessHelper.SONG_DURATION ) ) );
                    intent.putExtra( "SELECTED_SONG_TITLE",
                                     mCursor.getString( mCursor.getColumnIndex( DBAccessHelper.SONG_TITLE ) ) );
                    intent.putExtra( "SELECTED_SONG_ARTIST",
                                     mCursor.getString( mCursor.getColumnIndex( DBAccessHelper.SONG_ARTIST ) ) );
                    intent.putExtra( "SELECTED_SONG_ALBUM",
                                     mCursor.getString( mCursor.getColumnIndex( DBAccessHelper.SONG_ALBUM ) ) );
                    intent.putExtra( "SONG_SELECTED_INDEX", 0 );
                    intent.putExtra( "SELECTED_SONG_DATA_URI",
                                     mCursor.getString( mCursor.getColumnIndex( DBAccessHelper.SONG_FILE_PATH ) ) );
                    intent.putExtra( "SELECTED_SONG_GENRE",
                                     mCursor.getString( mCursor.getColumnIndex( DBAccessHelper.SONG_GENRE ) ) );
                    intent.putExtra( "NEW_PLAYLIST", true );
                    intent.putExtra( "NUMBER_SONGS", mCursor.getCount() );
                    intent.putExtra( "CALLED_FROM_FOOTER", false );

                } else {
                    Toast.makeText( mContext, R.string.error_occurred, Toast.LENGTH_LONG ).show();
                    break;
                }

                mFragment.getActivity().startActivity( intent );
                mFragment.getActivity()
                         .overridePendingTransition( R.anim.slide_in_from_right, R.anim.slide_out_to_left );
                break;
            case 1:
                int numberOfSongs = mCursor.getCount();
                String toastMessage;
                if( numberOfSongs == 1 ) {
                    if( mPlayNext ) {
                        toastMessage = mPlayingNext + " " + mContext.getResources()
                                                                    .getString( R.string.will_be_played_next );
                    } else {
                        toastMessage = numberOfSongs + " " + mContext.getResources()
                                                                     .getString( R.string.song_enqueued_toast );
                    }

                } else {
                    if( mPlayNext ) {
                        toastMessage = mPlayingNext + " " + mContext.getResources()
                                                                    .getString( R.string.will_be_played_next );
                    } else {
                        toastMessage = numberOfSongs + " " + mContext.getResources()
                                                                     .getString( R.string.songs_enqueued_toast );
                    }

                }

                Toast.makeText( mContext, toastMessage, Toast.LENGTH_SHORT ).show();
                break;
        }

    }

    @Override
    protected void onPostExecute( Boolean result ) {
        super.onPostExecute( result );

        //Send out a broadcast that loads the new queue across the app.
        Intent intent = new Intent( "com.jams.music.player.NEW_SONG_UPDATE_UI" );
        intent.putExtra( "MESSAGE", "com.jams.music.player.NEW_SONG_UPDATE_UI" );
        intent.putExtra( "INIT_QUEUE_DRAWER_ADAPTER", true );

        //Start preparing the next song if the current song is the last track.
        if( mApp.getAudioPlaybackService().getCurrentSongIndex() == ( originalPlaybackIndecesSize - 1 ) ) {

            //Check if the service is running.
            if( mApp.isServiceRunning() ) {
                mApp.getAudioPlaybackService().prepareAlternateMediaPlayer();

            }

        }

    }

}
