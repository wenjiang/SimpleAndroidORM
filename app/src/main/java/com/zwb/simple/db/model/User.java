package com.zwb.simple.db.model;

import com.zwb.simple.db.annotation.Column;
import com.zwb.simple.db.annotation.Table;

/**
 * Created by pc on 2015/3/11.
 */
@Table(table = "user")
public class User extends BaseTable {
    @Column
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
