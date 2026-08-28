package org.chud.springuniapi.exception;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String resource, Long id) {
        super("%s with id %d not found".formatted(resource, id));
    }

    public ResourceNotFoundException(String resource, String name) {
        super("%s with name %s not found".formatted(resource, name));
    }
}