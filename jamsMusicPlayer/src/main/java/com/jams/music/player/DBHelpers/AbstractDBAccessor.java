package com.jams.music.player.DBHelpers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.google.analytics.tracking.android.Log;

public abstract class AbstractDBAccessor extends SQLiteOpenHelper {

    //
    // defines

    public static final String DATABASE_NAME = "djp.db";

    public static final int DATABASE_VERSION = 1;

    public static final Column COLUMN_ID = new Column( "_id", ColumnType.INTEGER, ColumnFlag.PRIMARY_KEY );


    //
    // private members

    private final String mTableName;

    protected AbstractDBAccessor( final String tableName, final Context context ) {
        super( context, DATABASE_NAME, null, DATABASE_VERSION );
        mTableName = tableName;
    }

    private void begin() {
        if( !getWritableDatabase().inTransaction() ) {
            getWritableDatabase().beginTransaction();
        }
    }

    public void commit() {
        if( getWritableDatabase().inTransaction() ) {
            getWritableDatabase().setTransactionSuccessful();
            getWritableDatabase().endTransaction();
        }
    }

    protected abstract Column[] getTableColumns();

    protected abstract Column[] getAdditionalColumns( final int version );

    @Override
    public void onCreate( final SQLiteDatabase db ) {
        final String createStatement = buildCreateStatement( mTableName, getTableColumns() );
        db.execSQL( createStatement );
    }


