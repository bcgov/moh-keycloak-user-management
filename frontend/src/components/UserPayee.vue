<template>
  <v-card class="payee-card">
    <v-card-title>
      <span class="headline">Edit User Payee</span>
    </v-card-title>
    <v-card-text>
      <label dense for="payee">Payee</label>
      <v-text-field
        class="payee-text-field"
        dense
        outlined
        id="payee"
        maxlength="10"
        v-model.trim="payee"
      />
    </v-card-text>

    <v-card-actions>
      <v-btn class="primary" @click="save()">Save Payee</v-btn>
      <v-btn outlined class="primary--text" @click="close()">Cancel</v-btn>
    </v-card-actions>
  </v-card>
</template>

<script>
  export default {
    name: "UserPayee",
    components: {},
    props: {
      initialPayee: {
        type: String,
        required: false,
      },
    },
    data() {
      return {
        payee: this.initialPayee,
      };
    },
    methods: {
      close() {
        this.$emit("close");
      },
      save() {
        this.$emit("save", this.payee);
      },
    },
  };
</script>

<style scoped>
  .payee-card {
    padding: 15px;
  }
  .payee-text-field {
    width: 200px;
  }
</style>
