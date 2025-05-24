<template>
  <div class="main-content" v-loading="loading" element-loading-text="正在进入，请稍后...">
    <el-container style="height: 100%" v-if="!loading">
      <el-header class="main-content-header">
        <el-image class="logo" src="https://element-plus.org/images/element-plus-logo.svg"/>
        <div style="flex: 1;padding: 0 20px;text-align: center">
          <el-input v-model="searchInput.text" style="width: 100%;max-width: 500px"
                    placeholder="搜索论坛相关内容...">
            <template #prefix>
              <el-icon>
                <Search/>
              </el-icon>
            </template>
            <template #append>
              <el-select style="width: 120px" v-model="searchInput.type">
                <el-option value="1" label="帖子广场"/>
                <el-option value="2" label="校园活动"/>
                <el-option value="3" label="表白墙"/>
                <el-option value="4" label="教务通知"/>
              </el-select>
            </template>
          </el-input>
        </div>
        <div class="user-info">
          <el-tooltip content="切换深浅色模式" placement="bottom">
            <div class="theme-switch" @click="toggleDark()">
              <el-icon><component :is="isDark ? Moon : Sunny"/></el-icon>
              <div style="font-size: 10px">主题</div>
            </div>
          </el-tooltip>
          <el-popover placement="bottom" :width="350" trigger="click">
            <template #reference>
              <el-badge style="margin-right: 15px" is-dot :hidden="!notification.length">
                <div class="notification">
                  <el-icon><Bell/></el-icon>
                  <div style="font-size: 10px">消息</div>
                </div>
              </el-badge>
            </template>
            <el-empty :image-size="80" description="暂时没有未读消息哦~" v-if="!notification.length"/>
            <el-scrollbar :max-height="500" v-else>
              <light-card v-for="item in notification" class="notification-item"
                          @click="confirmNotification(item.id, item.url)">
                <div>
                  <el-tag size="small" :type="item.type">消息</el-tag>&nbsp;
                  <span style="font-weight: bold">{{item.title}}</span>
                </div>
                <el-divider style="margin: 7px 0 3px 0"/>
                <div style="font-size: 13px;color: grey">
                  {{item.content}}
                </div>
              </light-card>
            </el-scrollbar>
            <div style="margin-top: 10px">
              <el-button size="small" type="info" :icon="Check" @click="deleteAllNotification"
                         style="width: 100%" plain>清除全部未读消息</el-button>
            </div>
          </el-popover>
          <div class="profile">
            <div>{{ username }}</div>
            <div>{{ email }}</div>
          </div>
          <el-dropdown>
            <el-avatar :src="defaultAvatar"/>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item>
                  <el-icon>
                    <Operation/>
                  </el-icon>
                  个人设置
                </el-dropdown-item>
                <el-dropdown-item>
                  <el-icon>
                    <Message/>
                  </el-icon>
                  消息列表
                </el-dropdown-item>
                <el-dropdown-item @click="handleLogout" divided>
                  <el-icon>
                    <Back/>
                  </el-icon>
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      <el-container>
        <el-aside width="230px">
          <el-scrollbar style="height: calc(100vh - 55px)">
            <el-menu
                    router
                    :default-active="$route.path"
                    :default-openeds="['1', '2', '3']"
                    style="min-height: calc(100vh - 55px)">
              <el-sub-menu index="1">
                <template #title>
                  <el-icon>
                    <Location/>
                  </el-icon>
                  <span><b>校园论坛</b></span>
                </template>
                <el-menu-item index="/index">
                  <template #title>
                    <el-icon>
                      <ChatDotSquare/>
                    </el-icon>
                    帖子广场
                  </template>
                </el-menu-item>
                <el-menu-item>
                  <template #title>
                    <el-icon>
                      <Bell/>
                    </el-icon>
                    失物招领
                  </template>
                </el-menu-item>
                <el-menu-item>
                  <template #title>
                    <el-icon>
                      <Notification/>
                    </el-icon>
                    校园活动
                  </template>
                </el-menu-item>
                <el-menu-item>
                  <template #title>
                    <el-icon>
                      <Umbrella/>
                    </el-icon>
                    表白墙
                  </template>
                </el-menu-item>
                <el-menu-item>
                  <template #title>
                    <el-icon>
                      <School/>
                    </el-icon>
                    海文考研
                    <el-tag style="margin-left: 10px" size="small">合作机构</el-tag>
                  </template>
                </el-menu-item>
              </el-sub-menu>
              <el-sub-menu index="2">
                <template #title>
                  <el-icon>
                    <Position/>
                  </el-icon>
                  <span><b>探索与发现</b></span>
                </template>
                <el-menu-item>
                  <template #title>
                    <el-icon>
                      <Document/>
                    </el-icon>
                    成绩查询
                  </template>
                </el-menu-item>
                <el-menu-item>
                  <template #title>
                    <el-icon>
                      <Files/>
                    </el-icon>
                    班级课程表
                  </template>
                </el-menu-item>
                <el-menu-item>
                  <template #title>
                    <el-icon>
                      <Monitor/>
                    </el-icon>
                    教务通知
                  </template>
                </el-menu-item>
                <el-menu-item>
                  <template #title>
                    <el-icon>
                      <Collection/>
                    </el-icon>
                    在线图书馆
                  </template>
                </el-menu-item>
                <el-menu-item>
                  <template #title>
                    <el-icon>
                      <DataLine/>
                    </el-icon>
                    预约教室
                  </template>
                </el-menu-item>
              </el-sub-menu>
              <el-sub-menu index="3">
                <template #title>
                  <el-icon>
                    <Operation/>
                  </el-icon>
                  <span><b>个人设置</b></span>
                </template>
                <el-menu-item index="/index/user-setting">
                  <template #title>
                    <el-icon>
                      <User/>
                    </el-icon>
                    个人信息设置
                  </template>
                </el-menu-item>
                <el-menu-item index="/index/privacy-setting">
                  <template #title>
                    <el-icon>
                      <Lock/>
                    </el-icon>
                    账号安全设置
                  </template>
                </el-menu-item>
              </el-sub-menu>
              
              <!-- 添加开发测试专用区域 -->
              <el-divider>开发测试区域</el-divider>
              <el-menu-item index="/login">
                <template #title>
                  <el-icon><Back /></el-icon>
                  访问登录页面
                </template>
              </el-menu-item>
            </el-menu>
          </el-scrollbar>
        </el-aside>
        <el-main class="main-content-page">
          <el-scrollbar style="height: calc(100vh - 55px)">
            <router-view v-slot="{ Component }">
              <transition name="el-fade-in-linear" mode="out-in">
                <component :is="Component" style="height: 100%"/>
              </transition>
            </router-view>
          </el-scrollbar>
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import {get, logout} from '@/net'
import { useRouter } from 'vue-router'
import {reactive, ref, watch} from "vue"
import {
  Back,
  Bell,
  ChatDotSquare, Check, Collection, DataLine,
  Document, Files,
  Location, Lock, Message, Monitor,
  Notification, Operation,
  Position,
  School, Search,
  Umbrella, User, Moon, Sunny
} from "@element-plus/icons-vue"
import { useDark, useToggle } from '@vueuse/core'
import LightCard from "@/components/LightCard.vue"
import { useStore } from "@/store"

