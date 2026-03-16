package com.example.mentalhealth.common;

public class Result<T> {

    private int code;
    private String msg;
    private T data;
    private String traceId;
    private long timestamp;

    public Result() {
    }

    public Result(int code, String msg, T data, String traceId) {
        this.code = code;
        this.msg = msg;
        this.data = data;
        this.traceId = traceId;
        this.timestamp = System.currentTimeMillis();
    }

    public static <T> Result<T> success(T data, String traceId) {
        return new Result<>(0, "success", data, traceId);
    }

    public static <T> Result<T> fail(int code, String msg, String traceId) {
        return new Result<>(code, msg, null, traceId);
    }

    public static <T> Result<T> fail(ErrorCode errorCode, String msg, String traceId) {
        return new Result<>(errorCode.getCode(), msg, null, traceId);
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
