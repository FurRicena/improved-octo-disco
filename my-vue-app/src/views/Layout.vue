<template>
  <el-container class="layout-container">
    <el-aside :width="isCollapse ? '64px' : '220px'" class="aside">
      <div class="logo" :class="{ collapse: isCollapse }">
        <span v-if="!isCollapse">点餐系统</span>
        <span v-else>餐</span>
      </div>
      <el-menu
          :default-active="activeMenu"
          :collapse="isCollapse"
          :collapse-transition="false"
          router
          background-color="#304156"
          text-color="#bfcbd9"
          active-text-color="#409EFF"
      >
        <template v-for="item in menuList" :key="item.path">
          <!-- 有子菜单 -->
          <el-sub-menu v-if="item.children && item.children.length" :index="item.path">
            <template #title>
              <el-icon>
                <component :is="item.icon"/>
              </el-icon>
              <span>{{ item.name }}</span>
            </template>
            <el-menu-item v-for="child in item.children" :key="child.path" :index="child.path">
              {{ child.name }}
            </el-menu-item>
          </el-sub-menu>
          <!-- 无子菜单 -->
          <el-menu-item v-else :index="item.path">
            <el-icon>
              <component :is="item.icon"/>
            </el-icon>
            <span>{{ item.name }}</span>
          </el-menu-item>
        </template>
      </el-menu>
    </el-aside>
    <!-- 右侧主体 -->
    <el-container>
      <el-header class="header">
        <div class="header-left">
          <el-icon :size="20" @click="toggleCollapse">
            <Fold/>
          </el-icon>
        </div>
        <div class="header-right">
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              {{ userStore.userInfo?.username || '用户' }}
              <el-icon><CaretBottom/></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      <el-main class="main">
        <router-view v-slot="{ Component }">
          <keep-alive>
            <component :is="Component"/>
          </keep-alive>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import {ref, computed} from 'vue'
import {useRouter, useRoute} from 'vue-router'
import {useUserStore} from '@/stores/user'
import {Fold, CaretBottom} from '@element-plus/icons-vue'

interface MenuItem {
  path: string
  name: string
  icon: string
  meta?: { roles: string[] }
  children?: MenuItem[]   // 可选子菜单
}

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const isCollapse = ref(false)

// 根据角色动态生成菜单
const menuList = computed<MenuItem[]>(() => {
  const isAdmin = userStore.userInfo?.role === 'ADMIN'
  const commonMenus: MenuItem[] = [
    {path: '/order', name: '点菜', icon: 'Shop', meta: {roles: ['USER', 'ADMIN']}},
    {path: '/my-orders', name: '我的订单', icon: 'List', meta: {roles: ['USER', 'ADMIN']}},
  ]
  if (isAdmin) {
    const adminMenus: MenuItem[] = [
      {
        path: '/admin',
        name: '系统管理',
        icon: 'Setting',
        children: [
          {path: '/admin/menu', name: '菜品管理', icon: 'Dish', meta: {roles: ['ADMIN']}},
          {path: '/admin/orders', name: '订单管理', icon: 'Tickets', meta: {roles: ['ADMIN']}},
          {path: '/admin/users', name: '用户管理', icon: 'User', meta: {roles: ['ADMIN']}},
          {path: '/admin/statistics', name: '销量统计', icon: 'DataLine', meta: {roles: ['ADMIN']}},
          {path: '/admin/dashboard', name: '数据仪表盘', icon: 'DataLine', meta: {roles: ['ADMIN']}},
          {path: '/admin/logs', name: '日志管理', icon: 'Document', meta: {roles: ['ADMIN']}},
          {path: '/admin/comments', name: '评论管理', icon: 'Document', meta: {roles: ['ADMIN']}}
        ]
      }
    ]
    return [...commonMenus, ...adminMenus]
  }
  return commonMenus
})

// 当前高亮菜单（根据路由路径匹配）
const activeMenu = computed(() => route.path)

const toggleCollapse = () => {
  isCollapse.value = !isCollapse.value
}

const handleCommand = (cmd: string) => {
  if (cmd === 'logout') {
    userStore.logout()
    router.push('/login')
  }
}
</script>

<style scoped>
/* 样式保持不变，参考之前的 */
.layout-container {
  height: 100vh;
}

.aside {
  background-color: #304156;
  transition: width 0.3s;
  overflow-x: hidden;
}

.logo {
  height: 60px;
  line-height: 60px;
  text-align: center;
  color: white;
  font-size: 20px;
  font-weight: bold;
  background-color: #263445;
}

.logo.collapse {
  font-size: 18px;
}

.header {
  background-color: white;
  border-bottom: 1px solid #e6e6e6;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
}

.header-left {
  cursor: pointer;
}

.user-info {
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 4px;
}

.main {
  background-color: #f5f7fa;
  padding: 20px;
  overflow-y: auto;
}
</style>