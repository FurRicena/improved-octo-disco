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

    <!-- 内容行：网格 + 侧边栏 -->
    <div class="content-row">
    <!-- 菜品展示区（网格布局） -->
      <div class="menu-grid">
        <el-card
            v-for="item in filteredMenu"
            :key="item.id"
            class="menu-card"
            shadow="hover"
            body-style="padding: 12px"
            @click="router.push({
            path: `/menu-detail`,
            query: {id: item.id}})"
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
                    @change="(val: number) => cart.updateQuantity(item.menuId, val)"
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

        <!-- AI 推荐卡片 -->
        <el-card class="ai-card" :body-style="{ padding: '16px' }">
          <template #header>
            <div class="ai-header">
<!--              <span>🤖 AI 点餐助手</span>-->
<!--              &lt;!&ndash; 在头部右侧放置数字人 &ndash;&gt;-->
              <DigitalHuman ref="digitalHumanRef" @repeat="handleRepeatSpeak" />
            </div>
          </template>
          <div class="ai-recommend-bar">
            <el-input
                v-model="aiQuery"
                placeholder="说句话，AI帮你点菜，比如：推荐两个辣的菜，30元以下"
                size="small"
                clearable
                @keyup.enter="getAiRecommend"
            />
            <el-button type="info" size="small" @click="getAiRecommend" :loading="aiLoading">
              推荐
            </el-button>
          </div>

          <!-- AI 推荐结果：横向滚动卡片 -->
          <div v-if="aiRecommendations.length > 0" class="ai-results">
            <div class="ai-results-title">推荐菜品：</div>
            <div class="ai-results-scroll">
              <div v-for="item in aiRecommendations" :key="item.id" class="ai-card-item">
                <div class="ai-card-img-wrapper">
                  <el-image
                      v-if="item.imageUrl"
                      :src="item.imageUrl"
                      fit="cover"
                      class="ai-card-img"
                      lazy
                  >
                    <template #error>
                      <div class="image-placeholder">暂无图片</div>
                    </template>
                  </el-image>
                  <div v-else class="image-placeholder">暂无图片</div>
                </div>
                <div class="ai-card-info">
                  <div class="ai-card-name">{{ item.name }}</div>
                  <div class="ai-card-price">¥{{ item.price.toFixed(2) }}</div>
                  <el-button type="primary" size="small" @click="addToCart(item)">添加</el-button>
                </div>
              </div>
            </div>
          </div>
        </el-card>

      </div>

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
import { useCartStore } from '@/stores/cart.ts'
import { useUserStore } from '@/stores/user.ts'   // 需要你已有的用户store，或自己实现一个
import { getMenuList } from '@/api/menu.ts'        // 获取菜品API
import {aiRecommend, createOrder} from '@/api/orders.ts'      // 创建订单API
import type { Menu } from "@/types/Menu.ts"

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

// ai
const aiQuery = ref('')
const aiLoading = ref(false)
const aiRecommendations = ref<Menu[]>([])

import DigitalHuman from '@/components/DigitalHuman.vue'
import { useTTS } from '@/composables/useTTS'

// 数字人组件 ref
const digitalHumanRef = ref<InstanceType<typeof DigitalHuman> | null>(null)
const { speak, stop: stopTTS } = useTTS()

// 修改 getAiRecommend 函数，加入数字人状态控制
const getAiRecommend = async () => {
  if (!aiQuery.value.trim()) return

  // 1. 数字人切换为聆听状态
  digitalHumanRef.value?.startListening()

  aiLoading.value = true
  try {
    // 假设后端接口 /ai/recommend 返回的数据结构为：
    // { data: { recommendations: Menu[], speakText: string } }
    const res = await aiRecommend(aiQuery.value)
    const recommendations = res.data.menus
    const speakText = res.data.speakText   // 推荐语，例如：“亲，根据您的要求，我推荐清炒西兰花和鸡胸肉沙拉哦～”

    // 更新推荐结果
    aiRecommendations.value = recommendations

    // 2. 如果有推荐语，则开始说话并播放语音
    if (speakText) {
      digitalHumanRef.value?.startSpeaking(speakText)
      await speak(
          speakText,
          () => {
            // 语音开始时的额外操作（可选）
          },
          () => {
            // 语音结束后，数字人恢复空闲
            digitalHumanRef.value?.stopSpeaking()
          }
      )
    } else {
      // 没有推荐语，直接恢复空闲
      digitalHumanRef.value?.stopSpeaking()
    }
  } catch (error) {
    console.error('AI推荐失败', error)
    digitalHumanRef.value?.stopSpeaking()
    ElMessage.error('获取推荐失败，请稍后重试')
  } finally {
    aiLoading.value = false
  }
}

