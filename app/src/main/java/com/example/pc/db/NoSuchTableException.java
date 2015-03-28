package com.example.pc.db;

/**
 * Created by pc on 2015/3/11.
 */
public class NoSuchTableException extends Exception {
    public NoSuchTableException(String message) {
        super(message);
    }
}
