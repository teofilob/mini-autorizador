package br.com.teofilob.miniautorizador.application.dto;

import java.math.BigDecimal;
import java.util.Objects;

public record TransacaoCommand(String numeroCartao, String senha, BigDecimal valor) {

    public TransacaoCommand {
        Objects.requireNonNull(numeroCartao, "Número do cartão é obrigatório");
        Objects.requireNonNull(senha, "Senha é obrigatória");
        Objects.requireNonNull(valor, "Valor é obrigatório");

        if (numeroCartao.isBlank()) {
            throw new IllegalArgumentException("Número do cartão não pode estar vazio");
        }

        if (senha.isBlank()) {
            throw new IllegalArgumentException("Senha não pode estar vazia");
        }

        if (valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Valor da transação deve ser maior que zero");
        }
    }

    @Override
    public String toString() {
        String numeroCartaoMascarado = numeroCartao.length() > 4
                ? "*".repeat(numeroCartao.length() - 4) + numeroCartao.substring(numeroCartao.length() - 4)
                : "*".repeat(numeroCartao.length());
        return "TransacaoCommand[numeroCartao=" + numeroCartaoMascarado
                + ", senha=[OCULTADA], valor=" + valor + "]";
    }
}
