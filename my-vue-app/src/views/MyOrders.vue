<template>
  <div class="my-orders">
    <h2>我的订单</h2>

    <!-- 状态筛选 Tab -->
    <el-tabs v-model="activeStatus" @tab-change="handleStatusChange">
      <el-tab-pane label="全部" name="all" />
      <el-tab-pane label="待接单" name="PENDING" />
      <el-tab-pane label="已接单" name="ACCEPTED" />
      <el-tab-pane label="制作中" name="COOKING" />
      <el-tab-pane label="已完成" name="FINISHED" />
      <el-tab-pane label="已取消" name="CANCELLED" />
    </el-tabs>

    <!-- 订单列表 -->
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="3" animated />
    </div>
    <div v-else-if="orderList.length === 0" class="empty-state">
      <el-empty description="暂无订单" />
    </div>
    <div v-else class="order-list">
      <el-card
          v-for="order in orderList"
          :key="order.id"
          class="order-card"
          shadow="hover"
          @click="viewDetail(order.id)"
      >
        <div class="order-header">
          <div class="order-info">
            <span class="order-id">订单号：{{ order.id }}</span>
            <span class="order-time">{{ formatDate(order.createTime) }}</span>
          </div>
          <el-tag :type="getStatusType(order.status)">
            {{ getStatusText(order.status) }}
          </el-tag>
        </div>
        <div class="order-footer">
          <span class="order-total">¥{{ order.totalPrice.toFixed(2) }}</span>
        </div>
      </el-card>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :total="total"
            :page-size-options="[5, 10, 20]"
            layout="total, sizes, prev, pager, next"
            @current-change="fetchOrders"
            @size-change="fetchOrders"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getUserOrdersPage } from '@/api/orders'
import { useUserStore } from '@/stores/user'
import type { Orders } from '@/types/Orders'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const orderList = ref<Orders[]>([])
const activeStatus = ref('all')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const fetchOrders = async () => {
  const userId = userStore.userInfo?.id
  if (!userId) {
    ElMessage.error('用户未登录')
    return
  }

  loading.value = true
  try {
    const res = await getUserOrdersPage(
        userId,
        currentPage.value,
        pageSize.value,
        activeStatus.value === 'all' ? undefined : activeStatus.value
    )
    console.log(currentPage.value)
    orderList.value = res.data.content
    total.value = res.data.totalElements
  } catch (error) {
    ElMessage.error('获取订单列表失败')
  } finally {
    loading.value = false
  }
}

const handleStatusChange = () => {
  currentPage.value = 1
  fetchOrders()
}

const viewDetail = (orderId: number) => {
  router.push({
    path: `/order-detail`,
    query: {
      orderId: orderId
    }
  })
}

const formatDate = (dateStr: string) => {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleString()
}

const getStatusType = (status: string) => {
  const map: Record<string, string> = {
    PENDING: 'warning',
    ACCEPTED: 'primary',
    COOKING: 'info',
    FINISHED: 'success',
    CANCELLED: 'danger'
  }
  return map[status] || 'info'
}

const getStatusText = (status: string) => {
  const map: Record<string, string> = {
    PENDING: '待接单',
    ACCEPTED: '已接单',
    COOKING: '制作中',
    FINISHED: '已完成',
    CANCELLED: '已取消'
  }
  return map[status] || status
}

onMounted(() => {
  fetchOrders()
})
</script>

<style scoped>
/* 样式保持不变，与之前相同 */
.my-orders {
  max-width: 900px;
  margin: 20px auto;
  padding: 0 16px;
}
.order-list {
  margin-top: 20px;
}
.order-card {
  margin-bottom: 16px;
  cursor: pointer;
  transition: transform 0.2s;
}
.order-card:hover {
  transform: translateY(-2px);
}
.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}
.order-info {
  display: flex;
  gap: 16px;
  font-size: 14px;
  color: #666;
}
.order-id {
  font-weight: bold;
  color: #333;
}
.order-footer {
  text-align: right;
}
.order-total {
  font-weight: bold;
  color: #f56c6c;
  font-size: 16px;
}
.pagination-container {
  margin-top: 24px;
  display: flex;
  justify-content: center;
}
.loading-container, .empty-state {
  padding: 40px 0;
}
</style>