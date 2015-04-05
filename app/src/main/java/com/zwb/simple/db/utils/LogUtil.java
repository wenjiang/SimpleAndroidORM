package com.zwb.simple.db.utils;

import android.util.Log;

/**
 * Log的工具类
 * Created by wenbiao_zheng on 2014/10/10.
 *
 * @author wenbiao_zheng
 */
public class LogUtil {
    private static String className;
    private static String methodName;
    private static int lineNumber;
    private static boolean isLogEnable = true;

    //禁止声明实例，因为该类只是工具类
    private LogUtil() {
    }

    /**
     * 打开Log信息（默认打开）
     */
    public static void enableLog() {
        isLogEnable = true;
    }

    /**
     * 关闭Log信息
     */
    public static void disableLog() {
        isLogEnable = false;
    }

    /**
     * 拼接Log的信息
     *
     * @param log Log信息
     * @return 拼接好的Log字符串
     */
    private static String createLog(String log) {
        StringBuffer buffer = new StringBuffer();
        buffer.append("[");
        buffer.append(methodName);
        buffer.append(":");
        buffer.append(lineNumber);
        buffer.append("]");
        buffer.append(log);

        return buffer.toString();
    }

    /**
     * 获取方法名
     *
     * @param sElements 栈信息
     */
    private static void getMethodNames(StackTraceElement[] sElements) {
        className = sElements[1].getFileName();
        methodName = sElements[1].getMethodName();
        lineNumber = sElements[1].getLineNumber();
    }

    /**
     * 显示error信息
     *
     * @param message error信息
     */
    public static void e(String message) {
        if (isLogEnable) {
            getMethodNames(new Throwable().getStackTrace());
            Log.e(className, createLog(message));
        }
    }

    /**
     * 显示error信息
     *
     * @param tag     标签
     * @param message error信息
     */
    public static void e(String tag, String message) {
        if (isLogEnable) {
            getMethodNames(new Throwable().getStackTrace());
            Log.e(tag, createLog(message));
        }
    }

    /**
     * 显示info信息
     *
     * @param message info信息
     */
    public static void i(String message) {
        if (isLogEnable) {
            getMethodNames(new Throwable().getStackTrace());
            Log.i(className, createLog(message));
        }
    }

    /**
     * 显示info信息
     *
     * @param tag     标签
     * @param message info信息
     */
    public static void i(String tag, String message) {
        if (isLogEnable) {
            getMethodNames(new Throwable().getStackTrace());
            Log.i(tag, createLog(message));
        }
    }

    /**
     * 显示debug信息
     *
     * @param message debug信息
     */
    public static void d(String message) {
        if (isLogEnable) {
            getMethodNames(new Throwable().getStackTrace());
            Log.d(className, createLog(message));
        }
    }

    /**
     * 显示debug信息
     *
     * @param tag     标签
     * @param message debug信息
     */
    public static void d(String tag, String message) {
        if (isLogEnable) {
            getMethodNames(new Throwable().getStackTrace());
            Log.d(tag, createLog(message));
        }
    }

    /**
     * 显示verbose信息
     *
     * @param message verbose信息
     */
    public static void v(String message) {
        if (isLogEnable) {
            getMethodNames(new Throwable().getStackTrace());
            Log.v(className, createLog(message));
        }
    }

    /**
     * 显示verbose信息
     *
     * @param tag     标签
     * @param message verbose信息
     */
    public static void v(String tag, String message) {
        if (isLogEnable) {
            getMethodNames(new Throwable().getStackTrace());
            Log.v(tag, createLog(message));
        }
    }

    /**
     * 显示warn信息
     *
     * @param message warn信息
     */
    public static void w(String message) {
        if (isLogEnable) {
            getMethodNames(new Throwable().getStackTrace());
            Log.w(className, createLog(message));
        }
    }

    /**
     * 显示warn信息
     *
     * @param tag     标签
     * @param message warn信息
     */
    public static void w(String tag, String message) {
        if (isLogEnable) {
            getMethodNames(new Throwable().getStackTrace());
            Log.w(tag, createLog(message));
        }
    }

    /**
     * 显示wtf信息
     *
     * @param message wft信息
     */
    public static void wtf(String message) {
        if (isLogEnable) {
            getMethodNames(new Throwable().getStackTrace());
            Log.wtf(className, createLog(message));
        }
    }

    /**
     * 显示wtf信息
     *
     * @param tag     标签
     * @param message wft信息
     */
    public static void wft(String tag, String message) {
        if (isLogEnable) {
            getMethodNames(new Throwable().getStackTrace());
            Log.wtf(tag, createLog(message));
        }
    }
}
