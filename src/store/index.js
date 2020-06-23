import Vue from 'vue'
import Vuex from 'vuex'

Vue.use(Vuex)

const getDefaultState = () => {
  return {
    user: {
      username: '',
      firstName: '',
      lastName: '',
      email: '',
      enabled: true,
      attributes: {
        phone: '',
        org_details: '',
        lockout_reason: '',
        revoked: ''
      }
    }
  }
}

const state = getDefaultState()

const mutations = {
  resetState (state) {
    Object.assign(state, getDefaultState())
  },
  setUser (state, user) {
    state.user = user
  },
  setUsername (state, username) {
    state.user.username = username
  },
  setFirstName (state, firstName) {
    state.user.firstName = firstName
  },
  setLastName (state, lastName) {
    state.user.lastName = lastName
  },
  setEmail (state, email) {
    state.user.email = email
  },
  setEnabled (state, enabled) {
    state.user.enabled = enabled
  },
  setPhone (state, phone) {
    state.user.attributes.phone = phone
  },
  setOrgDetails (state, org_details) {
    state.user.attributes.org_details = org_details
  },
  setLockoutReason (state, lockout_reason) {
    state.user.attributes.lockout_reason = lockout_reason
  },
  setRevoked (state, revoked) {
    state.user.attributes.revoked = revoked
  }
}

export default new Vuex.Store({
  state,
  mutations
})
