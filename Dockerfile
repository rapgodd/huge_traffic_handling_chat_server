# Jar 전송
FROM gradle:7.6-jdk17 AS build


WORKDIR /app


COPY . /app


RUN gradle clean build -x test --no-daemon


# jar 받아서 실행
FROM eclipse-temurin:17-jre AS runtime


WORKDIR /app


COPY --from=build /app/build/libs/*.jar /app/app.jar


ENTRYPOINT ["java","-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=*:5005","-jar","/app/app.jar"]