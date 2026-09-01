# ADR-003 - Uso do New Relic para observabilidade

## Status

Aceita.

## Contexto

A Fase 3 exige monitoramento de APIs, recursos Kubernetes, healthchecks, uptime, alertas, logs estruturados e dashboards operacionais. A equipe ja possui familiaridade com New Relic.

## Decisao

Usar New Relic como plataforma de observabilidade da Fase 3.

## Escopo

- Java Agent na API Spring Boot.
- New Relic Kubernetes integration via Helm `nri-bundle` no EKS.
- Logs JSON com `correlationId`.
- Metricas customizadas de ordens de servico via Micrometer.
- Dashboards para volume diario, tempo medio por status e falhas.

## Consequencias Positivas

- Curva de aprendizado menor para a equipe.
- Uma ferramenta cobre APM, logs, infraestrutura e dashboards.
- Facilita demonstracao ao vivo no video.

## Consequencias Negativas

- Requer license key e configuracao manual de conta.
- Alguns dashboards/alertas precisam ser criados na UI ou por automacao adicional.

## Mitigacoes

- Manter variaveis/secrets documentados nos READMEs.
- Enviar metricas e logs padronizados pela aplicacao.
- Criar queries NRQL documentadas para reproduzir dashboards.
