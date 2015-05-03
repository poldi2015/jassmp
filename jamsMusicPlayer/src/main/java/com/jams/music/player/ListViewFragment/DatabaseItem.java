package com.jams.music.player.ListViewFragment;

import com.jams.music.player.DBHelpers.DBAccessHelper;

public enum DatabaseItem {
    TITLE( DBAccessHelper.SONG_TITLE ),
    ARTIST( DBAccessHelper.SONG_ARTIST ),
    ALBUM( DBAccessHelper.SONG_ALBUM ),
    GENRE( DBAccessHelper.SONG_GENRE ),
    DURATION( DBAccessHelper.SONG_DURATION ),
    BPM( DBAccessHelper.SONG_BPM ),
    RATING( DBAccessHelper.SONG_RATING ),
    FILE_PATH( DBAccessHelper.SONG_FILE_PATH ),
    COVER_PATH( DBAccessHelper.SONG_ALBUM_ART_PATH );

    private final String mDbColumn;

    DatabaseItem( final String dbColumn ) {
        mDbColumn = dbColumn;
    }

    public String getDbColumn() {
        return mDbColumn;
    }
}
