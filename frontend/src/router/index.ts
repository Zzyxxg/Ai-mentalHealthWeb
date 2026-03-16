import { createRouter, createWebHistory } from 'vue-router'

import { useAuthStore } from '../stores/auth'
import { pinia } from '../stores/pinia'
import type { UserRole } from '../types/auth'

import AdminLayout from '../views/layouts/AdminLayout.vue'
import CounselorLayout from '../views/layouts/CounselorLayout.vue'
import StudentLayout from '../views/layouts/StudentLayout.vue'
import ForbiddenPage from '../views/pages/ForbiddenPage.vue'
import LoginPage from '../views/pages/LoginPage.vue'
import NotFoundPage from '../views/pages/NotFoundPage.vue'

function homePathByRole(role: UserRole | '') {
  if (role === 'ADMIN') return '/admin'
  if (role === 'CONSULTANT') return '/counselor'
  if (role === 'STUDENT') return '/student'
  return '/login'
}

export const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', redirect: () => homePathByRole(useAuthStore(pinia).role) },
    { path: '/login', component: LoginPage },
    { path: '/403', component: ForbiddenPage },
    {
      path: '/student',
      component: StudentLayout,
      meta: { auth: true, roles: ['STUDENT'] },
      children: [{ path: '', name: 'student-home', component: () => import('../views/pages/student/StudentHome.vue') }],
    },
    {
      path: '/counselor',
      component: CounselorLayout,
      meta: { auth: true, roles: ['CONSULTANT'] },
      children: [{ path: '', name: 'counselor-home', component: () => import('../views/pages/counselor/CounselorHome.vue') }],
    },
    {
      path: '/admin',
      component: AdminLayout,
      meta: { auth: true, roles: ['ADMIN'] },
      children: [{ path: '', name: 'admin-home', component: () => import('../views/pages/admin/AdminHome.vue') }],
    },
    { path: '/:pathMatch(.*)*', name: 'not-found', component: NotFoundPage },
  ],
})

router.beforeEach((to) => {
  const authStore = useAuthStore(pinia)
  const isAuthed = authStore.isAuthed
  const role = authStore.role

  if (to.path === '/login' && isAuthed) return homePathByRole(role)

  const requiresAuth = Boolean(to.meta.auth)
  if (requiresAuth && !isAuthed) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }

  const allowedRoles = (to.meta.roles as UserRole[] | undefined) ?? undefined
  if (allowedRoles && (!role || !allowedRoles.includes(role as UserRole))) return '/403'

  return true
})
