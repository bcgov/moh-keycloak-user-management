<template>
  <div class="nav-tabs" style="width: 100%">
     <!--  <a v-on:click="$emit('searchTabClicked')" class="active-tab">Users</a> -->
  </div>
</template>

<script>
export default {
  name: 'TheSubNav'
}
</script>

<style scoped>
.nav-tabs {
    border-bottom: 4px solid #003366;
    margin-bottom: 20px;
    font-size: 15px;
}
.nav-tabs,
.v-application .nav-tabs a {
  color: #ffffff; 
}

.nav-tabs a {
  display: inline-block;
  padding: 0 20px;
  height: 36px;
  font-size: 16px;
  font-weight: bold;
  line-height: 40px;
}
.active-tab,
.active-tab:hover{
    background: #003366;
    margin-right:10px; 
    text-decoration: none;
    border-radius: 8px 8px 0 0;
}
.inactive-tab {
    background: #999999; 
    margin-right:10px;
    text-decoration: none;
    border-radius: 8px 8px 0 0;
}
.inactive-tab:hover {
    background: #666666;
    text-decoration: none;
}
</style>