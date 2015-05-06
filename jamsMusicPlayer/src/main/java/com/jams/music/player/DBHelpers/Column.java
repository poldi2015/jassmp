package com.jams.music.player.DBHelpers;

import android.content.Context;

public class Column {
    public final  String                           name;
    public final  AbstractTableAccessor.ColumnType type;
    private       String                           defaultValue;
    private final int                              defaultResource;
    public final  AbstractTableAccessor.ColumnFlag flag;

    public Column( final String name ) {
        this( name, AbstractTableAccessor.ColumnType.TEXT, null, -1, AbstractTableAccessor.ColumnFlag.NONE );
    }

    public Column( final String name, final AbstractTableAccessor.ColumnFlag flag ) {
        this( name, AbstractTableAccessor.ColumnType.TEXT, null, -1, flag );
    }

    public Column( final String name, final String defaultValue ) {
        this( name, AbstractTableAccessor.ColumnType.TEXT, defaultValue, -1, AbstractTableAccessor.ColumnFlag.NONE );
    }

    public Column( final String name, final int defaultResource ) {
        this( name, AbstractTableAccessor.ColumnType.TEXT, null, defaultResource,
              AbstractTableAccessor.ColumnFlag.NONE );
    }

    public Column( final String name, final AbstractTableAccessor.ColumnType type, final AbstractTableAccessor.ColumnFlag flag ) {
        this( name, type, null, -1, flag );
    }

    public Column( final String name, final AbstractTableAccessor.ColumnType type, final String defaultValue ) {
        this( name, type, defaultValue, -1, AbstractTableAccessor.ColumnFlag.NONE );
    }

    public Column( final String name, final AbstractTableAccessor.ColumnType type, final int defaultResource ) {
        this( name, type, null, defaultResource, AbstractTableAccessor.ColumnFlag.NONE );
    }

    private Column( final String name, final AbstractTableAccessor.ColumnType type, final String defaultValue, final int defaultResource, final AbstractTableAccessor.ColumnFlag flag ) {
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
}
