<template>
  <div class="logs-container">
    <h2>操作日志</h2>

    <!-- 搜索栏（参照 Menu 页面风格） -->
    <div class="search-bar">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="操作人">
          <el-input v-model="searchForm.operator" placeholder="请输入操作人" clearable />
        </el-form-item>
        <el-form-item label="操作类型">
          <el-input v-model="searchForm.operation" placeholder="请输入操作类型" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 日志表格 -->
    <el-table :data="logs" border v-loading="loading" stripe>
      <el-table-column prop="id" label="ID" width="80" align="center" />
      <el-table-column prop="operator" label="操作人" width="120" />
      <el-table-column prop="operation" label="操作类型" width="150" />
      <el-table-column prop="target" label="操作对象" width="150" />
      <el-table-column prop="detail" label="详情" show-overflow-tooltip />
      <el-table-column prop="ip" label="IP地址" width="150" />
      <el-table-column prop="createTime" label="操作时间" width="180">
        <template #default="{ row }">{{ formatDate(row.createTime) }}</template>
      </el-table-column>
    </el-table>

    <!-- 分页（后端分页） -->
    <div class="pagination-container">
      <el-pagination
          :current-page="currentPage"
          :page-size="pageSize"
          :total="total"
          :page-size-options="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @current-change="handleCurrentChange"
          @size-change="handleSizeChange"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getLogs, type LogQueryParams } from '@/api/log'
import type { Log } from '@/api/log.ts'

// 搜索表单（参照 Menu 的 searchForm）
const searchForm = reactive({
  operator: '',
  operation: ''
})

const logs = ref<Log[]>([])
const loading = ref(false)

// 分页相关
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 获取日志列表（支持筛选参数）
const fetchLogs = async () => {
  loading.value = true
  try {
    const params: LogQueryParams = {
      // 只有非空字符串才传给后端（与 Menu 示例一致）
      operator: searchForm.operator || undefined,
      operation: searchForm.operation || undefined,
      pageNum: currentPage.value,
      pageSize: pageSize.value
    }
    const res = await getLogs(params)
    logs.value = res.data.content
    total.value = res.data.totalElements
  } catch (error) {
    console.error('获取日志失败', error)
    ElMessage.error('获取日志列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索（重置页码为第一页）
const handleSearch = () => {
  currentPage.value = 1
  fetchLogs()
}

// 重置搜索（清空表单，重置页码，重新查询）
const resetSearch = () => {
  searchForm.operator = ''
  searchForm.operation = ''
  currentPage.value = 1
  fetchLogs()
}

// 页码改变
const handleCurrentChange = (page: number) => {
  currentPage.value = page
  fetchLogs()
}

// 每页条数改变
const handleSizeChange = (size: number) => {
  pageSize.value = size
  currentPage.value = 1
  fetchLogs()
}

// 格式化日期
const formatDate = (dateStr: string) => {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleString()
}

onMounted(() => {
  fetchLogs()
})
</script>

<style scoped>
.logs-container {
  padding: 20px;
  background: #f5f7fa;
  min-height: 100vh;
}

/* 参照 Menu 页面的搜索栏样式 */
.search-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  background: white;
  padding: 16px 20px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.search-form {
  flex: 1;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
  background: white;
  padding: 12px 16px;
  border-radius: 8px;
}
</style>