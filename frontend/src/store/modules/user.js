const getDefaultState = () => {
  return {
    username: "",
    firstName: "",
    lastName: "",
    email: "",
    enabled: true,
    attributes: {
      phone: "",
      org_details: "",
      access_team_notes: "",
      sfds_auth_1: [],
      sfds_auth_2: [],
      sfds_auth_3: [],
      sfds_auth_4: [],
      sfds_auth_5: [],
    },
    federatedIdentities: [],
  };
};

const state = getDefaultState();

const mutations = {
  resetState(state) {
    Object.assign(state, getDefaultState());
  },
  setUser(state, user) {
    state.username = user.username;
    state.firstName = user.firstName;
    state.lastName = user.lastName;
    state.email = user.email;
    state.enabled = user.enabled;
    if (user.attributes) {
      //Copy over all attributes
      for (const property in user.attributes) {
        state.attributes[property] = user.attributes[property];
      }
    }
    state.federatedIdentities = user.federatedIdentities;
  },
  setUserDetails(state, user) {
    state.username = user.username;
    state.firstName = user.firstName;
    state.lastName = user.lastName;
    state.email = user.email;
    state.enabled = user.enabled;
    if (user.attributes) {
      //Copy over all attributes
      for (const property in user.attributes) {
        state.attributes[property] = user.attributes[property];
      }
    }
  },
  setUsername(state, username) {
    state.username = username;
  },
  setFirstName(state, firstName) {
    state.firstName = firstName;
  },
  setLastName(state, lastName) {
    state.lastName = lastName;
  },
  setEmail(state, email) {
    state.email = email;
  },
  setEnabled(state, enabled) {
    state.enabled = enabled;
  },
  setPhone(state, phone) {
    state.attributes.phone = phone;
  },
  setOrgDetails(state, org_details) {
    state.attributes.org_details = org_details;
  },
  setAccessTeamNotes(state, access_team_notes) {
    state.attributes.access_team_notes = access_team_notes;
  },
  setSfdsAuth1(state, sfds_auth_1) {
    state.attributes.sfds_auth_1 = sfds_auth_1;
  },
  setSfdsAuth2(state, sfds_auth_1) {
    state.attributes.sfds_auth_2 = sfds_auth_1;
  },
  setSfdsAuth3(state, sfds_auth_1) {
    state.attributes.sfds_auth_3 = sfds_auth_1;
  },
  setSfdsAuth4(state, sfds_auth_1) {
    state.attributes.sfds_auth_4 = sfds_auth_1;
  },
  setSfdsAuth5(state, sfds_auth_1) {
    state.attributes.sfds_auth_5 = sfds_auth_1;
  },
};

export default {
  namespaced: true,
  state,
  mutations,
};
