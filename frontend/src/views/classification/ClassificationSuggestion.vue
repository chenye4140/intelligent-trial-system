<template>
  <div class="classification-page">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>五级定密建议</span>
        </div>
      </template>

      <!-- 输入区域 -->
      <el-form :model="form" label-width="100px" class="input-form">
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="案件ID">
              <el-input v-model="form.caseId" placeholder="请输入案件ID" @keyup.enter="handleGetSuggestion" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label=" ">
              <el-button type="primary" @click="handleGetSuggestion" :loading="loading">
                <el-icon><Search /></el-icon> 获取建议
              </el-button>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
    </el-card>

    <!-- 建议结果 -->
    <el-card shadow="hover" style="margin-top: 16px" v-if="suggestion.id">
      <template #header>
        <div class="card-header">
          <span>AI 定密建议</span>
          <el-tag :type="levelMap[suggestion.level]?.type" size="large">
            {{ levelMap[suggestion.level]?.text || '未知' }}
          </el-tag>
        </div>
      </template>

      <el-descriptions :column="2" border class="suggestion-desc">
        <el-descriptions-item label="建议等级">
          <el-tag :type="levelMap[suggestion.level]?.type">
            {{ levelMap[suggestion.level]?.text || suggestion.level }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="案件ID">{{ suggestion.caseId }}</el-descriptions-item>
        <el-descriptions-item label="建议来源" :span="2">{{ suggestion.source || 'AI 智能分析' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ suggestion.createTime }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="suggestion.adopted ? 'success' : 'info'" size="small">
            {{ suggestion.adopted ? '已采纳' : '待确认' }}
          </el-tag>
        </el-descriptions-item>
      </el-descriptions>

      <!-- AI 分析详情 -->
      <el-card shadow="never" style="margin-top: 16px">
        <template #header>
          <span>AI 分析依据</span>
        </template>
        <div class="analysis-detail">
          <el-row :gutter="16">
            <el-col :span="12" v-if="suggestion.analysis">
              <h4>分析说明</h4>
              <p>{{ suggestion.analysis }}</p>
            </el-col>
            <el-col :span="12" v-if="suggestion.reasons && suggestion.reasons.length">
              <h4>定密依据</h4>
              <ul>
                <li v-for="(reason, index) in suggestion.reasons" :key="index">{{ reason }}</li>
              </ul>
            </el-col>
          </el-row>
          <div v-if="suggestion.content" class="content-area">
            <h4>完整分析</h4>
            <pre>{{ typeof suggestion.content === 'string' ? suggestion.content : JSON.stringify(suggestion.content, null, 2) }}</pre>
          </div>
        </div>
      </el-card>

      <div class="action-buttons" style="margin-top: 16px">
        <el-button
          type="success"
          @click="handleAdopt"
          :disabled="suggestion.adopted"
          :loading="adoptLoading"
        >
          <el-icon><Check /></el-icon> 采纳建议
        </el-button>
        <el-button @click="handleGetSuggestion" :loading="loading">
          <el-icon><Refresh /></el-icon> 重新获取
        </el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Check, Refresh } from '@element-plus/icons-vue'
import { classificationApi } from '@/api/classification'

const levelMap = {
  1: { text: '绝密', type: 'danger' },
  2: { text: '机密', type: 'warning' },
  3: { text: '秘密', type: 'info' },
  4: { text: '内部', type: '' },
  5: { text: '公开', type: 'success' },
}

const form = reactive({
  caseId: '',
})

const loading = ref(false)
const adoptLoading = ref(false)
const suggestion = reactive({})

// 获取建议
const handleGetSuggestion = async () => {
  if (!form.caseId) {
    ElMessage.warning('请输入案件ID')
    return
  }
  loading.value = true
  try {
    // 先尝试获取已有建议
    const res = await classificationApi.getSuggestion(form.caseId)
    if (res.data) {
      Object.assign(suggestion, res.data)
    }
  } catch (e) {
    console.log('无已有建议，尝试生成...')
    // 如果没有已有建议，尝试生成
    try {
      const genRes = await classificationApi.generate({ caseId: form.caseId })
      if (genRes.data) {
        Object.assign(suggestion, genRes.data)
        ElMessage.success('AI 定密建议已生成')
      }
    } catch (genErr) {
      console.error('生成定密建议失败', genErr)
    }
  } finally {
    loading.value = false
  }
}

// 采纳建议
const handleAdopt = async () => {
  if (!suggestion.id) {
    ElMessage.warning('无有效建议')
    return
  }
  adoptLoading.value = true
  try {
    await classificationApi.adopt(suggestion.id)
    ElMessage.success('建议已采纳')
    suggestion.adopted = true
  } catch (e) {
    console.error('采纳失败', e)
  } finally {
    adoptLoading.value = false
  }
}
</script>

<style lang="scss" scoped>
.classification-page {
  padding: 4px;

  .card-header {
    font-weight: 600;
    font-size: 16px;
  }

  .input-form {
    margin-bottom: 8px;
  }

  .suggestion-desc {
    margin-top: 8px;
  }

  .analysis-detail {
    h4 {
      color: #303133;
      margin-bottom: 8px;
      font-size: 14px;
    }

    p {
      color: #606266;
      line-height: 1.6;
    }

    ul {
      padding-left: 20px;
      color: #606266;
      line-height: 1.8;

      li {
        margin-bottom: 4px;
      }
    }

    .content-area {
      margin-top: 16px;

      pre {
        background: #f5f7fa;
        padding: 16px;
        border-radius: 8px;
        font-family: 'Courier New', monospace;
        font-size: 13px;
        white-space: pre-wrap;
        word-break: break-all;
      }
    }
  }

  .action-buttons {
    display: flex;
    gap: 12px;
  }
}
</style>
