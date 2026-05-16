### build stage
FROM eclipse-temurin:17-jdk AS build
WORKDIR /workspace

# copy maven wrapper + pom first for layer caching
COPY .mvn .mvn
COPY mvnw mvnw
COPY pom.xml pom.xml
RUN if [ -f mvnw ]; then chmod +x mvnw && ./mvnw -q -B -DskipTests dependency:go-offline; fi

# copy sources and build
COPY src src
RUN if [ -f mvnw ]; then ./mvnw -q -B -DskipTests package; \
    else apt-get update && apt-get install -y --no-install-recommends maven && mvn -q -B -DskipTests package; fi

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
