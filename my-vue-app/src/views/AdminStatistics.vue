<template>
  <div class="statistics-container">
    <h2>菜品销量统计</h2>
    <el-table :data="salesData" border stripe v-loading="loading">
      <el-table-column prop="menuId" label="菜品ID" width="80" />
      <el-table-column prop="menuName" label="菜品名称" min-width="150" />
      <el-table-column prop="price" label="单价" width="100">
        <template #default="{ row }">¥{{ row.price.toFixed(2) }}</template>
      </el-table-column>
      <el-table-column prop="totalSold" label="总销量" width="120" sortable />
      <el-table-column label="销售额" width="120">
        <template #default="{ row }">¥{{ (row.price * row.totalSold).toFixed(2) }}</template>
      </el-table-column>
    </el-table>

    <!-- 可选：使用 ECharts 展示柱状图 -->
    <div v-if="salesData.length" ref="chartRef" style="height: 400px; margin-top: 30px;"></div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import { getMenuSales, type MenuSales } from '@/api/statistics'
// import * as THREE from 'three'

const loading = ref(false)
const salesData = ref<MenuSales[]>([])
const chartRef = ref<HTMLElement>()

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getMenuSales()
    salesData.value = res.data
  } catch (error) {
    ElMessage.error('获取统计数据失败')
  } finally {
    loading.value = false
  }
}

// 绘制柱状图（按销量）
const renderChart = () => {
  if (!chartRef.value || !salesData.value.length) return
  const chart = echarts.init(chartRef.value)
  const names = salesData.value.map(item => item.menuName)
  const sold = salesData.value.map(item => item.totalSold)
  chart.setOption({
    title: { text: '菜品销量排行', left: 'center' },
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    xAxis: { type: 'category', data: names, axisLabel: { rotate: 30 } },
    yAxis: { type: 'value', name: '销量' },
    series: [{ type: 'bar', data: sold, itemStyle: { color: '#409EFF' } }]
  })
}

onMounted(() => {
  fetchData()
})

watch(salesData, async () => {
  await nextTick()
  renderChart()
})
</script>

<style scoped>
.statistics-container {
  padding: 20px;
  background: #f5f7fa;
  min-height: 100vh;
}
</style>