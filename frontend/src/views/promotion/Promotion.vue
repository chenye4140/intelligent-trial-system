<template>
  <div class="promotion-page">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>以案促改分析</span>
        </div>
      </template>

      <!-- 分析生成表单 -->
      <el-form :model="generateForm" label-width="100px" class="generate-form">
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="案件ID">
              <el-input v-model="generateForm.caseId" placeholder="请输入案件ID" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="分析类型">
              <el-select v-model="generateForm.analysisType" placeholder="请选择分析类型" style="width: 100%">
                <el-option label="制度漏洞分析" value="system_loophole" />
                <el-option label="廉政风险分析" value="integrity_risk" />
                <el-option label="警示教育方案" value="warning_education" />
                <el-option label="整改建议报告" value="rectification" />
                <el-option label="综合分析报告" value="comprehensive" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label=" ">
              <el-button type="primary" @click="handleGenerate" :loading="generateLoading">
                <el-icon><MagicStick /></el-icon> 生成分析
              </el-button>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
    </el-card>

    <!-- 历史记录列表 -->
    <el-card shadow="hover" style="margin-top: 16px">
      <template #header>
        <div class="card-header">
          <span>历史分析记录</span>
          <el-button type="primary" size="small" @click="loadList">
            <el-icon><Refresh /></el-icon> 刷新
          </el-button>
        </div>
      </template>

      <!-- 搜索 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="案件ID">
          <el-input v-model="searchForm.caseId" placeholder="请输入案件ID" clearable style="width: 160px" />
        </el-form-item>
        <el-form-item label="分析类型">
          <el-select v-model="searchForm.analysisType" placeholder="全部" clearable style="width: 140px">
            <el-option label="制度漏洞分析" value="system_loophole" />
            <el-option label="廉政风险分析" value="integrity_risk" />
            <el-option label="警示教育方案" value="warning_education" />
            <el-option label="整改建议报告" value="rectification" />
            <el-option label="综合分析报告" value="comprehensive" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable style="width: 120px">
            <el-option label="生成中" value="generating" />
            <el-option label="已完成" value="completed" />
            <el-option label="已采纳" value="adopted" />
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
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="caseId" label="案件ID" width="120" />
        <el-table-column prop="analysisType" label="分析类型" width="140">
          <template #default="{ row }">
            <el-tag size="small">{{ analysisTypeMap[row.analysisType] || row.analysisType }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="statusMap[row.status]?.type" size="small">
              {{ statusMap[row.status]?.text || row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="标题" min-width="180" show-overflow-tooltip />
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleView(row)">查看</el-button>
            <el-button type="info" link size="small" @click="handleCheckStatus(row)" v-if="row.status === 'generating'">状态</el-button>
            <el-button type="success" link size="small" @click="handleAdopt(row)" v-if="row.status === 'completed'">采纳</el-button>
            <el-button type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="pagination.pageNum"
        v-model:page-size="pagination.pageSize"
        :total="pagination.total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        @current-change="loadList"
        @size-change="loadList"
        style="margin-top: 16px; justify-content: flex-end"
      />
    </el-card>

    <!-- 查看分析对话框 -->
    <el-dialog v-model="viewDialogVisible" title="分析报告" width="800px" top="5vh">
      <div v-loading="viewLoading" class="analysis-content">
        <h3 v-if="viewData.title">{{ viewData.title }}</h3>
        <el-divider />
        <pre class="content-text">{{ viewData.content || JSON.stringify(viewData, null, 2) }}</pre>
      </div>
      <template #footer>
        <el-button @click="viewDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 状态对话框 -->
    <el-dialog v-model="statusDialogVisible" title="分析任务状态" width="500px">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="任务ID">{{ statusData.id }}</el-descriptions-item>
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
import { ElMessage, ElMessageBox } from 'element-plus'
import { MagicStick, Refresh, Search, RefreshLeft } from '@element-plus/icons-vue'
import { promotionApi } from '@/api/promotion'

const analysisTypeMap = {
  system_loophole: '制度漏洞分析',
  integrity_risk: '廉政风险分析',
  warning_education: '警示教育方案',
  rectification: '整改建议报告',
  comprehensive: '综合分析报告',
}

const statusMap = {
  generating: { text: '生成中', type: 'warning' },
  completed: { text: '已完成', type: 'success' },
  adopted: { text: '已采纳', type: 'info' },
  failed: { text: '失败', type: 'danger' },
}

// 生成表单
const generateForm = reactive({
  caseId: '',
  analysisType: 'comprehensive',
})
const generateLoading = ref(false)

// 搜索
const searchForm = reactive({
  caseId: '',
  analysisType: '',
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
const viewData = reactive({})

// 状态
const statusDialogVisible = ref(false)
const statusData = reactive({})

// 加载列表
const loadList = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
    }
    if (searchForm.caseId) params.caseId = searchForm.caseId
    if (searchForm.analysisType) params.analysisType = searchForm.analysisType
    if (searchForm.status) params.status = searchForm.status
    const res = await promotionApi.list(params)
    tableData.value = res.data?.list || []
    pagination.total = res.data?.total || 0
  } catch (e) {
    console.error('加载列表失败', e)
  } finally {
    loading.value = false
  }
}

// 生成分析
const handleGenerate = async () => {
  if (!generateForm.caseId) {
    ElMessage.warning('请输入案件ID')
    return
  }
  generateLoading.value = true
  try {
    await promotionApi.generate({ ...generateForm })
    ElMessage.success('分析任务已提交')
    loadList()
  } catch (e) {
    console.error('生成分析失败', e)
  } finally {
    generateLoading.value = false
  }
}

// 查看
const handleView = async (row) => {
  viewDialogVisible.value = true
  viewLoading.value = true
  try {
    const res = await promotionApi.getById(row.id)
    Object.assign(viewData, res.data || {})
  } catch (e) {
    console.error('获取分析详情失败', e)
  } finally {
    viewLoading.value = false
  }
}

// 查看状态
const handleCheckStatus = async (row) => {
  try {
    const res = await promotionApi.getStatus(row.id)
    Object.assign(statusData, res.data || {})
    statusDialogVisible.value = true
  } catch (e) {
    console.error('获取状态失败', e)
  }
}

// 采纳
const handleAdopt = async (row) => {
  try {
    await ElMessageBox.confirm('确定采纳该分析报告吗？', '提示', { type: 'info' })
    await promotionApi.updateStatus(row.id, 'adopted')
    ElMessage.success('采纳成功')
    loadList()
  } catch (e) {
    if (e !== 'cancel') console.error('采纳失败', e)
  }
}

// 删除
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定删除该分析记录吗？', '提示', { type: 'warning' })
    await promotionApi.remove(row.id)
    ElMessage.success('删除成功')
    loadList()
  } catch (e) {
    if (e !== 'cancel') console.error('删除失败', e)
  }
}

// 搜索
const handleSearch = () => {
  pagination.pageNum = 1
  loadList()
}

const resetSearch = () => {
  searchForm.caseId = ''
  searchForm.analysisType = ''
  searchForm.status = ''
  pagination.pageNum = 1
  loadList()
}

onMounted(() => {
  loadList()
})
</script>

<style lang="scss" scoped>
.promotion-page {
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

  .analysis-content {
    max-height: 70vh;
    overflow: auto;

    h3 {
      margin-bottom: 8px;
    }
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