// 使用状态管理
const store = useStore()

// 深色模式支持
const isDark = useDark()
const toggleDark = useToggle(isDark)

const router = useRouter()

// 设置默认头像
const defaultAvatar = ref(store.avatarUrl)

// 当深色模式变化时更新Element Plus的主题
watch(isDark, (val) => {
  document.documentElement.setAttribute('data-theme', val ? 'dark' : 'light')
  document.documentElement.setAttribute('class', val ? 'dark' : 'light')
  localStorage.setItem('theme', val ? 'dark' : 'light')
}, { immediate: true })

const loading = ref(true)
const username = ref(store.user.username)
const email = ref(store.user.email)

const searchInput = reactive({
    type: '1',
    text: ''
})

const notification = ref([])

// 模拟加载效果，这样用户体验会更好
setTimeout(() => {
  loading.value = false
}, 1000)

// 加载通知消息
get('/api/notification/list', 
  data => notification.value = data,
  (errorMsg) => {
    console.warn('获取通知列表失败:', errorMsg)
    // 设置空通知列表，避免界面出错
    notification.value = []
  }
)

// 加载用户信息
// 在开发或生产环境中都会尝试获取用户信息
get('/api/user/info', 
  (data) => {
  store.user = data
  username.value = store.user.username
  email.value = store.user.email
  },
  (errorMsg) => {
    console.warn('获取用户信息失败:', errorMsg)
    // 失败时也结束加载状态
    loading.value = false
  }
)

