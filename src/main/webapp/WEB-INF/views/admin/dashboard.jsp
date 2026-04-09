<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<jsp:include page="layout/header.jsp" />

    <main class="admin-main" id="main-content">

        <%-- Page Header --%>
        <div class="page-header">
            <div>
                <h1 class="page-title">Painel de Controle</h1>
                <p class="page-subtitle">
                    <c:out value="${pageSubtitle}" />
                </p>
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

        <%-- Metric Cards --%>
        <section class="metrics-grid" aria-label="Métricas do Painel">

            <div class="metric-card">
                <div class="metric-icon purple" aria-hidden="true">wine_bar</div>
                <div class="metric-label">Total de Vinhos</div>
                <div class="metric-value"><c:out value="${totalVinhos}" /></div>
                <div class="metric-sub">no catálogo ativo</div>
            </div>

            <div class="metric-card">
                <div class="metric-icon red" aria-hidden="true">warning</div>
                <div class="metric-label">Alertas de Estoque</div>
                <div class="metric-value">
                    <c:out value="${qtdAlertasEstoque}" />
                </div>
                <div class="metric-sub">vinhos abaixo da reserva mínima</div>
            </div>

        </section>

        <%-- Stock Alerts Table --%>
        <c:if test="${qtdAlertasEstoque > 0}">
        <section class="card mb-3" aria-label="Vinhos com estoque baixo">
            <div class="card-header">
                <h2 class="card-title">
                    <span class="nav-icon" aria-hidden="true">warning</span>
                    Alertas de Estoque
                </h2>
            </div>
            <div class="data-table-wrap">
                <table class="data-table" aria-label="Vinhos com estoque abaixo do mínimo">
                    <thead>
                        <tr>
                            <th scope="col">Vinho</th>
                            <th scope="col">Tipo</th>
                            <th scope="col">Estoque Atual</th>
                            <th scope="col">Estoque Mínimo</th>
                            <th scope="col">Ação</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="v" items="${vinhos}">
                            <c:if test="${v.estoque <= v.estoqueMinimo}">
                                <tr>
                                    <td class="text-primary fw-600"><c:out value="${v.nome}" /></td>
                                    <td><c:out value="${v.tipo}" /></td>
                                    <td class="stock-low"><c:out value="${v.estoque}" /></td>
                                    <td class="text-muted"><c:out value="${v.estoqueMinimo}" /></td>
                                    <td>
                                        <a href="${pageContext.request.contextPath}/admin/catalogo?id=${v.id}"
                                           class="btn btn-secondary btn-sm">
                                            <span class="nav-icon" aria-hidden="true">edit</span>
                                            Atualizar
                                        </a>
                                    </td>
                                </tr>
                            </c:if>
                        </c:forEach>
                    </tbody>
                </table>
            </div>
        </section>
        </c:if>

        <%-- Quick Navigation Cards --%>
        <section aria-label="Acesso rápido">
            <h2 class="page-title mb-2" style="font-size: 1rem;">Acesso Rápido</h2>
            <div class="panel-grid">
                <a href="${pageContext.request.contextPath}/admin/catalogo" class="card" style="text-decoration:none;">
                    <div class="card-body d-flex align-center gap-2">
                        <div class="metric-icon purple" aria-hidden="true">wine_bar</div>
                        <div>
                            <div class="text-primary fw-600">Catálogo</div>
                            <div class="text-muted" style="font-size:.8125rem;">Gerencie seus vinhos</div>
                        </div>
                    </div>
                </a>
                <a href="${pageContext.request.contextPath}/admin/pedidos" class="card" style="text-decoration:none;">
                    <div class="card-body d-flex align-center gap-2">
                        <div class="metric-icon gold" aria-hidden="true">shopping_cart</div>
                        <div>
                            <div class="text-primary fw-600">Pedidos</div>
                            <div class="text-muted" style="font-size:.8125rem;">Acompanhe os pedidos</div>
                        </div>
                    </div>
                </a>
                <a href="${pageContext.request.contextPath}/admin/relatorios" class="card" style="text-decoration:none;">
                    <div class="card-body d-flex align-center gap-2">
                        <div class="metric-icon blue" aria-hidden="true">analytics</div>
                        <div>
                            <div class="text-primary fw-600">Relatórios</div>
                            <div class="text-muted" style="font-size:.8125rem;">Analise o desempenho</div>
                        </div>
                    </div>
                </a>
                <a href="${pageContext.request.contextPath}/admin/qrcode" class="card" style="text-decoration:none;">
                    <div class="card-body d-flex align-center gap-2">
                        <div class="metric-icon green" aria-hidden="true">qr_code_2</div>
                        <div>
                            <div class="text-primary fw-600">QR Code</div>
                            <div class="text-muted" style="font-size:.8125rem;">Gere QR Codes da adega</div>
                        </div>
                    </div>
                </a>
            </div>
        </section>

    </main>

<jsp:include page="layout/footer.jsp" />
