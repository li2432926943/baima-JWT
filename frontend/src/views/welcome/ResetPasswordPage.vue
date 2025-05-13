<template>
  <div class="reset-container">
    <!-- 左侧欢迎区域 -->
    <div class="reset-banner">
      <el-image 
        class="background-image" 
        :src="animeBg" 
        fit="cover"
        :preview-teleported="true"
        :preview-src-list="[animeBg]">
      </el-image>
      <div class="welcome-text">
        <h1>找回密码</h1>
        <p>别担心，我们会帮助您重置密码</p>
      </div>
    </div>
    
    <!-- 右侧重置密码表单 -->
    <div class="reset-form-container">
      <div class="reset-form-wrapper">
        <h2 class="reset-title">设置新密码</h2>
        <p class="reset-desc">验证成功，请设置您的新密码</p>
        
        <el-form :model="resetForm" class="reset-form" :rules="passwordRules" ref="passwordFormRef">
          <el-form-item prop="newPassword" class="no-error-margin">
            <el-input 
              v-model="resetForm.newPassword" 
              type="password"
              placeholder="请输入新密码">
            </el-input>
            <div class="error-tip" v-if="passwordError">{{ passwordError }}</div>
          </el-form-item>
          
          <el-form-item prop="confirmPassword" class="no-error-margin">
            <el-input 
              v-model="resetForm.confirmPassword" 
              type="password"
              placeholder="请确认新密码">
            </el-input>
            <div class="error-tip" v-if="confirmPasswordError">{{ confirmPasswordError }}</div>
          </el-form-item>
          
          <el-button type="primary" class="submit-button" @click="resetPassword">确认重置</el-button>
        </el-form>
        
        <div class="divider">
          <span>或者</span>
        </div>
        
        <el-button class="back-button" @click="goToLogin">返回登录</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter, useRoute } from 'vue-router'
import animeBg from '@/assets/anime-bg.svg'

const router = useRouter()
const route = useRoute()
const passwordFormRef = ref(null)
const email = ref('')

// 获取路由参数中的邮箱地址
if (route.query.email) {
  email.value = route.query.email
}

// 错误提示
const passwordError = ref('')
const confirmPasswordError = ref('')

const resetForm = reactive({
  newPassword: '',
  confirmPassword: ''
})

// 密码规则
const passwordRules = reactive({
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少为6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    { 
      validator: (rule, value, callback) => {
        if (value !== resetForm.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      }, 
      trigger: 'blur' 
    }
  ]
})

// 重置密码
const resetPassword = async () => {
  // 清除错误提示
  passwordError.value = ''
  confirmPasswordError.value = ''
  
  if (!resetForm.newPassword) {
    passwordError.value = '请输入新密码'
    return
  }
  
  if (resetForm.newPassword.length < 6) {
    passwordError.value = '密码长度至少为6位'
    return
  }
  
  if (!resetForm.confirmPassword) {
    confirmPasswordError.value = '请确认新密码'
    return
  }
  
  if (resetForm.newPassword !== resetForm.confirmPassword) {
    confirmPasswordError.value = '两次输入的密码不一致'
    return
  }
  
  // 这里应该有实际重置密码的API调用
  // 例如: await resetPasswordApi(email.value, resetForm.newPassword)
  
  ElMessage.success('密码重置成功')
  setTimeout(() => {
    router.push('/')
  }, 2000)
}

const goToLogin = () => {
  router.push('/')
}
</script>

<style scoped>
.reset-container {
  display: flex;
  height: 100%;
  width: 100%;
  overflow: hidden;
}

.reset-banner {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
  padding: 40px;
  color: white;
  position: relative;
  overflow: hidden;
}

.background-image {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: -1;
}

/* 添加渐变和模糊效果覆盖层 */
.reset-banner::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(to bottom, rgba(30,60,114,0.2), rgba(42,82,152,0.6));
  backdrop-filter: blur(0.5px);
  z-index: 0;
}

/* 保持原有的发光效果 */
.reset-banner::after {
  content: '';
  position: absolute;
  left: -10%;
  bottom: -10%;
  width: 70%;
  height: 70%;
  background: linear-gradient(45deg, #00a8ff, #0097e6, #00d2d3);
  border-radius: 50%;
  filter: blur(60px);
  opacity: 0.3;
  animation: glow 10s ease-in-out infinite;
  z-index: 0;
}

@keyframes glow {
  0% {
    transform: scale(1) rotate(0deg);
    opacity: 0.3;
  }
  50% {
    transform: scale(1.2) rotate(180deg);
    opacity: 0.5;
  }
  100% {
    transform: scale(1) rotate(360deg);
    opacity: 0.3;
  }
}

.welcome-text {
  position: relative;
  z-index: 1;
  max-width: 80%;
}

.welcome-text h1 {
  font-size: 32px;
  margin-bottom: 20px;
}

.welcome-text p {
  font-size: 16px;
  line-height: 1.6;
  margin-bottom: 10px;
  opacity: 0.9;
}

.reset-form-container {
  width: 500px;
  background: #fff;
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 0 50px;
}

.reset-form-wrapper {
  width: 100%;
}

.reset-title {
  font-size: 28px;
  font-weight: 600;
  text-align: center;
  margin-bottom: 16px;
  color: #333;
}

.reset-desc {
  font-size: 14px;
  color: #999;
  text-align: center;
  margin-bottom: 40px;
}

.reset-form {
  width: 100%;
}

.reset-form :deep(.el-input__wrapper) {
  padding: 12px 15px;
}

.no-error-margin :deep(.el-form-item__error) {
  display: none;
}

.error-tip {
  color: #f56c6c;
  font-size: 12px;
  line-height: 1;
  padding-top: 4px;
  position: absolute;
  top: 100%;
  left: 0;
}

.submit-button, .back-button {
  width: 100%;
  padding: 12px 0;
  font-size: 16px;
  border-radius: 4px;
}

.submit-button {
  margin-bottom: 20px;
}

.divider {
  display: flex;
  align-items: center;
  margin: 20px 0;
}

.divider::before, .divider::after {
  content: '';
  flex: 1;
  height: 1px;
  background: #eee;
}

.divider span {
  padding: 0 15px;
  color: #999;
  font-size: 14px;
}

.back-button {
  background-color: #f9f9f9;
  color: #666;
  border: 1px solid #dcdfe6;
}

.back-button:hover {
  background-color: #f2f2f2;
}

@media (max-width: 768px) {
  .reset-container {
    flex-direction: column;
  }
  
  .reset-banner {
    height: 200px;
    padding: 20px;
  }
  
  .reset-form-container {
    width: 100%;
    padding: 30px 20px;
  }
}
</style> 