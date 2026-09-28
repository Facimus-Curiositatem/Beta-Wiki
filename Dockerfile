# syntax=docker/dockerfile:1

FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build

# La wiki empaqueta los .md de ArchProposalDaza/docs (ver <resources> en beta/pom.xml)
COPY beta/ ./beta/
COPY ArchProposalDaza/docs/ ./ArchProposalDaza/docs/

WORKDIR /build/beta
RUN mvn -DskipTests package && \
    cp target/*.jar /build/app.jar

FROM eclipse-temurin:21-jre

RUN useradd -r -u 1001 appuser
USER appuser

WORKDIR /app

COPY --from=build --chown=appuser:appuser /build/app.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]