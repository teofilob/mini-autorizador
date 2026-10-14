package br.com.teofilob.miniautorizador.domain.exception;

public class CartaoInexistenteException extends BusinessException {

    public CartaoInexistenteException(String message) {
        super(message);
    }
}
