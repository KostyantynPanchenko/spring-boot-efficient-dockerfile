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
