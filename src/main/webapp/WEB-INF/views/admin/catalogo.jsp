<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<jsp:include page="layout/header.jsp" />

        <%-- Page Header --%>
        <div class="page-header">
            <div>
                <h1 class="page-title">Catálogo de Vinhos</h1>
                <p class="page-subtitle">
                    Curadoria digital da adega. Monitore estoques, gerencie safras e mantenha seu portfólio.
                </p>
            </div>
            <button class="btn btn-primary" id="btnAddWine"
                    onclick="document.getElementById('formWine').style.display='block'; this.style.display='none';"
                    aria-expanded="false" aria-controls="formWine">
                <span class="nav-icon" aria-hidden="true">add</span>
                Novo Vinho
            </button>
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

        <%-- Add / Edit Wine Form (hidden by default) --%>
        <section class="card mb-3" id="formWine" style="display:none;" aria-label="Formulário de Vinho">
            <div class="card-header">
                <h2 class="card-title">
                    <span class="nav-icon" aria-hidden="true">wine_bar</span>
                    Cadastrar Novo Vinho
                </h2>
            </div>
            <div class="card-body">
                <form method="post" action="${pageContext.request.contextPath}/admin/catalogo"
                      id="wineForm" novalidate>
                    <input type="hidden" name="action" value="create" id="formAction">
                    <input type="hidden" name="id"     value="" id="formId">
                    <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">

                    <div class="form-grid">
                        <div class="form-group">
                            <label class="form-label" for="nome">
                                Nome <span class="required" aria-hidden="true">*</span>
                            </label>
                            <input type="text" id="nome" name="nome" class="form-control"
                                   placeholder="Ex: Malbec Reserva" required maxlength="120">
                            <span class="field-error" id="nomeError"></span>
                        </div>
                        <div class="form-group">
                            <label class="form-label" for="tipo">Tipo</label>
                            <select id="tipo" name="tipo" class="form-control">
                                <option value="">— Selecione —</option>
                                <c:forEach var="t" items="${tipos}">
                                    <option value="${t}"><c:out value="${t}" /></option>
                                </c:forEach>
                            </select>
                        </div>
                    </div>

                    <div class="form-grid form-grid-3">
                        <div class="form-group">
                            <label class="form-label" for="preco">
                                Preço (R$) <span class="required" aria-hidden="true">*</span>
                            </label>
                            <input type="number" id="preco" name="preco" class="form-control"
                                   placeholder="0.00" step="0.01" min="0.01" required>
                        </div>
                        <div class="form-group">
                            <label class="form-label" for="estoque">Estoque</label>
                            <input type="number" id="estoque" name="estoque" class="form-control"
                                   placeholder="0" min="0">
                        </div>
                        <div class="form-group">
                            <label class="form-label" for="estoqueMinimo">Estoque Mínimo</label>
                            <input type="number" id="estoqueMinimo" name="estoqueMinimo"
                                   class="form-control" placeholder="0" min="0">
                        </div>
                    </div>

                    <div class="form-grid">
                        <div class="form-group">
                            <label class="form-label" for="pais">País</label>
                            <input type="text" id="pais" name="pais" class="form-control" placeholder="Ex: Argentina">
                        </div>
                        <div class="form-group">
                            <label class="form-label" for="safra">Safra</label>
                            <input type="text" id="safra" name="safra" class="form-control" placeholder="Ex: 2020">
                        </div>
                    </div>

                    <div class="form-group">
                        <label class="form-label" for="descricao">Descrição</label>
                        <textarea id="descricao" name="descricao" class="form-control"
                                  placeholder="Descreva as características do vinho..."></textarea>
                    </div>

                    <div class="form-group">
                        <label class="form-label" for="fotoUrl">URL da Imagem</label>
                        <input type="text" id="fotoUrl" name="fotoUrl" class="form-control"
                               placeholder="https://...">
                    </div>

                    <div class="d-flex gap-2">
                        <button type="submit" class="btn btn-primary">
                            <span class="nav-icon" aria-hidden="true">save</span>
                            Salvar Vinho
                        </button>
                        <button type="button" class="btn btn-secondary"
                                onclick="document.getElementById('formWine').style.display='none'; document.getElementById('btnAddWine').style.display='inline-flex';">
                            Cancelar
                        </button>
                    </div>
                </form>
            </div>
        </section>

        <%-- Catalog Wine Grid --%>
        <c:choose>
            <c:when test="${empty vinhos}">
                <div class="card">
                    <div class="card-body" style="text-align:center; padding:3rem">
                        <span class="nav-icon" style="font-family:'Material Icons Round';font-size:3rem;font-style:normal;color:var(--color-text-muted);" aria-hidden="true">wine_bar</span>
                        <p class="text-muted mt-2">Nenhum vinho cadastrado ainda.</p>
                        <button class="btn btn-primary mt-2"
                                onclick="document.getElementById('formWine').style.display='block';">
                            Adicionar primeiro vinho
                        </button>
                    </div>
                </div>
            </c:when>
            <c:otherwise>
                <div class="wine-grid">
                    <c:forEach var="v" items="${vinhos}">
                        <article class="wine-card">
                            <div class="wine-card-img" aria-hidden="true">
                                <c:choose>
                                    <c:when test="${not empty v.fotoUrl}">
                                        <img src="<c:out value='${v.fotoUrl}'/>"
                                             alt="Foto de <c:out value='${v.nome}'/>"
                                             style="width:100%;height:12rem;object-fit:cover;">
                                    </c:when>
                                    <c:otherwise>🍷</c:otherwise>
                                </c:choose>
                            </div>
                            <div class="wine-card-body">
                                <div class="wine-card-name"><c:out value="${v.nome}" /></div>
                                <div class="wine-card-meta">
                                    <c:out value="${v.tipo}" />
                                    <c:if test="${not empty v.pais}"> · <c:out value="${v.pais}" /></c:if>
                                    <c:if test="${not empty v.safra}"> · <c:out value="${v.safra}" /></c:if>
                                </div>
                                <div class="d-flex align-center justify-between">
                                    <span class="text-accent fw-700">R$ <c:out value="${v.preco}" /></span>
                                    <c:choose>
                                        <c:when test="${v.estoque <= v.estoqueMinimo}">
                                            <span class="badge badge-danger">estoque baixo</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="badge badge-success">em estoque</span>
                                        </c:otherwise>
                                    </c:choose>
                                </div>
                            </div>
                            <div class="wine-card-footer">
                                <span class="text-muted" style="font-size:.75rem;">
                                    Estoque: <c:out value="${v.estoque}" />
                                </span>
                                <div class="d-flex gap-1">
                                    <%-- Edit: uses data attributes to avoid unsafe inline JS arguments --%>
                                    <button class="btn btn-secondary btn-sm btn-icon edit-wine-btn"
                                            data-id="${v.id}"
                                            data-nome="<c:out value='${v.nome}'/>"
                                            data-preco="<c:out value='${v.preco}'/>"
                                            data-tipo="${v.tipo}"
                                            data-estoque="${v.estoque}"
                                            data-estoqueMinimo="${v.estoqueMinimo}"
                                            data-pais="<c:out value='${v.pais}'/>"
                                            data-safra="<c:out value='${v.safra}'/>"
                                            data-descricao="<c:out value='${v.descricao}'/>"
                                            data-foto="<c:out value='${v.fotoUrl}'/>"
                                            title="Editar <c:out value='${v.nome}'/>">
                                        <span class="nav-icon" aria-hidden="true">edit</span>
                                    </button>
                                    <%-- Deactivate (soft delete) --%>
                                    <form method="post"
                                          action="${pageContext.request.contextPath}/admin/catalogo"
                                          style="display:inline"
                                          onsubmit="return confirm('Desativar este vinho?')">
                                        <input type="hidden" name="action" value="delete">
                                        <input type="hidden" name="id" value="${v.id}">
                                        <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
                                        <button type="submit" class="btn btn-danger btn-sm btn-icon"
                                                title="Desativar <c:out value='${v.nome}'/>">
                                            <span class="nav-icon" aria-hidden="true">delete</span>
                                        </button>
                                    </form>
                                </div>
                            </div>
                        </article>
                    </c:forEach>
                </div>
            </c:otherwise>
        </c:choose>

