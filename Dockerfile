### build stage
FROM eclipse-temurin:17-jdk AS build
WORKDIR /workspace

# Install Maven because this repository does not include Maven Wrapper files.
RUN apt-get update \
    && apt-get install -y --no-install-recommends maven \
    && rm -rf /var/lib/apt/lists/*

# copy pom first for layer caching
COPY pom.xml pom.xml
RUN mvn -q -B -DskipTests dependency:go-offline

# copy sources and build
COPY src src
RUN mvn -q -B -DskipTests package

### runtime stage
FROM eclipse-temurin:17-jre
WORKDIR /app

# non-root user
RUN useradd -r -u 1001 -g root appuser

COPY --from=build /workspace/target/*.jar /app/app.jar

USER appuser
EXPOSE 8080

ENV JAVA_OPTS=""
ENTRYPOINT ["sh","-c","exec java $JAVA_OPTS -jar /app/app.jar"]
