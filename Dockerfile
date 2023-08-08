FROM adoptopenjdk/maven-openjdk11
COPY target/sellerservice-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8086:8086
ENTRYPOINT ["java","-jar","app.jar"]