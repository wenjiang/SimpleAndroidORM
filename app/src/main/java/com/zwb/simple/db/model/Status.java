package com.zwb.simple.db.model;

import com.zwb.simple.db.annotation.Column;
import com.zwb.simple.db.annotation.ColumnType;
import com.zwb.simple.db.annotation.Table;

import org.json.JSONObject;

/**
 * Created by pc on 2015/3/4.
 */
@Table(table = "status")
public class Status extends BaseTable {
    @Column
    @ColumnType(ColumnType = "String")
    private JSONObject text;
    @Column
    private int age;

    public JSONObject getText() {
        return text;
    }

    public void setText(JSONObject text) {
        this.text = text;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public int getAge() {
        return age;
    }
}
