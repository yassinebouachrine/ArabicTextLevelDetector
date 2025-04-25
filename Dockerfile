FROM openjdk:17-jdk

WORKDIR /app

# Copier le JAR compilé
COPY target/*.jar app.jar

# Créer les répertoires nécessaires
RUN mkdir -p /app/models
RUN mkdir -p /app/data
RUN mkdir -p /app/templates
RUN mkdir -p /app/static

# Copier les ressources
COPY src/main/resources/models/ /app/models/
COPY src/main/resources/data/ /app/data/
COPY src/main/resources/templates/ /app/templates/
COPY src/main/resources/static/ /app/static/

# Créer un fichier application.properties modifié avec les chemins absolus
RUN echo "spring.thymeleaf.prefix=file:/app/templates/" > /app/application.properties
RUN echo "spring.thymeleaf.suffix=.html" >> /app/application.properties
RUN echo "spring.thymeleaf.cache=false" >> /app/application.properties
RUN echo "model.path=/app/models/arabic.model" >> /app/application.properties
RUN echo "arff.header=/app/data/features.arff" >> /app/application.properties

# Définir des variables d'environnement pour la journalisation détaillée
ENV LOGGING_LEVEL_ROOT=INFO
ENV LOGGING_LEVEL_COM_ARABICTEXTCOMPLEXITY=DEBUG

# Exposer le port
EXPOSE 8080

# Démarrer l'application avec le fichier de configuration personnalisé
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.config.location=file:/app/application.properties"]