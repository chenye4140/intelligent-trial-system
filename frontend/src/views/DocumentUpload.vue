<template>
  <div class="upload-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>📤 文档上传与解析</span>
        </div>
      </template>

      <el-form :model="form" label-width="100px" style="max-width: 600px">
        <el-form-item label="文档类型" required>
          <el-select v-model="form.docType" placeholder="请选择文档类型" style="width: 100%">
            <el-option label="法规库" value="regulation" />
            <el-option label="资料库" value="material" />
            <el-option label="裁判文书库" value="judgment" />
            <el-option label="案例库" value="case" />
          </el-select>
        </el-form-item>

        <el-form-item label="所属目录">
          <el-input v-model="form.categoryPath" placeholder="留空表示根目录" />
        </el-form-item>

        <el-form-item label="密级">
          <el-select v-model="form.securityLevel" style="width: 100%">
            <el-option label="绝密" value="top_secret" />
            <el-option label="机密" value="secret" />
            <el-option label="秘密" value="confidential" />
            <el-option label="内部" value="internal" />
            <el-option label="公开" value="public" />
          </el-select>
        </el-form-item>

        <el-form-item label="上传文件" required>
          <el-upload
            ref="uploadRef"
            :auto-upload="false"
            :limit="1"
            accept=".doc,.docx,.pdf"
            :on-change="handleFileChange"
          >
            <template #trigger>
              <el-button type="primary">选择文件</el-button>
            </template>
            <template #tip>
              <div class="el-upload__tip">
                支持 .doc / .docx / .pdf 格式，单个文件不超过50MB
              </div>
            </template>
          </el-upload>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleUpload" :loading="uploading" :disabled="!form.file">
            上传并解析
          </el-button>
          <el-button @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 上传结果 -->
      <el-card v-if="result" style="margin-top: 20px; background: #f8f9fa">
        <template #header>
          <span>📋 解析结果</span>
        </template>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="标题">{{ result.title }}</el-descriptions-item>
          <el-descriptions-item label="类型">{{ result.docType }}</el-descriptions-item>
          <el-descriptions-item label="解析状态">
            <el-tag :type="result.parseStatus === 'parsed' ? 'success' : 'danger'">
              {{ result.parseStatus }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="内容摘要">
            <div style="max-height: 150px; overflow-y: auto; white-space: pre-wrap">
              {{ result.contentSummary || '无' }}
            </div>
          </el-descriptions-item>
        </el-descriptions>
      </el-card>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { documentApi } from '@/api'
import { ElMessage } from 'element-plus'

const uploading = ref(false)
const result = ref(null)

const form = reactive({
  docType: '',
  categoryPath: '',
  securityLevel: 'internal',
  file: null,
})

const handleFileChange = (file) => {
  form.file = file.raw
}

const handleUpload = async () => {
  if (!form.docType || !form.file) {
    ElMessage.warning('请选择文档类型和上传文件')
    return
  }

  uploading.value = true
  try {
    const res = await documentApi.upload(
      form.file,
      form.docType,
      form.categoryPath,
      form.securityLevel
    )
    result.value = res.data
    ElMessage.success('文档上传解析成功！')
  } catch (error) {
    console.error('上传失败', error)
  } finally {
    uploading.value = false
  }
}

const resetForm = () => {
  form.docType = ''
  form.categoryPath = ''
  form.securityLevel = 'internal'
  form.file = null
  result.value = null
}
</script>

<style lang="scss" scoped>
.upload-page {
  .card-header {
    font-weight: 600;
    font-size: 16px;
  }
}
</style>
