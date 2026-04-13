<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<jsp:include page="layout/header.jsp" />

        <div class="page-header">
            <div>
                <h1 class="page-title">Relatórios</h1>
                <p class="page-subtitle">Análise de desempenho e inventário da adega.</p>
            </div>
        </div>

        <%-- Summary Metrics --%>
        <section class="metrics-grid" aria-label="Métricas de inventário">
            <div class="metric-card">
                <div class="metric-icon purple" aria-hidden="true">wine_bar</div>
                <div class="metric-label">Total de Vinhos</div>
                <div class="metric-value"><c:out value="${totalVinhos}" /></div>
                <div class="metric-sub">no catálogo ativo</div>
            </div>
            <div class="metric-card">
                <div class="metric-icon red" aria-hidden="true">warning</div>
                <div class="metric-label">Alertas de Estoque</div>
                <div class="metric-value"><c:out value="${alertasEstoque.size()}" /></div>
                <div class="metric-sub">vinhos abaixo do mínimo</div>
            </div>
        </section>

        <div class="panel-grid">

            <%-- Wines by Type --%>
            <section class="card" aria-label="Vinhos por tipo">
                <div class="card-header">
                    <h2 class="card-title">
                        <span class="nav-icon" aria-hidden="true">pie_chart</span>
                        Distribuição por Tipo
                    </h2>
                </div>
                <div class="card-body">
                    <c:choose>
                        <c:when test="${empty vinhosPorTipo}">
                            <p class="text-muted">Nenhum dado disponível.</p>
                        </c:when>
                        <c:otherwise>
                            <ul style="list-style:none;display:flex;flex-direction:column;gap:.75rem;">
                                <c:forEach var="entry" items="${vinhosPorTipo}">
                                    <li class="d-flex align-center justify-between">
                                        <span class="text-secondary"><c:out value="${entry.key}" /></span>
                                        <span class="badge badge-purple"><c:out value="${entry.value}" /></span>
                                    </li>
                                </c:forEach>
                            </ul>
                        </c:otherwise>
                    </c:choose>
                </div>
            </section>

            <%-- Top Wines by Stock --%>
            <section class="card" aria-label="Top vinhos por estoque">
                <div class="card-header">
                    <h2 class="card-title">
                        <span class="nav-icon" aria-hidden="true">star</span>
                        Top Vinhos (Estoque)
                    </h2>
                </div>
                <div class="card-body">
                    <c:choose>
                        <c:when test="${empty topVinhos}">
                            <p class="text-muted">Nenhum vinho cadastrado.</p>
                        </c:when>
                        <c:otherwise>
                            <ul style="list-style:none;display:flex;flex-direction:column;gap:.75rem;">
                                <c:forEach var="v" items="${topVinhos}">
                                    <li class="d-flex align-center justify-between">
                                        <span class="text-secondary"><c:out value="${v.nome}" /></span>
                                        <span class="badge badge-success stock-ok"><c:out value="${v.estoque}" /> un.</span>
                                    </li>
                                </c:forEach>
                            </ul>
                        </c:otherwise>
                    </c:choose>
                </div>
            </section>

        </div>

        <%-- Stock Alerts Table --%>
        <c:if test="${not empty alertasEstoque}">
        <section class="card" aria-label="Vinhos com estoque abaixo do mínimo">
            <div class="card-header">
                <h2 class="card-title">
                    <span class="nav-icon" aria-hidden="true">warning</span>
                    Alertas de Estoque Baixo
                </h2>
            </div>
            <div class="data-table-wrap">
                <table class="data-table" aria-label="Tabela de vinhos com estoque baixo">
                    <thead>
                        <tr>
                            <th scope="col">Vinho</th>
                            <th scope="col">Tipo</th>
                            <th scope="col">Estoque</th>
                            <th scope="col">Mínimo</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="v" items="${alertasEstoque}">
                            <tr>
                                <td class="text-primary fw-600"><c:out value="${v.nome}" /></td>
                                <td class="text-secondary"><c:out value="${v.tipo}" /></td>
                                <td class="stock-low"><c:out value="${v.estoque}" /></td>
                                <td class="text-muted"><c:out value="${v.estoqueMinimo}" /></td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
            </div>
        </section>
        </c:if>

<jsp:include page="layout/footer.jsp" />
