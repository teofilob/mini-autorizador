package br.com.teofilob.miniautorizador.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

public record Transacao(String numeroCartao, BigDecimal valor, LocalDateTime dataHora) {

    public Transacao {
        Objects.requireNonNull(numeroCartao, "Número do cartão é obrigatório");
        Objects.requireNonNull(valor, "Valor da transação é obrigatório");
        Objects.requireNonNull(dataHora, "Data e hora da transação são obrigatórias");

        if (numeroCartao.isBlank()) {
            throw new IllegalArgumentException("Número do cartão não pode estar vazio");
        }
        if (valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Valor da transação deve ser maior que zero");
        }
    }
}
