package com.jassmp.Playback;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.view.View;
import android.widget.RemoteViews;

import com.jassmp.Dao.SongDao;
import com.jassmp.MainActivity.MainActivity;
import com.jassmp.R;

public class NotificationBuilder extends Playback {

    //
    // defines

    public static final int NOTIFICATION_ID = 2045;


    //
    // private members

    private final Context mContext;

    public NotificationBuilder( final Context context ) {
        super( context );
        mContext = context;
    }

    public Notification build( final PlayerState state, final SongDao song ) {
        if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN ) {
            return buildJBNotification( state, song );
        } else {
            return buildICSNotification( state, song );
        }
    }

    public void updateNotification( final PlayerState state, final SongDao song ) {
        final Notification notification;
        if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN ) {
            notification = buildJBNotification( state, song );
        } else {
            notification = buildICSNotification( state, song );
        }

        //Update the current notification.
        final android.app.NotificationManager notificationManager
                = (android.app.NotificationManager) mContext.getSystemService( Context.NOTIFICATION_SERVICE );
        notificationManager.notify( NOTIFICATION_ID, notification );
    }

    /**
     * Builds and returns a fully constructed Notification for devices
     * on Jelly Bean and above (API 16+).
     */
    @SuppressLint( "NewApi" )
    private Notification buildJBNotification( final PlayerState state, final SongDao song ) {
        final NotificationCompat.Builder builder = new NotificationCompat.Builder( mContext );
        builder.setOngoing( true );
        builder.setAutoCancel( false );
        builder.setSmallIcon( R.drawable.notif_icon );

        //Open up the player screen when the user taps on the notification.
        final Intent startApplicationIntent = new Intent( mContext, MainActivity.class );
        builder.setContentIntent(
                PendingIntent.getBroadcast( mContext.getApplicationContext(), 0, startApplicationIntent, 0 ) );

        //Grab the notification layouts.
        final RemoteViews notificationView = new RemoteViews( mContext.getPackageName(),
                                                              R.layout.notification_custom_layout );
        final RemoteViews expNotificationView = new RemoteViews( mContext.getPackageName(),
                                                                 R.layout.notification_custom_expanded_layout );

        //Initialize the notification layout buttons.
        // TODO: Buttons are not working, it seems that intents are not connected to buttons
        final PendingIntent previousTrackPendingIntent = PendingIntent.getBroadcast( mContext.getApplicationContext(),
                                                                                     0, createPreviousIntent(), 0 );
        notificationView.setOnClickPendingIntent( R.id.notification_base_previous, previousTrackPendingIntent );
        expNotificationView.setOnClickPendingIntent( R.id.notification_expanded_base_previous,
                                                     previousTrackPendingIntent );

        final PendingIntent playPauseTrackPendingIntent = PendingIntent.getBroadcast( mContext.getApplicationContext(),
                                                                                      0, createPlayPauseIntent(), 0 );
        notificationView.setOnClickPendingIntent( R.id.notification_base_play, playPauseTrackPendingIntent );

        final PendingIntent nextTrackPendingIntent = PendingIntent.getBroadcast( mContext.getApplicationContext(), 0,
                                                                                 createNextIntent(), 0 );
        notificationView.setOnClickPendingIntent( R.id.notification_base_next, nextTrackPendingIntent );
        expNotificationView.setOnClickPendingIntent( R.id.notification_expanded_base_next, nextTrackPendingIntent );


        final PendingIntent stopServicePendingIntent = PendingIntent.getBroadcast( mContext.getApplicationContext(), 0,
                                                                                   createPauseIntent(), 0 );
        expNotificationView.setOnClickPendingIntent( R.id.notification_expanded_base_collapse,
                                                     stopServicePendingIntent );
        notificationView.setOnClickPendingIntent( R.id.notification_base_collapse, stopServicePendingIntent );

        //Check if audio is playing and set the appropriate play/pause button.
        if( state.getState() == PlayerState.State.PLAYING ) {
            notificationView.setImageViewResource( R.id.notification_base_play, R.drawable.btn_playback_pause_light );
            expNotificationView.setImageViewResource( R.id.notification_expanded_base_play,
                                                      R.drawable.btn_playback_pause_light );
        } else {
            notificationView.setImageViewResource( R.id.notification_base_play, R.drawable.btn_playback_play_light );
            expNotificationView.setImageViewResource( R.id.notification_expanded_base_play,
                                                      R.drawable.btn_playback_play_light );
        }

        //Set the notification content.
        expNotificationView.setTextViewText( R.id.notification_expanded_base_line_one,
                                             song != null ? song.getTitle() : "" );
        notificationView.setTextViewText( R.id.notification_base_line_one, song != null ? song.getTitle() : "" );
        expNotificationView.setTextViewText( R.id.notification_expanded_base_line_two,
                                             song != null ? song.getArtist() : "" );
        notificationView.setTextViewText( R.id.notification_base_line_two, song != null ? song.getArtist() : "" );
        expNotificationView.setTextViewText( R.id.notification_expanded_base_line_three,
                                             song != null ? song.getAlbum() : "" );
        expNotificationView.setViewVisibility( R.id.notification_expanded_base_previous, View.VISIBLE );
        expNotificationView.setViewVisibility( R.id.notification_expanded_base_next, View.VISIBLE );
        expNotificationView.setOnClickPendingIntent( R.id.notification_expanded_base_play,
                                                     playPauseTrackPendingIntent );
        notificationView.setViewVisibility( R.id.notification_base_previous, View.VISIBLE );
        notificationView.setViewVisibility( R.id.notification_base_next, View.VISIBLE );

        //Set the album art.
        if( song != null ) {
            song.loadAlbumArt( mContext, new SongDao.AlbumArtLoadedListener() {
                @Override
                public void artLoaded( final Bitmap bitmap ) {
                    expNotificationView.setImageViewBitmap( R.id.notification_expanded_base_image, bitmap );
                    notificationView.setImageViewBitmap( R.id.notification_base_image, bitmap );
                }
            } );
        }

        //Attach the shrunken layout to the notification.
        builder.setContent( notificationView );

        //Build the notification object.
        Notification notification = builder.build();

        //Attach the expanded layout to the notification and set its flags.
        notification.bigContentView = expNotificationView;
        notification.flags = Notification.FLAG_FOREGROUND_SERVICE |
                             Notification.FLAG_NO_CLEAR |
                             Notification.FLAG_ONGOING_EVENT;

        return notification;
    }

    /**
     * Builds and returns a fully constructed Notification for devices
     * on Ice Cream Sandwich (APIs 14 & 15).
     */
    private Notification buildICSNotification( final PlayerState state, final SongDao song ) {
        final NotificationCompat.Builder builder = new NotificationCompat.Builder( mContext );
        builder.setOngoing( true );
        builder.setAutoCancel( false );
        builder.setSmallIcon( R.drawable.notif_icon );

        //Open up the player screen when the user taps on the notification.
        final Intent startApplicationIntent = new Intent( mContext, MainActivity.class );
        final PendingIntent launchNowPlayingPendingIntent = PendingIntent.getBroadcast(
                mContext.getApplicationContext(), 0, startApplicationIntent, 0 );
        builder.setContentIntent( launchNowPlayingPendingIntent );

        //Grab the notification layout.
        final RemoteViews notificationView = new RemoteViews( mContext.getPackageName(),
                                                              R.layout.notification_custom_layout );

        //Initialize the notification layout buttons.
        final PendingIntent previousTrackPendingIntent = PendingIntent.getBroadcast( mContext.getApplicationContext(),
                                                                                     0, createPreviousIntent(), 0 );
        notificationView.setOnClickPendingIntent( R.id.notification_base_previous, previousTrackPendingIntent );

        final PendingIntent playPauseTrackPendingIntent = PendingIntent.getBroadcast( mContext.getApplicationContext(),
                                                                                      0, createPlayPauseIntent(), 0 );
        notificationView.setOnClickPendingIntent( R.id.notification_base_play, playPauseTrackPendingIntent );

        final PendingIntent nextTrackPendingIntent = PendingIntent.getBroadcast( mContext.getApplicationContext(), 0,
                                                                                 createNextIntent(), 0 );
        notificationView.setOnClickPendingIntent( R.id.notification_base_next, nextTrackPendingIntent );

        final PendingIntent stopServicePendingIntent = PendingIntent.getBroadcast( mContext.getApplicationContext(), 0,
                                                                                   createPauseIntent(), 0 );
        notificationView.setOnClickPendingIntent( R.id.notification_base_collapse, stopServicePendingIntent );

        //Check if audio is playing and set the appropriate play/pause button.
        if( state.getState() == PlayerState.State.PLAYING ) {
            notificationView.setImageViewResource( R.id.notification_base_play, R.drawable.btn_playback_pause_light );
        } else {
            notificationView.setImageViewResource( R.id.notification_base_play, R.drawable.btn_playback_play_light );
        }

        //Set the notification content.
        notificationView.setTextViewText( R.id.notification_base_line_one, song != null ? song.getTitle() : "" );
        notificationView.setTextViewText( R.id.notification_base_line_two, song != null ? song.getArtist() : "" );
        notificationView.setViewVisibility( R.id.notification_base_previous, View.VISIBLE );
        notificationView.setViewVisibility( R.id.notification_base_next, View.VISIBLE );

        //Set the album art.
        if( song != null ) {
            song.loadAlbumArt( mContext, new SongDao.AlbumArtLoadedListener() {
                @Override
                public void artLoaded( final Bitmap bitmap ) {
                    notificationView.setImageViewBitmap( R.id.notification_base_image, bitmap );
                }
            } );
        }

        //Attach the shrunken layout to the notification.
        builder.setContent( notificationView );

        //Build the notification object and set its flags.
        Notification notification = builder.build();
        notification.flags = Notification.FLAG_FOREGROUND_SERVICE |
                             Notification.FLAG_NO_CLEAR |
                             Notification.FLAG_ONGOING_EVENT;

        return notification;
    }

}
