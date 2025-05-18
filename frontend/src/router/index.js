import { createRouter, createWebHistory } from 'vue-router'
import { unauthorized } from "@/net/index";

// 开发环境标志，打包时可改为false
const isDevelopment = true;

const router = createRouter({
    history: createWebHistory(import.meta.env.BASE_URL),
    routes: [
        {
            path: '/',
            name: 'welcome',
            component: () => import('@/views/WelcomeView.vue'),
            children: [
                {
                    path: '',
                    name: 'welcome-login',
                    component: () => import('@/views/welcome/LoginPage.vue')
                }, {
                    path: 'register',
                    name: 'welcome-register',
                    component: () => import('@/views/welcome/RegisterPage.vue')
                }, {
                    path: 'forget',
                    name: 'welcome-forget',
                    component: () => import('@/views/welcome/ForgetPage.vue')
                }, {
                    path: 'reset',
                    name: 'welcome-reset',
                    component: () => import('@/views/welcome/ResetPasswordPage.vue')
                }
            ]
        },
        // 添加专用的登录页面路由，即使在开发环境也可以访问
        {
            path: '/login',
            name: 'login-page',
            component: () => import('@/views/WelcomeView.vue'),
            children: [
                {
                    path: '',
                    name: 'login-default',
                    component: () => import('@/views/welcome/LoginPage.vue')
                }
            ]
        },
        {
            path: '/index',
            name: 'index',
            component: () => import('@/views/IndexView.vue'),
            children: [
                {
                    path: '',
                    name: 'index-default',
                    component: () => import('@/views/index/ForumPage.vue')
                },
                {
                    path: 'user-setting',
                    name: 'user-setting',
                    component: () => import('@/views/index/UserSettingPage.vue')
                },
                {
                    path: 'privacy-setting',
                    name: 'privacy-setting',
                    component: () => import('@/views/index/PrivacySettingPage.vue')
                }
            ]
        }
    ]
})

router.beforeEach((to, from, next) => {
    // 开发环境下，跳过登录验证
    if (isDevelopment) {
        // 直接跳转到主页，除非明确请求登录页
        if (to.path === '/' && !to.path.startsWith('/login')) {
            next('/index');
        } else {
            next();
        }
        return;
    }
    
    // 生产环境正常验证登录状态
    const isUnauthorized = unauthorized()
    if(to.name && to.name.startsWith('welcome') && !isUnauthorized) {
        next('/index')
    } else if(to.fullPath.startsWith('/index') && isUnauthorized) {
        next('/')
    } else {
        next()
    }
})

export default router 