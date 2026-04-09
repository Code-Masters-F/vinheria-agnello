<%@ page contentType="text/html;charset=UTF-8" language="java" isErrorPage="true" %>
<%
    String ctx = request.getContextPath();
    Throwable cause = (Throwable) request.getAttribute("javax.servlet.error.exception");
    if (cause == null) cause = (Throwable) request.getAttribute("jakarta.servlet.error.exception");
%>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>500 — Erro Interno | Vinheria Agnello</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;600;700;800&display=swap" rel="stylesheet">
    <link href="https://fonts.googleapis.com/icon?family=Material+Icons+Round" rel="stylesheet">
    <link rel="stylesheet" href="<%= ctx %>/static/css/admin.css">
</head>
<body>
<div class="error-page">
    <div class="error-code">500</div>
    <div class="error-message">Erro Interno do Servidor</div>
    <p class="error-detail">
        Ocorreu um erro inesperado. Nossa equipe foi notificada.
        Por favor, tente novamente em alguns instantes.
    </p>
    <a href="<%= ctx %>/admin/dashboard" class="btn btn-primary">
        <span class="nav-icon" aria-hidden="true">dashboard</span>
        Voltar ao Painel
    </a>
</div>
</body>
</html>
