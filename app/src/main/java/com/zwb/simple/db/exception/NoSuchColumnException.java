package com.zwb.simple.db.exception;

/**
 * Created by pc on 2015/3/11.
 */
public class NoSuchColumnException extends Exception {
    public NoSuchColumnException(String message) {
        super(message);
    }
}
