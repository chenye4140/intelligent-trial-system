<template>
  <div class="punishment-container">
    <!-- 搜索栏 -->
    <el-card shadow="hover" class="search-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="案件ID">
          <el-input v-model="searchForm.caseId" placeholder="请输入案件ID" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item label="处分类型">
          <el-select v-model="searchForm.punishmentType" placeholder="请选择" clearable style="width: 150px">
            <el-option label="警告" value="警告" />
            <el-option label="记过" value="记过" />
            <el-option label="降级" value="降级" />
            <el-option label="撤职" value="撤职" />
            <el-option label="开除" value="开除" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择" clearable style="width: 120px">
            <el-option label="待执行" :value="0" />
            <el-option label="执行中" :value="1" />
            <el-option label="已完成" :value="2" />
            <el-option label="已撤销" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">🔍 搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 统计卡片 -->
    <el-row :gutter="16" class="stats-row">
      <el-col :span="6">
        <el-card shadow="hover">
          <template #header>待执行</template>
          <div class="stat-value" style="color: #e6a23c">{{ stats['0'] || 0 }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <template #header>执行中</template>
          <div class="stat-value" style="color: #409eff">{{ stats['1'] || 0 }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <template #header>已完成</template>
          <div class="stat-value" style="color: #67c23a">{{ stats['2'] || 0 }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <template #header>⚠️ 逾期</template>
          <div class="stat-value" style="color: #f56c6c">{{ overdueCount }}</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 操作按钮 -->
    <div class="toolbar">
      <el-button type="primary" @click="handleAdd">➕ 新增处分执行</el-button>
      <el-button type="warning" @click="showOverdue = true">⚠️ 查看逾期</el-button>
    </div>

    <!-- 表格 -->
    <el-table :data="tableData" v-loading="loading" stripe border>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="caseId" label="案件ID" width="140" />
      <el-table-column prop="punishmentType" label="处分类型" width="100">
        <template #default="{ row }">
          <el-tag :type="getPunishmentTypeTag(row.punishmentType)">{{ row.punishmentType }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="decisionDate" label="决定日期" width="120" />
      <el-table-column prop="startDate" label="开始日期" width="120" />
      <el-table-column prop="endDate" label="结束日期" width="120" />
      <el-table-column prop="statusText" label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="getStatusTag(row.status)">{{ row.statusText }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="逾期" width="70">
        <template #default="{ row }">
          <el-tag v-if="row.isOverdue === 1" type="danger">是</el-tag>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="260" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="handleDetail(row)">详情</el-button>
          <el-button size="small" type="primary" @click="handleEdit(row)">编辑</el-button>
          <el-dropdown @command="(cmd) => handleStatusChange(row, cmd)">
            <el-button size="small" type="success">状态</el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item :command="0">待执行</el-dropdown-item>
                <el-dropdown-item :command="1">执行中</el-dropdown-item>
                <el-dropdown-item :command="2">已完成</el-dropdown-item>
                <el-dropdown-item :command="3">已撤销</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
          <el-popconfirm title="确认删除？" @confirm="handleDelete(row.id)">
            <template #reference>
              <el-button size="small" type="danger">删除</el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <el-pagination
      v-model:current-page="pagination.pageNum"
      v-model:page-size="pagination.pageSize"
      :total="pagination.total"
      :page-sizes="[10, 20, 50]"
      layout="total, sizes, prev, pager, next"
      class="pagination"
      @current-change="fetchData"
      @size-change="fetchData"
    />

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑处分执行' : '新增处分执行'" width="600px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="案件ID">
          <el-input v-model="form.caseId" placeholder="请输入案件ID" />
        </el-form-item>
        <el-form-item label="处分类型">
          <el-select v-model="form.punishmentType" placeholder="请选择" style="width: 100%">
            <el-option label="警告" value="警告" />
            <el-option label="记过" value="记过" />
            <el-option label="降级" value="降级" />
            <el-option label="撤职" value="撤职" />
            <el-option label="开除" value="开除" />
          </el-select>
        </el-form-item>
        <el-form-item label="决定日期">
          <el-date-picker v-model="form.decisionDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
        </el-form-item>
        <el-form-item label="开始日期">
          <el-date-picker v-model="form.startDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
        </el-form-item>
        <el-form-item label="结束日期">
          <el-date-picker v-model="form.endDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
        </el-form-item>
        <el-form-item label="状态" v-if="isEdit">
          <el-select v-model="form.status" placeholder="请选择" style="width: 100%">
            <el-option label="待执行" :value="0" />
            <el-option label="执行中" :value="1" />
            <el-option label="已完成" :value="2" />
            <el-option label="已撤销" :value="3" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 详情对话框 -->
    <el-dialog v-model="detailVisible" title="处分执行详情" width="700px">
      <el-descriptions :column="2" border v-if="detailData">
        <el-descriptions-item label="案件ID">{{ detailData.caseId }}</el-descriptions-item>
        <el-descriptions-item label="处分类型">
          <el-tag :type="getPunishmentTypeTag(detailData.punishmentType)">{{ detailData.punishmentType }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="决定日期">{{ detailData.decisionDate }}</el-descriptions-item>
        <el-descriptions-item label="开始日期">{{ detailData.startDate }}</el-descriptions-item>
        <el-descriptions-item label="结束日期">{{ detailData.endDate }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusTag(detailData.status)">{{ detailData.statusText }}</el-tag>
        </el-descriptions-item>
      </el-descriptions>

      <el-divider>关联材料</el-divider>
      <el-table :data="detailData?.materials || []" size="small" border>
        <el-table-column prop="materialType" label="材料类型" />
        <el-table-column prop="filePath" label="文件路径" show-overflow-tooltip />
        <el-table-column prop="uploadTime" label="上传时间" width="170" />
      </el-table>
    </el-dialog>

    <!-- 逾期记录对话框 -->
    <el-dialog v-model="showOverdue" title="⚠️ 逾期处分执行记录" width="800px">
      <el-table :data="overdueData" border size="small">
        <el-table-column prop="caseId" label="案件ID" />
        <el-table-column prop="punishmentType" label="处分类型" />
        <el-table-column prop="endDate" label="结束日期" />
        <el-table-column prop="statusText" label="状态" />
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { punishmentApi } from '@/api/punishment'

const loading = ref(false)
const tableData = ref([])
const dialogVisible = ref(false)
const detailVisible = ref(false)
const showOverdue = ref(false)
const isEdit = ref(false)
const stats = ref({})
const overdueCount = ref(0)
const overdueData = ref([])
const detailData = ref(null)

const searchForm = reactive({
  caseId: '',
  punishmentType: '',
  status: null,
  isOverdue: null,
})

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0,
})

const form = reactive({
  id: null,
  caseId: '',
  punishmentType: '',
  decisionDate: '',
  startDate: '',
  endDate: '',
  status: 0,
})

const fetchData = async () => {
  loading.value = true
  try {
    const params = { ...searchForm, pageNum: pagination.pageNum, pageSize: pagination.pageSize }
    const { data } = await punishmentApi.getPage(params)
    tableData.value = data.records
    pagination.total = data.total
  } catch (e) {
    ElMessage.error('获取数据失败')
  } finally {
    loading.value = false
  }
}

const fetchStats = async () => {
  try {
    const { data } = await punishmentApi.statistics()
    const map = {}
    data.forEach(item => { map[item.status] = item.count })
    stats.value = map
  } catch (e) { /* ignore */ }

  try {
    const { data } = await punishmentApi.getOverdue()
    overdueCount.value = data.length
    overdueData.value = data
  } catch (e) { /* ignore */ }
}

const handleSearch = () => {
  pagination.pageNum = 1
  fetchData()
}

const handleReset = () => {
  Object.assign(searchForm, { caseId: '', punishmentType: '', status: null, isOverdue: null })
  handleSearch()
}

const handleAdd = () => {
  isEdit.value = false
  Object.assign(form, { id: null, caseId: '', punishmentType: '', decisionDate: '', startDate: '', endDate: '', status: 0 })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  Object.assign(form, {
    id: row.id,
    caseId: row.caseId,
    punishmentType: row.punishmentType,
    decisionDate: row.decisionDate,
    startDate: row.startDate,
    endDate: row.endDate,
    status: row.status,
  })
  dialogVisible.value = true
}

const handleSubmit = async () => {
  try {
    if (isEdit.value) {
      await punishmentApi.update(form)
      ElMessage.success('更新成功')
    } else {
      await punishmentApi.create(form)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    fetchData()
    fetchStats()
  } catch (e) {
    ElMessage.error(isEdit.value ? '更新失败' : '创建失败')
  }
}

const handleDelete = async (id) => {
  try {
    await punishmentApi.delete(id)
    ElMessage.success('删除成功')
    fetchData()
    fetchStats()
  } catch (e) {
    ElMessage.error('删除失败')
  }
}

const handleStatusChange = async (row, status) => {
  try {
    await punishmentApi.changeStatus(row.id, status)
    ElMessage.success('状态更新成功')
    fetchData()
    fetchStats()
  } catch (e) {
    ElMessage.error('状态更新失败')
  }
}

const handleDetail = async (row) => {
  try {
    const { data } = await punishmentApi.getDetail(row.id)
    detailData.value = data
    detailVisible.value = true
  } catch (e) {
    ElMessage.error('获取详情失败')
  }
}

const getStatusTag = (status) => {
  const map = { 0: 'warning', 1: '', 2: 'success', 3: 'info' }
  return map[status] || ''
}

const getPunishmentTypeTag = (type) => {
  const map = { 警告: 'info', 记过: '', 降级: 'warning', 撤职: 'danger', 开除: 'danger' }
  return map[type] || ''
}

onMounted(() => {
  fetchData()
  fetchStats()
})
</script>

<style scoped>
.punishment-container { padding: 20px; }
.search-card { margin-bottom: 16px; }
.stats-row { margin-bottom: 16px; }
.stat-value { font-size: 28px; font-weight: bold; text-align: center; padding: 10px 0; }
.toolbar { margin-bottom: 16px; }
.pagination { margin-top: 16px; justify-content: flex-end; }
</style>
