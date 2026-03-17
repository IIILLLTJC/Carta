import { createRouter, createWebHistory } from 'vue-router'
import { adminMenus } from '@/constants/menus'
import AdminLayout from '@/layouts/AdminLayout.vue'
import UserLayout from '@/layouts/UserLayout.vue'
import LoginView from '@/views/LoginView.vue'
import AdminHomeView from '@/views/admin/AdminHomeView.vue'
import AdminManageView from '@/views/admin/AdminManageView.vue'
import AdminProfileView from '@/views/admin/AdminProfileView.vue'
import RegionManageView from '@/views/admin/RegionManageView.vue'
import UserManageView from '@/views/admin/UserManageView.vue'
import CarManageView from '@/views/admin/CarManageView.vue'
import DeployManageView from '@/views/admin/DeployManageView.vue'
import OrderCenterView from '@/views/admin/OrderCenterView.vue'
import ReturnManageView from '@/views/admin/ReturnManageView.vue'
import RecoveryManageView from '@/views/admin/RecoveryManageView.vue'
import UserHomeView from '@/views/user/UserHomeView.vue'
import UserPointView from '@/views/user/UserPointView.vue'
import UserOrderView from '@/views/user/UserOrderView.vue'
import UserReturnView from '@/views/user/UserReturnView.vue'
import UserProfileView from '@/views/user/UserProfileView.vue'
import ModulePlaceholder from '@/views/shared/ModulePlaceholder.vue'

const builtInAdminRoutes = [
  '/admin/home',
  '/admin/profile',
  '/admin/users',
  '/admin/regions',
  '/admin/cars',
  '/admin/deploy',
  '/admin/orders',
  '/admin/returns',
  '/admin/recovery',
  '/admin/admins'
]

const adminChildren = [
  { path: '', redirect: '/admin/home' },
  { path: 'home', component: AdminHomeView, meta: { requiresAuth: true, roles: ['ADMIN', 'SUPER_ADMIN'] } },
  { path: 'profile', component: AdminProfileView, meta: { requiresAuth: true, roles: ['ADMIN', 'SUPER_ADMIN'] } },
  { path: 'regions', component: RegionManageView, meta: { requiresAuth: true, roles: ['ADMIN', 'SUPER_ADMIN'] } },
  { path: 'cars', component: CarManageView, meta: { requiresAuth: true, roles: ['ADMIN', 'SUPER_ADMIN'] } },
  { path: 'users', component: UserManageView, meta: { requiresAuth: true, roles: ['ADMIN', 'SUPER_ADMIN'] } },
  { path: 'deploy', component: DeployManageView, meta: { requiresAuth: true, roles: ['ADMIN', 'SUPER_ADMIN'] } },
  { path: 'orders', component: OrderCenterView, meta: { requiresAuth: true, roles: ['ADMIN', 'SUPER_ADMIN'] } },
  { path: 'returns', component: ReturnManageView, meta: { requiresAuth: true, roles: ['ADMIN', 'SUPER_ADMIN'] } },
  { path: 'recovery', component: RecoveryManageView, meta: { requiresAuth: true, roles: ['ADMIN', 'SUPER_ADMIN'] } },
  { path: 'admins', component: AdminManageView, meta: { requiresAuth: true, roles: ['ADMIN', 'SUPER_ADMIN'] } },
  ...adminMenus
    .filter((item) => !builtInAdminRoutes.includes(item.path))
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
  { path: 'points', component: UserPointView, meta: { requiresAuth: true, roles: ['USER'] } },
  { path: 'orders', component: UserOrderView, meta: { requiresAuth: true, roles: ['USER'] } },
  { path: 'returns', component: UserReturnView, meta: { requiresAuth: true, roles: ['USER'] } },
  { path: 'profile', component: UserProfileView, meta: { requiresAuth: true, roles: ['USER'] } }
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