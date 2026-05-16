<template>
  <div class="menu-container">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>菜单管理</span>
          <el-button type="primary" size="small" @click="addMenu(null)">
            <el-icon><Plus /></el-icon>
            新增菜单
          </el-button>
        </div>
      </template>

      <!-- Menu Tree Table -->
      <el-table
        :data="menuList"
        row-key="id"
        :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
        default-expand-all
        stripe
        style="width: 100%"
      >
        <el-table-column prop="title" label="菜单名称" min-width="200" />
        <el-table-column label="图标" width="80">
          <template #default="{ row }">
            <el-icon v-if="row.icon"><component :is="row.icon" /></el-icon>
          </template>
        </el-table-column>
        <el-table-column prop="path" label="路由路径" width="180" />
        <el-table-column prop="permission" label="权限标识" width="180" />
        <el-table-column label="类型" width="80">
          <template #default="{ row }">
            <el-tag v-if="row.type === 'menu'" type="primary" size="small">菜单</el-tag>
            <el-tag v-else-if="row.type === 'button'" type="warning" size="small">按钮</el-tag>
            <el-tag v-else type="info" size="small">目录</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="sort" label="排序" width="80" />
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag v-if="row.visible" type="success" size="small">显示</el-tag>
            <el-tag v-else type="danger" size="small">隐藏</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="addMenu(row.id)">新增子菜单</el-button>
            <el-button type="warning" link size="small" @click="editMenu(row)">编辑</el-button>
            <el-button type="danger" link size="small" @click="deleteMenu(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- Menu Dialog -->
    <el-dialog v-model="menuDialogVisible" :title="menuDialogTitle" width="600px">
      <el-form :model="menuForm" label-width="100px">
        <el-form-item label="上级菜单">
          <el-tree-select
            v-model="menuForm.parentId"
            :data="menuList"
            :props="{ label: 'title', children: 'children' }"
            node-key="id"
            check-strictly
            placeholder="顶级菜单"
            clearable
          />
        </el-form-item>
        <el-form-item label="菜单类型">
          <el-radio-group v-model="menuForm.type">
            <el-radio label="catalog">目录</el-radio>
            <el-radio label="menu">菜单</el-radio>
            <el-radio label="button">按钮</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="菜单名称">
          <el-input v-model="menuForm.title" placeholder="请输入菜单名称" />
        </el-form-item>
        <el-form-item v-if="menuForm.type !== 'button'" label="路由路径">
          <el-input v-model="menuForm.path" placeholder="请输入路由路径" />
        </el-form-item>
        <el-form-item v-if="menuForm.type !== 'button'" label="图标">
          <el-input v-model="menuForm.icon" placeholder="请输入图标名称，如 HomeFilled" />
        </el-form-item>
        <el-form-item label="权限标识">
          <el-input v-model="menuForm.permission" placeholder="如 system:user:list" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="menuForm.sort" :min="0" />
        </el-form-item>
        <el-form-item label="是否显示">
          <el-switch v-model="menuForm.visible" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="menuDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveMenu">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMenuList, addMenu as addMenuApi, updateMenu as updateMenuApi, deleteMenu as deleteMenuApi } from '@/api/menu'

const menuList = ref([])
const menuDialogVisible = ref(false)
const menuDialogTitle = ref('')

const menuForm = reactive({
  id: null,
  parentId: null,
  type: 'menu',
  title: '',
  path: '',
  icon: '',
  permission: '',
  sort: 0,
  visible: true
})

const fetchMenus = async () => {
  try {
    const res = await getMenuList()
    if (res.data) {
      menuList.value = res.data
    }
  } catch (e) {
    // handled by interceptor
  }
}

const addMenu = (parentId) => {
  Object.assign(menuForm, {
    id: null,
    parentId: parentId,
    type: 'menu',
    title: '',
    path: '',
    icon: '',
    permission: '',
    sort: 0,
    visible: true
  })
  menuDialogTitle.value = parentId ? '新增子菜单' : '新增菜单'
  menuDialogVisible.value = true
}

const editMenu = (row) => {
  Object.assign(menuForm, { ...row })
  menuDialogTitle.value = '编辑菜单'
  menuDialogVisible.value = true
}

const saveMenu = async () => {
  try {
    if (menuForm.id) {
      await updateMenuApi(menuForm.id, { ...menuForm })
      ElMessage.success('编辑成功')
    } else {
      await addMenuApi({ ...menuForm })
      ElMessage.success('新增成功')
    }
    menuDialogVisible.value = false
    fetchMenus()
  } catch (e) {
    // handled by interceptor
  }
}

const deleteMenu = async (row) => {
  try {
    await ElMessageBox.confirm(`确定删除菜单 "${row.title}" 吗？`, '提示', { type: 'warning' })
    await deleteMenuApi(row.id)
    ElMessage.success('删除成功')
    fetchMenus()
  } catch (e) {
    if (e !== 'cancel') {}
  }
}

fetchMenus()
</script>

<style scoped>
.menu-container {
  padding: 4px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
