package com.jassmp.Dao;

import android.content.ContentValues;

import com.jassmp.JassMpDb.AbstractTableAccessor;
import com.jassmp.R;

import java.util.List;
import java.util.Map;

public class FilterDao {

    public static final Column COLUMN_ID       = AbstractTableAccessor.COLUMN_ID;
    public static final Column COLUMN_NAME     = new Column( "name", Column.ColumnFlag.UNIQUE );
    public static final Column COLUMN_SELECTED = new Column( "selected", Column.ColumnType.INTEGER, "1" );
    public static final Column COLUMN_COUNT    = new Column( "count", Column.ColumnType.INTEGER, "0" );
    public static final Column COLUMN_ARTIST   = new Column( "artist", R.string.unknown_artist );
    public static final Column COLUMN_ART_PATH = new Column( "art_path" );

    public static final Column[] ALL_COLUMNS = { COLUMN_ID, COLUMN_NAME, COLUMN_COUNT, COLUMN_SELECTED, COLUMN_ARTIST,
                                                 COLUMN_ART_PATH };


    private       int     mId;
    private final String  mName;
    private       int     mCount;
    private       boolean mSelected;
    private final String  mArtist;
    private final String  mArtPath;

    public FilterDao( final Map<Column, Object> values ) {
        mId = (Integer) values.get( COLUMN_ID );
        mName = (String) values.get( COLUMN_NAME );
        mCount = (Integer) values.get( COLUMN_COUNT );
        mSelected = ( (Integer) values.get( COLUMN_SELECTED ) ) > 0;
        mArtist = (String) values.get( COLUMN_ARTIST );
        mArtPath = (String) values.get( COLUMN_ART_PATH );
    }

    public FilterDao( final SongDao songDao, Column nameColumnInSong, List<Column> filterColumns ) {
        mId = -1;
        mName = songDao.getValue( nameColumnInSong ).toString();
        mCount = 1;
        mSelected = true;
        mArtist = filterColumns.contains( COLUMN_ARTIST ) ? songDao.getArtist() : null;
        mArtPath = filterColumns.contains( COLUMN_ART_PATH ) ? songDao.getAlbumArtPath() : null;
    }

    public void setId( final int id ) {
        mId = id;
    }

    public int getId() {
        return mId;
    }

    public String getName() {
        return mName;
    }

    public void setCount( final int count ) {
        mCount = count;
    }

    public int getCount() {
        return mCount;
    }

    public void setSelected( boolean selected ) {
        mSelected = selected;
    }

    public boolean isSelected() {
        return mSelected;
    }

    public String getArtist() {
        return mArtist;
    }

    public String getArtPath() {
        return mArtPath;
    }

    public ContentValues getContentValues() {
        final ContentValues contentValues = new ContentValues();
        if( mId >= 0 ) {
            contentValues.put( COLUMN_ID.name, mId );
        }
        contentValues.put( COLUMN_NAME.name, mName );
        contentValues.put( COLUMN_COUNT.name, mCount );
        contentValues.put( COLUMN_SELECTED.name, mSelected );
        if( mArtist != null ) {
            contentValues.put( COLUMN_ARTIST.name, mArtist );
        }
        if( mArtPath != null ) {
            contentValues.put( COLUMN_ART_PATH.name, mArtPath );
        }

        return contentValues;
    }

    @Override
    public String toString() {
        return "FilterDao{" +
               "id=" + mId +
               ", name='" + mName + '\'' +
               ", selected=" + mSelected +
               '}';
    }
}
