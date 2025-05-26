<template>
  <div class="light-card">
    <slot/>
  </div>
</template>

<style scoped>
.light-card {
  background-color: var(--el-bg-color);
  border-radius: 5px;
  padding: 10px;
  margin-bottom: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
  transition: box-shadow 0.3s, transform 0.3s;
}

.light-card:hover {
  box-shadow: 0 4px 16px 0 rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

.dark .light-card {
  background-color: #2c2c30;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.2);
}

.dark .light-card:hover {
  box-shadow: 0 4px 16px 0 rgba(0, 0, 0, 0.3);
}
</style> 