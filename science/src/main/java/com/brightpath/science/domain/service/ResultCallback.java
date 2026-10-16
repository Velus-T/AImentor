package com.brightpath.science.domain.service;

public interface ResultCallback<T> {
        /*
 * Responsibility: Performs the `onSuccess` operation for ResultCallback within the Science Module.
 * Purpose: Encapsulates this responsibility inside the Science Module component so the code is easier to read, test, maintain, and locate during the review.
 */
void onSuccess(T result);
        /*
 * Responsibility: Performs the `onError` operation for ResultCallback within the Science Module.
 * Purpose: Encapsulates this responsibility inside the Science Module component so the code is easier to read, test, maintain, and locate during the review.
 */
void onError(String message, Throwable error);
}
