# mariadb:11 이미지를 기반으로 사용
FROM mariadb:11

# 패키지 목록 업데이트 및 Mroonga 플러그인 설치
RUN apt-get update && \
    apt-get install -y mariadb-plugin-mroonga && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# 컨테이너 초기화 시 Mroonga 플러그인을 자동으로 설치하도록 SQL 스크립트 복사
COPY install-mroonga.sql /docker-entrypoint-initdb.d/