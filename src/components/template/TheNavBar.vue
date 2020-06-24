<template>
  <nav role="navigation">
    <div class="container">
      <ul>
        <li id="users-link" :class="($route.name == 'UserSearch' || $route.name == 'UserUpdate' || $route.name == 'UserCreate') ? 'active' : 'inactive'">
            <router-link @click.native="resetAlert" :to="{ name: 'UserSearch'}"> Users</router-link>   
        </li>
        <li id="event-log-link" :class="$route.name == 'EventLog' ? 'active' : 'inactive'">
            <router-link @click.native="resetAlert" :to="{ name: 'EventLog'}">Event Log</router-link>
        </li>
      </ul>
    </div>
  </nav>
</template>

<script>
export default {
  name: 'TheNavBar',
  methods: {
    resetAlert: function () {
      this.$store.commit("alert/dismissAlert");
    }
  }
}
</script>

<style scoped>
/* Main Navigation */
nav {
    height: 40px;
    background-color: #38598A;
}
nav .container {
    max-width: 1320px;
    min-width: 1100px;
    margin: 0 auto;
    padding: 0 60px;
}
nav .container ul {
  padding: 0;
}
nav .container ul li {
    color: #FFFFFF;
    display: inline-block;
    height: 40px;
    line-height: 40px;
    vertical-align: top;
    font-weight: bold;
}
nav .container ul li.active {
    background-color: #496DA2;
}
nav .container ul li:first-child {
    margin-left: -20px;
}
nav .container ul li:hover {
    cursor: pointer;
    background-color: #496DA2;
}
nav .container ul li a {
    display: block;
    font-size: 1rem;
    line-height: 40px;
    color: #FFFFFF;
    text-decoration: none;
    height: 40px;
    padding: 0 20px;
}
nav .container ul li a:focus {
    background-color: #496DA2;
}
</style>