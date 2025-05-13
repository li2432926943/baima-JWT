<template>
  <div class="register-container">
    <!-- 左侧欢迎区域 -->
    <div class="register-banner">
      <el-image 
        class="background-image" 
        :src="animeBg" 
        fit="cover"
        :preview-teleported="true"
        :preview-src-list="[animeBg]">
      </el-image>
      <div class="welcome-text">
        <h1>注册成为我们的一员</h1>
        <p>加入我们的学习平台，开启您的Java学习之旅</p>
      </div>
    </div>
    
    <!-- 右侧注册表单 -->
    <div class="register-form-container">
      <div class="register-form-wrapper">
        <h2 class="register-title">注册账号</h2>
        <p class="register-desc">填写以下信息完成账号注册</p>
        
        <el-form :model="registerForm" class="register-form" :rules="rules" ref="registerFormRef" @validate="onValidate">
          <el-form-item prop="username" class="no-error-margin">
            <el-input 
              v-model="registerForm.username" 
              :prefix-icon="User"
              placeholder="用户名">
            </el-input>
            <div class="error-tip" v-if="usernameError">{{ usernameError }}</div>
          </el-form-item>
          
          <el-form-item prop="password" class="no-error-margin">
            <el-input 
              v-model="registerForm.password" 
              :prefix-icon="Lock"
              type="password" 
              placeholder="密码">
            </el-input>
            <div class="error-tip" v-if="passwordError">{{ passwordError }}</div>
          </el-form-item>
          
          <el-form-item prop="confirmPassword" class="no-error-margin">
            <el-input 
              v-model="registerForm.confirmPassword" 
              :prefix-icon="Lock"
              type="password" 
              placeholder="确认密码">
            </el-input>
            <div class="error-tip" v-if="confirmPasswordError">{{ confirmPasswordError }}</div>
          </el-form-item>
          
          <el-form-item prop="email" class="no-error-margin">
            <el-input 
              v-model="registerForm.email" 
              :prefix-icon="Message"
              placeholder="邮箱">
            </el-input>
            <div class="error-tip" v-if="emailError">{{ emailError }}</div>
          </el-form-item>
          
          <el-form-item prop="verifyCode" class="verify-code-item no-error-margin">
            <div class="verify-code-wrapper">
              <el-input 
                v-model="registerForm.verifyCode" 
                placeholder="请输入验证码">
              </el-input>
              <el-button 
                class="verify-code-btn" 
                type="primary" 
                :disabled="coldTime > 0"
                @click="sendVerifyCode">
                {{ coldTime > 0 ? `${coldTime}秒后重新获取` : '获取验证码' }}
              </el-button>
            </div>
            <div class="error-tip" v-if="codeError">{{ codeError }}</div>
          </el-form-item>
          
          <el-button type="primary" class="register-button" @click="handleRegister">立即注册</el-button>
          
          <div class="divider">
            <span>已有账号</span>
          </div>
          
          <el-button class="login-button" @click="goToLogin">返回登录</el-button>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { User, Lock, Message, EditPen } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { get, post } from '@/net'
import animeBg from '@/assets/anime-bg.svg'

const router = useRouter()
const registerFormRef = ref(null)
const isEmailValid = ref(false)
const coldTime = ref(0)

// 错误提示
const usernameError = ref('')
const emailError = ref('')
const passwordError = ref('')
const confirmPasswordError = ref('')
const codeError = ref('')

const registerForm = reactive({
  username: '',
  email: '',
  password: '',
  confirmPassword: '',
  verifyCode: ''
})

// 用户名验证
const validateUsername = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请输入用户名'))
  } else if(!/^[a-zA-Z0-9\u4e00-\u9fa5]+$/.test(value)){
    callback(new Error('用户名不能包含特殊字符，只能是中文/英文'))
  } else {
    callback()
  }
}

