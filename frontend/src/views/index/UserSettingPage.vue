<template>
  <div class="user-setting-container">
    <div class="user-setting-header">
      <h2>个人信息设置</h2>
    </div>
    <div class="user-setting-content">
      <el-form :model="userForm" label-width="120px">
        <el-form-item label="头像">
          <el-avatar :size="100" :src="userForm.avatar" />
          <el-button style="margin-left: 20px" type="primary">更换头像</el-button>
        </el-form-item>
        <el-form-item label="用户名">
          <el-input v-model="userForm.username" />
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model="userForm.nickname" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="userForm.email" />
        </el-form-item>
        <el-form-item label="个人简介">
          <el-input v-model="userForm.bio" type="textarea" :rows="4" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary">保存更改</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { reactive } from 'vue'
import { useStore } from '@/store'

const store = useStore()

const userForm = reactive({
  username: store.user.username,
  nickname: store.user.nickname || '',
  email: store.user.email,
  bio: store.user.bio || '',
  avatar: store.avatarUrl
})
</script>

<style scoped>
.user-setting-container {
  padding: 20px;
  background-color: var(--el-bg-color);
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
  margin: 20px;
}

.user-setting-header {
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 1px solid var(--el-border-color-light);
}

.user-setting-content {
  max-width: 600px;
}
</style> 