<template>
  <div class="user-management">
    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="用户名">
          <el-input v-model="searchForm.username" placeholder="请输入用户名" clearable />
        </el-form-item>
        <el-form-item label="创建时间">
          <el-date-picker
              v-model="searchForm.createTimeRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
      <el-button type="success" @click="openAddDialog">+ 新增用户</el-button>
    </div>

    <!-- 用户表格 -->
    <el-table :data="displayData" border stripe v-loading="loading">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="username" label="用户名" min-width="120" />
      <el-table-column prop="role" label="角色" width="100">
        <template #default="{ row }">
          <el-tag :type="row.role === 'ADMIN' ? 'danger' : 'success'">
            {{ row.role }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="注册时间" width="170">
        <template #default="{ row }">{{ formatDate(row.createTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="200" fixed="right" align="center">
        <template #default="{ row }">
          <el-button link type="primary" size="small" @click="openEditDialog(row)">编辑</el-button>
          <el-button link type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          <el-button link type="info" size="small" @click="viewOrders(row)">查看订单</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页（后端分页） -->
    <div class="pagination-container">
      <el-pagination
          :current-page="currentPage"
          :page-size="pageSize"
          :total="total"
          :page-size-options="[10, 20, 30, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          @current-change="handleCurrentChange"
          @size-change="handleSizeChange"
      />
    </div>

    <!-- 新增/编辑用户弹窗（保持不变） -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form ref="formRef" :model="formData" label-width="80px">
        <el-form-item label="用户名" required>
          <el-input v-model="formData.username" />
        </el-form-item>
        <el-form-item label="密码" :required="!formData.id">
          <el-input v-model="formData.password" type="password" show-password />
          <div v-if="formData.id" class="form-tip">留空则不修改密码</div>
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="formData.role" disabled>
            <el-option label="普通用户" value="USER" />
          </el-select>
          <div class="form-tip">管理员只能创建普通用户</div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>

    <!-- 查看订单对话框（保持不变） -->
    <el-dialog v-model="orderDialogVisible" title="用户订单" width="800px">
      <el-tabs v-model="activeOrderTab" type="border-card">
        <el-tab-pane label="全部订单" name="all">
          <div v-for="order in userOrders" :key="order.id" class="order-card">
            <div class="order-header">
              <span>订单号：{{ order.id }}</span>
              <span>总金额：¥{{ order.totalPrice?.toFixed(2) }}</span>
              <el-tag :type="getOrderStatusType(order.status)">{{ order.status }}</el-tag>
              <span>{{ formatDate(order.createTime) }}</span>
            </div>
            <el-table :data="order.items" border size="small">
              <el-table-column prop="name" label="菜品" />
              <el-table-column prop="quantity" label="数量" width="80" />
              <el-table-column prop="price" label="单价" width="100">
                <template #default="{ row }">¥{{ row.price?.toFixed(2) }}</template>
              </el-table-column>
              <el-table-column label="小计" width="100">
                <template #default="{ row }">¥{{ (row.price * row.quantity).toFixed(2) }}</template>
              </el-table-column>
            </el-table>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAdminUserPage, updateUser, deleteUser, register } from '@/api/user'
import { getOrderDetail, getUserOrders } from "@/api/orders.ts"
import type { User } from "@/types/User.ts"
import type { Orders } from "@/types/Orders.ts"

// 搜索表单
const searchForm = reactive({
  username: '',
  createTimeRange: [] as string[]
})

// 表格数据
const displayData = ref<User[]>([])
const loading = ref(false)

// 分页相关
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 获取用户列表（后端分页，只查 role=USER）
const fetchUserList = async () => {
  loading.value = true
  try {
    const params = {
      username: searchForm.username || undefined,
      startTime: searchForm.createTimeRange?.[0] || undefined,
      endTime: searchForm.createTimeRange?.[1] || undefined,
      pageNum: currentPage.value,
      pageSize: pageSize.value
    }
    const res = await getAdminUserPage(params)
    // 假设后端返回格式为 { content, totalElements }
    displayData.value = res.data.content
    total.value = res.data.totalElements
  } catch (error) {
    ElMessage.error('获取用户列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索/重置
const handleSearch = () => {
  currentPage.value = 1
  fetchUserList()
}
const resetSearch = () => {
  searchForm.username = ''
  searchForm.createTimeRange = []
  handleSearch()
}

// 分页事件
const handleCurrentChange = (page: number) => {
  currentPage.value = page
  fetchUserList()
}
const handleSizeChange = (size: number) => {
  pageSize.value = size
  currentPage.value = 1
  fetchUserList()
}

// 新增/编辑弹窗
const dialogVisible = ref(false)
const dialogTitle = ref('')
// const formRef = ref()
const formData = reactive({
  id: null as number | null,
  username: '',
  password: '',
  role: 'USER'
})

const openAddDialog = () => {
  dialogTitle.value = '新增用户'
  Object.assign(formData, { id: null, username: '', password: '', role: 'USER' })
  dialogVisible.value = true
}

const openEditDialog = (row: User) => {
  dialogTitle.value = '编辑用户'
  Object.assign(formData, { id: row.id, username: row.username, password: '', role: row.role })
  dialogVisible.value = true
}

const submitForm = async () => {
  if (!formData.username) {
    ElMessage.warning('请输入用户名')
    return
  }
  try {
    if (formData.id) {
      // 编辑
      const payload: any = { username: formData.username, role: formData.role }
      if (formData.password) payload.password = formData.password
      await updateUser(formData.id, payload)
      ElMessage.success('更新成功')
    } else {
      // 新增
      if (!formData.password) {
        ElMessage.warning('请输入密码')
        return
      }
      await register({
        username: formData.username,
        password: formData.password,
        role: 'USER'
      })
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    await fetchUserList()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

// 删除用户
const handleDelete = (row: User) => {
  ElMessageBox.confirm(`确定删除用户“${row.username}”吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    if (row.id) {
      await deleteUser(row.id)
      ElMessage.success('删除成功')
      await fetchUserList()
    }
  }).catch(() => {})
}

// 查看订单（保持不变）
const orderDialogVisible = ref(false)
const userOrders = ref<Orders[]>([])
const activeOrderTab = ref('all')

const viewOrders = async (user: User) => {
  try {
    if (user.id) {
      const res = await getUserOrders(user.id)
      const orders = res.data
      const detailRequests = orders.map(order =>
          getOrderDetail(order.id)
              .then(detailRes => ({ ...order, items: detailRes.data.items || [] }))
              .catch(() => ({ ...order, items: [] }))
      )
      userOrders.value = await Promise.all(detailRequests)
      orderDialogVisible.value = true
    }
  } catch (error) {
    ElMessage.error('获取订单失败')
  }
}

const getOrderStatusType = (status: 'PENDING' | 'ACCEPTED' | 'COOKING' | 'FINISHED'): string => {
  const map = {
    'PENDING': 'warning',
    'ACCEPTED': 'success',
    'COOKING': 'info',
    'FINISHED': 'danger',
    'CANCELED': 'warning'
  }
  return map[status] || 'info'
}

const formatDate = (dateStr: string) => {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleString()
}

onMounted(() => {
  fetchUserList()
})
</script>

<style scoped>
/* 样式保持不变，可复用原有 */
.user-management {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: 100vh;
}
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
.order-card {
  border: 1px solid #ebeef5;
  border-radius: 8px;
  padding: 12px;
  margin-bottom: 16px;
}
.order-header {
  display: flex;
  gap: 16px;
  align-items: center;
  margin-bottom: 12px;
  font-size: 14px;
}
.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}
</style>