package org.example.projectsagant.dispatch;

public class DispatchException extends RuntimeException {
    public DispatchException(String message, Throwable cause) {
        super(message, cause);
    }
}