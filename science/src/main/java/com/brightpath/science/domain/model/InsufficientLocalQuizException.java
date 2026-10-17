package com.brightpath.science.domain.model;

/** A small built-in sample must never be presented as a full newly generated quiz. */
public final class InsufficientLocalQuizException extends RuntimeException {
  public final int available;
  public final int requested;

  public InsufficientLocalQuizException(int available, int requested) {
    super("Not enough built-in questions");
    this.available = available;
    this.requested = requested;
  }
}
