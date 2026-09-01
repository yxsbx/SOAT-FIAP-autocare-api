# ADR-002 - Uso de Horizontal Pod Autoscaler

## Status

Aceita.

## Contexto

O desafio pede escalabilidade e deteccao de gargalos. API e Web precisam reagir a variacao de carga sem intervencao manual constante.

## Decisao

Usar Kubernetes Horizontal Pod Autoscaler para API e Web, com manifests `backend-hpa.yaml` e `frontend-hpa.yaml` no repositorio `SOAT-FIAP-autocare-infra-k8s`.

## Consequencias Positivas

- Escala replicas conforme consumo de recursos.
- Ajuda a manter latencia sob aumento de trafego.
- E facilmente demonstravel com `kubectl get hpa` e dashboards do New Relic.

## Consequencias Negativas

- Depende de metrics-server/metricas disponiveis no cluster.
- Nao resolve gargalos de banco ou limites externos sozinho.

## Mitigacoes

- Configurar requests/limits nos Deployments.
- Monitorar CPU, memoria, latencia e erros no New Relic.
- Usar alertas para falhas de OS, nao apenas escala automatica.
