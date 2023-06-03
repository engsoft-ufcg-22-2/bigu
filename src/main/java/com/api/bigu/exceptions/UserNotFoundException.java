package com.api.bigu.exceptions;

public class UserNotFoundException extends Throwable {
    public UserNotFoundException(String message) {
        super(message);
    }

    public String getMessage() {
        return "Usuário não encontrado.";
    }
}
