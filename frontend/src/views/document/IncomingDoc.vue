<template>
  <div class="incoming-doc-container">
    <!-- 搜索栏 -->
    <el-card shadow="hover" class="search-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="来文标题">
          <el-input v-model="searchForm.title" placeholder="请输入标题" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item label="来文单位">
          <el-input v-model="searchForm.fromUnit" placeholder="请输入单位" clearable style="width: 160px" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择" clearable style="width: 120px">
            <el-option label="待处理" :value="0" />
            <el-option label="处理中" :value="1" />
            <el-option label="已办结" :value="2" />
            <el-option label="已归档" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="日期范围">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            style="width: 240px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">🔍 搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 操作按钮 -->
    <div class="toolbar">
      <el-button type="primary" @click="handleAdd">➕ 来文登记</el-button>
    </div>

    <!-- 表格 -->
    <el-table :data="tableData" v-loading="loading" stripe border>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="docNo" label="来文文号" width="140" />
      <el-table-column prop="title" label="来文标题" min-width="200" show-overflow-tooltip />
      <el-table-column prop="fromUnit" label="来文单位" width="150" show-overflow-tooltip />
      <el-table-column prop="receiveDate" label="收到日期" width="120" />
      <el-table-column prop="status" label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="getStatusTag(row.status)" size="small">{{ getStatusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="280" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link size="small" @click="handleDetail(row)">详情</el-button>
          <el-button type="warning" link size="small" @click="handleEdit(row)">编辑</el-button>
          <el-button
            v-if="row.status === 0"
            type="success"
            link
            size="small"
            @click="handleChangeStatus(row, 1)"
          >开始处理</el-button>
          <el-button
            v-if="row.status === 1"
            type="success"
            link
            size="small"
            @click="handleChangeStatus(row, 2)"
          >办结</el-button>
          <el-button
            v-if="row.status !== 3"
            type="info"
            link
            size="small"
            @click="handleChangeStatus(row, 3)"
          >归档</el-button>
          <el-button type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="pagination-wrapper">
      <el-pagination
        v-model:current-page="pagination.pageNum"
        v-model:page-size="pagination.pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="pagination.total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="fetchData"
        @current-change="fetchData"
      />
    </div>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑来文登记' : '来文登记'"
      width="600px"
      @close="resetForm"
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="来文文号" prop="docNo">
          <el-input v-model="form.docNo" placeholder="如：X纪发〔2026〕1号" />
        </el-form-item>
        <el-form-item label="来文标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入来文标题" />
        </el-form-item>
        <el-form-item label="来文单位" prop="fromUnit">
          <el-input v-model="form.fromUnit" placeholder="请输入来文单位" />
        </el-form-item>
        <el-form-item label="收到日期" prop="receiveDate">
          <el-date-picker
            v-model="form.receiveDate"
            type="date"
            placeholder="选择日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="事由/主题" prop="subject">
          <el-input v-model="form.subject" type="textarea" :rows="3" placeholder="请输入事由或主题" />
        </el-form-item>
        <el-form-item label="OCR内容">
          <el-input v-model="form.ocrContent" type="textarea" :rows="4" placeholder="OCR识别内容（可选）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 详情对话框 -->
    <el-dialog v-model="detailVisible" title="来文详情" width="700px">
      <el-descriptions :column="2" border v-if="detailData">
        <el-descriptions-item label="来文文号">{{ detailData.docNo || '—' }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusTag(detailData.status)">{{ getStatusText(detailData.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="来文标题" :span="2">{{ detailData.title || '—' }}</el-descriptions-item>
        <el-descriptions-item label="来文单位">{{ detailData.fromUnit || '—' }}</el-descriptions-item>
        <el-descriptions-item label="收到日期">{{ detailData.receiveDate || '—' }}</el-descriptions-item>
        <el-descriptions-item label="事由/主题" :span="2">{{ detailData.subject || '—' }}</el-descriptions-item>
        <el-descriptions-item label="OCR内容" :span="2">
          <div style="max-height: 200px; overflow-y: auto; white-space: pre-wrap;">
            {{ detailData.ocrContent || '—' }}
          </div>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ detailData.createTime || '—' }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ detailData.updateTime || '—' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { incomingDocApi } from '@/api/incoming-doc'

const loading = ref(false)
const tableData = ref([])
const dialogVisible = ref(false)
const detailVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)
const dateRange = ref([])
const detailData = ref(null)

const searchForm = reactive({
  title: '',
  fromUnit: '',
  status: null,
})

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0,
})

