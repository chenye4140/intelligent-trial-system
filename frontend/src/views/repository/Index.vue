<template>
  <div class="repository-container">
    <el-row :gutter="16" style="height: calc(100vh - 140px)">
      <!-- Left: Directory Tree -->
      <el-col :span="5" class="tree-col">
        <el-card shadow="hover" class="tree-card">
          <template #header>
            <div class="tree-header">
              <span>目录树</span>
              <el-button type="primary" link size="small" @click="addDirectory(null)">
                <el-icon><Plus /></el-icon>
              </el-button>
            </div>
          </template>
          <div class="tree-wrapper" @contextmenu.prevent>
            <el-tree
              ref="treeRef"
              :data="directoryTree"
              :props="{ label: 'name', children: 'children' }"
              node-key="id"
              default-expand-all
              highlight-current
              @node-contextmenu="handleContextMenu"
            >
              <template #default="{ node, data }">
                <span class="tree-node">
                  <el-icon><FolderOpened /></el-icon>
                  <span>{{ node.label }}</span>
                </span>
              </template>
            </el-tree>
          </div>
        </el-card>
      </el-col>

      <!-- Right: Document List -->
      <el-col :span="19" class="content-col">
        <el-card shadow="hover">
          <template #header>
            <div class="content-header">
              <span>文档列表</span>
              <el-button type="primary" size="small" @click="addDocument">
                <el-icon><Plus /></el-icon>
                新增文档
              </el-button>
            </div>
          </template>

          <!-- Search -->
          <el-form :inline="true" :model="searchForm" class="search-form">
            <el-form-item label="关键词">
              <el-input v-model="searchForm.keyword" placeholder="请输入关键词" clearable />
            </el-form-item>
            <el-form-item label="库类型">
              <el-select v-model="searchForm.repoType" placeholder="全部" clearable>
                <el-option label="法规库" :value="1" />
                <el-option label="资料库" :value="2" />
                <el-option label="裁判文书库" :value="3" />
                <el-option label="案例库" :value="4" />
              </el-select>
            </el-form-item>
            <el-form-item label="有效性">
              <el-select v-model="searchForm.validity" placeholder="全部" clearable>
                <el-option label="有效" value="valid" />
                <el-option label="失效" value="invalid" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleSearch">搜索</el-button>
              <el-button @click="resetSearch">重置</el-button>
            </el-form-item>
          </el-form>

          <!-- Table -->
          <el-table :data="documents" v-loading="loading" stripe style="width: 100%">
            <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
            <el-table-column prop="docNumber" label="文号" width="150" />
            <el-table-column prop="publishOrg" label="发布单位" width="150" show-overflow-tooltip />
            <el-table-column prop="publishDate" label="发布日期" width="120" />
            <el-table-column prop="securityLevel" label="密级" width="80">
              <template #default="{ row }">
                <el-tag v-if="row.securityLevel === 'secret'" type="danger" size="small">秘密</el-tag>
                <el-tag v-else-if="row.securityLevel === 'confidential'" type="warning" size="small">机密</el-tag>
                <el-tag v-else type="success" size="small">公开</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="180" fixed="right">
              <template #default="{ row }">
                <el-button type="primary" link size="small" @click="previewDocument(row)">预览</el-button>
                <el-button type="success" link size="small" @click="downloadDocument(row)">下载</el-button>
                <el-button type="warning" link size="small" @click="editDocument(row)">编辑</el-button>
                <el-button type="danger" link size="small" @click="deleteDocument(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>

          <el-pagination
            v-model:current-page="pagination.page"
            v-model:page-size="pagination.pageSize"
            :total="pagination.total"
            :page-sizes="[10, 20, 50]"
            layout="total, sizes, prev, pager, next"
            @current-change="fetchDocuments"
            @size-change="fetchDocuments"
            style="margin-top: 16px; justify-content: flex-end"
          />
        </el-card>
      </el-col>
    </el-row>

    <!-- Context Menu -->
    <div v-show="contextMenuVisible" class="context-menu" :style="{ left: contextMenuX + 'px', top: contextMenuY + 'px' }">
      <el-dropdown trigger="click" placement="bottom-start" :teleported="false" @command="handleContextMenuCommand" @visible-change="onContextMenuVisibleChange">
        <el-button text style="padding: 0; background: transparent" />
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="add"><el-icon><Plus /></el-icon> 新增子目录</el-dropdown-item>
            <el-dropdown-item command="edit"><el-icon><Edit /></el-icon> 编辑目录</el-dropdown-item>
            <el-dropdown-item command="delete" divided><el-icon><Delete /></el-icon> 删除目录</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>

    <!-- Directory Dialog -->
    <el-dialog v-model="directoryDialogVisible" :title="directoryDialogTitle" width="500px">
      <el-form :model="directoryForm" label-width="80px">
        <el-form-item label="目录名称">
          <el-input v-model="directoryForm.name" placeholder="请输入目录名称" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="directoryForm.sort" :min="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="directoryDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveDirectory">保存</el-button>
      </template>
    </el-dialog>

    <!-- Document Dialog -->
    <el-dialog v-model="documentDialogVisible" :title="documentDialogTitle" width="600px">
      <el-form :model="documentForm" label-width="100px">
        <el-form-item label="标题">
          <el-input v-model="documentForm.title" placeholder="请输入标题" />
        </el-form-item>
        <el-form-item label="文号">
          <el-input v-model="documentForm.docNumber" placeholder="请输入文号" />
        </el-form-item>
        <el-form-item label="发布单位">
          <el-input v-model="documentForm.publishOrg" placeholder="请输入发布单位" />
        </el-form-item>
        <el-form-item label="发布日期">
          <el-date-picker v-model="documentForm.publishDate" type="date" />
        </el-form-item>
        <el-form-item label="密级">
          <el-select v-model="documentForm.securityLevel">
            <el-option label="公开" value="public" />
            <el-option label="秘密" value="secret" />
            <el-option label="机密" value="confidential" />
          </el-select>
        </el-form-item>
        <el-form-item label="内容">
          <el-input v-model="documentForm.content" type="textarea" :rows="6" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="documentDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveDocument">保存</el-button>
      </template>
    </el-dialog>

    <!-- Preview Dialog -->
    <el-dialog v-model="previewVisible" title="文档预览" width="80%" top="5vh">
      <div class="preview-content">
        <h3>{{ previewDoc?.title }}</h3>
        <p><strong>文号：</strong>{{ previewDoc?.docNumber }}</p>
        <p><strong>发布单位：</strong>{{ previewDoc?.publishOrg }}</p>
        <p><strong>发布日期：</strong>{{ previewDoc?.publishDate }}</p>
        <el-divider />
        <pre>{{ previewDoc?.content }}</pre>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getDirectoryTree, addDirectory as addDirApi, updateDirectory as updateDirApi, deleteDirectory as deleteDirApi, searchDocuments, addDocument as addDocApi, updateDocument as updateDocApi, deleteDocument as deleteDocApi, downloadDocument as downloadDocApi } from '@/api/repository'

