package com.jhl.mds.dto;

import lombok.Getter;

@Getter
public class ApiResponse<T> {

    private boolean success;
    private int errorCode;
    private String errorMessage;
    private T data;

    private ApiResponse() {

    }

    public static <T> ApiResponse<T> success(T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.success = true;
        response.data = data;
        return response;
    }

    public static <T> ApiResponse<T> error(Exception e) {
        return error(e, 0);
    }

    public static <T> ApiResponse<T> error(Exception e, int errorCode) {
        ApiResponse<T> response = new ApiResponse<>();
        response.success = false;
        response.errorMessage = e.getMessage();
        return response;
    }

    public static <T> ApiResponse<T> error(String errorMessage) {
        return error(errorMessage, 0);
    }

    public static <T> ApiResponse<T> error(String errorMessage, int errorCode) {
        ApiResponse<T> response = new ApiResponse<>();
        response.success = false;
        response.errorMessage = errorMessage;
        return response;
    }
}
