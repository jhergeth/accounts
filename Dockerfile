FROM openjdk:17-alpine
WORKDIR /home/app
COPY layers/libs /home/app/libs
COPY layers/resources /home/app/resources
COPY layers/resources/config /home/app/config
COPY layers/application.jar /home/app/application.jar
EXPOSE 80
ENTRYPOINT ["java", "-jar", "/home/app/application.jar"]
