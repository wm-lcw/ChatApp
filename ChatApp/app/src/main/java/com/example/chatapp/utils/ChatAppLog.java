package com.example.chatapp.utils;


/**
 * @ClassName: ChatAppLog
 * @Description: 打印输出类
 * @Author: wm
 * @CreateDate: 2023/2/20
 * @UpdateUser: updater
 * @UpdateDate: 2023/2/20
 * @UpdateRemark: 更新内容
 * @Version: 1.0
 */
public class ChatAppLog {
    private static final boolean LOCAL_DBG_SWITCH = true;
    private static final String TAG = "ChatAppLog";

    public static void info(String log) {
        if (LOCAL_DBG_SWITCH) {
            StackTraceElement[] stacks = new Throwable().getStackTrace();
            StackTraceElement currentStack = stacks[1];

            String strMsg = currentStack.getFileName() + "(" + currentStack.getLineNumber() + ")::"
                    + currentStack.getMethodName() + " - " + log;
            android.util.Log.i(TAG, strMsg);
        }
    }

    public static void debug(String log) {
        if (LOCAL_DBG_SWITCH) {
            StackTraceElement[] stacks = new Throwable().getStackTrace();
            StackTraceElement currentStack = stacks[1];

            String strMsg = currentStack.getFileName() + "(" + currentStack.getLineNumber() + ")::"
                    + currentStack.getMethodName() + " - " + log;
            android.util.Log.d(TAG, strMsg);
        }
    }

    public static void debug() {
        if (LOCAL_DBG_SWITCH) {
            StackTraceElement[] stacks = new Throwable().getStackTrace();
            StackTraceElement currentStack = stacks[1];

            String strMsg = currentStack.getFileName() + "(" + currentStack.getLineNumber() + ")::"
                    + currentStack.getMethodName();
            android.util.Log.d(TAG, strMsg);
        }
    }

    public static void error(String log) {
        if (LOCAL_DBG_SWITCH) {
            StackTraceElement[] stacks = new Throwable().getStackTrace();
            StackTraceElement currentStack = stacks[1];

            String strMsg = currentStack.getFileName() + "(" + currentStack.getLineNumber() + ")::"
                    + currentStack.getMethodName() + " - " + log;
            android.util.Log.e(TAG, strMsg);
        }
    }
}
