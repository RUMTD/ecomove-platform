# ═══════════════════════════════════════════════════════════════════════════
# EcoMove — Dockerfile Multi-Stage
#
# USAGE LOCAL (compile depuis le code source) :
#   docker build -t ecomove:local .
#   docker run -p 8080:8080 ecomove:local
#
# USAGE CI/CD (le JAR est déjà compilé par Maven dans le pipeline) :
#   Le pipeline copie le JAR dans target/ avant de builder
#   Le Stage 1 est bypassé car le JAR existe déjà
# ═══════════════════════════════════════════════════════════════════════════

# ─── STAGE 1 : Compilation Maven (utilisé seulement en local) ─────────────
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /app

# Copie pom.xml en premier → cache des dépendances Maven
COPY pom.xml .
RUN mvn dependency:go-offline -q

# Copie le code source et compile
COPY src ./src
RUN mvn package -DskipTests -q
# Résultat : /app/target/ecomove-platform-1.0.0.jar

# ─── STAGE 2 : Image légère de production ─────────────────────────────────
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# En CI/CD : le JAR vient de target/ (copié par le pipeline depuis l'artifact)
# En local  : le JAR vient du Stage 1 (builder)
COPY --from=builder /app/target/ecomove-platform-*.jar app.jar

EXPOSE 8080

# Variables d'environnement par défaut (surchargées par docker-compose / CI)
ENV DB_USER=ecomove \
    DB_PASSWORD=ecomove \
    SPRING_PROFILES_ACTIVE=prod

# Démarrage avec exec form (pour que SIGTERM soit transmis à la JVM)
ENTRYPOINT ["java", "-jar", "app.jar"]
