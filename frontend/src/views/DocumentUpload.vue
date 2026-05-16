<template>
  <div class="upload-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>📤 文档上传与解析</span>
        </div>
      </template>

      <el-form :model="form" label-width="100px" style="max-width: 600px">
        <el-form-item label="库类型" required>
          <el-select v-model="form.repoType" placeholder="请选择库类型" style="width: 100%">
            <el-option label="法规库" :value="1" />
            <el-option label="资料库" :value="2" />
            <el-option label="裁判文书库" :value="3" />
            <el-option label="案例库" :value="4" />
          </el-select>
        </el-form-item>

        <el-form-item label="所属目录">
          <el-tree-select
            v-model="form.directoryId"
            :data="directoryTree"
            :props="{ label: 'name', children: 'children' }"
            node-key="id"
            placeholder="选择目录（可选）"
            clearable
            check-strictly
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="标题">
          <el-input v-model="form.title" placeholder="可选，默认使用文件名" />
        </el-form-item>

        <el-form-item label="文号">
          <el-input v-model="form.docNo" placeholder="可选，如：中纪发〔2024〕1号" />
        </el-form-item>

        <el-form-item label="发布单位">
          <el-input v-model="form.publishUnit" placeholder="可选" />
        </el-form-item>

        <el-form-item label="发布日期">
          <el-date-picker
            v-model="form.publishDate"
            type="date"
            placeholder="选择日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="有效性">
          <el-select v-model="form.validityStatus" style="width: 100%">
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
            accept=".doc,.docx,.pdf,.xls,.xlsx,.txt"
            :on-change="handleFileChange"
          >
            <template #trigger>
              <el-button type="primary">选择文件</el-button>
            </template>
            <template #tip>
              <div class="el-upload__tip">
                支持 .doc / .docx / .pdf / .xls / .xlsx / .txt 格式，单个文件不超过50MB
              </div>
            </template>
          </el-upload>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleUpload" :loading="uploading" :disabled="!form.file">
            上传
          </el-button>
          <el-button @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 上传结果 -->
      <el-card v-if="result" style="margin-top: 20px; background: #f8f9fa">
        <template #header>
          <span>📋 上传结果</span>
        </template>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="标题">{{ result.title }}</el-descriptions-item>
          <el-descriptions-item label="库类型">{{ repoTypeMap[result.repoType] || result.repoType }}</el-descriptions-item>
          <el-descriptions-item label="文号">{{ result.docNo || '-' }}</el-descriptions-item>
          <el-descriptions-item label="发布单位">{{ result.publishUnit || '-' }}</el-descriptions-item>
          <el-descriptions-item label="有效性">{{ validityMap[result.validityStatus] || '-' }}</el-descriptions-item>
          <el-descriptions-item label="文件类型">{{ result.fileType || '-' }}</el-descriptions-item>
          <el-descriptions-item label="文件大小" :span="2">{{ formatFileSize(result.fileSize) }}</el-descriptions-item>
        </el-descriptions>
      </el-card>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getDirectoryTree, addDocument } from '@/api/repository'
import { ElMessage } from 'element-plus'

const uploading = ref(false)
const result = ref(null)
const directoryTree = ref([])

const repoTypeMap = {
  1: '法规库',
  2: '资料库',
  3: '裁判文书库',
  4: '案例库',
}

const validityMap = {
  valid: '有效',
  invalid: '失效',
  pending: '待生效',
}

const form = reactive({
  repoType: null,
  directoryId: null,
  title: '',
  docNo: '',
  publishUnit: '',
  publishDate: '',
  validityStatus: 'valid',
  file: null,
})

const handleFileChange = (file) => {
  form.file = file.raw
  if (!form.title) {
    form.title = file.name.replace(/\.[^.]+$/, '')
  }
}

const formatFileSize = (bytes) => {
  if (!bytes) return '-'
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
  return (bytes / (1024 * 1024)).toFixed(1) + ' MB'
}

const handleUpload = async () => {
  if (!form.repoType || !form.file) {
    ElMessage.warning('请选择库类型和上传文件')
    return
  }

  uploading.value = true
  try {
    const docData = {
      repoType: form.repoType,
      title: form.title || form.file.name.replace(/\.[^.]+$/, ''),
      validityStatus: form.validityStatus,
      status: 1,
    }
    if (form.directoryId) docData.directoryId = form.directoryId
    if (form.docNo) docData.docNo = form.docNo
    if (form.publishUnit) docData.publishUnit = form.publishUnit
    if (form.publishDate) docData.publishDate = form.publishDate

    const res = await addDocument(docData)
    result.value = res.data
    ElMessage.success('文档上传成功！')
  } catch (error) {
    console.error('上传失败', error)
  } finally {
    uploading.value = false
  }
}

const resetForm = () => {
  form.repoType = null
  form.directoryId = null
  form.title = ''
  form.docNo = ''
  form.publishUnit = ''
  form.publishDate = ''
  form.validityStatus = 'valid'
  form.file = null
  result.value = null
}

const loadDirectoryTree = async () => {
  try {
    // 默认加载法规库的目录树
    const res = await getDirectoryTree(1)
    directoryTree.value = res.data || []
  } catch (error) {
    // 目录树加载失败不影响上传功能
  }
}

onMounted(() => {
  loadDirectoryTree()
})
</script>

<style lang="scss" scoped>
.upload-page {
  .card-header {
    font-weight: 600;
    font-size: 16px;
  }
}
</style>
