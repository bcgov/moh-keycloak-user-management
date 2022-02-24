import Vue from 'vue'
import VueRouter from 'vue-router'
import Users from '../views/Users.vue'
import UserSearch from '../components/UserSearch.vue'
import UserUpdate from '../components/UserUpdate.vue'
import UserCreate from '../components/UserCreate.vue'

import Organizations from '../views/Organizations.vue'
import OrganizationSearch from '../components/OrganizationSearch.vue'
import OrganizationCreate from '../components/OrganizationCreate.vue'

import EventLog from '../views/EventLog.vue'
import AdminEventLog from '../views/AdminEventLog.vue'

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
  },{
    path: '/organizations',
    component: Organizations,
    children: [
      {
        path: '',
        component: OrganizationSearch,
        name: 'OrganizationSearch'
      },
      {
        path: 'create',
        component: OrganizationCreate,
        name: 'OrganizationCreate'
      },
    ]
  },
  {
    path: '/event-log',
    name: 'EventLog',
    component: EventLog
  },
  {
    path: '/admin-event-log',
    name: 'AdminEventLog',
    component: AdminEventLog
  }
]

const router = new VueRouter({
  routes
})

export default router
