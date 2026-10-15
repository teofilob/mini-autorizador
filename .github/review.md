Resultado da revisão
Aderência geral: parcial. A implementação cobre corretamente a maioria dos contratos REST e documenta bem a estratégia de concorrência. Os principais desvios são o saldo inicial de 1100.00 em vez de 500.00, a resposta de cartão duplicado baseada nos dados da nova requisição e a ausência de contêinerização da aplicação.
1. Regras de negócio e contratos REST — Parcialmente Conforme
   Gaps encontrados
   •
   O saldo inicial é 1100.00, não 500.00: CriarCartaoService.java:19,36. O teste também fixa 1100.00 (CartaoControllerTest.java:58-60) e o README repete esse valor (README.md:90).
   •
   Ao receber número duplicado, o serviço descarta o cartão encontrado e lança a exceção com o número e a senha da requisição atual (CriarCartaoService.java:32-33). A resposta usa esses valores (CartaoDuplicadoException.java:12-15; ApiExceptionHandler.java:27-32). Se a senha enviada for diferente da já cadastrada, o corpo não representa o cartão existente. O teste usa a mesma senha nos dois envios (CartaoControllerTest.java:63-75) e não cobre esse caso.
   •
   O serviço persiste histórico de transações (AutorizacaoService.java:51-55; TransacaoPersistenceAdapter.java:17-25), embora o checklist indique que essa entidade não é necessária. É uma extensão de escopo, não uma falha nos códigos HTTP; o README documenta essa decisão (README.md:141).
   Conforme
   •
   Cadastro bem-sucedido retorna 201 com senha e numeroCartao (CartaoContoller.java:35-40); duplicidade retorna 422 (ApiExceptionHandler.java:27-32).
   •
   Consulta retorna o saldo numérico em 200 ou 404 sem corpo (CartaoContoller.java:43-47).
   •
   A requisição de transação contém numeroCartao, senhaCartao e valor (TransacaoRequest.java:10-21); aprovação retorna 201 com OK (TransacaoController.java:25-32).
   •
   As recusas mapeiam para 422 e os códigos previstos (ApiExceptionHandler.java:34-53). A ordem implementada é cartão inexistente, senha inválida e saldo insuficiente (AutorizacaoService.java:40-47).
   •
   O débito é feito sob transação e salvo no cartão; há teste de rollback quando a persistência do histórico falha (AutorizacaoService.java:38-55; JpaCartaoRepositoryAdapter.java:35-42; AutorizacaoServiceTransactionTest.java:57-70).
   Plano de ação
1.
Ajustar o saldo inicial para 500.00 e alinhar o teste e o README.
2.
Fazer a resposta de duplicidade usar os dados do cartão já armazenado; cobrir o caso em que a senha recebida difere da existente, inclusive na colisão concorrente.
3.
Decidir se o histórico é uma extensão intencional. Se não for requisito, remover a persistência e seus testes; se for, identificá-lo claramente como decisão adicional.
2. Segurança e autenticação — Parcialmente Conforme
   Gaps encontrados
   •
   HTTP Basic está habilitado (SecurityConfig.java:20-27) e o GET /cartoes/{numeroCartao} exige autenticação. Porém, POST /cartoes e POST /transacoes ficam públicos por causa de .anyRequest().permitAll() (SecurityConfig.java:23-26). Isso diverge do critério do checklist para requisições sem autenticação nesses caminhos. O README confirma essa exposição (README.md:145-148).
   •
   Os testes verificam 401 sem credenciais e com credenciais inválidas apenas na consulta de saldo (CartaoControllerTest.java:110-121); não verificam esses resultados nos POST.
   •
   Há um fallback de senha diferente em SecurityConfig.java:33 (passwordpassword), embora application.properties forneça normalmente o padrão password via APP_SECURITY_PASSWORD. Essa diferença merece harmonização para evitar comportamento inesperado se a propriedade não estiver disponível.
   Conforme
   •
   O padrão efetivo de aplicação usa username/password e permite configuração por variáveis de ambiente (application.properties:14-15).
   •
   Os testes cobrem 401 na consulta e 404 sem corpo após autenticação (CartaoControllerTest.java:102-120). As respostas de negócio 422 também estão mapeadas no handler e exercitadas nos testes de transação.
   Plano de ação
