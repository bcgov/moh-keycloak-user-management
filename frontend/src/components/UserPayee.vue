<template>
  <v-card class="payee-card" v-if="payeeLoaded">
    <v-card-title>
      <span class="text-h5">Edit User Payee</span>
    </v-card-title>
    <v-card-text>
      <label dense for="payee">Payee</label>
      <v-text-field
        class="payee-text-field"
        density="compact"
        variant="outlined"
        id="payee"
        maxlength="10"
        v-model.trim="payee"
      />
    </v-card-text>

    <v-card-actions>
      <v-btn class="bg-primary" @click="save()">Save Payee</v-btn>
      <v-btn variant="outlined" class="text-primary" @click="close()">Cancel</v-btn>
    </v-card-actions>
  </v-card>
  <v-card class="payee-card payee-error" v-else>Error loading Payee</v-card>
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
      payeeLoaded: {
        type: Boolean,
        required: true,
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
  .payee-error {
    color: #ff5252;
    font-weight: bold;
    padding: 16px 24px 16px 24px;
  }
  .payee-text-field {
    width: 200px;
  }
</style>
