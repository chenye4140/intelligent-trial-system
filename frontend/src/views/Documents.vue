<template>
  <div class="documents-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>📚 多库管理</span>
        </div>
      </template>

      <!-- 搜索栏 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="库类型">
          <el-select v-model="searchForm.docType" placeholder="全部" clearable style="width: 150px">
            <el-option label="法规库" value="regulation" />
            <el-option label="资料库" value="material" />
            <el-option label="裁判文书库" value="judgment" />
            <el-option label="案例库" value="case" />
          </el-select>
        </el-form-item>
        <el-form-item label="关键词">
          <el-input v-model="searchForm.keyword" placeholder="搜索标题或内容" clearable style="width: 200px" />
        </el-form-item>
        <el-form-item label="密级">
          <el-select v-model="searchForm.securityLevel" placeholder="全部" clearable style="width: 120px">
            <el-option label="绝密" value="top_secret" />
            <el-option label="机密" value="secret" />
            <el-option label="秘密" value="confidential" />
            <el-option label="内部" value="internal" />
            <el-option label="公开" value="public" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadDocuments">查询</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 文档列表 -->
      <el-table :data="tableData" v-loading="loading" border stripe style="margin-top: 20px">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="title" label="标题" min-width="200" />
        <el-table-column prop="docType" label="类型" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.docType === 'regulation'" type="danger">法规</el-tag>
            <el-tag v-else-if="row.docType === 'material'" type="warning">资料</el-tag>
            <el-tag v-else-if="row.docType === 'judgment'" type="info">裁判文书</el-tag>
            <el-tag v-else-if="row.docType === 'case'" type="success">案例</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="securityLevel" label="密级" width="80">
          <template #default="{ row }">
            <el-tag size="small">{{ securityMap[row.securityLevel] || row.securityLevel }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="validityStatus" label="有效性" width="80">
          <template #default="{ row }">
            <el-tag :type="row.validityStatus === 'valid' ? 'success' : 'danger'" size="small">
              {{ validityMap[row.validityStatus] || row.validityStatus }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="fileType" label="格式" width="70" />
        <el-table-column prop="parseStatus" label="解析状态" width="100">
          <template #default="{ row }">
            <el-tag :type="parseStatusMap[row.parseStatus]?.type || 'info'" size="small">
              {{ parseStatusMap[row.parseStatus]?.text || row.parseStatus }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="上传时间" width="160" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleView(row)">查看</el-button>
            <el-button type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        v-model:current-page="searchForm.page"
        v-model:page-size="searchForm.size"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @current-change="loadDocuments"
        @size-change="loadDocuments"
        style="margin-top: 20px; justify-content: flex-end"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { documentApi } from '@/api'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)

const securityMap = {
  top_secret: '绝密',
  secret: '机密',
  confidential: '秘密',
  internal: '内部',
  public: '公开',
}

const validityMap = {
  valid: '有效',
  invalid: '失效',
  draft: '草案',
}

const parseStatusMap = {
  pending: { text: '待解析', type: 'info' },
  parsing: { text: '解析中', type: 'warning' },
  parsed: { text: '已解析', type: 'success' },
  failed: { text: '解析失败', type: 'danger' },
}

const searchForm = reactive({
  page: 1,
  size: 10,
  docType: '',
  keyword: '',
  securityLevel: '',
})

const loadDocuments = async () => {
  loading.value = true
  try {
    const res = await documentApi.list(searchForm)
    // 后端 PageResult 结构：{ total, pageNum, pageSize, pages, list }
    tableData.value = res.data.list || []
    total.value = res.data.total || 0
  } catch (error) {
    console.error('加载文档失败', error)
  } finally {
    loading.value = false
  }
}

const resetSearch = () => {
  searchForm.docType = ''
  searchForm.keyword = ''
  searchForm.securityLevel = ''
  searchForm.page = 1
  loadDocuments()
}

const handleView = (row) => {
  ElMessage.info(`查看文档: ${row.title}`)
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定删除文档「${row.title}」吗？`, '提示', {
      type: 'warning',
    })
    await documentApi.delete(row.id)
    ElMessage.success('删除成功')
    loadDocuments()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败', error)
    }
  }
}

onMounted(() => {
  loadDocuments()
})
</script>

<style lang="scss" scoped>
.documents-page {
  .card-header {
    font-weight: 600;
    font-size: 16px;
  }

  .search-form {
    margin-bottom: 10px;
  }
}
</style>
