<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%
    /* Helper: resolve the context path for static assets */
    String ctx = request.getContextPath();
    String currentPage = (String) request.getAttribute("currentPage");
    if (currentPage == null) currentPage = "";

    br.com.vinheiro.model.UsuarioAdmin admin =
        (br.com.vinheiro.model.UsuarioAdmin) session.getAttribute("usuarioAdmin");
    String adminNome = (admin != null && admin.getNome() != null) ? admin.getNome() : "Admin";
    String adminInitial = adminNome.substring(0, 1).toUpperCase();
%>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="robots" content="noindex, nofollow">
    <title>${pageTitle != null ? pageTitle : 'Painel Admin'} — Vinheria Agnello</title>
    <!-- Google Fonts -->
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap" rel="stylesheet">
    <!-- Material Icons -->
    <link href="https://fonts.googleapis.com/icon?family=Material+Icons+Round" rel="stylesheet">
    <!-- Admin CSS (Vanilla CSS — No frameworks) -->
    <link rel="stylesheet" href="<%= ctx %>/static/css/admin.css">
</head>
<body>
<div class="admin-shell">

    <!-- ===== SIDEBAR ===== -->
    <aside class="admin-sidebar" id="adminSidebar" role="navigation" aria-label="Menu de Navegação Admin">
        <div class="sidebar-logo">
            <div class="sidebar-logo-icon" aria-hidden="true">🍷</div>
            <div>
                <div class="sidebar-logo-text">Vinheria Agnello</div>
                <span class="sidebar-logo-sub">Digital Cellar</span>
            </div>
        </div>

        <nav class="sidebar-nav">
            <span class="sidebar-section-label">Principal</span>

            <a href="<%= ctx %>/admin/dashboard"
               class="nav-item <%= "dashboard".equals(currentPage) ? "active" : "" %>"
               aria-current="<%= "dashboard".equals(currentPage) ? "page" : "false" %>">
                <span class="nav-icon" aria-hidden="true">dashboard</span>
                Dashboard
            </a>

            <a href="<%= ctx %>/admin/catalogo"
               class="nav-item <%= "catalogo".equals(currentPage) ? "active" : "" %>"
               aria-current="<%= "catalogo".equals(currentPage) ? "page" : "false" %>">
                <span class="nav-icon" aria-hidden="true">wine_bar</span>
                Catálogo
            </a>

            <a href="<%= ctx %>/admin/pedidos"
               class="nav-item <%= "pedidos".equals(currentPage) ? "active" : "" %>"
               aria-current="<%= "pedidos".equals(currentPage) ? "page" : "false" %>">
                <span class="nav-icon" aria-hidden="true">shopping_cart</span>
                Pedidos
            </a>

            <span class="sidebar-section-label">Ferramentas</span>

            <a href="<%= ctx %>/admin/relatorios"
               class="nav-item <%= "relatorios".equals(currentPage) ? "active" : "" %>"
               aria-current="<%= "relatorios".equals(currentPage) ? "page" : "false" %>">
                <span class="nav-icon" aria-hidden="true">analytics</span>
                Relatórios
            </a>

            <a href="<%= ctx %>/admin/qrcode"
               class="nav-item <%= "qrcode".equals(currentPage) ? "active" : "" %>"
               aria-current="<%= "qrcode".equals(currentPage) ? "page" : "false" %>">
                <span class="nav-icon" aria-hidden="true">qr_code_2</span>
                QR Code
            </a>
        </nav>

        <div class="sidebar-footer">
            <div class="admin-profile">
                <div class="admin-avatar" aria-hidden="true"><%= adminInitial %></div>
                <div class="admin-profile-info">
                    <div class="admin-name"><%= adminNome %></div>
                    <div class="admin-role">Administrador</div>
                </div>
            </div>
        </div>
    </aside>

    <!-- ===== HEADER ===== -->
    <header class="admin-header" role="banner">
        <div>
            <div class="header-title">${pageTitle != null ? pageTitle : 'Painel Admin'}</div>
            <div class="header-subtitle">${pageSubtitle != null ? pageSubtitle : 'Vinheria Agnello'}</div>
        </div>
        <div class="header-actions">
            <a href="<%= ctx %>/auth/logout" class="btn btn-secondary btn-sm" title="Sair">
                <span class="nav-icon" aria-hidden="true">logout</span>
                Sair
            </a>
        </div>
    </header>
