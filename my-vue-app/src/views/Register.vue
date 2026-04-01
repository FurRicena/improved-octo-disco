<script setup lang="ts">
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { register } from '@/api/user'
import type { RegisterForm } from '@/types/user'
import type { FormInstance, FormRules } from 'element-plus'
import { useRouter } from "vue-router";

// 路由
const router = useRouter()

// 表单实例
const formRef = ref<FormInstance>()

// 表单数据
const form = reactive<RegisterForm>({
  username: '',
  password: ''
})

// 校验规则
const rules = reactive<FormRules>({
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, message: '用户名至少3位', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少6位', trigger: 'blur' }
  ]
})

// 注册按钮
const handleRegister = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    try {
      const res = await register(form)

      ElMessage.success('注册成功！')

      console.log(res)
      // 👇 核心：跳转到登录页
      router.push('/login')

      // 可以跳转到登录页（后面教你router）
    } catch (err) {
      ElMessage.error('注册失败')
    }
  })
}
</script>

<template>
  <div class="register-container">
    <el-card class="register-card">
      <h2>用户注册</h2>

      <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          label-width="80px"
      >
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" />
        </el-form-item>

        <el-form-item label="密码" prop="password">
          <el-input type="password" v-model="form.password" />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleRegister">
            注册
          </el-button>

          <el-button link @click="router.push('/login')">
            已有账号？去登录
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<style scoped>
.register-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background: #f5f5f5;
}

.register-card {
  width: 400px;
}
</style>