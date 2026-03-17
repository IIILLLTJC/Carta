import { createRouter, createWebHistory } from 'vue-router'
import { adminMenus, userMenus } from '@/constants/menus'
import AdminLayout from '@/layouts/AdminLayout.vue'
import UserLayout from '@/layouts/UserLayout.vue'
import LoginView from '@/views/LoginView.vue'
import AdminHomeView from '@/views/admin/AdminHomeView.vue'
import RegionManageView from '@/views/admin/RegionManageView.vue'
import UserManageView from '@/views/admin/UserManageView.vue'
import CarManageView from '@/views/admin/CarManageView.vue'
import UserHomeView from '@/views/user/UserHomeView.vue'
import ModulePlaceholder from '@/views/shared/ModulePlaceholder.vue'

const adminChildren = [
  { path: '', redirect: '/admin/home' },
  { path: 'home', component: AdminHomeView, meta: { requiresAuth: true, roles: ['ADMIN', 'SUPER_ADMIN'] } },
  { path: 'regions', component: RegionManageView, meta: { requiresAuth: true, roles: ['ADMIN', 'SUPER_ADMIN'] } },
  { path: 'cars', component: CarManageView, meta: { requiresAuth: true, roles: ['ADMIN', 'SUPER_ADMIN'] } },
  { path: 'users', component: UserManageView, meta: { requiresAuth: true, roles: ['ADMIN', 'SUPER_ADMIN'] } },
  ...adminMenus
    .filter((item) => !['/admin/home', '/admin/regions', '/admin/cars', '/admin/users'].includes(item.path))
    .map((item) => ({
      path: item.path.replace('/admin/', ''),
      component: ModulePlaceholder,
      props: {
        title: item.title,
        description: item.description,
        roleLabel: '管理端'
      },
      meta: { requiresAuth: true, roles: ['ADMIN', 'SUPER_ADMIN'] }
    }))
]

const userChildren = [
  { path: '', redirect: '/user/home' },
  { path: 'home', component: UserHomeView, meta: { requiresAuth: true, roles: ['USER'] } },
  ...userMenus
    .filter((item) => item.path !== '/user/home')
    .map((item) => ({
      path: item.path.replace('/user/', ''),
      component: ModulePlaceholder,
      props: {
        title: item.title,
        description: item.description,
        roleLabel: '用户端'
      },
      meta: { requiresAuth: true, roles: ['USER'] }
    }))
]

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', redirect: '/login' },
    { path: '/login', component: LoginView },
    { path: '/admin', component: AdminLayout, children: adminChildren },
    { path: '/user', component: UserLayout, children: userChildren }
  ]
})

router.beforeEach((to) => {
  const token = localStorage.getItem('cartaxi-token')
  const role = localStorage.getItem('cartaxi-role')

  if (to.path === '/login' && token) {
    return role === 'USER' ? '/user/home' : '/admin/home'
  }

  if (to.meta.requiresAuth && !token) {
    return '/login'
  }

  if (to.meta.roles?.length && !to.meta.roles.includes(role)) {
    return role === 'USER' ? '/user/home' : '/admin/home'
  }

  return true
})

export default router