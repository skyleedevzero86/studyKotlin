# 프로젝트 특징

이 프로젝트는 다음과 같은 특이점들을 가지고 있습니다:

1. **Mroonga 검색 엔진 사용**

    - MariaDB에 Mroonga 플러그인을 설치하여 한글 검색 기능 구현
    - `infra/mariadb_1/Dockerfile`에서 Mroonga 플러그인 설치 설정
    - 래퍼 모드(Wrapper Mode)를 사용하여 기존 테이블을 Mroonga 인덱스로 변환

2. **커스텀 Hibernate 함수 등록**

    - `MariaDBFunctionContributor`를 통해 MATCH AGAINST 함수를 Hibernate에 등록
    - `META-INF/services/org.hibernate.boot.model.FunctionContributor`에 등록

3. **QueryDSL을 활용한 복잡한 검색 구현**

    - `PostRepositoryImpl`에서 QueryDSL을 사용하여 복잡한 검색 로직 구현
    - 점수 기반 정렬과 키워드 검색 기능 통합

4. **Kotlin 기반 Spring Boot 프로젝트**

    - Java 대신 Kotlin을 사용하여 더 간결하고 안전한 코드 작성
    - Kotlin의 null safety와 확장 함수 활용

5. **Docker Compose를 통한 개발 환경 구성**

    - MariaDB를 Docker 컨테이너로 실행
    - 데이터 영속화를 위한 볼륨 설정

6. **테스트 데이터 자동 생성**

    - `NotProdInitData` 클래스에서 개발 환경용 테스트 데이터 자동 생성
    - 다양한 주제의 게시글 샘플 데이터 포함

7. **스키마 관리 방식**

    - `schema.sql`과 `ddl-auto: update`를 함께 사용하여 테이블 구성
    - JPA 엔티티 기반의 자동 스키마 생성과 수동 스키마 정의의 조화

8. **QueryDSL 설정 최적화**

    - `JpaConfig`에서 JPAQueryFactory 빈 설정
    - `allOpen` 설정으로 JPA 엔티티 클래스 최적화

9. **엔티티 설계의 특이점**

    - `Post` 엔티티에서 `LONGTEXT` 타입의 content 필드 사용
    - `Member`와 `Post` 간의 양방향 관계 설정

10. **검색 최적화 설정**
    - Mroonga의 래퍼 모드를 사용하여 기존 테이블에 검색 인덱스 추가
    - `Post` 테이블의 title과 content 필드에 대한 Mroonga 인덱스 설정

11. **도커계정생성**
   - docker run --name mariadb_komroonga -e MYSQL_ROOT_PASSWORD=yourpassword -d mariadb:latest
   - docker exec -it mariadb_komroonga mariadb -u root -p
   - CREATE USER '계정'@'%' IDENTIFIED BY 'yourpassword';
   - GRANT ALL PRIVILEGES ON *.* TO '아이디'@'%' WITH GRANT OPTION;
   - FLUSH PRIVILEGES;
   - SHOW GRANTS FOR '계정'@'%';
   - docker exec -it mariadb_komroonga mariadb -u 아이디 -p
   - docker inspect -f '{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}' mariadb_komroonga
   - docker-compose down 
   - docker-compose up -d 
   - 

