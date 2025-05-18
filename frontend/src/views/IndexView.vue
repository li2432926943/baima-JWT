<template>
  <div class="index-container">
    <el-container>
      <el-header>
        <div class="header-content">
          <el-image class="logo" src="https://element-plus.org/images/element-plus-logo.svg"/>
          <el-menu mode="horizontal" :router="true" class="main-menu">
            <el-menu-item index="/index">首页</el-menu-item>
            <el-menu-item index="/about">关于</el-menu-item>
            <el-menu-item index="/index/learning">学习平台</el-menu-item>
            <el-menu-item index="/index/forum">论坛</el-menu-item>
            <el-menu-item index="/index/user-setting">用户设置</el-menu-item>
            <el-menu-item index="/index/privacy-setting">隐私设置</el-menu-item>
          </el-menu>
          <div class="user-actions">
            <el-switch
              v-model="isDark"
              class="theme-switch"
              inline-prompt
              :active-icon="Moon"
              :inactive-icon="Sunny"
            />
            <el-dropdown class="user-dropdown">
              <div class="user-avatar-wrapper">
                <el-avatar :size="32" :src="defaultAvatar" />
                <span class="user-name">用户名</span>
              </div>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item>个人中心</el-dropdown-item>
                  <el-dropdown-item>设置</el-dropdown-item>
                  <el-dropdown-item divided @click="handleLogout">退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
      </el-header>
      <el-main>
        <router-view />
      </el-main>
      <el-footer>Copyright © 2023 白马JWT</el-footer>
    </el-container>
  </div>
</template>

<script setup>
import { useDark, useToggle } from '@vueuse/core'
import { ref, watch } from 'vue'
import { Moon, Sunny, ArrowDown } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { logout } from '@/net'

// 使用VueUse的深色模式钩子
const isDark = useDark()
const toggleDark = useToggle(isDark)
const router = useRouter()

// 默认头像URL
const defaultAvatar = ref('https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png')

// 当深色模式变化时更新Element Plus的主题
watch(isDark, (val) => {
  // 更新Element Plus主题
  document.documentElement.setAttribute('data-theme', val ? 'dark' : 'light')
  // 自动设置html标签的类
  document.documentElement.setAttribute('class', val ? 'dark' : 'light')
}, { immediate: true })

// 退出登录
const handleLogout = () => {
  logout(() => {
    router.push('/')
  })
}
</script>

<style scoped>
.index-container {
  height: 100%;
}

.el-container {
  height: 100%;
}

.el-header {
  background-color: var(--el-bg-color);
  border-bottom: 1px solid var(--el-border-color-light);
  padding: 0 20px;
}

.header-content {
  display: flex;
  align-items: center;
  height: 100%;
}

.logo {
  height: 40px;
  margin-right: 40px;
}

.main-menu {
  flex: 1;
  border-bottom: none;
}

.user-actions {
  margin-left: auto;
  display: flex;
  align-items: center;
  gap: 16px;
}

.theme-switch {
  margin-right: 8px;
}

.user-avatar-wrapper {
  display: flex;
  align-items: center;
  cursor: pointer;
  padding: 2px 8px;
  border-radius: 4px;
  transition: background-color 0.3s;
}

.user-avatar-wrapper:hover {
  background-color: var(--el-fill-color-light);
}

.user-name {
  margin-left: 8px;
  font-size: 14px;
  color: var(--el-text-color-primary);
}

.user-dropdown {
  height: 100%;
  display: flex;
  align-items: center;
}

.el-main {
  padding: 20px;
  background-color: var(--el-bg-color-page);
}

.el-footer {
  display: flex;
  justify-content: center;
  align-items: center;
  color: var(--el-text-color-secondary);
  font-size: 14px;
  background-color: var(--el-bg-color);
  border-top: 1px solid var(--el-border-color-light);
  height: 60px !important;
}
</style>