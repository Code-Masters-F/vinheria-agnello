<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%
    /* Helper: resolve the context path for static assets */
    String ctx = request.getContextPath();
    String currentPage = (String) request.getAttribute("currentPage");
    if (currentPage == null) currentPage = "";

    br.com.agnellovinheria.model.UsuarioAdmin admin =
        (br.com.agnellovinheria.model.UsuarioAdmin) session.getAttribute("usuarioAdmin");
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

    <jsp:include page="sidebar.jsp" />

    <!-- ===== HEADER ===== -->
    <header class="admin-header" role="banner">
        <div class="d-flex align-center gap-1">
            <button class="mobile-toggle" onclick="document.body.classList.toggle('sidebar-open')" aria-label="Abrir menu">
                <span class="nav-icon" aria-hidden="true">menu</span>
            </button>
            <div>
                <div class="header-title">${pageTitle != null ? pageTitle : 'Painel Admin'}</div>
                <div class="header-subtitle">${pageSubtitle != null ? pageSubtitle : 'Vinheria Agnello'}</div>
            </div>
        </div>
        <div class="header-actions">
            <a href="<%= ctx %>/auth/logout" class="btn btn-secondary btn-sm" title="Sair">
                <span class="nav-icon" aria-hidden="true">logout</span>
                Sair
            </a>
        </div>
    </header>
