package com.mrngwozdz.setup.platform.http;

import com.mrngwozdz.setup.platform.result.Failure;
import com.mrngwozdz.setup.platform.result.Success;
import io.vavr.control.Either;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.function.Function;

public final class RestResults {
    private RestResults() {}

    public static <T> ResponseEntity<T> ok(Success<T> s) {
        return ResponseEntity.ok(s.value());
    }

    public static ResponseEntity<Object> toResponse(Failure f) {
        HttpStatus status = switch (f.code()) {
            case VALIDATION -> HttpStatus.BAD_REQUEST;
            case NOT_FOUND  -> HttpStatus.NOT_FOUND;
            case CONFLICT   -> HttpStatus.CONFLICT;
            case TIMEOUT    -> HttpStatus.GATEWAY_TIMEOUT;
            case UNAVAILABLE-> HttpStatus.SERVICE_UNAVAILABLE;
            case IO_ERROR   -> HttpStatus.INTERNAL_SERVER_ERROR;
            default         -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
        var problem = ResponseProblem.of(
                status.value(),
                f.code().name(),
                f.message(),
                f.context()
        );
        return ResponseEntity.status(status).body(problem);
    }

    @SuppressWarnings("unchecked")
    public static <T, R> ResponseEntity<R> toResponseEntity(Either<Failure, T> either, Function<T, R> mapper) {
        return either.fold(
                failure -> (ResponseEntity<R>) toResponse(failure),
                success -> ResponseEntity.ok(mapper.apply(success))
        );
    }

    @SuppressWarnings("unchecked")
    public static <T, R> ResponseEntity<R> toCreatedResponseEntity(
            Either<Failure, T> either,
            Function<T, R> responseMapper,
            Function<T, Object> idExtractor
    ) {
        return either.fold(
                failure -> (ResponseEntity<R>) toResponse(failure),
                success -> {
                    Object id = idExtractor.apply(success);
                    URI location = ServletUriComponentsBuilder
                            .fromCurrentRequest()
                            .path("/{id}")
                            .buildAndExpand(id)
                            .toUri();
                    return ResponseEntity
                            .created(location)
                            .body(responseMapper.apply(success));
                }
        );
    }
}