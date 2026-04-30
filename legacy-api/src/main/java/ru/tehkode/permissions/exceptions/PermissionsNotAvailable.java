package ru.tehkode.permissions.exceptions;

public class PermissionsNotAvailable extends RuntimeException {
    public PermissionsNotAvailable() {
    }

    public PermissionsNotAvailable(String message) {
        super(message);
    }
}
