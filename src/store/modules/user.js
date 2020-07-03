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
            revoked: '',
            access_team_notes: ''
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
        if (user.attributes) {
            state.attributes.phone = user.attributes.phone
            state.attributes.org_details = user.attributes.org_details
            state.attributes.lockout_reason = user.attributes.lockout_reason
            state.attributes.revoked = user.attributes.revoked
            state.attributes.access_team_notes = user.attributes.access_team_notes
        }  
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
    },
    setAccessTeamNotes(state, access_team_notes) {
        state.attributes.access_team_notes = access_team_notes
    }
}

export default {
    namespaced: true,
    state,
    mutations
}