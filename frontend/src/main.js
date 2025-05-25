import { createApp } from 'vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import 'element-plus/theme-chalk/dark/css-vars.css'  // 引入Element Plus深色模式CSS变量
import '@/assets/styles/dark-theme.css'  // 引入自定义深色模式样式
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import App from './App.vue'
import router from './router'
import { clearAllTokens } from './net' // 导入清除token函数
import axios from 'axios' // 导入axios
import { createPinia } from "pinia";

// 应用启动时清除所有token，确保以未登录状态开始
clearAllTokens()

// 设置axios默认baseURL
axios.defaults.baseURL = 'http://localhost:8080'
console.log('axios baseURL 设置为:', axios.defaults.baseURL)

const app = createApp(App)

// 注册所有图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}
app.use(createPinia())
app.use(ElementPlus)
app.use(router)
app.mount('#app') 