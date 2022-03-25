import Vue from 'vue'
import VueRouter from 'vue-router'
import Users from '../views/Users.vue'
import UserSearch from '../components/UserSearch.vue'
import UserUpdate from '../components/UserUpdate.vue'
import UserCreate from '../components/UserCreate.vue'

Vue.use(VueRouter)

const routes = [
  {path: '/', redirect: '/users'},
  {
    path: '/users',
    component: Users,
    children: [
      {
        path: '',
        component: UserSearch,
        name: 'UserSearch'
      },
      {
        path: 'create',
        component: UserCreate,
        name: 'UserCreate'
      },
      {
        path: ':userid',
        component: UserUpdate,
        name: 'UserUpdate'
      }    
    ]
  },
]

const router = new VueRouter({
  routes
})

export default router
