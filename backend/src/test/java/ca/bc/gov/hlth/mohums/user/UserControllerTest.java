package ca.bc.gov.hlth.mohums.user;

import ca.bc.gov.hlth.mohums.controller.UsersController;
import ca.bc.gov.hlth.mohums.validator.PermissionsValidator;
import ca.bc.gov.hlth.mohums.webclient.KeycloakApiService;
import ca.bc.gov.hlth.mohums.webclient.PayeeApiService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

@AutoConfigureJsonTesters
@WebMvcTest(UsersController.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private UserService userService;

    //Below beans are mocked so the test context is loaded
    @MockBean
    private KeycloakApiService keycloakApiService;
    @MockBean
    private PayeeApiService payeeApiService;
    @MockBean
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    @MockBean
    private PermissionsValidator permissionsValidator;

    // This object will be initialized thanks to @AutoConfigureJsonTesters
    @Autowired
    private JacksonTester<UserDTO> jsonTester;

    @Test
    public void getUserById() throws Exception {
        Mockito.when(userService.getUserByID("existing-id")).thenReturn(Optional.of(new UserDTO()));

        MockHttpServletResponse response = mvc.perform(MockMvcRequestBuilders.get("/user/existing-id"))
                .andReturn().getResponse();

        assertEquals((HttpStatus.OK.value()), response.getStatus());
        assertEquals(jsonTester.write(new UserDTO()).getJson(), response.getContentAsString());
    }

    @Test
    public void getUserByIdNotFound() throws Exception {
        Mockito.when(userService.getUserByID("non-existing-id")).thenReturn(Optional.empty());

        MockHttpServletResponse response = mvc.perform(MockMvcRequestBuilders.get("/user/non-existing-id"))
                .andReturn().getResponse();

        assertEquals((HttpStatus.NOT_FOUND.value()), response.getStatus());
        assertEquals("{\"httpStatus\":\"NOT_FOUND\",\"error\":\"User not found\"}", response.getContentAsString());
    }
}
