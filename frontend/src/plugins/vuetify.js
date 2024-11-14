import { createVuetify } from "vuetify";
import * as components from "vuetify/components";
import * as directives from "vuetify/directives";
import "vuetify/styles";

export default createVuetify({
  components,
  directives,
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
