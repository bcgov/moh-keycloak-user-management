<template>
  <div>
    <v-alert id="primary-alert" v-model="active" :type="type" dismissible>{{ message }}</v-alert>
  </div>
</template>

<script>
import { mapState } from "vuex";

export default {
  computed: {
    ...mapState("alert", {
      message: state => state.message,
      type: state => state.type
    }),
    active: {
      get() {
        return this.$store.state.alert.active;
      },
      set() {
        this.$store.commit("alert/dismissAlert");
      }
    }
  }
};
</script>