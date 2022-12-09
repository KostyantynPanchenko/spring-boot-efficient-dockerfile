# Spring Boot efficient Dockerfile example

### Steps to reproduce

* `./gradlew clean build`
* `mkdir build/libs/extracted`
* `java -Djarmode=layertools -jar build/libs/*.jar extract --destination build/libs/extracted`
* `docker build --tag example/spring-boot-efficient-dockerfile:latest .`
* `docker run -p 8082:8080 --name efficient-dockerfile example/spring-boot-efficient-dockerfile`
* `curl localhost:8082/hello?name=Adam`

### Notes About the Artifacts and Multi-Stage Builds
The example above be modified to use Docker multi-stage build. Just replace the content of the Dockerfile with the following:

```shell
FROM eclipse-temurin:17-jdk-alpine@sha256:9c379272e10177b992a06692bd07ee457681f5f56c131607a045a269a4ddc36b as theBuildStage

WORKDIR /app

COPY gradlew .
COPY .gradle .gradle
COPY gradle gradle
COPY settings.gradle .
COPY build.gradle .
COPY src src

RUN ./gradlew clean build
RUN mkdir -p build/libs/dependency && (cd build/libs/dependency; jar -xf ../*.jar)

FROM eclipse-temurin:17-jre-alpine@sha256:ddcde24217dc1a9df56c7dd206ee1f4dc89f6988c9364968cd02c6cbeb21b1de
VOLUME /tmp

RUN addgroup --system dockerfilegroup && adduser --system dockerfile --ingroup dockerfilegroup
USER dockerfile:dockerfilegroup

ARG DEPENDENCY=/app/build/libs/dependency
COPY --from=theBuildStage ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY --from=theBuildStage ${DEPENDENCY}/META-INF /app/META-INF
COPY --from=theBuildStage ${DEPENDENCY}/BOOT-INF/classes /app

ENTRYPOINT java -cp app:app/lib/* com.example.dockerfile.SpringBootEfficientDockerfileApplication
```

Then you can simply run:
* `docker build --tag example/spring-boot-efficient-dockerfile:latest .`
* `docker run -p 8082:8080 --name efficient-dockerfile example/spring-boot-efficient-dockerfile`
* `curl localhost:8082/hello?name=Adam`

### Useful Links
* [A Better Dockerfile](https://spring.io/guides/topicals/spring-boot-docker/)
* [Official Gradle documentation](https://docs.gradle.org)
* [Spring Boot Gradle Plugin Reference Guide](https://docs.spring.io/spring-boot/docs/2.7.6/gradle-plugin/reference/html/)
* [Create an OCI image](https://docs.spring.io/spring-boot/docs/2.7.6/gradle-plugin/reference/html/#build-image)
* [Spring Web](https://docs.spring.io/spring-boot/docs/2.7.6/reference/htmlsingle/#web)

