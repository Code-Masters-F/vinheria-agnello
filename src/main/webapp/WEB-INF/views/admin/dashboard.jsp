<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<fmt:setLocale value="pt_BR" />
<jsp:include page="layout/header.jsp" />

        <%-- ====== Page Header ====== --%>
        <div class="page-header">
            <div>
                <h1 class="page-title">Painel de Controle</h1>
                <p class="page-subtitle">
                    <c:out value="${pageSubtitle}" />
                </p>
            </div>
            <div class="page-header-actions">
                <span class="period-filter">
                    <span class="nav-icon" aria-hidden="true">calendar_today</span>
                    Últimos 7 dias
                </span>
                <a href="${pageContext.request.contextPath}/admin/pedidos" class="btn btn-primary">
                    <span class="nav-icon" aria-hidden="true">add</span>
                    Novo Pedido
                </a>
            </div>
        </div>

        <%-- Feedback messages (PRG pattern) --%>
        <c:if test="${not empty successMessage}">
            <div class="alert alert-success" role="alert">
                <span class="nav-icon" aria-hidden="true">check_circle</span>
                <c:out value="${successMessage}" />
            </div>
        </c:if>
        <c:if test="${not empty errorMessage}">
            <div class="alert alert-danger" role="alert">
                <span class="nav-icon" aria-hidden="true">error</span>
                <c:out value="${errorMessage}" />
            </div>
        </c:if>

        <%-- ====== KPI Cards ====== --%>
        <section class="kpi-grid" aria-label="Indicadores do Painel">

            <%-- KPI 1: Vendas Totais (Mês) --%>
            <div class="kpi-card">
                <div class="kpi-icon green" aria-hidden="true">payments</div>
                <div class="kpi-body">
                    <div class="kpi-label">Vendas Totais (Mês)</div>
                    <div class="kpi-value">
                        R$ <fmt:formatNumber value="${vendasMes}" type="number" minFractionDigits="2" maxFractionDigits="2" />
                    </div>
                    <div class="kpi-sub">
                        <c:choose>
                            <c:when test="${variacaoVendas > 0}">
                                <span class="kpi-trend kpi-trend-up">
                                    <span class="nav-icon" aria-hidden="true">trending_up</span>
                                    +<fmt:formatNumber value="${variacaoVendas}" type="number" maxFractionDigits="1" />%
                                </span>
                                vs. mês anterior
                            </c:when>
                            <c:when test="${variacaoVendas < 0}">
                                <span class="kpi-trend kpi-trend-down">
                                    <span class="nav-icon" aria-hidden="true">trending_down</span>
                                    <fmt:formatNumber value="${variacaoVendas}" type="number" maxFractionDigits="1" />%
                                </span>
                                vs. mês anterior
                            </c:when>
                            <c:otherwise>
                                <span class="kpi-trend kpi-trend-neutral">
                                    <span class="nav-icon" aria-hidden="true">trending_flat</span>
                                    0%
                                </span>
                                vs. mês anterior
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>
            </div>

            <%-- KPI 2: Pedidos Pendentes --%>
            <div class="kpi-card">
                <div class="kpi-icon gold" aria-hidden="true">shopping_cart</div>
                <div class="kpi-body">
                    <div class="kpi-label">Pedidos Pendentes</div>
                    <div class="kpi-value"><c:out value="${pedidosPendentes}" /></div>
                    <div class="kpi-sub">Média de processamento: 45 min</div>
                </div>
            </div>

            <%-- KPI 3: Alertas de Estoque --%>
            <div class="kpi-card">
                <div class="kpi-icon red" aria-hidden="true">inventory</div>
                <div class="kpi-body">
                    <div class="kpi-label">Alertas de Estoque</div>
                    <div class="kpi-value">
                        <fmt:formatNumber value="${qtdAlertasEstoque}" pattern="00" />
                    </div>
                    <div class="kpi-sub">
                        <c:choose>
                            <c:when test="${severidadeEstoque == 'Crítico'}">
                                <span class="severity-badge severity-critical">Crítico</span>
                            </c:when>
                            <c:when test="${severidadeEstoque == 'Atenção'}">
                                <span class="severity-badge severity-warning">Atenção</span>
                            </c:when>
                            <c:otherwise>
                                <span class="severity-badge severity-normal">Normal</span>
                            </c:otherwise>
                        </c:choose>
                        vinhos abaixo da reserva mínima
                    </div>
                </div>
            </div>

        </section>

        <%-- ====== Desempenho Semanal (CSS-only bar chart, static data v1) ====== --%>
        <section class="card mb-3" aria-label="Desempenho Semanal">
            <div class="card-header">
                <div>
                    <h2 class="card-title">Desempenho Semanal</h2>
                    <p class="card-subtitle">Volume de vendas por dia</p>
                </div>
                <div class="chart-legend">
                    <span class="chart-legend-item">
                        <span class="chart-legend-dot chart-legend-current"></span>
                        Sem. Atual
                    </span>
                    <span class="chart-legend-item">
                        <span class="chart-legend-dot chart-legend-previous"></span>
                        Sem. Anterior
                    </span>
                </div>
            </div>
            <div class="card-body">
                <div class="bar-chart" role="img" aria-label="Gráfico de barras de desempenho semanal">
                    <div class="bar-chart-group">
                        <div class="bar-chart-bars">
                            <div class="bar bar-current" style="height: 65%;" title="Sem. Atual: 65%"></div>
                            <div class="bar bar-previous" style="height: 45%;" title="Sem. Anterior: 45%"></div>
                        </div>
                        <span class="bar-chart-label">SEG</span>
                    </div>
                    <div class="bar-chart-group">
                        <div class="bar-chart-bars">
                            <div class="bar bar-current" style="height: 80%;" title="Sem. Atual: 80%"></div>
                            <div class="bar bar-previous" style="height: 60%;" title="Sem. Anterior: 60%"></div>
                        </div>
                        <span class="bar-chart-label">TER</span>
                    </div>
                    <div class="bar-chart-group">
                        <div class="bar-chart-bars">
                            <div class="bar bar-current" style="height: 55%;" title="Sem. Atual: 55%"></div>
                            <div class="bar bar-previous" style="height: 70%;" title="Sem. Anterior: 70%"></div>
                        </div>
                        <span class="bar-chart-label">QUA</span>
                    </div>
                    <div class="bar-chart-group">
                        <div class="bar-chart-bars">
                            <div class="bar bar-current" style="height: 90%;" title="Sem. Atual: 90%"></div>
                            <div class="bar bar-previous" style="height: 50%;" title="Sem. Anterior: 50%"></div>
                        </div>
                        <span class="bar-chart-label">QUI</span>
                    </div>
                    <div class="bar-chart-group">
                        <div class="bar-chart-bars">
                            <div class="bar bar-current" style="height: 100%;" title="Sem. Atual: 100%"></div>
                            <div class="bar bar-previous" style="height: 75%;" title="Sem. Anterior: 75%"></div>
                        </div>
                        <span class="bar-chart-label">SEX</span>
                    </div>
                    <div class="bar-chart-group">
                        <div class="bar-chart-bars">
                            <div class="bar bar-current" style="height: 85%;" title="Sem. Atual: 85%"></div>
                            <div class="bar bar-previous" style="height: 90%;" title="Sem. Anterior: 90%"></div>
                        </div>
                        <span class="bar-chart-label">SAB</span>
                    </div>
                    <div class="bar-chart-group">
                        <div class="bar-chart-bars">
                            <div class="bar bar-current" style="height: 40%;" title="Sem. Atual: 40%"></div>
                            <div class="bar bar-previous" style="height: 35%;" title="Sem. Anterior: 35%"></div>
                        </div>
                        <span class="bar-chart-label">DOM</span>
                    </div>
                </div>
            </div>
        </section>

        <%-- ====== Últimos Pedidos ====== --%>
        <section class="card" aria-label="Últimos Pedidos">
            <div class="card-header">
                <h2 class="card-title">Últimos Pedidos</h2>
                <a href="${pageContext.request.contextPath}/admin/pedidos" class="btn btn-ghost btn-sm">
                    Ver todos os pedidos
                    <span class="nav-icon" aria-hidden="true">arrow_forward</span>
                </a>
            </div>

            <c:choose>
                <c:when test="${not empty ultimosPedidos}">
                    <div class="data-table-wrap">
                        <table class="data-table" aria-label="Tabela de pedidos recentes">
                            <thead>
                                <tr>
                                    <th scope="col">ID Pedido</th>
                                    <th scope="col">Cliente</th>
                                    <th scope="col">Vinho</th>
                                    <th scope="col">Data</th>
                                    <th scope="col">Total</th>
                                    <th scope="col">Status</th>
                                    <th scope="col">Ações</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="p" items="${ultimosPedidos}">
                                    <tr>
                                        <td class="fw-600"><c:out value="${p.idFormatado}" /></td>
                                        <td>
                                            <div class="d-flex align-center gap-1">
                                                <span class="order-avatar" aria-hidden="true"><c:out value="${p.clienteInicial}" /></span>
                                                <span><c:out value="${p.clienteNome != null ? p.clienteNome : 'Cliente Anônimo'}" /></span>
                                            </div>
                                        </td>
                                        <td class="text-secondary" style="font-style: italic;">
                                            <c:out value="${p.vinhoNome != null ? p.vinhoNome : '-'}" />
                                        </td>
                                        <td class="text-secondary">
                                            <c:if test="${p.criadoEm != null}">
                                                <fmt:parseDate value="${p.criadoEm}" pattern="yyyy-MM-dd'T'HH:mm" var="parsedDate" type="both" />
                                                <fmt:formatDate value="${parsedDate}" pattern="dd/MM/yyyy HH:mm" />
                                            </c:if>
                                        </td>
                                        <td class="fw-600">
                                            R$ <fmt:formatNumber value="${p.total}" type="number" minFractionDigits="2" maxFractionDigits="2" />
                                        </td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${p.status == 'entregue'}">
                                                    <span class="status-badge status-success">Entregue</span>
                                                </c:when>
                                                <c:when test="${p.status == 'cancelado'}">
                                                    <span class="status-badge status-error">Cancelado</span>
                                                </c:when>
                                                <c:when test="${p.status == 'em_separacao' || p.status == 'pago'}">
                                                    <span class="status-badge status-warning">Processando</span>
                                                </c:when>
                                                <c:when test="${p.status == 'pronto'}">
                                                    <span class="status-badge status-info">Pronto</span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="status-badge status-neutral">Aguardando</span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td>
                                            <button class="btn btn-icon btn-ghost" aria-label="Ações do pedido" disabled>
                                                <span class="nav-icon">more_vert</span>
                                            </button>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="card-body" style="text-align: center; padding: 2rem;">
                        <span class="nav-icon text-muted" aria-hidden="true" style="font-size: 2.5rem;">receipt_long</span>
                        <p class="text-muted mt-2">Nenhum pedido recente</p>
                    </div>
                </c:otherwise>
            </c:choose>
        </section>

<jsp:include page="layout/footer.jsp" />
