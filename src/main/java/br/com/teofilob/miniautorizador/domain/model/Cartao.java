package br.com.teofilob.miniautorizador.domain.model;

import br.com.teofilob.miniautorizador.domain.exception.SaldoInsuficienteException;
import br.com.teofilob.miniautorizador.domain.exception.SenhaInvalidaException;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;

public record Cartao(String numeroCartao, String senha, BigDecimal saldo) {

    public Cartao {
        Objects.requireNonNull(numeroCartao, "Número do cartão é obrigatório");
        Objects.requireNonNull(senha, "Senha é obrigatória");
        Objects.requireNonNull(saldo, "Saldo é obrigatório");

        if (numeroCartao.isBlank()) {
            throw new IllegalArgumentException("Número do cartão não pode estar vazio");
        }

        if (senha.isBlank()) {
            throw new IllegalArgumentException("Senha não pode estar vazia");
        }

        if (saldo.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Saldo não pode ser negativo");
        }
    }

    public boolean senhaValida(String senhaInformada) {
        return senha.equals(senhaInformada);
    }

    public boolean possuiSaldoPara(BigDecimal valor) {
        return saldo.compareTo(valor) >= 0;
    }

    public void validarSenha(String senhaInformada) {
        Optional.ofNullable(senhaInformada)
                .filter(this::senhaValida)
                .orElseThrow(() -> new SenhaInvalidaException("Senha inválida."));
    }

    public Cartao debitar(BigDecimal valor) {
        return Optional.ofNullable(valor)
                .filter(this::possuiSaldoPara)
                .map(v -> new Cartao(this.numeroCartao, this.senha, this.saldo.subtract(v)))
                .orElseThrow(() -> new SaldoInsuficienteException("Saldo insuficiente."));
    }
}
