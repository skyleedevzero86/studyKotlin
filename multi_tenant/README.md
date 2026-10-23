# multi_tenant

Spring Boot와 Hibernate로 행 분리, 스키마 분리, DB 분리를 같은 조회 코드에서 비교하는 예제다.

API는 `18080`이다. 테넌트는 헤더 `X-Tenant-Id`로 고른다.

## 버전

- Java 25
- Kotlin 2.4.21
- Spring Boot 4.1.2
- Hibernate는 Spring Boot가 관리하는 버전
- PostgreSQL 17 (테스트는 Testcontainers)

## 모듈

| 모듈                  | 역할                                        |
| --------------------- | ------------------------------------------- |
| `domain`              | 주문, 배역, `TenantContext` (`ScopedValue`) |
| `application`         | 조회와 세션 비교                            |
| `adapter-web`         | 헤더 필터와 HTTP                            |
| `adapter-persistence` | JPA, Flyway, 스키마 매퍼, 테넌트 DataSource |
| `bootstrap`           | 실행과 프로파일                             |

## 실행

테스트는 Docker가 필요하다.

```powershell
cd D:\intel\studyKotlin\multi_tenant
.\gradlew.bat test
```

로컬 서버는 기존 PostgreSQL `127.0.0.1:5432`에 아래 데이터베이스만 새로 만든 뒤 띄운다.

```sql
CREATE DATABASE multitenant;
CREATE DATABASE multitenant_shared;
CREATE DATABASE multitenant_metadata;
CREATE DATABASE multitenant_marvel;
CREATE DATABASE multitenant_dc;
```

```powershell
.\gradlew.bat :bootstrap:bootRun
.\gradlew.bat :bootstrap:bootRun --args="--spring.profiles.active=schema"
.\gradlew.bat :bootstrap:bootRun --args="--spring.profiles.active=database"
```

기본 프로파일은 `row`다.

```powershell
curl.exe -s -H "X-Tenant-Id: bookshop" http://127.0.0.1:18080/orders/ORDER-001
curl.exe -s -H "X-Tenant-Id: recordshop" http://127.0.0.1:18080/orders/ORDER-001
curl.exe -s -H "X-Tenant-Id: marvel" http://127.0.0.1:18080/castings/ryan-reynolds
curl.exe -s -H "X-Tenant-Id: dc" http://127.0.0.1:18080/castings/ryan-reynolds
```

## 설계 결정

행 분리는 `orders.tenant_id`에 Hibernate `@TenantId`를 붙인다. <br/>
Repository 메서드는 테넌트 인자를 받지 않는다. <br/>
세션을 열 때 `CurrentTenantIdentifierResolver`가 `TenantContext`를 읽고, Hibernate가 SQL에 `tenant_id` 조건을 넣는다. 저장할 때도 그 값으로 컬럼을 채운다.<br/>

스키마 분리는 같은 DataSource로 `multitenant_shared`에 붙고, `TenantSchemaMapper`가 `marvel`을 `marvel_schema`, `dc`를 `dc_schema`로 바꾼다.<br/>
Hibernate가 `Connection.setSchema()`를 호출한다.<br/>

DB 분리는 `MultiTenantConnectionProvider`가 세션의 테넌트 ID로 `multitenant_marvel` 또는 `multitenant_dc` 풀을 고른다.<br/>
테넌트 없이 JDBC 메타데이터를 볼 때는 `multitenant_metadata`를 쓴다. 이 DB에는 배역 테이블이 없다.<br/>

세 방식 모두 요청 필터가 `X-Tenant-Id`를 `ScopedValue`에 묶고, 그 범위에서 필터 체인을 실행한다. <br/>Hibernate가 저장소 쿼리를 만드는 시점에는 아직 요청이 없다.<br/>
그때는 등록된 첫 테넌트를 돌려 주고, 요청이 묶인 뒤에는 그 값을 검사한다.<br/>
`open-in-view`는 꺼 두어서 세션은 `@Transactional` 동안만 열린다. 바인딩은 범위가 끝나면 풀린다.<br/>

없는 주문과 없는 배역은 **404**다.<br/> 다른 테넌트에 같은 키가 있어도 그 사실을 응답으로 알리지 않기 위해서다. <br/>헤더가 없거나 등록되지 않은 테넌트는 **400**이다.<br/>
권한 검사는 이 예제에서 하지 않는다.<br/>
헤더 값 자체를 테넌트 ID로 쓴다.<br/>

스키마 분리와 DB 분리의 `ddl-auto`는 `none`이다. Hibernate 검증이 메타데이터 DB나 `public` 스키마를 보면 `casting` 테이블이 없다고 실패한다.<br/>
테이블은 Flyway가 테넌트 스키마 또는 테넌트 DB에 만든다.<br/>
행 분리만 `validate`다.

### 네이티브 SQL

`@TenantId` 조건은 Hibernate가 만드는 SQL에만 붙는다. <br/>
`OrderJpaRepository.findNativeByOrderId`는 `order_id`만 조건으로 두므로 두 테넌트 행이 같이 나온다. <br/>`RowModeTest.nativeQuerySeesEveryTenant`가 그 차이를 확인한다.<br/>

직접 작성한 SQL에는 테넌트 조건을 쿼리 안에 넣는다.

```sql
select id, tenant_id, order_id, status
from orders
where tenant_id = :tenantId
  and order_id = :orderId
```

`:tenantId`는 `TenantContext.required()`로 읽은 값이다.

### 이미 열린 세션

`CastingComparison.sharingSession`은 메서드에 `@Transactional`이 있다.<br/>
바깥에서 `marvel`로 이 메서드를 호출하면 세션 테넌트는 `marvel`로 고정된다.<br/>
반복문 안에서 `dc`를 다시 묶어도 같은 세션에 참여하므로 둘 다 Deadpool이 된다.<br/>

`newSessions`에는 트랜잭션이 없다.<br/>
안의 `FindCasting.find`가 호출될 때마다 새 세션이 열리고, 그때의 `ScopedValue`를 읽는다.<br/>
그래서 Marvel은 Deadpool, DC는 Green Lantern이다.<br/>

`open-in-view`가 켜져 있으면 요청 동안 세션이 유지되므로, 바깥 트랜잭션만 지우는 것으로는 테넌트가 바뀌지 않는다.<br/>

### ScopedValue

값은 지정한 실행 범위가 끝나면 풀린다. <br/>
예외로 빠져도 같다. 중첩된 `call`이 끝나면 바깥 값으로 돌아간다. <br/>
필터는 `finally`에서 직접 지우지 않는다.<br/>

## 이 예제에 없는 것

기관 JWT, SOP 설정, 엑셀 이관, 비동기 컨텍스트 전달은 넣지 않았다. 이미지와 글의 조회 흐름만 구현했다.
