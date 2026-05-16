<template>
  <div class="user-container">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>用户管理</span>
          <el-button type="primary" size="small" @click="addUser">
            <el-icon><Plus /></el-icon>
            新增用户
          </el-button>
        </div>
      </template>

      <!-- Search -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="用户名">
          <el-input v-model="searchForm.username" placeholder="请输入用户名" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- Table -->
      <el-table :data="users" v-loading="loading" stripe style="width: 100%">
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="realName" label="姓名" width="120" />
        <el-table-column prop="email" label="邮箱" width="180" show-overflow-tooltip />
        <el-table-column prop="phone" label="手机号" width="140" />
        <el-table-column label="角色" min-width="150">
          <template #default="{ row }">
            <el-tag v-for="role in row.roles" :key="role" size="small" class="role-tag">{{ role }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-switch v-model="row.status" :active-value="1" :inactive-value="0" @change="toggleStatus(row)" />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="editUser(row)">编辑</el-button>
            <el-button type="warning" link size="small" @click="assignRole(row)">分配角色</el-button>
            <el-button type="info" link size="small" @click="resetPassword(row)">重置密码</el-button>
            <el-button type="danger" link size="small" @click="deleteUser(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.pageSize"
        :total="pagination.total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @current-change="fetchUsers"
        @size-change="fetchUsers"
        style="margin-top: 16px; justify-content: flex-end"
      />
    </el-card>

    <!-- User Dialog -->
    <el-dialog v-model="userDialogVisible" :title="userDialogTitle" width="500px">
      <el-form :model="userForm" label-width="80px" ref="userFormRef" :rules="userRules">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="userForm.username" placeholder="请输入用户名" :disabled="!!userForm.id" />
        </el-form-item>
        <el-form-item label="姓名" prop="realName">
          <el-input v-model="userForm.realName" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="userForm.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="userForm.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item v-if="!userForm.id" label="密码" prop="password">
          <el-input v-model="userForm.password" type="password" placeholder="请输入密码" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="userDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveUser">保存</el-button>
      </template>
    </el-dialog>

    <!-- Assign Role Dialog -->
    <el-dialog v-model="roleDialogVisible" title="分配角色" width="500px">
      <el-form label-width="80px">
        <el-form-item label="角色">
          <el-select v-model="selectedRoles" multiple placeholder="请选择角色" style="width: 100%">
            <el-option label="管理员" value="admin" />
            <el-option label="普通用户" value="user" />
            <el-option label="审核员" value="auditor" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="roleDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveRoles">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getUserList, addUser as addUserApi, updateUser as updateUserApi, deleteUser as deleteUserApi, resetPassword as resetPasswordApi, toggleUserStatus as toggleUserStatusApi, assignRoles as assignRolesApi } from '@/api/user'

const users = ref([])
const loading = ref(false)
const userDialogVisible = ref(false)
const userDialogTitle = ref('')
const roleDialogVisible = ref(false)
const selectedRoles = ref([])
const userFormRef = ref()
const currentUserId = ref(null)

const searchForm = reactive({ username: '' })

const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0
})

const userForm = reactive({
  id: null,
  username: '',
  realName: '',
  email: '',
  phone: '',
  password: ''
})

const userRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  realName: [{ required: true, message: '请输入姓名', trigger: 'blur' }]
}

const fetchUsers = async () => {
  loading.value = true
  try {
    const res = await getUserList({ ...searchForm, ...pagination })
    if (res.data) {
      users.value = res.data.records || res.data.list || []
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
  fetchUsers()
}

const resetSearch = () => {
  searchForm.username = ''
  pagination.page = 1
  fetchUsers()
}

const addUser = () => {
  Object.assign(userForm, { id: null, username: '', realName: '', email: '', phone: '', password: '' })
  userDialogTitle.value = '新增用户'
  userDialogVisible.value = true
}

const editUser = (row) => {
  Object.assign(userForm, { ...row, password: '' })
  userDialogTitle.value = '编辑用户'
  userDialogVisible.value = true
}

const saveUser = async () => {
  await userFormRef.value.validate(async (valid) => {
    if (!valid) return
    try {
      if (userForm.id) {
        await updateUserApi(userForm.id, { ...userForm })
        ElMessage.success('编辑成功')
      } else {
        await addUserApi({ ...userForm })
        ElMessage.success('新增成功')
      }
      userDialogVisible.value = false
      fetchUsers()
    } catch (e) {
      // handled by interceptor
    }
  })
}

const assignRole = (row) => {
  currentUserId.value = row.id
  selectedRoles.value = row.roles || []
  roleDialogVisible.value = true
}

const saveRoles = async () => {
  try {
    await assignRolesApi(currentUserId.value, selectedRoles.value)
    ElMessage.success('角色分配成功')
    roleDialogVisible.value = false
    fetchUsers()
  } catch (e) {
    // handled by interceptor
  }
}

const resetPassword = async (row) => {
  try {
    const { value } = await ElMessageBox.prompt('请输入新密码', `重置用户 "${row.username}" 的密码`, {
      inputType: 'password',
      inputPlaceholder: '请输入新密码',
      inputValidator: (val) => val && val.length >= 6 ? true : '密码至少6位'
    })
    await resetPasswordApi({ userId: row.id, newPassword: value })
    ElMessage.success('密码重置成功')
  } catch (e) {
    if (e !== 'cancel') {}
  }
}

const toggleStatus = async (row) => {
  try {
    await toggleUserStatusApi(row.id, row.status)
    ElMessage.success(row.status === 1 ? '已启用' : '已禁用')
  } catch (e) {
    row.status = row.status === 1 ? 0 : 1
  }
}

const deleteUser = async (row) => {
  try {
    await ElMessageBox.confirm(`确定删除用户 "${row.username}" 吗？`, '提示', { type: 'warning' })
    await deleteUserApi(row.id)
    ElMessage.success('删除成功')
    fetchUsers()
  } catch (e) {
    if (e !== 'cancel') {}
  }
}

fetchUsers()
</script>

<style scoped>
.user-container {
  padding: 4px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.search-form {
  margin-bottom: 16px;
}
.role-tag {
  margin-right: 4px;
}
</style>
