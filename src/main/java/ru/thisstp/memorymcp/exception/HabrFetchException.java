package ru.thisstp.memorymcp.exception;

public class HabrFetchException extends RuntimeException {

    public HabrFetchException(String habrId, Throwable cause) {
        super("Failed to fetch habr article " + habrId + ": " + cause.getMessage(), cause);
    }

    public HabrFetchException(String habrId, String reason) {
        super("Failed to fetch habr article " + habrId + ": " + reason);
    }
}
