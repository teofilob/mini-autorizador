package br.com.teofilob.miniautorizador.infrastructure.adapter.inbound.exception;

import org.junit.jupiter.api.Test;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
}
