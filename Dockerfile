FROM gradle:6.5.0-jdk11

ENV UAT_PATH=/home/gradle
WORKDIR $UAT_PATH

RUN ln -sf /opt/java/openjdk/bin/java /usr/bin/java

COPY gradle.properties ${UAT_PATH}/
COPY build.gradle ${UAT_PATH}/

RUN gradle wrapper
RUN gradle dependencies --refresh-dependencies

COPY src/ ${UAT_PATH}/src/

RUN ./gradlew :compileJava

COPY start.sh ${UAT_PATH}/start.sh
RUN chmod +x ${UAT_PATH}/start.sh

ENTRYPOINT ["/home/gradle/start.sh"]
