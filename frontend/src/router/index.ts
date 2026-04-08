import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router'
import { isAuthenticated } from '../api/auth'

const routes: Array<RouteRecordRaw> = [
  {
    path: '/',
    redirect: '/chat'  // 默认跳转到聊天页面（会被路由守卫拦截）
  },
  {
    path: '/login',
    name: 'login',
    component: () => import('../views/LoginView.vue')
  },
  {
    path: '/chat',
    name: 'chat',
    component: () => import('../views/ChatView.vue'),
    meta: { requiresAuth: true }
  }
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
})

// 路由守卫 - 使用新的认证检查
router.beforeEach((to, _from, next) => {
  const authenticated = isAuthenticated()
  
  // 需要认证的页面
  if (to.meta.requiresAuth && !authenticated) {
    next('/login')
  } 
  // 已登录用户访问登录页，重定向到聊天页
  else if (to.path === '/login' && authenticated) {
    next('/chat')
  } 
  else {
    next()
  }
})

export default router
