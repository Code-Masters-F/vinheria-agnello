<%@ page contentType="text/html;charset=UTF-8" language="java" isErrorPage="true" %>
<%
    String ctx = request.getContextPath();
%>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>404 — Página não encontrada | Vinheria Agnello</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;600;700;800&display=swap" rel="stylesheet">
    <link href="https://fonts.googleapis.com/icon?family=Material+Icons+Round" rel="stylesheet">
    <link rel="stylesheet" href="<%= ctx %>/static/css/admin.css">
</head>
<body>
<div class="error-page">
    <div class="error-code">404</div>
    <div class="error-message">Página não encontrada</div>
    <p class="error-detail">
        A página que você está procurando não existe ou foi movida.
        Verifique o endereço e tente novamente.
    </p>
    <a href="<%= ctx %>/admin/dashboard" class="btn btn-primary">
        <span class="nav-icon" aria-hidden="true">dashboard</span>
        Voltar ao Painel
    </a>
</div>
</body>
</html>