// 密码验证
const validatePass = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请输入密码'))
  } else {
    if (registerForm.confirmPassword !== '') {
      registerFormRef.value.validateField('confirmPassword')
    }
    callback()
  }
}

// 确认密码验证
const validatePass2 = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== registerForm.password) {
    callback(new Error('两次输入密码不一致!'))
  } else {
    callback()
  }
}

const rules = reactive({
  username: [
    { validator: validateUsername, trigger: ['blur', 'change'] },
    { min: 2, max: 8, message: '用户名的长度必须在2-8个字符之间', trigger: ['blur', 'change'] },
  ],
  email: [
    { required: true, message: '请输入邮箱地址', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: ['blur', 'change'] }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 16, message: '密码的长度必须在6-16个字符之间', trigger: ['blur', 'change'] }
  ],
  confirmPassword: [
    { validator: validatePass2, trigger: ['blur', 'change'] },
  ],
  verifyCode: [
    { required: true, message: '请输入获取的验证码', trigger: 'blur' },
  ]
})

const onValidate = (prop, isValid) => {
  if(prop === 'email')
    isEmailValid.value = isValid
}

// 发送验证码
const sendVerifyCode = async () => {
  // 清除错误提示
  emailError.value = ''
  
  if (!registerForm.email) {
    emailError.value = '请先输入邮箱地址'
    return
  }
  
  // 验证邮箱格式
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  if (!emailRegex.test(registerForm.email)) {
    emailError.value = '请输入正确的邮箱地址'
    return
  }
  
  if (coldTime.value > 0) return
  
  coldTime.value = 60
  get(`/api/auth/ask-code?email=${registerForm.email}&type=register`, () => {
    ElMessage.success(`验证码已发送至邮箱: ${registerForm.email}，请注意查收`)
    const handle = setInterval(() => {
      coldTime.value--
      if(coldTime.value === 0) {
        clearInterval(handle)
      }
    }, 1000)
  }, undefined, (message) => {
    ElMessage.warning(message)
    coldTime.value = 0
  })
}

// 处理注册
const handleRegister = async () => {
  registerFormRef.value.validate((isValid) => {
    if(isValid) {
      post('/api/auth/register', {
        username: registerForm.username,
        password: registerForm.password,
        email: registerForm.email,
        code: registerForm.verifyCode
      }, () => {
        ElMessage.success('注册成功，欢迎加入我们')
        router.push("/")
      })
    } else {
      ElMessage.warning('请完整填写注册表单内容！')
    }
  })
}

const goToLogin = () => {
  router.push('/')
}
</script>

<style scoped>
.register-container {
  display: flex;
  height: 100%;
  width: 100%;
  overflow: hidden;
}

.register-banner {
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
.register-banner::before {
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
.register-banner::after {
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

.register-form-container {
  width: 500px;
  background: #fff;
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 0 50px;
}

.register-form-wrapper {
  width: 100%;
}

.register-title {
  font-size: 28px;
  font-weight: 600;
  text-align: center;
  margin-bottom: 16px;
  color: #333;
}

.register-desc {
  font-size: 14px;
  color: #999;
  text-align: center;
  margin-bottom: 40px;
}

.register-form {
  width: 100%;
}

.register-form :deep(.el-input__wrapper) {
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
  display: flex;
  align-items: center;
  justify-content: center;
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

.register-button, .login-button {
  width: 100%;
  padding: 12px 0;
  font-size: 16px;
  border-radius: 4px;
}

.register-button {
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

.login-button {
  background-color: #f9f9f9;
  color: #666;
  border: 1px solid #dcdfe6;
}

.login-button:hover {
  background-color: #f2f2f2;
}

@media (max-width: 768px) {
  .register-container {
    flex-direction: column;
  }
  
  .register-banner {
    height: 200px;
    padding: 20px;
  }
  
  .register-form-container {
    width: 100%;
    padding: 30px 20px;
  }
}
</style> 