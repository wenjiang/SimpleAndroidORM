package com.example.pc.db;

import android.database.sqlite.SQLiteDatabase;

import org.litepal.parser.LitePalAttr;
import org.litepal.tablemanager.Connector;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by pc on 2015/2/27.
 */
public class DataManager {

    private static final String DEFAULT_TABLE_NAME = "org.litepal.model.Table_Schema";

    protected LitePalAttr mAttr;

    protected List<String> mTableClassNames = null;

    public DataManager() {
        prepare();
        mAttr = LitePalAttr.getInstance();
    }

    /**
     * 集体执行对表的操作
     *
     * @param method 表实体要执行的方法名
     */
    public void callStaticMethodOnTables(String method) {
        prepareTable();
        for (String table : mTableClassNames) {
            try {
                Class<?> entity = Class.forName(table);
                Method todo = entity.getMethod(method, SQLiteDatabase.class);
                todo.invoke(null, Connector.getDatabase());
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    private static final void prepare() {
        Connector.getDatabase();
    }

    protected final void prepareTable() {
        if (null == mTableClassNames) {
            mTableClassNames = new ArrayList<String>(mAttr.getClassNames());
            mTableClassNames.remove(DEFAULT_TABLE_NAME);
        }
    }

}