    @Override
    public void onUpgrade( SQLiteDatabase db, int oldVersion, int newVersion ) {
        for( int version = oldVersion + 1; version <= newVersion; version++ ) {
            final Column[] columns = getAdditionalColumns( version );
            for( final Column column : columns ) {
                final String upgradeStatement = buildUpgradeStatement( mTableName, column );
                db.execSQL( upgradeStatement );
            }
        }
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            getWritableDatabase().close();
            super.finalize();
        } catch( Exception e ) {
            Log.e( e );
        }
    }

    private String buildCreateStatement( final String tableName, final Column[] columns ) {
        final StringBuilder createStatement = new StringBuilder( "CREATE TABLE IF NOT EXISTS " );

        createStatement.append( tableName );
        createStatement.append( " ( " );
        boolean first = true;

        for( final Column column : columns ) {
            if( !first ) {
                createStatement.append( ", " );
            } else {
                first = false;
            }
            createStatement.append( column.name );
            createStatement.append( " " );
            createStatement.append( column.type.name() );
            switch( column.flag ) {
                case PRIMARY_KEY:
                    if( column.type == ColumnType.INTEGER ) {
                        createStatement.append( " PRIMARY KEY AUTOINCREMENT " );
                    } else {
                        createStatement.append( " PRIMARY KEY " );
                    }
                    break;
                case UNIQUE:
                    createStatement.append( " UNIQUE " );
                    break;
                default:
                    if( column.hasDefaultValue() ) {
                        createStatement.append( " DEFAULT " );
                        createStatement.append( column.getDefaultLiteral() );
                    }
                    break;
            }
        }

        createStatement.append( ")" );

        return createStatement.toString();
    }

    private String buildUpgradeStatement( final String tableName, final Column column ) {
        final StringBuilder upgradeStatement = new StringBuilder( "ALTER TABLE " );
        upgradeStatement.append( tableName );
        upgradeStatement.append( " ADD COLUMN " );
        upgradeStatement.append( column.name );
        upgradeStatement.append( " " );
        upgradeStatement.append( column.type.name() );
        if( column.hasDefaultValue() ) {
            upgradeStatement.append( " DEFAULT " );
            upgradeStatement.append( column.getDefaultLiteral() );
        }

        return upgradeStatement.toString();
    }

    protected Cursor queryEntries( final QueryBuilder query ) {
        return getWritableDatabase().rawQuery( query.build(), null );
    }

    protected boolean resetToFirst( final Cursor cursor ) {
        return !( cursor == null || cursor.getCount() == 0 ) && cursor.moveToFirst();
    }

    protected String escapeTextLiteral( String text ) {
        return text.replace( "'", "''" );
    }

    protected boolean hasEntries( String whereExpr ) {
        final String query = new QueryBuilder().addResultExpr( "count(*)" ).setWhereWhereExpr( whereExpr ).build();
        final Cursor cursor = getWritableDatabase().rawQuery( query, null );
        try {
            return cursor != null && cursor.getCount() > 0;
        } finally {
            if( cursor != null ) {
                cursor.close();
            }
        }
    }

    protected void insertEntry( final ContentValues values ) {
        begin();
        getWritableDatabase().insertWithOnConflict( mTableName, null, values, SQLiteDatabase.CONFLICT_IGNORE );
    }

    protected void removeEntry( final Column column, final String value ) {
        begin();
        getWritableDatabase().delete( mTableName, column.name + " =  '" + value + "'", null );
    }

    protected void updateEntry( final ContentValues values, final String whereExpr ) {
        begin();
        getWritableDatabase().updateWithOnConflict( mTableName, values, whereExpr, null,
                                                    SQLiteDatabase.CONFLICT_IGNORE );
    }

    protected void deleteEntries( final String where ) {
        begin();
        getWritableDatabase().delete( mTableName, where, null );
    }

    protected void deleteAllEntries() {
        deleteEntries( null );
    }

    protected static class Column {
        public final String     name;
        public final ColumnType type;
        public final String     defaultValue;
        public final ColumnFlag flag;

        public Column( final String name ) {
            this( name, ColumnType.TEXT, null, ColumnFlag.NONE );
        }

        public Column( final String name, final ColumnFlag flag ) {
            this( name, ColumnType.TEXT, null, flag );
        }

        public Column( final String name, final ColumnType type, final ColumnFlag flag ) {
            this( name, type, null, flag );
        }

        public Column( final String name, final ColumnType type, final String defaultValue ) {
            this( name, type, defaultValue, ColumnFlag.NONE );
        }

        private Column( final String name, final ColumnType type, final String defaultValue, final ColumnFlag flag ) {
            this.name = name;
            this.type = type;
            this.defaultValue = defaultValue;
            this.flag = flag;
        }

        public String getDefaultLiteral() {
            if( defaultValue == null ) {
                return "NULL";
            }
            if( type.isNumber() ) {
                return defaultValue;
            }
            return "'" + defaultValue + "'";
        }

        public boolean hasDefaultValue() {
            return defaultValue != null;
        }

    }

    protected static enum ColumnFlag {
        NONE, PRIMARY_KEY, UNIQUE
    }

    protected static enum ColumnType {
        TEXT, INTEGER, REAL, BLOB;

        public boolean isNumber() {
            switch( this ) {
                case INTEGER:
                case REAL:
                    return true;
                default:
                    return false;
            }
        }
    }

    protected class QueryBuilder {

        private StringBuilder mResultExpr  = null;
        private String        mWhereExpr   = null;
        private String        mOrderExpr   = null;
        private String        mGroupByExpr = null;

        public QueryBuilder addResultExpr( final String result ) {
            if( mResultExpr == null ) {
                mResultExpr = new StringBuilder( result );
            } else {
                mResultExpr.append( ", " );
                mResultExpr.append( result );
            }

            return this;
        }

        public QueryBuilder addResultColumn( final Column column ) {
            if( mResultExpr == null ) {
                mResultExpr = new StringBuilder( column.name );
            } else {
                mResultExpr.append( ", " );
                mResultExpr.append( column.name );
            }

            return this;
        }

        public QueryBuilder setWhereWhereExpr( final String whereExpr ) {
            this.mWhereExpr = whereExpr;

            return this;
        }

        public QueryBuilder setOrderByColumn( final Column column, final OrderDirection orderDirection ) {
            mOrderExpr = column.name + " " + orderDirection.name();

            return this;
        }

        //        public QueryBuilder setGroupByColumn(final Column column) {
        //            mGroupByExpr = column.name;
        //
        //            return this;
        //        }


        protected String build() {
            final StringBuilder query = new StringBuilder( "SELECT " );
            query.append( mResultExpr != null ? mResultExpr.toString() : "*" );
            query.append( " FROM " );
            query.append( mTableName );
            if( mWhereExpr != null ) {
                query.append( " WHERE " );
                query.append( mWhereExpr );
            }
            if( mGroupByExpr != null ) {
                query.append( " GROUP BY " );
                query.append( mGroupByExpr );
            }
            if( mOrderExpr != null ) {
                query.append( " ORDER BY " );
                query.append( mOrderExpr );
            }

            return query.toString();
        }

        @Override
        public String toString() {
            return build();
        }
    }

}
