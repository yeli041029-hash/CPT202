package com.group32.cpt202.CY_project;

public class Result {
    private int code;
    private String msg;
    private Object data;

    public static Result success(Object data) {
        Result result = new Result();
        result.code = 200;
        result.msg = "success";
        result.data = data;
        return result;
    }

    public static Result success(String msg) {
        Result result = new Result();
        result.code = 200;
        result.msg = msg;
        return result;
    }

    public static Result fail(String msg) {
        Result result = new Result();
        result.code = 500;
        result.msg = msg;
        return result;
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

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
