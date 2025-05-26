<script setup>
defineProps({
    type: Object
})
</script>

<template>
    <el-tag v-if="type" :color="type.color" size="small" style="color: white;">
        {{type.name}}
    </el-tag>
</template>

<style scoped>

</style> 