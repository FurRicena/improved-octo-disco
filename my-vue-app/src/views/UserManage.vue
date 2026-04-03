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
    <el-table :data="displayData" border stripe>
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

    <!-- 分页 -->
    <div class="pagination-container">
      <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          :page-size-options="[10, 20, 30, 50]"
          layout="total, sizes, prev, pager, next, jumper"
      />
    </div>

    <!-- 新增/编辑用户弹窗 -->
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

    <!-- 查看订单对话框 -->
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
              <el-table-column prop="menuName" label="菜品" />
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
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
// import { Plus } from '@element-plus/icons-vue'
import {getUserList, updateUser, deleteUser, register} from '@/api/user'  // 假设的API
import { getUserOrders} from "@/api/orders.ts";
import type { User } from "@/types/User.ts";
import type { Orders } from "@/types/Orders.ts";


// 搜索表单
const searchForm = reactive({
  username: '',
  createTimeRange: []
})

// 表格数据
const tableData = ref<User[]>([])
const displayData = ref<User[]>([])

//  分页相关
const currentPage = ref(1)
const pageSize = ref(10)
// total 会自动跟随 displayData.value.length 变化
const total = computed(() => displayData.value.length)

// // 展示数据（前端分页用，建议后端分页直接使用tableData）
// const displayData = computed(() => tableData.value)

// 获取用户列表（只含USER角色）
const fetchUserList = async () => {
  try {
    const res = await getUserList()
    tableData.value = res.data
    displayData.value = res.data
  } catch (error) {
    ElMessage.error('获取用户列表失败')
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

// 新增/编辑弹窗
const dialogVisible = ref(false)
const dialogTitle = ref('')
// const formRef = ref(null)
const formData = reactive({
  id: null,
  username: '',
  password: '',
  role: 'USER'
})

const openAddDialog = () => {
  dialogTitle.value = '新增用户'
  Object.assign(formData, { id: null, username: '', password: '', role: 'USER' })
  dialogVisible.value = true
}

const openEditDialog = (row : User) => {
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
      if(formData.password) {
        const payload = { username: formData.username, role: formData.role, password: formData.password }
        await updateUser(formData.id, payload)
        ElMessage.success('更新成功')
      } else {
        const payload = { username: formData.username, role: formData.role }
        await updateUser(formData.id, payload)
        ElMessage.success('更新成功')
      }


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
const handleDelete = (row : User) => {
  ElMessageBox.confirm(`确定删除用户“${row.username}”吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    if(row.id) {
      await deleteUser(row.id)
      ElMessage.success('删除成功')
      await fetchUserList()
    }
  }).catch(() => {})
}

// 查看订单
const orderDialogVisible = ref(false)
const userOrders = ref<Orders[]>([])
const activeOrderTab = ref('all')

const viewOrders = async (user : User) => {
  try {
    if (user.id) {
      const res = await getUserOrders(user.id)  // 返回订单列表，每个订单包含items
      userOrders.value = res.data
      orderDialogVisible.value = true
    }
  } catch (error) {
    ElMessage.error('获取订单失败')
  }
}

//    PENDING,   // 待接单
// ACCEPTED,  // 已接单
//     COOKING,   // 制作中
//     FINISHED   // 已完成

const getOrderStatusType = (status: 'PENDING' | 'ACCEPTED' | 'COOKING' | 'FINISHED'): string => {
  const map = {
    'PENDING': 'warning',
    'ACCEPTED': 'success',
    'COOKING': 'info',
    'FINISHED': 'danger'
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
/* 复用原有样式，添加订单卡片样式 */
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