# 1단계: 빌드 스테이지
FROM mariadb:11 AS builder

RUN apt-get update && \
    apt-get install -y \
        gcc \
        make \
        libcurl4-openssl-dev \
        libmariadb-dev

COPY http_post.c /usr/src/http_post.c

RUN gcc -std=c99 -Wall \
    -I/usr/include/mariadb \
    -fPIC -shared \
    /usr/src/http_post.c \
    -o /usr/lib/mysql/plugin/http_post.so \
    -lcurl

# 2단계: 최종 스테이지
FROM mariadb:11

RUN apt-get update && \
    apt-get install -y \
        mariadb-plugin-mroonga \
        libcurl4-openssl-dev && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

COPY --from=builder /usr/lib/mysql/plugin/http_post.so /usr/lib/mysql/plugin/http_post.so

COPY install-mroonga.sql /docker-entrypoint-initdb.d/