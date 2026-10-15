package br.com.teofilob.miniautorizador.infrastructure.adapter.inbound.exception;

import org.junit.jupiter.api.Test;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ApiExceptionHandlerTest {

    private final ApiExceptionHandler exceptionHandler = new ApiExceptionHandler();

    @Test
    void deveRetornar503QuandoNaoForPossivelAdquirirLockPessimista() {
        var response = exceptionHandler.handlePessimisticLockingFailure(
                new CannotAcquireLockException("Lock timeout"));

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertEquals(MediaType.TEXT_PLAIN, response.getHeaders().getContentType());
        assertEquals("TRANSACAO_TEMPORARIAMENTE_INDISPONIVEL", response.getBody());
    }

    @Test
    void deveRetornar400ComMensagemDoPrimeiroErroDeValidacao() {
        var bindingResult = mock(BindingResult.class);
        var exception = mock(MethodArgumentNotValidException.class);
        var fieldError = new FieldError(
                "transacaoRequest",
                "valor",
                null,
                false,
                null,
                null,
                "Valor da transação deve ser maior que zero");
        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        var response = exceptionHandler.handleMethodArgumentNotValid(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("VALIDATION_ERROR", response.getBody().status());
        assertEquals("Valor da transação deve ser maior que zero", response.getBody().mensagem());
    }

    @Test
    void deveRetornarMensagemPadraoQuandoNaoHouverErrosDeCampo() {
        var bindingResult = mock(BindingResult.class);
        var exception = mock(MethodArgumentNotValidException.class);
        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of());

        var response = exceptionHandler.handleMethodArgumentNotValid(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("VALIDATION_ERROR", response.getBody().status());
        assertEquals("Dados inválidos.", response.getBody().mensagem());
    }
}
