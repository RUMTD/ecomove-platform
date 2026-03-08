package com.ecomove.exception;
public class NoSeatsAvailableException extends RuntimeException {
    public NoSeatsAvailableException(Long tripId) { super("Plus de places disponibles pour le trajet id=" + tripId); }
}
