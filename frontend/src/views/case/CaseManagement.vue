<template>
  <div class="case-management-page">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>案件管理</span>
        </div>
      </template>

      <!-- 搜索表单 -->
      <el-form :inline="true" :model="searchForm" class="search-form" label-width="80px">
        <el-form-item label="案件编号">
          <el-input v-model="searchForm.caseCode" placeholder="请输入案件编号" clearable style="width: 160px" />
        </el-form-item>
        <el-form-item label="案件名称">
          <el-input v-model="searchForm.caseName" placeholder="请输入案件名称" clearable style="width: 160px" />
        </el-form-item>
        <el-form-item label="案件类型">
          <el-select v-model="searchForm.caseType" placeholder="全部" clearable style="width: 120px">
            <el-option label="违纪" :value="1" />
            <el-option label="违法" :value="2" />
            <el-option label="职务犯罪" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable style="width: 120px">
            <el-option label="草稿" :value="0" />
            <el-option label="审理中" :value="1" />
            <el-option label="已完结" :value="2" />
            <el-option label="已归档" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="被调查人">
          <el-input v-model="searchForm.respondentName" placeholder="请输入被调查人姓名" clearable style="width: 140px" />
        </el-form-item>
        <el-form-item label="立案时间">
          <el-date-picker
            v-model="searchForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            style="width: 240px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon> 查询
          </el-button>
          <el-button @click="resetSearch">
            <el-icon><RefreshLeft /></el-icon> 重置
          </el-button>
        </el-form-item>
      </el-form>

      <!-- 工具栏 -->
      <div class="toolbar">
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon> 新增案件
        </el-button>
      </div>

      <!-- 数据表格 -->
      <el-table :data="tableData" v-loading="loading" border stripe style="width: 100%" row-key="id">
        <el-table-column prop="caseCode" label="案件编号" width="140" />
        <el-table-column prop="caseName" label="案件名称" min-width="180" show-overflow-tooltip />
        <el-table-column prop="caseType" label="案件类型" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="caseTypeMap[row.caseType]?.type" size="small">
              {{ caseTypeMap[row.caseType]?.text || row.caseType }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="respondentName" label="被调查人" width="120" />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="statusMap[row.status]?.type" size="small">
              {{ statusMap[row.status]?.text || row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="filingDate" label="立案日期" width="120" />
        <el-table-column prop="handlingDept" label="承办部门" width="140" />
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleView(row)">查看</el-button>
            <el-button type="warning" link size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button type="info" link size="small" @click="handleChangeStatus(row)">状态变更</el-button>
            <el-button type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        v-model:current-page="pagination.pageNum"
        v-model:page-size="pagination.pageSize"
        :total="pagination.total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @current-change="loadCases"
        @size-change="loadCases"
        style="margin-top: 16px; justify-content: flex-end"
      />
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="caseDialogVisible"
      :title="caseDialogTitle"
      width="700px"
      :close-on-click-modal="false"
    >
      <el-form
        :model="caseForm"
        :rules="caseRules"
        ref="caseFormRef"
        label-width="100px"
        label-position="right"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="案件编号" prop="caseCode">
              <el-input v-model="caseForm.caseCode" placeholder="请输入案件编号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="案件名称" prop="caseName">
              <el-input v-model="caseForm.caseName" placeholder="请输入案件名称" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="案件类型" prop="caseType">
              <el-select v-model="caseForm.caseType" placeholder="请选择案件类型" style="width: 100%">
                <el-option label="违纪" :value="1" />
                <el-option label="违法" :value="2" />
                <el-option label="职务犯罪" :value="3" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-select v-model="caseForm.status" placeholder="请选择状态" style="width: 100%">
                <el-option label="草稿" :value="0" />
                <el-option label="审理中" :value="1" />
                <el-option label="已完结" :value="2" />
                <el-option label="已归档" :value="3" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="被调查人" prop="respondentName">
              <el-input v-model="caseForm.respondentName" placeholder="请输入被调查人姓名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="承办部门" prop="handlingDept">
              <el-input v-model="caseForm.handlingDept" placeholder="请输入承办部门" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="立案日期" prop="filingDate">
              <el-date-picker
                v-model="caseForm.filingDate"
                type="date"
                placeholder="请选择立案日期"
                value-format="YYYY-MM-DD"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="承办人" prop="handlerName">
              <el-input v-model="caseForm.handlerName" placeholder="请输入承办人" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="案件描述" prop="description">
          <el-input v-model="caseForm.description" type="textarea" :rows="3" placeholder="请输入案件描述" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="caseForm.remark" type="textarea" :rows="2" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="caseDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveCase">保存</el-button>
      </template>
    </el-dialog>

    <!-- 详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="案件详情" width="900px">
      <template #header>
        <div class="dialog-header">
          <span>案件详情</span>
          <span class="case-code">编号：{{ detailData.caseCode }}</span>
        </div>
      </template>
      <el-tabs v-model="activeTab">
        <el-tab-pane label="基本信息" name="basic">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="案件编号">{{ detailData.caseCode }}</el-descriptions-item>
            <el-descriptions-item label="案件名称">{{ detailData.caseName }}</el-descriptions-item>
            <el-descriptions-item label="案件类型">
              <el-tag :type="caseTypeMap[detailData.caseType]?.type" size="small">
                {{ caseTypeMap[detailData.caseType]?.text || detailData.caseType }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="状态">
              <el-tag :type="statusMap[detailData.status]?.type" size="small">
                {{ statusMap[detailData.status]?.text || detailData.status }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="被调查人">{{ detailData.respondentName }}</el-descriptions-item>
            <el-descriptions-item label="承办部门">{{ detailData.handlingDept }}</el-descriptions-item>
            <el-descriptions-item label="承办人">{{ detailData.handlerName || '-' }}</el-descriptions-item>
            <el-descriptions-item label="立案日期">{{ detailData.filingDate || '-' }}</el-descriptions-item>
            <el-descriptions-item label="创建时间">{{ detailData.createTime }}</el-descriptions-item>
            <el-descriptions-item label="更新时间">{{ detailData.updateTime || '-' }}</el-descriptions-item>
            <el-descriptions-item label="案件描述" :span="2">{{ detailData.description || '-' }}</el-descriptions-item>
            <el-descriptions-item label="备注" :span="2">{{ detailData.remark || '-' }}</el-descriptions-item>
          </el-descriptions>
        </el-tab-pane>
        <el-tab-pane label="涉案人员" name="parties">
          <div class="tab-content">
            <el-button type="primary" size="small" @click="handleAddParty" style="margin-bottom: 12px">
              <el-icon><Plus /></el-icon> 添加人员
            </el-button>
            <el-table :data="parties" border stripe v-loading="partiesLoading" style="width: 100%">
              <el-table-column prop="name" label="姓名" width="120" />
              <el-table-column label="性别" width="80" align="center">
                <template #default="{ row }">
                  {{ genderMap[row.gender] || '-' }}
                </template>
              </el-table-column>
              <el-table-column label="人员类型" width="120" align="center">
                <template #default="{ row }">
                  <el-tag size="small">{{ partyTypeMap[row.partyType] || row.partyType }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="phone" label="联系电话" width="140" />
              <el-table-column prop="idCard" label="身份证号" width="180" />
              <el-table-column prop="address" label="住址" min-width="160" show-overflow-tooltip />
              <el-table-column prop="remark" label="备注" min-width="120" show-overflow-tooltip />
              <el-table-column label="操作" width="80" fixed="right">
                <template #default="{ row }">
                  <el-button type="danger" link size="small" @click="handleDeleteParty(row)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-tab-pane>
        <el-tab-pane label="违纪违法事实" name="violations">
          <div class="tab-content">
            <el-button type="primary" size="small" @click="handleAddViolation" style="margin-bottom: 12px">
              <el-icon><Plus /></el-icon> 添加事实
            </el-button>
            <el-table :data="violations" border stripe v-loading="violationsLoading" style="width: 100%">
              <el-table-column prop="violationType" label="类型" width="120" align="center">
                <template #default="{ row }">
                  <el-tag :type="caseTypeMap[row.violationType]?.type" size="small">
                    {{ caseTypeMap[row.violationType]?.text || row.violationType }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="description" label="事实描述" min-width="250" show-overflow-tooltip />
              <el-table-column prop="occurDate" label="发生时间" width="120" />
              <el-table-column prop="location" label="发生地点" width="160" show-overflow-tooltip />
              <el-table-column prop="severity" label="严重程度" width="100" align="center">
                <template #default="{ row }">
                  <el-tag :type="severityMap[row.severity]?.type" size="small">
                    {{ severityMap[row.severity]?.text || row.severity }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="140" fixed="right">
                <template #default="{ row }">
                  <el-button type="primary" link size="small" @click="handleEditViolation(row)">编辑</el-button>
                  <el-button type="danger" link size="small" @click="handleDeleteViolation(row)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-tab-pane>
      </el-tabs>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 状态变更对话框 -->
    <el-dialog v-model="statusDialogVisible" title="变更案件状态" width="400px">
      <el-form :model="statusForm" label-width="80px">
        <el-form-item label="当前状态">
          <el-tag :type="statusMap[statusForm.currentStatus]?.type" size="small">
            {{ statusMap[statusForm.currentStatus]?.text || statusForm.currentStatus }}
          </el-tag>
        </el-form-item>
        <el-form-item label="目标状态" required>
          <el-select v-model="statusForm.newStatus" placeholder="请选择目标状态" style="width: 100%">
            <el-option label="草稿" :value="0" />
            <el-option label="审理中" :value="1" />
            <el-option label="已完结" :value="2" />
            <el-option label="已归档" :value="3" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="statusDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmChangeStatus">确定</el-button>
      </template>
    </el-dialog>

    <!-- 添加人员对话框 -->
    <el-dialog v-model="partyDialogVisible" title="添加涉案人员" width="600px" :close-on-click-modal="false">
      <el-form :model="partyForm" :rules="partyRules" ref="partyFormRef" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="姓名" prop="name">
              <el-input v-model="partyForm.name" placeholder="请输入姓名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="性别" prop="gender">
              <el-select v-model="partyForm.gender" placeholder="请选择" style="width: 100%">
                <el-option label="男" :value="1" />
                <el-option label="女" :value="0" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="人员类型" prop="partyType">
              <el-select v-model="partyForm.partyType" placeholder="请选择" style="width: 100%">
                <el-option label="被调查人" :value="1" />
                <el-option label="证人" :value="2" />
                <el-option label="举报人" :value="3" />
                <el-option label="其他" :value="4" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系电话" prop="phone">
              <el-input v-model="partyForm.phone" placeholder="请输入联系电话" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="身份证号" prop="idCard">
          <el-input v-model="partyForm.idCard" placeholder="请输入身份证号" />
        </el-form-item>
        <el-form-item label="住址" prop="address">
          <el-input v-model="partyForm.address" placeholder="请输入住址" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="partyForm.remark" type="textarea" :rows="2" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="partyDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveParty">保存</el-button>
      </template>
    </el-dialog>

    <!-- 添加/编辑违法事实对话框 -->
    <el-dialog v-model="violationDialogVisible" :title="violationDialogTitle" width="600px" :close-on-click-modal="false">
      <el-form :model="violationForm" :rules="violationRules" ref="violationFormRef" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="类型" prop="violationType">
              <el-select v-model="violationForm.violationType" placeholder="请选择" style="width: 100%">
                <el-option label="违纪" :value="1" />
                <el-option label="违法" :value="2" />
                <el-option label="职务犯罪" :value="3" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="严重程度" prop="severity">
              <el-select v-model="violationForm.severity" placeholder="请选择" style="width: 100%">
                <el-option label="轻微" :value="1" />
                <el-option label="一般" :value="2" />
                <el-option label="严重" :value="3" />
                <el-option label="特别严重" :value="4" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="发生时间" prop="occurDate">
              <el-date-picker
                v-model="violationForm.occurDate"
                type="date"
                placeholder="请选择"
                value-format="YYYY-MM-DD"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="发生地点" prop="location">
              <el-input v-model="violationForm.location" placeholder="请输入发生地点" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="事实描述" prop="description">
          <el-input v-model="violationForm.description" type="textarea" :rows="4" placeholder="请输入事实描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="violationDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveViolation">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, RefreshLeft, Plus } from '@element-plus/icons-vue'
import { caseApi } from '@/api/case'

// ===== 映射字典 =====
const caseTypeMap = {
  1: { text: '违纪', type: 'warning' },
  2: { text: '违法', type: 'danger' },
  3: { text: '职务犯罪', type: 'info' },
}

const statusMap = {
  0: { text: '草稿', type: 'info' },
  1: { text: '审理中', type: 'warning' },
  2: { text: '已完结', type: 'success' },
  3: { text: '已归档', type: '' },
}

const partyTypeMap = {
  1: '被调查人',
  2: '证人',
  3: '举报人',
  4: '其他',
}

const genderMap = {
  0: '女',
  1: '男',
}

const severityMap = {
  1: { text: '轻微', type: 'info' },
  2: { text: '一般', type: '' },
  3: { text: '严重', type: 'warning' },
  4: { text: '特别严重', type: 'danger' },
}

// ===== 搜索 =====
const searchForm = reactive({
  caseCode: '',
  caseName: '',
  caseType: undefined,
  status: undefined,
  respondentName: '',
  dateRange: null,
})

// ===== 分页 =====
const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0,
})

// ===== 表格 =====
const tableData = ref([])
const loading = ref(false)

// ===== 案件表单 =====
const caseDialogVisible = ref(false)
const caseDialogTitle = ref('')
const caseFormRef = ref()
const caseForm = reactive({
  id: null,
  caseCode: '',
  caseName: '',
  caseType: undefined,
  status: 0,
  respondentName: '',
  handlingDept: '',
  handlerName: '',
  filingDate: '',
  description: '',
  remark: '',
})

const caseRules = {
  caseCode: [{ required: true, message: '请输入案件编号', trigger: 'blur' }],
  caseName: [{ required: true, message: '请输入案件名称', trigger: 'blur' }],
  caseType: [{ required: true, message: '请选择案件类型', trigger: 'change' }],
  respondentName: [{ required: true, message: '请输入被调查人', trigger: 'blur' }],
  filingDate: [{ required: true, message: '请选择立案日期', trigger: 'change' }],
}

// ===== 详情 =====
const detailDialogVisible = ref(false)
const detailData = reactive({})
const activeTab = ref('basic')

// ===== 涉案人员 =====
const parties = ref([])
const partiesLoading = ref(false)
const partyDialogVisible = ref(false)
const partyFormRef = ref()
const partyForm = reactive({
  id: null,
  name: '',
  gender: 1,
  partyType: 1,
  phone: '',
  idCard: '',
  address: '',
  remark: '',
})

const partyRules = {
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  partyType: [{ required: true, message: '请选择人员类型', trigger: 'change' }],
}

// ===== 违法事实 =====
const violations = ref([])
const violationsLoading = ref(false)
const violationDialogVisible = ref(false)
const violationDialogTitle = ref('')
const violationFormRef = ref()
const violationForm = reactive({
  id: null,
  violationType: undefined,
  description: '',
  occurDate: '',
  location: '',
  severity: 2,
})

const violationRules = {
  violationType: [{ required: true, message: '请选择类型', trigger: 'change' }],
  description: [{ required: true, message: '请输入事实描述', trigger: 'blur' }],
}

// ===== 状态变更 =====
const statusDialogVisible = ref(false)
const statusForm = reactive({
  caseId: null,
  currentStatus: 0,
  newStatus: undefined,
})

// ===== 当前案件ID =====
const currentCaseId = ref(null)

// ===== 加载案件列表 =====
const loadCases = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
    }
    if (searchForm.caseCode) params.caseCode = searchForm.caseCode
    if (searchForm.caseName) params.caseName = searchForm.caseName
    if (searchForm.caseType !== undefined && searchForm.caseType !== null) params.caseType = searchForm.caseType
    if (searchForm.status !== undefined && searchForm.status !== null) params.status = searchForm.status
    if (searchForm.respondentName) params.respondentName = searchForm.respondentName
    if (searchForm.dateRange && searchForm.dateRange.length === 2) {
      params.startDate = searchForm.dateRange[0]
      params.endDate = searchForm.dateRange[1]
    }
    const res = await caseApi.pageCase(params)
    tableData.value = res.data.list || []
    pagination.total = res.data.total || 0
  } catch (e) {
    console.error('加载案件列表失败', e)
  } finally {
    loading.value = false
  }
}

// ===== 搜索 =====
const handleSearch = () => {
  pagination.pageNum = 1
  loadCases()
}

const resetSearch = () => {
  searchForm.caseCode = ''
  searchForm.caseName = ''
  searchForm.caseType = undefined
  searchForm.status = undefined
  searchForm.respondentName = ''
  searchForm.dateRange = null
  pagination.pageNum = 1
  loadCases()
}

// ===== 新增 =====
const handleAdd = () => {
  Object.assign(caseForm, {
    id: null, caseCode: '', caseName: '', caseType: undefined,
    status: 0, respondentName: '', handlingDept: '',
    handlerName: '', filingDate: '', description: '', remark: '',
  })
  caseDialogTitle.value = '新增案件'
  caseDialogVisible.value = true
}

// ===== 编辑 =====
const handleEdit = async (row) => {
  try {
    const res = await caseApi.getCase(row.id)
    Object.assign(caseForm, res.data)
    caseDialogTitle.value = '编辑案件'
    caseDialogVisible.value = true
  } catch (e) {
    console.error('获取案件详情失败', e)
  }
}

// ===== 保存案件 =====
const saveCase = async () => {
  try {
    await caseFormRef.value.validate()
    if (caseForm.id) {
      await caseApi.updateCase({ ...caseForm })
      ElMessage.success('编辑成功')
    } else {
      await caseApi.addCase({ ...caseForm })
      ElMessage.success('新增成功')
    }
    caseDialogVisible.value = false
    loadCases()
  } catch (e) {
    if (e?.message) {
      // validation error
    } else {
      console.error('保存失败', e)
    }
  }
}

// ===== 查看详情 =====
const handleView = async (row) => {
  try {
    const res = await caseApi.getCase(row.id)
    Object.assign(detailData, res.data)
    currentCaseId.value = row.id
    activeTab.value = 'basic'
    detailDialogVisible.value = true
    await loadParties()
    await loadViolations()
  } catch (e) {
    console.error('获取案件详情失败', e)
  }
}

// ===== 删除 =====
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定删除案件「${row.caseName}」吗？`, '提示', { type: 'warning' })
    await caseApi.deleteCase(row.id)
    ElMessage.success('删除成功')
    loadCases()
  } catch (e) {
    if (e !== 'cancel') {
      console.error('删除失败', e)
    }
  }
}

// ===== 状态变更 =====
const handleChangeStatus = (row) => {
  statusForm.caseId = row.id
  statusForm.currentStatus = row.status
  statusForm.newStatus = undefined
  statusDialogVisible.value = true
}

const confirmChangeStatus = async () => {
  if (statusForm.newStatus === undefined || statusForm.newStatus === null) {
    ElMessage.warning('请选择目标状态')
    return
  }
  if (statusForm.newStatus === statusForm.currentStatus) {
    ElMessage.warning('目标状态与当前状态相同')
    return
  }
  try {
    await caseApi.changeStatus(statusForm.caseId, statusForm.newStatus)
    ElMessage.success('状态变更成功')
    statusDialogVisible.value = false
    loadCases()
  } catch (e) {
    console.error('状态变更失败', e)
  }
}

// ===== 涉案人员 =====
const loadParties = async () => {
  if (!currentCaseId.value) return
  partiesLoading.value = true
  try {
    const res = await caseApi.getParties(currentCaseId.value)
    parties.value = res.data || []
  } catch (e) {
    console.error('加载涉案人员失败', e)
  } finally {
    partiesLoading.value = false
  }
}

const handleAddParty = () => {
  Object.assign(partyForm, {
    id: null, name: '', gender: 1, partyType: 1,
    phone: '', idCard: '', address: '', remark: '',
  })
  partyDialogVisible.value = true
}

const saveParty = async () => {
  try {
    await partyFormRef.value.validate()
    await caseApi.addParty({
      ...partyForm,
      caseId: currentCaseId.value,
    })
    ElMessage.success('添加成功')
    partyDialogVisible.value = false
    await loadParties()
  } catch (e) {
    if (!e?.message) console.error('保存人员失败', e)
  }
}

const handleDeleteParty = async (row) => {
  try {
    await ElMessageBox.confirm(`确定删除涉案人员「${row.name}」吗？`, '提示', { type: 'warning' })
    await caseApi.deleteParty(row.id)
    ElMessage.success('删除成功')
    await loadParties()
  } catch (e) {
    if (e !== 'cancel') console.error('删除失败', e)
  }
}

// ===== 违法事实 =====
const loadViolations = async () => {
  if (!currentCaseId.value) return
  violationsLoading.value = true
  try {
    const res = await caseApi.getViolationFacts(currentCaseId.value)
    violations.value = res.data || []
  } catch (e) {
    console.error('加载违法事实失败', e)
  } finally {
    violationsLoading.value = false
  }
}

const handleAddViolation = () => {
  Object.assign(violationForm, {
    id: null, violationType: undefined, description: '',
    occurDate: '', location: '', severity: 2,
  })
  violationDialogTitle.value = '添加违法事实'
  violationDialogVisible.value = true
}

const handleEditViolation = (row) => {
  Object.assign(violationForm, { ...row })
  violationDialogTitle.value = '编辑违法事实'
  violationDialogVisible.value = true
}

const saveViolation = async () => {
  try {
    await violationFormRef.value.validate()
    if (violationForm.id) {
      await caseApi.updateViolationFact({ ...violationForm })
      ElMessage.success('编辑成功')
    } else {
      await caseApi.addViolationFact({
        ...violationForm,
        caseId: currentCaseId.value,
      })
      ElMessage.success('添加成功')
    }
    violationDialogVisible.value = false
    await loadViolations()
  } catch (e) {
    if (!e?.message) console.error('保存失败', e)
  }
}

const handleDeleteViolation = async (row) => {
  try {
    await ElMessageBox.confirm('确定删除该违法事实吗？', '提示', { type: 'warning' })
    await caseApi.deleteViolationFact(row.id)
    ElMessage.success('删除成功')
    await loadViolations()
  } catch (e) {
    if (e !== 'cancel') console.error('删除失败', e)
  }
}

onMounted(() => {
  loadCases()
})
</script>

<style lang="scss" scoped>
.case-management-page {
  padding: 4px;

  .card-header {
    font-weight: 600;
    font-size: 16px;
  }

  .search-form {
    margin-bottom: 10px;
  }

  .toolbar {
    margin-bottom: 16px;
  }

  .dialog-header {
    display: flex;
    align-items: center;
    justify-content: space-between;

    .case-code {
      font-size: 14px;
      color: #909399;
    }
  }

  .tab-content {
    min-height: 200px;
  }
}
</style>
