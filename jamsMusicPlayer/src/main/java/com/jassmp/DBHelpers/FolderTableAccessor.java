package com.jassmp.DBHelpers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Manages the table with the path patterns that are looked up in the Android MediaStore.
 * <p/>
 * If no path pattern is configured in the table all music from the MediaStore is loaded.
 */
public class FolderTableAccessor extends AbstractTableAccessor {

    //
    // defines

    public static final String TABLE_NAME = "MusicFoldersTable";

    public static final Column COLUMN_FOLDER_PATH = new Column( "folder_path" );
    public static final Column COLUMN_INCLUDE     = new Column( "include", ColumnType.INTEGER, "1" );

    public static final Column[] COLUMNS = { COLUMN_ID, COLUMN_FOLDER_PATH, COLUMN_INCLUDE, };

    //
    // private members

    private static FolderTableAccessor sInstance = null;

    public FolderTableAccessor( Context context ) {
        super( TABLE_NAME, context );
    }

    public static synchronized FolderTableAccessor getInstance( Context context ) {
        if( sInstance == null ) {
            sInstance = new FolderTableAccessor( context.getApplicationContext() );
        }

        return sInstance;
    }

    @Override
    protected Column[] getTableColumns() {
        return COLUMNS;
    }

    public void addMusicFolderPath( final String folderPath, final boolean include ) {
        ContentValues values = new ContentValues();
        values.put( COLUMN_FOLDER_PATH.name, normalizePath( folderPath ) );
        values.put( COLUMN_INCLUDE.name, include );

        insertEntry( values );
    }

    /**
     * Replace all entries in the table.
     *
     * @param entries path + include where if include is set to true path is considered an include patterns else
     *                an exclude pattern
     */
    public void replaceMusicFolders( final Map<String, Boolean> entries ) {
        try {
            deleteAllMusicFolderPaths();
            for( final Map.Entry<String, Boolean> entry : entries.entrySet() ) {
                addMusicFolderPath( entry.getKey(), entry.getValue() );
            }
        } finally {
            commit();
        }
    }


    /**
     * Deletes all music folders from the table.
     */
    public void deleteAllMusicFolderPaths() {
        deleteAllEntries();
    }

    //    /**
    //     * Allows to configure if the path should be added to the include or the exclude patterns.
    //     *
    //     * @param folderPath The path to reconfigure
    //     * @param include    true of path is an include pattern, false if it is an exclude pattern
    //     */
    //    public void setInclude( final String folderPath, final boolean include ) {
    //        ContentValues values = new ContentValues();
    //        values.put( COLUMN_INCLUDE.name, include );
    //        final String where = COLUMN_FOLDER_PATH.name + " = '" + normalizePath( folderPath ) + "'";
    //        updateEntry( values, where );
    //    }

    public boolean hasMusicFolders() {
        return hasEntries( null );
    }

    public LinkedHashMap<String, Boolean> getAllMusicFolderPaths() {
        final String where = COLUMN_INCLUDE.name;
        final Cursor cursor = queryEntries( new QueryBuilder().addResultColumn( COLUMN_FOLDER_PATH )
                                                              .addResultColumn( COLUMN_INCLUDE )
                                                              .setWhereWhereExpr( where )
                                                              .setOrderByColumn( COLUMN_FOLDER_PATH,
                                                                                 OrderDirection.ASC ) );
        final LinkedHashMap<String, Boolean> folderPaths = new LinkedHashMap<String, Boolean>();
        try {
            if( resetToFirst( cursor ) ) {
                do {
                    folderPaths.put( cursor.getString( 0 ), cursor.getInt( 1 ) != 0 );
                } while( cursor.moveToNext() );
            }
        } finally {
            if( cursor != null ) {
                cursor.close();
            }
        }

        return folderPaths;
    }

    /**
     * Extract last two path elements and quote single ticks.
     * <p/>
     * This is the path that is stored in the database.
     *
     * @param path the path to process
     * @return the stripped path
     */
    private String normalizePath( String path ) {
        path = path.replace( "//", "/" );
        int secondSlashIndex = path.lastIndexOf( "/", path.lastIndexOf( "/" ) - 1 );
        if( ( secondSlashIndex < path.length() ) && secondSlashIndex != -1 ) {
            path = path.substring( secondSlashIndex, path.length() );
        }

        return escapeTextLiteral( path );
    }

}
