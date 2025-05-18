<template>
  <div class="forum-container">
    <div class="forum-header">
      <h2>帖子广场</h2>
      <el-button type="primary" size="large" round>
        <el-icon><Plus /></el-icon>发布新帖
      </el-button>
    </div>
    <div class="forum-content">
      <el-empty description="暂无内容，快来发布第一篇帖子吧！" />
    </div>
  </div>
</template>

<script setup>
import { Plus } from '@element-plus/icons-vue'
</script>

<style scoped>
.forum-container {
  padding: 20px;
  background-color: var(--el-bg-color);
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
  margin: 20px;
  min-height: calc(100vh - 95px);
}

.forum-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 1px solid var(--el-border-color-light);
}

.forum-content {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 400px;
}
</style> 