<template>
  <div class="document-parse-container">
    <!-- Upload Area -->
    <el-card shadow="hover" class="upload-card">
      <template #header>
        <span>文档上传</span>
      </template>
      <el-upload
        drag
        multiple
        :action="'/api/document/parse/upload'"
        :headers="uploadHeaders"
        :on-success="handleUploadSuccess"
        :on-error="handleUploadError"
        :before-upload="beforeUpload"
        :accept="'.doc,.docx,.pdf,.png,.jpg,.jpeg'"
      >
        <el-icon :size="64" color="#409eff"><UploadFilled /></el-icon>
        <div class="el-upload__text">
          拖拽文件到此处或 <em>点击上传</em>
        </div>
        <template #tip>
          <div class="el-upload__tip">
            支持 .doc/.docx/.pdf/图片 格式，可批量上传
          </div>
        </template>
      </el-upload>
    </el-card>

    <!-- Task List -->
    <el-card shadow="hover" class="task-card">
      <template #header>
        <div class="card-header">
          <span>解析任务列表</span>
          <el-button type="primary" size="small" @click="fetchTasks">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
        </div>
      </template>

      <el-table :data="tasks" v-loading="loading" stripe style="width: 100%">
        <el-table-column prop="fileName" label="文件名" min-width="200" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.status === 'success'" type="success" size="small">成功</el-tag>
            <el-tag v-else-if="row.status === 'processing'" type="warning" size="small">处理中</el-tag>
            <el-tag v-else-if="row.status === 'failed'" type="danger" size="small">失败</el-tag>
            <el-tag v-else type="info" size="small">等待中</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="进度" width="180">
          <template #default="{ row }">
            <el-progress :percentage="row.progress" :status="row.status === 'failed' ? 'exception' : row.status === 'success' ? 'success' : ''" />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === 'success'" type="primary" link size="small" @click="viewResult(row)">
              查看结果
            </el-button>
            <el-button v-if="row.status === 'failed'" type="danger" link size="small" @click="retryTask(row)">
              重试
            </el-button>
            <el-button type="danger" link size="small" @click="deleteTask(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.pageSize"
        :total="pagination.total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @current-change="fetchTasks"
        @size-change="fetchTasks"
        style="margin-top: 16px; justify-content: flex-end"
      />
    </el-card>

    <!-- Result Dialog -->
    <el-dialog v-model="resultVisible" title="解析结果" width="70%" top="5vh">
      <div class="result-content">
        <el-tree :data="resultTree" :props="{ label: 'key', children: 'children' }" default-expand-all />
        <pre class="json-preview">{{ JSON.stringify(resultData, null, 2) }}</pre>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { uploadDocument, getParseTaskList, retryParse, getParseResult, deleteParseTask } from '@/api/document'

const tasks = ref([])
const loading = ref(false)
const resultVisible = ref(false)
const resultData = ref({})
const resultTree = ref([])

const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0
})

const uploadHeaders = computed(() => ({
  Authorization: `Bearer ${localStorage.getItem('token') || ''}`
}))

const fetchTasks = async () => {
  loading.value = true
  try {
    const res = await getParseTaskList({
      page: pagination.page,
      pageSize: pagination.pageSize
    })
    if (res.data) {
      tasks.value = res.data.records || res.data.list || []
      pagination.total = res.data.total || 0
    }
  } catch (e) {
    // handled by interceptor
  } finally {
    loading.value = false
  }
}

const beforeUpload = (file) => {
  const allowedTypes = ['.doc', '.docx', '.pdf', '.png', '.jpg', '.jpeg']
  const ext = '.' + file.name.split('.').pop().toLowerCase()
  if (!allowedTypes.includes(ext)) {
    ElMessage.error('不支持的文件格式')
    return false
  }
  if (file.size > 100 * 1024 * 1024) {
    ElMessage.error('文件大小不能超过 100MB')
    return false
  }
  return true
}

const handleUploadSuccess = (res) => {
  ElMessage.success('文件上传成功')
  fetchTasks()
}

const handleUploadError = () => {
  ElMessage.error('文件上传失败')
}

const viewResult = async (row) => {
  try {
    const res = await getParseResult(row.id)
    resultData.value = res.data || {}
    resultTree.value = buildTree(res.data || {})
    resultVisible.value = true
  } catch (e) {
    // handled by interceptor
  }
}

const retryTask = async (row) => {
  try {
    await retryParse(row.id)
    ElMessage.success('已重新提交解析任务')
    fetchTasks()
  } catch (e) {
    // handled by interceptor
  }
}

const deleteTask = async (row) => {
  try {
    await ElMessageBox.confirm('确定删除该解析任务吗？', '提示', { type: 'warning' })
    await deleteParseTask(row.id)
    ElMessage.success('删除成功')
    fetchTasks()
  } catch (e) {
    if (e !== 'cancel') {
      // handled by interceptor
    }
  }
}

function buildTree(obj, prefix = '') {
  const result = []
  if (typeof obj !== 'object' || obj === null) {
    return [{ key: prefix || 'root', value: String(obj) }]
  }
  for (const [key, value] of Object.entries(obj)) {
    const node = { key }
    if (typeof value === 'object' && value !== null) {
      node.children = buildTree(value, key)
    } else {
      node.children = [{ key: '值', value: String(value) }]
    }
    result.push(node)
  }
  return result
}

fetchTasks()
</script>

<style scoped>
.document-parse-container {
  padding: 4px;
}
.upload-card {
  margin-bottom: 16px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.el-upload__text {
  margin-top: 16px;
  color: #606266;
  font-size: 14px;
}
.el-upload__tip {
  margin-top: 8px;
  color: #909399;
  font-size: 13px;
}
.result-content {
  max-height: 70vh;
  overflow: auto;
}
.json-preview {
  background: #f5f7fa;
  padding: 16px;
  border-radius: 8px;
  font-family: 'Courier New', monospace;
  font-size: 13px;
  white-space: pre-wrap;
  word-break: break-all;
  margin-top: 16px;
}
</style>
