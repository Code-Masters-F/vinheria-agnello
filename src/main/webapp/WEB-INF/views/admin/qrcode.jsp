<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<jsp:include page="layout/header.jsp" />

    <main class="admin-main" id="main-content">

        <div class="page-header">
            <div>
                <h1 class="page-title">Gerador de QR Code</h1>
                <p class="page-subtitle">Crie QR Codes para cada ocasião da sua adega.</p>
            </div>
        </div>

        <%-- Feedback --%>
        <c:if test="${not empty errorMessage}">
            <div class="alert alert-danger" role="alert">
                <span class="nav-icon" aria-hidden="true">error</span>
                <c:out value="${errorMessage}" />
            </div>
        </c:if>

        <div class="panel-grid">

            <%-- Generator Form --%>
            <section class="card" aria-label="Formulário de geração de QR Code">
                <div class="card-header">
                    <h2 class="card-title">
                        <span class="nav-icon" aria-hidden="true">qr_code_2</span>
                        Configurar QR Code
                    </h2>
                </div>
                <div class="card-body">
                        <form method="post"
                              action="${pageContext.request.contextPath}/admin/qrcode"
                              id="qrForm">
                            <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">

                            <div class="form-group">
                                <label class="form-label" for="ocasiao">
                                    Ocasião <span class="required" aria-hidden="true">*</span>
                                </label>
                                <select id="ocasiao" name="ocasiao" class="form-control" required>
                                    <option value="">— Selecione a ocasião —</option>
                                    <option value="Jantar Romântico"   ${ocasiao == 'Jantar Romântico'   ? 'selected':''}>Jantar Romântico</option>
                                    <option value="Jantar em Família"  ${ocasiao == 'Jantar em Família'  ? 'selected':''}>Jantar em Família</option>
                                    <option value="Reunião de Negócios"${ocasiao == 'Reunião de Negócios'? 'selected':''}>Reunião de Negócios</option>
                                    <option value="Celebração"         ${ocasiao == 'Celebração'         ? 'selected':''}>Celebração</option>
                                    <option value="Presente"           ${ocasiao == 'Presente'           ? 'selected':''}>Presente</option>
                                    <option value="Casual"             ${ocasiao == 'Casual'             ? 'selected':''}>Casual</option>
                                </select>
                            </div>

                            <button type="submit" class="btn btn-primary" style="width:100%;">
                                <span class="nav-icon" aria-hidden="true">qr_code_2</span>
                                Gerar QR Code
                            </button>
                        </form>
                    </div>
                </section>

                <%-- QR Preview --%>
                <section class="card" aria-label="Visualização do QR Code gerado">
                    <div class="card-header">
                        <h2 class="card-title">
                            <span class="nav-icon" aria-hidden="true">preview</span>
                            Pré-visualização
                        </h2>
                        <c:if test="${not empty targetUrl}">
                            <a href="${not empty qrImageBase64 ? qrImageBase64 : qrApiUrl}" 
                               download="qrcode-${ocasiao}.png"
                               class="btn btn-secondary btn-sm">
                                <span class="nav-icon" aria-hidden="true">download</span>
                                Baixar
                            </a>
                        </c:if>
                    </div>
                    <div class="card-body">
                        <div class="qr-preview-box">
                            <c:choose>
                                <c:when test="${not empty qrImageBase64 or not empty qrApiUrl}">
                                    <div style="text-align:center;">
                                        <img src="${not empty qrImageBase64 ? qrImageBase64 : qrApiUrl}"
                                             alt="QR Code para ${ocasiao}"
                                             style="border-radius:.5rem; max-width:16rem;">
                                        <p class="text-muted mt-2" style="font-size:.75rem;word-break:break-all;">
                                            <c:out value="${targetUrl}" />
                                        </p>
                                    </div>
                                </c:when>
                                <c:otherwise>
                                    <div class="qr-placeholder">
                                        <span class="nav-icon" aria-hidden="true">qr_code_2</span>
                                        <p>Selecione uma ocasião e clique em<br><strong>Gerar QR Code</strong></p>
                                    </div>
                                </c:otherwise>
                            </c:choose>
                        </div>
                    </div>
                </section>

        </div>

    </main>

<jsp:include page="layout/footer.jsp" />
