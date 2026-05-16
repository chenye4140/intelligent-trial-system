<template>
  <div class="directory-container">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>目录管理</span>
          <div class="header-actions">
            <el-button type="success" size="small" @click="handleImport">
              <el-icon><Upload /></el-icon>
              Excel 导入
            </el-button>
            <el-button type="warning" size="small" @click="handleExport">
              <el-icon><Download /></el-icon>
              Excel 导出
            </el-button>
            <el-button type="primary" size="small" @click="addDirectory(null)">
              <el-icon><Plus /></el-icon>
              新增目录
            </el-button>
          </div>
        </div>
      </template>

      <!-- Repo Type Tabs -->
      <el-tabs v-model="activeRepoType" @tab-change="fetchDirectoryTree">
        <el-tab-pane label="法规库" :name="1" />
        <el-tab-pane label="资料库" :name="2" />
        <el-tab-pane label="裁判文书库" :name="3" />
        <el-tab-pane label="案例库" :name="4" />
      </el-tabs>

      <!-- Directory Tree Table -->
      <el-table
        :data="directoryTree"
        row-key="id"
        :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
        default-expand-all
        stripe
        style="width: 100%"
      >
        <el-table-column prop="name" label="目录名称" min-width="200" />
        <el-table-column prop="sort" label="排序" width="80" />
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="addDirectory(row.id)">新增子目录</el-button>
            <el-button type="warning" link size="small" @click="editDirectory(row)">编辑</el-button>
            <el-button type="success" link size="small" @click="moveDirectory(row)">移动</el-button>
            <el-button type="danger" link size="small" @click="deleteDirectory(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- Directory Dialog -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form :model="directoryForm" label-width="100px">
        <el-form-item label="目录名称">
          <el-input v-model="directoryForm.name" placeholder="请输入目录名称" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="directoryForm.sort" :min="0" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="directoryForm.description" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveDirectory">保存</el-button>
      </template>
    </el-dialog>

    <!-- Move Dialog -->
    <el-dialog v-model="moveDialogVisible" title="移动目录" width="500px">
      <el-form label-width="100px">
        <el-form-item label="目标目录">
          <el-tree-select
            v-model="moveTargetId"
            :data="directoryTree"
            :props="{ label: 'name', children: 'children' }"
            node-key="id"
            check-strictly
            placeholder="选择目标目录"
            clearable
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="moveDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveMove">确定</el-button>
      </template>
    </el-dialog>

    <!-- Import Dialog -->
    <el-dialog v-model="importDialogVisible" title="Excel 导入" width="500px">
      <el-upload
        drag
        :action="'/api/repository/directory/import/' + activeRepoType"
        :headers="uploadHeaders"
        :on-success="handleImportSuccess"
        :on-error="handleImportError"
        :before-upload="beforeImportUpload"
        accept=".xlsx,.xls"
      >
        <el-icon :size="48" color="#409eff"><UploadFilled /></el-icon>
        <div class="el-upload__text">拖拽文件到此处或 <em>点击上传</em></div>
        <template #tip>
          <div class="el-upload__tip">支持 .xlsx/.xls 格式</div>
        </template>
      </el-upload>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getDirectoryTree, addDirectory as addDirApi, updateDirectory as updateDirApi, deleteDirectory as deleteDirApi, importDirectoryExcel, exportDirectoryExcel } from '@/api/repository'

const activeRepoType = ref(1)
const directoryTree = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('')
const moveDialogVisible = ref(false)
const importDialogVisible = ref(false)
const moveTargetId = ref(null)

const directoryForm = reactive({
  id: null,
  name: '',
  sort: 0,
  description: '',
  parentId: null
})

const uploadHeaders = computed(() => ({
  Authorization: `Bearer ${localStorage.getItem('token') || ''}`
}))

const fetchDirectoryTree = async () => {
  try {
    const res = await getDirectoryTree(activeRepoType.value)
    if (res.data) {
      directoryTree.value = res.data
    }
  } catch (e) {
    // handled by interceptor
  }
}

const addDirectory = (parentId) => {
  Object.assign(directoryForm, {
    id: null,
    name: '',
    sort: 0,
    description: '',
    parentId: parentId
  })
  dialogTitle.value = parentId ? '新增子目录' : '新增目录'
  dialogVisible.value = true
}

const editDirectory = (row) => {
  Object.assign(directoryForm, { ...row })
  dialogTitle.value = '编辑目录'
  dialogVisible.value = true
}

const saveDirectory = async () => {
  try {
    if (directoryForm.id) {
      await updateDirApi(directoryForm.id, { ...directoryForm })
      ElMessage.success('编辑成功')
    } else {
      await addDirApi({ ...directoryForm, repoType: activeRepoType.value })
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    fetchDirectoryTree()
  } catch (e) {
    // handled by interceptor
  }
}

const moveDirectory = (row) => {
  moveTargetId.value = null
  directoryForm.id = row.id
  moveDialogVisible.value = true
}

const saveMove = async () => {
  try {
    await updateDirApi(directoryForm.id, { parentId: moveTargetId.value })
    ElMessage.success('移动成功')
    moveDialogVisible.value = false
    fetchDirectoryTree()
  } catch (e) {
    // handled by interceptor
  }
}

const deleteDirectory = async (row) => {
  try {
    await ElMessageBox.confirm(`确定删除目录 "${row.name}" 吗？`, '提示', { type: 'warning' })
    await deleteDirApi(row.id)
    ElMessage.success('删除成功')
    fetchDirectoryTree()
  } catch (e) {
    if (e !== 'cancel') {}
  }
}

const handleImport = () => {
  importDialogVisible.value = true
}

const beforeImportUpload = (file) => {
  const ext = file.name.split('.').pop().toLowerCase()
  if (!['xlsx', 'xls'].includes(ext)) {
    ElMessage.error('仅支持 Excel 文件')
    return false
  }
  return true
}

const handleImportSuccess = () => {
  ElMessage.success('导入成功')
  importDialogVisible.value = false
  fetchDirectoryTree()
}

const handleImportError = () => {
  ElMessage.error('导入失败')
}

const handleExport = async () => {
  try {
    const res = await exportDirectoryExcel(activeRepoType.value)
    const blob = new Blob([res], { type: 'application/vnd.ms-excel' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `目录导出_${activeRepoType.value}.xlsx`
    link.click()
    window.URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } catch (e) {
    // handled by interceptor
  }
}

fetchDirectoryTree()
</script>

<style scoped>
.directory-container {
  padding: 4px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.header-actions {
  display: flex;
  gap: 8px;
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
</style>
