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


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;

import com.jassmp.Dialogs.AlbumArtSourceDialog;
import com.jassmp.Dialogs.CustomizeScreensDialog;
import com.jassmp.Dialogs.NowPlayingColorSchemesDialog;
import com.jassmp.Dialogs.ScanFrequencyDialog;
import com.jassmp.R;
import com.jassmp.Utils.Common;

/**
 * **********************************************************************
 * This class displays a dummy activity which fires up a FragmentDialog
 * from PreferencesActivity.
 *
 * @author Saravan Pantham
 *         ***********************************************************************
 */
public class PreferenceDialogLauncherActivity extends FragmentActivity {

    private Context mContext;
    private Common  mApp;

    @Override
    public void onCreate( Bundle savedInstanceState ) {

        mContext = this;
        mApp = (Common) mContext.getApplicationContext();

        if( mApp.getCurrentTheme() == Common.DARK_THEME ) {
            this.setTheme( R.style.AppThemeTransparent );
        } else {
            this.setTheme( R.style.AppThemeTransparentLight );
        }

        super.onCreate( savedInstanceState );

        //Get the index that specifies which dialog to launch.
        int index = getIntent().getExtras().getInt( "INDEX" );

        if( index == 0 ) {
        } else if( index == 1 ) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            NowPlayingColorSchemesDialog appThemeDialog = new NowPlayingColorSchemesDialog();
            //appThemeDialog.show(ft, "colorSchemesDialog");
        } else if( index == 2 ) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            CustomizeScreensDialog screensDialog = new CustomizeScreensDialog();
            screensDialog.show( ft, "customizeScreensDialog" );
        } else if( index == 4 ) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            AlbumArtSourceDialog albumArtSourceDialog = new AlbumArtSourceDialog();
            albumArtSourceDialog.show( ft, "albumArtSourceDialog" );
        } else if( index == 5 ) {


        } else if( index == 6 ) {
            //Seting the "REBUILD_LIBRARY" flag to true will force MainActivity to rescan the folders.
            mApp.getSharedPreferences().edit().putBoolean( "REBUILD_LIBRARY", true ).commit();

            //Restart the app.
            Intent i = getBaseContext().getPackageManager()
                                       .getLaunchIntentForPackage( getBaseContext().getPackageName() );
            i.addFlags( Intent.FLAG_ACTIVITY_CLEAR_TOP );
            finish();
            startActivity( i );

        } else if( index == 7 ) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            Bundle bundle = new Bundle();
            bundle.putBoolean( "CALLED_FROM_WELCOME", false );
            ScanFrequencyDialog scanFrequencyDialog = new ScanFrequencyDialog();
            scanFrequencyDialog.setArguments( bundle );
            scanFrequencyDialog.show( ft, "scanFrequencyDialog" );

        } else if( index == 9 ) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            Bundle bundle = new Bundle();
            bundle.putBoolean( "CALLED_FROM_WELCOME", false );
            ScanFrequencyDialog scanFrequencyDialog = new ScanFrequencyDialog();
            scanFrequencyDialog.setArguments( bundle );
            scanFrequencyDialog.show( ft, "scanFrequencyDialog" );

        }
    }

}
