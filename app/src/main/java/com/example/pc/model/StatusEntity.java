package com.example.pc.model;

import com.example.pc.sqlpratice.Field;
import com.example.pc.sqlpratice.FieldType;
import com.example.pc.sqlpratice.Table;

import org.json.JSONObject;

/**
 * Created by pc on 2015/3/4.
 */
@Table(table = "status")
public class StatusEntity extends BaseEntity {
    @Field
    @FieldType(ColumnType = "String")
    private JSONObject text;
    @Field
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
