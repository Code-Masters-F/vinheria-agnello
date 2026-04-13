<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<jsp:include page="layout/header.jsp" />

        <div class="page-header">
            <div>
                <h1 class="page-title">Gestão de Pedidos</h1>
                <p class="page-subtitle">Acompanhe e atualize o status de cada pedido da adega.</p>
            </div>
        </div>

        <%-- Feedback --%>
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

        <%-- Orders Table --%>
        <section class="card" aria-label="Lista de Pedidos">
            <div class="card-header">
                <h2 class="card-title">
                    <span class="nav-icon" aria-hidden="true">shopping_cart</span>
                    Pedidos Recentes
                </h2>
            </div>
            <div class="table-responsive">
                <c:choose>
                    <c:when test="${empty pedidos}">
                        <div class="card-body" style="text-align:center; padding:3rem;">
                            <span class="nav-icon" style="font-family:'Material Icons Round';font-size:3rem;font-style:normal;color:var(--color-text-muted);" aria-hidden="true">receipt_long</span>
                            <p class="text-muted mt-2">Nenhum pedido registrado ainda.</p>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <table class="data-table" aria-label="Tabela de pedidos">
                            <thead>
                                <tr>
                                    <th scope="col">#</th>
                                    <th scope="col">Cliente</th>
                                    <th scope="col">Total</th>
                                    <th scope="col">Status</th>
                                    <th scope="col">Data</th>
                                    <th scope="col">Ação</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="p" items="${pedidos}">
                                    <tr>
                                        <td class="text-primary fw-600">#<c:out value="${p.id}"/></td>
                                        <td><c:out value="${p.clienteId}"/></td>
                                        <td class="text-accent fw-600">R$ <c:out value="${p.total}"/></td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${p.status == 'aguardando_pagamento'}">
                                                    <span class="badge badge-warning"><c:out value="${p.status}"/></span>
                                                </c:when>
                                                <c:when test="${p.status == 'pago' || p.status == 'pronto'}">
                                                    <span class="badge badge-success"><c:out value="${p.status}"/></span>
                                                </c:when>
                                                <c:when test="${p.status == 'cancelado'}">
                                                    <span class="badge badge-danger"><c:out value="${p.status}"/></span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="badge badge-info"><c:out value="${p.status}"/></span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td class="text-muted"><c:out value="${p.criadoEm}"/></td>
                                        <td>
                                            <form method="post"
                                                  action="${pageContext.request.contextPath}/admin/pedidos"
                                                  style="display:flex;gap:.5rem;align-items:center;">
                                                <input type="hidden" name="pedidoId" value="${p.id}">
                                                <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
                                                <select name="novoStatus" class="form-control" style="width:auto;padding:.375rem .625rem;font-size:.8125rem;">
                                                    <c:forEach var="s" items="${statusOptions}">
                                                        <option value="${s}" ${p.status == s ? 'selected' : ''}>
                                                            <c:out value="${s}"/>
                                                        </option>
                                                    </c:forEach>
                                                </select>
                                                <button type="submit" class="btn btn-primary btn-sm">
                                                    Atualizar
                                                </button>
                                            </form>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </c:otherwise>
                </c:choose>
            </div>
        </section>

<jsp:include page="layout/footer.jsp" />
