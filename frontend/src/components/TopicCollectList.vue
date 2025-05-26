<script setup>
import {ref, defineEmits, defineProps} from 'vue'

const props = defineProps({
    show: Boolean
})

const emit = defineEmits(['close'])

const collectList = ref([
    {id: 1, title: '示例收藏主题1', time: new Date()},
    {id: 2, title: '示例收藏主题2', time: new Date()},
])

function handleClose() {
    emit('close')
}
</script>

<template>
    <el-drawer :model-value="show" title="我的收藏" size="400px" @close="handleClose" @update:model-value="handleClose">
        <div v-if="collectList.length">
            <div v-for="item in collectList" :key="item.id" class="collect-item">
                <div class="collect-title">{{item.title}}</div>
                <div class="collect-time">{{item.time.toLocaleDateString()}}</div>
            </div>
        </div>
        <el-empty v-else description="暂无收藏内容"/>
    </el-drawer>
</template>

<style scoped>
.collect-item {
    padding: 15px;
    border-bottom: 1px solid var(--el-border-color-light);
    cursor: pointer;
    transition: background-color 0.3s;
}

.collect-item:hover {
    background-color: var(--el-fill-color-light);
}

.collect-title {
    font-weight: bold;
    margin-bottom: 5px;
}

.collect-time {
    font-size: 12px;
    color: var(--el-text-color-secondary);
}
</style> 