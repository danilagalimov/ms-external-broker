package com.broker.controller.advice;

import com.broker.controller.TradeSubmitController;
import com.broker.controller.TradeViewController;
import com.broker.exception.TradeNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.stream.Collectors;

/**
 *  Trade controllers exception handler. Handles validation and not found exceptions
 */
@ControllerAdvice(assignableTypes = {TradeSubmitController.class, TradeViewController.class})
public class TradeControllerExceptionHandler extends ResponseEntityExceptionHandler {
    private final String tradeNotFoundMessage;

    public TradeControllerExceptionHandler(@Value("${service.message.trade.not.found}") String tradeNotFoundMessage) {
        this.tradeNotFoundMessage = tradeNotFoundMessage;
    }

    @ExceptionHandler({TradeNotFoundException.class})
    protected ResponseEntity<Object> handleNotFound(TradeNotFoundException ex, WebRequest request) {
        return handleExceptionInternal(ex, tradeNotFoundMessage, new HttpHeaders(), HttpStatus.NOT_FOUND, request);
    }

    /**
     * Overridden to provide better validation messages
     */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        String bodyOfResponse = ex.getBindingResult().getFieldErrors().stream()
                .map(TradeControllerExceptionHandler::formatFieldError)
                .collect(Collectors.joining("\n"));

        return handleExceptionInternal(ex, bodyOfResponse, headers, status, request);
    }

    private static String formatFieldError(FieldError e) {
        return e.getField() + " " + e.getDefaultMessage();
    }
}
