<template>
  <div class="menu-management">
    <!-- 搜索栏与操作栏 -->
    <div class="search-bar">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="菜品名称">
          <el-input v-model="searchForm.name" placeholder="请输入菜品名称" clearable />
        </el-form-item>
        <el-form-item label="菜品类别">
          <el-select v-model="searchForm.category" style="width: 240px" placeholder="请选择类别" clearable>
            <el-option label="凉菜" value="凉菜" />
            <el-option label="热菜" value="热菜" />
            <el-option label="汤羹" value="汤羹" />
            <el-option label="主食" value="主食" />
            <el-option label="饮品" value="饮品" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" style="width: 240px" placeholder="请选择状态" clearable>
            <el-option label="上架" :value="1" />
            <el-option label="下架" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
      <el-button type="success" @click="openAddDialog">+ 新增菜品</el-button>
    </div>

    <!-- 菜品表格 -->
    <el-table :data="displayData" border stripe v-loading="loading" style="width: 100%">
      <el-table-column prop="id" label="ID" width="80" align="center" />
      <el-table-column prop="name" label="菜品名称" min-width="120" />
      <el-table-column label="菜品图片" width="100" align="center">
        <template #default="{ row }">
          <el-image
              v-if="row.imageUrl"
              :src="row.imageUrl"
              :preview-src-list="[row.imageUrl]"
              fit="cover"
              style="width: 60px; height: 60px; border-radius: 4px"
          >
            <template #error>
              <div class="image-placeholder">暂无图片</div>
            </template>
          </el-image>
          <div v-else class="image-placeholder">无图片</div>
        </template>
      </el-table-column>
      <el-table-column prop="category" label="类别" width="100" align="center" />
      <el-table-column prop="price" label="价格(元)" width="100" align="center">
        <template #default="{ row }"> ¥{{ row.price?.toFixed(2) }} </template>
      </el-table-column>
      <el-table-column prop="description" label="菜品介绍" min-width="180" show-overflow-tooltip />
      <el-table-column prop="createTime" label="创建时间" width="170" align="center">
        <template #default="{ row }"> {{ formatDate(row.createTime) }} </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="90" align="center">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'">
            {{ row.status === 1 ? '上架' : '下架' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="150" fixed="right" align="center">
        <template #default="{ row }">
          <el-button link type="primary" size="small" @click="openEditDialog(row)">编辑</el-button>
          <el-button link type="danger" size="small" @click="handleDelete(row)">删除</el-button>
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

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px" destroy-on-close>
      <el-form ref="formRef" :model="formData" label-width="100px">
        <el-form-item label="菜品名称">
          <el-input v-model="formData.name" placeholder="请输入菜品名称" />
        </el-form-item>
        <el-form-item label="菜品类别">
          <el-select v-model="formData.category" placeholder="请选择类别" style="width: 100%">
            <el-option label="凉菜" value="凉菜" />
            <el-option label="热菜" value="热菜" />
            <el-option label="汤羹" value="汤羹" />
            <el-option label="主食" value="主食" />
            <el-option label="饮品" value="饮品" />
          </el-select>
        </el-form-item>
        <el-form-item label="菜品价格">
          <el-input-number v-model="formData.price" :precision="2" :min="0" :step="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="菜品状态">
          <el-radio-group v-model="formData.status">
            <el-radio :label="1">上架</el-radio>
            <el-radio :label="0">下架</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="菜品图片">
          <el-upload
              class="avatar-uploader"
              :auto-upload="false"
              :show-file-list="false"
              :on-change="handleImageChange"
              :before-upload="beforeImageUpload"
          >
            <img v-if="formData.imageUrl" :src="formData.imageUrl" class="avatar" alt="" />
            <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
          </el-upload>
        </el-form-item>
        <el-form-item label="菜品介绍">
          <el-input v-model="formData.description" type="textarea" :rows="3" placeholder="请输入菜品介绍" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitForm">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElLoading, ElMessage, ElMessageBox, type FormInstance, type UploadFile, type UploadProps } from 'element-plus'
import { addMenu, deleteMenu, updateMenu, getAdminMenuPage } from "@/api/menu.ts"
import { uploadFile } from "@/api/upload.ts"
import type { Menu } from "@/types/Menu.ts"
import { Plus } from "@element-plus/icons-vue"

// 搜索表单
const searchForm = reactive({
  name: '',
  category: '',
  status: undefined as number | undefined
})

// 表格数据
const displayData = ref<Menu[]>([])
const loading = ref(false)

// 分页相关
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const formRef = ref<FormInstance>()

// 获取菜品列表（后端分页）
const fetchMenuList = async () => {
  loading.value = true
  try {
    const params = {
      name: searchForm.name || undefined,
      category: searchForm.category || undefined,
      status: searchForm.status,
      pageNum: currentPage.value,
      pageSize: pageSize.value
    }
    const res = await getAdminMenuPage(params)
    // 假设后端返回格式为 { content, totalElements }
    displayData.value = res.data.content
    total.value = res.data.totalElements
  } catch (error) {
    ElMessage.error('获取菜品列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  currentPage.value = 1
  fetchMenuList()
}

// 重置搜索
const resetSearch = () => {
  searchForm.name = ''
  searchForm.category = ''
  searchForm.status = undefined
  currentPage.value = 1
  fetchMenuList()
}

// 分页事件
const handleCurrentChange = (page: number) => {
  currentPage.value = page
  fetchMenuList()
}

const handleSizeChange = (size: number) => {
  pageSize.value = size
  currentPage.value = 1
  fetchMenuList()
}

// 图片上传前校验
const beforeImageUpload: UploadProps['beforeUpload'] = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 10
  if (!isImage) {
    ElMessage.error('只能上传图片文件！')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 10MB！')
    return false
  }
  return true
}

// 图片文件改变时自动上传
const handleImageChange = async (file: UploadFile) => {
  if (!file.raw) return
  const loadingInstance = ElLoading.service({ text: '上传中...', background: 'rgba(0,0,0,0.7)' })
  try {
    const res = await uploadFile(file.raw)
    formData.imageUrl = res.data   // 根据后端实际返回调整
  } catch {
    ElMessage.error('网络错误，上传失败')
  } finally {
    loadingInstance.close()
  }
}

// 弹窗相关
const dialogVisible = ref(false)
const dialogTitle = ref('新增菜品')
const formData = reactive<Menu>({
  name: '',
  category: '',
  price: 0,
  status: 1,
  imageUrl: '',
  description: '',
})

const openAddDialog = () => {
  dialogTitle.value = '新增菜品'
  Object.assign(formData, {
    id: undefined,
    name: '',
    category: '',
    price: 0,
    status: 1,
    imageUrl: '',
    description: '',
  })
  dialogVisible.value = true
}

const openEditDialog = (row: Menu) => {
  dialogTitle.value = '编辑菜品'
  Object.assign(formData, {
    id: row.id,
    name: row.name,
    category: row.category,
    price: row.price,
    status: row.status,
    imageUrl: row.imageUrl || '',
    description: row.description || '',
  })
  dialogVisible.value = true
}

// 提交新增/编辑
const submitForm = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    const loadingInstance = ElLoading.service({ text: '提交中...', background: 'rgba(0,0,0,0.7)' })
    try {
      if (dialogTitle.value === '新增菜品') {
        const { id, ...addData } = formData
        await addMenu(addData)
        ElMessage.success('新增成功')
      } else {
        if (!formData.id) {
          ElMessage.error('菜品ID丢失')
          return
        }
        await updateMenu(formData.id, formData)
        ElMessage.success('编辑成功')
      }
      dialogVisible.value = false
      await fetchMenuList()  // 刷新列表（保持当前页码）
    } catch (error) {
      ElMessage.error('操作失败')
      console.error(error)
    } finally {
      loadingInstance.close()
    }
  })
}

