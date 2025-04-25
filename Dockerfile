FROM openjdk:17-jdk

WORKDIR /app

COPY target/*.jar app.jar

RUN mkdir -p /app/models
RUN mkdir -p /app/data
RUN mkdir -p /app/templates
RUN mkdir -p /app/static

COPY src/main/resources/models/ /app/models/
COPY src/main/resources/data/ /app/data/
COPY src/main/resources/templates/ /app/templates/
COPY src/main/resources/static/ /app/static/

RUN echo "spring.thymeleaf.prefix=file:/app/templates/" > /app/application.properties
RUN echo "spring.thymeleaf.suffix=.html" >> /app/application.properties
RUN echo "spring.thymeleaf.cache=false" >> /app/application.properties
RUN echo "model.path=/app/models/arabic.model" >> /app/application.properties
RUN echo "arff.header=/app/data/features.arff" >> /app/application.properties

ENV LOGGING_LEVEL_ROOT=INFO
ENV LOGGING_LEVEL_COM_ARABICTEXTCOMPLEXITY=DEBUG

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar", "--spring.config.location=file:/app/application.properties"]