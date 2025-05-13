<template>
  <div class="index-container">
    <el-container>
      <el-header>
        <div class="header-content">
          <h2>学习平台</h2>
          <el-menu mode="horizontal" :router="true" class="main-menu">
            <el-menu-item index="/index">论坛</el-menu-item>
            <el-menu-item index="/index/user-setting">用户设置</el-menu-item>
            <el-menu-item index="/index/privacy-setting">隐私设置</el-menu-item>
          </el-menu>
          <div class="user-actions">
            <el-dropdown>
              <span class="el-dropdown-link">
                用户中心<el-icon class="el-icon--right"><arrow-down /></el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item>个人信息</el-dropdown-item>
                  <el-dropdown-item @click="handleLogout">退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
      </el-header>
      <el-main>
        <router-view />
      </el-main>
      <el-footer>Copyright © 2023 学习平台</el-footer>
    </el-container>
  </div>
</template>

<script setup>
import { ArrowDown } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { logout } from '@/net'

const router = useRouter()

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
  background-color: #fff;
  border-bottom: 1px solid #eee;
  padding: 0 20px;
}

.header-content {
  display: flex;
  align-items: center;
  height: 100%;
}

.header-content h2 {
  margin-right: 40px;
  color: #409EFF;
}

.main-menu {
  flex: 1;
}

.user-actions {
  margin-left: auto;
}

.el-dropdown-link {
  cursor: pointer;
  display: flex;
  align-items: center;
}

.el-main {
  padding: 20px;
  background-color: #f5f7fa;
}

.el-footer {
  display: flex;
  justify-content: center;
  align-items: center;
  color: #999;
  font-size: 14px;
  background-color: #fff;
  border-top: 1px solid #eee;
}
</style> 