<script>
document.addEventListener('DOMContentLoaded', () => {
    // Edit helper using data attributes
    document.querySelectorAll('.edit-wine-btn').forEach(btn => {
        btn.addEventListener('click', () => {
            const d = btn.dataset;
            document.getElementById('formAction').value = 'update';
            document.getElementById('formId').value = d.id;
            document.getElementById('nome').value = d.nome || '';
            document.getElementById('preco').value = d.preco || '';
            document.getElementById('tipo').value = d.tipo || '';
            document.getElementById('estoque').value = d.estoque || '0';
            document.getElementById('estoqueMinimo').value = d.estoqueMinimo || '0';
            document.getElementById('pais').value = d.pais || '';
            document.getElementById('safra').value = d.safra || '';
            document.getElementById('descricao').value = d.descricao || '';
            document.getElementById('fotoUrl').value = d.foto || '';

            const formPanel = document.getElementById('formWine');
            formPanel.style.display = 'block';
            formPanel.querySelector('.card-title').innerHTML = '<span class="nav-icon">edit</span> Editar Vinho';
            
            document.getElementById('btnAddWine').style.display = 'none';
            formPanel.scrollIntoView({ behavior: 'smooth' });
        });
    });
});

function resetWineForm() {
    document.getElementById('wineForm').reset();
    document.getElementById('formAction').value = 'create';
    document.getElementById('formId').value = '';
    document.getElementById('formWine').style.display = 'none';
    document.getElementById('btnAddWine').style.display = 'inline-flex';
    document.getElementById('formWine').querySelector('.card-title').innerHTML = '<span class="nav-icon">wine_bar</span> Cadastrar Novo Vinho';
}
</script>

<jsp:include page="layout/footer.jsp" />
