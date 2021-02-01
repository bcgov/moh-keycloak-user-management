package ca.bc.gov.hlth.mohums.util;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.function.Predicate;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.SpelEvaluationException;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.util.CollectionUtils;

public final class FilterUserByOrgId implements Predicate<Object> {

    private static final int ABBREVIATED_USER_ENTITY_LENGTH = 64;
    
    private static final Logger LOGGER = LoggerFactory.getLogger(FilterUserByOrgId.class);

    private static final Expression EXPRESSION = new SpelExpressionParser()
                .parseExpression("#this['attributes']['org_details']");

    private final String orgId;

    public FilterUserByOrgId(String id) {
        this.orgId = id;
    }
    
    @Override
    public String toString() {
        return String.format("%s(%s)", getClass().getSimpleName(), orgId);
    }
    
    @Override
    public boolean test(Object userEntity) {
        EvaluationContext context = new StandardEvaluationContext(userEntity);
        Collection<?> orgDetails = Collections.emptyList();

        try {
            orgDetails = EXPRESSION.getValue(context, Collection.class);
        } catch (SpelEvaluationException e) {
            // This exception is thrown when the evaluated user entity
            // does not contain the nested entries identified by EXPRESSION.
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Unable to filter \"{}\" by Org. ID {}: {}.",
                        abbreviateEntity(userEntity), orgId, e.toString());
            }
        }

        return !CollectionUtils.isEmpty(orgDetails) && orgDetails
                .stream()
                .map(FilterUserByOrgId::extractOrgId)
                .anyMatch(id -> StringUtils.equalsIgnoreCase(orgId, id));
    }
    
    static String extractOrgId(Object orgDetails) {
        String organizationDetails = Objects.toString(orgDetails, "{}");
        JSONParser jsonParser = new JSONParser(JSONParser.DEFAULT_PERMISSIVE_MODE);
        String id = StringUtils.EMPTY; // Default value.

        try {
            id = ((JSONObject) jsonParser.parse(organizationDetails)).getOrDefault("id", id).toString();
        } catch (ParseException | RuntimeException e) {
            // These exceptions are thrown when the value found by EXPRESSION
            // is not a valid JSON object. We can't stop people from creating
            // 'org_details' attributes that don't contain JSON data.
            LOGGER.warn("Unable to parse: \"{}\": {}.", organizationDetails, e.toString());
        }

        return id;
    }

    private static String abbreviateEntity(Object userEntity) {
        return StringUtils.abbreviate(Objects.toString(userEntity), ABBREVIATED_USER_ENTITY_LENGTH);
    }
    
}
