<template>
  <div class="privacy-setting-container">
    <div class="privacy-setting-header">
      <h2>账号安全设置</h2>
    </div>
    <div class="privacy-setting-content">
      <el-card class="setting-card">
        <template #header>
          <div class="card-header">
            <h3>修改密码</h3>
          </div>
        </template>
        <el-form :model="passwordForm" label-width="120px">
          <el-form-item label="当前密码">
            <el-input v-model="passwordForm.currentPassword" type="password" show-password />
          </el-form-item>
          <el-form-item label="新密码">
            <el-input v-model="passwordForm.newPassword" type="password" show-password />
          </el-form-item>
          <el-form-item label="确认新密码">
            <el-input v-model="passwordForm.confirmPassword" type="password" show-password />
          </el-form-item>
          <el-form-item>
            <el-button type="primary">修改密码</el-button>
          </el-form-item>
        </el-form>
      </el-card>

      <el-card class="setting-card">
        <template #header>
          <div class="card-header">
            <h3>账号安全</h3>
          </div>
        </template>
        <el-form>
          <el-form-item label="登录设备管理">
            <el-button type="warning">查看所有登录设备</el-button>
          </el-form-item>
          <el-form-item label="二次验证">
            <el-switch v-model="securitySettings.twoFactorAuth" />
            <span class="setting-desc">启用后，登录时将需要额外的验证码</span>
          </el-form-item>
          <el-form-item label="登录通知">
            <el-switch v-model="securitySettings.loginNotification" />
            <span class="setting-desc">启用后，每次登录都会向您的邮箱发送通知</span>
          </el-form-item>
        </el-form>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { reactive } from 'vue'

const passwordForm = reactive({
  currentPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const securitySettings = reactive({
  twoFactorAuth: false,
  loginNotification: true
})
</script>

<style scoped>
.privacy-setting-container {
  padding: 20px;
  margin: 20px;
}

.privacy-setting-header {
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 1px solid var(--el-border-color-light);
}

.privacy-setting-content {
  max-width: 800px;
}

.setting-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.setting-desc {
  margin-left: 10px;
  color: var(--el-text-color-secondary);
  font-size: 14px;
}
</style> 