const directoryTree = ref([])
const documents = ref([])
const loading = ref(false)
const treeRef = ref()

const searchForm = reactive({
  keyword: '',
  repoType: null,
  validity: ''
})

const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0
})

// Context menu
const contextMenuVisible = ref(false)
const contextMenuX = ref(0)
const contextMenuY = ref(0)
const contextMenuNode = ref(null)

// Directory dialog
const directoryDialogVisible = ref(false)
const directoryDialogTitle = ref('')
const directoryForm = reactive({
  id: null,
  name: '',
  sort: 0,
  parentId: null
})

// Document dialog
const documentDialogVisible = ref(false)
const documentDialogTitle = ref('')
const documentForm = reactive({
  id: null,
  title: '',
  docNumber: '',
  publishOrg: '',
  publishDate: '',
  securityLevel: 'public',
  content: ''
})

// Preview
const previewVisible = ref(false)
const previewDoc = ref(null)

const fetchDirectoryTree = async () => {
  try {
    const res = await getDirectoryTree(1)
    if (res.data) {
      directoryTree.value = res.data
    }
  } catch (e) {
    // handled by interceptor
  }
}

const fetchDocuments = async () => {
  loading.value = true
  try {
    const res = await searchDocuments({
      ...searchForm,
      page: pagination.page,
      pageSize: pagination.pageSize
    })
    if (res.data) {
      documents.value = res.data.records || res.data.list || []
      pagination.total = res.data.total || 0
    }
  } catch (e) {
    // handled by interceptor
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.page = 1
  fetchDocuments()
}

const resetSearch = () => {
  searchForm.keyword = ''
  searchForm.repoType = null
  searchForm.validity = ''
  pagination.page = 1
  fetchDocuments()
}

const handleContextMenu = (e, data, node) => {
  contextMenuVisible.value = true
  contextMenuX.value = e.clientX
  contextMenuY.value = e.clientY
  contextMenuNode.value = { data, node }
}

const onContextMenuVisibleChange = (visible) => {
  if (!visible) {
    contextMenuVisible.value = false
  }
}

const handleContextMenuCommand = async (command) => {
  const { data, node } = contextMenuNode.value
  if (command === 'add') {
    addDirectory(data.id)
  } else if (command === 'edit') {
    directoryForm.id = data.id
    directoryForm.name = data.name
    directoryForm.sort = data.sort || 0
    directoryForm.parentId = data.parentId
    directoryDialogTitle.value = '编辑目录'
    directoryDialogVisible.value = true
  } else if (command === 'delete') {
    try {
      await ElMessageBox.confirm(`确定删除目录 "${data.name}" 吗？`, '提示', { type: 'warning' })
      await deleteDirApi(data.id)
      ElMessage.success('删除成功')
      fetchDirectoryTree()
    } catch (e) {
      if (e !== 'cancel') {}
    }
  }
}

const addDirectory = (parentId) => {
  directoryForm.id = null
  directoryForm.name = ''
  directoryForm.sort = 0
  directoryForm.parentId = parentId
  directoryDialogTitle.value = parentId ? '新增子目录' : '新增目录'
  directoryDialogVisible.value = true
}

const saveDirectory = async () => {
  try {
    if (directoryForm.id) {
      await updateDirApi(directoryForm.id, { ...directoryForm })
      ElMessage.success('编辑成功')
    } else {
      await addDirApi({ ...directoryForm })
      ElMessage.success('新增成功')
    }
    directoryDialogVisible.value = false
    fetchDirectoryTree()
  } catch (e) {
    // handled by interceptor
  }
}

const addDocument = () => {
  Object.assign(documentForm, {
    id: null,
    title: '',
    docNumber: '',
    publishOrg: '',
    publishDate: '',
    securityLevel: 'public',
    content: ''
  })
  documentDialogTitle.value = '新增文档'
  documentDialogVisible.value = true
}

const editDocument = (row) => {
  Object.assign(documentForm, { ...row })
  documentDialogTitle.value = '编辑文档'
  documentDialogVisible.value = true
}

const saveDocument = async () => {
  try {
    if (documentForm.id) {
      await updateDocApi(documentForm.id, { ...documentForm })
      ElMessage.success('编辑成功')
    } else {
      await addDocApi({ ...documentForm })
      ElMessage.success('新增成功')
    }
    documentDialogVisible.value = false
    fetchDocuments()
  } catch (e) {
    // handled by interceptor
  }
}

const previewDocument = async (row) => {
  previewDoc.value = row
  previewVisible.value = true
}

const downloadDocument = async (row) => {
  try {
    const res = await downloadDocApi(row.id)
    const blob = new Blob([res])
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = row.title + '.pdf'
    link.click()
    window.URL.revokeObjectURL(url)
    ElMessage.success('下载成功')
  } catch (e) {
    // handled by interceptor
  }
}

const deleteDocument = async (row) => {
  try {
    await ElMessageBox.confirm(`确定删除文档 "${row.title}" 吗？`, '提示', { type: 'warning' })
    await deleteDocApi(row.id)
    ElMessage.success('删除成功')
    fetchDocuments()
  } catch (e) {
    if (e !== 'cancel') {}
  }
}

fetchDirectoryTree()
fetchDocuments()
</script>

<style scoped>
.repository-container {
  height: 100%;
}
.tree-col {
  height: 100%;
}
.tree-card {
  height: 100%;
}
.tree-card :deep(.el-card__body) {
  padding: 0;
}
.tree-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.tree-wrapper {
  padding: 12px;
  max-height: calc(100vh - 220px);
  overflow-y: auto;
}
.tree-node {
  display: flex;
  align-items: center;
  gap: 4px;
}
.content-col {
  height: 100%;
}
.content-col :deep(.el-card__body) {
  height: 100%;
  display: flex;
  flex-direction: column;
}
.search-form {
  margin-bottom: 16px;
}
.context-menu {
  position: fixed;
  z-index: 3000;
  background: #fff;
  border-radius: 4px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.15);
}
.preview-content {
  padding: 16px;
}
.preview-content pre {
  white-space: pre-wrap;
  word-break: break-all;
  background: #f5f7fa;
  padding: 16px;
  border-radius: 8px;
}
</style>
