<template>
  <div class="documents-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>📚 多库管理</span>
          <el-button type="primary" size="small" @click="showUploadDialog">
            <el-icon><Plus /></el-icon>
            上传文档
          </el-button>
        </div>
      </template>

      <!-- 搜索栏 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="库类型">
          <el-select v-model="searchForm.repoType" placeholder="全部" clearable style="width: 150px">
            <el-option label="法规库" :value="1" />
            <el-option label="资料库" :value="2" />
            <el-option label="裁判文书库" :value="3" />
            <el-option label="案例库" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="关键词">
          <el-input v-model="searchForm.keyword" placeholder="搜索标题、文号或发布单位" clearable style="width: 200px" />
        </el-form-item>
        <el-form-item label="有效性">
          <el-select v-model="searchForm.validityStatus" placeholder="全部" clearable style="width: 120px">
            <el-option label="有效" value="valid" />
            <el-option label="失效" value="invalid" />
            <el-option label="待生效" value="pending" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadDocuments">查询</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 文档列表 -->
      <el-table :data="tableData" v-loading="loading" border stripe style="margin-top: 20px">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
        <el-table-column prop="docNo" label="文号" width="140" show-overflow-tooltip />
        <el-table-column prop="publishUnit" label="发布单位" width="140" show-overflow-tooltip />
        <el-table-column prop="repoType" label="类型" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.repoType === 1" type="danger">法规</el-tag>
            <el-tag v-else-if="row.repoType === 2" type="warning">资料</el-tag>
            <el-tag v-else-if="row.repoType === 3" type="info">裁判文书</el-tag>
            <el-tag v-else-if="row.repoType === 4" type="success">案例</el-tag>
            <el-tag v-else>未知</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="validityStatus" label="有效性" width="90">
          <template #default="{ row }">
            <el-tag :type="validityType(row.validityStatus)" size="small">
              {{ validityMap[row.validityStatus] || row.validityStatus }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="fileType" label="格式" width="70" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" size="small">
              {{ statusMap[row.status] || '未知' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="publishDate" label="发布日期" width="110" />
        <el-table-column prop="createTime" label="上传时间" width="160" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handlePreview(row)">预览</el-button>
            <el-button type="success" link size="small" @click="handleDownload(row)">下载</el-button>
            <el-button type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        v-model:current-page="searchForm.pageNum"
        v-model:page-size="searchForm.pageSize"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @current-change="loadDocuments"
        @size-change="loadDocuments"
        style="margin-top: 20px; justify-content: flex-end"
      />
    </el-card>

    <!-- 上传对话框 -->
    <el-dialog v-model="uploadDialogVisible" title="上传文档" width="500px">
      <el-form :model="uploadForm" label-width="100px">
        <el-form-item label="库类型" required>
          <el-select v-model="uploadForm.repoType" placeholder="请选择" style="width: 100%">
            <el-option label="法规库" :value="1" />
            <el-option label="资料库" :value="2" />
            <el-option label="裁判文书库" :value="3" />
            <el-option label="案例库" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="标题">
          <el-input v-model="uploadForm.title" placeholder="可选，默认使用文件名" />
        </el-form-item>
        <el-form-item label="有效性">
          <el-select v-model="uploadForm.validityStatus" style="width: 100%">
            <el-option label="有效" value="valid" />
            <el-option label="失效" value="invalid" />
            <el-option label="待生效" value="pending" />
          </el-select>
        </el-form-item>
        <el-form-item label="上传文件" required>
          <el-upload
            ref="uploadRef"
            :auto-upload="false"
            :limit="1"
            :on-change="handleFileChange"
            accept=".doc,.docx,.pdf,.xls,.xlsx,.txt"
          >
            <template #trigger>
              <el-button type="primary">选择文件</el-button>
            </template>
            <template #tip>
              <div class="el-upload__tip">支持 .doc/.docx/.pdf/.xls/.xlsx/.txt，不超过50MB</div>
            </template>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="uploadDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="uploading" @click="handleUpload">上传</el-button>
      </template>
    </el-dialog>

    <!-- 预览对话框 -->
    <el-dialog v-model="previewVisible" :title="previewDoc?.title || '文档预览'" width="80%" top="5vh">
      <div v-if="previewDoc" class="preview-content">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="文号">{{ previewDoc.docNo || '-' }}</el-descriptions-item>
          <el-descriptions-item label="发布单位">{{ previewDoc.publishUnit || '-' }}</el-descriptions-item>
          <el-descriptions-item label="发布日期">{{ previewDoc.publishDate || '-' }}</el-descriptions-item>
          <el-descriptions-item label="有效性">{{ validityMap[previewDoc.validityStatus] || '-' }}</el-descriptions-item>
          <el-descriptions-item label="文件类型">{{ previewDoc.fileType || '-' }}</el-descriptions-item>
          <el-descriptions-item label="文件大小">{{ formatFileSize(previewDoc.fileSize) }}</el-descriptions-item>
        </el-descriptions>
        <el-divider />
        <div v-if="previewDoc.summary" style="margin-bottom: 16px">
          <strong>摘要：</strong>{{ previewDoc.summary }}
        </div>
        <div v-if="previewContent" class="preview-body">
          <pre>{{ previewContent }}</pre>
        </div>
        <div v-else style="text-align: center; padding: 40px; color: #999">
          该文档暂无可预览内容，请<a href="javascript:void(0)" @click="handleDownload(previewDoc)">下载</a>查看
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { searchDocuments, deleteDocument, addDocument, downloadDocument as downloadDocApi } from '@/api/repository'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)

const validityMap = {
  valid: '有效',
  invalid: '失效',
  pending: '待生效',
}

const statusMap = {
  0: '草稿',
  1: '已发布',
  2: '已下架',
}

const searchForm = reactive({
  pageNum: 1,
  pageSize: 10,
  repoType: null,
  keyword: '',
  validityStatus: '',
})

// 上传相关
const uploadDialogVisible = ref(false)
const uploading = ref(false)
const uploadForm = reactive({
  repoType: null,
  title: '',
  validityStatus: 'valid',
  file: null,
})

// 预览相关
const previewVisible = ref(false)
const previewDoc = ref(null)
const previewContent = ref('')

const validityType = (status) => {
  if (status === 'valid') return 'success'
  if (status === 'invalid') return 'danger'
  return 'warning'
}

const statusType = (status) => {
  if (status === 1) return 'success'
  if (status === 2) return 'info'
  return 'warning'
}

const formatFileSize = (bytes) => {
  if (!bytes) return '-'
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
  return (bytes / (1024 * 1024)).toFixed(1) + ' MB'
}

const loadDocuments = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: searchForm.pageNum,
      pageSize: searchForm.pageSize,
    }
    if (searchForm.repoType !== null && searchForm.repoType !== '') {
      params.repoType = searchForm.repoType
    }
    if (searchForm.keyword) {
      params.keyword = searchForm.keyword
    }
    if (searchForm.validityStatus) {
      params.validityStatus = searchForm.validityStatus
    }

    // 后端返回 IPage<Document>，结构: { records, total, current, size, pages }
    const res = await searchDocuments(params)
    const pageData = res.data
    tableData.value = pageData.records || []
    total.value = pageData.total || 0
  } catch (error) {
    console.error('加载文档失败', error)
    ElMessage.error('加载文档失败')
  } finally {
    loading.value = false
  }
}