const form = reactive({
  id: null,
  docNo: '',
  title: '',
  fromUnit: '',
  receiveDate: '',
  subject: '',
  ocrContent: '',
  status: 0,
})

const rules = {
  title: [{ required: true, message: '请输入来文标题', trigger: 'blur' }],
  fromUnit: [{ required: true, message: '请输入来文单位', trigger: 'blur' }],
  receiveDate: [{ required: true, message: '请选择收到日期', trigger: 'change' }],
}

const statusMap = {
  0: { text: '待处理', tag: 'info' },
  1: { text: '处理中', tag: 'warning' },
  2: { text: '已办结', tag: 'success' },
  3: { text: '已归档', tag: 'info' },
}

const getStatusText = (status) => statusMap[status]?.text || '未知'
const getStatusTag = (status) => statusMap[status]?.tag || 'info'

const fetchData = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      title: searchForm.title || undefined,
      fromUnit: searchForm.fromUnit || undefined,
      status: searchForm.status,
    }
    if (dateRange.value && dateRange.value.length === 2) {
      params.startDate = dateRange.value[0]
      params.endDate = dateRange.value[1]
    }
    const res = await incomingDocApi.getPage(params)
    if (res.code === 200) {
      tableData.value = res.data.list || []
      pagination.total = res.data.total || 0
    }
  } catch (e) {
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.pageNum = 1
  fetchData()
}

const handleReset = () => {
  searchForm.title = ''
  searchForm.fromUnit = ''
  searchForm.status = null
  dateRange.value = []
  pagination.pageNum = 1
  fetchData()
}

const handleAdd = () => {
  isEdit.value = false
  resetForm()
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  Object.assign(form, {
    id: row.id,
    docNo: row.docNo,
    title: row.title,
    fromUnit: row.fromUnit,
    receiveDate: row.receiveDate,
    subject: row.subject,
    ocrContent: row.ocrContent,
    status: row.status,
  })
  dialogVisible.value = true
}

const handleDetail = async (row) => {
  try {
    const res = await incomingDocApi.getDetail(row.id)
    if (res.code === 200) {
      detailData.value = res.data
      detailVisible.value = true
    }
  } catch (e) {
    ElMessage.error('加载详情失败')
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    try {
      if (isEdit.value) {
        await incomingDocApi.update({ ...form })
        ElMessage.success('更新成功')
      } else {
        await incomingDocApi.create({ ...form })
        ElMessage.success('登记成功')
      }
      dialogVisible.value = false
      fetchData()
    } catch (e) {
      ElMessage.error(isEdit.value ? '更新失败' : '登记失败')
    }
  })
}

const handleChangeStatus = async (row, status) => {
  try {
    await incomingDocApi.changeStatus(row.id, status)
    ElMessage.success('状态变更成功')
    fetchData()
  } catch (e) {
    ElMessage.error('状态变更失败')
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确认删除「${row.title}」？`, '确认删除', { type: 'warning' })
    await incomingDocApi.delete(row.id)
    ElMessage.success('删除成功')
    fetchData()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('删除失败')
  }
}

const resetForm = () => {
  Object.assign(form, {
    id: null,
    docNo: '',
    title: '',
    fromUnit: '',
    receiveDate: '',
    subject: '',
    ocrContent: '',
    status: 0,
  })
  if (formRef.value) formRef.value.clearValidate()
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.incoming-doc-container {
  padding: 16px;
}
.search-card {
  margin-bottom: 16px;
}
.toolbar {
  margin-bottom: 16px;
}
.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
