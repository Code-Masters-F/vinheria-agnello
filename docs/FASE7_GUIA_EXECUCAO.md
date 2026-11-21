# 🧭 Guia de execução — Fase 7 (microsserviços, DHCP/DNS, HTTPS e Jenkins)

Passo a passo para rodar, do zero, tudo o que foi feito na Fase 7: os microsserviços **Identidade** e **Catálogo**,
a rede com **DHCP e DNS**, o **HTTPS**, o **Jenkins** com deploy automático e a **demonstração no Wireshark**.
Visão técnica, diagramas e decisões: [FASE7_INFRA_DEVOPS.md](FASE7_INFRA_DEVOPS.md).

> Todos os comandos são executados **na raiz do repositório**, em um terminal **bash**
> (Linux, macOS ou, no Windows, o terminal do **WSL 2**).

---

## 📋 Índice

1. [Pré-requisitos](#1-pré-requisitos)
2. [Obter o código](#2-obter-o-código)
3. [Configurar o ambiente (uma única vez)](#3-configurar-o-ambiente-uma-única-vez)
4. [Subir o ambiente](#4-subir-o-ambiente)
5. [Verificar se está tudo funcionando](#5-verificar-se-está-tudo-funcionando)
6. [Usar os microsserviços](#6-usar-os-microsserviços)
7. [Jenkins: pipeline e deploy](#7-jenkins-pipeline-e-deploy)
8. [Demonstração: sem criptografia × com criptografia](#8-demonstração-sem-criptografia--com-criptografia)
9. [Rodar os testes automatizados](#9-rodar-os-testes-automatizados)
10. [Parar, reiniciar, atualizar e reiniciar do zero](#10-parar-reiniciar-atualizar-e-reiniciar-do-zero)
11. [Solução de problemas](#11-solução-de-problemas)

---

## 1. Pré-requisitos

| Ferramenta | Para quê | Como conferir |
|---|---|---|
| **Docker** com **Compose v2** | Rodar todos os componentes | `docker compose version` |
| **git**, **bash**, **openssl**, **curl** | Clonar, scripts, certificados e testes | `git --version`, `openssl version`, `curl --version` |
| Wireshark *(opcional)* | Demonstração de tráfego | — |
| .NET 8 SDK *(opcional)* | **Não é necessário**: o deploy inicial e os testes usam o container oficial do SDK quando ele não está instalado | `dotnet --list-sdks` |

Recursos recomendados: 4 GB de RAM livres para o Docker e ~5 GB de disco. Portas usadas no host (somente `127.0.0.1`):
**8441, 8442** (HTTPS), **8081, 8082** (HTTP, só no modo de demonstração) e **8090** (Jenkins).

<details>
<summary><b>Linux (Fedora / Ubuntu)</b></summary>

```bash
# Fedora
sudo dnf install -y moby-engine docker-compose git openssl curl
# Ubuntu 24.04+ (ou siga https://docs.docker.com/engine/install/ubuntu/)
sudo apt install -y docker.io docker-compose-v2 git openssl curl

sudo systemctl enable --now docker        # inicia o Docker
sudo usermod -aG docker $USER             # usar docker sem sudo (faça logout/login depois)
```

No Fedora (SELinux ativo) nada extra é necessário: os volumes do compose já usam o sufixo `:z`.
</details>

<details>
<summary><b>Windows</b></summary>

1. Instale o **Docker Desktop** com o backend **WSL 2** e uma distribuição Linux (ex.: `wsl --install -d Ubuntu`).
2. No Docker Desktop: *Settings → Resources → WSL integration* → habilite a distribuição.
3. Abra o terminal **Ubuntu (WSL)** e instale as ferramentas: `sudo apt install -y git openssl curl`.
4. **Clone o repositório dentro do WSL** (ex.: `~/vinheria-agnello`), **não** em `C:\` / `/mnt/c`: a pasta de
   publicação usa links simbólicos, que não funcionam bem em pastas do Windows montadas no Docker.
</details>

<details>
<summary><b>macOS</b></summary>

Instale o **Docker Desktop** e, pelo [Homebrew](https://brew.sh), o git e o OpenSSL — o `openssl` que vem no macOS
é o LibreSSL, que pode não aceitar as opções usadas na geração dos certificados:

```bash
brew install git openssl
export PATH="$(brew --prefix openssl)/bin:$PATH"   # use este openssl ao rodar o configurar-ambiente.sh
```
</details>

---

## 2. Obter o código

```bash
git clone https://github.com/Code-Masters-F/vinheria-agnello.git
cd vinheria-agnello
git checkout fase7-infra-devops      # enquanto a Fase 7 não estiver na main
```

---

## 3. Configurar o ambiente (uma única vez)

```bash
./infra/scripts/configurar-ambiente.sh
```

O script só cria o que estiver faltando (pode ser executado de novo sem problemas) e **nada disso vai para o git**:

| Criado | Onde | Uso |
|---|---|---|
| Chave RSA de assinatura dos tokens | `infra/secrets/jwt-privada.pem` | Identidade assina os JWT |
| CA interna + certificado HTTPS de cada serviço | `infra/secrets/tls/` | HTTPS entre os serviços e com o host |
| Senhas aleatórias do PostgreSQL e do admin do Jenkins | `.env` | `POSTGRES_PASSWORD`, `IDENTIDADE_DB_PASSWORD`, `CATALOGO_DB_PASSWORD`, `JENKINS_ADMIN_PASSWORD` |
| Deploy inicial (versão 0) dos dois serviços | `publicacao/` | Os serviços sobem antes do primeiro build do Jenkins |

**Antes do merge na `main`**, aponte o Jenkins para a branch da Fase 7:

```bash
echo 'VINHERIA_BRANCH=*/fase7-infra-devops' >> .env
```

---

## 4. Subir o ambiente

```bash
docker compose up -d --build
docker compose ps
```

Na **primeira vez**, o Docker baixa as imagens e monta a do Jenkins (com .NET 8 SDK e plugins): leva de **5 a 15
minutos**, dependendo da internet. Depois disso, sobe em segundos. O Jenkins leva mais 1 a 2 minutos para iniciar.

Resultado esperado em `docker compose ps`: `vinheria-dns`, `vinheria-db` (healthy), `vinheria-identidade-rede`,
`vinheria-identidade` (healthy), `vinheria-catalogo-rede`, `vinheria-catalogo` (healthy) e `vinheria-jenkins`.

---

## 5. Verificar se está tudo funcionando

```bash
./infra/scripts/verificar-rede.sh
```

O script mostra, em uma tela (ótimo para capturas de tela):

| Seção | O que conferir |
|---|---|
| Modo do ambiente | `HTTPS (seguro)` |
| Concessões DHCP / Troca DHCP | `identidade` e `catalogo` com IPs `172.30.0.2xx`; `DHCPDISCOVER → OFFER → REQUEST → ACK` |
| Resolução DNS | `identidade`, `catalogo`, `db`, `dns` e `jenkins` `.vinheria.local` resolvidos |
| Certificado | emitido por `Vinheria Agnello CA Interna`, `TLSv1.3`, `Verify return code: 0 (ok)` |
| HTTP desativado | `000` (conexão recusada) |
| Fluxo de autenticação | `GET` sem token **200**, `POST` sem token **401**, `POST` com token de Admin **201**, `DELETE` **204** |

---

## 6. Usar os microsserviços

Os certificados são da CA interna, então use `--cacert` no `curl` (ou importe `infra/secrets/tls/ca.crt` no
navegador como autoridade confiável: Firefox → *Configurações → Certificados → Importar*; Windows → `certmgr.msc` →
*Autoridades de Certificação Raiz Confiáveis*; macOS → *Acesso às Chaves → Sistema*).

**Usuários de demonstração:** `admin@agnello.com` / `admin123` (Admin) · `cliente@agnello.com` / `cliente123` (Cliente).

```bash
CA=infra/secrets/tls/ca.crt
ID=https://localhost:8441/api/v1/identidade
CAT=https://localhost:8442/api/v1/catalogo/produtos

# Catálogo é público para leitura
curl --cacert $CA $CAT

# Login (o refresh token fica no arquivo de cookies; o JWT vem no corpo)
TOKEN=$(curl -s --cacert $CA -c cookies.txt -H 'Content-Type: application/json' \
  -d '{"email":"admin@agnello.com","senha":"admin123"}' $ID/login | sed -E 's/.*"accessToken":"([^"]+)".*/\1/')

# Cadastrar um vinho (somente Admin; a vinheria vem do token)
curl --cacert $CA -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' \
  -d '{"nome":"Malbec Reserva","tipo":"Tinto","preco":89.9,"estoque":10}' $CAT

# Renovar a sessão pelo cookie (gera novo JWT e troca o refresh token) e sair
curl --cacert $CA -b cookies.txt -c cookies.txt -X POST $ID/refresh
curl --cacert $CA -b cookies.txt -c cookies.txt -X POST $ID/logout
rm cookies.txt
```

| Teste | Resultado esperado |
|---|---|
| `POST` em `$CAT` sem token | 401 |
| `POST` em `$CAT` com token de `cliente@agnello.com` | 403 |
| `PUT`/`DELETE` em `$CAT/4` com o Admin da Agnello (produto de outra vinheria) | 403 |
| `POST $ID/refresh` depois do logout | 401 |

Saúde e versão em execução: `curl --cacert $CA https://localhost:8441/health` e `.../versao`
(o mesmo para a porta 8442).

---

## 7. Jenkins: pipeline e deploy

1. Acesse **http://localhost:8090** e entre com o usuário **`admin`** e a senha do `.env`:
   ```bash
   grep JENKINS_ADMIN_PASSWORD .env
   ```
2. Abra o job **`vinheria-deploy`** (já criado automaticamente, sem configuração manual) → **Build Now**.
3. Acompanhe em **Stage View**: *Build → Testes → Segurança → Publicação → Verificação pós-deploy*. O console do
   build mostra `Deploy da Vinheria`, os testes aprovados, `Nenhum pacote vulnerável encontrado` e
   `✔ Deploy da versão N verificado`.
4. Confira o deploy:
   ```bash
   ls -l publicacao/*/                                  # atual -> releases/N
   curl --cacert infra/secrets/tls/ca.crt https://localhost:8441/versao
   docker logs vinheria-identidade | grep servidor      # "novo deploy detectado ... reciclando o processo"
   ```

**Deploy automático:** a cada `git push` na branch configurada (`VINHERIA_BRANCH`), o Jenkins detecta a mudança em
até ~2 minutos e roda o pipeline sozinho (causa do build: *Started by an SCM change*).

**Rollback** (as 5 últimas versões ficam guardadas):

```bash
./infra/scripts/rollback.sh identidade        # volta para a versão anterior
./infra/scripts/rollback.sh identidade 6      # ou para uma versão específica
```

**Trocar a branch lida pelo Jenkins:** edite `VINHERIA_BRANCH` no `.env` (ex.: `*/main` ou remova a linha depois do
merge) e aplique com `docker compose up -d jenkins`.

---

## 8. Demonstração: sem criptografia × com criptografia

Roteiro completo, filtros e o que destacar em cada captura: [FASE7_INFRA_DEVOPS.md, seção 7](FASE7_INFRA_DEVOPS.md#7-demonstração-com-wireshark).

```bash
# 1) Modo inseguro (HTTP puro) — somente para a demonstração
docker compose -f docker-compose.yml -f docker-compose.inseguro.yml up -d

# 2) Captura: no Linux, Wireshark direto na interface "br-vinheria".
#    No Windows/macOS (ou se preferir), capture com o script e abra o .pcap no Wireshark:
./infra/scripts/capturar-trafego.sh demo-http        # Enter para parar -> capturas/demo-http.pcap

# 3) Em outro terminal, gere o tráfego (login, chamada autenticada, refresh, logout)
./infra/scripts/gerar-trafego-demo.sh

# 4) Volte ao modo seguro e repita a captura para comparar
docker compose up -d
./infra/scripts/capturar-trafego.sh demo-https
./infra/scripts/gerar-trafego-demo.sh
```

| Modo | O que o Wireshark mostra |
|---|---|
| HTTP | senha, JWT, cookie de sessão, a busca do JWKS entre os serviços e os dados do produto, em texto puro |
| HTTPS | apenas o handshake TLS 1.3 e *Application Data* cifrado (o nome do servidor/SNI continua visível) |

> No Linux, para capturar sem `sudo`: `sudo usermod -aG wireshark $USER` e faça logout/login.
> A pasta `capturas/` não vai para o git (os arquivos contêm senhas e tokens de demonstração).

---

## 9. Rodar os testes automatizados

```bash
./infra/scripts/rodar-testes.sh
```

Executa os 70 testes (28 da Identidade + 42 do Catálogo) — os mesmos do pipeline. Usa o .NET local se ele tiver o
runtime do ASP.NET Core 8; senão, roda no container `mcr.microsoft.com/dotnet/sdk:8.0`.

---

## 10. Parar, reiniciar, atualizar e reiniciar do zero

| Ação | Comando |
|---|---|
| Parar (mantém bancos, Jenkins e versões publicadas) | `docker compose down` |
| Subir de novo | `docker compose up -d` |
| Ver logs de um serviço | `docker logs -f vinheria-identidade` (ou `vinheria-catalogo`, `vinheria-dns`, `vinheria-jenkins`) |
| Atualizar após `git pull` | `docker compose up -d --build` — e, se o `init.sql` da Identidade mudou: `docker exec -i vinheria-db psql -U identidade_app -d identidade_db < identidade-api/database/init.sql` (idempotente) |
| **Reiniciar do zero** (apaga bancos, histórico do Jenkins e versões) | `docker compose down -v && rm -rf publicacao` e depois `./infra/scripts/configurar-ambiente.sh && docker compose up -d --build` |

> Ao reiniciar do zero, **mantenha o `.env`** ou apague também os volumes (`-v`): o banco é criado com as senhas do
> `.env` na primeira subida; trocar as senhas sem recriar o volume impede os serviços de conectar.

---

## 11. Solução de problemas

| Sintoma | Causa provável | Solução |
|---|---|---|
| `docker: unknown command: docker compose` | Plugin Compose ausente | Fedora: `sudo dnf install docker-compose`; Ubuntu: `sudo apt install docker-compose-v2` (ou `docker-compose-plugin` do repositório oficial do Docker) |
| `Cannot connect to the Docker daemon` | Docker parado | `sudo systemctl start docker` (Linux) ou abra o Docker Desktop |
| `permission denied ... docker.sock` | Usuário fora do grupo `docker` | `sudo usermod -aG docker $USER` e logout/login |
| `$'\r': command not found` ao rodar um script | Arquivo com quebra de linha do Windows | Clone dentro do WSL (o `.gitattributes` já força LF); em clones antigos: `git rm -r --cached . -q && git reset --hard` |
| `Pool overlaps with other one on this address space` | A sub-rede `172.30.0.0/24` já é usada na máquina | Troque a sub-rede em `docker-compose.yml`, `infra/dnsmasq/dnsmasq.conf` e nos IPs fixos (`.2`, `.10`, `.20`) |
| `port is already allocated` (8441, 8442, 8081, 8082, 8090) | Porta ocupada por outro programa | Encerre o programa ou altere a porta do host em `docker-compose.yml` |
| Serviço não fica *healthy*; log `aguardando o primeiro deploy` | Pasta `publicacao/` sem versão | `./infra/scripts/configurar-ambiente.sh` (faz o deploy inicial) ou rode o pipeline no Jenkins |
| `curl: (60) SSL certificate problem` | CA interna não informada | Use `curl --cacert infra/secrets/tls/ca.crt ...` |
| Token recusado (401) logo após trocar de modo | O emissor do token muda entre HTTP e HTTPS | Faça login de novo no modo atual |
| Serviço não encontrado pelo nome (`*.vinheria.local`) | Concessão DHCP perdida | `docker compose restart identidade-rede` (ou `catalogo-rede`); o serviço reinicia junto e o DNS é atualizado |
| Jenkins falha no *Checkout* (`couldn't find remote ref`) | `VINHERIA_BRANCH` aponta para uma branch inexistente no GitHub | Corrija no `.env` e rode `docker compose up -d jenkins` |
| Jenkins falha na *Publicação* com `Permission denied` | Pasta `publicacao/` sem permissão para o Jenkins (uid 1000) | `chmod -R a+rwX publicacao` |
| Login responde 500 depois de atualizar o código | Tabela nova ainda não existe no banco | Reaplique o `init.sql` (seção 10, "Atualizar após git pull") |
| Testes falham com `PipeWriter.UnflushedBytes` | Testes rodando em .NET 9/10 | Use `./infra/scripts/rodar-testes.sh` (roda no .NET 8) |
| `br-vinheria` não aparece no Wireshark (Windows/macOS) | A interface fica dentro da VM do Docker Desktop | Use `./infra/scripts/capturar-trafego.sh` e abra o `.pcap` |
