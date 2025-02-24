FROM openjdk:8-jre-slim-buster

ARG SPARK_VERSION=2.4.8
ARG HADOOP_VERSION=2.6
ARG TINI_VERSION="v0.18.0"

ENV DEBIAN_FRONTEND noninteractive

ENV SPARK_HOME /opt/spark
ENV PATH "$PATH:$SPARK_HOME/bin"
ENV LD_LIBRARY_PATH="${LD_LIBRARY_PATH}:/usr/lib/hadoop/lib/native"

ENV HADOOP_CONF_DIR=/etc/hadoop/conf/

# LIGHT DEPENDENCIES START
RUN apt update -qq && apt install -yqq --no-install-recommends \
      ftp wget curl unzip -q telnet openssh-client krb5-user zip && \
    rm -rf /var/lib/apt/lists/*
# LIGHT DEPENDENCIES END

# TINI INSTALL START
RUN set -ex && \
    mkdir -p /opt/spark/work-dir && \
    touch /opt/spark/RELEASE && \
    rm /bin/sh && \
    ln -sv /bin/bash /bin/sh && \
    echo "auth required pam_wheel.so use_uid" >> /etc/pam.d/su && \
    chgrp root /etc/passwd && chmod ug+rw /etc/passwd && \
    export TINI_HOME="/usr/local/sbin" && \
    curl -fSL "https://github.com/krallin/tini/releases/download/$TINI_VERSION/tini" -o "${TINI_HOME}/tini" && \
    curl -fSL "https://github.com/krallin/tini/releases/download/$TINI_VERSION/tini.asc" -o "${TINI_HOME}/tini.asc" && \
    chmod +x "${TINI_HOME}/tini" && \
    ln -s ${TINI_HOME}/tini /sbin/tini && \
    "${TINI_HOME}/tini" -h
# TINI INSTALL END

# SPARK INSTALL START
RUN mkdir -p /tmp/spark && \
    cd /tmp/spark && \
    wget -nv https://archive.apache.org/dist/spark/spark-${SPARK_VERSION}/spark-${SPARK_VERSION}-bin-hadoop${HADOOP_VERSION}.tgz && \
    tar xf spark-*.tgz && \
    rm spark-*.tgz && \
    cp -R /tmp/spark/*/jars /opt/spark && \
    cp -R /tmp/spark/*/bin /opt/spark && \
    cp -R /tmp/spark/*/sbin /opt/spark && \
    rm -Rf /tmp/spark
# SPARK INSTALL END
# SPARK UI FIX SDKTECHNO#83 START 
RUN mkdir -p /tmp/spark && cd /tmp/spark/ && \
    cp /opt/spark/jars/spark-core_2.11-${SPARK_VERSION}.jar /tmp/spark/spark-core_2.11-${SPARK_VERSION}.jar && \
    unzip -q spark-core_2.11-${SPARK_VERSION}.jar && \
    rm spark-core_2.11-${SPARK_VERSION}.jar && \
    sed -i -- 's#location.origin#location.href.replace(/executors\\\/$/, "")#g' org/apache/spark/ui/static/executorspage.js && \
    zip -r spark-core_2.11-${SPARK_VERSION}.jar . && \
    cp /tmp/spark/spark-core_2.11-${SPARK_VERSION}.jar /opt/spark/jars/spark-core_2.11-${SPARK_VERSION}.jar && \
    rm -Rf /tmp/spark
# SPARK UI FIX SDKTECHNO#83 END


COPY entrypoint.sh /opt/
RUN chmod 755 /opt/entrypoint.sh

WORKDIR /sandbox/

ENTRYPOINT [ "/opt/entrypoint.sh" ]
