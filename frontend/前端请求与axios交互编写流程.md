好的，我来详细说明前端调用Axios的完整步骤，并对应到具体代码：

## 1. 创建网络请求模块
```javascript
// src/net/index.js
import axios from "axios";
import {ElMessage} from "element-plus";

// ... 其他代码 ...
```

## 2. 封装请求方法
```javascript
// 封装GET请求
function get(url, success, failure = defaultFailure) {
    internalGet(url, accessHeader(), success, failure)
}

// 封装POST请求
function post(url, data, success, failure = defaultFailure) {
    internalPost(url, data, accessHeader(), success, failure)
}
```

## 3. 导出请求函数
```javascript
export { post, get, login, logout, unauthorized, accessHeader }
```

## 4. 在组件中使用的具体步骤

### 第一步：导入请求函数
```javascript
// 在组件中导入所需的请求函数
import { login, get, post } from '@/net/index.js'
```

### 第二步：创建请求数据
```javascript
// 登录表单数据
const loginForm = reactive({
  username: '',
  password: '',
  remember: false
})
```

### 第三步：定义调用函数并处理响应
```javascript
const handleLogin = async () => {
  if (!loginFormRef.value) return
  
  await loginFormRef.value.validate((valid) => {
    if (valid) {
      // 调用login函数发送请求
      login(
        loginForm.username,       // 参数1：用户名
        loginForm.password,       // 参数2：密码
        loginForm.remember,       // 参数3：是否记住登录状态
        // 参数4：成功回调函数
        (userData) => {
          // 请求成功后的操作
          console.log('登录成功，获取到的用户数据:', userData)
          router.push('/index')   // 跳转到首页
        },
        // 参数5：失败回调函数(可选)
        (message, code, url) => {
          console.error(`登录失败: ${message}, 错误码: ${code}`)
          // 特定错误处理
        }
      )
    }
  })
}
```

### 第四步：绑定函数到页面事件
```html
<el-button type="primary" class="login-button" @click="handleLogin">立即登录</el-button>
```

## 5. 数据获取示例

### 第一步：定义状态变量
```javascript
const userInfo = ref({})  // 用于存储获取到的数据
const loading = ref(true) // 加载状态
```

### 第二步：创建获取数据的函数
```javascript
const fetchUserInfo = () => {
  loading.value = true
  // 调用get函数获取数据
  get(
    '/api/user/info',            // 参数1：请求地址
    // 参数2：成功回调
    (data) => {
      userInfo.value = data      // 保存获取到的数据
      loading.value = false      // 更新加载状态
    }
    // 参数3：失败回调(可选，使用默认处理)
  )
}
```

### 第三步：在生命周期钩子中调用
```javascript
// 页面加载时自动获取数据
onMounted(() => {
  fetchUserInfo()
})
```

### 第四步：在模板中展示数据
```html
<div v-if="loading">加载中...</div>
<div v-else>
  <p>用户名: {{ userInfo.username }}</p>
  <p>邮箱: {{ userInfo.email }}</p>
  <!-- 其他数据展示 -->
</div>
```

## 6. 数据提交示例

### 第一步：准备提交的数据
```javascript
const updateForm = reactive({
  nickname: '',
  phone: ''
})
```

### 第二步：创建提交数据的函数
```javascript
const updateUserInfo = () => {
  // 调用post函数发送数据
  post(
    '/api/user/update',          // 参数1：请求地址
    updateForm,                  // 参数2：要提交的数据
    // 参数3：成功回调
    () => {
      ElMessage.success('个人信息更新成功')
      fetchUserInfo()            // 重新获取最新数据
    }
    // 参数4：失败回调(可选，使用默认处理)
  )
}
```

### 第三步：绑定到按钮点击事件
```html
<el-button type="primary" @click="updateUserInfo">保存修改</el-button>
```

通过以上完整步骤，前端组件就可以与后端API进行交互，实现数据的获取和提交功能。这些请求会自动携带认证信息并统一处理各种响应情况。