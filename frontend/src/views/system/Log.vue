<template>
  <div class="log-container">
    <el-card shadow="hover">
      <template #header>
        <span>审计日志</span>
      </template>

      <!-- Search -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="模块">
          <el-select v-model="searchForm.module" placeholder="全部" clearable>
            <el-option label="用户管理" value="user" />
            <el-option label="角色管理" value="role" />
            <el-option label="菜单管理" value="menu" />
            <el-option label="文档管理" value="document" />
            <el-option label="多库管理" value="repository" />
          </el-select>
        </el-form-item>
        <el-form-item label="操作用户">
          <el-input v-model="searchForm.username" placeholder="请输入用户名" clearable />
        </el-form-item>
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="searchForm.dateRange"
            type="daterange"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- Table -->
      <el-table :data="logs" v-loading="loading" stripe style="width: 100%">
        <el-table-column prop="createTime" label="时间" width="180" />
        <el-table-column prop="username" label="用户" width="120" />
        <el-table-column prop="module" label="模块" width="100">
          <template #default="{ row }">
            <el-tag size="small">{{ row.moduleName }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="action" label="操作" width="120" />
        <el-table-column prop="ip" label="IP地址" width="140" />
        <el-table-column prop="duration" label="耗时" width="80">
          <template #default="{ row }">
            {{ row.duration }}ms
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column label="操作" width="80" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="viewDetail(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.pageSize"
        :total="pagination.total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next"
        @current-change="fetchLogs"
        @size-change="fetchLogs"
        style="margin-top: 16px; justify-content: flex-end"
      />
    </el-card>

    <!-- Detail Dialog -->
    <el-dialog v-model="detailVisible" title="日志详情" width="600px">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="操作时间">{{ detailLog?.createTime }}</el-descriptions-item>
        <el-descriptions-item label="操作用户">{{ detailLog?.username }}</el-descriptions-item>
        <el-descriptions-item label="所属模块">{{ detailLog?.moduleName }}</el-descriptions-item>
        <el-descriptions-item label="操作类型">{{ detailLog?.action }}</el-descriptions-item>
        <el-descriptions-item label="请求IP">{{ detailLog?.ip }}</el-descriptions-item>
        <el-descriptions-item label="耗时">{{ detailLog?.duration }}ms</el-descriptions-item>
        <el-descriptions-item label="操作描述">{{ detailLog?.description }}</el-descriptions-item>
        <el-descriptions-item label="请求方法">{{ detailLog?.method }}</el-descriptions-item>
        <el-descriptions-item label="请求URL">{{ detailLog?.requestUrl }}</el-descriptions-item>
        <el-descriptions-item label="请求参数">
          <pre class="detail-json">{{ JSON.stringify(detailLog?.requestParams, null, 2) }}</pre>
        </el-descriptions-item>
        <el-descriptions-item label="响应结果">
          <pre class="detail-json">{{ JSON.stringify(detailLog?.responseResult, null, 2) }}</pre>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { getAuditLogList } from '@/api/audit-log'

const logs = ref([])
const loading = ref(false)
const detailVisible = ref(false)
const detailLog = ref(null)

const searchForm = reactive({
  module: '',
  username: '',
  dateRange: null
})

const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0
})

const fetchLogs = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: pagination.page,
      pageSize: pagination.pageSize,
      module: searchForm.module
    }
    if (searchForm.dateRange && searchForm.dateRange.length === 2) {
      params.startTime = searchForm.dateRange[0]
      params.endTime = searchForm.dateRange[1]
    }
    const res = await getAuditLogList(params)
    if (res.data) {
      logs.value = res.data.records || res.data.list || []
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
  fetchLogs()
}

const resetSearch = () => {
  searchForm.module = ''
  searchForm.username = ''
  searchForm.dateRange = null
  pagination.page = 1
  fetchLogs()
}

const viewDetail = (row) => {
  detailLog.value = row
  detailVisible.value = true
}

fetchLogs()
</script>

<style scoped>
.log-container {
  padding: 4px;
}
.search-form {
  margin-bottom: 16px;
}
.detail-json {
  background: #f5f7fa;
  padding: 8px;
  border-radius: 4px;
  font-family: 'Courier New', monospace;
  font-size: 12px;
  white-space: pre-wrap;
  word-break: break-all;
  max-height: 200px;
  overflow-y: auto;
}
</style>
