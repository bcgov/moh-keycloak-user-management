import Vue from 'vue'
import VueRouter from 'vue-router'
import Users from '../views/Users.vue'
import UserSearch from '../components/UserSearch.vue'
import UserUpdate from '../components/UserUpdate.vue'
import UserCreate from '../components/UserCreate.vue'
import Dashboard from '../components/Dashboard.vue'
import Organizations from '../views/Organizations.vue'
import OrganizationsSearch from '../components/OrganizationsSearch.vue'
import OrganizationsCreate from '../components/OrganizationsCreate.vue'
import NotFound from  '../views/NotFound.vue'
import AccessDenied from  '../views/AccessDenied.vue'
import keycloak from '../keycloak';

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
        name: 'UserSearch',
        meta: {
          requiredRole: ['view-users', 'view-clients', 'view-groups']
        }
      },
      {
        path: 'create',
        component: UserCreate,
        name: 'UserCreate',
        meta: {
          requiredRole: ['create-user']
        }
      },
      {
        path: ':userid',
        component: UserUpdate,
        name: 'UserUpdate',
        meta: {
          requiredRole: ['view-users', 'view-clients', 'view-groups']
        }
      }
    ]
  },
  {
    path: '/organizations',
    component: Organizations,
    children: [
      {
        path: '',
        component: OrganizationsSearch,
        name: 'OrganizationsSearch',
        meta: {
          requiredRole: ['manage-org']
        }
      },
      {
        path: 'create',
        component: OrganizationsCreate,
        name: 'OrganizationsCreate',
        meta: {
          requiredRole: ['manage-org']
        }
      },
    ]
  },
  {
    path: '/dashboard',
    component: Dashboard,
    name: 'Dashboard',
    meta: {
      requiredRole: ['view-metrics']
    }
  },
  {
    path: '/unauthorized',
    component: AccessDenied,
    name: 'AccessDenied'
  },
  {
    path: '/:catchAll(.*)',
    component: NotFound,
    name: 'NotFound'
  }
]

const router = new VueRouter({
  routes
});

const checkAccess = (requiredRoles) => {
  return !!requiredRoles.every(r => keycloak.tokenParsed.resource_access?.['USER-MANAGEMENT-SERVICE']?.roles.includes(r))
}

router.beforeEach((to, from, next) => {
  if(to.meta?.requiredRole){
    if(!checkAccess(to.meta?.requiredRole) && to.name !== 'AccessDenied'){
      next({name: 'AccessDenied'})
    } 
    else {
      next();
    }
  }
  next();
})

export default router
