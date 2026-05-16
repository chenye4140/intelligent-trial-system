<template>
  <div class="categories-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>📁 目录管理</span>
          <el-button type="primary" size="small" @click="showAddDialog(null)">
            <el-icon><Plus /></el-icon>
            新增根目录
          </el-button>
        </div>
      </template>

      <!-- 库类型切换 -->
      <el-tabs v-model="activeLibrary" @tab-click="loadCategories">
        <el-tab-pane label="法规库" name="regulation" />
        <el-tab-pane label="资料库" name="material" />
        <el-tab-pane label="裁判文书库" name="judgment" />
        <el-tab-pane label="案例库" name="case" />
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
            <span>{{ node.label }}</span>
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
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="400px">
      <el-form :model="dialogForm" label-width="80px">
        <el-form-item label="目录名称">
          <el-input v-model="dialogForm.name" placeholder="请输入目录名称" />
        </el-form-item>
        <el-form-item label="密级">
          <el-select v-model="dialogForm.securityLevel" style="width: 100%">
            <el-option label="绝密" value="top_secret" />
            <el-option label="机密" value="secret" />
            <el-option label="秘密" value="confidential" />
            <el-option label="内部" value="internal" />
            <el-option label="公开" value="public" />
          </el-select>
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="dialogForm.sortOrder" :min="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const activeLibrary = ref('regulation')
const treeData = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('新增目录')
const dialogMode = ref('add') // add or edit

const dialogForm = reactive({
  id: null,
  name: '',
  parentId: 0,
  securityLevel: 'internal',
  sortOrder: 0,
})

const showAddDialog = (parent) => {
  dialogMode.value = 'add'
  dialogTitle.value = parent ? `添加子目录到「${parent.name}」` : '新增根目录'
  dialogForm.id = null
  dialogForm.name = ''
  dialogForm.parentId = parent ? parent.id : 0
  dialogForm.securityLevel = 'internal'
  dialogForm.sortOrder = 0
  dialogVisible.value = true
}

const showEditDialog = (data) => {
  dialogMode.value = 'edit'
  dialogTitle.value = '编辑目录'
  dialogForm.id = data.id
  dialogForm.name = data.name
  dialogForm.parentId = data.parentId
  dialogForm.securityLevel = data.securityLevel || 'internal'
  dialogForm.sortOrder = data.sortOrder || 0
  dialogVisible.value = true
}

const handleSave = () => {
  ElMessage.success('保存成功（演示模式）')
  dialogVisible.value = false
}

const handleDelete = async (data) => {
  try {
    await ElMessageBox.confirm(`确定删除目录「${data.name}」及其子目录吗？`, '提示', {
      type: 'warning',
    })
    ElMessage.success('删除成功（演示模式）')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败', error)
    }
  }
}

const loadCategories = () => {
  // TODO: 调用API加载目录数据
  treeData.value = [
    {
      id: 1,
      name: '示例目录',
      parentId: 0,
      children: [
        { id: 2, name: '子目录A', parentId: 1 },
        { id: 3, name: '子目录B', parentId: 1 },
      ],
    },
  ]
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
      display: inline;
    }
  }
}
</style>