// 删除
const handleDelete = (row: Menu) => {
  ElMessageBox.confirm(`确定要删除菜品“${row.name}”吗？`, '删除确认', {
    confirmButtonText: '确定删除',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(async () => {
    const loadingInstance = ElLoading.service({ text: '删除中...', background: 'rgba(0,0,0,0.7)' })
    try {
      if (!row.id) {
        ElMessage.error('菜品ID丢失')
        return
      }
      await deleteMenu(row.id)
      ElMessage.success('删除成功')
      await fetchMenuList()
    } catch (error) {
      ElMessage.error('删除失败')
    } finally {
      loadingInstance.close()
    }
  }).catch(() => {})
}

// 格式化日期
const formatDate = (date?: string | Date) => {
  if (!date) return ''
  const d = new Date(date)
  return `${d.getFullYear()}-${(d.getMonth() + 1).toString().padStart(2, '0')}-${d.getDate().toString().padStart(2, '0')} ${d.getHours().toString().padStart(2, '0')}:${d.getMinutes().toString().padStart(2, '0')}:${d.getSeconds().toString().padStart(2, '0')}`
}

onMounted(() => {
  fetchMenuList()
})
</script>

<style scoped>
.menu-management {
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
.image-placeholder {
  width: 60px;
  height: 60px;
  background-color: #f5f7fa;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  color: #909399;
  border-radius: 4px;
}
.avatar-uploader .avatar {
  width: 100px;
  height: 100px;
  display: block;
  object-fit: cover;
}
.avatar-uploader :deep(.el-upload) {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  width: 100px;
  height: 100px;
}
.avatar-uploader :deep(.el-upload:hover) {
  border-color: #409eff;
}
.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 100px;
  height: 100px;
  text-align: center;
  line-height: 100px;
}
</style>