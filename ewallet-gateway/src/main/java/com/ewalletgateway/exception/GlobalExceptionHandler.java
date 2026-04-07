package com.ewalletgateway.exception;

import com.ewalletgateway.api.dto.ErrorResponse;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(StatusRuntimeException.class)
    public ResponseEntity<ErrorResponse> handleGrpc(StatusRuntimeException e) {
        Status.Code code = e.getStatus().getCode();
        HttpStatus http = switch (code) {
            case UNAUTHENTICATED -> HttpStatus.UNAUTHORIZED;
            case PERMISSION_DENIED -> HttpStatus.FORBIDDEN;
            case NOT_FOUND -> HttpStatus.NOT_FOUND;
            case INVALID_ARGUMENT -> HttpStatus.BAD_REQUEST;
            case ALREADY_EXISTS -> HttpStatus.CONFLICT;
            case FAILED_PRECONDITION -> HttpStatus.CONFLICT;
            case DEADLINE_EXCEEDED -> HttpStatus.GATEWAY_TIMEOUT;
            case UNAVAILABLE -> HttpStatus.BAD_GATEWAY;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
        String msg = e.getStatus().getDescription();
        if (msg == null || msg.isEmpty()) {
            msg = code.name();
        }
        String grpcCode = code.name();
        return ResponseEntity.status(http).body(new ErrorResponse(grpcCode, msg));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException e) {
        String msg = e.getMessage() != null ? e.getMessage() : "Bad request";
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse("BAD_REQUEST", msg));
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatus(ResponseStatusException e) {
        HttpStatus status = HttpStatus.valueOf(e.getStatusCode().value());
        String msg = e.getReason() != null ? e.getReason() : status.getReasonPhrase();
        return ResponseEntity.status(status).body(new ErrorResponse(status.name(), msg));
    }
}
