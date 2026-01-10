package com.cloud_kitchen.application.DTO;

public class ApiResponse<R> {
    private Boolean success;
    private String message;
    private R data;

    public ApiResponse(Boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public ApiResponse(Boolean success, String message, R data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    public Boolean getSuccess() { return success; }
    public void setSuccess(Boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public R getData() { return data; }
    public void setData(R data) { this.data = data; }
}