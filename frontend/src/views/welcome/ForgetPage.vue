<template>
  <div class="forget-container">
    <!-- 左侧欢迎区域 -->
    <div class="forget-banner">
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
    
    <!-- 右侧忘记密码表单 - 根据步骤切换内容 -->
    <div class="forget-form-container">
      <div class="forget-form-wrapper">
        <!-- 步骤一：输入邮箱获取验证码 -->
        <div v-if="currentStep === 1">
          <h2 class="forget-title">重置密码</h2>
          <p class="forget-desc">请输入需要重置密码的电子邮件地址</p>
          <el-form :model="forgetForm" class="forget-form" :rules="emailRules" ref="emailFormRef">
            <el-form-item prop="email" class="no-error-margin">
              <el-input 
                v-model="forgetForm.email" 
                :prefix-icon="Message"
                placeholder="请输入合法的电子邮件地址">
              </el-input>
              <div class="error-tip" v-if="emailError">{{ emailError }}</div>
            </el-form-item>
            
            <el-form-item prop="verifyCode" class="verify-code-item no-error-margin">
              <div class="verify-code-wrapper">
                <el-input 
                  v-model="forgetForm.verifyCode" 
                  placeholder="请输入验证码">
                </el-input>
                <el-button 
                  class="verify-code-btn" 
                  type="primary" 
                  :disabled="isCodeSending"
                  @click="sendVerifyCode">
                  {{ codeButtonText }}
                </el-button>
              </div>
              <div class="error-tip" v-if="codeError">{{ codeError }}</div>
            </el-form-item>
            
            <el-button type="primary" class="submit-button" @click="verifyCode">验证并继续</el-button>
          </el-form>
          
          <div class="divider">
            <span>或者</span>
          </div>
          
          <el-button class="back-button" @click="goToLogin">返回登录</el-button>
        </div>
        
        <!-- 步骤二：设置新密码 -->
        <div v-else-if="currentStep === 2">
          <h2 class="forget-title">设置新密码</h2>
          <p class="forget-desc">验证成功，请设置您的新密码</p>
          <el-form :model="resetForm" class="forget-form" :rules="passwordRules" ref="passwordFormRef">
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
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { Message } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import animeBg from '@/assets/anime-bg.svg'
import { get, post } from '@/net'

const router = useRouter()
const emailFormRef = ref(null)
const passwordFormRef = ref(null)
const currentStep = ref(1)
const isCodeSending = ref(false)
const codeButtonText = ref('获取验证码')
let countdownTimer = null

// 错误提示
const emailError = ref('')
const codeError = ref('')
const passwordError = ref('')
const confirmPasswordError = ref('')

const forgetForm = reactive({
  email: '',
  verifyCode: ''
})

const resetForm = reactive({
  newPassword: '',
  confirmPassword: ''
})

// 邮箱和验证码规则
const emailRules = reactive({
  email: [
    { required: true, message: '请输入邮箱地址', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  verifyCode: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { min: 6, max: 6, message: '验证码长度应为6位', trigger: 'blur' }
  ]
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

// 发送验证码
const sendVerifyCode = async () => {
  // 清除错误提示
  emailError.value = ''
  
  if (!forgetForm.email) {
    emailError.value = '请先输入邮箱地址'
    return
  }
  
  // 验证邮箱格式
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  if (!emailRegex.test(forgetForm.email)) {
    emailError.value = '请输入正确的邮箱地址'
    return
  }
  
  if (isCodeSending.value) return
  
  isCodeSending.value = true
  let countdown = 60
  
  // 发送验证码到后端
  get(`/api/auth/ask-code?email=${forgetForm.email}&type=reset`, () => {
    ElMessage.success(`验证码已发送至邮箱: ${forgetForm.email}，请注意查收`)
  codeButtonText.value = `${countdown}秒后重新获取`
  
  countdownTimer = setInterval(() => {
    countdown--
    codeButtonText.value = `${countdown}秒后重新获取`
    
    if (countdown <= 0) {
      clearInterval(countdownTimer)
      codeButtonText.value = '获取验证码'
      isCodeSending.value = false
    }
  }, 1000)
  }, (message) => {
    ElMessage.warning(message)
    isCodeSending.value = false
  })
}

// 验证验证码
const verifyCode = async () => {
  // 清除错误提示
  emailError.value = ''
  codeError.value = ''
  
  if (!forgetForm.email) {
    emailError.value = '请输入邮箱地址'
    return
  }
  
  if (!forgetForm.verifyCode) {
    codeError.value = '请输入验证码'
    return
  }
  
  if (forgetForm.verifyCode.length !== 6) {
    codeError.value = '验证码长度应为6位'
    return
  }
  
  // 验证验证码
  post('/api/auth/reset-confirm', {
    email: forgetForm.email,
    code: forgetForm.verifyCode
  }, () => {
  ElMessage.success('验证码验证通过')
    // 验证成功，切换到第二步
    currentStep.value = 2
  }, (message) => {
    codeError.value = message
  })
}

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
  
  // 调用重置密码API
  post('/api/auth/reset-password', {
    email: forgetForm.email,
    code: forgetForm.verifyCode,
    password: resetForm.newPassword
  }, () => {
  ElMessage.success('密码重置成功')
  setTimeout(() => {
    router.push('/')
  }, 2000)
  }, (message) => {
    ElMessage.error(message)
  })
}

const goToLogin = () => {
  router.push('/')
}
</script>

<style scoped>
.forget-container {
  display: flex;
  height: 100%;
  width: 100%;
  overflow: hidden;
}

.forget-banner {
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
.forget-banner::before {
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
.forget-banner::after {
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

.forget-form-container {
  width: 500px;
  background: #fff;
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 0 50px;
}

.forget-form-wrapper {
  width: 100%;
}

.forget-title {
  font-size: 28px;
  font-weight: 600;
  text-align: center;
  margin-bottom: 16px;
  color: #333;
}

.forget-desc {
  font-size: 14px;
  color: #999;
  text-align: center;
  margin-bottom: 40px;
}

.forget-form {
  width: 100%;
}

.forget-form :deep(.el-input__wrapper) {
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

.verify-code-item {
  margin-bottom: 24px;
}

.verify-code-wrapper {
  display: flex;
  width: 100%;
}

.verify-code-btn {
  margin-left: 10px;
  white-space: nowrap;
  padding: 0 15px;
  height: 50px;
}

.verify-code-wrapper :deep(.el-input__wrapper) {
  height: 50px;
  line-height: 50px;
  box-sizing: border-box;
}

.verify-code-wrapper :deep(.el-button) {
  height: 50px;
  display: flex;
  align-items: center;
  justify-content: center;
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
  .forget-container {
    flex-direction: column;
  }
  
  .forget-banner {
    height: 200px;
    padding: 20px;
  }
  
  .forget-form-container {
    width: 100%;
    padding: 30px 20px;
  }
}
</style> 