const resetSearch = () => {
  searchForm.repoType = null
  searchForm.keyword = ''
  searchForm.validityStatus = ''
  searchForm.pageNum = 1
  loadDocuments()
}

const showUploadDialog = () => {
  uploadForm.repoType = null
  uploadForm.title = ''
  uploadForm.validityStatus = 'valid'
  uploadForm.file = null
  uploadDialogVisible.value = true
}

const handleFileChange = (file) => {
  uploadForm.file = file.raw
  if (!uploadForm.title) {
    uploadForm.title = file.name.replace(/\.[^.]+$/, '')
  }
}

const handleUpload = async () => {
  if (!uploadForm.repoType || !uploadForm.file) {
    ElMessage.warning('请选择库类型和上传文件')
    return
  }

  uploading.value = true
  try {
    // 先创建文档记录，然后通过后端上传接口处理
    const docData = {
      repoType: uploadForm.repoType,
      title: uploadForm.title,
      validityStatus: uploadForm.validityStatus,
      status: 1,
    }
    await addDocument(docData)
    ElMessage.success('文档记录创建成功，请在目录管理中上传文件')
    uploadDialogVisible.value = false
    loadDocuments()
  } catch (error) {
    console.error('上传失败', error)
  } finally {
    uploading.value = false
  }
}

const handlePreview = async (row) => {
  previewDoc.value = row
  previewContent.value = row.content || ''
  previewVisible.value = true
}

const handleDownload = async (row) => {
  try {
    const res = await downloadDocApi(row.id)
    const blob = new Blob([res])
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = (row.title || row.docNo || 'document') + '.' + (row.fileType || 'pdf')
    link.click()
    window.URL.revokeObjectURL(url)
    ElMessage.success('下载成功')
  } catch (error) {
    console.error('下载失败', error)
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定删除文档「${row.title}」吗？`, '提示', {
      type: 'warning',
    })
    await deleteDocument(row.id)
    ElMessage.success('删除成功')
    loadDocuments()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败', error)
    }
  }
}

onMounted(() => {
  loadDocuments()
})
</script>

<style lang="scss" scoped>
.documents-page {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    font-weight: 600;
    font-size: 16px;
  }

  .search-form {
    margin-bottom: 10px;
  }

  .preview-content {
    padding: 8px;
  }

  .preview-body {
    max-height: 500px;
    overflow-y: auto;
    background: #f5f7fa;
    padding: 16px;
    border-radius: 8px;

    pre {
      white-space: pre-wrap;
      word-break: break-all;
      margin: 0;
      font-size: 14px;
      line-height: 1.6;
    }
  }
}
</style>
