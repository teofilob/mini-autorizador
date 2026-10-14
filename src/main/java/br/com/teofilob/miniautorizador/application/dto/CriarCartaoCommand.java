package br.com.teofilob.miniautorizador.application.dto;

import java.util.Objects;

public record CriarCartaoCommand (String numeroCartao, String senha){

    public CriarCartaoCommand {
        Objects.requireNonNull(numeroCartao, "Número do cartão é obrigatório");
        Objects.requireNonNull(senha, "Senha é obrigatória");

        if (numeroCartao == null || numeroCartao.isBlank()) {
            throw new IllegalArgumentException("Número do cartão é obrigatório");
        }

        if (senha == null || senha.isBlank()) {
            throw new IllegalArgumentException("Senha é obrigatória");
        }
    }
}
