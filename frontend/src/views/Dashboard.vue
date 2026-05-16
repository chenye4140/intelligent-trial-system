<template>
  <div class="dashboard">
    <h2>👋 欢迎使用智能审理系统</h2>
    <p class="desc">AI驱动的纪检监察一体化平台 - 第一阶段</p>

    <el-row :gutter="20" style="margin-top: 30px">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <el-statistic title="法规文档" :value="0">
            <template #suffix>篇</template>
          </el-statistic>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <el-statistic title="案例文档" :value="0">
            <template #suffix>篇</template>
          </el-statistic>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <el-statistic title="裁判文书" :value="0">
            <template #suffix>篇</template>
          </el-statistic>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <el-statistic title="资料库" :value="0">
            <template #suffix>篇</template>
          </el-statistic>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>📊 系统状态</span>
            </div>
          </template>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="MySQL">
              <el-tag type="success">运行中</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="Redis">
              <el-tag type="success">运行中</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="Milvus">
              <el-tag type="success">运行中</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="MinIO">
              <el-tag type="success">运行中</el-tag>
            </el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>🚀 快速开始</span>
            </div>
          </template>
          <div class="quick-actions">
            <el-button type="primary" @click="$router.push('/upload')" style="width: 100%; margin-bottom: 10px">
              <el-icon><Upload /></el-icon>
              上传文档
            </el-button>
            <el-button @click="$router.push('/documents')" style="width: 100%; margin-bottom: 10px">
              <el-icon><Document /></el-icon>
              浏览文档库
            </el-button>
            <el-button @click="$router.push('/categories')" style="width: 100%">
              <el-icon><FolderOpened /></el-icon>
              管理目录
            </el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
</script>

<style lang="scss" scoped>
.dashboard {
  h2 {
    font-size: 24px;
    color: #333;
    margin: 0;
  }

  .desc {
    color: #666;
    margin-top: 8px;
  }
}

.stat-card {
  text-align: center;

  :deep(.el-statistic__head) {
    font-size: 14px;
    color: #606266;
  }

  :deep(.el-statistic__number) {
    font-size: 28px;
    font-weight: 600;
    color: #409eff;
  }
}

.card-header {
  font-weight: 600;
  font-size: 16px;
}

.quick-actions {
  display: flex;
  flex-direction: column;
}
</style>
