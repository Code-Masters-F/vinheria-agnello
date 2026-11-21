// =====================================================================
// Pipeline de CI/CD — microsserviços Identidade e Catálogo (Vinheria Agnello)
// =====================================================================
// Build -> Testes -> Segurança (DevSecOps) -> Publicação (pasta de produção) -> Verificação pós-deploy
//
// A "Publicação" grava cada versão em /publicacao/<servico>/releases/<build> e troca o link "atual";
// os servidores de aplicação (infra/runtime/executar.sh) reciclam o processo para a nova versão,
// como o IIS faz ao detectar um novo deploy. O Jenkins não tem acesso ao Docker do host.

pipeline {
    agent any

    options {
        timestamps()
        disableConcurrentBuilds()
        buildDiscarder(logRotator(numToKeepStr: '20'))
        timeout(time: 30, unit: 'MINUTES')
    }

    // Repositório verificado a cada ~2 min (Jenkins local, sem webhook público)
    triggers {
        pollSCM('H/2 * * * *')
    }

    environment {
        DOTNET_CLI_TELEMETRY_OPTOUT = '1'
        DOTNET_NOLOGO = '1'
        PUBLICACAO = '/publicacao'
        CA_CERT = '/run/secrets/tls/ca.crt'
    }

    stages {
        stage('Build') {
            steps {
                sh 'echo "Deploy da Vinheria"'
                sh 'dotnet --version'
                sh 'dotnet build identidade-api/IdentidadeApi.Tests/IdentidadeApi.Tests.csproj -c Release'
                sh 'dotnet build csharp-api/VinheriaApi.Tests/VinheriaApi.Tests.csproj -c Release'
            }
        }

        stage('Testes') {
            steps {
                sh 'dotnet test identidade-api/IdentidadeApi.Tests/IdentidadeApi.Tests.csproj -c Release --no-build --logger "console;verbosity=normal"'
                sh 'dotnet test csharp-api/VinheriaApi.Tests/VinheriaApi.Tests.csproj -c Release --no-build --logger "console;verbosity=normal"'
            }
        }

        stage('Segurança') {
            steps {
                // Pacotes NuGet com vulnerabilidades conhecidas bloqueiam o deploy.
                // Os testes de configuração (etapa anterior) já impedem senhas e chaves em arquivos versionados.
                sh 'bash infra/scripts/verificar-vulnerabilidades.sh'
            }
        }

        stage('Publicação') {
            steps {
                sh "bash infra/scripts/publicar.sh identidade ${env.BUILD_NUMBER}"
                sh "bash infra/scripts/publicar.sh catalogo ${env.BUILD_NUMBER}"
                sh 'ls -l /publicacao/identidade /publicacao/catalogo'
            }
        }

        stage('Verificação pós-deploy') {
            steps {
                sh "bash infra/scripts/verificar-deploy.sh ${env.BUILD_NUMBER}"
            }
        }
    }

    post {
        success {
            echo "Versão ${env.BUILD_NUMBER} publicada e em execução."
        }
        failure {
            // Falhas em Build, Testes ou Segurança impedem a publicação: produção continua na versão anterior.
            echo 'Pipeline falhou. Se a falha ocorreu antes da Publicação, a versão em produção não foi alterada.'
        }
        always {
            cleanWs(deleteDirs: true, notFailBuild: true)
        }
    }
}
