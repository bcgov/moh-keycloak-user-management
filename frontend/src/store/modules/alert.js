const getDefaultState = () => {
  return {
    message: "",
    type: "success",
    active: false,
  };
};
const state = getDefaultState();

const mutations = {
  setAlert(state, alert) {
    state.message = alert.message;
    state.type = alert.type;
    state.active = true;
  },
  dismissAlert(state) {
    state.active = false;
  },
};

export default {
  namespaced: true,
  state,
  mutations,
};
