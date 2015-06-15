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

import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.jassmp.Dialogs.NowPlayingColorSchemesDialog;
import com.jassmp.GuiHelper.UIElementsHelper;
import com.jassmp.Preferences.Preferences;
import com.jassmp.Preferences.Theme;
import com.jassmp.R;
import com.jassmp.Utils.Common;

/**
 * @author Saravan Pantham
 */
public class SettingsAppearanceFragment extends PreferenceFragment {

    private Context mContext;
    private Common  mApp;

    private View     mRootView;
    private ListView mListView;

    private Preference         mAppThemePreference;
    private Preference         mColorPreference;
    private CheckBoxPreference mLockscreenControlsPreference;
    private Preferences mPreferences = null;

    @Override
    public void onCreate( Bundle onSavedInstanceState ) {
        super.onCreate( onSavedInstanceState );
        addPreferencesFromResource( R.xml.settings_appearance );

    }

    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle onSavedInstanceState ) {
        mRootView = super.onCreateView( inflater, container, onSavedInstanceState );

        mContext = getActivity().getApplicationContext();
        mApp = (Common) mContext;
        mPreferences = new Preferences( mContext );
        mListView = (ListView) mRootView.findViewById( android.R.id.list );

        //Set the ActionBar background and text color.
        applyKitKatTranslucency();
        getActivity().getActionBar().setTitle( R.string.settings );
        int titleId = getResources().getIdentifier( "action_bar_title", "id", "android" );
        TextView actionBarText = (TextView) getActivity().findViewById( titleId );
        actionBarText.setTextColor( 0xFFFFFFFF );

        mAppThemePreference = getPreferenceManager().findPreference( "preference_key_app_theme" );
        mColorPreference = getPreferenceManager().findPreference( "preference_key_player_color_scheme" );
        mLockscreenControlsPreference = (CheckBoxPreference) getPreferenceManager().findPreference(
                "preference_key_lockscreen_controls" );

        //Apply the click listeners.
        mAppThemePreference.setOnPreferenceClickListener( mAppThemeClickListener );
        mColorPreference.setOnPreferenceClickListener( colorClickListener );
        mLockscreenControlsPreference.setOnPreferenceChangeListener( lockscreenControlsClickListener );

        return mRootView;
    }

    /**
     * Applies KitKat specific translucency.
     */
    private void applyKitKatTranslucency() {
        if( Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT ) {

            //Calculate ActionBar and navigation bar height.
            TypedValue tv = new TypedValue();
            int actionBarHeight = 0;
            if( getActivity().getTheme().resolveAttribute( android.R.attr.actionBarSize, tv, true ) ) {
                actionBarHeight = TypedValue.complexToDimensionPixelSize( tv.data, getResources().getDisplayMetrics() );
            }

            mListView.setBackgroundColor( 0xFFEEEEEE );
            mRootView.setPadding( 0, actionBarHeight + mApp.getStatusBarHeight( mContext ), 0, 0 );
            mListView.setPadding( 10, 0, 10, mApp.getNavigationBarHeight( mContext ) );
            mListView.setClipToPadding( false );

            //Set the window color.
            getActivity().getWindow()
                         .setBackgroundDrawable( UIElementsHelper.getGeneralActionBarBackground( mContext ) );

        }

    }

    /**
     * Click listener for the color preference.
     */
    private Preference.OnPreferenceClickListener mAppThemeClickListener = new Preference.OnPreferenceClickListener() {

        @Override
        public boolean onPreferenceClick( Preference preference ) {
            final Theme currentTheme = mPreferences.getCurrentTheme();

            AlertDialog.Builder builder = new AlertDialog.Builder( getActivity() );
            builder.setTitle( R.string.app_theme );
            builder.setSingleChoiceItems( R.array.app_theme_choices, currentTheme.ordinal(),
                                          new DialogInterface.OnClickListener() {

                                              @Override
                                              public void onClick( DialogInterface dialog, int which ) {
                                                  mPreferences.setCurrentTheme( Theme.values()[ which ] );
                                                  dialog.dismiss();
                                                  Toast.makeText( mContext, R.string.changes_saved, Toast.LENGTH_SHORT )
                                                       .show();

                                              }

                                          } );

            builder.create().show();
            return false;
        }

    };

    /**
     * Click listener for the color preference.
     */
    private Preference.OnPreferenceClickListener colorClickListener = new Preference.OnPreferenceClickListener() {

        @Override
        public boolean onPreferenceClick( Preference preference ) {
            FragmentTransaction ft = getChildFragmentManager().beginTransaction();
            NowPlayingColorSchemesDialog appThemeDialog = new NowPlayingColorSchemesDialog();
            appThemeDialog.show( ft, "colorSchemesDialog" );

            return false;
        }

    };

    /**
     * Checkbox click listener for the lockscreen controls preference.
     */
    private Preference.OnPreferenceChangeListener lockscreenControlsClickListener
            = new Preference.OnPreferenceChangeListener() {

        @Override
        public boolean onPreferenceChange( Preference preference, Object newValue ) {
            boolean value = (Boolean) newValue;
            mPreferences.setShowLockScreenControls( value );
            ( (CheckBoxPreference) preference ).setChecked( value );
            return false;
        }
    };

    @Override
    public void onResume() {
        super.onResume();

        if( Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT ) {
            getActivity().getActionBar()
                         .setBackgroundDrawable( UIElementsHelper.getGeneralActionBarBackground( mContext ) );
        }

    }

}
