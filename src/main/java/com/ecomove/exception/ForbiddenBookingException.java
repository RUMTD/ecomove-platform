package com.ecomove.exception;
public class ForbiddenBookingException extends RuntimeException {
    public ForbiddenBookingException() { super("Un conducteur ne peut pas reserver son propre trajet"); }
}
