<template>
  <v-card>
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
        v-model="payee"
      />
    </v-card-text>

    <v-card-actions>
      <v-btn class="primary" @click="save()">Save Payee</v-btn>
      <v-btn outlined class="primary--text" @click="close()">Cancel</v-btn>
    </v-card-actions>
  </v-card>
</template>

<script>
  import UsersRepository from "@/api/UsersRepository";

  export default {
    name: "UserPayee",
    components: {},
    props: {
      userId: {
        type: String,
        required: false,
      },
    },
    async created() {
      console.log("created");
      let response = await UsersRepository.getUserPayee(this.userId);
      this.payee = response.data.payeeNumber;
    },
    data() {
      return {
        payee: "",
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
  .payee-text-field {
    width: 200px;
  }
</style>