1.
Confirmar se todos os métodos em /cartoes e /transacoes devem exigir Basic Auth; se sim, restringir esses métodos na SecurityConfig e testar ausência e credenciais inválidas.
2.
Padronizar o fallback da senha entre a configuração Java e application.properties.
3.
Manter testes cobrindo 404 e 422 com a configuração de segurança ativa.
3. Configuração do ambiente e execução — Parcialmente Conforme
   Gaps encontrados
   •
   Não há Dockerfile no repositório, nem serviço da aplicação no docker-compose.yml; o Compose inicia apenas o MySQL (docker-compose.yml:3-14). Portanto, a aplicação não pode ser orquestrada junto ao banco via Docker Compose.
   •
   O datasource usa DB_HOST com padrão localhost (src/main/resources/application.properties:9). Isso funciona quando a aplicação roda no host, como orienta o README, mas não quando roda em contêiner: nessa situação, o hostname deve ser mysql, nome do serviço do Compose.
   •
   Não foi possível confirmar se o Compose preserva as declarações originais do desafio, pois não há uma versão original para comparação. O arquivo presente declara MySQL 5.7 e mapeia 3306:3306 (docker-compose.yml:5,9-14).
   Conforme
   •
   O Compose declara somente o banco selecionado, sem serviço de banco alternativo que possa interferir.
   •
   O padrão de senha do datasource coincide com o valor configurado no Compose; o README orienta a configurar as variáveis para execução local.
   Plano de ação
1.
Adicionar um Dockerfile para a aplicação e um serviço correspondente no Compose, ou documentar explicitamente que a aplicação deve rodar apenas no host.
2.
Configurar o hostname do datasource conforme o ambiente: localhost para execução no host e mysql para execução em contêiner.
3.
Manter a comparação com o Compose original como item pendente até que essa referência esteja disponível.
4. Premissas, arquitetura e README — Parcialmente Conforme
   Gaps encontrados
   •
   O README declara saldo inicial 1100.00 (README.md:90), incompatível com o valor exigido no checklist.
   •
   Há instruções de build e testes (README.md:75-82,161-175) e explicação detalhada do lock pessimista (README.md:12-18,139-141), mas não há instruções para construir/executar um contêiner da aplicação, coerente com a ausência de Dockerfile.
   •
   O README descreve persistência do histórico como parte da transação (README.md:141), embora o checklist trate a entidade de transação como desnecessária.
   •
   A estratégia opcional de não usar if não está implementada como padrão no fluxo inteiro: CriarCartaoService.java:32 contém uma condição explícita. O README não a apresenta como desafio implementado; portanto, isso só é gap se esse opcional fizer parte do escopo esperado.
   Conforme
   •
   Estão documentadas a escolha do MySQL, a integridade por unicidade e a prevenção de gastos concorrentes (README.md:7-21).
   •
   Há comandos para compilar, rodar testes e gerar relatório JaCoCo (README.md:75-82,161-175).
   •
   As instruções explicam a execução local com o banco em Docker e a aplicação no host (README.md:41-82).
   Plano de ação
1.
Alinhar documentação, implementação e testes ao saldo exigido.
2.
Documentar ou remover a persistência do histórico conforme a decisão de escopo.
3.
Completar as instruções Docker se a aplicação passar a ser contêinerizada.
4.
Declarar explicitamente se o desafio opcional de eliminar if foi implementado ou ficou fora do escopo.

## Reavaliação após implementação

Os apontamentos de implementação acima foram tratados:

- Saldo inicial ajustado para `500.00`; testes de cadastro e consulta atualizados.
- Cartão duplicado agora retorna os dados persistidos do cartão, inclusive quando a senha enviada no segundo pedido é diferente. A colisão concorrente também é reconsultada após violação de integridade.
- Persistência do histórico de transações removida; a autorização mantém a atualização do saldo sob lock pessimista e transação.
- HTTP Basic agora é exigido em todos os métodos de `/cartoes` e `/transacoes`; os testes cobrem POST autenticado e respostas `401` sem credenciais ou com credenciais incorretas.
- Fallback de senha alinhado para `password`.
- Dockerfile Java 25 e serviço da API adicionados ao Compose, com healthcheck do MySQL, conexão pelo hostname `mysql` e instruções atualizadas no README.
- O README declara que Strategy/Chain of Responsibility para eliminar condicionais não foi implementado e explica que o histórico não é persistido.

Validação após as alterações: `docker compose config --quiet` e `docker compose build app` concluíram com sucesso; a suíte Maven executada em contêiner Java 25 passou (30 testes, nenhuma falha ou erro). A configuração do Compose original ainda não pode ser comparada sem a versão de referência do desafio.