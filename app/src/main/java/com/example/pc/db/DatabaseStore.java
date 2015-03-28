package com.example.pc.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import com.example.pc.model.BaseEntity;
import com.example.pc.sqlpratice.FieldType;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by pc on 2015/2/9.
 */
public class DatabaseStore {
    private static DatabaseStore store;
    private String whereStr = "";
    private String orderStr = "";
    private String table = "";
    private BaseSQLiteOpenHelper helper;
    private SQLiteDatabase db;

    private DatabaseStore() {
    }

    public static DatabaseStore getInstance() {
        if (store == null) {
            store = new DatabaseStore();
        }

        return store;
    }

    public void init(Context context) {
        helper = BaseSQLiteOpenHelper.getInstance(context);
        db = helper.getWritableDatabase();
        db.beginTransaction();
    }

    public <T> List<T> findAll(Class<T> clazz) throws BaseSQLiteException {
        if (table.equals("")) {
            throw new BaseSQLiteException("Please call from() at the first");
        }
        Field[] fields = clazz.getDeclaredFields();
        List<String> fieldNames = new ArrayList<String>();
        Map<String, String> types = new HashMap<String, String>();
        for (Field field : fields) {
            fieldNames.add(field.getName());
            if (field.isAnnotationPresent(FieldType.class)) {
                FieldType fieldType = field.getAnnotation(FieldType.class);
                types.put(field.getName().toLowerCase(), fieldType.ColumnType());
            }
        }

        List<Method> setMethods = getSetMethods(clazz);
        Cursor cursor = db.query(table, null, null, null, null, null, null);//查询并获得游标
        List<T> list = getList(clazz, cursor, setMethods, fieldNames, fields, types);
        cursor.close();
        table = "";
        return list;
    }

    private Constructor<?> findBestSuitConstructor(Class<?> modelClass) {
        Constructor<?> finalConstructor = null;
        Constructor<?>[] constructors = modelClass.getConstructors();
        for (Constructor<?> constructor : constructors) {
            if (finalConstructor == null) {
                finalConstructor = constructor;
            } else {
                int finalParamLength = finalConstructor.getParameterTypes().length;
                int newParamLength = constructor.getParameterTypes().length;
                if (newParamLength < finalParamLength) {
                    finalConstructor = constructor;
                }
            }
        }
        finalConstructor.setAccessible(true);
        return finalConstructor;
    }

    public DatabaseStore where(String key, Object value) {
        whereStr += " where " + key + " like '%" + value + "%'";
        return this;
    }

    public <T> List<T> find(Class<T> clazz) throws BaseSQLiteException {
        if (table.equals("")) {
            throw new BaseSQLiteException("Please call from() at the first");
        }
        if (whereStr.equals("") && orderStr.equals("")) {
            throw new BaseSQLiteException("There are not any conditions before find method invoked");
        }

        String sql = "select * from " + table + (whereStr.equals("") ? "" : whereStr) + (orderStr.equals("") ? "" : orderStr);
        Cursor cursor = db.rawQuery(sql, null);
        Field[] fields = clazz.getDeclaredFields();
        List<String> fieldNames = new ArrayList<String>();
        Map<String, String> types = new HashMap<String, String>();
        for (Field field : fields) {
            fieldNames.add(field.getName());
            if (field.isAnnotationPresent(FieldType.class)) {
                FieldType fieldType = field.getAnnotation(FieldType.class);
                types.put(field.getName().toLowerCase(), fieldType.ColumnType());
            }
        }

        List<Method> setMethods = getSetMethods(clazz);
        List<T> list = getList(clazz, cursor, setMethods, fieldNames, fields, types);
        cursor.close();
        whereStr = "";
        orderStr = "";
        table = "";
        return list;
    }

    private <T> List<T> getList(Class<T> clazz, Cursor cursor, List<Method> methods, List<String> fieldNames, Field[] fields, Map<String, String> types) {
        List<T> list = new ArrayList<T>();
        Constructor<?> constructor = findBestSuitConstructor(clazz);
        Set<String> keySet = types.keySet();
        while (cursor.moveToNext()) {
            try {
                T data = (T) constructor
                        .newInstance();
                for (Method method : methods) {
                    String name = method.getName();
                    String valueName = name.substring(3).substring(0, 1).toLowerCase() + name.substring(4);
                    String type = null;
                    String fieldType = null;
                    int index = 0;
                    if (fieldNames.contains(valueName)) {
                        index = fieldNames.indexOf(valueName);
                        type = fields[index].getGenericType().toString();
                        if (keySet.contains(valueName)) {
                            fieldType = types.get(valueName);
                        }
                    }
                    Object value = getColumnValue(cursor, valueName, type, fieldType);
                    fields[index].setAccessible(true);
                    fields[index].set(data, value);
                }

                list.add(data);
            } catch (InstantiationException e) {
                Log.e("DatabaseStore", e.toString());
            } catch (IllegalAccessException e) {
                Log.e("DatabaseStore", e.toString());
            } catch (InvocationTargetException e) {
                Log.e("DatabaseStore", e.toString());
            } catch (JSONException e) {
                Log.e("DatabaseStore", e.toString());
            }
        }
        return list;
    }

