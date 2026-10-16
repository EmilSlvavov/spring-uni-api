package org.chud.springuniapi.exception;

import java.util.Set;

public class InvalidSortException extends RuntimeException {
    public InvalidSortException(String property, Set<String> allowed) {
        super("cannot sort by " + property + "; allowed: " + String.join(", ", allowed));
    }
}
