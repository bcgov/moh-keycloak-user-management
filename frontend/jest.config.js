module.exports = {
  preset: "@vue/cli-plugin-unit-jest",
  setupFiles: ["<rootDir>/tests/unit/globalSetup.js"],
  moduleNameMapper: {
    "\\.(jpg|jpeg|png|gif|eot|otf|webp|svg|ttf|woff|woff2|mp4|webm|wav|mp3|m4a|aac|oga)$":
      "<rootDir>/tests/__mocks__/fileMock.js",
    "\\.(css|sass)$": "<rootDir>/tests/__mocks__/styleMock.js",
  },
  transformIgnorePatterns: ["node_modules/(?!vue-router|@babel|vuetify)"],
  transform: {
    ".*\\.(vue)$": "vue-jest",
    "^.+\\.vue$": "vue-jest",
  },
};
