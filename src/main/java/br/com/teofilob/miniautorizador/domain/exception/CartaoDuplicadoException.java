package br.com.teofilob.miniautorizador.domain.exception;

public class CartaoDuplicadoException extends BusinessException {

    private final String numeroCartao;
    private final String senha;

    public CartaoDuplicadoException(String numeroCartao, String senha) {
        this(numeroCartao, senha, null);
    }

    public CartaoDuplicadoException(String numeroCartao, String senha, Throwable cause) {
        super("Cartão já cadastrado.", cause);
        this.numeroCartao = numeroCartao;
        this.senha = senha;
    }

    public String getNumeroCartao() {
        return numeroCartao;
    }

    public String getSenha() {
        return senha;
    }
}
