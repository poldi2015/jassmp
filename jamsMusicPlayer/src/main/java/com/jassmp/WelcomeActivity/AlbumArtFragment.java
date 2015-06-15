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
package com.jassmp.WelcomeActivity;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;

import com.jassmp.GuiHelper.TypefaceHelper;
import com.jassmp.Preferences.AlbumArtSource;
import com.jassmp.Preferences.Preferences;
import com.jassmp.R;
import com.jassmp.Utils.Common;

public class AlbumArtFragment extends Fragment {

    private Context mContext;
    private Common  mApp;

    private TextView welcomeHeader;
    private TextView welcomeText1;

    private RadioGroup  radioGroup;
    private RadioButton mPickWhatsBestRadioButton;
    private RadioButton mUseEmbeddedArtOnlyRadioButton;
    private RadioButton mUseFolderArtOnlyRadioButton;

    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState ) {

        mContext = getActivity().getApplicationContext();
        mApp = (Common) mContext;
        final Preferences preferences = new Preferences( mContext );
        View rootView = (View) getActivity().getLayoutInflater().inflate( R.layout.fragment_welcome_screen_4, null );

        welcomeHeader = (TextView) rootView.findViewById( R.id.welcome_header );
        welcomeHeader.setTypeface( TypefaceHelper.getTypeface( getActivity(), "Roboto-Light" ) );

        welcomeText1 = (TextView) rootView.findViewById( R.id.welcome_text_1 );
        welcomeText1.setTypeface( TypefaceHelper.getTypeface( getActivity(), "Roboto-Regular" ) );

        radioGroup = (RadioGroup) rootView.findViewById( R.id.album_art_radio_group );
        mPickWhatsBestRadioButton = (RadioButton) rootView.findViewById( R.id.pick_whats_best_for_me );
        mUseEmbeddedArtOnlyRadioButton = (RadioButton) rootView.findViewById( R.id.use_embedded_art_only );
        mUseFolderArtOnlyRadioButton = (RadioButton) rootView.findViewById( R.id.use_folder_art_only );

        mPickWhatsBestRadioButton.setTypeface( TypefaceHelper.getTypeface( getActivity(), "Roboto-Regular" ) );
        mUseEmbeddedArtOnlyRadioButton.setTypeface( TypefaceHelper.getTypeface( getActivity(), "Roboto-Regular" ) );
        mUseFolderArtOnlyRadioButton.setTypeface( TypefaceHelper.getTypeface( getActivity(), "Roboto-Regular" ) );

        //Check which album art source is selected and set the appropriate flag.
        switch( preferences.getAlbumArtSource() ) {
            default:
            case PREFER_EMBEDDED_ART:
            case PREFER_FOLDER_ART:
                mPickWhatsBestRadioButton.setChecked( true );
                break;
            case EMBEDDED_ART_ONLY:
                mUseEmbeddedArtOnlyRadioButton.setChecked( true );
                break;
            case FOLDER_ART_OLNY:
                mUseFolderArtOnlyRadioButton.setChecked( true );
                break;
        }

        radioGroup.setOnCheckedChangeListener( new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged( RadioGroup group, int checkedId ) {
                switch( checkedId ) {
                    case R.id.pick_whats_best_for_me:
                        preferences.setAlbumArtSource( AlbumArtSource.PREFER_EMBEDDED_ART );
                        break;
                    case R.id.use_embedded_art_only:
                        preferences.setAlbumArtSource( AlbumArtSource.EMBEDDED_ART_ONLY );
                        break;
                    case R.id.use_folder_art_only:
                        preferences.setAlbumArtSource( AlbumArtSource.FOLDER_ART_OLNY );
                        break;
                }
            }

        } );

        return rootView;
    }

}

