package com.jassmp.Preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.jassmp.Dao.Column;
import com.jassmp.JassMpDb.OrderDirection;
import com.jassmp.MainActivity.MainActivity;
import com.jassmp.Playback.RepeatMode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Preferences {

    //
    // defines

    public static final  String NAME                              = "com.jams.music.player";
    public static final  String PLAYBACK_CURRENT_INDEX            = "PlaybackCurrentIndex";
    public static final  String PLAYBACK_QUEUE                    = "PlaybackQueue";
    public static final  String SHOW_LOCK_SCREEN_CONTROLS         = "ShowLockscreenControls";
    public static final  String CROSS_FADE_ENABLED                = "CrossfadeEnabled";
    public static final  String CROSS_FADE_DURATION               = "CrossfadeDuration";
    public static final  String REPEAT_MODE                       = "RepeatMode";
    public static final  String PLAYBACK_POSITION                 = "PlaybackPosition";
    public static final  String CURRENT_THEME                     = "CurrentTheme";
    private static final String NOW_PLAYING_COLOR                 = "NOW_PLAYING_COLOR";
    public static final  String FIRST_RUN                         = "FirstRun";
    public static final  String START_COUNT                       = "START_COUNT";
    public static final  String LIBRARY_SCAN_FREQUENCY            = "SCAN_FREQUENCY";
    public static final  String RESCAN_ALBUM_ART                  = "RESCAN_ALBUM_ART";
    public static final  String REBUILD_LIBRARY                   = "REBUILD_LIBRARY";
    public static final  String SONG_LIST_SORT_COLUMN             = "SongListSortColumn";
    public static final  String NOW_PLAYING_SORT_COLUMN           = "NowPlayingSortColumn";
    public static final  String SONG_LIST_SORT_DIRECTION_COLUMN   = "SongListSortDirectionColumn";
    public static final  String NOW_PLAYING_SORT_DIRECTION_COLUMN = "NowPlayingSortDirectionColumn";
    public static final  String MUSIC_FOLDERS_SELECTION           = "MUSIC_FOLDERS_SELECTION";
    public static final  String CURRENT_MAIN_FRAGMENT_ID          = "CurrentMainFragmentId";
    public static final  String ALBUM_ART_SOURCE                  = "ALBUM_ART_SOURCE";
    public static final  String SHOW_ELAPSED_TIME                 = "showElapsedTime";


    //
    // private members

    private final SharedPreferences mPreferences;

    public Preferences( final Context context ) {
        mPreferences = context.getSharedPreferences( NAME, Context.MODE_PRIVATE );
    }

    public int getPlaybackCurrentIndex() {
        return mPreferences.getInt( PLAYBACK_CURRENT_INDEX, 0 );
    }

    public void setPlaybackCurrentIndex( final int index ) {
        mPreferences.edit().putInt( PLAYBACK_CURRENT_INDEX, index ).apply();
    }

    public int getPlaybackPosition() {
        return mPreferences.getInt( PLAYBACK_POSITION, 0 );
    }

    public void setPlaybackPosition( final int position ) {
        mPreferences.edit().putInt( PLAYBACK_POSITION, position ).apply();
    }

    public List<String> getPlaybackQueue() {
        final String playBackQueue = mPreferences.getString( PLAYBACK_QUEUE, "" );
        return new ArrayList( Arrays.asList( playBackQueue.split( "," ) ) );
    }

    public void setPlaybackQueue( final List<String> queue ) {
        final String playBackQueue = TextUtils.join( ",", queue );
        mPreferences.edit().putString( PLAYBACK_QUEUE, playBackQueue ).apply();
    }

    public boolean getShowLockScreenControls() {
        return mPreferences.getBoolean( SHOW_LOCK_SCREEN_CONTROLS, true );
    }

    public void setShowLockScreenControls( boolean show ) {
        mPreferences.edit().putBoolean( SHOW_LOCK_SCREEN_CONTROLS, show ).apply();
    }

    public boolean isCrossFadeEnabled() {
        return mPreferences.getBoolean( CROSS_FADE_ENABLED, false );
    }

    public void setCrossFadeEnabled( final boolean crossFadeEnabled ) {
        mPreferences.edit().putBoolean( CROSS_FADE_ENABLED, crossFadeEnabled ).apply();
    }

    public int getCrossFadeDuration() {
        return mPreferences.getInt( CROSS_FADE_DURATION, 5 );
    }

    public void setCrossFadeDuration( final int duration ) {
        mPreferences.edit().putInt( CROSS_FADE_DURATION, duration ).apply();
    }

    public RepeatMode getRepeatMode() {
        final int repeatMode = mPreferences.getInt( REPEAT_MODE, 0 );
        final RepeatMode[] values = RepeatMode.values();
        return repeatMode >= 0 && repeatMode <= values.length ? values[ repeatMode ] : RepeatMode.NONE;
    }

    public void setRepeatMode( final RepeatMode repeatMode ) {
        mPreferences.edit().putInt( REPEAT_MODE, repeatMode.ordinal() ).apply();
    }

    public Theme getCurrentTheme() {
        return Theme.values()[ mPreferences.getInt( CURRENT_THEME, Theme.DARK.ordinal() ) ];
    }

    public void setCurrentTheme( final Theme theme ) {
        mPreferences.edit().putInt( CURRENT_THEME, theme.ordinal() ).apply();
    }

    public boolean isFirstRun() {
        return mPreferences.getBoolean( FIRST_RUN, true );
    }

    public void clearFirstRun() {
        mPreferences.edit().putBoolean( FIRST_RUN, false ).apply();
    }

    public int getStartCount() {
        return mPreferences.getInt( START_COUNT, 0 );
    }

    public void setStartCount( final int startCount ) {
        mPreferences.edit().putInt( START_COUNT, startCount ).apply();
    }

    public int getLibraryScanFrequency() {
        return mPreferences.getInt( LIBRARY_SCAN_FREQUENCY, 5 );
    }

    public void setLibraryScanFrequency( final int frequency ) {
        mPreferences.edit().putInt( LIBRARY_SCAN_FREQUENCY, frequency ).apply();
    }

    public boolean getRescanAlbumArt() {
        return mPreferences.getBoolean( RESCAN_ALBUM_ART, false );
    }

    public void setRescanAlbumArt( final boolean rescan ) {
        mPreferences.edit().putBoolean( RESCAN_ALBUM_ART, rescan ).apply();
    }

    public boolean getRebuildLibrary() {
        return mPreferences.getBoolean( REBUILD_LIBRARY, false );
    }

    public void setRebuildLibrary( final boolean rescan ) {
        mPreferences.edit().putBoolean( REBUILD_LIBRARY, rescan ).apply();
    }

    public int getSongListSortColumn() {
        return mPreferences.getInt( SONG_LIST_SORT_COLUMN, -1 );
    }

    public void setSongListSortColumn( final int column ) {
        mPreferences.edit().putInt( SONG_LIST_SORT_COLUMN, column ).apply();
    }

    public int getNowPlayingSortColumn() {
        return mPreferences.getInt( NOW_PLAYING_SORT_COLUMN, -1 );
    }

    public void setNowPlayingSortColumn( final int column ) {
        mPreferences.edit().putInt( NOW_PLAYING_SORT_COLUMN, column ).apply();
    }

    public OrderDirection getSongListSortDirection( final Column column ) {
        return OrderDirection.valueOf( mPreferences.getString( SONG_LIST_SORT_DIRECTION_COLUMN + "_" + column.name,
                                                               OrderDirection.ASC.name() ) );
    }

    public void setSongListSortDirection( final Column column, final OrderDirection orderDirection ) {
        mPreferences.edit()
                    .putString( SONG_LIST_SORT_DIRECTION_COLUMN + "_" + column.name, orderDirection.name() )
                    .apply();
    }

    public OrderDirection getNowPlayingSortDirection( final Column column ) {
        return OrderDirection.valueOf( mPreferences.getString( NOW_PLAYING_SORT_DIRECTION_COLUMN + "_" + column.name,
                                                               OrderDirection.ASC.name() ) );
    }

    public void setNowPlayingSortDirection( final Column column, final OrderDirection orderDirection ) {
        mPreferences.edit()
                    .putString( NOW_PLAYING_SORT_DIRECTION_COLUMN + "_" + column.name, orderDirection.name() )
                    .apply();
    }

    public boolean isMusicFoldersSelected() {
        final int musicFoldersSelected = mPreferences.getInt( MUSIC_FOLDERS_SELECTION, 0 );
        return musicFoldersSelected != 0;
    }

    public void setMusicFoldersSelected( final boolean selected ) {
        mPreferences.edit().putInt( MUSIC_FOLDERS_SELECTION, selected ? 1 : 0 ).apply();
    }

    public MainActivity.FragmentId getCurrentMainFragmentId() {
        return MainActivity.FragmentId.values()[ mPreferences.getInt( CURRENT_MAIN_FRAGMENT_ID,
                                                                      MainActivity.FragmentId.SONGS.ordinal() ) ];
    }

    public void setCurrentMainFragmentId( final MainActivity.FragmentId fragmentId ) {
        mPreferences.edit().putInt( CURRENT_MAIN_FRAGMENT_ID, fragmentId.ordinal() ).apply();
    }

    public ColorTheme getNowPlayingColorTheme() {
        return ColorTheme.valueOf( mPreferences.getString( NOW_PLAYING_COLOR, ColorTheme.BLUE.name() ) );
    }

    public void setNowPlayingColorTheme( final ColorTheme colorTheme ) {
        mPreferences.edit().putString( NOW_PLAYING_COLOR, colorTheme.name() ).apply();
    }

    public AlbumArtSource getAlbumArtSource() {
        return AlbumArtSource.values()[ mPreferences.getInt( ALBUM_ART_SOURCE,
                                                             AlbumArtSource.PREFER_EMBEDDED_ART.ordinal() ) ];
    }

    public void setAlbumArtSource( final AlbumArtSource albumArtSource ) {
        mPreferences.edit().putInt( ALBUM_ART_SOURCE, albumArtSource.ordinal() ).apply();
    }

    public boolean getShowElapsedTime() {
        return mPreferences.getBoolean( SHOW_ELAPSED_TIME, true );
    }

    public void setShowElapsedTime( final boolean showElapsedTime ) {
        mPreferences.edit().putBoolean( SHOW_ELAPSED_TIME, showElapsedTime ).apply();
    }


}
