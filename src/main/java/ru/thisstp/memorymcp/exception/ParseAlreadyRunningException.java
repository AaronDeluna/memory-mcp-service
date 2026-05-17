package ru.thisstp.memorymcp.exception;

public class ParseAlreadyRunningException extends RuntimeException {

    public ParseAlreadyRunningException() {
        super("parseAll is already running");
    }
}
