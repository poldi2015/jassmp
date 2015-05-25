package com.jassmp.JassMpDb;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.jassmp.Dao.Column;

public abstract class AbstractTableAccessor {

    //
    // defines

    public static final String TAG = AbstractTableAccessor.class.getSimpleName();

    public static final Column COLUMN_ID = new Column( "_id", Column.ColumnType.INTEGER,
                                                       Column.ColumnFlag.PRIMARY_KEY );


    //
    // private members

    private final String           mTableName;
    private final DatabaseAccessor mDatabaseAccessor;

    protected AbstractTableAccessor( final String tableName, final Context context ) {
        mDatabaseAccessor = DatabaseAccessor.getInstance( context );
        mTableName = tableName;
    }

    protected Context getContext() {
        return mDatabaseAccessor.getContext();
    }

    public String getTableName() {
        return mTableName;
    }

    public abstract Column[] getTableColumns();

    private void begin() {
        if( !mDatabaseAccessor.getDatabase().inTransaction() ) {
            mDatabaseAccessor.getDatabase().beginTransaction();
        }
    }

    protected void beginNonExclusive() {
        if( !mDatabaseAccessor.getDatabase().inTransaction() ) {
            mDatabaseAccessor.getDatabase().beginTransactionNonExclusive();
        }
    }

    public void commit() {
        if( mDatabaseAccessor.getDatabase().inTransaction() ) {
            mDatabaseAccessor.getDatabase().setTransactionSuccessful();
            mDatabaseAccessor.getDatabase().endTransaction();
        }
    }

