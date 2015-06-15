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
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.jassmp.AsyncTasks.AsyncBuildLibraryTask;
import com.jassmp.AsyncTasks.AsyncBuildLibraryTask.OnBuildLibraryProgressUpdate;
import com.jassmp.GuiHelper.TypefaceHelper;
import com.jassmp.MainActivity.MainActivity;
import com.jassmp.R;

public class BuildingLibraryProgressFragment extends Fragment implements OnBuildLibraryProgressUpdate {

    private Context        mContext;
    private RelativeLayout mProgressElementsContainer;
    private ProgressBar    mProgressBar;
    private Animation      mFadeInAnimation;

    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState ) {
        mContext = getActivity().getApplicationContext();
        final View rootView = getActivity().getLayoutInflater()
                                           .inflate( R.layout.fragment_building_library_progress, null );

        mProgressElementsContainer = (RelativeLayout) rootView.findViewById( R.id.progress_elements_container );
        mProgressElementsContainer.setVisibility( View.INVISIBLE );

        final TextView currentTaskText = (TextView) rootView.findViewById( R.id.building_library_task );
        currentTaskText.setTypeface( TypefaceHelper.getTypeface( mContext, "Roboto-Light" ) );
        currentTaskText.setPaintFlags(
                currentTaskText.getPaintFlags() | Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG );

        mProgressBar = (ProgressBar) rootView.findViewById( R.id.building_library_progress );
        mProgressBar.setMax( 1000000 );

        mFadeInAnimation = AnimationUtils.loadAnimation( mContext, R.anim.fade_in );
        mFadeInAnimation.setAnimationListener( mFadeInListener );
        mFadeInAnimation.setDuration( 700 );

        return rootView;
    }

    /**
     * Fade in animation listener.
     */
    private AnimationListener mFadeInListener = new AnimationListener() {

        @Override
        public void onAnimationEnd( Animation arg0 ) {
            mProgressElementsContainer.setVisibility( View.VISIBLE );

        }

        @Override
        public void onAnimationRepeat( Animation arg0 ) {
        }

        @Override
        public void onAnimationStart( Animation arg0 ) {
        }

    };

    @Override
    public void onStartBuildingLibrary() {
        mProgressElementsContainer.startAnimation( mFadeInAnimation );
    }

    @Override
    public void onProgressUpdate( AsyncBuildLibraryTask task, String mCurrentTask, int overallProgress,
                                  int maxProgress, boolean mediaStoreTransferDone ) {
        /**
         * overallProgress refers to the progress that the service's notification
         * progress bar will display. Since this fragment will only show the progress
         * of building the library (and not scanning the album art), we need to
         * multiply the overallProgress by 4 (the building library task only takes
         * up a quarter of the overall progress bar).
         */
        mProgressBar.setProgress( overallProgress * 4 );

        //This fragment only shows the MediaStore transfer progress.
        if( mediaStoreTransferDone ) {
            onFinishBuildingLibrary( task );
        }
    }

    @Override
    public void onFinishBuildingLibrary( AsyncBuildLibraryTask task ) {
        task.mBuildLibraryProgressUpdate.remove( 0 );
        final Intent intent = new Intent( mContext, MainActivity.class );
        intent.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
        mContext.startActivity( intent );
    }

}

