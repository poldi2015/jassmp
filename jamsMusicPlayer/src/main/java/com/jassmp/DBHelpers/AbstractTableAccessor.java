package com.jassmp.DBHelpers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public abstract class AbstractTableAccessor {

    //
    // defines

    public static final String TAG = AbstractTableAccessor.class.getSimpleName();

    public static final Column COLUMN_ID = new Column( "_id", ColumnType.INTEGER, ColumnFlag.PRIMARY_KEY );


    //
    // private members

    private final String           mTableName;
    private final DatabaseAccessor mDatabaseAccessor;

    protected AbstractTableAccessor( final String tableName, final Context context ) {
        mDatabaseAccessor = DatabaseAccessor.getInstance( context );
        mTableName = tableName;
    }

    protected abstract Column[] getTableColumns();

    private void begin() {
        if( !mDatabaseAccessor.getDatabase().inTransaction() ) {
            mDatabaseAccessor.getDatabase().beginTransaction();
        }
    }

    public void commit() {
        if( mDatabaseAccessor.getDatabase().inTransaction() ) {
            mDatabaseAccessor.getDatabase().setTransactionSuccessful();
            mDatabaseAccessor.getDatabase().endTransaction();
        }
    }

    public void onCreate( final SQLiteDatabase db ) {
        final String dropTableStatement = "DROP TABLE IF EXISTS " + mTableName;
        db.execSQL( dropTableStatement );
        final String createStatement = buildCreateStatement( mTableName, getTableColumns() );
        db.execSQL( createStatement );
    }


    public void onUpgrade( final SQLiteDatabase db, final int oldVersion, final int newVersion ) {
        if( oldVersion != newVersion ) {
            onCreate( db );
        }
    }

    protected void finalize() throws Throwable {
        try {
            mDatabaseAccessor.getDatabase().close();
            super.finalize();
        } catch( Exception e ) {
            Log.e( TAG, "Failed to close database", e );
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
                        createStatement.append( column.getDefaultLiteral( mDatabaseAccessor.getContext() ) );
                    }
                    break;
            }
        }

        createStatement.append( ")" );

        return createStatement.toString();
    }

    protected Cursor queryEntries( final QueryBuilder query ) {
        return mDatabaseAccessor.getDatabase().rawQuery( query.build(), null );
    }

    protected boolean resetToFirst( final Cursor cursor ) {
        return !( cursor == null || cursor.getCount() == 0 ) && cursor.moveToFirst();
    }

    protected String escapeTextLiteral( String text ) {
        return text.replace( "'", "''" );
    }

    protected boolean hasEntries( String whereExpr ) {
        final String query = new QueryBuilder().addResultExpr( "count(*)" ).setWhereWhereExpr( whereExpr ).build();
        final Cursor cursor = mDatabaseAccessor.getDatabase().rawQuery( query, null );
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
        mDatabaseAccessor.getDatabase()
                         .insertWithOnConflict( mTableName, null, values, SQLiteDatabase.CONFLICT_IGNORE );
    }

    protected void replaceEntry( final ContentValues values ) {
        begin();
        mDatabaseAccessor.getDatabase()
                         .insertWithOnConflict( mTableName, null, values, SQLiteDatabase.CONFLICT_REPLACE );
    }

    protected void removeEntry( final Column column, final String value ) {
        begin();
        mDatabaseAccessor.getDatabase().delete( mTableName, column.name + " =  '" + value + "'", null );
    }

    protected void updateEntry( final ContentValues values, final String whereExpr ) {
        begin();
        mDatabaseAccessor.getDatabase()
                         .updateWithOnConflict( mTableName, values, whereExpr, null, SQLiteDatabase.CONFLICT_IGNORE );
    }

    protected void deleteEntries( final String where ) {
        begin();
        mDatabaseAccessor.getDatabase().delete( mTableName, where, null );
    }

    protected void deleteAllEntries() {
        deleteEntries( null );
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
