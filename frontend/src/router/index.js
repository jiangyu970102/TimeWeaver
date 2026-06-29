import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/Login.vue'),
    meta: { requiresAuth: false },
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/login/Register.vue'),
    meta: { requiresAuth: false },
  },
  {
    path: '/',
    component: () => import('@/views/layout/Layout.vue'),
    meta: { requiresAuth: true },
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/Dashboard.vue'),
      },
      {
        path: 'records',
        name: 'Records',
        component: () => import('@/views/records/RecordList.vue'),
      },
      {
        path: 'stats',
        name: 'Stats',
        component: () => import('@/views/stats/StatsDashboard.vue'),
      },
      {
        path: 'pomodoro',
        name: 'Pomodoro',
        component: () => import('@/views/pomodoro/PomodoroView.vue'),
      },
      {
        path: 'calendar',
        name: 'Calendar',
        component: () => import('@/views/stats/CalendarView.vue'),
      },
      {
        path: 'yearly',
        name: 'YearlyReport',
        component: () => import('@/views/stats/YearlyReport.vue'),
      },
      {
        path: 'goals',
        name: 'Goals',
        component: () => import('@/views/goals/GoalList.vue'),
      },
      {
        path: 'ai-sessions',
        name: 'AiSessions',
        component: () => import('@/views/ai/AiSessionDashboard.vue'),
      },
      {
        path: 'git',
        name: 'GitSettings',
        component: () => import('@/views/git/GitSettings.vue'),
      },
      {
        path: 'templates',
        name: 'Templates',
        component: () => import('@/views/templates/TemplateList.vue'),
      },
      {
        path: 'reports',
        name: 'Reports',
        component: () => import('@/views/reports/WeeklyReport.vue'),
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/profile/Profile.vue'),
      },
    ],
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/error/NotFound.vue'),
    meta: { requiresAuth: false },
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

// Navigation guard
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  if (to.meta.requiresAuth && !token) {
    next('/login')
  } else {
    next()
  }
})

export default router
