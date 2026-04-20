<script setup lang="ts">
import { reactive } from 'vue'
// import { login } from '@/api/user'
import type { LoginForm } from '@/types/User.ts'
import { useRouter } from 'vue-router'
import {ElMessage} from "element-plus";
import { useUserStore} from "@/stores/user.ts"

// 路由
const router = useRouter()
const userStore = useUserStore()

// TS写法（类型约束）
const form = reactive<LoginForm>({
  username: '',
  password: ''
})

const handleLogin = async () => {
  await userStore.login(form)
  ElMessage.success("登录成功")
  await router.push('/')
}
</script>

<template>
  <div class="login-container">
    <el-card class="login-card">
      <h2>用户登录</h2>

      <el-form
          ref="formRef"
          :model="form"
          label-width="80px"
      >
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" />
        </el-form-item>

        <el-form-item label="密码" prop="password">
          <el-input type="password" v-model="form.password" />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleLogin">
            登录
          </el-button>

          <el-button link @click="router.push('/register')">
            没有账号？去注册
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background: #f5f5f5;
}

.login-card {
  width: 400px;
}
</style>
