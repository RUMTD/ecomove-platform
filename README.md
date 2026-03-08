# EcoMove Platform

Plateforme B2B de covoiturage domicile-travail.

## Stack technique
- Java 17 + Spring Boot 3.2
- PostgreSQL (prod) / H2 (tests)
- JUnit 5 + Mockito
- JaCoCo (couverture)
- SonarQube (qualite)
- GitHub Actions (CI/CD)

## Lancer les tests
```bash
mvn clean verify -Dspring.profiles.active=test
```

## Lancer l application
```bash
mvn spring-boot:run
```

## Pipeline CI/CD
Voir `.github/workflows/ci-cd.yml`
Etapes : Tests → SonarQube → Build → Deploy Staging

## Quality Gate
- Couverture minimum : 80%
- Bugs critiques : 0
- Vulnerabilites : 0
- Duplication : < 3%
