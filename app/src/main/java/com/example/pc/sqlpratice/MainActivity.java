package com.example.pc.sqlpratice;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.example.pc.db.BaseSQLiteException;
import com.example.pc.db.DatabaseStore;
import com.example.pc.db.MaomaoDbManager;
import com.example.pc.db.NoSuchTableException;
import com.example.pc.model.StatusEntity;
import com.example.pc.model.StatusInterface;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.maomao.client.yong.NetError;
import com.maomao.client.yong.annotations.Done;
import com.maomao.client.yong.annotations.Fail;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;


public class MainActivity extends ActionBarActivity implements MaomaoDbManager.FinishInitDb {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        MaomaoDbManager dbManager = new MaomaoDbManager();
//        dbManager.initDb(this);
        DatabaseStore.getInstance().init(this);
//        createData();
//        Status status = new Status();
//        status.setText("麻痹");
//        status.save();
//        long start = System.currentTimeMillis();
//        List<Status> statuses = DataSupport.findAll(Status.class);
//        long end = System.currentTimeMillis();
//        Log.e("data", (end - start) + "");
//
//        Status status = (Status) DatabaseStore.getInstance().findLast(Status.class);
//        try {
//            DatabaseStore.getInstance().init(this);
//            UserEntity user = new UserEntity();
//            user.setName("郑文彪");
//            user.save();
//
//        try {
//            List<StatusEntity> statusEntities = DatabaseStore.getInstance().from("status").findAll(StatusEntity.class);
//        } catch (BaseSQLiteException e) {
//            e.printStackTrace();
//        }
        //            user.delete();
//            String name1 = (String) DatabaseStore.getInstance().from("user").where("name", "郑文彪").findColumn("name", String.class);
//            Log.e("MainActivity", name1);
//        } catch (Exception e) {
//            DatabaseStore.getInstance().reset();
//            Log.e("MainActivity", e.toString());
//        } catch (Throwable throwable) {
//            DatabaseStore.getInstance().reset();
//            Log.e("MainActivity", throwable.toString());
//        }
//        Yong yong = Yong.get();
//        StatusInterface statusInterface = yong.async(StatusInterface.class, MainActivity.this);
//        statusInterface.allStatuses("null", true, 10, "51dd272e24ac20158506aa00,4f8d30bd24ac297b2b7e8da3");
//        long end = System.currentTimeMillis();
//        Log.e("LitePal", (end - start) + "");

        for (int i = 0; i < 10; i++) {
            StatusEntity status = new StatusEntity();
            try {
                JSONObject json = new JSONObject();
                json.put("name", "xbs");
                status.setText(json);
            } catch (JSONException e) {
                Log.e("DatabaseStore", e.toString());
            }
            status.setAge(20);
            try {
                status.save();
            } catch (NoSuchTableException e) {
                Log.e("MainActivity", e.toString());
            }
        }
        DatabaseStore.getInstance().commit();

        try {
            List<StatusEntity> statusEntities = (List<StatusEntity>) DatabaseStore.getInstance().from("status").where("age", 20).findAll(StatusEntity.class);
            for (StatusEntity entity : statusEntities) {
                Log.e("MainActivity", entity.getText().toString());
            }
        } catch (BaseSQLiteException e) {
            Log.e("MainActivity", e.toString());
        }
    }

    @Done(value = StatusInterface.class)
    public void ApiDone(JSONObject response) {
        try {
            Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation()
                    .serializeNulls().create();
            JSONArray statusArr = response.getJSONArray("statusArray");
            int length = statusArr.length();
            for (int i = 0; i < length; i++) {
                JSONObject statusJson = statusArr.getJSONObject(i);
                StatusEntity status = gson.fromJson(statusJson.getString("status"), new TypeToken<StatusEntity>() {
                }.getType());
                status.save();
            }
        } catch (JSONException e) {
            Log.e("MainActivity", e.toString());
        } catch (NoSuchTableException e) {
            Log.e("MainActivity", e.toString());
        }
        try {
            List<StatusEntity> statusEntities = DatabaseStore.getInstance().from("status").findAll(StatusEntity.class);
            for (StatusEntity entity : statusEntities) {
//                Log.e("s", entity.getAge());
            }
        } catch (BaseSQLiteException e) {
            Log.e("MainActivity", e.toString());
        } catch (Exception e) {
            Log.e("MainActivity", e.toString());
        }
    }

    @Fail(StatusInterface.class)
    public void ApiFail(NetError error) {
        Log.e("haha", error.toString());
    }

    private void createData() {
//        for (int i = 0; i < 1000; i++) {
//            StatusEntity entity = new StatusEntity();
//            entity.setText("你好");
//            try {
//                entity.save();
//            } catch (NoSuchTableException e) {
//                e.printStackTrace();
//            }
//        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void doneCall() {

    }
}
