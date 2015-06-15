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
package com.jassmp.SettingsActivity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.jassmp.GuiHelper.TypefaceHelper;
import com.jassmp.GuiHelper.UIElementsHelper;
import com.jassmp.JassMpDb.FolderTableAccessor;
import com.jassmp.Preferences.Preferences;
import com.jassmp.R;
import com.jassmp.Utils.Common;
import com.jassmp.WelcomeActivity.WelcomeActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class SettingsMusicFoldersDialog extends DialogFragment {

    private Context mContext;
    private Common  mApp;
    private boolean mWelcomeSetup = false;

    private RelativeLayout mUpLayout;
    private ImageView      mUpIcon;
    private TextView       mUpText;
    private TextView       mCurrentFolderText;

    private ListView mFoldersListView;

    private String mRootDir;
    private String mCurrentDir;

    private List<String>         mFileFolderNamesList;
    private List<String>         mFileFolderPathsList;
    private List<String>         mFileFolderSizesList;
    private Map<String, Boolean> mMusicFolders;

    @Override
    public Dialog onCreateDialog( Bundle onSavedInstanceState ) {
        AlertDialog.Builder builder = new AlertDialog.Builder( getActivity() );

        mContext = getActivity().getApplicationContext();
        mApp = (Common) mContext;
        View rootView = getActivity().getLayoutInflater().inflate( R.layout.fragment_folders_selection, null );

        mFoldersListView = (ListView) rootView.findViewById( R.id.folders_list_view );
        mFoldersListView.setFastScrollEnabled( true );
        mWelcomeSetup = getArguments().getBoolean( "com.jams.music.player.WELCOME" );

        mUpLayout = (RelativeLayout) rootView.findViewById( R.id.folders_up_layout );
        mUpIcon = (ImageView) rootView.findViewById( R.id.folders_up_icon );
        mUpText = (TextView) rootView.findViewById( R.id.folders_up_text );
        mCurrentFolderText = (TextView) rootView.findViewById( R.id.folders_current_directory_text );

        mUpText.setTypeface( TypefaceHelper.getTypeface( mContext, "Roboto-Regular" ) );
        mCurrentFolderText.setTypeface( TypefaceHelper.getTypeface( mContext, "Roboto-Regular" ) );

        mUpLayout.setOnClickListener( new View.OnClickListener() {

            @Override
            public void onClick( View v ) {
                try {
                    getDir( new File( mCurrentDir ).getParentFile().getCanonicalPath() );
                } catch( Exception e ) {
                    e.printStackTrace();
                }

            }

        } );

        if( mWelcomeSetup ) {
            mFoldersListView.setDivider( getResources().getDrawable( R.drawable.icon_list_divider_light ) );
            mUpIcon.setImageResource( R.drawable.up );
        } else {
            mUpIcon.setImageResource( UIElementsHelper.getIcon( mContext, "up" ) );

            switch( new Preferences( mApp ).getCurrentTheme() ) {
                case DARK:
                    mUpIcon.setImageResource( R.drawable.icon_list_divider_light );
                    break;
                case LIGHT:
                    mUpIcon.setImageResource( R.drawable.icon_list_divider );
                    break;
            }

        }

        mFoldersListView.setDividerHeight( 1 );
        mRootDir = Environment.getExternalStorageDirectory().getAbsolutePath().toString();
        mCurrentDir = mRootDir;
        mMusicFolders = FolderTableAccessor.getInstance( mApp ).getAllMusicFolderPaths();

        //Get the folder hierarchy of the selected folder.
        getDir( mRootDir );

        mFoldersListView.setOnItemClickListener( new OnItemClickListener() {

            @Override
            public void onItemClick( AdapterView<?> arg0, View arg1, int index, long arg3 ) {
                String newPath = mFileFolderPathsList.get( index );
                getDir( newPath );

            }

        } );

        builder.setTitle( R.string.select_music_folders );
        builder.setView( rootView );
        builder.setPositiveButton( R.string.ok, new DialogInterface.OnClickListener() {

            @Override
            public void onClick( DialogInterface dialog, int which ) {
                getActivity().finish();

                Intent intent = new Intent( mContext, WelcomeActivity.class );
                intent.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP );
                intent.putExtra( WelcomeActivity.INTENT_REFRESH_MUSIC_LIBRARY, true );
                mContext.startActivity( intent );

            }

        } );

        builder.setNegativeButton( R.string.cancel, new DialogInterface.OnClickListener() {

            @Override
            public void onClick( DialogInterface dialog, int which ) {
                dialog.dismiss();

            }

        } );

        return builder.create();
    }

    /**
     * Sets the current directory's text.
     */
    private void setCurrentDirText() {
        mCurrentFolderText.setText( mCurrentDir );
    }

    /**
     * Retrieves the folder hierarchy for the specified folder
     * (this method is NOT recursive and doesn't go into the parent
     * folder's subfolders.
     */
    private void getDir( String dirPath ) {

        mFileFolderNamesList = new ArrayList<String>();
        mFileFolderPathsList = new ArrayList<String>();
        mFileFolderSizesList = new ArrayList<String>();

        File f = new File( dirPath );
        File[] files = f.listFiles();
        Arrays.sort( files );

        if( files != null ) {

            for( int i = 0; i < files.length; i++ ) {

                File file = files[ i ];

                if( !file.isHidden() && file.canRead() ) {

                    if( file.isDirectory() ) {

						/*
                         * Starting with Android 4.2, /storage/emulated/legacy/...
						 * is a symlink that points to the actual directory where 
						 * the user's files are stored. We need to detect the 
						 * actual directory's file path here.
						 */
                        String filePath;
                        if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 ) {
                            filePath = getRealFilePath( file.getAbsolutePath() );
                        } else {
                            filePath = file.getAbsolutePath();
                        }

                        mFileFolderPathsList.add( filePath );
                        mFileFolderNamesList.add( file.getName() );

                        File[] listOfFiles = file.listFiles();

                        if( listOfFiles != null ) {
                            if( listOfFiles.length == 1 ) {
                                mFileFolderSizesList.add( "" + listOfFiles.length + " item" );
                            } else {
                                mFileFolderSizesList.add( "" + listOfFiles.length + " items" );
                            }

                        }

                    }

                }

            }

        }

        boolean dirChecked = false;

        //Get the directory and the parent dir.
        String concatatedString = "";
        int secondSlashIndex = dirPath.lastIndexOf( "/", dirPath.lastIndexOf( "/" ) - 1 );
        if( ( secondSlashIndex < dirPath.length() ) && secondSlashIndex != -1 ) {
            concatatedString = dirPath.substring( secondSlashIndex, dirPath.length() );
        }

        dirChecked = mMusicFolders.containsKey( concatatedString ) ? mMusicFolders.get( concatatedString ) : false;

        SettingsMultiselectAdapter mFoldersListViewAdapter = new SettingsMultiselectAdapter( getActivity(), this,
                                                                                             mWelcomeSetup,
                                                                                             dirChecked );

        mFoldersListView.setAdapter( mFoldersListViewAdapter );
        mFoldersListViewAdapter.notifyDataSetChanged();

        mCurrentDir = dirPath;
        setCurrentDirText();

    }

    /**
     * Resolves the /storage/emulated/legacy paths to
     * their true folder path representations. Required
     * for Nexuses and other devices with no SD card.
     */
    @SuppressLint("SdCardPath")
    private String getRealFilePath( String filePath ) {

        if( filePath.equals( "/storage/emulated/0" ) ||
            filePath.equals( "/storage/emulated/0/" ) ||
            filePath.equals( "/storage/emulated/legacy" ) ||
            filePath.equals( "/storage/emulated/legacy/" ) ||
            filePath.equals( "/storage/sdcard0" ) ||
            filePath.equals( "/storage/sdcard0/" ) ||
            filePath.equals( "/sdcard" ) ||
            filePath.equals( "/sdcard/" ) ||
            filePath.equals( "/mnt/sdcard" ) ||
            filePath.equals( "/mnt/sdcard/" ) ) {

            return Environment.getExternalStorageDirectory().toString();
        }

        return filePath;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if( isRemoving() ) {
            FolderTableAccessor.getInstance( mApp ).replaceMusicFolders( mMusicFolders );
        }

    }

    /*
     * Getter methods.
     */
    public Map<String, Boolean> getMusicFolders() {
        return mMusicFolders;
    }

    public List<String> getFileFolderNamesList() {
        return mFileFolderNamesList;
    }

    public List<String> getFileFolderSizesList() {
        return mFileFolderSizesList;
    }

    public List<String> getFileFolderPathsList() {
        return mFileFolderPathsList;
    }

}

