/**
 * Domain layer for the AutoCare Hub workshop service lifecycle.
 *
 * <p>The code keeps stable English class names, while the ubiquitous language used by the academic
 * documentation is:
 *
 * <ul>
 *   <li>{@code Customer}: Cliente
 *   <li>{@code Vehicle}: Veículo
 *   <li>{@code ServiceOrder}: Ordem de Serviço
 *   <li>{@code WorkshopService}: Serviço
 *   <li>{@code Part}: Peça/Insumo
 *   <li>{@code Budget}: Orçamento
 *   <li>{@code BudgetItem}: Item de orçamento
 *   <li>{@code StockMovement}: Movimentação de estoque
 *   <li>{@code Document}: CPF/CNPJ
 *   <li>{@code Plate}: Placa
 *   <li>{@code Money}: Valor monetário
 * </ul>
 *
 * <p>{@code ServiceOrder} is the main aggregate for the atendimento flow. It centralizes status
 * transitions, budget generation, approval and the rules that protect the Ordem de Serviço
 * lifecycle.
 */
@org.jspecify.annotations.NullMarked
package br.com.autocarehub.domain.model;
