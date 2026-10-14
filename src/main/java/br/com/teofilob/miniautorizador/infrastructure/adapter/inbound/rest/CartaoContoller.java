package br.com.teofilob.miniautorizador.infrastructure.adapter.inbound.rest;

import br.com.teofilob.miniautorizador.application.dto.CriarCartaoCommand;
import br.com.teofilob.miniautorizador.application.dto.ResultadoCriarCartao;
import br.com.teofilob.miniautorizador.application.port.inbound.ConsultarSaldoUseCase;
import br.com.teofilob.miniautorizador.application.port.inbound.CriarCartaoUseCase;
import br.com.teofilob.miniautorizador.infrastructure.adapter.inbound.dto.CartaoRequest;
import br.com.teofilob.miniautorizador.infrastructure.adapter.inbound.dto.CartaoResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/cartoes")
public class CartaoContoller {

    private final CriarCartaoUseCase criarCartaoUseCase;
    private final ConsultarSaldoUseCase consultarSaldoUseCase;

    public CartaoContoller(
            CriarCartaoUseCase criarCartaoUseCase,
            ConsultarSaldoUseCase consultarSaldoUseCase) {
        this.criarCartaoUseCase = criarCartaoUseCase;
        this.consultarSaldoUseCase = consultarSaldoUseCase;
    }

    @PostMapping
    public ResponseEntity<CartaoResponse> criarCartao(@Valid @RequestBody CartaoRequest request) {
        ResultadoCriarCartao resultado = criarCartaoUseCase.criarCartao(
                new CriarCartaoCommand(request.numeroCartao(), request.senha())
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(CartaoResponse.from(resultado));
    }

    @GetMapping("/{numeroCartao}")
    public ResponseEntity<BigDecimal> consultarSaldo(@PathVariable String numeroCartao) {
        return consultarSaldoUseCase.consultarSaldo(numeroCartao)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
