package com.example.aimentor.science.domain.service;

public interface ResultCallback<T> {
    void onSuccess(T result);
    void onError(String message, Throwable error);
}
