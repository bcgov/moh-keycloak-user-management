const getDefaultState = () => {
    return {
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

const state = getDefaultState()

const mutations = {
    resetState(state) {
        Object.assign(state, getDefaultState())
    },
    setUser(state, user) {
        state.username = user.username
        state.firstName = user.firstName
        state.lastName = user.lastName
        state.email = user.email
        state.enabled = user.enabled
        state.attributes.phone = user.phone
        state.attributes.org_details = user.org_details
        state.attributes.lockout_reason = user.lockout_reason
        state.attributes.revoked = user.revoked
    },
    setUsername(state, username) {
        state.username = username
    },
    setFirstName(state, firstName) {
        state.firstName = firstName
    },
    setLastName(state, lastName) {
        state.lastName = lastName
    },
    setEmail(state, email) {
        state.email = email
    },
    setEnabled(state, enabled) {
        state.enabled = enabled
    },
    setPhone(state, phone) {
        state.attributes.phone = phone
    },
    setOrgDetails(state, org_details) {
        state.attributes.org_details = org_details
    },
    setLockoutReason(state, lockout_reason) {
        state.attributes.lockout_reason = lockout_reason
    },
    setRevoked(state, revoked) {
        state.attributes.revoked = revoked
    }
}

export default {
    namespaced: true,
    state,
    mutations
}