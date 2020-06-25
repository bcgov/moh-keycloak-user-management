import Vue from 'vue'
import Vuex from 'vuex'
import user from './modules/user'
import alert from './modules/alert'

Vue.use(Vuex)

export default new Vuex.Store({
  modules: {
    user,
    alert
  }
})
