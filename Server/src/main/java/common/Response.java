package common;

import java.io.Serializable;

public class Response implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum Status {
        OK, ERROR
    }

    private Status status;
    private String message;   // human-readable
    private String errorCode; // optional: e.g. AUTH_FAILED, VALIDATION_ERROR
    private Object data;      // DTO / List<DTO> / null

    private Response(Status status, String message, String errorCode, Object data) {
        this.status = status;
        this.message = message;
        this.errorCode = errorCode;
        this.data = data;
    }

    public static Response ok(Object data) {
        return new Response(Status.OK, null, null, data);
    }

    public static Response okMessage(String message) {
        return new Response(Status.OK, message, null, null);
    }

    public static Response error(String errorCode, String message) {
        return new Response(Status.ERROR, message, errorCode, null);
    }

    public Status getStatus() { return status; }
    public String getMessage() { return message; }
    public String getErrorCode() { return errorCode; }
    public Object getData() { return data; }

    public boolean isOk() { return status == Status.OK; }
}
