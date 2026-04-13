<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="pt-br">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Login | Vinheria Agnello</title>
    
    <!-- Google Fonts -->
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">
    
    <!-- Admin CSS (Vanilla CSS — No frameworks) -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin/variables.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin/layout.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin/components.css">
</head>
<body class="login-body">

    <div class="login-container">
        <div class="login-brand">
            <h1>Vinheria Agnello</h1>
            <p>Painel Administrativo B2B</p>
        </div>

        <c:if test="${not empty error}">
            <div class="alert alert-danger">
                ${error}
            </div>
        </c:if>

        <form action="${pageContext.request.contextPath}/auth/login" method="POST">
            <div class="form-group">
                <label class="form-label" for="email">E-mail</label>
                <input type="email" id="email" name="email" class="form-control" placeholder="admin@vinheria.com.br" required autofocus>
            </div>

            <div class="form-group">
                <label class="form-label" for="senha">Senha</label>
                <input type="password" id="senha" name="senha" class="form-control" placeholder="••••••••" required>
            </div>

            <button type="submit" class="btn btn-primary" style="width: 100%;">Entrar no Sistema</button>
        </form>

        <div class="login-footer">
            &copy; 2024 Vinheria Agnello. Todos os direitos reservados.
        </div>
    </div>

</body>
</html>
