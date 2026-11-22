# Jenkins da Vinheria: LTS + .NET 8 SDK + plugins + configuração como código (JCasC)
FROM jenkins/jenkins:lts-jdk21

USER root

# .NET 8 SDK para build, testes e publicação dos microsserviços
RUN apt-get update \
 && apt-get install -y --no-install-recommends ca-certificates curl libicu-dev \
 && curl -fsSL https://dot.net/v1/dotnet-install.sh -o /tmp/dotnet-install.sh \
 && bash /tmp/dotnet-install.sh --channel 8.0 --install-dir /usr/share/dotnet \
 && ln -s /usr/share/dotnet/dotnet /usr/local/bin/dotnet \
 && rm -rf /var/lib/apt/lists/* /tmp/dotnet-install.sh \
 # Pasta de publicação (montada do host); criada aqui para pertencer ao usuário jenkins
 && mkdir -p /publicacao && chown jenkins:jenkins /publicacao

ENV DOTNET_ROOT=/usr/share/dotnet \
    DOTNET_CLI_TELEMETRY_OPTOUT=1 \
    DOTNET_NOLOGO=1

USER jenkins

COPY plugins.txt /usr/share/jenkins/ref/plugins.txt
RUN jenkins-plugin-cli --plugin-file /usr/share/jenkins/ref/plugins.txt

COPY casc.yaml /usr/share/jenkins/ref/casc.yaml
# Sem assistente de instalação: usuário, segurança e job vêm do casc.yaml
ENV CASC_JENKINS_CONFIG=/usr/share/jenkins/ref/casc.yaml \
    JAVA_OPTS="-Djenkins.install.runSetupWizard=false"
