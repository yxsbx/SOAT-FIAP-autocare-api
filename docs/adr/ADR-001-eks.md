# ADR-001 - Uso de Amazon EKS para workloads Kubernetes

## Status

Aceita.

## Contexto

A Fase 3 exige aplicacao principal executando em Kubernetes, escalabilidade, deploy automatico e infraestrutura como codigo. A Fase 2 ja possuia manifests Kubernetes locais, mas nao um cluster gerenciado real.

## Decisao

Usar Amazon EKS com managed node group provisionado pelo repositorio `SOAT-FIAP-autocare-infra-k8s`.

## Consequencias Positivas

- Mantem compatibilidade com manifests Kubernetes ja existentes.
- Permite HPA, services, probes e rollout declarativo.
- Integra com ECR, API Gateway via VPC Link e New Relic Kubernetes integration.
- Reduz operacao do control plane Kubernetes.

## Consequencias Negativas

- EKS tem custo fixo e exige configuracao de rede/IAM.
- Para um MVP pequeno, pode ser mais complexo do que ECS ou App Runner.

## Mitigacoes

- Usar node group pequeno em homolog.
- Manter Terraform validado e variaveis documentadas.
- Automatizar deploy por GitHub Actions.
