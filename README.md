# Spring Boot efficient Dockerfile example

## Build the Docker image and run it

* `docker build --tag example/spring-boot-efficient-dockerfile:0.1.0 .`
* `docker run -p 8082:8080 --name efficient-dockerfile example/spring-boot-efficient-dockerfile:0.1.0`
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
RUN java -Djarmode=layertools -jar build/libs/*.jar extract --destination build/libs/dependency

FROM eclipse-temurin:17-jre-alpine@sha256:ddcde24217dc1a9df56c7dd206ee1f4dc89f6988c9364968cd02c6cbeb21b1de
VOLUME /tmp

RUN addgroup --system dockerfilegroup && adduser --system dockerfile --ingroup dockerfilegroup
USER dockerfile:dockerfilegroup

ARG DEPENDENCY=/app/build/libs/dependency
COPY --from=theBuildStage ${DEPENDENCY}/dependencies/ ./
COPY --from=theBuildStage ${DEPENDENCY}/spring-boot-loader/ ./
COPY --from=theBuildStage ${DEPENDENCY}/snapshot-dependencies/ ./
COPY --from=theBuildStage ${DEPENDENCY}/application/ ./

ENTRYPOINT java org.springframework.boot.loader.JarLauncher
```

Then you can simply run:
* `docker build --tag example/spring-boot-efficient-dockerfile:0.1.0 .`
* `docker run -p 8082:8080 --name efficient-dockerfile example/spring-boot-efficient-dockerfile:0.1.0`
* `curl localhost:8082/hello?name=Adam`

## Run in local Minikube

* Open the terminal and run `minikube start`
* Pull the image into the Minikube cluster `minikube image load example/spring-boot-efficient-dockerfile:0.1.0`
* Navigate to `deploy` folder `cd deploy`
* Run `kubectl apply -f efficient-dockerfile-deployment.yaml`
* Verify the service is up and running `kubectl get all`
* Forward the port from the
  cluster `kubectl port-forward deployment/efficient-dockerfile-deployment 8080:8080`
* Issue `GET` request `http://localhost:8080/hello?name=Stan`

## Production Considerations for Spring on Kubernetes

* 1000 millicores - 1 vCPU
* CPU request - the <b><i>minimum</i></b> guaranteed amount of CPU; they relate with resource allocation
* CPU limits - the <b><i>maximum</i></b> amount of CPU that your workload can utilize before being throttled; they can impact performance

So how should you set those values?

|  All Pods have  |                        CPU Limits                        |                              No CPU Limits                              |
|:---------------:|:--------------------------------------------------------:|:-----------------------------------------------------------------------:|
|  CPU requests   | You are guaranteed CPU between the request and the limit | You are guaranteed your request. Excess CPU is available and not wasted |
| No CPU requests |      You are guaranteed the limit, no more, no less      |                 No one is guaranteed any CPU! Wild west                 |

* Always set CPU requests, never set CPU limits!
* Set `-XX:ActiveProcessorCount` <b><i>explicitly</i></b>!
* Always set memory requests == limits

### Graceful Shutdown Flow
* `preStop` is being executed (in this example we are waiting up to 10 seconds, see deployment file preStop:exec:command)
* Kubernetes sends a `SIGTERM` to a pod
* A pod receives the signal and begins the shutdown process
* Any new requests to the web server are blocked, waiting for in-flight requests to complete their tasks up to `timeout-per-shutwodn-phase`
* We should have Kubernetes `terminationGracePeriodSeconds` > `preStop` + `timeout-per-shutwodn-phase`
* Be aware that `terminationGracePeriodSeconds` defaults to 30s and `timeout-per-shutwodn-phase` also defaults to 30s!

### Useful Links
* [A Better Dockerfile](https://spring.io/guides/topicals/spring-boot-docker/)
* [Production Considerations for Spring on Kubernetes](https://www.youtube.com/watch?v=hAHXp_jQWVo)
* [Spring Boot Loves K8s](https://www.youtube.com/watch?v=nPACI6-J9Jc)
* [kubectl Cheat Sheet](https://kubernetes.io/docs/reference/kubectl/cheatsheet/)

### Official docs

* [Official Gradle documentation](https://docs.gradle.org)
* [Spring Boot Gradle Plugin Reference Guide](https://docs.spring.io/spring-boot/docs/2.7.6/gradle-plugin/reference/html/)
* [Create an OCI image](https://docs.spring.io/spring-boot/docs/2.7.6/gradle-plugin/reference/html/#build-image)
* [Spring Web](https://docs.spring.io/spring-boot/docs/2.7.6/reference/htmlsingle/#web)
