FROM clojure:temurin-21-alpine AS builder
ENV CLOJURE_VERSION=1.11.1

RUN mkdir -p /build
WORKDIR /build

COPY deps.edn build.clj workspace.edn /build/
COPY projects/web-api/deps.edn /build/projects/web-api/deps.edn

RUN clojure -P -X:build :project web-api

COPY components /build/components
COPY bases /build/bases
COPY projects /build/projects

RUN clojure -T:build uberjar :project web-api

FROM eclipse-temurin:21-jre-alpine AS final
RUN mkdir -p /service

WORKDIR /service
COPY --from=builder /build/projects/web-api/target/web-api.jar web-api.jar

ENTRYPOINT ["java", "-jar", "/service/web-api.jar"]
