package ca.bc.gov.hlth.mohums.util;

import java.util.Collections;
import java.util.Map;
import java.util.function.Predicate;
import org.assertj.core.api.Assertions;
import org.assertj.core.util.Maps;
import org.junit.jupiter.api.Test;

public class FilterUserByOrgIdTest {
    
    private static final String ORG_ID = "orgId";

    private final Predicate<Object> filter = new FilterUserByOrgId(ORG_ID);

    @Test
    public void testToStringUsingNullId() {
        Assertions.assertThat(new FilterUserByOrgId(null))
                .hasToString("FilterUserByOrgId(null)");
    }

    @Test
    public void testToStringUsingId() {
        Assertions.assertThat(filter)
                .hasToString("FilterUserByOrgId(" + ORG_ID + ")");
    }
    
    @Test
    public void givenNull_WhenTest_ThenReturnsFalse() {
        Assertions.assertThat(filter.test(null)).isFalse();
    }
    
    @Test
    public void givenMissingAttributes_WhenTest_ThenReturnsFalse() {
        Assertions.assertThat(filter.test(Collections.emptyMap())).isFalse();
    }
    
    @Test
    public void givenMissingOrgDetails_WhenTest_ThenReturnsFalse() {
        Map<String, Object> userEntity = Maps.newHashMap("attributes", Collections.emptyMap());

        Assertions.assertThat(filter.test(userEntity)).isFalse();
    }
    
    @Test
    public void givenMismatchingOrgId_WhenTest_ThenReturnsFalse() {
        String orgDetails = "{\"id\":\"MismatchingOrgId\"}";
        Map<String, Object> attributes = Maps.newHashMap("org_details", orgDetails);
        Map<String, Object> userEntity = Maps.newHashMap("attributes", attributes);

        Assertions.assertThat(filter.test(userEntity)).isFalse();
    }
    
    @Test
    public void givenMatchingOrgId_WhenTest_ThenReturnsTrue() {
        String orgDetails = "{\"id\":\""
                + ORG_ID.toUpperCase() // Case Insensitive!
                + "\"}";
        Map<String, Object> attributes = Maps.newHashMap("org_details", orgDetails);
        Map<String, Object> userEntity = Maps.newHashMap("attributes", attributes);

        Assertions.assertThat(filter.test(userEntity)).isTrue();
    }

    @Test
    public void givenNull_WhenExtractOrgId_ThenReturnsEmpty() {
        Assertions.assertThat(FilterUserByOrgId.extractOrgId(null))
                .isEmpty();
    }

    @Test
    public void givenBlank_WhenExtractOrgId_ThenReturnsEmpty() {
        Assertions.assertThat(FilterUserByOrgId.extractOrgId(" "))
                .isEmpty();
    }

    @Test
    public void givenEmptyJSONObject_WhenExtractOrgId_ThenReturnsEmpty() {
        Assertions.assertThat(FilterUserByOrgId.extractOrgId("{}"))
                .isEmpty();
    }

    @Test
    public void givenInvalidJSON_WhenExtractOrgId_ThenReturnsEmpty() {
        Assertions.assertThat(FilterUserByOrgId.extractOrgId("1234567"))
                .isEmpty();
    }

    @Test
    public void givenNoId_WhenExtractOrgId_ThenReturnsEmpty() {
        Assertions.assertThat(FilterUserByOrgId.extractOrgId("{\"name\":\"Org. Name\"}"))
                .isEmpty();
    }

    @Test
    public void givenId_WhenExtractOrgId_ThenReturnsEmpty() {
        Assertions.assertThat(FilterUserByOrgId.extractOrgId("{\"id\":\"OrgID\"}"))
                .isEqualTo("OrgID");
    }
    
}
