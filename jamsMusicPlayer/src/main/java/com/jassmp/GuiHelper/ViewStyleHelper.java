package com.jassmp.GuiHelper;

import android.content.Context;
import android.graphics.Paint;
import android.os.Build;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.andraskindler.quickscroll.QuickScroll;
import com.andraskindler.quickscroll.Scrollable;
import com.jassmp.Helpers.PauseOnScrollHelper;
import com.jassmp.Preferences.Preferences;
import com.jassmp.R;
import com.jassmp.Utils.Common;

public class ViewStyleHelper {

    private final Common      mApp;
    private final Context     mContext;
    private final Preferences mPreferences;

    public ViewStyleHelper( final Common app, final Context context ) {
        mApp = app;
        mContext = context;
        mPreferences = new Preferences( mContext );
    }

    public void styleRootView( final View rootView ) {
        //Set the background. We're using getGridViewBackground() since the list doesn't have
        // card items.
        rootView.setBackgroundColor( UIElementsHelper.getGridViewBackground( mContext ) );
    }

    public void styleListView( final QuickScroll quickScroll, final ListView listView ) {

        listView.setVerticalScrollBarEnabled( false );

        //Apply the ListViews' dividers.
        switch( mPreferences.getCurrentTheme() ) {
            case DARK:
                listView.setDivider( mContext.getResources().getDrawable( R.drawable.icon_list_divider ) );
                break;
            case LIGHT:
                listView.setDivider( mContext.getResources().getDrawable( R.drawable.icon_list_divider_light ) );
                break;
        }
        listView.setDividerHeight( 1 );

        //KitKat translucent navigation/status bar.
        if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT ) {
            int topPadding = Common.getStatusBarHeight( mContext );

            //Calculate navigation bar height.
            int navigationBarHeight = 0;
            int resourceId = mContext.getResources().getIdentifier( "navigation_bar_height", "dimen", "android" );
            if( resourceId > 0 ) {
                navigationBarHeight = mContext.getResources().getDimensionPixelSize( resourceId );
            }

            listView.setClipToPadding( false );
            listView.setPadding( 0, topPadding, 0, navigationBarHeight );
            quickScroll.setPadding( 0, topPadding, 0, navigationBarHeight );
        }
    }

    public void styleAndInitQuickScroll( final QuickScroll quickScroll, final ListView listView,
                                         final Scrollable adapter ) {
        //Init the quick scroll widget.
        quickScroll.init( QuickScroll.TYPE_INDICATOR_WITH_HANDLE, listView, adapter, QuickScroll.STYLE_HOLO );

        int[] quickScrollColors = UIElementsHelper.getQuickScrollColors( mContext );
        PauseOnScrollHelper scrollListener = new PauseOnScrollHelper( mApp.getPicasso(), null, true, true );

        quickScroll.setOnScrollListener( scrollListener );
        quickScroll.setPicassoInstance( mApp.getPicasso() );
        quickScroll.setHandlebarColor( quickScrollColors[ 0 ], quickScrollColors[ 0 ], quickScrollColors[ 1 ] );
        quickScroll.setIndicatorColor( quickScrollColors[ 1 ], quickScrollColors[ 0 ], quickScrollColors[ 2 ] );
        quickScroll.setTextSize( TypedValue.COMPLEX_UNIT_DIP, 48 );

    }

    public void styleEmptyView( final TextView emptyView ) {
        emptyView.setTypeface( TypefaceHelper.getTypeface( mContext, "Roboto-Light" ) );
        emptyView.setPaintFlags( emptyView.getPaintFlags() | Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG );

    }

    public void animateFloatInFromBottom( final View containerView, final View floatingView ) {
        TranslateAnimation animation = new TranslateAnimation( Animation.RELATIVE_TO_SELF, 0.0f,
                                                               Animation.RELATIVE_TO_SELF, 0.0f,
                                                               Animation.RELATIVE_TO_SELF, 2.0f,
                                                               Animation.RELATIVE_TO_SELF, 0.0f );

        animation.setDuration( 300 );
        animation.setInterpolator( new AccelerateDecelerateInterpolator() );

        animation.setAnimationListener( new Animation.AnimationListener() {

            @Override
            public void onAnimationEnd( Animation arg0 ) {
                containerView.setVisibility( View.VISIBLE );

            }

            @Override
            public void onAnimationRepeat( Animation arg0 ) {
            }

            @Override
            public void onAnimationStart( Animation arg0 ) {
                floatingView.setVisibility( View.VISIBLE );

            }

        } );

        floatingView.startAnimation( animation );
    }


    public void styleCoverImage( final ImageView coverImage ) {
        coverImage.setImageResource( UIElementsHelper.getEmptyCircularColorPatch( mContext ) );
    }

    public void styleTileText( final TextView titleText ) {
        titleText.setTextColor( UIElementsHelper.getRegularTextColor( mContext ) );
        titleText.setTypeface( TypefaceHelper.getTypeface( mContext, "Roboto-Regular" ) );
    }

    public void styleSubTitleText( final TextView subTitleText ) {
        subTitleText.setTextColor( UIElementsHelper.getSmallTextColor( mContext ) );
        subTitleText.setTypeface( TypefaceHelper.getTypeface( mContext, "Roboto-Regular" ) );

    }

}
