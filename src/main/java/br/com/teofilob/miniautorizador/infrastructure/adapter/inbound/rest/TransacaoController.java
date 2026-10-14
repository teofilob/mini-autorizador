package br.com.teofilob.miniautorizador.infrastructure.adapter.inbound.rest;

import br.com.teofilob.miniautorizador.application.dto.TransacaoCommand;
import br.com.teofilob.miniautorizador.application.port.inbound.AutorizacaoUseCase;
import br.com.teofilob.miniautorizador.infrastructure.adapter.inbound.dto.TransacaoRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/transacoes")
public class TransacaoController {

    private final AutorizacaoUseCase autorizacaoUseCase;

    public TransacaoController(AutorizacaoUseCase autorizacaoUseCase) {
        this.autorizacaoUseCase = autorizacaoUseCase;
    }

    @PostMapping
    public ResponseEntity<String> autorizar(@Valid @RequestBody TransacaoRequest request) {
        autorizacaoUseCase.autorizar(
                new TransacaoCommand(request.numeroCartao(), request.senhaCartao(), request.valor()));

        return ResponseEntity.status(HttpStatus.CREATED)
                .contentType(MediaType.TEXT_PLAIN)
                .body("OK");
    }
}
