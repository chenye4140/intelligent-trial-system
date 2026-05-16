#!/bin/bash
# 智能审理系统 - 一键启动脚本

echo "🚀 启动智能审理系统..."

# 1. 启动基础设施
echo "📦 启动 MySQL, Redis, MinIO..."
cd ~/intelligent-trial-system
docker-compose up -d mysql redis minio

# 等待 MySQL 就绪
echo "⏳ 等待 MySQL 启动..."
while ! docker exec trial-mysql mysqladmin ping -h localhost --silent 2>/dev/null; do
    sleep 2
    echo "   等待中..."
done
echo "✅ MySQL 就绪!"

# 2. 启动后端
echo "🔧 启动后端服务..."
cd ~/intelligent-trial-system/document
mvn spring-boot:run -DskipTests &

# 等待后端就绪
echo "⏳ 等待后端启动..."
while ! curl -s http://localhost:8082/actuator/health > /dev/null 2>&1; do
    sleep 3
    echo "   等待中..."
done
echo "✅ 后端就绪!"

# 3. 启动前端
echo "🎨 启动前端..."
cd ~/intelligent-trial-system/frontend
npm install
npm run dev &

echo ""
echo "======================================"
echo "  🎉 系统启动完成!"
echo "  前端: http://localhost:3000"
echo "  后端: http://localhost:8082"
echo "  MinIO: http://localhost:9001"
echo "======================================"
