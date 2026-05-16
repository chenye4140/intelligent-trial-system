<template>
  <div class="workflow-page">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>工作流审批</span>
        </div>
      </template>

      <!-- Tab 切换 -->
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane label="流程定义" name="definitions" />
        <el-tab-pane label="我的待办" name="my-tasks" />
        <el-tab-pane label="全部待办" name="pending-tasks" />
        <el-tab-pane label="启动流程" name="start-process" />
      </el-tabs>

      <!-- 流程定义 -->
      <div v-show="activeTab === 'definitions'">
        <el-table :data="definitions" v-loading="loading" border stripe style="width: 100%">
          <el-table-column prop="key" label="流程标识" width="160" />
          <el-table-column prop="name" label="流程名称" width="180" />
          <el-table-column prop="version" label="版本" width="80" align="center" />
          <el-table-column prop="deploymentId" label="部署ID" width="140" />
          <el-table-column prop="createTime" label="部署时间" width="170" />
          <el-table-column label="操作" width="120" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" link size="small" @click="handleDeploy(row)">重新部署</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 我的待办 -->
      <div v-show="activeTab === 'my-tasks'">
        <el-table :data="myTasks" v-loading="loading" border stripe style="width: 100%">
          <el-table-column prop="id" label="任务ID" width="100" />
          <el-table-column prop="name" label="任务名称" width="180" />
          <el-table-column prop="processDefinitionName" label="流程名称" width="160" />
          <el-table-column prop="assignee" label="办理人" width="120" />
          <el-table-column prop="createTime" label="创建时间" width="170" />
          <el-table-column label="操作" width="200" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" link size="small" @click="handleComplete(row)">办理</el-button>
              <el-button type="warning" link size="small" @click="handleClaim(row)" v-if="!row.assignee">签收</el-button>
              <el-button type="info" link size="small" @click="handleHistory(row)">历史</el-button>
            </template>
          </el-table-column>
        </el-table>

        <el-pagination
          v-model:current-page="myTasksPagination.pageNum"
          v-model:page-size="myTasksPagination.pageSize"
          :total="myTasksPagination.total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @current-change="loadMyTasks"
          @size-change="loadMyTasks"
          style="margin-top: 16px; justify-content: flex-end"
        />
      </div>

      <!-- 全部待办 -->
      <div v-show="activeTab === 'pending-tasks'">
        <el-table :data="pendingTasks" v-loading="loading" border stripe style="width: 100%">
          <el-table-column prop="id" label="任务ID" width="100" />
          <el-table-column prop="name" label="任务名称" width="180" />
          <el-table-column prop="processDefinitionName" label="流程名称" width="160" />
          <el-table-column prop="assignee" label="办理人" width="120" />
          <el-table-column prop="createTime" label="创建时间" width="170" />
          <el-table-column label="操作" width="200" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" link size="small" @click="handleClaim(row)">签收</el-button>
              <el-button type="info" link size="small" @click="handleHistory(row)">历史</el-button>
            </template>
          </el-table-column>
        </el-table>

        <el-pagination
          v-model:current-page="pendingPagination.pageNum"
          v-model:page-size="pendingPagination.pageSize"
          :total="pendingPagination.total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @current-change="loadPendingTasks"
          @size-change="loadPendingTasks"
          style="margin-top: 16px; justify-content: flex-end"
        />
      </div>

      <!-- 启动流程 -->
      <div v-show="activeTab === 'start-process'">
        <el-form :model="startForm" label-width="100px" style="max-width: 600px">
          <el-form-item label="流程定义">
            <el-select v-model="startForm.processDefinitionKey" placeholder="请选择流程" style="width: 100%">
              <el-option
                v-for="def in definitions"
                :key="def.key"
                :label="def.name"
                :value="def.key"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="案件ID">
            <el-input v-model="startForm.caseId" placeholder="请输入关联案件ID" />
          </el-form-item>
          <el-form-item label="业务标题">
            <el-input v-model="startForm.businessTitle" placeholder="请输入业务标题" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleStartProcess" :loading="startLoading">
              <el-icon><Promotion /></el-icon> 启动流程
            </el-button>
          </el-form-item>
        </el-form>
      </div>
    </el-card>

    <!-- 办理任务对话框 -->
    <el-dialog v-model="completeDialogVisible" title="办理任务" width="600px" :close-on-click-modal="false">
      <el-form :model="completeForm" label-width="100px">
        <el-form-item label="任务名称">
          <el-input :value="completeForm.taskName" disabled />
        </el-form-item>
        <el-form-item label="审批意见">
          <el-select v-model="completeForm.approval" placeholder="请选择审批意见" style="width: 100%">
            <el-option label="同意" value="agree" />
            <el-option label="驳回" value="reject" />
            <el-option label="退回" value="return" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="completeForm.comment" type="textarea" :rows="3" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="completeDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmComplete">确定</el-button>
      </template>
    </el-dialog>

    <!-- 历史对话框 -->
    <el-dialog v-model="historyDialogVisible" title="流程历史" width="700px">
      <el-timeline>
        <el-timeline-item
          v-for="item in historyList"
          :key="item.id"
          :timestamp="item.endTime || item.createTime"
          :type="item.approval === 'agree' ? 'success' : item.approval === 'reject' ? 'danger' : 'primary'"
        >
          <div>
            <strong>{{ item.taskName }}</strong> - {{ item.assignee || '未分配' }}
            <el-tag v-if="item.approval" :type="item.approval === 'agree' ? 'success' : 'danger'" size="small" style="margin-left: 8px">
              {{ item.approval === 'agree' ? '同意' : item.approval === 'reject' ? '驳回' : item.approval }}
            </el-tag>
          </div>
          <div v-if="item.comment" style="color: #909399; font-size: 13px; margin-top: 4px">
            {{ item.comment }}
          </div>
        </el-timeline-item>
      </el-timeline>
      <template #footer>
        <el-button @click="historyDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Promotion } from '@element-plus/icons-vue'
