<template>
  <div class="menu-detail-container">
    <!-- 菜品基本信息 -->
    <el-card class="menu-card" shadow="hover">
      <div class="menu-info">
        <div class="menu-img">
          <el-image
              v-if="menu.imageUrl"
              :src="menu.imageUrl"
              fit="cover"
              class="menu-img-large"
              lazy
          >
            <template #error>
              <div class="image-placeholder">暂无图片</div>
            </template>
          </el-image>
          <div v-else class="image-placeholder large">暂无图片</div>
        </div>
        <div class="menu-details">
          <h1>{{ menu.name }}</h1>

          <!-- 平均评分展示（只读，半星） -->
          <div class="comment-rating">
            <el-rate
                v-model="averageRating"
                disabled
                allow-half
                :colors="['#F7BA2A', '#F7BA2A', '#F7BA2A']"
            />
            <span class="rating-text">({{ averageRating?.toFixed(1) || 0 }} 分)</span>
          </div>

          <p class="price">¥{{ menu.price?.toFixed(2) }}</p>
          <p class="category">类别：{{ menu.category }}</p>
          <p class="description">{{ menu.description || '暂无描述' }}</p>
          <el-button type="primary" @click="addToCart">加入购物车</el-button>
        </div>
      </div>
    </el-card>

    <!-- 评论区域 -->
    <el-card class="comment-card" shadow="hover">
      <template #header>
        <div class="comment-header">
          <span>用户评价</span>
          <el-button type="primary" size="small" @click="openCommentDialog" :disabled="!isLoggedIn">
            发表评论
          </el-button>
        </div>
      </template>

      <!-- 评论列表 -->
      <div v-if="comments.length === 0" class="empty-comment">暂无评论，快来发表第一条吧～</div>
      <div v-else class="comment-list">
        <div v-for="item in comments" :key="item.id" class="comment-item">
          <div class="comment-avatar">
            <el-avatar :size="40">{{ item.username?.charAt(0) }}</el-avatar>
          </div>
          <div class="comment-content">
            <div class="comment-user">{{ item.username }}</div>
            <div class="comment-rating">
              <el-rate v-model="item.rating" disabled :colors="['#F7BA2A', '#F7BA2A', '#F7BA2A']" />
            </div>
            <div class="comment-text">{{ item.content }}</div>
            <div class="comment-time">{{ formatDate(item.createTime) }}</div>
          </div>
        </div>
      </div>

      <!-- 分页（如果后端支持分页） -->
      <div class="pagination-container" v-if="total > pageSize">
        <el-pagination
            :current-page="currentPage"
            :page-size="pageSize"
            :total="total"
            layout="prev, pager, next"
            @current-change="fetchComments"
        />
      </div>
    </el-card>

    <!-- 发表评论弹窗 -->
    <el-dialog v-model="dialogVisible" title="发表评论" width="500px">
      <el-form :model="commentForm" label-width="80px">
        <el-form-item label="评分" required>
          <el-rate v-model="commentForm.rating" :colors="['#F7BA2A', '#F7BA2A', '#F7BA2A']" />
        </el-form-item>
        <el-form-item label="评价内容" required>
          <el-input
              v-model="commentForm.content"
              type="textarea"
              :rows="4"
              placeholder="说说你对这道菜的感受..."
              maxlength="500"
              show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitComment" :loading="submitting">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import {ref, onMounted, computed, watch} from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useCartStore } from '@/stores/cart.ts'
import { useUserStore } from '@/stores/user.ts'
import { getMenuById } from '@/api/menu.ts'
import { getCommentsByMenu, addComment, getAverageRating} from '@/api/comment.ts'
import type { Menu } from '@/types/Menu.ts'
import type { Comment } from '@/types/Comment.ts'

const route = useRoute()
const router = useRouter()
const cartStore = useCartStore()
const userStore = useUserStore()

const menuId = computed(() => Number(route.query.id))

// 菜品信息
const menu = ref<Menu>({} as Menu)
const loadingMenu = ref(false)
const averageRating = ref<number>(0)
const ratingLoading = ref(false)

