# Étape 1 : Build de l'application avec Maven
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# Copier pom.xml et télécharger les dépendances
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copier le code source et builder le jar
COPY src ./src
RUN mvn clean package -DskipTests

# Étape 2 : Image finale minimaliste avec JRE uniquement
FROM eclipse-temurin:21-jre
WORKDIR /app

# On copie explicitement le JAR produit depuis l'étape précédente
COPY --from=build /app/target/*.jar /app/app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
