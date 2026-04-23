<template>
  <div class="admin-orders">
    <h2>订单管理</h2>

    <!-- 筛选栏 -->
    <el-form :inline="true" :model="searchForm" class="filter-form">
      <el-form-item label="用户名">
        <el-input v-model="searchForm.username" placeholder="请输入用户名" clearable />
      </el-form-item>
      <el-form-item label="订单状态">
        <el-select v-model="searchForm.status" placeholder="全部" style="width: 240px" clearable>
          <el-option label="待接单" value="PENDING" />
          <el-option label="已接单" value="ACCEPTED" />
          <el-option label="制作中" value="COOKING" />
          <el-option label="已完成" value="FINISHED" />
          <el-option label="已取消" value="CANCELLED" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSearch">搜索</el-button>
        <el-button @click="resetSearch">重置</el-button>
        <el-button type="warning" @click="exportOrdersHandler" :loading="exportLoading">导出订单</el-button>
      </el-form-item>
    </el-form>

    <!-- 订单表格 -->
    <el-table :data="orderList" border stripe v-loading="loading">
      <el-table-column prop="id" label="订单号" width="100" />
      <el-table-column prop="username" label="用户名" width="120" />
      <el-table-column prop="totalPrice" label="总金额" width="120">
        <template #default="{ row }">¥{{ row.totalPrice.toFixed(2) }}</template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.status)">
            {{ getStatusText(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="下单时间" width="180">
        <template #default="{ row }">{{ formatDate(row.createTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="240" fixed="right">
        <template #default="{ row }">
          <el-button
              v-if="row.status === 'PENDING'"
              type="primary"
              size="small"
              @click="changeStatus(row.id, 'ACCEPTED')"
          >接单</el-button>
          <el-button
              v-if="row.status === 'ACCEPTED'"
              type="warning"
              size="small"
              @click="changeStatus(row.id, 'COOKING')"
          >开始制作</el-button>
          <el-button
              v-if="row.status === 'COOKING'"
              type="success"
              size="small"
              @click="changeStatus(row.id, 'FINISHED')"
          >完成</el-button>
          <el-button
              v-if="row.status === 'PENDING'"
              type="danger"
              size="small"
              @click="cancelOrder(row.id)"
          >取消订单</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
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
import { ElMessage, ElMessageBox } from 'element-plus'
import {exportOrders, getAdminOrdersPage, updateOrderStatus} from '@/api/orders.ts'
import type { Orders } from '@/types/Orders.ts'

// 扩展订单类型，添加用户名
interface AdminOrder extends Orders {
  username: string
}

const loading = ref(false)
const orderList = ref<AdminOrder[]>([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const searchForm = reactive({
  username: '',
  status: ''
})

const fetchOrders = async () => {
  loading.value = true
  try {
    const params = {
      username: searchForm.username || undefined,
      status: searchForm.status || undefined,
      pageNum: currentPage.value,
      pageSize: pageSize.value
    }
    const res = await getAdminOrdersPage(params)
    // 假设返回格式为 { content, totalElements }
    orderList.value = res.data.content
    total.value = res.data.totalElements
  } catch (error) {
    ElMessage.error('获取订单列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  currentPage.value = 1
  fetchOrders()
}

const resetSearch = () => {
  searchForm.username = ''
  searchForm.status = 'PENDING'
  handleSearch()
}

const handleCurrentChange = (page: number) => {
  currentPage.value = page
  fetchOrders()
}

const handleSizeChange = (size: number) => {
  pageSize.value = size
  currentPage.value = 1
  fetchOrders()
}

const changeStatus = async (orderId: number, newStatus: string) => {
  try {
    await updateOrderStatus(orderId, newStatus)
    ElMessage.success('状态更新成功')
    await fetchOrders() // 刷新列表
  } catch (error) {
    ElMessage.error('状态更新失败')
  }
}

const cancelOrder = async (orderId: number) => {
  await ElMessageBox.confirm('确定要取消该订单吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
  await changeStatus(orderId, 'CANCELLED')
}

const getStatusType = (status: string) => {
  const map: Record<string, string> = {
    PENDING: 'warning',
    ACCEPTED: 'primary',
    COOKING: 'info',
    FINISHED: 'success'
    // CANCELLED: 'danger'
  }
  return map[status] || 'info'
}

const getStatusText = (status: string) => {
  const map: Record<string, string> = {
    PENDING: "待接单",
    ACCEPTED: "已接单",
    COOKING: "制作中",
    FINISHED: "已完成",
    CANCELLED: '已取消'
  }
  return map[status] || status
}

const formatDate = (dateStr: string) => {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleString()
}

const exportLoading = ref(false)

const exportOrdersHandler = async () => {
  exportLoading.value = true
  try {
    const params = {
      username: searchForm.username || undefined,
      status: searchForm.status || undefined
    }
    const response = await exportOrders(params)

    // 关键：判断响应类型是否是 application/json（表示后端返回了错误）
    const contentType = response.headers['content-type'] || ''
    if (contentType.includes('application/json')) {
      // 将 blob 转为字符串读取错误信息
      const text = await response.data.text()
      const errorData = JSON.parse(text)
      ElMessage.error(errorData.msg || '导出失败')
      return
    }

    // 正常导出 Excel
    const blob = new Blob([response.data], {
      type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
    })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.setAttribute('download', `orders_${new Date().getTime()}.xlsx`)
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)

    ElMessage.success('导出成功')
  } catch (error) {
    ElMessage.error('导出失败，请重试')
  } finally {
    exportLoading.value = false
  }
}

onMounted(() => {
  fetchOrders()
})
</script>

<style scoped>
.admin-orders {
  padding: 20px;
}
.filter-form {
  margin-bottom: 20px;
}
.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>