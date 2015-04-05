package com.zwb.simple.db.activity;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.zwb.simple.db.DatabaseStore;
import com.zwb.simple.db.R;
import com.zwb.simple.db.exception.BaseSQLiteException;
import com.zwb.simple.db.model.Status;
import com.zwb.simple.db.utils.LogUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DatabaseStore.getInstance().init(this);
        List<Status> statuses = new ArrayList<Status>();
        for (int i = 0; i < 10; i++) {
            Status status = new Status();
            try {
                JSONObject json = new JSONObject();
                json.put("name", "xbs");
                status.setText(json);
            } catch (JSONException e) {
                LogUtil.e(e.toString());
            }
            status.setAge(20);
            statuses.add(status);
        }

        try {
            DatabaseStore.getInstance().saveAll(statuses);
//            status.save();
        } catch (Exception e) {
            LogUtil.e(e.toString());
        }
        try {
            List<Status> statusEntities = (List<Status>) DatabaseStore.getInstance().from("status").findAll(Status.class);
            for (Status entity : statusEntities) {
                LogUtil.e(entity.getText().toString() + ":" + entity.getAge());
            }
        } catch (BaseSQLiteException e) {
            LogUtil.e(e.toString());
        }
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
}
