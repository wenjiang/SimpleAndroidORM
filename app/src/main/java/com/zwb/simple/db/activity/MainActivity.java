package com.zwb.simple.db.activity;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.zwb.simple.db.DatabaseStore;
import com.zwb.simple.db.R;
import com.zwb.simple.db.exception.BaseSQLiteException;
import com.zwb.simple.db.exception.NoSuchTableException;
import com.zwb.simple.db.model.Status;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;


public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DatabaseStore.getInstance().init(this);
        for (int i = 0; i < 10; i++) {
            Status status = new Status();
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
            List<Status> statusEntities = (List<Status>) DatabaseStore.getInstance().from("status").where("age", 20).findAll(Status.class);
            for (Status entity : statusEntities) {
                Log.e("MainActivity", entity.getText().toString());
            }
        } catch (BaseSQLiteException e) {
            Log.e("MainActivity", e.toString());
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