    /**
     * Does a {@link android.database.sqlite.SQLiteDatabase#yieldIfContendedSafely()} if within a transaction
     */
    public void yieldCommit() {
        if( mDatabaseAccessor.getDatabase().inTransaction() ) {
            mDatabaseAccessor.getDatabase().yieldIfContendedSafely();
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
                    if( column.type == Column.ColumnType.INTEGER ) {
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

    protected int queryId( final WhereBuilder whereExpr ) {
        final QueryBuilder builder = new QueryBuilder().result( COLUMN_ID ).where( whereExpr );

        return (int) DatabaseUtils.longForQuery( mDatabaseAccessor.getDatabase(), builder.build(), null );
    }

    protected boolean resetToFirst( final Cursor cursor ) {
        return !( cursor == null || cursor.getCount() == 0 ) && cursor.moveToFirst();
    }

    protected String escapeText( final String text ) {
        return text.replace( "'", "''" );
    }

    protected String escapeAndQuoteText( final String text ) {
        return DatabaseUtils.sqlEscapeString( text );
    }

    protected int getNumberOfEntries( final WhereBuilder whereExpr ) {
        final QueryBuilder builder = new QueryBuilder().count();
        if( whereExpr != null ) {
            builder.where( whereExpr );
        }
        return (int) DatabaseUtils.longForQuery( mDatabaseAccessor.getDatabase(), builder.build(), null );
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

    protected void updateEntry( final ContentValues values, final UnqualifiedWhereBuilder whereExpr ) {
        begin();
        mDatabaseAccessor.getDatabase()
                         .updateWithOnConflict( mTableName, values, whereExpr != null ? whereExpr.build() : null, null,
                                                SQLiteDatabase.CONFLICT_IGNORE );
    }

    protected void removeEntries( final UnqualifiedWhereBuilder whereExpr ) {
        begin();
        mDatabaseAccessor.getDatabase().delete( mTableName, whereExpr != null ? whereExpr.build() : null, null );
    }

    protected void deleteAllEntries() {
        removeEntries( null );
    }

    protected class WhereBuilder<T extends WhereBuilder> {

        //
        // private members

        private StringBuilder mWhereExpr = null;

        public T where( final WhereBuilder whereExpr ) {
            mWhereExpr = whereExpr.mWhereExpr;
            return (T) this;
        }

        protected T where( final String whereExpr ) {
            if( whereExpr == null ) {
                return (T) this;
            }
            mWhereExpr = new StringBuilder( whereExpr );

            return (T) this;
        }

        protected T appendWhere( final String whereExpr ) {
            if( whereExpr == null ) {
                return (T) this;
            }
            if( mWhereExpr == null ) {
                where( whereExpr );
            } else {
                mWhereExpr.append( whereExpr );
            }

            return (T) this;
        }

        public T whereAnd() {
            if( mWhereExpr != null ) {
                appendWhere( " AND " );
            }
            return (T) this;
        }

        @SuppressWarnings("unused")
        public T whereOr() {
            if( mWhereExpr != null ) {
                appendWhere( " OR " );
            }
            return (T) this;
        }

        public T whereEq() {
            appendWhere( " = " );
            return (T) this;
        }

        @SuppressWarnings("unused")
        public T whereNe() {
            appendWhere( " != " );
            return (T) this;
        }

        public T where( final Column column ) {
            where( null, column );
            return (T) this;
        }

        public T where( final String tableAs, final Column column ) {
            if( tableAs != null ) {
                appendWhere( tableAs );
                appendWhere( "." );
            } else {
                appendWhere( "this." );
            }
            appendWhere( column.name );
            return (T) this;
        }

        @SuppressWarnings("unused")
        public T whereText( final String text ) {
            appendWhere( escapeAndQuoteText( text ) );
            return (T) this;
        }

        public T whereText( final int number ) {
            appendWhere( Integer.toString( number ) );
            return (T) this;
        }

        protected String build() {
            return mWhereExpr != null ? mWhereExpr.toString() : null;
        }

        @Override
        public String toString() {
            return build();
        }
    }

    protected class UnqualifiedWhereBuilder extends WhereBuilder<UnqualifiedWhereBuilder> {
        public UnqualifiedWhereBuilder where( final Column column ) {
            appendWhere( column.name );
            return this;
        }

        public UnqualifiedWhereBuilder where( final String tableAs, final Column column ) {
            throw new RuntimeException( "Additional tables cannot be used in UnqualifiedWhereBuilder" );
        }


    }

    protected class QueryBuilder extends WhereBuilder<QueryBuilder> {

        private StringBuilder mResultExpr       = null;
        private StringBuilder mAdditionalTables = null;
        private String        mOrderExpr        = null;
        private String        mGroupByExpr      = null;

        public QueryBuilder count() {
            result( "count(*)" );
            return this;
        }

        private QueryBuilder result( final String result ) {
            if( mResultExpr == null ) {
                mResultExpr = new StringBuilder( result );
            } else {
                mResultExpr.append( ", " );
                mResultExpr.append( result );
            }

            return this;
        }

        public QueryBuilder result( final Column[] columns ) {
            for( final Column column : columns ) {
                result( column );
            }
            return this;
        }

        public QueryBuilder result( final Column column ) {
            if( mResultExpr == null ) {
                mResultExpr = new StringBuilder( "this." );
            } else {
                mResultExpr.append( ", this." );
            }
            mResultExpr.append( column.name );

            return this;
        }

        public QueryBuilder table( final String tableName, final String as ) {
            if( mAdditionalTables == null ) {
                mAdditionalTables = new StringBuilder();
            }
            mAdditionalTables.append( ", " );
            mAdditionalTables.append( tableName );
            mAdditionalTables.append( " AS " );
            mAdditionalTables.append( as );

            return this;
        }


        public QueryBuilder order( final Column column, final OrderDirection orderDirection ) {
            if( column != null ) {
                mOrderExpr = column.name + " " + ( orderDirection != null ? orderDirection.name()
                                                                          : OrderDirection.ASC );
            }

            return this;
        }

        @SuppressWarnings( "unused" )
        public QueryBuilder setGroupByColumn( final Column column ) {
            mGroupByExpr = column.name;

            return this;
        }


        protected String build() {
            final StringBuilder query = new StringBuilder( "SELECT " );
            query.append( mResultExpr != null ? mResultExpr.toString() : "*" );
            query.append( " FROM " );
            query.append( mTableName );
            query.append( " AS " );
            query.append( " this " );
            if( mAdditionalTables != null ) {
                query.append( mAdditionalTables.toString() );
            }
            final String whereExpr = super.build();
            if( whereExpr != null ) {
                query.append( " WHERE " );
                query.append( whereExpr );
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
