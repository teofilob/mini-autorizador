package br.com.teofilob.miniautorizador.application.dto;

import br.com.teofilob.miniautorizador.domain.enums.StatusAutorizacao;

import java.math.BigDecimal;

public record ResultadoAutorizacao(
        StatusAutorizacao status,
        String mensagem,
        String numeroCartao,
        BigDecimal saldoDisponivel
) {
}
