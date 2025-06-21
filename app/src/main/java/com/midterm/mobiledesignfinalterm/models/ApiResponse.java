package com.midterm.mobiledesignfinalterm.models;

/**
 * Class đại diện cho cấu trúc phản hồi API
 * @param <T> Loại dữ liệu được trả về trong phần "data" của phản hồi
 */
public class ApiResponse<T> {
    private boolean status;
    private String message;
    private T data;

    // Getters và Setters
    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Trả về dữ liệu
     */
    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}