package br.com.teofilob.miniautorizador.infrastructure.adapter.inbound.exception;

import br.com.teofilob.miniautorizador.domain.enums.StatusAutorizacao;
import br.com.teofilob.miniautorizador.domain.exception.CartaoDuplicadoException;
import br.com.teofilob.miniautorizador.domain.exception.CartaoInexistenteException;
import br.com.teofilob.miniautorizador.domain.exception.SaldoInsuficienteException;
import br.com.teofilob.miniautorizador.domain.exception.SenhaInvalidaException;
import br.com.teofilob.miniautorizador.infrastructure.adapter.inbound.dto.CartaoResponse;
import br.com.teofilob.miniautorizador.infrastructure.adapter.inbound.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(CartaoDuplicadoException.class)
    public ResponseEntity<CartaoResponse> handleCartaoDuplicado(CartaoDuplicadoException ex) {
        return ResponseEntity.unprocessableEntity()
                .body(new CartaoResponse(ex.getSenha(), ex.getNumeroCartao()));
    }

    @ExceptionHandler(CartaoInexistenteException.class)
    public ResponseEntity<String> handleCartaoInexistente(CartaoInexistenteException ex) {
        return unprocessableEntity(StatusAutorizacao.CARTAO_INEXISTENTE.name());
    }

    @ExceptionHandler(SenhaInvalidaException.class)
    public ResponseEntity<String> handleSenhaInvalida(SenhaInvalidaException ex) {
        return unprocessableEntity(StatusAutorizacao.SENHA_INVALIDA.name());
    }

    @ExceptionHandler(SaldoInsuficienteException.class)
    public ResponseEntity<String> handleSaldoInsuficiente(SaldoInsuficienteException ex) {
        return unprocessableEntity(StatusAutorizacao.SALDO_INSUFICIENTE.name());
    }

    private ResponseEntity<String> unprocessableEntity(String message) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .contentType(MediaType.TEXT_PLAIN)
                .body(message);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(FieldError::getDefaultMessage)
                .orElse("Dados inválidos.");

        return ResponseEntity.badRequest()
                .body(new ErrorResponse("VALIDATION_ERROR", message));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        return ResponseEntity.badRequest()
                .body(new ErrorResponse("INVALID_REQUEST", "Requisição inválida."));
    }
}
