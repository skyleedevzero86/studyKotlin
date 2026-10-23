# studyKotlin

코틀린으로 주제마다 따로 만들어 보는 학습 저장소다. <br/>
프로젝트는 디렉터리 하나로 두고, 실행 방법과 구조는 그 프로젝트의 README에 적는다. <br/>
여기에는 저장소에 올라온 프로젝트만 적는다.<br/>

## 프로젝트

| 디렉터리                                               | 설명                                                                                                               |
| ------------------------------------------------------ | ------------------------------------------------------------------------------------------------------------------ |
| [dense_vector](dense_vector/README.md)                 | Elasticsearch 8.15용. `hnsw`로 코사인 kNN을 본다.                                                                  |
| [dense_vector2](dense_vector2/README.md)               | 최신 코틀린(2.4) 프로젝트. 8.15와 분리한 Elasticsearch 9.5에서 `bbq_hnsw`, `bbq_disk`, `vectordb_document`를 본다. |
| [youtube_download](youtube_download/README.md)         | 공유 주소를 MySQL에 남기고, CDC로 Elasticsearch 9 `bbq_hnsw` 목록과 MinIO 파일을 맞춘다.                           |
| [youtube_download_msa](youtube_download_msa/README.md) | 같은 흐름을 게이트웨이, 다운로드 원장, 미디어 워커, 검색 서비스로 나눈다.                                          |
| [officeDoc](officeDoc/README.md)                       | Vue 화면에서 문서를 편집하고, 검사 결과는 PostgreSQL, 본문은 MinIO, 다운로드 토큰은 Redis에 둔다.                  |
