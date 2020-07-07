FROM java:alpine
VOLUME /tmp
ADD bus-server-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java","-Xmx512m","-Xms512m","-Xmn256m","-XX:SurvivorRatio=6","-XX:+PrintGCTimeStamps","-XX:+PrintGCDetails","-Xloggc:/data/logs/gc.log","-XX:+UseG1GC","-XX:+DisableExplicitGC","-jar","/app.jar"]