// 退出登录功能
const handleLogout = () => {
  logout(() => {
    router.push('/')
  })
}

// 消息通知相关功能（目前只是占位）
function confirmNotification(id, url) {
    // 实现确认通知的功能
}

function deleteAllNotification() {
    // 实现删除所有通知的功能
    notification.value = []
}
</script>

<style lang="less" scoped>
.notification-item {
    transition: .3s;
    &:hover {
        cursor: pointer;
        opacity: 0.7;
    }
}

.notification {
    font-size: 22px;
    line-height: 14px;
    text-align: center;
    transition: color .3s;

    &:hover {
        color: grey;
        cursor: pointer;
    }
}

.main-content-page {
    padding: 0;
    background-color: #f7f8fa;
    height: calc(100vh - 55px);  /* 减去头部高度 */
    display: flex;
    flex-direction: column;
}

.dark .main-content-page {
    background-color: #212225;
}

.main-content {
    height: 100vh;
    width: 100vw;
    display: flex;
    flex-direction: column;
    overflow: hidden;  /* 防止出现滚动条 */
}

.main-content-header {
    border-bottom: solid 1px var(--el-border-color);
    height: 55px;
    display: flex;
    align-items: center;
    box-sizing: border-box;
    flex-shrink: 0;  /* 防止头部被压缩 */

    .logo {
        height: 32px;
    }

    .user-info {
        display: flex;
        justify-content: flex-end;
        align-items: center;

        .el-avatar:hover {
            cursor: pointer;
        }

        .profile {
            text-align: right;
            margin-right: 20px;

            :first-child {
                font-size: 18px;
                font-weight: bold;
                line-height: 20px;
            }

            :last-child {
                font-size: 10px;
                color: grey;
            }
        }
    }
}

.theme-switch {
    cursor: pointer;
    margin-right: 15px;
    text-align: center;
    color: var(--el-text-color-regular);
}

.theme-switch:hover {
    color: var(--el-color-primary);
}

/* 调整侧边栏和主内容区域的布局 */
.el-container {
    height: calc(100vh - 55px);
    flex: 1;
    overflow: hidden;
}

.el-aside {
    height: 100%;
    overflow: hidden;
    border-right: 1px solid var(--el-border-color);
}

.el-main {
    height: 100%;
    overflow: hidden;
    padding: 0;
}

/* 调整滚动区域 */
.el-scrollbar {
    height: 100%;
    
    :deep(.el-scrollbar__wrap) {
        overflow-x: hidden;
    }
}

/* 调整菜单样式 */
.el-menu {
    border-right: none;
}

/* 路由过渡动画容器 */
.el-fade-in-linear-enter-active {
    transition: all 0.3s ease;
}

.el-fade-in-linear-leave-active {
    transition: all 0.3s ease;
}

.el-fade-in-linear-enter-from,
.el-fade-in-linear-leave-to {
    opacity: 0;
}
</style>