import { workflowApi } from '@/api/workflow'

const activeTab = ref('definitions')
const loading = ref(false)
const definitions = ref([])
const myTasks = ref([])
const pendingTasks = ref([])
const historyList = ref([])

// 分页
const myTasksPagination = reactive({ pageNum: 1, pageSize: 10, total: 0 })
const pendingPagination = reactive({ pageNum: 1, pageSize: 10, total: 0 })

// 启动流程
const startForm = reactive({
  processDefinitionKey: '',
  caseId: '',
  businessTitle: '',
})
const startLoading = ref(false)

// 办理任务
const completeDialogVisible = ref(false)
const completeForm = reactive({
  taskId: null,
  taskName: '',
  approval: 'agree',
  comment: '',
})

// 历史
const historyDialogVisible = ref(false)

// 加载流程定义
const loadDefinitions = async () => {
  loading.value = true
  try {
    const res = await workflowApi.getDefinitions()
    definitions.value = res.data || []
  } catch (e) {
    console.error('加载流程定义失败', e)
  } finally {
    loading.value = false
  }
}

// 加载我的待办
const loadMyTasks = async () => {
  loading.value = true
  try {
    const params = { pageNum: myTasksPagination.pageNum, pageSize: myTasksPagination.pageSize }
    const res = await workflowApi.getMyTasks(params)
    myTasks.value = res.data?.list || []
    myTasksPagination.total = res.data?.total || 0
  } catch (e) {
    console.error('加载我的待办失败', e)
  } finally {
    loading.value = false
  }
}

// 加载全部待办
const loadPendingTasks = async () => {
  loading.value = true
  try {
    const params = { pageNum: pendingPagination.pageNum, pageSize: pendingPagination.pageSize }
    const res = await workflowApi.getPendingTasks(params)
    pendingTasks.value = res.data?.list || []
    pendingPagination.total = res.data?.total || 0
  } catch (e) {
    console.error('加载全部待办失败', e)
  } finally {
    loading.value = false
  }
}

// Tab 切换
const handleTabChange = (tab) => {
  if (tab === 'definitions') loadDefinitions()
  else if (tab === 'my-tasks') loadMyTasks()
  else if (tab === 'pending-tasks') loadPendingTasks()
}

// 部署流程
const handleDeploy = async (row) => {
  try {
    await workflowApi.deploy(row.key)
    ElMessage.success('流程部署成功')
    loadDefinitions()
  } catch (e) {
    console.error('部署失败', e)
  }
}

// 启动流程
const handleStartProcess = async () => {
  if (!startForm.processDefinitionKey) {
    ElMessage.warning('请选择流程定义')
    return
  }
  startLoading.value = true
  try {
    await workflowApi.startProcess({ ...startForm })
    ElMessage.success('流程启动成功')
    startForm.caseId = ''
    startForm.businessTitle = ''
  } catch (e) {
    console.error('启动流程失败', e)
  } finally {
    startLoading.value = false
  }
}

// 办理任务
const handleComplete = (row) => {
  completeForm.taskId = row.id
  completeForm.taskName = row.name
  completeForm.approval = 'agree'
  completeForm.comment = ''
  completeDialogVisible.value = true
}

const confirmComplete = async () => {
  try {
    await workflowApi.completeTask(completeForm.taskId, {
      approval: completeForm.approval,
      comment: completeForm.comment,
    })
    ElMessage.success('任务办理成功')
    completeDialogVisible.value = false
    loadMyTasks()
  } catch (e) {
    console.error('办理任务失败', e)
  }
}

// 签收任务
const handleClaim = async (row) => {
  try {
    await workflowApi.claimTask(row.id)
    ElMessage.success('签收成功')
    if (activeTab.value === 'my-tasks') loadMyTasks()
    else loadPendingTasks()
  } catch (e) {
    console.error('签收失败', e)
  }
}

// 查看历史
const handleHistory = async (row) => {
  try {
    const res = await workflowApi.getHistory(row.processInstanceId)
    historyList.value = res.data || []
    historyDialogVisible.value = true
  } catch (e) {
    console.error('获取历史失败', e)
  }
}

onMounted(() => {
  loadDefinitions()
})
</script>

<style lang="scss" scoped>
.workflow-page {
  padding: 4px;

  .card-header {
    font-weight: 600;
    font-size: 16px;
  }
}
</style>
