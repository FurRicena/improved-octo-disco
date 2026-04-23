<template>
  <div class="admin-comments">
    <h2>评论管理</h2>
    <el-table :data="comments" border v-loading="loading">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="username" label="用户" width="120" />
      <el-table-column prop="menuName" label="菜品" width="150" />
      <el-table-column prop="rating" label="评分" width="180">
        <template #default="{ row }">
          <el-rate v-model="row.rating" disabled show-score />
        </template>
      </el-table-column>
      <el-table-column prop="content" label="评论内容" show-overflow-tooltip />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">
            {{ row.status === 1 ? '显示' : '隐藏' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="评论时间" width="180" />
      <el-table-column label="操作" width="180">
        <template #default="{ row }">
          <el-button v-if="row.status === 1" size="small" @click="toggleStatus(row, 0)">隐藏</el-button>
          <el-button v-else size="small" type="success" @click="toggleStatus(row, 1)">显示</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination
        :current-page="currentPage"
        :page-size="pageSize"
        :total="total"
        @current-change="fetchComments"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAdminComments, updateCommentStatus, deleteComment } from '@/api/comment.ts'
import type { Comment } from "@/types/Comment.ts";

const comments = ref<Comment[]>([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const fetchComments = async () => {
  loading.value = true
  try {
    const res = await getAdminComments(currentPage.value, pageSize.value)
    comments.value = res.data.content
    total.value = res.data.totalElements
  } finally {
    loading.value = false
  }
}

const toggleStatus = async (row: { id: number; }, status: number) => {
  await updateCommentStatus(row.id, status)
  ElMessage.success('状态已更新')
  await fetchComments()
}

const handleDelete = (row: { id: number; }) => {
  ElMessageBox.confirm('确定删除该评论吗？', '提示', { type: 'warning' }).then(async () => {
    await deleteComment(row.id)
    ElMessage.success('删除成功')
    await fetchComments()
  })
}

onMounted(fetchComments)
</script>