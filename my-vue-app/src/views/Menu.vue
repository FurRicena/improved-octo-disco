<template>
  <div class="menu-management">
    <!-- 搜索栏与操作栏 -->
    <div class="search-bar">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="菜品名称">
          <el-input v-model="searchForm.name" placeholder="请输入菜品名称" clearable />
        </el-form-item>
        <el-form-item label="菜品类别">
          <el-select v-model="searchForm.category" placeholder="请选择类别" clearable>
            <el-option label="凉菜" value="凉菜" />
            <el-option label="热菜" value="热菜" />
            <el-option label="汤羹" value="汤羹" />
            <el-option label="主食" value="主食" />
            <el-option label="饮品" value="饮品" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
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
    <el-table :data="displayData" border stripe style="width: 100%">
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

    <!-- 分页（静态展示，不做实际分页） -->
    <div class="pagination-container">
      <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          :page-size-options="[10, 20, 30, 50]"
          layout="total, sizes, prev, pager, next, jumper"
      />
    </div>

    <!-- 新增/编辑弹窗（静态演示，保存不做真实修改） -->
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
<!--        <el-form-item label="图片URL">-->
<!--          <el-input v-model="formData.imageUrl" placeholder="请输入图片URL地址" />-->
<!--        </el-form-item>-->
        <el-form-item label="菜品图片">
          <el-upload
              class="avatar-uploader"
              :auto-upload="false"
              :show-file-list="false"
              :on-change="handleImageChange"
              :before-upload="beforeImageUpload"
          >
            <img v-if="formData.imageUrl" :src="formData.imageUrl" class="avatar"  alt=""/>
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
import {ref, reactive, onMounted, computed} from 'vue'
import {ElLoading, ElMessage, ElMessageBox, type FormInstance, type UploadFile, type UploadProps} from 'element-plus'
import { addMenu, deleteMenu, getMenuList, updateMenu } from "@/api/menu.ts"
import { uploadFile } from "@/api/upload.ts"
import type {Menu} from "@/types/Menu.ts"
import { Plus } from "@element-plus/icons-vue"


// 菜品类型定义
type SearchForm = Pick<Menu, 'name' | 'category' | 'status'>
const searchForm = reactive<SearchForm>({
  name: '',
  category: '',
  status: undefined
})

// 原始数据，展示数据，loading
const displayData = ref<Menu[]>([])
const loading = ref(false)
const allMenuList = ref<Menu[]>([])
const formRef = ref<FormInstance>()

//  分页相关
const currentPage = ref(1)
const pageSize = ref(10)
// total 会自动跟随 displayData.value.length 变化
const total = computed(() => displayData.value.length)

// 获取后台数据
const fetchMenuList = async () => {
  loading.value = true
  try {
    const res = await getMenuList()
    displayData.value = res.data   //
    allMenuList.value = res.data   //
  } finally {
    loading.value = false
  }
}


// 表格展示的数据（可根据搜索过滤，但为了静态演示，简单处理）


// 搜索（仅前端静态过滤，不修改原始默认数据）
const handleSearch = () => {
  let result = allMenuList.value
  if (searchForm.name) {
    result = result.filter(item => item.name?.toLowerCase().includes(searchForm.name.toLowerCase()))
  }
  if (searchForm.category) {
    result = result.filter(item => item.category === searchForm.category)
  }
  if (searchForm.status !== undefined) {
    result = result.filter(item => item.status === searchForm.status)
  }
  displayData.value = result
  ElMessage.info('演示模式：已过滤数据')
}

// 重置
const resetSearch = () => {
  searchForm.name = ''
  searchForm.category = ''
  searchForm.status = undefined
  displayData.value = allMenuList.value
  ElMessage.info('已重置搜索条件')
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
  const loading = ElLoading.service({ text: '上传中...', background: 'rgba(0,0,0,0.7)' })
  try {
    const res = await uploadFile(file.raw)   // 调用上传 API
    // 根据后端实际返回结构调整：假设 res 为 { code, message, data }
    formData.imageUrl = res.data   // res.data 是图片 URL 字符串
  } catch {
    ElMessage.error('网络错误，上传失败')
  } finally {
    loading.close()
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
  // 清空表单
  formData.name = ''
  formData.category = ''
  formData.price = 0
  formData.status = 1
  formData.imageUrl = ''
  formData.description = ''
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

// 新增
const submitForm = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    const loading = ElLoading.service({ text: '提交中...', background: 'rgba(0,0,0,0.7)' })
    try {
      if (dialogTitle.value === '新增菜品') {
        // 新增时不需要传递 id
        const { id, ...addData } = formData
        await addMenu(addData)
        ElMessage.success('新增成功')
        dialogVisible.value = false
        await fetchMenuList()   // 刷新列表
      } else {
        // 编辑时需要 id
        if (!formData.id) {
          ElMessage.error('菜品ID丢失')
          return
        }
        await updateMenu(formData.id, formData)
        ElMessage.success('编辑成功')
        dialogVisible.value = false
        await fetchMenuList()
      }
    } catch (error) {
      ElMessage.error('操作失败')
      console.error(error)
    } finally {
      loading.close()
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
    const loading = ElLoading.service({ text: '删除中...', background: 'rgba(0,0,0,0.7)' })
    try {
      if (!row.id) {
        ElMessage.error('菜品ID丢失')
        return
      }
      await deleteMenu(row.id)
      ElMessage.success('删除成功')
      await fetchMenuList()   // 刷新列表
    } catch (error) {
      ElMessage.error('删除失败')
    } finally {
      loading.close()
    }
  }).catch(() => {})  // 用户取消删除不做任何事
}

// 辅助函数：格式化日期
const formatDate = (date?: Date) => {
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