import { createStore } from "vuex";
import alert from "./modules/alert";
import user from "./modules/user";

export default createStore({
  modules: {
    user,
    alert,
  },
});
