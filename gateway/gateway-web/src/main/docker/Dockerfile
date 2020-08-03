FROM java:alpine
VOLUME /tmp
ADD gateway-web-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java","-Xmx1024m","-Xms1024m","-Xmn256m","-XX:SurvivorRatio=6","-XX:+PrintGCTimeStamps","-XX:+PrintGCDetails","-Xloggc:/data/logs/gc.log","-XX:+UseG1GC","-XX:+DisableExplicitGC","-jar","/app.jar"]
