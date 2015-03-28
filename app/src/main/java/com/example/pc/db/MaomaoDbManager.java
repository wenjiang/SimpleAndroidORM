package com.example.pc.db;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;

import com.maomao.client.db.DataManager;
import com.maomao.client.yomo.TaskSet;
import com.maomao.client.yomo.Yomo;

import org.litepal.LitePalApplication;
import org.litepal.tablemanager.Connector;
import org.litepal.util.SharedUtil;

/**
 * Maomao整个应用的数据库管理类
 *
 * @author Jinsen
 * @since 2014-10-29
 */
public class MaomaoDbManager extends DataManager {

    public interface FinishInitDb {
        void doneCall();
    }

    private static final String LOG_TAG = "MaomaoDbManager";
    private static final String DBMANAGER_PREF = "dbmanager_pref";
    private static final String IS_INDEXED = "is_indexed";

    public MaomaoDbManager() {
        super();
    }

    public void initDb(final FinishInitDb callback) {
        TaskSet task = new TaskSet() {
            @Override
            public Object background() throws Exception {
                SQLiteDatabase db = Connector.getDatabase();
                int oldVersion = SharedUtil.getLastVersion();
                int newVersion = mAttr.getVersion();

                if (newVersion - oldVersion > 1) {
                    if (oldVersion != 0) {
                        // TODO: 升级数据库
                    }
                }
                if (!isTableIndexed()) {
                    /* 创建索引 */
                    commitDbIndex();
                }
                return super.background();
            }
        };
        task.done(new TaskSet.UiDoneCallBack() {
            @Override
            public void done(Object o) {
                callback.doneCall();
            }
        });

        Yomo.add(task, this);
    }

    private void commitDbIndex() {
        callStaticMethodOnTables("commitIndexed");
        setTableIndexed(true);
    }

    private boolean isTableIndexed() {
        SharedPreferences pref = LitePalApplication.getContext().getSharedPreferences(
                DBMANAGER_PREF, Context.MODE_PRIVATE);
        return pref.getBoolean(IS_INDEXED, false);
    }

    private void setTableIndexed(boolean ok) {
        SharedPreferences.Editor editor = LitePalApplication.getContext().getSharedPreferences(
                DBMANAGER_PREF, Context.MODE_PRIVATE).edit();
        editor.putBoolean(IS_INDEXED, ok);
        editor.commit();
    }

}
