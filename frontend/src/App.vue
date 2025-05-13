<template>
  <div class="app-container">
    <router-view />
  </div>
</template>

<script setup>
// 原来引入LoginPage组件的代码可以移除，由路由系统管理
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
}
</style> 