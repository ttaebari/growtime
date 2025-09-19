#!/bin/bash

# GrowTime Docker 실행 스크립트

echo "🌱 GrowTime Docker 환경 시작..."

# 환경 변수 체크
if [ -z "$GITHUB_CLIENT_ID" ] || [ -z "$GITHUB_CLIENT_SECRET" ]; then
    echo "⚠️  경고: GitHub OAuth 환경 변수가 설정되지 않았습니다."
    echo "   GITHUB_CLIENT_ID와 GITHUB_CLIENT_SECRET를 설정해주세요."
    echo ""
    echo "   예시:"
    echo "   export GITHUB_CLIENT_ID=your_client_id"
    echo "   export GITHUB_CLIENT_SECRET=your_client_secret"
    echo ""
fi

# Docker Compose 실행
echo "🐳 Docker 컨테이너들을 시작하는 중..."
docker-compose up -d

echo ""
echo "✅ Docker 컨테이너 시작 완료!"
echo ""
echo "📋 서비스 접속 정보:"
echo "   🚀 애플리케이션: http://localhost:8088"
echo "   🗄️  pgAdmin:     http://localhost:5050 (admin@growtime.com / admin123)"
echo "   💾 PostgreSQL:  localhost:5432 (postgres / qwer1234!!)"
echo ""
echo "📊 상태 확인:"
echo "   docker-compose ps          # 컨테이너 상태 확인"
echo "   docker-compose logs app    # 애플리케이션 로그"
echo "   docker-compose logs postgres # DB 로그"
echo ""
echo "🛑 중지:"
echo "   docker-compose down        # 컨테이너 중지 및 삭제"
echo "   docker-compose down -v     # 데이터까지 완전 삭제" 