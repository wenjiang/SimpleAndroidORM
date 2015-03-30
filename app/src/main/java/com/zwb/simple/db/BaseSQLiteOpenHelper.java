package com.zwb.simple.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.zwb.simple.db.exception.BaseSQLiteException;
import com.zwb.simple.db.exception.NoSuchTableException;
import com.zwb.simple.db.model.BaseTable;
import com.zwb.simple.db.annotation.Column;
import com.zwb.simple.db.annotation.Table;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by pc on 2015/3/10.
 */
public class BaseSQLiteOpenHelper extends SQLiteOpenHelper {
    private static Set<String> tableSet;
    private static String dbName;
    private static int version;

    public static BaseSQLiteOpenHelper getInstance(Context context) {
        try {
            readXml(context);
        } catch (BaseSQLiteException e) {
            Log.e("DatabaseStore", e.toString());
        }
        return new BaseSQLiteOpenHelper(context, dbName, version);
    }

    private BaseSQLiteOpenHelper(Context context, String name, int version) {
        super(context, name, null, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createTable(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        List<String> tableModelList = SharedPreferencesManager.getInstance().getListString("tables");
        List<String> tableList = new ArrayList<String>();
        for (String tableEntity : tableModelList) {
            try {
                BaseTable entity = (BaseTable) (Class.forName(tableEntity).newInstance());
                String tableName = getTableName(entity);
                tableList.add(tableName);
            } catch (InstantiationException e) {
                Log.e("BaseSQLiteOpenHelper", e.toString());
            } catch (IllegalAccessException e) {
                Log.e("BaseSQLiteOpenHelper", e.toString());
            } catch (ClassNotFoundException e) {
                Log.e("BaseSQLiteOpenHelper", e.toString());
            } catch (NoSuchTableException e) {
                Log.e("BaseSQLiteOpenHelper", e.toString());
            }
        }
        if (oldVersion < newVersion) {
            db.beginTransaction();
            for (String table : tableList) {
                String selectSql = "select * from " + table;
                Cursor cursor = db.rawQuery(selectSql, null);
                List<String> oldColumns = new ArrayList<String>();
                for (String column : cursor.getColumnNames()) {
                    oldColumns.add(column);
                }
                String alterSql = "alter table " + table + " rename to " + table + "_temp";
                db.execSQL(alterSql);
                List<String> newColumns = createTable(db);
                StringBuilder upgradeSqlBuilder = new StringBuilder("insert into " + table + " select id, ");
                int i = 0;
                for (String column : newColumns) {
                    if (oldColumns.contains(column)) {
                        upgradeSqlBuilder.append(column + ", ");
                        i++;
                    }
                }

                if (i != 0 && i < newColumns.size()) {
                    for (int j = 0, length = newColumns.size() - i; j < length; j++) {
                        upgradeSqlBuilder.append("'', ");
                    }
                }
                String upgradeStr = upgradeSqlBuilder.toString();
                String upgradeSql = upgradeStr.substring(0, upgradeStr.length() - 2) + " from " + table + "_temp";
                db.execSQL(upgradeSql);
                String deleteSql = "drop table if exists " + table + "_temp";
                db.execSQL(deleteSql);
            }
            db.setTransactionSuccessful();
            db.endTransaction();
        }
    }

    private List<String> createTable(SQLiteDatabase db) {
        List<String> columnList = new ArrayList<String>();
        List<String> tableList = new ArrayList<String>();
        for (String table : tableSet) {
            try {
                StringBuilder sql = new StringBuilder("create table if not exists ");
                BaseTable entity = (BaseTable) (Class.forName(table).newInstance());
                String tableName = getTableName(entity);
                tableList.add(tableName);
                sql.append(tableName);
                sql.append(" (id integer primary key autoincrement, ");
                columnList = getColumns(entity);
                for (int i = 0, length = columnList.size(); i < length; i++) {
                    sql.append(columnList.get(i) + " ");
                    if (i == length - 1) {
                        break;
                    }
                    sql.append(", ");
                }
                sql.append(");");
                db.execSQL(sql.toString());
            } catch (InstantiationException e) {
                Log.e("DatabaseStore", e.toString());
            } catch (IllegalAccessException e) {
                Log.e("DatabaseStore", e.toString());
            } catch (ClassNotFoundException e) {
                Log.e("DatabaseStore", e.toString());
            } catch (NoSuchTableException e) {
                Log.e("DatabaseStore", e.toString());
            }
        }

        SharedPreferencesManager.getInstance().putListString("tables", tableList);
        return columnList;
    }

    private String getTableName(BaseTable entity) throws NoSuchTableException {
        String tableName = "";
        if (entity.getClass().isAnnotationPresent(Table.class)) {
            Table table = entity.getClass().getAnnotation(Table.class);
            tableName = table.table();

            if (tableName.length() == 0) {
                throw new NoSuchTableException("The table + " + entity.getClass().getSimpleName() + " is not exist");
            }
        }

        return tableName;
    }

    private List<String> getColumns(BaseTable entity) {
        Set<String> columnSet = new HashSet<String>();
        java.lang.reflect.Field[] fields = entity.getClass().getDeclaredFields();
        for (java.lang.reflect.Field field : fields) {
            if (field.isAnnotationPresent(Column.class)) {
                Column meta = field.getAnnotation(Column.class);
                String column = meta.column();
                if (column.equals("")) {
                    column = field.getName();
                }

                columnSet.add(column);
            }
        }

        List<String> columnList = new ArrayList<String>();
        for (String column : columnSet) {
            columnList.add(column);
        }
        return columnList;
    }

    private static void readXml(Context context) throws BaseSQLiteException {
        SharedPreferencesManager.init(context);
        tableSet = new HashSet<String>();
        InputStream in = null;
        try {
            in = context.getResources()
                    .getAssets().open("database.xml");
        } catch (IOException e) {
            throw new BaseSQLiteException("database.xml is not exist");
        }
        XmlPullParserFactory factory;
        try {
            factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser xpp = factory.newPullParser();
            xpp.setInput(in, "UTF-8");
            int evtType = xpp.getEventType();
            // 一直循环，直到文档结束
            while (evtType != XmlPullParser.END_DOCUMENT) {
                switch (evtType) {
                    case XmlPullParser.START_TAG:
                        String tag = xpp.getName();
                        if (tag.equals("dbname")) {
                            dbName = xpp.getAttributeValue(0);
                        } else if (tag.equals("version")) {
                            version = Integer.valueOf(xpp.getAttributeValue(0));
                        } else if (tag.equals("mapping")) {
                            tableSet.add(xpp.getAttributeValue(0));
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        break;
                    default:
                        break;
                }
                //获得下一个节点的信息
                evtType = xpp.next();
            }
        } catch (Exception e) {
            Log.e("DatabaseStore", e.toString());
        } finally {
            List<String> tableList = new ArrayList<String>();
            for (String table : tableSet) {
                tableList.add(table);
            }
        }
    }
}
