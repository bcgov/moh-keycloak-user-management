import { createRouter, createWebHashHistory } from "vue-router";
import Dashboard from "../components/Dashboard.vue";
import GroupReport from "../components/GroupReport.vue";
import OrganizationsCreate from "../components/OrganizationsCreate.vue";
import OrganizationsSearch from "../components/OrganizationsSearch.vue";
import UserCreate from "../components/UserCreate.vue";
import UserSearch from "../components/UserSearch.vue";
import UserUpdate from "../components/UserUpdate.vue";
import keycloak from "../keycloak";
import AccessDenied from "../views/AccessDenied.vue";
import NotFound from "../views/NotFound.vue";
import Organizations from "../views/Organizations.vue";
import Users from "../views/Users.vue";

let initialLoad = true;

const routes = [
  { path: "/", redirect: "/users" },
  {
    path: "/users",
    component: Users,
    children: [
      {
        path: "",
        component: UserSearch,
        name: "UserSearch",
        meta: {
          requiredRole: ["view-users", "view-clients", "view-groups"],
        },
      },
      {
        path: "create",
        component: UserCreate,
        name: "UserCreate",
        meta: {
          requiredRole: ["create-user"],
        },
      },
      {
        path: ":userid",
        component: UserUpdate,
        name: "UserUpdate",
        meta: {
          requiredRole: ["view-users", "view-clients", "view-groups"],
        },
      },
    ],
  },
  {
    path: "/organizations",
    component: Organizations,
    children: [
      {
        path: "",
        component: OrganizationsSearch,
        name: "OrganizationsSearch",
        meta: {
          requiredRole: ["manage-org"],
        },
      },
      {
        path: "create",
        component: OrganizationsCreate,
        name: "OrganizationsCreate",
        meta: {
          requiredRole: ["manage-org"],
        },
      },
    ],
  },
  {
    path: "/dashboard",
    component: Dashboard,
    name: "Dashboard",
    meta: {
      requiredRole: ["view-metrics"],
    },
  },
  {
    path: "/groupreport",
    component: GroupReport,
    name: "GroupReport",
    meta: {
      requiredRole: ["view-users", "view-groups"],
    },
  },
  {
    path: "/unauthorized",
    component: AccessDenied,
    name: "AccessDenied",
  },
  {
    path: "/:catchAll(.*)",
    component: NotFound,
    name: "NotFound",
  },
];

const router = createRouter({
  history: createWebHashHistory(),
  routes,
});

const checkAccess = (requiredRoles) => {
  return !!requiredRoles.every((r) =>
    keycloak.tokenParsed.resource_access?.[
      "USER-MANAGEMENT-SERVICE"
    ]?.roles.includes(r)
  );
};

router.beforeEach((to, from, next) => {
  //workaround for Keycloak response fragment not being removed from Vue router
  //removes the "state", "session_state", "code" from path and redirects to the same route
  //https://github.com/keycloak/keycloak/issues/14742
  if (initialLoad && to.path) {
    const keycloakResponseParams = ["state", "session_state", "code"];
    const params = to.path;
    const paramsArray = params.split("&");

    const cleanedParamsArray = paramsArray.filter(
      (param) =>
        !keycloakResponseParams.some((key) => param.startsWith(key + "="))
    );

    const cleanedPath = cleanedParamsArray.join("&");
    initialLoad = false;
    next({ path: cleanedPath, replace: true });
    return;
  }

  if (to.meta?.requiredRole) {
    if (!checkAccess(to.meta?.requiredRole) && to.name !== "AccessDenied") {
      next({ name: "AccessDenied" });
      return;
    } else {
      next();
      return;
    }
  }
  next();
  return;
});

export default router;
