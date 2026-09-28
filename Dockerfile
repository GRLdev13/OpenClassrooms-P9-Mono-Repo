FROM node:20 AS front-build
COPY ./front /src
WORKDIR /src

RUN npm i

COPY front/ ./
RUN npm run build -- --optimization

FROM gradle:jdk17 AS back-build

COPY ./back /src

WORKDIR /src

RUN sed -i 's/\r$//' gradlew \
    && chmod +x gradlew \
    && ./gradlew --no-daemon clean build

FROM alpine:3.19 AS front

COPY --from=front-build /src/dist/microcrm/browser /app/front
COPY misc/docker/Caddyfile /app/Caddyfile

RUN apk add --no-cache caddy

WORKDIR /app

EXPOSE 443

CMD ["/usr/sbin/caddy", "run"]

FROM alpine:3.19 AS back

COPY --from=back-build /src/build/libs/microcrm-0.0.1-SNAPSHOT.jar /app/back/microcrm-0.0.1-SNAPSHOT.jar

RUN apk add --no-cache openjdk21-jre-headless

WORKDIR /app

EXPOSE 8081

CMD ["java", "-jar", "/app/back/microcrm-0.0.1-SNAPSHOT.jar"]