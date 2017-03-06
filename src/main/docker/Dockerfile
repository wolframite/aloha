FROM openjdk:8
VOLUME /tmp
ADD aloha-*.jar /aloha.jar
RUN sh -c 'touch /aloha.jar'
ENV JAVA_OPTS="-server -Xms1g -Xmx1g -Dinfinispan.cluster.jgroups.config=/default-configs/default-jgroups-tcp.xml -Djgroups.tcp.address=SITE_LOCAL -Djava.net.preferIPv4Stack=true"
EXPOSE 11211 7800
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /aloha.jar", "--spring.profiles.active=cluster" ]
