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
import android.media.MediaMetadataRetriever;
import android.os.AsyncTask;
import android.os.PowerManager;
import android.widget.Toast;

import com.jassmp.Dao.SongCursorAdapter;
import com.jassmp.Dao.SongDao;
import com.jassmp.Helpers.FileExtensionFilter;
import com.jassmp.JassMpDb.DatabaseAccessor;
import com.jassmp.JassMpDb.FolderTableAccessor;
import com.jassmp.JassMpDb.SongTableAccessor;
import com.jassmp.MediaStore.MediaStoreSongIterator;
import com.jassmp.R;
import com.jassmp.Utils.Common;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class AsyncBuildLibraryTask extends AsyncTask<String, String, Void> {

    private Context                                 mContext;
    private Common                                  mApp;
    public  ArrayList<OnBuildLibraryProgressUpdate> mBuildLibraryProgressUpdate;

    private String                  mCurrentTask      = "";
    private int                     mOverallProgress  = 0;
    private HashMap<String, String> mFolderArtHashMap = new HashMap<String, String>();
    private MediaMetadataRetriever  mMMDR             = new MediaMetadataRetriever();

    private PowerManager.WakeLock mWakeLock;

    public AsyncBuildLibraryTask( final Context context ) {
        mContext = context;
        mApp = (Common) mContext;
        mBuildLibraryProgressUpdate = new ArrayList<OnBuildLibraryProgressUpdate>();
    }

    /**
     * Provides callback methods that expose this
     * AsyncTask's progress.
     *
     * @author Saravan Pantham
     */
    public interface OnBuildLibraryProgressUpdate {

        /**
         * Called when this AsyncTask begins executing
         * its doInBackground() method.
         */
        public void onStartBuildingLibrary();

        /**
         * Called whenever mOverall Progress has been updated.
         */
        public void onProgressUpdate( AsyncBuildLibraryTask task, String mCurrentTask, int overallProgress, int maxProgress, boolean mediaStoreTransferDone );

        /**
         * Called when this AsyncTask finishes executing
         * its onPostExecute() method.
         */
        public void onFinishBuildingLibrary( AsyncBuildLibraryTask task );

    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        mApp.setIsBuildingLibrary( true );
        mApp.setIsScanFinished( false );

        if( mBuildLibraryProgressUpdate != null ) {
            for( int i = 0; i < mBuildLibraryProgressUpdate.size(); i++ ) {
                if( mBuildLibraryProgressUpdate.get( i ) != null ) {
                    mBuildLibraryProgressUpdate.get( i ).onStartBuildingLibrary();
                }
            }
        }

        // Acquire a wakelock to prevent the CPU from sleeping while the process is running.
        final PowerManager powerManager = (PowerManager) mContext.getSystemService( Context.POWER_SERVICE );
        mWakeLock = powerManager.newWakeLock( PowerManager.PARTIAL_WAKE_LOCK,
                                              "com.jams.music.player.AsyncTasks.AsyncBuildLibraryTask" );
        mWakeLock.acquire();

    }

    @Override
    protected Void doInBackground( String... params ) {

		/* 
         * Get a cursor of songs from MediaStore. The cursor
		 * is limited by the folders that have been selected 
		 * by the user.
		 */
        mCurrentTask = mContext.getResources().getString( R.string.building_music_library );
        updateMediaDatabase( getMediaStoreSongIterator() );

        //Notify all listeners that the MediaStore transfer is complete.
        publishProgress( new String[]{ "MEDIASTORE_TRANSFER_COMPLETE" } );

        //Save album art paths for each song to the database.
        getAlbumArt();

        return null;
    }

    private MediaStoreSongIterator getMediaStoreSongIterator() {
        final FolderTableAccessor musicFolders = DatabaseAccessor.getInstance( mApp ).getFolderTableAccessor();
        final LinkedHashMap<String, Boolean> folderFilter = musicFolders.hasMusicFolders()
                                                            ? musicFolders.getAllMusicFolderPaths() : null;
        return new MediaStoreSongIterator( mContext, folderFilter );
    }

    private void updateMediaDatabase( final MediaStoreSongIterator mediaStoreSongIterator ) {
        //Tracks the progress of this method.
        int progressStepSize = 0;
        if( mediaStoreSongIterator.getNumberOfSongs() != 0 ) {
            progressStepSize = 250000 / mediaStoreSongIterator.getNumberOfSongs();
        } else {
            progressStepSize = 250000;
        }

        SongDao song;
        final DatabaseAccessor databaseAccessor = DatabaseAccessor.getInstance( mContext );
        // TODO: Clear database
        while( ( song = mediaStoreSongIterator.getNext() ) != null ) {
            databaseAccessor.updateSong( song );

            mOverallProgress += progressStepSize;
            publishProgress();
        }
        databaseAccessor.commit();
    }


    /**
     * Loops through a cursor of all local songs in
     * the library and searches for their album art.
     */
    private void getAlbumArt() {
        final SongTableAccessor songTableAccessor = SongTableAccessor.getInstance( mApp );
        final SongCursorAdapter songCursorAdapter = songTableAccessor.getAllSongsCursorAdapter( null, null );
        final int count = songCursorAdapter.getCount();
        if( count <= 0 ) {
            return;
        }

        mCurrentTask = mContext.getResources().getString( R.string.building_album_art );
        int subProgress = 750000 / count;
        DatabaseAccessor.getInstance( mApp ).getDatabase().beginTransactionNonExclusive();

        for( int position = 0; position < count; position++ ) {
            final SongDao dao = songCursorAdapter.getDaoFromCursor( position );
            mOverallProgress += subProgress;
            publishProgress();
            songTableAccessor.setArtworkPath( dao, getArtwork( dao.getFilePath() ) );
        }
        songCursorAdapter.close();
        songTableAccessor.commit();
    }

    private String getArtwork( final String songFilePath ) {
        if( mApp.getSharedPreferences().getInt( "ALBUM_ART_SOURCE", 0 ) == 0
            || mApp.getSharedPreferences().getInt( "ALBUM_ART_SOURCE", 0 ) == 1 ) {
            return getEmbeddedArtwork( songFilePath );
        } else {
            return getArtworkFromFolder( songFilePath );
        }
    }

    /**
     * Searchs for folder art within the specified file's
     * parent folder. Returns a path string to the artwork
     * image file if it exists. Returns an empty string
     * otherwise.
     */
    public String getArtworkFromFolder( String filePath ) {

        File file = new File( filePath );
        if( !file.exists() ) {
            return "";

        } else {
            //Create a File that points to the parent directory of the album.
            File directoryFile = file.getParentFile();
            String directoryPath = "";
            String albumArtPath = "";
            try {
                directoryPath = directoryFile.getCanonicalPath();
            } catch( IOException e1 ) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }

            //Check if album art was already found in this directory.
            if( mFolderArtHashMap.containsKey( directoryPath ) ) {
                return mFolderArtHashMap.get( directoryPath );
            }

            //Get a list of images in the album's folder.
            FileExtensionFilter IMAGES_FILTER = new FileExtensionFilter(
                    new String[]{ ".jpg", ".jpeg", ".png", ".gif" } );
            File[] folderList = directoryFile.listFiles( IMAGES_FILTER );

            //Check if any image files were found in the folder.
            if( folderList.length == 0 ) {
                //No images found.
                return "";

            } else {

                //Loop through the list of image files. Use the first jpeg file if it's found.
                for( int i = 0; i < folderList.length; i++ ) {

                    try {
                        albumArtPath = folderList[ i ].getCanonicalPath();
                        if( albumArtPath.endsWith( "jpg" ) || albumArtPath.endsWith( "jpeg" ) ) {

                            //Add the folder's album art file to the hash.
                            mFolderArtHashMap.put( directoryPath, albumArtPath );
                            return albumArtPath;
                        }

                    } catch( Exception e ) {
                        //Skip the file if it's corrupted or unreadable.
                        continue;
                    }

                }

                //If an image was not found, check for gif or png files (lower priority).
                for( int i = 0; i < folderList.length; i++ ) {

                    try {
                        albumArtPath = folderList[ i ].getCanonicalPath();
                        if( albumArtPath.endsWith( "png" ) || albumArtPath.endsWith( "gif" ) ) {

                            //Add the folder's album art file to the hash.
                            mFolderArtHashMap.put( directoryPath, albumArtPath );
                            return albumArtPath;
                        }

                    } catch( Exception e ) {
                        //Skip the file if it's corrupted or unreadable.
                        continue;
                    }

                }

            }

            //Add the folder's album art file to the hash.
            mFolderArtHashMap.put( directoryPath, albumArtPath );
            return "";
        }

    }

    /**
     * Searchs for embedded art within the specified file.
     * Returns a path string to the artwork if it exists.
     * Returns an empty string otherwise.
     */
    public String getEmbeddedArtwork( String filePath ) {
        File file = new File( filePath );
        if( !file.exists() ) {
            if( mApp.getSharedPreferences().getInt( "ALBUM_ART_SOURCE", 0 ) == 0 ) {
                return getArtworkFromFolder( filePath );
            } else {
                return "";
            }

        } else {
            mMMDR.setDataSource( filePath );
            byte[] embeddedArt = mMMDR.getEmbeddedPicture();

            if( embeddedArt != null ) {
                return "byte://" + filePath;
            } else {
                if( mApp.getSharedPreferences().getInt( "ALBUM_ART_SOURCE", 0 ) == 0 ) {
                    return getArtworkFromFolder( filePath );
                } else {
                    return "";
                }

            }

        }

    }

    @Override
    protected void onProgressUpdate( String... progressParams ) {
        super.onProgressUpdate( progressParams );

        if( progressParams.length > 0 && progressParams[ 0 ].equals( "MEDIASTORE_TRANSFER_COMPLETE" ) ) {
            for( int i = 0; i < mBuildLibraryProgressUpdate.size(); i++ ) {
                if( mBuildLibraryProgressUpdate.get( i ) != null ) {
                    mBuildLibraryProgressUpdate.get( i )
                                               .onProgressUpdate( this, mCurrentTask, mOverallProgress, 1000000, true );
                }
            }

            return;
        }

        if( mBuildLibraryProgressUpdate != null ) {
            for( int i = 0; i < mBuildLibraryProgressUpdate.size(); i++ ) {
                if( mBuildLibraryProgressUpdate.get( i ) != null ) {
                    mBuildLibraryProgressUpdate.get( i )
                                               .onProgressUpdate( this, mCurrentTask, mOverallProgress, 1000000,
                                                                  false );
                }
            }
        }

    }

    @Override
    protected void onPostExecute( Void arg0 ) {
        //Release the wakelock.
        mWakeLock.release();
        mApp.setIsBuildingLibrary( false );
        mApp.setIsScanFinished( true );

        Toast.makeText( mContext, R.string.finished_scanning_album_art, Toast.LENGTH_LONG ).show();

        if( mBuildLibraryProgressUpdate != null ) {
            for( int i = 0; i < mBuildLibraryProgressUpdate.size(); i++ ) {
                if( mBuildLibraryProgressUpdate.get( i ) != null ) {
                    mBuildLibraryProgressUpdate.get( i ).onFinishBuildingLibrary( this );
                }
            }
        }

    }

    /**
     * Setter methods.
     */
    public void setOnBuildLibraryProgressUpdate( OnBuildLibraryProgressUpdate buildLibraryProgressUpdate ) {
        if( buildLibraryProgressUpdate != null ) {
            mBuildLibraryProgressUpdate.add( buildLibraryProgressUpdate );
        }
    }

}
