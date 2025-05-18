<template>
  <div class="app-container" :class="{ 'dark': isDark }">
    <router-view />
  </div>
</template>

<script setup>
import { useDark, useToggle } from '@vueuse/core'
import { watch, ref } from 'vue'
import { Moon, Sunny } from '@element-plus/icons-vue'

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