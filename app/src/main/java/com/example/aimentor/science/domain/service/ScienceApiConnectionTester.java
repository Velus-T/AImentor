package com.example.aimentor.science.domain.service;

/** Tests user-entered AI connection settings without coupling the UI to HTTP. */
public interface ScienceApiConnectionTester {
    void test(String apiKey, String modelName, ResultCallback<String> callback);
}
