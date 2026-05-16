<template>
  <div class="report-page">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>文书生成</span>
        </div>
      </template>

      <!-- 生成表单 -->
      <el-form :model="generateForm" label-width="100px" class="generate-form">
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="案件ID" prop="caseId">
              <el-input v-model="generateForm.caseId" placeholder="请输入案件ID" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="文书模板" prop="templateId">
              <el-select v-model="generateForm.templateId" placeholder="请选择模板" style="width: 100%">
                <el-option
                  v-for="tpl in templates"
                  :key="tpl.id"
                  :label="tpl.name"
                  :value="tpl.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label=" ">
              <el-button type="primary" @click="handleGenerate" :loading="generateLoading">
                <el-icon><Document /></el-icon> 生成文书
              </el-button>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
    </el-card>

    <!-- 生成记录列表 -->
    <el-card shadow="hover" style="margin-top: 16px">
      <template #header>
        <div class="card-header">
          <span>生成记录</span>
          <el-button type="primary" size="small" @click="loadRecords">
            <el-icon><Refresh /></el-icon> 刷新
          </el-button>
        </div>
      </template>

      <!-- 搜索 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="案件ID">
          <el-input v-model="searchForm.caseId" placeholder="请输入案件ID" clearable style="width: 160px" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable style="width: 120px">
            <el-option label="生成中" value="generating" />
            <el-option label="已完成" value="completed" />
            <el-option label="失败" value="failed" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon> 查询
          </el-button>
          <el-button @click="resetSearch">
            <el-icon><RefreshLeft /></el-icon> 重置
          </el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" v-loading="loading" border stripe style="width: 100%">
        <el-table-column prop="id" label="记录ID" width="80" />
        <el-table-column prop="caseId" label="案件ID" width="120" />
        <el-table-column prop="templateName" label="模板名称" width="140" />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="statusMap[row.status]?.type" size="small">
              {{ statusMap[row.status]?.text || row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="fileName" label="文件名" min-width="180" show-overflow-tooltip />
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleView(row)">查看</el-button>
            <el-button type="info" link size="small" @click="handleCheckStatus(row)">状态</el-button>
            <el-button type="success" link size="small" @click="handleDownload(row)" v-if="row.status === 'completed'">下载</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="pagination.pageNum"
        v-model:page-size="pagination.pageSize"
        :total="pagination.total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        @current-change="loadRecords"
        @size-change="loadRecords"
        style="margin-top: 16px; justify-content: flex-end"
      />
    </el-card>

    <!-- 查看文书对话框 -->
    <el-dialog v-model="viewDialogVisible" title="文书内容" width="800px">
      <div v-loading="viewLoading" class="report-content">
        <pre class="content-text">{{ reportContent }}</pre>
      </div>
      <template #footer>
        <el-button @click="viewDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 状态查看对话框 -->
    <el-dialog v-model="statusDialogVisible" title="文书生成状态" width="500px">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="记录ID">{{ statusData.id }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="statusMap[statusData.status]?.type" size="small">
            {{ statusMap[statusData.status]?.text || statusData.status }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="进度">
          <el-progress :percentage="statusData.progress || 0" />
        </el-descriptions-item>
        <el-descriptions-item label="消息">{{ statusData.message || '-' }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="statusDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Document, Refresh, Search, RefreshLeft } from '@element-plus/icons-vue'
import { reportApi } from '@/api/report'

const statusMap = {
  generating: { text: '生成中', type: 'warning' },
  completed: { text: '已完成', type: 'success' },
  failed: { text: '失败', type: 'danger' },
}

// 生成表单
const generateForm = reactive({
  caseId: '',
  templateId: '',
})
const generateLoading = ref(false)
const templates = ref([])

// 搜索
const searchForm = reactive({
  caseId: '',
  status: '',
})

// 分页
const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0,
})

// 表格
const tableData = ref([])
const loading = ref(false)

// 查看
const viewDialogVisible = ref(false)
const viewLoading = ref(false)
const reportContent = ref('')

// 状态
const statusDialogVisible = ref(false)
const statusData = reactive({})

// 加载模板
const loadTemplates = async () => {
  try {
    const res = await reportApi.getTemplates()
    templates.value = res.data || []
  } catch (e) {
    console.error('加载模板失败', e)
  }
}

// 加载记录
const loadRecords = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
    }
    if (searchForm.caseId) params.caseId = searchForm.caseId
    if (searchForm.status) params.status = searchForm.status
    const res = await reportApi.list(params)
    tableData.value = res.data?.list || []
    pagination.total = res.data?.total || 0
  } catch (e) {
    console.error('加载记录失败', e)
  } finally {
    loading.value = false
  }
}

// 生成文书
const handleGenerate = async () => {
  if (!generateForm.caseId) {
    ElMessage.warning('请输入案件ID')
    return
  }
  if (!generateForm.templateId) {
    ElMessage.warning('请选择文书模板')
    return
  }
  generateLoading.value = true
  try {
    await reportApi.generate({ ...generateForm })
    ElMessage.success('文书生成任务已提交')
    loadRecords()
  } catch (e) {
    console.error('生成文书失败', e)
  } finally {
    generateLoading.value = false
  }
}

// 查看文书
const handleView = async (row) => {
  viewDialogVisible.value = true
  viewLoading.value = true
  try {
    const res = await reportApi.getRecord(row.id)
    reportContent.value = res.data?.content || JSON.stringify(res.data || {}, null, 2)
  } catch (e) {
    console.error('获取文书失败', e)
  } finally {
    viewLoading.value = false
  }
}

// 查看状态
const handleCheckStatus = async (row) => {
  try {
    const res = await reportApi.getStatus(row.id)
    Object.assign(statusData, res.data || {})
    statusDialogVisible.value = true
  } catch (e) {
    console.error('获取状态失败', e)
  }
}

// 下载
const handleDownload = (row) => {
  ElMessage.info('文书下载功能：' + row.fileName)
}

// 搜索
const handleSearch = () => {
  pagination.pageNum = 1
  loadRecords()
}

const resetSearch = () => {
  searchForm.caseId = ''
  searchForm.status = ''
  pagination.pageNum = 1
  loadRecords()
}

onMounted(() => {
  loadTemplates()
  loadRecords()
})
</script>

<style lang="scss" scoped>
.report-page {
  padding: 4px;

  .card-header {
    font-weight: 600;
    font-size: 16px;
  }

  .generate-form {
    margin-bottom: 8px;
  }

  .search-form {
    margin-bottom: 10px;
  }

  .report-content {
    max-height: 60vh;
    overflow: auto;
  }

  .content-text {
    background: #f5f7fa;
    padding: 16px;
    border-radius: 8px;
    font-family: 'Courier New', monospace;
    font-size: 13px;
    white-space: pre-wrap;
    word-break: break-all;
  }
}
</style>
