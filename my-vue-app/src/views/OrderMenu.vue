<template>
  <div class="order-menu">
    <!-- 顶部搜索与筛选栏 -->
    <div class="filter-bar">
      <el-input
          v-model="searchKeyword"
          placeholder="搜索菜品"
          clearable
          prefix-icon="Search"
          style="width: 240px"
          @clear="handleSearch"
          @keyup.enter="handleSearch"
      />
      <el-select v-model="selectedCategory" placeholder="全部分类" clearable @change="handleSearch">
        <el-option
            v-for="cat in allCategories"
            :key="cat"
            :label="cat"
            :value="cat"
        />
      </el-select>
    </div>

    <!-- 菜品展示区（网格布局） -->
    <div class="menu-grid">
      <el-card
          v-for="item in filteredMenu"
          :key="item.id"
          class="menu-card"
          shadow="hover"
          body-style="padding: 12px"
      >
        <div class="menu-img-wrapper">
          <el-image
              v-if="item.imageUrl"
              :src="item.imageUrl"
              fit="cover"
              class="menu-img"
              lazy
          >
            <template #error>
              <div class="image-placeholder">暂无图片</div>
            </template>
          </el-image>
          <div v-else class="image-placeholder">暂无图片</div>
        </div>
        <div class="menu-info">
          <h4 class="menu-name">{{ item.name }}</h4>
          <p class="menu-price">¥{{ item.price.toFixed(2) }}</p>
          <el-button type="primary" size="small" @click="openAddDialog(item)">
            加入购物车
          </el-button>
        </div>
      </el-card>
    </div>

    <!-- 购物车侧边栏 -->
    <div class="cart-sidebar">
      <el-card class="cart-card" :body-style="{ padding: '16px' }">
        <template #header>
          <div class="cart-header">
            <span>我的点单</span>
            <span class="cart-count">{{ cart.totalCount }}件</span>
          </div>
        </template>

        <div v-if="cart.items.length === 0" class="empty-cart">
          <el-empty description="购物车空空如也" :image-size="80" />
        </div>

        <div v-else class="cart-list">
          <div v-for="item in cart.items" :key="item.menuId" class="cart-item">
            <div class="cart-item-info">
              <div class="cart-item-name">{{ item.name }}</div>
              <div class="cart-item-price">¥{{ item.price.toFixed(2) }}</div>
            </div>
            <div class="cart-item-actions">
              <el-input-number
                  v-model="item.quantity"
                  :min="1"
                  size="small"
                  controls-position="right"
                  @change="(val) => cart.updateQuantity(item.menuId, val)"
              />
              <el-button link type="danger" size="small" @click="cart.removeItem(item.menuId)">
                删除
              </el-button>
            </div>
            <div class="cart-item-subtotal">
              ¥{{ (item.price * item.quantity).toFixed(2) }}
            </div>
          </div>
        </div>

        <el-divider v-if="cart.items.length > 0" />

        <div v-if="cart.items.length > 0" class="cart-footer">
          <div class="cart-total">
            <span>合计</span>
            <span class="total-price">¥{{ cart.totalPrice.toFixed(2) }}</span>
          </div>
          <el-button type="success" block @click="submitOrder" :loading="submitting">
            去结算
          </el-button>
          <el-button block @click="cart.clearCart">清空购物车</el-button>
        </div>
      </el-card>
    </div>

    <!-- 加入购物车数量选择弹窗 -->
    <el-dialog v-model="addDialogVisible" title="加入购物车" width="300px">
      <div class="add-dialog">
        <span>数量：</span>
        <el-input-number v-model="addQuantity" :min="1" :max="99" />
      </div>
      <template #footer>
        <el-button @click="addDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmAdd">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useCartStore } from '@/stores/cart'
import { useUserStore } from '@/stores/user'   // 需要你已有的用户store，或自己实现一个
import { getMenuList } from '@/api/menu'        // 获取菜品API
import { createOrder } from '@/api/orders'      // 创建订单API
import { Menu } from "@/types/Menu.ts"

// 使用stores
const cart = useCartStore()
const userStore = useUserStore()
const router = useRouter()

// 加载本地购物车数据
cart.loadFromLocal()

// 菜品数据
const allMenu = ref<Menu[]>([])
const searchKeyword = ref('')
const selectedCategory = ref('')

// 所有分类（从菜品中提取）
const allCategories = computed(() => {
  const cats = new Set(allMenu.value.map(item => item.category))
  return Array.from(cats)
})

