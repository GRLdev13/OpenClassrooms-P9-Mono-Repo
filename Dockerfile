FROM node AS front-build

COPY ./front /src

WORKDIR /src

RUN npm ci \
    && npm run build -- --optimization

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

EXPOSE 80
EXPOSE 443

CMD ["/usr/sbin/caddy", "run"]

FROM alpine:3.19 AS back

COPY --from=back-build /src/build/libs/microcrm-0.0.1-SNAPSHOT.jar /app/back/microcrm-0.0.1-SNAPSHOT.jar

RUN apk add --no-cache openjdk21-jre-headless

WORKDIR /app

EXPOSE 8081

CMD ["java", "-jar", "/app/back/microcrm-0.0.1-SNAPSHOT.jar"]

FROM alpine:3.19 AS standalone

COPY --from=front-build /src/dist/microcrm/browser /app/front
COPY --from=back-build /src/build/libs/microcrm-0.0.1-SNAPSHOT.jar /app/back/microcrm-0.0.1-SNAPSHOT.jar
COPY misc/docker/Caddyfile /app/Caddyfile
COPY misc/docker/supervisor.ini /app/supervisor.ini

RUN apk add --no-cache caddy openjdk21-jre-headless supervisor

WORKDIR /app

EXPOSE 80
EXPOSE 443
EXPOSE 8081

CMD ["/usr/bin/supervisord", "-c", "/app/supervisor.ini"]



