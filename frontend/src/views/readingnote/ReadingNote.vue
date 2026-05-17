<template>
  <div class="reading-note-container">
    <!-- 搜索栏 -->
    <el-card shadow="hover" class="search-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="案件ID">
          <el-input v-model="searchForm.caseId" placeholder="请输入案件ID" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item label="笔记类型">
          <el-select v-model="searchForm.noteType" placeholder="请选择" clearable style="width: 150px">
            <el-option label="阅卷摘要" :value="1" />
            <el-option label="证据分析" :value="2" />
            <el-option label="法律适用" :value="3" />
            <el-option label="审理意见" :value="4" />
            <el-option label="其他" :value="5" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">🔍 搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 操作按钮 -->
    <div class="toolbar">
      <el-button type="primary" @click="handleAdd">➕ 新增笔记</el-button>
      <el-button type="info" @click="showSharedNotes = true">👥 查看共享笔记</el-button>
    </div>

    <!-- 表格 -->
    <el-table :data="tableData" v-loading="loading" stripe border>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="caseId" label="案件ID" width="140" />
      <el-table-column prop="title" label="标题" min-width="180" show-overflow-tooltip />
      <el-table-column prop="noteTypeText" label="类型" width="110">
        <template #default="{ row }">
          <el-tag :type="getNoteTypeTag(row.noteType)">{{ getNoteTypeText(row.noteType) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="tags" label="标签" width="150" show-overflow-tooltip />
      <el-table-column label="共享" width="70">
        <template #default="{ row }">
          <el-tag v-if="row.isShared === 1" type="success">是</el-tag>
          <el-tag v-else type="info">否</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="170" />
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="handleDetail(row)">详情</el-button>
          <el-button size="small" type="primary" @click="handleEdit(row)">编辑</el-button>
          <el-button size="small" :type="row.isShared === 1 ? 'warning' : 'success'"
            @click="handleToggleShare(row)">
            {{ row.isShared === 1 ? '取消共享' : '共享' }}
          </el-button>
          <el-popconfirm title="确定删除此笔记？" @confirm="handleDelete(row.id)">
            <template #reference>
              <el-button size="small" type="danger">删除</el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="pagination-container">
      <el-pagination
        v-model:current-page="pagination.pageNum"
        v-model:page-size="pagination.pageSize"
        :total="pagination.total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="loadData"
        @current-change="loadData"
      />
    </div>

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="700px" @close="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="案件ID" prop="caseId">
          <el-input v-model="form.caseId" placeholder="请输入案件ID" />
        </el-form-item>
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入笔记标题" />
        </el-form-item>
        <el-form-item label="笔记类型" prop="noteType">
          <el-select v-model="form.noteType" placeholder="请选择类型" style="width: 100%">
            <el-option label="阅卷摘要" :value="1" />
            <el-option label="证据分析" :value="2" />
            <el-option label="法律适用" :value="3" />
            <el-option label="审理意见" :value="4" />
            <el-option label="其他" :value="5" />
          </el-select>
        </el-form-item>
        <el-form-item label="标签">
          <el-input v-model="form.tags" placeholder="多个标签用逗号分隔" />
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input v-model="form.content" type="textarea" :rows="8" placeholder="请输入笔记内容" />
        </el-form-item>
        <el-form-item label="是否共享">
          <el-switch v-model="form.isShared" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 详情对话框 -->
    <el-dialog v-model="detailVisible" title="笔记详情" width="700px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="ID">{{ detailData.id }}</el-descriptions-item>
        <el-descriptions-item label="案件ID">{{ detailData.caseId }}</el-descriptions-item>
        <el-descriptions-item label="标题" :span="2">{{ detailData.title }}</el-descriptions-item>
        <el-descriptions-item label="类型">
          <el-tag :type="getNoteTypeTag(detailData.noteType)">{{ getNoteTypeText(detailData.noteType) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="标签">{{ detailData.tags }}</el-descriptions-item>
        <el-descriptions-item label="共享">
          <el-tag v-if="detailData.isShared === 1" type="success">是</el-tag>
          <el-tag v-else type="info">否</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ detailData.createTime }}</el-descriptions-item>
        <el-descriptions-item label="更新时间" :span="2">{{ detailData.updateTime }}</el-descriptions-item>
        <el-descriptions-item label="内容" :span="2">
          <div style="white-space: pre-wrap; max-height: 400px; overflow-y: auto">{{ detailData.content }}</div>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <!-- 共享笔记列表 -->
    <el-dialog v-model="showSharedNotes" title="共享笔记" width="800px">
      <el-form inline style="margin-bottom: 16px">
        <el-form-item label="案件ID">
          <el-input v-model="sharedCaseId" placeholder="输入案件ID查看共享笔记" style="width: 200px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadSharedNotes">加载</el-button>
        </el-form-item>
      </el-form>
      <el-table :data="sharedNotesData" v-loading="sharedLoading" stripe border>
        <el-table-column prop="title" label="标题" min-width="160" />
        <el-table-column prop="caseId" label="案件ID" width="140" />
        <el-table-column prop="noteTypeText" label="类型" width="110">
          <template #default="{ row }">
            <el-tag :type="getNoteTypeTag(row.noteType)">{{ getNoteTypeText(row.noteType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="tags" label="标签" width="120" show-overflow-tooltip />
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="80">
          <template #default="{ row }">
            <el-button size="small" @click="handleDetail(row)">查看</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { readingNoteApi } from '@/api/readingnote'

const loading = ref(false)
const tableData = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('新增笔记')
const detailVisible = ref(false)
const detailData = ref({})
const showSharedNotes = ref(false)
const sharedNotesData = ref([])
const sharedLoading = ref(false)
const sharedCaseId = ref('')
const formRef = ref(null)

const searchForm = reactive({
  caseId: '',
  noteType: null,
})

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0,
})

const form = reactive({
  id: null,
  caseId: '',
  title: '',
  content: '',
  tags: '',
  noteType: 1,
  isShared: 0,
})

const rules = {
  caseId: [{ required: true, message: '请输入案件ID', trigger: 'blur' }],
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入内容', trigger: 'blur' }],
}

const NOTE_TYPE_MAP = {
  1: { text: '阅卷摘要', tag: 'primary' },
  2: { text: '证据分析', tag: 'success' },
  3: { text: '法律适用', tag: 'warning' },
  4: { text: '审理意见', tag: 'danger' },
  5: { text: '其他', tag: 'info' },
}

function getNoteTypeText(type) {
  return NOTE_TYPE_MAP[type]?.text || '未知'
}

function getNoteTypeTag(type) {
  return NOTE_TYPE_MAP[type]?.tag || 'info'
}

async function loadData() {
  loading.value = true
  try {
    const params = {
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
    }
    if (searchForm.caseId) params.caseId = searchForm.caseId
    if (searchForm.noteType !== null) params.noteType = searchForm.noteType

    const res = await readingNoteApi.getPage(params)
    if (res.code === 200 && res.data) {
      tableData.value = res.data.records || []
      pagination.total = res.data.total || 0
    }
  } catch (err) {
    ElMessage.error('加载笔记列表失败')
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pagination.pageNum = 1
  loadData()
}

function handleReset() {
  searchForm.caseId = ''
  searchForm.noteType = null
  pagination.pageNum = 1
  loadData()
}

function handleAdd() {
  dialogTitle.value = '新增笔记'
  resetForm()
  dialogVisible.value = true
}

function handleEdit(row) {
  dialogTitle.value = '编辑笔记'
  Object.assign(form, {
    id: row.id,
    caseId: row.caseId,
    title: row.title,
    content: row.content,
    tags: row.tags,
    noteType: row.noteType,
    isShared: row.isShared,
  })
  dialogVisible.value = true
}

function handleDetail(row) {
  detailData.value = row
  detailVisible.value = true
}

async function handleDelete(id) {
  try {
    const res = await readingNoteApi.delete(id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      loadData()
    }
  } catch (err) {
    ElMessage.error('删除失败')
  }
}

async function handleToggleShare(row) {
  const newStatus = row.isShared === 1 ? 0 : 1
  try {
    const res = await readingNoteApi.toggleShared(row.id, newStatus)
    if (res.code === 200) {
      ElMessage.success(newStatus === 1 ? '已共享' : '已取消共享')
      loadData()
    }
  } catch (err) {
    ElMessage.error('操作失败')
  }
}

async function handleSubmit() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    try {
      if (form.id) {
        const res = await readingNoteApi.update({ ...form })
        if (res.code === 200) {
          ElMessage.success('更新成功')
          dialogVisible.value = false
          loadData()
        }
      } else {
        const res = await readingNoteApi.create({ ...form })
        if (res.code === 200) {
          ElMessage.success('创建成功')
          dialogVisible.value = false
          loadData()
        }
      }
    } catch (err) {
      ElMessage.error('操作失败')
    }
  })
}

function resetForm() {
  form.id = null
  form.caseId = ''
  form.title = ''
  form.content = ''
  form.tags = ''
  form.noteType = 1
  form.isShared = 0
  if (formRef.value) formRef.value.resetFields()
}

async function loadSharedNotes() {
  if (!sharedCaseId.value) {
    ElMessage.warning('请输入案件ID')
    return
  }
  sharedLoading.value = true
  try {
    const res = await readingNoteApi.getSharedNotes(sharedCaseId.value)
    if (res.code === 200) {
      sharedNotesData.value = res.data || []
    }
  } catch (err) {
    ElMessage.error('加载共享笔记失败')
  } finally {
    sharedLoading.value = false
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.reading-note-container {
  padding: 20px;
}
.search-card {
  margin-bottom: 16px;
}
.toolbar {
  margin-bottom: 16px;
}
.pagination-container {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
