package com.example.usermanagement.controller;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 统一API响应格式
 */
@Schema(description = "统一API响应格式")
public class Result<T> {
    
    @Schema(description = "状态码", example = "200")
    private int code;
    
    @Schema(description = "响应消息", example = "操作成功")
    private String message;
    
    @Schema(description = "响应数据")
    private T data;
    
    public Result(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }
    
    public static <T> Result<T> success(T data, String message) {
        return new Result<>(200, message, data);
    }
    
    public static <T> Result<T> error(int code, String message) {
        return new Result<>(code, message, null);
    }
    
    public int getCode() { return code; }
    public void setCode(int code) { this.code = code; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public T getData() { return data; }
    public void setData(T data) { this.data = data; }
}
