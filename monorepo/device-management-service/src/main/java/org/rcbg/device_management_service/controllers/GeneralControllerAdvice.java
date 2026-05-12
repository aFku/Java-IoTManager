package org.rcbg.device_management_service.controllers;

import lombok.extern.slf4j.Slf4j;
import org.rcbg.device_management_service.exceptions.ObjectDoesNotExistException;
import org.rcbg.device_management_service.models.dto.errors.StandardProblemDetail;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GeneralControllerAdvice {

    // TODO: Implement validation errors aggregator
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<StandardProblemDetail> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        StandardProblemDetail problemDetail = new StandardProblemDetail();
        problemDetail.setStatus(HttpStatus.BAD_REQUEST.value());
        problemDetail.setType("validation-error");
        problemDetail.setTitle("Incorrect request body");
        // TODO: Fetch specific messages instead of whole stacktrace
        problemDetail.setDetail(ex.getMessage());
        log.error("Cannot save object due to: {}", problemDetail.getDetail());
        return new ResponseEntity<>(problemDetail, new HttpHeaders(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ObjectDoesNotExistException.class)
    public ResponseEntity<StandardProblemDetail> handleObjectDoesNotExistException(ObjectDoesNotExistException ex) {
        StandardProblemDetail problemDetail = new StandardProblemDetail();
        problemDetail.setStatus(HttpStatus.NOT_FOUND.value());
        problemDetail.setType("not-found");
        problemDetail.setTitle("Object has not been found");
        problemDetail.setDetail(ex.getMessage());
        log.error("Cannot find object: {}, userId: {}", problemDetail.getDetail(), ex.getUserId());
        return new ResponseEntity<>(problemDetail, new HttpHeaders(), HttpStatus.NOT_FOUND);
    }

}
