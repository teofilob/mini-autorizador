package br.com.teofilob.miniautorizador.infrastructure.adapter.inbound.dto;

import br.com.teofilob.miniautorizador.application.dto.ResultadoCriarCartao;

public record CartaoResponse(String senha, String numeroCartao) {

    public static CartaoResponse from(ResultadoCriarCartao resultado) {
        return new CartaoResponse(resultado.senha(), resultado.numeroCartao());
    }
}
