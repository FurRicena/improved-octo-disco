<template>
  <div class="order-success">
    <div class="success-card">
      <div class="icon-wrapper">
        <el-icon :size="64" color="#67c23a"><CircleCheckFilled /></el-icon>
      </div>
      <h2>订单提交成功</h2>
      <p>感谢您的点餐，我们将尽快为您制作</p>

      <el-card class="order-info" shadow="never">
        <div class="info-row">
          <span>订单号：</span>
          <span>{{ orderId }}</span>
          <el-button link type="primary" size="small" @click="copyOrderId">复制</el-button>
        </div>
        <div class="info-row">
          <span>下单时间：</span>
          <span>{{ orderTime }}</span>
        </div>
        <div class="info-row">
          <span>支付金额：</span>
          <span class="total-price">¥{{ totalPrice.toFixed(2) }}</span>
        </div>
        <div class="info-row" v-if="status === 'PENDING'">
          <span>支付状态：</span>
          <el-tag type="warning">待支付</el-tag>
        </div>
      </el-card>

      <div class="action-buttons">
        <el-button type="primary" @click="viewOrder">查看订单</el-button>
        <el-button @click="continueOrder">继续点餐</el-button>
        <el-button v-if="status === 'PENDING'" type="success" @click="goPay">去支付</el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { CircleCheckFilled } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()

const orderId = ref('')
const orderTime = ref('')
const totalPrice = ref(0)
const status = ref('PENDING')  // 从后端获取

onMounted(() => {
  // 从路由参数获取订单信息（下单成功后传递）
  orderId.value = route.query.orderId as string
  orderTime.value = route.query.orderTime as string
  totalPrice.value = Number(route.query.totalPrice)
  status.value = route.query.status as string || 'PENDING'
})

const copyOrderId = () => {
  navigator.clipboard.writeText(orderId.value)
  ElMessage.success('订单号已复制')
}

const viewOrder = () => {
  router.push({
    path: `/orderDetail`,
    query: {
      orderId: orderId.value
    }
  })
}

const continueOrder = () => {
  router.push('/orderMenu')  // 返回点菜页面
}

const goPay = () => {
  // 跳转到支付页面（如果有）
  router.push(`/pay?orderId=${orderId.value}`)
}
</script>

<style scoped>
.order-success {
  min-height: 100vh;
  background: #f5f7fa;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}
.success-card {
  max-width: 500px;
  width: 100%;
  background: white;
  border-radius: 16px;
  padding: 32px 24px;
  text-align: center;
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}
.icon-wrapper {
  margin-bottom: 16px;
}
.order-info {
  margin: 24px 0;
  text-align: left;
}
.info-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 12px;
  font-size: 14px;
}
.total-price {
  color: #f56c6c;
  font-weight: bold;
  font-size: 18px;
}
.action-buttons {
  display: flex;
  gap: 12px;
  justify-content: center;
  flex-wrap: wrap;
}
</style>