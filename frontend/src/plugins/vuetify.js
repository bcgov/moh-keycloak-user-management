import Vue from "vue";
import Vuetify from "vuetify/lib";

Vue.use(Vuetify);

export default new Vuetify({
  theme: {
    themes: {
      light: {
        primary: "#003366",
        secondary: "#38598a",
        textLink: "#1A5A96",
        red: "#D8292F",
        success: "#2E8540",
      },
    },
  },
});
