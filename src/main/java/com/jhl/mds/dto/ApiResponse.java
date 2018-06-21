package com.jhl.mds.dto;

import lombok.Getter;

@Getter
public class ApiResponse<T> {

    private boolean success;
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
        ApiResponse<T> response = new ApiResponse<>();
        response.success = true;
        response.errorMessage = e.getMessage();
        return response;
    }

    public static <T> ApiResponse<T> error(String errorMessage) {
        ApiResponse<T> response = new ApiResponse<>();
        response.success = true;
        response.errorMessage = errorMessage;
        return response;
    }
}