// 过滤后的菜品（搜索+分类）
const filteredMenu = computed(() => {
  let result = allMenu.value
  if (searchKeyword.value) {
    result = result.filter(item => item.name.includes(searchKeyword.value))
  }
  if (selectedCategory.value) {
    result = result.filter(item => item.category === selectedCategory.value)
  }
  return result
})

// 加入购物车弹窗
const addDialogVisible = ref(false)
const currentMenuItem = ref<Menu | null>(null)
const addQuantity = ref(1)

const openAddDialog = (item: Menu) => {
  currentMenuItem.value = item
  addQuantity.value = 1
  addDialogVisible.value = true
}

const confirmAdd = () => {
  if (currentMenuItem.value) {
    cart.addItem(currentMenuItem.value, addQuantity.value)
    ElMessage.success(`已添加 ${currentMenuItem.value.name} x ${addQuantity.value}`)
  }
  addDialogVisible.value = false
}

// 提交订单
const submitting = ref(false)
const submitOrder = async () => {
  if (cart.items.length === 0) {
    ElMessage.warning('购物车为空，请先点菜')
    return
  }

  // 获取当前登录用户ID（从userStore获取）
  const userId = userStore.userInfo?.id
  if (!userId) {
    ElMessage.error('用户未登录，请先登录')
    router.push('/login')
    return
  }

  // 构造请求数据
  const orderData = {
    userId,
    items: cart.items.map(item => ({
      menuId: item.menuId,
      quantity: item.quantity,
    })),
  }

  submitting.value = true
  try {
    const newOrder = await createOrder(orderData)
    ElMessage.success('订单创建成功！')
    cart.clearCart()  // 清空购物车
    // 可选：跳转到订单详情页或我的订单页
    router.push(`/my-orders?orderId=${newOrder.id}`)
  } catch (error: any) {
    ElMessage.error(error.message || '下单失败，请重试')
  } finally {
    submitting.value = false
  }
}

// 获取菜品列表（只取上架的）
const fetchMenu = async () => {
  try {
    const data = await getMenuList({ status: 1 })
    allMenu.value = data
  } catch (error) {
    ElMessage.error('获取菜单失败')
  }
}

// 搜索重置
const handleSearch = () => {
  // 由于 computed 自动响应，无需额外操作
}

onMounted(() => {
  fetchMenu()
})
</script>

<style scoped>
.order-menu {
  display: flex;
  gap: 24px;
  padding: 20px;
  min-height: 100vh;
  background: #f5f7fa;
}

/* 筛选栏 */
.filter-bar {
  position: fixed;
  top: 20px;
  left: 20px;
  right: 20px;
  z-index: 10;
  display: flex;
  gap: 12px;
  background: white;
  padding: 12px 20px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  margin-bottom: 20px;
}

/* 菜品网格区域 */
.menu-grid {
  flex: 3;
  margin-top: 70px; /* 为固定筛选栏留出空间 */
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 16px;
}

.menu-card {
  cursor: pointer;
  transition: transform 0.2s;
}
.menu-card:hover {
  transform: translateY(-4px);
}

.menu-img-wrapper {
  width: 100%;
  height: 140px;
  overflow: hidden;
  border-radius: 8px;
  background-color: #f0f0f0;
  display: flex;
  align-items: center;
  justify-content: center;
}

.menu-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.image-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #999;
  font-size: 12px;
  background: #f5f5f5;
}

.menu-info {
  text-align: center;
  margin-top: 8px;
}
.menu-name {
  font-size: 16px;
  margin: 8px 0 4px;
  font-weight: 500;
}
.menu-price {
  color: #f56c6c;
  font-weight: bold;
  margin: 4px 0 8px;
}

/* 购物车侧边栏 */
.cart-sidebar {
  flex: 1;
  min-width: 280px;
  position: sticky;
  top: 90px;
  height: fit-content;
  margin-top: 70px;
}

.cart-card {
  border-radius: 12px;
}

.cart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
  font-size: 16px;
}
.cart-count {
  background: #409eff;
  color: white;
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 12px;
}

.empty-cart {
  padding: 20px 0;
}

.cart-list {
  max-height: 500px;
  overflow-y: auto;
}

.cart-item {
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #eee;
}
.cart-item-info {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
}
.cart-item-name {
  font-weight: 500;
}
.cart-item-price {
  color: #f56c6c;
}
.cart-item-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
}
.cart-item-subtotal {
  text-align: right;
  font-size: 14px;
  color: #666;
}

.cart-footer {
  margin-top: 12px;
}
.cart-total {
  display: flex;
  justify-content: space-between;
  font-size: 16px;
  font-weight: bold;
  margin-bottom: 16px;
}
.total-price {
  color: #f56c6c;
  font-size: 18px;
}

.add-dialog {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
}
</style>