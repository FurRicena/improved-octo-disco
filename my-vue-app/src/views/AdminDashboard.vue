<template>
  <div class="dashboard-container">
    <h2>数据统计仪表盘</h2>

    <!-- 指标卡片 -->
    <el-row :gutter="20" class="indicator-row">
      <el-col :span="6">
        <el-card class="indicator-card">
          <div class="card-icon"><el-icon><User /></el-icon></div>
          <div class="card-content">
            <div class="card-value">{{ dashboardData.totalUsers }}</div>
            <div class="card-label">总用户数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="indicator-card">
          <div class="card-icon"><el-icon><ShoppingCart /></el-icon></div>
          <div class="card-content">
            <div class="card-value">{{ dashboardData.totalOrders }}</div>
            <div class="card-label">总订单数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="indicator-card">
          <div class="card-icon"><el-icon><Money /></el-icon></div>
          <div class="card-content">
            <div class="card-value">¥{{ dashboardData.totalSales.toFixed(2) }}</div>
            <div class="card-label">总销售额</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="indicator-card">
          <div class="card-icon"><el-icon><TrendCharts /></el-icon></div>
          <div class="card-content">
            <div class="card-value">{{ completionRate }}%</div>
            <div class="card-label">订单完成率</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="20">
      <el-col :span="16">
        <el-card class="chart-card">
          <template #header>近7天订单趋势</template>
          <div ref="trendChartRef" style="height: 350px"></div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="chart-card">
          <template #header>订单状态分布</template>
          <div ref="statusChartRef" style="height: 350px"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="24">
        <el-card class="chart-card">
          <template #header>热门菜品TOP5</template>
          <el-table :data="dashboardData.top5Menus" border stripe>
            <el-table-column prop="menuName" label="菜品名称" />
            <el-table-column prop="totalSold" label="销量" sortable />
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, watch, nextTick } from 'vue'
import * as echarts from 'echarts'
import { getDashboardData, type DashboardData } from '@/api/statistics'
import { ElMessage } from 'element-plus'
import { User, ShoppingCart, Money, TrendCharts } from '@element-plus/icons-vue'

const dashboardData = ref<DashboardData>({
  totalUsers: 0,
  totalOrders: 0,
  totalSales: 0,
  last7DaysTrend: [],
  top5Menus: [],
  statusDistribution: {}
})

const loading = ref(false)
const trendChartRef = ref<HTMLElement>()
const statusChartRef = ref<HTMLElement>()
let trendChart: echarts.ECharts | null = null
let statusChart: echarts.ECharts | null = null

// 订单完成率 = 已完成订单数 / 总订单数
const completionRate = computed(() => {
  const finished = dashboardData.value.statusDistribution['FINISHED'] || 0
  const total = dashboardData.value.totalOrders
  if (total === 0) return 0
  return ((finished / total) * 100).toFixed(1)
})

const fetchDashboard = async () => {
  loading.value = true
  try {
    const res = await getDashboardData()
    dashboardData.value = res.data
  } catch (error) {
    ElMessage.error('获取统计数据失败')
  } finally {
    loading.value = false
  }
}

const renderCharts = () => {
  if (!trendChartRef.value || !statusChartRef.value) return

  // 近7天趋势图（双轴：订单数量和销售额）
  if (trendChart) trendChart.dispose()
  trendChart = echarts.init(trendChartRef.value)
  const dates = dashboardData.value.last7DaysTrend.map(item => item.date)
  const orderCounts = dashboardData.value.last7DaysTrend.map(item => item.orderCount)
  const sales = dashboardData.value.last7DaysTrend.map(item => item.sales)
  trendChart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['订单数量', '销售额(元)'] },
    xAxis: { type: 'category', data: dates },
    yAxis: [{ type: 'value', name: '订单数量' }, { type: 'value', name: '销售额(元)' }],
    series: [
      { name: '订单数量', type: 'line', data: orderCounts, smooth: true },
      { name: '销售额(元)', type: 'bar', data: sales, yAxisIndex: 1 }
    ]
  })

  // 订单状态分布饼图
  if (statusChart) statusChart.dispose()
  statusChart = echarts.init(statusChartRef.value)
  const statusData = Object.entries(dashboardData.value.statusDistribution).map(([name, value]) => ({
    name: statusNameMap(name),
    value
  }))
  statusChart.setOption({
    tooltip: { trigger: 'item' },
    legend: { orient: 'vertical', left: 'left' },
    series: [{ type: 'pie', radius: '50%', data: statusData, label: { show: true } }]
  })
}

const statusNameMap = (status: string) => {
  const map: Record<string, string> = {
    PENDING: '待接单', ACCEPTED: '已接单', COOKING: '制作中', FINISHED: '已完成', CANCELLED: '已取消'
  }
  return map[status] || status
}

watch(dashboardData, async () => {
  await nextTick()
  renderCharts()
})

onMounted(() => {
  fetchDashboard()
})
</script>

<style scoped>
.dashboard-container {
  padding: 20px;
  background: #f5f7fa;
  min-height: 100vh;
}
.indicator-row {
  margin-bottom: 20px;
}
.indicator-card {
  text-align: center;
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.card-icon {
  font-size: 48px;
  color: #409eff;
}
.card-value {
  font-size: 28px;
  font-weight: bold;
}
.card-label {
  font-size: 14px;
  color: #666;
}
.chart-card {
  margin-bottom: 20px;
}
</style>