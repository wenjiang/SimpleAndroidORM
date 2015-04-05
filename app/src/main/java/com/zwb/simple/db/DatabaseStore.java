package com.zwb.simple.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.zwb.simple.db.annotation.ColumnType;
import com.zwb.simple.db.exception.BaseSQLiteException;
import com.zwb.simple.db.model.BaseTable;
import com.zwb.simple.db.utils.LogUtil;

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
 * 数据库的操作类
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

    /**
     * DatabaseStore的单例
     *
     * @return DatabaseStore的单例
     */
    public static DatabaseStore getInstance() {
        if (store == null) {
            store = new DatabaseStore();
        }

        return store;
    }

    /**
     * 初始化
     *
     * @param context 上下文
     */
    public void init(Context context) {
        helper = BaseSQLiteOpenHelper.getInstance(context);
        db = helper.getWritableDatabase();
    }

    /**
     * 查询全部的数据
     *
     * @param clazz 要查询的表对象的class对象
     * @param <T>   表对象的类型
     * @return 表对象的List
     * @throws BaseSQLiteException
     */
    public <T> List<T> findAll(Class<T> clazz) throws BaseSQLiteException {
        if (table.equals("")) {
            throw new BaseSQLiteException("Please call from() at the first");
        }
        Field[] fields = clazz.getDeclaredFields();
        List<String> fieldNames = new ArrayList<String>();
        Map<String, String> types = new HashMap<String, String>();
        for (Field field : fields) {
            fieldNames.add(field.getName());
            if (field.isAnnotationPresent(ColumnType.class)) {
                ColumnType fieldType = field.getAnnotation(ColumnType.class);
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

    /**
     * 寻找最适合的构造器
     *
     * @param modelClass 表对象的class对象
     * @return 构造器
     */
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

    /**
     * where条件语句的拼接
     *
     * @param column 列名
     * @param value  值
     * @return DatabaseStore
     */
    public DatabaseStore where(String column, Object value) {
        whereStr += " where " + column + " like '%" + value + "%'";
        return this;
    }

    /**
     * 查询
     *
     * @param clazz 表对象的class对象
     * @param <T>   表对象的类型
     * @return 表对象的List
     * @throws BaseSQLiteException
     */
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
            if (field.isAnnotationPresent(ColumnType.class)) {
                ColumnType fieldType = field.getAnnotation(ColumnType.class);
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

    /**
     * 获取数据
     *
     * @param clazz      表对象的class对象
     * @param cursor     光标
     * @param methods    方法List
     * @param fieldNames 字段名
     * @param fields     字段数组
     * @param types      字段类型数组
     * @param <T>        表对象的类型
     * @return 表对象的List
     */
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
                LogUtil.e(e.toString());
            } catch (IllegalAccessException e) {
                LogUtil.e(e.toString());
            } catch (InvocationTargetException e) {
                LogUtil.e(e.toString());
            } catch (JSONException e) {
                LogUtil.e(e.toString());
            }
        }
        return list;
    }

    /**
     * 获取Set方法
     *
     * @param clazz 表对象的class对象
     * @return 方法数组
     */
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

    /**
     * order语句的拼接
     *
     * @param column 列名
     * @return DatabaseStore
     */
    public DatabaseStore order(String column) {
        orderStr += " order by " + column;
        return this;
    }

    /**
     * 数量
     *
     * @return 数量
     * @throws BaseSQLiteException
     */
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

    /**
     * 计算平均值
     *
     * @param column 列名
     * @return 平均值
     * @throws BaseSQLiteException
     */
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

    /**
     * 删除
     *
     * @param table    表名
     * @param valueMap 要删除的列名和值
     */
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

    /**
     * 删除全部
     *
     * @param column 列名
     * @param value  值
     * @return 被删除的数量
     * @throws BaseSQLiteException
     */
    public int deleteAll(String column, String value) throws BaseSQLiteException {
        if (table.equals("")) {
            throw new BaseSQLiteException("Please call from() at the first");
        }
        int count = db.delete(table, column + "= ?", new String[]{value});
        table = "";
        return count;
    }

    /**
     * 更新全部数据
     *
     * @param contentValues 更新的ContentValues
     * @param column        列名
     * @param value         值
     * @return 更新的数量
     * @throws BaseSQLiteException
     */
    public int updateAll(ContentValues contentValues, String column, String value) throws BaseSQLiteException {
        if (table.equals("")) {
            throw new BaseSQLiteException("Please call from() at the first");
        }
        int count = db.update(table, contentValues, column + "=?", new String[]{value});
        table = "";
        return count;
    }

    /**
     * 保存全部
     *
     * @param collection 数据集合
     * @param <T>        表的类型
     * @throws Exception
     */
    public <T extends BaseTable> void saveAll(Collection<T> collection) throws Exception {
        db.beginTransaction();
        BaseTable[] array = collection.toArray(new BaseTable[0]);
        for (BaseTable data : array) {
            data.save();
        }
        db.setTransactionSuccessful();
        db.endTransaction();
    }

    /**
     * 查询列的某个值
     *
     * @param column    列
     * @param valueType 值类型
     * @return 列的值
     * @throws BaseSQLiteException
     */
    public Object findColumn(String column, Class<?> valueType) throws BaseSQLiteException {
        if (table.equals("")) {
            throw new BaseSQLiteException("Please call from() at the first");
        }
        String sql = " select " + column + " from " + table + (whereStr.equals("") ? "" : whereStr);
        Cursor cursor = db.rawQuery(sql, null);
        Object data = null;
        try {
            data = getRightColumn(cursor, column, valueType);
        } catch (JSONException e) {

        }
        cursor.close();
        whereStr = "";
        table = "";
        return data;
    }

    /**
     * 查询列的所有值
     *
     * @param column    列名
     * @param valueType 值类型
     * @return 列的所有值
     * @throws BaseSQLiteException
     */
    public List findColumns(String column, Class<?> valueType) throws BaseSQLiteException {
        if (table.equals("")) {
            throw new BaseSQLiteException("Please call from() at the first");
        }
        String sql = " select " + column + " from " + table + (whereStr.equals("") ? "" : whereStr);
        Cursor cursor = db.rawQuery(sql, null);
        List list = new ArrayList();
        while (cursor.moveToNext()) {
            try {
                list.add(getColumnValue(cursor, column, null, valueType.getSimpleName()));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        cursor.close();
        whereStr = "";
        table = "";
        return list;
    }

    /**
     * 查询列的最新记录
     *
     * @param column    列名
     * @param valueType 值类型
     * @return 最新记录
     * @throws BaseSQLiteException
     */
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

    /**
     * 查询列最早的记录
     *
     * @param column    列名
     * @param valueType 值类型
     * @return 最早记录
     * @throws BaseSQLiteException
     */
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

    /**
     * 查询数据库的最早记录
     *
     * @param clazz 表对象的class对象
     * @param <T>   表对象类型
     * @return 最早记录
     * @throws BaseSQLiteException
     */
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
            if (field.isAnnotationPresent(ColumnType.class)) {
                ColumnType fieldType = field.getAnnotation(ColumnType.class);
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

    /**
     * 查询数据库最新的记录
     *
     * @param clazz 表对象的class对象
     * @param <T>   表对象类型
     * @return 最新记录
     * @throws BaseSQLiteException
     */
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
            if (field.isAnnotationPresent(ColumnType.class)) {
                ColumnType fieldType = field.getAnnotation(ColumnType.class);
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

    /**
     * 获取正确的列值
     *
     * @param cursor    光标
     * @param column    列名
     * @param valueType 值类型
     * @return 列的值
     * @throws JSONException
     */
    private Object getRightColumn(Cursor cursor, String column, Class<?> valueType) throws JSONException {
        Object data = null;
        if (cursor.moveToNext()) {
            data = getColumnValue(cursor, column, null, valueType.getSimpleName());
        }

        return data;
    }

    /**
     * 获取列值
     *
     * @param cursor    光标
     * @param column    列名
     * @param fieldType 字段类型
     * @param dbType    数据库字段类型
     * @return 列值
     * @throws JSONException
     */
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

    /**
     * 设置表名
     *
     * @param table 表名
     * @return DatabaseStore
     */
    public DatabaseStore from(String table) {
        this.table = table;
        return this;
    }

    /**
     * 保存
     *
     * @param tableName 表名
     * @param values    ContentValues
     */
    public void save(String tableName, ContentValues values) {
        db.insert(tableName, null, values);
    }

    /**
     * 重置SQL语句
     */
    public void reset() {
        whereStr = "";
        orderStr = "";
        table = "";
    }
}
