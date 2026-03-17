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
      children: [
        { path: '', name: 'student-home', component: () => import('../views/pages/student/StudentHome.vue') },
        { path: 'appointments', name: 'student-appointments', component: () => import('../views/pages/student/MyAppointments.vue') },
        { path: 'consultations', name: 'student-consultations', component: () => import('../views/pages/student/ConsultationList.vue') },
        { path: 'consultations/:id', name: 'student-consultation-detail', component: () => import('../views/pages/student/ConsultationDetail.vue') },
        { path: 'notifications', name: 'student-notifications', component: () => import('../views/pages/common/NotificationList.vue') },
        { path: 'assessments', name: 'student-assessment-home', component: () => import('../views/pages/student/AssessmentHome.vue') },
        { path: 'assessments/history', name: 'student-assessment-history', component: () => import('../views/pages/student/AssessmentHistory.vue') },
        { path: 'assessments/:type', name: 'student-assessment-form', component: () => import('../views/pages/student/AssessmentForm.vue') },
        { path: 'assessments/result', name: 'student-assessment-result', component: () => import('../views/pages/student/AssessmentResult.vue') },
      ],
    },
    {
      path: '/counselor',
      component: CounselorLayout,
      meta: { auth: true, roles: ['CONSULTANT'] },
      children: [
        { path: '', name: 'counselor-home', component: () => import('../views/pages/counselor/CounselorHome.vue') },
        { path: 'schedule', name: 'counselor-schedule', component: () => import('../views/pages/counselor/ScheduleManage.vue') },
        { path: 'appointments', name: 'counselor-appointments', component: () => import('../views/pages/counselor/AppointmentList.vue') },
        { path: 'consultations', name: 'counselor-consultations', component: () => import('../views/pages/counselor/ConsultationList.vue') },
        { path: 'consultations/:id', name: 'counselor-consultation-detail', component: () => import('../views/pages/counselor/ConsultationDetail.vue') },
        { path: 'notifications', name: 'counselor-notifications', component: () => import('../views/pages/common/NotificationList.vue') },
      ],
    },
    {
      path: '/admin',
      component: AdminLayout,
      meta: { auth: true, roles: ['ADMIN'] },
      children: [
        { path: '', name: 'admin-home', component: () => import('../views/pages/admin/AdminHome.vue') },
        { path: 'users', name: 'admin-users', component: () => import('../views/pages/admin/AdminUsers.vue') },
        { path: 'appointments', name: 'admin-appointments', component: () => import('../views/pages/admin/AdminAppointments.vue') },
        { path: 'consultations', name: 'admin-consultations', component: () => import('../views/pages/admin/AdminConsultations.vue') },
        { path: 'stats', name: 'admin-stats', component: () => import('../views/pages/admin/AdminStats.vue') },
        { path: 'audit-logs', name: 'admin-audit-logs', component: () => import('../views/pages/admin/AdminAuditLogs.vue') },
        { path: 'notifications', name: 'admin-notifications', component: () => import('../views/pages/common/NotificationList.vue') },
      ],
    },
    { path: '/:pathMatch(.*)*', name: 'not-found', component: NotFoundPage },
  ],
})

router.beforeEach((to) => {
  const authStore = useAuthStore(pinia)
  const isAuthed = authStore.isAuthed
  const role = authStore.role

  // 调试日志：排查“无权限”问题
  console.log('[Router Guard] Navigating to:', to.path, '| role:', role, '| isAuthed:', isAuthed)

  if (to.path === '/login' && isAuthed) return homePathByRole(role)

  const requiresAuth = Boolean(to.meta.auth)
  if (requiresAuth && !isAuthed) {
    console.warn('[Router Guard] Unauthorized access to:', to.path, ', redirecting to login')
    return { path: '/login', query: { redirect: to.fullPath } }
  }

  const allowedRoles = (to.meta.roles as UserRole[] | undefined) ?? undefined
  if (allowedRoles) {
    if (!role || !allowedRoles.includes(role as UserRole)) {
      console.error('[Router Guard] Forbidden! User role:', role, 'not allowed for:', to.path, '(Allowed:', allowedRoles, ')')
      return '/403'
    }
  }

  return true
})
