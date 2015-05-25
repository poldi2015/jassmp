package com.jassmp.Dao;

import android.content.Context;

public class Column {
    public final  String     name;
    public final  ColumnType type;
    private       String     defaultValue;
    private final int        defaultResource;
    public final  ColumnFlag flag;

    public Column( final String name ) {
        this( name, ColumnType.TEXT, null, -1, ColumnFlag.NONE );
    }

    public Column( final String name, final ColumnFlag flag ) {
        this( name, ColumnType.TEXT, null, -1, flag );
    }

    public Column( final String name, final String defaultValue ) {
        this( name, ColumnType.TEXT, defaultValue, -1, ColumnFlag.NONE );
    }

    public Column( final String name, final int defaultResource ) {
        this( name, ColumnType.TEXT, null, defaultResource, ColumnFlag.NONE );
    }

    public Column( final String name, final ColumnType type, final ColumnFlag flag ) {
        this( name, type, null, -1, flag );
    }

    public Column( final String name, final ColumnType type, final String defaultValue ) {
        this( name, type, defaultValue, -1, ColumnFlag.NONE );
    }

    public Column( final String name, final ColumnType type, final int defaultResource ) {
        this( name, type, null, defaultResource, ColumnFlag.NONE );
    }

    private Column( final String name, final ColumnType type, final String defaultValue, final int defaultResource, final ColumnFlag flag ) {
        this.name = name;
        this.type = type;
        this.defaultValue = defaultValue;
        this.defaultResource = defaultResource;
        this.flag = flag;
    }

    public String getDefaultLiteral( final Context context ) {
        if( defaultValue == null && defaultResource != -1 ) {
            defaultValue = context.getResources().getString( defaultResource );
        }
        if( defaultValue == null ) {
            defaultValue = "NULL";
        }

        if( type.isNumber() ) {
            return defaultValue;
        }
        return "'" + defaultValue + "'";
    }

    public boolean hasDefaultValue() {
        return defaultValue != null;
    }

    @Override
    public String toString() {
        return "Column( " +
               "name='" + name + '\'' +
               ", type=" + type +
               ", defaultValue='" + defaultValue + '\'' +
               ", flag=" + flag +
               " )";
    }

    public static enum ColumnFlag {
        NONE, PRIMARY_KEY, UNIQUE
    }

    public static enum ColumnType {
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
}
