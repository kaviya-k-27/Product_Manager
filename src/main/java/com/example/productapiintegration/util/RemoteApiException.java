package com.example.productapiintegration.util;

public class RemoteApiException extends RuntimeException {
    private final int statusCode;
    private final String remoteBody;

    public RemoteApiException(String message, int statusCode, String remoteBody, Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
        this.remoteBody = remoteBody;
    }

    public RemoteApiException(String message, int statusCode, String remoteBody) {
        super(message);
        this.statusCode = statusCode;
        this.remoteBody = remoteBody;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getRemoteBody() {
        return remoteBody;
    }
}

