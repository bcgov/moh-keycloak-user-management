<template>
<div>
    <div id="error-container">
        <img src="../assets/images/accessDenied.png" width="80" class="logo" alt="403" style="margin-right: 1em;"/>
        <h3>Access Denied</h3>
    </div>
    You don't have permissions to view this page. Click <router-link :to="{ name: 'UserSearch'}">here</router-link> to return home.
</div>
</template>

<style>
    #error-container{
        display: flex;
        align-items: center;
    }
</style>