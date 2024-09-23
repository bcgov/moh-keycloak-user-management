<template>
  <footer class="primary">
    <div class="container">
      <v-row no-gutters>
        <v-col class="col-3">
          <ul>
            <li>
              <a
                href="https://www2.gov.bc.ca/gov/content/home/get-help-with-government-services"
                target="_blank"
              >
                Contact Us
              </a>
            </li>
            <li>
              <a
                href="/umc-guide.pdf"
                download="HLTH Keycloak User Management Console User Guide.pdf"
              >
                User Guide
              </a>
            </li>
          </ul>
        </v-col>
        <v-col class="col-3">
          <ul>
            <li>
              <a
                href="http://www2.gov.bc.ca/gov/content/home/disclaimer"
                target="_blank"
              >
                Disclaimer
              </a>
            </li>
            <li>
              <a
                href="http://www2.gov.bc.ca/gov/content/home/privacy"
                target="_blank"
              >
                Privacy
              </a>
            </li>
            <li>
              <a
                href="http://www2.gov.bc.ca/gov/content/home/accessibility"
                target="_blank"
              >
                Accessibility
              </a>
            </li>
            <li>
              <a
                href="http://www2.gov.bc.ca/gov/content/home/copyright"
                target="_blank"
              >
                Copyright
              </a>
            </li>
          </ul>
        </v-col>
      </v-row>
    </div>
  </footer>
</template>

<script>
  export default {
    name: "TheFooter",
  };
</script>

<style scoped>
  footer {
    height: 160px;
    background-color: #003366;
  }
  footer .container {
    max-width: 1320px;
    min-width: 1100px;
    margin: 0 auto;
    padding: 20px 60px 0px 60px;
  }
  footer li {
    margin-bottom: 10px;
  }
  footer li,
  .v-application footer li a {
    font-size: 0.8125rem;
    color: #ffffff;
  }
  footer li a {
    text-decoration: none;
  }
  footer li a:hover,
  footer li a:focus {
    text-decoration: underline;
  }
  .v-application footer ul {
    padding: 0px;
  }
</style>
