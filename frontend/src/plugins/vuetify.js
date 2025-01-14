import "@/scss/variables.scss";
import { createVuetify } from "vuetify";
import * as components from "vuetify/components";
import * as directives from "vuetify/directives";
import "vuetify/styles";

const lightTheme = {
  dark: false,
  colors: {
    primary: "#003366",
    secondary: "#38598a",
    textLink: "#1A5A96",
    red: "#D8292F",
    success: "#2E8540",
    background: "#F1F1F1",
  },
};

export default createVuetify({
  components,
  directives,
  theme: {
    defaultTheme: "lightTheme",
    themes: {
      lightTheme,
    },
  },
});
