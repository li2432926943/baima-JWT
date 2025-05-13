<template>
  <div class="login-container">
    <!-- 左侧欢迎区域 -->
    <div class="login-banner">
      <el-image 
        class="background-image" 
        :src="animeBg" 
        fit="cover"
        :preview-teleported="true"
        :preview-src-list="[animeBg]">
      </el-image>
      <div class="welcome-text">
        <h1>欢迎来到我们的学习平台</h1>
        <p>在这里你可以学习如何使用Java，如何搭建网站，并且与Java之父密切交流。</p>
        <p>在这里你可以同性交友，因为都是男的，没有学Java的女生。</p>
      </div>
    </div>
    
    <!-- 右侧登录表单 -->
    <div class="login-form-container">
      <div class="login-form-wrapper">
        <h2 class="login-title">登录</h2>
        <p class="login-desc">在进入系统之前请先输入用户名和密码进行登录</p>
        
        <el-form :model="loginForm" class="login-form" :rules="rules" ref="loginFormRef">
          <el-form-item prop="username">
            <el-input 
              v-model="loginForm.username" 
              :prefix-icon="User"
              placeholder="用户名/邮箱">
            </el-input>
          </el-form-item>
          
          <el-form-item prop="password">
            <el-input 
              v-model="loginForm.password" 
              :prefix-icon="Lock"
              type="password" 
              placeholder="密码"
              @keyup.enter="handleLogin">
            </el-input>
          </el-form-item>
          
          <div class="login-options">
            <el-checkbox v-model="loginForm.remember" label="记住我" />
            <el-button type="text" @click="forgotPassword">忘记密码?</el-button>
          </div>
          
          <el-button type="primary" class="login-button" :loading="loading" @click="handleLogin">立即登录</el-button>
          
          <div class="divider">
            <span>没有账号</span>
          </div>
          
          <el-button class="register-button" @click="goToRegister">注册账号</el-button>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { User, Lock } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { login } from '@/net'
import animeBg from '@/assets/anime-bg.svg'

const router = useRouter()
const loginFormRef = ref(null)
const loading = ref(false)

const loginForm = reactive({
  username: '',
  password: '',
  remember: true // 默认勾选记住我，提高用户体验
})

const rules = reactive({
  username: [
    { required: true, message: '请输入用户名或邮箱', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
  ]
})

const handleLogin = async () => {
  if (!loginFormRef.value) return
  
  await loginFormRef.value.validate((valid) => {
    if (valid) {
      loading.value = true
      login(loginForm.username, loginForm.password, loginForm.remember, 
        () => {
          loading.value = false
          router.push('/index')
        },
        (message) => {
          loading.value = false
          // 这里可以根据不同错误码显示更具体的错误信息
          if (message.includes('用户名') || message.includes('密码')) {
            ElMessage.error('用户名或密码错误，请重新输入')
          } else {
            ElMessage.error(message || '登录失败，请稍后再试')
          }
        }
      )
    }
  })
}

const forgotPassword = () => {
  router.push('/forget')
}

const goToRegister = () => {
  router.push({ name: 'welcome-register' })
}
</script>

<style scoped>
.login-container {
  display: flex;
  height: 100%;
  width: 100%;
  overflow: hidden;
}

.login-banner {
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
.login-banner::before {
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
.login-banner::after {
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

.login-form-container {
  width: 500px;
  background: #fff;
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 0 50px;
}

.login-form-wrapper {
  width: 100%;
}

.login-title {
  font-size: 28px;
  font-weight: 600;
  text-align: center;
  margin-bottom: 16px;
  color: #333;
}

.login-desc {
  font-size: 14px;
  color: #999;
  text-align: center;
  margin-bottom: 40px;
}

.login-form {
  width: 100%;
}

.login-form :deep(.el-input__wrapper) {
  padding: 12px 15px;
}

.login-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
}

.login-button, .register-button {
  width: 100%;
  padding: 12px 0;
  font-size: 16px;
  border-radius: 4px;
}

.login-button {
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

.register-button {
  background-color: #f9f9f9;
  color: #666;
  border: 1px solid #dcdfe6;
}

.register-button:hover {
  background-color: #f2f2f2;
}

@media (max-width: 768px) {
  .login-container {
    flex-direction: column;
  }
  
  .login-banner {
    height: 200px;
    padding: 20px;
  }
  
  .login-form-container {
    width: 100%;
    padding: 30px 20px;
  }
}
</style> 