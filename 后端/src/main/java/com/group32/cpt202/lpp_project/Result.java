package com.group32.cpt202.lpp_project;

public class Result {
    public int code;
    public String msg;
    public Object data;

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
}