// 处理重复播放（点击数字人头像时触发）
const handleRepeatSpeak = async (message: string) => {
  // 停止当前正在播放的语音
  stopTTS()
  // 如果数字人正在说话，先重置状态
  digitalHumanRef.value?.stopSpeaking()
  // 开始重新播放
  digitalHumanRef.value?.startSpeaking(message)
  await speak(
      message,
      undefined,
      () => digitalHumanRef.value?.stopSpeaking()
  )
}

// 直接加入购物车（数量为1）
const addToCart = (item: Menu) => {
  if (item.id && item.name && item.price) {
    cart.addItem(
        {
          id: item.id,
          name: item.name,
          price: item.price,
          imageUrl: item.imageUrl,
        },
        1
    );
    ElMessage.success(`已添加 ${item.name}`);
  } else {
    console.log(item)
    ElMessage.error('菜品信息不完整');
  }
};


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
  if (currentMenuItem.value && currentMenuItem.value.id) {
    cart.addItem(
        {
          id: currentMenuItem.value.id,
          name: currentMenuItem.value.name,
          price: currentMenuItem.value.price,
          imageUrl: currentMenuItem.value.imageUrl,
        },
        addQuantity.value
    );
    ElMessage.success(`已添加 ${currentMenuItem.value.name} x ${addQuantity.value}`);
    addDialogVisible.value = false;
  } else {
    ElMessage.error('菜品信息不完整，无法添加');
  }
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
    await router.push('/login')
    return
  }
  console.log(userId)

  // 构造请求数据
  const orderData = {
    userId,
    items: cart.items.map(item => ({
      menuId: item.menuId,
      quantity: item.quantity,
    })),
  }

  console.log(orderData)
  submitting.value = true
  try {
    const newOrder = await createOrder(orderData)
    ElMessage.success('订单创建成功！')
    cart.clearCart()  // 清空购物车
    // 可选：跳转到订单详情页或我的订单页
    // await router.push(`/my-orders?orderId=${newOrder.data.id}`)
    await router.push({
      path: '/order-success',
      query: {
        orderId: newOrder.data.id,
        totalPrice: newOrder.data.totalPrice,
        orderTime: newOrder.data.createTime,
        status: newOrder.data.status
      }
    })
  } catch (error: any) {
    ElMessage.error(error.message || '下单失败，请重试')
  } finally {
    submitting.value = false
  }
}

// 获取菜品列表（只取上架的）
const fetchMenu = async () => {
  try {
    const res = await getMenuList()
    allMenu.value = res.data
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
  flex-direction: column;   /* 垂直排列 */
  gap: 20px;
  padding: 20px;
  background: #f5f7fa;
  min-height: 100vh;
}

/* 搜索栏 - 不再固定定位，正常流式 */
.filter-bar {
  display: flex;
  gap: 12px;
  background: white;
  padding: 12px 20px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  /* 移除 position, top, left, right, margin-bottom 等 */
}

/* 内容行：网格 + 侧边栏并排 */
.content-row {
  display: flex;
  gap: 24px;
}

/* 菜品网格区域 */
.menu-grid {
  flex: 3;
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 16px;
  /* 移除 margin-top */
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
  top: 20px;        /* 侧边栏粘性定位 */
  height: fit-content;
  /* 移除 margin-top */
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

/* AI 卡片整体样式 */
.ai-card {
  margin-top: 20px;
  border-radius: 12px;
}
.ai-header {
  font-weight: bold;
  font-size: 16px;
}
.ai-recommend-bar {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
}
.ai-results {
  margin-top: 8px;
}
.ai-results-title {
  font-size: 13px;
  color: #666;
  margin-bottom: 8px;
}
/* 横向滚动容器 */
.ai-results-scroll {
  display: flex;
  gap: 12px;
  overflow-x: auto;
  overflow-y: hidden;
  padding-bottom: 8px;
}
/* 每个推荐卡片 - 固定宽度，与菜品卡片风格一致 */
.ai-card-item {
  flex-shrink: 0;
  width: 120px;
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  transition: transform 0.2s;
  text-align: center;
  padding: 8px;
}
.ai-card-item:hover {
  transform: translateY(-2px);
}
.ai-card-img-wrapper {
  width: 100%;
  height: 90px;
  overflow: hidden;
  border-radius: 6px;
  background-color: #f5f5f5;
  display: flex;
  align-items: center;
  justify-content: center;
}
.ai-card-img {
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
.ai-card-info {
  margin-top: 6px;
}
.ai-card-name {
  font-size: 13px;
  font-weight: 500;
  margin: 4px 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.ai-card-price {
  color: #f56c6c;
  font-weight: bold;
  font-size: 12px;
  margin-bottom: 6px;
}
</style>