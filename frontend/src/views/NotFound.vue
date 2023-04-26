<template>
  <div>
    <div id="error-container">
      <img
        src="../assets/images/notFound.png"
        width="80"
        class="logo"
        alt="404"
        style="margin-right: 1em"
      />
      <h3>Not Found</h3>
    </div>
    The requested page could not be found. Click
    <router-link :to="{ name: 'UserSearch' }">here</router-link>
    to return home.
  </div>
</template>

<style>
  #error-container {
    display: flex;
    align-items: center;
  }
</style>
