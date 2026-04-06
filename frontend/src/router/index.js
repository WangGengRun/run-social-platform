import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      name: 'home',
      component: () => import('../views/FeedView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/login',
      name: 'login',
      component: () => import('../views/LoginView.vue'),
      meta: { requiresAuth: false }
    },
    {
      path: '/register',
      name: 'register',
      component: () => import('../views/RegisterView.vue'),
      meta: { requiresAuth: false }
    },
    {
      path: '/feed',
      name: 'feed',
      component: () => import('../views/FeedView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/post/:id',
      name: 'postDetail',
      component: () => import('../views/PostDetail.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/profile/:id',
      name: 'profile',
      component: () => import('../views/ProfilePage.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/post/publish',
      name: 'postPublish',
      component: () => import('../views/PostPublish.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/profile/edit',
      name: 'profileEdit',
      component: () => import('../views/ProfileEdit.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/activity/create',
      name: 'activityCreate',
      component: () => import('../views/ActivityCreate.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/activity',
      name: 'activityList',
      component: () => import('../views/ActivityList.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/activity/my',
      name: 'activityMy',
      component: () => import('../views/ActivityMy.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/activity/:id',
      name: 'activityDetail',
      component: () => import('../views/ActivityDetail.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/activity/:id/participants',
      name: 'activityParticipants',
      component: () => import('../views/ActivityParticipants.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/message',
      name: 'conversations',
      component: () => import('../views/Conversations.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/message/detail',
      name: 'messageDetail',
      component: () => import('../views/MessageDetail.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/admin',
      name: 'Admin',
      component: () => import('../views/admin/AdminLayout.vue'),
      meta: { requiresAuth: true, requiresAdmin: true },
      children: [
        {
          path: 'dashboard',
          name: 'Dashboard',
          component: () => import('../views/admin/Dashboard.vue')
        },
        {
          path: 'users',
          name: 'UserManagement',
          component: () => import('../views/admin/UserManagement.vue')
        },
        {
          path: 'alumni-verify',
          name: 'AlumniVerifyAudit',
          component: () => import('../views/admin/AlumniVerifyAudit.vue')
        },
        {
          path: 'content',
          name: 'ContentReview',
          component: () => import('../views/admin/PostAudit.vue')
        },
        {
          path: 'comments',
          name: 'CommentReview',
          component: () => import('../views/admin/CommentAudit.vue')
        },
        {
          path: 'activities',
          name: 'ActivityManagement',
          component: () => import('../views/admin/ActivityManagement.vue')
        }
      ]
    },
    {
      path: '/:pathMatch(.*)*',
      redirect: '/'
    }
  ]
})

// 路由守卫
router.beforeEach((to, from, next) => {
  console.log('路由守卫触发:', {
    to: to.path,
    from: from.path,
    requiresAuth: to.matched.some(record => record.meta.requiresAuth),
    requiresAdmin: to.matched.some(record => record.meta.requiresAdmin),
    hasToken: !!localStorage.getItem('token')
  })
  
  const token = localStorage.getItem('token')
  const role = localStorage.getItem('role')
  const requiresAuth = to.matched.some(record => record.meta.requiresAuth)
  const requiresAdmin = to.matched.some(record => record.meta.requiresAdmin)
  const isLoggedIn = !!token
  const isAdmin = role === 'ADMIN'

  if (requiresAuth && !isLoggedIn) {
    console.log('未登录，跳转到登录页')
    next('/login')
  } else if (requiresAdmin && !isAdmin) {
    console.log('非管理员，跳转到首页')
    next('/')
  } else if (to.path === '/login' && isLoggedIn) {
    // 已登录用户访问登录页，重定向到首页
    console.log('已登录，跳转到首页')
    next('/')
  } else {
    console.log('允许跳转')
    next()
  }
})

export default router