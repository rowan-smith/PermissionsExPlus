package dev.rono.permissions.core.command;

/**
 * A command that cannot run as asked, such as one naming an unknown user. Its
 * message is shown to the sender instead of an internal error.
 */
final class CommandFailure extends RuntimeException {
    private static final long serialVersionUID = 1L;

    CommandFailure(String message) {
        super(message, null, false, false);
    }
}
