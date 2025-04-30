-- Mroonga 플러그인 설치
INSTALL SONAME 'ha_mroonga';
CREATE FUNCTION http_post RETURNS STRING SONAME 'http_post.so';