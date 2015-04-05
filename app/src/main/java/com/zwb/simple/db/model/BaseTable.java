package com.zwb.simple.db.model;

import android.content.ContentValues;

import com.zwb.simple.db.DatabaseStore;
import com.zwb.simple.db.annotation.Column;
import com.zwb.simple.db.annotation.ColumnType;
import com.zwb.simple.db.annotation.Table;
import com.zwb.simple.db.exception.NoSuchTableException;
import com.zwb.simple.db.utils.LogUtil;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * 所有表的基类，目前只是实现了删除和保存的基本操作
 * Created by pc on 2015/3/5.
 */
public class BaseTable {

    /**
     * 删除
     *
     * @throws NoSuchTableException
     */
    public void delete() throws NoSuchTableException {
        Map<String, Object> valueMap = new HashMap<String, Object>();
        String tableName = "";
        java.lang.reflect.Field[] fields = this.getClass().getDeclaredFields();
        if (this.getClass().isAnnotationPresent(Table.class)) {
            Table table = this.getClass().getAnnotation(Table.class);
            tableName = table.table();
            if (tableName.length() == 0) {
                throw new NoSuchTableException("The table + " + getClass().getSimpleName() + " is not exist");
            }
        }

        for (java.lang.reflect.Field field : fields) {
            if (field.isAnnotationPresent(Column.class)) {
                Column meta = field.getAnnotation(Column.class);
                String column = meta.column();
                if (column.equals("")) {
                    column = field.getName();
                }
                field.setAccessible(true);
                Object value = null;
                try {
                    value = field.get(this);
                } catch (IllegalAccessException e) {
                    LogUtil.e(e.toString());
                }
                valueMap.put(column, value);
            }
        }
        DatabaseStore.getInstance().delete(tableName, valueMap);
    }

    /**
     * 保存
     *
     * @throws NoSuchTableException
     */
    public void save() throws NoSuchTableException {
        String tableName = "";
        Field[] fields = this.getClass().getDeclaredFields();
        if (this.getClass().isAnnotationPresent(Table.class)) {
            Table table = this.getClass().getAnnotation(Table.class);
            tableName = table.table();
            if (tableName.length() == 0) {
                throw new NoSuchTableException("The table + " + getClass().getSimpleName() + " is not exist");
            }
        }

        ContentValues values = new ContentValues();
        for (java.lang.reflect.Field field : fields) {
            if (field.isAnnotationPresent(Column.class)) {
                Column meta = field.getAnnotation(Column.class);
                String column = meta.column();
                if (column.equals("")) {
                    column = field.getName();
                }

                String type = "";
                if (field.isAnnotationPresent(ColumnType.class)) {
                    ColumnType fieldType = field.getAnnotation(ColumnType.class);
                    type = fieldType.ColumnType();
                } else {
                    type = field.getType().getName();
                }
                field.setAccessible(true);
                if (!type.equals("")) {
                    Object value = null;
                    try {
                        value = field.get(this);
                    } catch (IllegalAccessException e) {
                        LogUtil.e(e.toString());
                    }
                    if (type.contains("String")) {
                        values.put(column, value.toString());
                    } else if (type.equals("int")) {
                        values.put(column, (int) value);
                    } else if (type.equals("double")) {
                        values.put(column, (double) value);
                    } else if (type.equals("float")) {
                        values.put(column, (float) value);
                    } else if (type.equals("boolean")) {
                        values.put(column, (boolean) value);
                    } else if (type.equals("long")) {
                        values.put(column, (long) value);
                    } else if (type.equals("short")) {
                        values.put(column, (short) value);
                    }
                }
            }
        }
        DatabaseStore.getInstance().save(tableName, values);
    }
}
