package io.smallrye.reactive.streams.api;

import java.util.ArrayList;
import java.util.List;

public class CompositeException extends Exception {

    private final List<Throwable> causes;

    public CompositeException(List<Throwable> causes) {
        super("Multiple exception caught:");
        this.causes = new ArrayList<>(causes);
    }

    @Override
    public String getMessage() {
        String message = super.getMessage();
        for (int i = 0; i < causes.size(); i++) {
            Throwable cause = causes.get(i);
            message = message + "\n\t[Exception " + i + "] " + cause;
        }
        return message;
    }

    public List<Throwable> getCauses() {
        return causes;
    }
}
