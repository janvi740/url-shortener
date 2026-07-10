package com.janvi.urlshortener.common.exception;

public class InvalidAliasException extends RuntimeException {

    public InvalidAliasException(String message) {
        super(message);
    }
}