-- GrowTime 데이터베이스 초기화 스크립트

-- postgres 사용자 생성 (이미 존재할 수 있으므로 IF NOT EXISTS 사용)
DO $$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'postgres') THEN
        CREATE ROLE postgres WITH LOGIN SUPERUSER CREATEDB CREATEROLE PASSWORD 'qwer1234!!';
    END IF;
END
$$;

-- growtime 데이터베이스가 존재하지 않으면 생성
SELECT 'CREATE DATABASE growtime'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'growtime')\gexec

-- growtime 데이터베이스에 대한 권한 설정
GRANT ALL PRIVILEGES ON DATABASE growtime TO postgres;

-- UTF-8 인코딩 설정 확인
SELECT current_setting('server_encoding');

-- 타임존 설정
SET timezone = 'Asia/Seoul';

-- 확장 모듈 설치 (필요한 경우)
-- CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- 초기 설정 완료 메시지
SELECT 'GrowTime PostgreSQL Database initialized successfully!' as status;
