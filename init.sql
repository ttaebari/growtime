-- GrowTime 데이터베이스 초기화 스크립트

-- 데이터베이스가 존재하지 않으면 생성 (이미 postgres DB를 사용하므로 주석 처리)
-- CREATE DATABASE growtime;

-- UTF-8 인코딩 설정 확인
SELECT current_setting('server_encoding');

-- 타임존 설정
SET timezone = 'Asia/Seoul';

-- 확장 모듈 설치 (필요한 경우)
-- CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- 초기 설정 완료 메시지
SELECT 'GrowTime PostgreSQL Database initialized successfully!' as status; 