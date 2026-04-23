<template>
  <div class="order-detail">
    <el-card v-loading="loading">
      <template #header>
        <div class="card-header">
          <span>订单详情</span>
          <el-button link @click="router.back()">返回</el-button>
        </div>
      </template>

      <div v-if="order" class="detail-content">
        <!-- 订单基本信息 -->
        <div class="info-section">
          <h4>订单信息</h4>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="订单号">{{ order.id }}</el-descriptions-item>
            <el-descriptions-item label="下单时间">{{ formatDate(order.createTime) }}</el-descriptions-item>
            <el-descriptions-item label="订单状态">
              <el-tag :type="getStatusType(order.status)">
                {{ getStatusText(order.status) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="总金额">¥{{ order.totalPrice?.toFixed(2) }}</el-descriptions-item>
          </el-descriptions>
        </div>

        <!-- 菜品明细 -->
        <div class="items-section">
          <h4>菜品清单</h4>
          <el-table :data="order.items || []" border stripe>
            <el-table-column prop="name" label="菜品名称" min-width="150" />
            <el-table-column prop="quantity" label="数量" width="80" align="center" />
            <el-table-column prop="price" label="单价" width="100" align="center">
              <template #default="{ row }">¥{{ row.price?.toFixed(2) }}</template>
            </el-table-column>
            <el-table-column label="小计" width="100" align="center">
              <template #default="{ row }">¥{{ ((row.price || 0) * row.quantity).toFixed(2) }}</template>
            </el-table-column>

            <!-- 新增一列：操作列，根据订单状态显示评价按钮 -->
            <el-table-column label="操作" width="100" align="center" v-if="order.status === 'FINISHED'">
              <template #default="{ row }">
                <el-button type="primary" size="small" @click="goToComment(row.menuId)">
                  去评价
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>

        <!-- 操作按钮（根据状态显示） -->
        <div class="action-buttons" v-if="order.status === 'PENDING'">
          <el-button type="danger" @click="cancelOrder">取消订单</el-button>
          <el-button type="success" @click="goPay">去支付</el-button>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {ElMessage, ElMessageBox} from 'element-plus'
// import { ElMessageBox} from "element-plus";
import {getOrderDetail, updateOrderStatus} from '@/api/orders'
// import { cancelOrder as cancelOrderApi } from '@/api/orders'
import type { Orders } from '@/types/Orders'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const order = ref<Orders | null>(null)
const orderId = ref()

onMounted(() => {
  // 从路由参数获取订单信息（下单成功后传递）
  orderId.value = route.query.orderId as string
})

const fetchDetail = async () => {
  if (!orderId.value) {
    ElMessage.error('订单ID无效')
    router.back()
    return
  }
  loading.value = true
  try {
    const res = await getOrderDetail(orderId.value)
    order.value = res.data
    console.log(res.data)
  } catch (error) {
    ElMessage.error('获取订单详情失败')
    router.back()
  } finally {
    loading.value = false
  }
}

// 取消订单
const cancelOrder = async () => {
  await ElMessageBox.confirm('确定要取消该订单吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
  try {
    await updateOrderStatus(order.value!.id, "CANCELLED")
    ElMessage.success('订单已取消')
    await fetchDetail()  // 刷新状态
  } catch (error) {
    ElMessage.error('取消失败')
  }
}

// 跳转评论
const goToComment = (menuId: number) => {
  router.push({
    path: `/menu-detail`,
    query: { id: menuId }
    // 也可以附带查询参数，如跳转后自动打开评论框
    // query: { orderId: orderId, autoComment: true }
  })
}

const goPay = () => {
  // 跳转到支付页面（如果有）
  router.push(`/pay?orderId=${order.value!.id}`)
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
    CANCELLED: 'warning'
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
  fetchDetail()
})
</script>

<style scoped>
.order-detail {
  max-width: 1000px;
  margin: 20px auto;
  padding: 0 16px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.info-section, .items-section {
  margin-bottom: 24px;
}
h4 {
  margin-bottom: 12px;
  color: #333;
}
.action-buttons {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
  margin-top: 24px;
}
</style>