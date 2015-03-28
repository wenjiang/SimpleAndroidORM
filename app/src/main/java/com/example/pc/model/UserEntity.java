package com.example.pc.model;

import com.example.pc.sqlpratice.Field;
import com.example.pc.sqlpratice.Table;

/**
 * Created by pc on 2015/3/11.
 */
@Table(table = "user")
public class UserEntity extends BaseEntity {
    @Field
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
