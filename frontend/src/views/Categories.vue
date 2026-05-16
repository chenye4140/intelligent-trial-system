<template>
  <div class="categories-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>📁 目录管理</span>
          <div>
            <el-button type="success" size="small" @click="handleImport">
              <el-icon><Upload /></el-icon>
              导入
            </el-button>
            <el-button type="warning" size="small" @click="handleExport">
              <el-icon><Download /></el-icon>
              导出
            </el-button>
            <el-button type="primary" size="small" @click="showAddDialog(null)">
              <el-icon><Plus /></el-icon>
              新增根目录
            </el-button>
          </div>
        </div>
      </template>

      <!-- 库类型切换 -->
      <el-tabs v-model="activeRepoType" @tab-click="loadCategories">
        <el-tab-pane label="法规库" :name="1" />
        <el-tab-pane label="资料库" :name="2" />
        <el-tab-pane label="裁判文书库" :name="3" />
        <el-tab-pane label="案例库" :name="4" />
      </el-tabs>

      <!-- 目录树 -->
      <el-tree
        :data="treeData"
        :props="{ label: 'name', children: 'children' }"
        node-key="id"
        default-expand-all
        style="margin-top: 20px"
      >
        <template #default="{ node, data }">
          <span class="tree-node">
            <span>
              <el-icon><FolderOpened /></el-icon>
              {{ node.label }}
            </span>
            <span class="tree-node-actions">
              <el-button type="primary" link size="small" @click="showAddDialog(data)">添加子目录</el-button>
              <el-button type="warning" link size="small" @click="showEditDialog(data)">编辑</el-button>
              <el-button type="danger" link size="small" @click="handleDelete(data)">删除</el-button>
            </span>
          </span>
        </template>
      </el-tree>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="450px">
      <el-form :model="dialogForm" label-width="100px" :rules="dialogRules" ref="dialogFormRef">
        <el-form-item label="目录名称" prop="name">
          <el-input v-model="dialogForm.name" placeholder="请输入目录名称" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="dialogForm.sort" :min="0" :max="9999" />
        </el-form-item>
        <el-form-item label="权限范围">
          <el-select v-model="dialogForm.permissionScope" style="width: 100%">
            <el-option label="公开" value="public" />
            <el-option label="内部" value="internal" />
            <el-option label="受限" value="restricted" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="dialogForm.status" :active-value="1" :inactive-value="0"
            active-text="启用" inactive-text="禁用" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave" :loading="saving">保存</el-button>
      </template>
    </el-dialog>

    <!-- 导入对话框 -->
    <el-dialog v-model="importDialogVisible" title="导入目录" width="400px">
      <el-upload
        ref="importUploadRef"
        :auto-upload="false"
        :limit="1"
        accept=".xlsx,.xls"
        :on-change="handleImportFileChange"
      >
        <template #trigger>
          <el-button type="primary">选择Excel文件</el-button>
        </template>
        <template #tip>
          <div class="el-upload__tip">仅支持 .xlsx/.xls 格式</div>
        </template>
      </el-upload>
      <template #footer>
        <el-button @click="importDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="importing" @click="handleImportSubmit">导入</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getDirectoryTree,
  addDirectory as addDirApi,
  updateDirectory as updateDirApi,
  deleteDirectory as deleteDirApi,
  importDirectoryExcel,
  exportDirectoryExcel,
} from '@/api/repository'

const activeRepoType = ref(1)
const treeData = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('新增目录')
const dialogMode = ref('add')
const saving = ref(false)
const dialogFormRef = ref()

const dialogRules = {
  name: [{ required: true, message: '请输入目录名称', trigger: 'blur' }],
}

const dialogForm = reactive({
  id: null,
  name: '',
  parentId: 0,
  sort: 0,
  permissionScope: 'public',
  status: 1,
})

// 导入相关
const importDialogVisible = ref(false)
const importing = ref(false)
const importFile = ref(null)

const loadCategories = async () => {
  try {
    const res = await getDirectoryTree(activeRepoType.value)
    treeData.value = res.data || []
  } catch (error) {
    console.error('加载目录树失败', error)
    ElMessage.error('加载目录树失败')
  }
}

const showAddDialog = (parent) => {
  dialogMode.value = 'add'
  dialogTitle.value = parent ? `添加子目录到「${parent.name}」` : '新增根目录'
  dialogForm.id = null
  dialogForm.name = ''
  dialogForm.parentId = parent ? parent.id : 0
  dialogForm.sort = 0
  dialogForm.permissionScope = 'public'
  dialogForm.status = 1
  dialogVisible.value = true
}

const showEditDialog = (data) => {
  dialogMode.value = 'edit'
  dialogTitle.value = '编辑目录'
  dialogForm.id = data.id
  dialogForm.name = data.name
  dialogForm.parentId = data.parentId
  dialogForm.sort = data.sort || 0
  dialogForm.permissionScope = data.permissionScope || 'public'
  dialogForm.status = data.status !== undefined ? data.status : 1
  dialogVisible.value = true
}

const handleSave = async () => {
  if (!dialogFormRef.value) return
  await dialogFormRef.value.validate(async (valid) => {
    if (!valid) return

    saving.value = true
    try {
      const payload = {
        repoType: activeRepoType.value,
        name: dialogForm.name,
        parentId: dialogForm.parentId,
        sort: dialogForm.sort,
        permissionScope: dialogForm.permissionScope,
        status: dialogForm.status,
      }

      if (dialogMode.value === 'add') {
        await addDirApi(payload)
        ElMessage.success('新增成功')
      } else {
        await updateDirApi(dialogForm.id, payload)
        ElMessage.success('编辑成功')
      }
      dialogVisible.value = false
      loadCategories()
    } catch (error) {
      console.error('保存失败', error)
    } finally {
      saving.value = false
    }
  })
}

const handleDelete = async (data) => {
  try {
    await ElMessageBox.confirm(`确定删除目录「${data.name}」及其子目录吗？`, '提示', {
      type: 'warning',
    })
    await deleteDirApi(data.id)
    ElMessage.success('删除成功')
    loadCategories()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败', error)
    }
  }
}

const handleImport = () => {
  importFile.value = null
  importDialogVisible.value = true
}

const handleImportFileChange = (file) => {
  importFile.value = file.raw
}

const handleImportSubmit = async () => {
  if (!importFile.value) {
    ElMessage.warning('请选择要导入的文件')
    return
  }
  importing.value = true
  try {
    const formData = new FormData()
    formData.append('file', importFile.value)
    const res = await importDirectoryExcel(activeRepoType.value, formData)
    ElMessage.success(res.data?.msg || `成功导入 ${res.data || 0} 条目录`)
    importDialogVisible.value = false
    loadCategories()
  } catch (error) {
    console.error('导入失败', error)
  } finally {
    importing.value = false
  }
}

const handleExport = async () => {
  try {
    const res = await exportDirectoryExcel(activeRepoType.value)
    const blob = new Blob([res])
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `directory_export_${activeRepoType.value}.xlsx`
    link.click()
    window.URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } catch (error) {
    console.error('导出失败', error)
  }
}

onMounted(() => {
  loadCategories()
})
</script>

<style lang="scss" scoped>
.categories-page {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    font-weight: 600;
    font-size: 16px;
  }

  .tree-node {
    display: flex;
    justify-content: space-between;
    align-items: center;
    width: 100%;
    padding-right: 10px;

    .tree-node-actions {
      display: none;
    }

    &:hover .tree-node-actions {
      display: inline-flex;
      gap: 4px;
    }
  }
}
</style>
