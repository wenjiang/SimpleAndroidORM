package com.zwb.simple.db.model;

import android.util.Log;

import com.zwb.simple.db.DatabaseStore;
import com.zwb.simple.db.annotation.Column;
import com.zwb.simple.db.annotation.ColumnType;
import com.zwb.simple.db.annotation.Table;
import com.zwb.simple.db.exception.NoSuchTableException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by pc on 2015/3/5.
 */
public class BaseTable {

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
                    Log.e("BaseEntity", e.toString());
                }
                valueMap.put(column, value);
            }
        }
        DatabaseStore.getInstance().delete(tableName, valueMap);
    }

    public void save() throws NoSuchTableException {
        List<String> columnList = new ArrayList<String>();
        Map<String, ColumnValuePair> valueMap = new HashMap<String, ColumnValuePair>();
        String tableName = "";
        StringBuilder insertSql = new StringBuilder("insert into ");
        java.lang.reflect.Field[] fields = this.getClass().getDeclaredFields();
        if (this.getClass().isAnnotationPresent(Table.class)) {
            Table table = this.getClass().getAnnotation(Table.class);
            tableName = table.table();
            if (tableName.length() == 0) {
                throw new NoSuchTableException("The table + " + getClass().getSimpleName() + " is not exist");
            }
        }

        insertSql.append(tableName + " (");
        for (java.lang.reflect.Field field : fields) {
            ColumnValuePair pair = new ColumnValuePair();
            if (field.isAnnotationPresent(Column.class)) {
                Column meta = field.getAnnotation(Column.class);
                String column = meta.column();
                if (column.equals("")) {
                    column = field.getName();
                }
                columnList.add(column);
                String fieldClass = "";
                if (field.isAnnotationPresent(ColumnType.class)) {
                    ColumnType fieldType = field.getAnnotation(ColumnType.class);
                    fieldClass = fieldType.ColumnType();
                }
                field.setAccessible(true);
                if (fieldClass.equals("")) {
                    String type = field.getType().getName();
                    Object value = getRealType(type, field);
                    pair.type = type;
                    pair.value = value;
                    continue;
                }

                Object value = getRealType(fieldClass, field);
                pair.type = fieldClass;
                pair.value = value;
            }

            valueMap.put(field.getName(), pair);
        }

        int length = columnList.size();
        for (int i = 0; i < length; i++) {
            if (i == length - 1) {
                insertSql.append(columnList.get(i) + ") values (");
                break;
            }
            insertSql.append(columnList.get(i) + ",");
        }

        for (int i = 0; i < length; i++) {
            if (i == length - 1) {
                insertSql.append("?)");
                break;
            }
            insertSql.append("?,");
        }

        DatabaseStore.getInstance().save(insertSql.toString(), valueMap);
    }

    private Object getRealType(String type, java.lang.reflect.Field field) {
        Object value = null;
        try {
            if (type.equals("String")) {
                value = field.get(this).toString();
            } else if (type.equals("int")) {
                value = Integer.valueOf(field.get(this).toString());
            } else if (type.equals("float")) {
                value = Float.valueOf(field.get(this).toString());
            } else if (type.equals("double")) {
                value = Double.valueOf(field.get(this).toString());
            } else if (type.equals("long")) {
                value = Long.valueOf(field.get(this).toString());
            } else if (type.equals("boolean")) {
                value = Boolean.valueOf(field.get(this).toString());
            } else if (type.equals("short")) {
                value = Short.valueOf(field.get(this).toString());
            } else {
                return null;
            }
        } catch (Exception e) {
            Log.e("BaseEntity", e.toString());
        }
        return value;
    }

    public class ColumnValuePair {
        String type;
        Object value;

        public String getType() {
            return type;
        }

        public Object getValue() {
            return value;
        }
    }
}
