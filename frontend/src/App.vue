<template>
  <div class="app-container" :class="{ 'dark': isDark }">
    <div class="theme-toggle">
      <span class="theme-text">{{ isDark ? '暗黑模式' : '浅色模式' }}</span>
      <el-switch
        v-model="isDark"
        active-text="暗"
        inactive-text="亮"
        inline-prompt
      />
    </div>
    <router-view />
  </div>
</template>

<script setup>
import { useDark, useToggle } from '@vueuse/core'
import { watch } from 'vue'

// 使用VueUse的深色模式钩子
const isDark = useDark()
const toggleDark = useToggle(isDark)

// 当深色模式变化时更新Element Plus的主题
watch(isDark, (val) => {
  // 更新Element Plus主题
  document.documentElement.setAttribute('data-theme', val ? 'dark' : 'light')
  // 自动设置html标签的类
  document.documentElement.setAttribute('class', val ? 'dark' : 'light')
}, { immediate: true })
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html, body, #app, .app-container {
  height: 100%;
  width: 100%;
  font-family: "Helvetica Neue", Helvetica, "PingFang SC", "Hiragino Sans GB", "Microsoft YaHei", "微软雅黑", Arial, sans-serif;
  transition: color 0.3s, background-color 0.3s;
}

/* 深色模式样式 */
html.dark {
  color-scheme: dark;
}

.theme-toggle {
  position: fixed;
  top: 20px;
  right: 20px;
  z-index: 9999;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: rgba(255, 255, 255, 0.4);
  padding: 8px 12px;
  border-radius: 20px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  transition: all 0.3s;
}

.theme-text {
  margin-right: 10px;
  font-size: 14px;
  font-weight: 500;
  transition: color 0.3s;
}

html.dark .theme-toggle {
  background-color: rgba(30, 30, 30, 0.7);
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.4);
}

html.dark .theme-text {
  color: white;
}

/* 设置深色模式下的全局样式 */
.dark {
  background-color: #222;
  color: #fff;
}

/* 确保Element Plus组件遵循深色主题 */
html.dark .el-button {
  --el-button-bg-color: #333;
  --el-button-text-color: #ddd;
  --el-button-border-color: #444;
}

html.dark .el-input__wrapper {
  background-color: #333;
  border-color: #444;
}

html.dark .el-input__inner {
  color: #ddd;
}

/* 覆盖 Element Plus 开关样式 */
:deep(.el-switch) {
  --el-switch-on-color: #409eff;
  --el-switch-off-color: #dcdfe6;
}
</style> 