package com.ecomove.exception;
public class TripNotFoundException extends RuntimeException {
    public TripNotFoundException(Long tripId) { super("Trajet introuvable : id=" + tripId); }
}
