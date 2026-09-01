# RFC-002 - Escolha do PostgreSQL no Amazon RDS

## Status

Aprovada para a entrega da Fase 3.

## Contexto

O dominio da oficina possui relacoes fortes: cliente possui veiculos, OS referencia cliente e veiculo, OS possui servicos e pecas, pecas possuem movimentacoes de estoque e usuarios podem estar vinculados a cliente ou empresa. A consistencia dessas relacoes e mais importante do que flexibilidade documental.

## Decisao Proposta

Usar PostgreSQL 16 em Amazon RDS, provisionado por Terraform no repositorio `SOAT-FIAP-autocare-infra-db`.

## Justificativa

- Modelo relacional combina naturalmente com o dominio existente.
- Chaves estrangeiras protegem integridade entre cliente, veiculo, OS, itens e estoque.
- Indices atendem consultas por cliente, veiculo, status, part_id e datas de leads.
- RDS reduz carga operacional de backup, patching, storage, monitoramento basico e alta disponibilidade.
- PostgreSQL suporta transacoes ACID, constraints, tipos numericos adequados para valores monetarios e UUIDs.

## Alternativas Consideradas

| Alternativa | Pontos positivos | Motivos para nao escolher agora |
|---|---|---|
| MySQL/RDS | Gerenciado e relacional. | PostgreSQL e mais expressivo para evolucoes futuras e ja estava alinhado ao projeto. |
| DynamoDB | Escala gerenciada e baixa latencia. | Exigiria remodelar agregados e duplicar consultas relacionais importantes. |
| PostgreSQL em Kubernetes | Menor dependencia de RDS. | Aumenta operacao, backup e risco para uma entrega que pede banco gerenciado. |

## Consequencias

- A API segue com Spring Data JPA e Flyway.
- A Lambda consulta diretamente a tabela `customers` apenas para autenticacao CPF.
- Segredos de conexao sao publicados no Secrets Manager.
- O schema deve continuar versionado por migrations, nao por alteracoes manuais no banco.
