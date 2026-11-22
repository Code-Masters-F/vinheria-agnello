#!/bin/sh
# Executado pelo container do PostgreSQL apenas na primeira inicialização (volume vazio).
#
# Database per Service + menor privilégio: cada microsserviço tem o próprio banco e o próprio
# usuário, dono apenas do seu banco. O acesso público (CONNECT) é revogado, então o usuário da
# Identidade não consegue nem se conectar ao banco do Catálogo, e vice-versa.
set -eu

criar_banco_do_servico() {
    servico="$1"
    senha="$2"

    # A senha vai como variável do psql (:'senha'), que faz o escape correto do valor.
    psql -v ON_ERROR_STOP=1 -U "$POSTGRES_USER" -d postgres -v senha="$senha" <<SQL
CREATE ROLE ${servico}_app LOGIN PASSWORD :'senha';
CREATE DATABASE ${servico}_db OWNER ${servico}_app;
REVOKE ALL ON DATABASE ${servico}_db FROM PUBLIC;
SQL

    # Tabelas e dados criados pelo próprio usuário do serviço (ele é o dono dos objetos).
    psql -v ON_ERROR_STOP=1 -U "${servico}_app" -d "${servico}_db" -f "/sql/${servico}.sql"
    echo "Banco ${servico}_db criado (usuário ${servico}_app)"
}

criar_banco_do_servico identidade "$IDENTIDADE_DB_PASSWORD"
criar_banco_do_servico catalogo "$CATALOGO_DB_PASSWORD"
