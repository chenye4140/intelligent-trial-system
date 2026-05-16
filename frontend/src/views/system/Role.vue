<template>
  <div class="role-container">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>角色管理</span>
          <el-button type="primary" size="small" @click="addRole">
            <el-icon><Plus /></el-icon>
            新增角色
          </el-button>
        </div>
      </template>

      <!-- Table -->
      <el-table :data="roles" v-loading="loading" stripe style="width: 100%">
        <el-table-column prop="roleName" label="角色名称" width="150" />
        <el-table-column prop="roleCode" label="角色编码" width="150" />
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">{{ row.status === 1 ? '启用' : '禁用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="editRole(row)">编辑</el-button>
            <el-button type="warning" link size="small" @click="assignMenus(row)">分配权限</el-button>
            <el-button type="danger" link size="small" @click="deleteRole(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.pageSize"
        :total="pagination.total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @current-change="fetchRoles"
        @size-change="fetchRoles"
        style="margin-top: 16px; justify-content: flex-end"
      />
    </el-card>

    <!-- Role Dialog -->
    <el-dialog v-model="roleDialogVisible" :title="roleDialogTitle" width="500px">
      <el-form :model="roleForm" label-width="80px">
        <el-form-item label="角色名称">
          <el-input v-model="roleForm.roleName" placeholder="请输入角色名称" />
        </el-form-item>
        <el-form-item label="角色编码">
          <el-input v-model="roleForm.roleCode" placeholder="请输入角色编码" :disabled="!!roleForm.id" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="roleForm.description" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="roleDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveRole">保存</el-button>
      </template>
    </el-dialog>

    <!-- Assign Menus Dialog -->
    <el-dialog v-model="menuDialogVisible" title="分配菜单权限" width="500px">
      <el-form label-width="80px">
        <el-form-item label="菜单权限">
          <el-tree
            ref="menuTreeRef"
            :data="menuTree"
            :props="{ label: 'title', children: 'children' }"
            show-checkbox
            node-key="id"
            :default-checked-keys="checkedMenuIds"
            default-expand-all
            check-strictly
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="menuDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveMenus">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getRoleList, addRole as addRoleApi, updateRole as updateRoleApi, deleteRole as deleteRoleApi, assignMenus as assignMenusApi } from '@/api/role'
import { getMenuTree } from '@/api/menu'

const roles = ref([])
const loading = ref(false)
const roleDialogVisible = ref(false)
const roleDialogTitle = ref('')
const menuDialogVisible = ref(false)
const menuTreeRef = ref()
const menuTree = ref([])
const checkedMenuIds = ref([])
const currentRoleId = ref(null)

const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0
})

const roleForm = reactive({
  id: null,
  roleName: '',
  roleCode: '',
  description: '',
  status: 1
})

const fetchRoles = async () => {
  loading.value = true
  try {
    const res = await getRoleList({ ...pagination })
    if (res.data) {
      roles.value = res.data.records || res.data.list || []
      pagination.total = res.data.total || 0
    }
  } catch (e) {
    // handled by interceptor
  } finally {
    loading.value = false
  }
}

const fetchMenuTree = async () => {
  try {
    const res = await getMenuTree()
    if (res.data) {
      menuTree.value = res.data
    }
  } catch (e) {
    // handled by interceptor
  }
}

const addRole = () => {
  Object.assign(roleForm, { id: null, roleName: '', roleCode: '', description: '', status: 1 })
  roleDialogTitle.value = '新增角色'
  roleDialogVisible.value = true
}

const editRole = (row) => {
  Object.assign(roleForm, { ...row })
  roleDialogTitle.value = '编辑角色'
  roleDialogVisible.value = true
}

const saveRole = async () => {
  try {
    if (roleForm.id) {
      await updateRoleApi({ ...roleForm })
      ElMessage.success('编辑成功')
    } else {
      await addRoleApi({ ...roleForm })
      ElMessage.success('新增成功')
    }
    roleDialogVisible.value = false
    fetchRoles()
  } catch (e) {
    // handled by interceptor
  }
}

const assignMenus = async (row) => {
  currentRoleId.value = row.id
  checkedMenuIds.value = row.menuIds || []
  await fetchMenuTree()
  menuDialogVisible.value = true
}

const saveMenus = async () => {
  try {
    const checked = menuTreeRef.value.getCheckedKeys()
    const halfChecked = menuTreeRef.value.getHalfCheckedKeys()
    await assignMenusApi(currentRoleId.value, [...checked, ...halfChecked])
    ElMessage.success('权限分配成功')
    menuDialogVisible.value = false
    fetchRoles()
  } catch (e) {
    // handled by interceptor
  }
}

const deleteRole = async (row) => {
  try {
    await ElMessageBox.confirm(`确定删除角色 "${row.roleName}" 吗？`, '提示', { type: 'warning' })
    await deleteRoleApi(row.id)
    ElMessage.success('删除成功')
    fetchRoles()
  } catch (e) {
    if (e !== 'cancel') {}
  }
}

fetchRoles()
</script>

<style scoped>
.role-container {
  padding: 4px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