    private List<Method> getSetMethods(Class clazz) {
        Method[] allMethods = clazz.getMethods();
        List<Method> setMethods = new ArrayList<Method>();
        for (Method method : allMethods) {
            String name = method.getName();

            if (name.contains("set") && !name.equals("offset")) {
                setMethods.add(method);
                continue;
            }
        }

        return setMethods;
    }

    public DatabaseStore order(String column) {
        orderStr += " order by " + column;
        return this;
    }

    public int count() throws BaseSQLiteException {
        if (table.equals("")) {
            throw new BaseSQLiteException("Please call from() at the first");
        }
        int count = 0;
        String sql = " select count(1) from " + table + (whereStr.equals("") ? "" : whereStr);
        Cursor cursor = db.rawQuery(sql, null);
        if (cursor.moveToNext()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        whereStr = "";
        table = "";
        return count;
    }

    public double average(String column) throws BaseSQLiteException {
        if (table.equals("")) {
            throw new BaseSQLiteException("Please call from() at the first");
        }
        double average = 0.0;
        String sql = " select avg( " + column + ") from " + table;
        Cursor cursor = db.rawQuery(sql, null);
        if (cursor.moveToNext()) {
            average = cursor.getDouble(0);
        }
        cursor.close();
        table = "";
        return average;
    }

    public void delete(String table, Map<String, Object> valueMap) {
        StringBuilder deleteSql = new StringBuilder("delete from " + table + " where ");
        String[] columnArr = valueMap.keySet().toArray(new String[]{});
        for (int i = 0, length = columnArr.length; i < length; i++) {
            String column = columnArr[i];
            Object value = valueMap.get(column);
            if (i == length - 1) {
                deleteSql.append(column + " = " + value);
                break;
            }
            deleteSql.append(column + " = " + value + " and ");
        }
        db.rawQuery(deleteSql.toString(), null);

    }

    public int deleteAll(String column, String value) throws BaseSQLiteException {
        if (table.equals("")) {
            throw new BaseSQLiteException("Please call from() at the first");
        }
        int count = db.delete(table, column + "= ?", new String[]{value});
        table = "";
        return count;
    }

    public int updateAll(ContentValues contentValues, String column, String value) throws BaseSQLiteException {
        if (table.equals("")) {
            throw new BaseSQLiteException("Please call from() at the first");
        }
        int count = db.update(table, contentValues, column + "=?", new String[]{value});
        table = "";
        return count;
    }

    public <T extends BaseEntity> void saveAll(Collection<T> collection) throws Exception {
        BaseEntity[] array = collection.toArray(new BaseEntity[0]);
        for (BaseEntity data : array) {
            data.save();
        }
    }

    public Object findColumn(String content, Class<?> valueType) throws BaseSQLiteException {
        if (table.equals("")) {
            throw new BaseSQLiteException("Please call from() at the first");
        }
        String sql = " select " + content + " from " + table + (whereStr.equals("") ? "" : whereStr);
        Cursor cursor = db.rawQuery(sql, null);
        Object data = null;
        try {
            data = getRightColumn(cursor, content, valueType);
        } catch (JSONException e) {

        }
        cursor.close();
        whereStr = "";
        table = "";
        return data;
    }

    public List findColumns(String content, Class<?> valueType) throws BaseSQLiteException {
        if (table.equals("")) {
            throw new BaseSQLiteException("Please call from() at the first");
        }
        String sql = " select " + content + " from " + table + (whereStr.equals("") ? "" : whereStr);
        Cursor cursor = db.rawQuery(sql, null);
        List list = new ArrayList();
        while (cursor.moveToNext()) {
            try {
                list.add(getColumnValue(cursor, content, null, valueType.getSimpleName()));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        cursor.close();
        whereStr = "";
        table = "";
        return list;
    }

    public Object findLastColumn(String column, Class<?> valueType) throws BaseSQLiteException {
        if (table.equals("")) {
            throw new BaseSQLiteException("Please call from() at the first");
        }
        String sql = " select " + column + " from " + table + " " + (whereStr.equals("") ? "" : whereStr) + " order by id desc limit 0,1";
        Cursor cursor = db.rawQuery(sql, null);
        Object data = null;
        try {
            data = getRightColumn(cursor, column, valueType);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        cursor.close();
        whereStr = "";
        table = "";
        return data;
    }

    public Object findFirstColumn(String column, Class<?> valueType) throws BaseSQLiteException {
        if (table.equals("")) {
            throw new BaseSQLiteException("Please call from() at the first");
        }
        String sql = " select " + column + " from " + table + " " + (whereStr.equals("") ? "" : whereStr) + " order by id limit 0,1";
        Cursor cursor = db.rawQuery(sql, null);
        Object data = null;
        try {
            data = getRightColumn(cursor, column, valueType);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        cursor.close();
        whereStr = "";
        table = "";
        return data;
    }

    public <T> Object findFirst(Class<T> clazz) throws BaseSQLiteException {
        if (table.equals("")) {
            throw new BaseSQLiteException("Please call from() at the first");
        }
        String sql = " select * from " + table + " " + (whereStr.equals("") ? "" : whereStr) + " order by id limit 0,1";
        Cursor cursor = db.rawQuery(sql, null);
        Field[] fields = clazz.getDeclaredFields();
        List<String> fieldNames = new ArrayList<String>();
        Map<String, String> typeMap = new HashMap<String, String>();
        for (Field field : fields) {
            if (field.isAnnotationPresent(FieldType.class)) {
                FieldType fieldType = field.getAnnotation(FieldType.class);
                typeMap.put(field.getName(), fieldType.ColumnType());
            }
            fieldNames.add(field.getName());
        }

        List<Method> setMethods = getSetMethods(clazz);
        List<T> list = getList(clazz, cursor, setMethods, fieldNames, fields, typeMap);
        cursor.close();
        whereStr = "";
        table = "";
        return list.get(0);
    }

    public <T> Object findLast(Class<T> clazz) throws BaseSQLiteException {
        if (table.equals("")) {
            throw new BaseSQLiteException("Please call from() at the first");
        }
        String sql = " select * from " + table + " " + (whereStr.equals("") ? "" : whereStr) + " order by id desc limit 0,1";
        Cursor cursor = db.rawQuery(sql, null);
        Field[] fields = clazz.getDeclaredFields();
        List<String> fieldNames = new ArrayList<String>();
        Map<String, String> types = new HashMap<String, String>();
        for (Field field : fields) {
            fieldNames.add(field.getName());
            if (field.isAnnotationPresent(FieldType.class)) {
                FieldType fieldType = field.getAnnotation(FieldType.class);
                types.put(field.getName().toLowerCase(), fieldType.ColumnType());
            }
        }

        List<Method> setMethods = getSetMethods(clazz);
        List<T> list = getList(clazz, cursor, setMethods, fieldNames, fields, types);
        cursor.close();
        whereStr = "";
        table = "";
        return list.get(0);
    }

    private Object getRightColumn(Cursor cursor, String column, Class<?> valueType) throws JSONException {
        Object data = null;
        if (cursor.moveToNext()) {
            data = getColumnValue(cursor, column, null, valueType.getSimpleName());
        }

        return data;
    }

    public Object getColumnValue(Cursor cursor, String column, String fieldType, String dbType) throws JSONException {
        Object data = null;

        if (dbType == null) {
            dbType = fieldType;
        }
        if (dbType.equals("String") || dbType.contains("String")) {
            data = cursor.getString(cursor.getColumnIndex(column));
            if (fieldType != null && fieldType.contains("JSONObject")) {
                data = new JSONObject((String) data);
            } else if (fieldType != null && fieldType.contains("JSONArray")) {
                data = new JSONArray((String) data);
            }
        } else if (dbType.equals("Integer") || dbType.equals("int")) {
            data = cursor.getInt(cursor.getColumnIndex(column));
        } else if (dbType.equals("Long") || dbType.equals("long")) {
            data = cursor.getLong(cursor.getColumnIndex(column));
        } else if (dbType.equals("Double") || dbType.equals("double")) {
            data = cursor.getDouble(cursor.getColumnIndex(column));
        } else if (dbType.equals("Float") || dbType.equals("float")) {
            data = cursor.getFloat(cursor.getColumnIndex(column));
        } else if (dbType.equals("Short") || dbType.equals("short")) {
            data = cursor.getShort(cursor.getColumnIndex(column));
        }
        return data;
    }

    public DatabaseStore from(String table) {
        this.table = table;
        return this;
    }

    public void save(String sql, Map<String, BaseEntity.ColumnValuePair> valueMap) {
        SQLiteStatement statement = db.compileStatement(sql);
        String[] columnArr = valueMap.keySet().toArray(new String[]{});
        for (int i = 0, length = columnArr.length; i < length; i++) {
            BaseEntity.ColumnValuePair pair = valueMap.get(columnArr[i]);
            String type = pair.getType();
            Object value = pair.getValue();
            if (type.equals("int") || type.equals("float") || type.equals("long") || type.equals("short")) {
                statement.bindLong(i + 1, (Long) value);
            } else if (type.equals("String")) {
                statement.bindString(i + 1, (String) value);
            } else if (type.equals("double")) {
                statement.bindDouble(i + 1, (Double) value);
            }

            statement.execute();
            statement.clearBindings();
        }
    }

    public void commit() {
        db.setTransactionSuccessful();
        db.endTransaction();
    }

    public void reset() {
        whereStr = "";
        orderStr = "";
        table = "";
    }
}
