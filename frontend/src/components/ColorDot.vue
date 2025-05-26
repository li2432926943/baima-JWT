<script setup>
defineProps({
    color: String
})
</script>

<template>
    <div class="color-dot" :style="{background: color}"></div>
</template>

<style scoped>
.color-dot {
    width: 12px;
    height: 12px;
    border-radius: 50%;
    display: inline-block;
    vertical-align: middle;
}
</style> 