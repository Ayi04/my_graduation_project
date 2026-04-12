import { createRouter, createWebHistory } from 'vue-router'
import Login from '../views/Login.vue'
import Home from '../views/Home.vue'
import SeatList from '../views/SeatList.vue'
import AreaDetail from '../views/AreaDetail.vue'
import ReservationList from '../views/ReservationList.vue'
import Profile from '../views/Profile.vue'
import Feedback from '../views/Feedback.vue'
import ComplaintManagement from '../views/ComplaintManagement.vue'
import UserManagement from '../views/UserManagement.vue'
import StaffManagement from '../views/StaffManagement.vue'
import Statistics from '../views/Statistics.vue'
import SystemManagement from '../views/SystemManagement.vue'
import StaffPanel from '../views/StaffPanel.vue'
import Rule from '../views/Rule.vue'
import RuleManagement from '../views/RuleManagement.vue'
import NoticeManagement from '../views/NoticeManagement.vue'
import NoticeDetail from '../views/NoticeDetail.vue'
import SeatManagement from '../views/SeatManagement.vue'
import ReservationManagement from '../views/ReservationManagement.vue'
import CheckinRecords from '../views/CheckinRecords.vue'
import CheckinManagement from '../views/CheckinManagement.vue'
import ViolationRecords from '../views/ViolationRecords.vue'
import ViolationManagement from '../views/ViolationManagement.vue'

const routes = [
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/login',
    name: 'Login',
    component: Login
  },
  {
    path: '/home',
    name: 'Home',
    component: Home,
    meta: { requiresAuth: true }
  },
  {
    path: '/seats',
    name: 'SeatList',
    component: SeatList,
    meta: { requiresAuth: true }
  },
  {
    path: '/area/:id',
    name: 'AreaDetail',
    component: AreaDetail,
    meta: { requiresAuth: true }
  },
  {
    path: '/reservations',
    name: 'ReservationList',
    component: ReservationList,
    meta: { requiresAuth: true }
  },
  {
    path: '/profile',
    name: 'Profile',
    component: Profile,
    meta: { requiresAuth: true }
  },
  {
    path: '/feedback',
    name: 'Feedback',
    component: Feedback,
    meta: { requiresAuth: true }
  },
  {
    path: '/complaint-management',
    name: 'ComplaintManagement',
    component: ComplaintManagement,
    meta: { requiresAuth: true, role: 'STAFF' }
  },
  {
    path: '/user-management',
    name: 'UserManagement',
    component: UserManagement,
    meta: { requiresAuth: true, role: 'ADMIN' }
  },
  {
    path: '/staff-management',
    name: 'StaffManagement',
    component: StaffManagement,
    meta: { requiresAuth: true, role: 'ADMIN' }
  },
  {
    path: '/statistics',
    name: 'Statistics',
    component: Statistics,
    meta: { requiresAuth: true, role: 'ADMIN' }
  },
  {
    path: '/system',
    name: 'SystemManagement',
    component: SystemManagement,
    meta: { requiresAuth: true, role: 'ADMIN' }
  },
  {
    path: '/staff',
    name: 'StaffPanel',
    component: StaffPanel,
    meta: { requiresAuth: true, role: 'STAFF' }
  },
  {
    path: '/rule',
    name: 'Rule',
    component: Rule,
    meta: { requiresAuth: true }
  },
  {
    path: '/rule-management',
    name: 'RuleManagement',
    component: RuleManagement,
    meta: { requiresAuth: true, role: 'ADMIN' }
  },
  {
    path: '/notice-management',
    name: 'NoticeManagement',
    component: NoticeManagement,
    meta: { requiresAuth: true, role: 'ADMIN' }
  },
  {
    path: '/seat-management',
    name: 'SeatManagement',
    component: SeatManagement,
    meta: { requiresAuth: true, role: 'STAFF' }
  },
  {
    path: '/reservation-management',
    name: 'ReservationManagement',
    component: ReservationManagement,
    meta: { requiresAuth: true, role: 'STAFF' }
  },
  {
    path: '/checkin-records',
    name: 'CheckinRecords',
    component: CheckinRecords,
    meta: { requiresAuth: true }
  },
  {
    path: '/checkin-management',
    name: 'CheckinManagement',
    component: CheckinManagement,
    meta: { requiresAuth: true, role: 'STAFF' }
  },
  {
    path: '/violation-records',
    name: 'ViolationRecords',
    component: ViolationRecords,
    meta: { requiresAuth: true }
  },
  {
    path: '/violation-management',
    name: 'ViolationManagement',
    component: ViolationManagement,
    meta: { requiresAuth: true, role: 'STAFF' }
  },
  {
    path: '/notice-detail/:id',
    name: 'NoticeDetail',
    component: NoticeDetail,
    meta: { requiresAuth: true }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const requiresAuth = to.matched.some(record => record.meta.requiresAuth)
  const currentUser = localStorage.getItem('user')
  
  if (requiresAuth && !currentUser) {
    next('/login')
  } else if (to.meta.role) {
    const user = JSON.parse(currentUser)
    if (user.role !== to.meta.role) {
      next('/home')
    } else {
      next()
    }
  } else {
    next()
  }
})

export default router