// 评论相关
const comments = ref<Comment[]>([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const loadingComments = ref(false)

// 评论弹窗
const dialogVisible = ref(false)
const submitting = ref(false)
const commentForm = ref({
  rating: 5,
  content: ''
})

// 登录状态
const isLoggedIn = computed(() => !!userStore.userInfo)

// 获取菜品详情
const fetchMenuDetail = async () => {
  loadingMenu.value = true
  try {
    const res = await getMenuById(menuId.value)
    menu.value = res.data
    await fetchAverageRating()
  } catch (error) {
    ElMessage.error('获取菜品信息失败')
    router.back()
  } finally {
    loadingMenu.value = false
  }
}

// 获取评分
const fetchAverageRating = async () => {
  if (!menuId) return
  ratingLoading.value = true
  try {
    const res = await getAverageRating(menuId.value)
    if (res.data === 0) res.data = 5
    // 假设 res 直接是数字，若 res.data 则取 res.data
    averageRating.value = res.data
  } catch (error) {
    console.error('获取评分失败', error)
    averageRating.value = 0
  } finally {
    ratingLoading.value = false
  }
}

// 获取评论列表
const fetchComments = async () => {
  loadingComments.value = true
  try {
    const res = await getCommentsByMenu(menuId.value, currentPage.value, pageSize.value)
    console.log(res)
    comments.value = res.data.content
    total.value = res.data.totalElements
  } catch (error) {
    ElMessage.error('获取评论失败')
  } finally {
    loadingComments.value = false
  }
}

// 打开评论弹窗
const openCommentDialog = () => {
  if (!isLoggedIn.value) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  commentForm.value = { rating: 5, content: '' }
  dialogVisible.value = true
}

// 提交评论
const submitComment = async () => {
  if (!commentForm.value.content.trim()) {
    ElMessage.warning('请输入评价内容')
    return
  }
  submitting.value = true
  try {
    await addComment({
      menuId: menuId.value,
      rating: commentForm.value.rating,
      content: commentForm.value.content.trim()
    })
    ElMessage.success('评论成功')
    dialogVisible.value = false
    // 刷新评论列表
    currentPage.value = 1
    await fetchComments()
  } catch (error: any) {
    ElMessage.error(error.message || '评论失败')
  } finally {
    submitting.value = false
  }
}

// 加入购物车
const addToCart = () => {
  if (!menu.value.id) return
  cartStore.addItem({
    id: menu.value.id,
    name: menu.value.name,
    price: menu.value.price,
    imageUrl: menu.value.imageUrl
  }, 1)
  ElMessage.success(`已添加 ${menu.value.name} 至购物车`)
}

// 格式化日期
const formatDate = (dateStr: string) => {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleString()
}



onMounted(() => {
  fetchMenuDetail()
  fetchComments()
})

watch(menuId, (newId) => {
  if (!newId) return
  fetchMenuDetail()
  fetchComments()
})
</script>

<style scoped>
.menu-detail-container {
  max-width: 1200px;
  margin: 20px auto;
  padding: 0 16px;
}
.menu-card {
  margin-bottom: 24px;
}
.menu-info {
  display: flex;
  gap: 32px;
  flex-wrap: wrap;
}
.menu-img {
  flex-shrink: 0;
}
.menu-img-large {
  width: 300px;
  height: 300px;
  border-radius: 8px;
  object-fit: cover;
}
.image-placeholder {
  background: #f5f7fa;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #909399;
}
.image-placeholder.large {
  width: 300px;
  height: 300px;
  font-size: 16px;
}
.menu-details {
  flex: 1;
}
.menu-details h1 {
  margin: 0 0 12px 0;
  font-size: 28px;
}
.price {
  font-size: 24px;
  color: #f56c6c;
  font-weight: bold;
  margin: 12px 0;
}
.category, .description {
  color: #606266;
  margin: 8px 0;
}
.comment-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.comment-list {
  margin-top: 8px;
}
.comment-item {
  display: flex;
  gap: 16px;
  padding: 16px 0;
  border-bottom: 1px solid #ebeef5;
}
.comment-avatar {
  flex-shrink: 0;
}
.comment-content {
  flex: 1;
}
.comment-user {
  font-weight: bold;
  margin-bottom: 6px;
}
.comment-rating {
  margin-bottom: 8px;
}
.comment-text {
  color: #606266;
  margin-bottom: 8px;
  line-height: 1.5;
}
.comment-time {
  font-size: 12px;
  color: #909399;
}
.empty-comment {
  text-align: center;
  padding: 40px 0;
  color: #909399;
}
.pagination-container {
  margin-top: 16px;
  display: flex;
  justify-content: center;
}
</style>