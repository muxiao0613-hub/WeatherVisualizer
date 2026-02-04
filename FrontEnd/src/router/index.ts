import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router'
import Dashboard from '@/views/Dashboard.vue'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    name: 'Dashboard',
    component: Dashboard
  }
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
})

export default router
