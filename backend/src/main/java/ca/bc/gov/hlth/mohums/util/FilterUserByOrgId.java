package ca.bc.gov.hlth.mohums.util;

import com.nimbusds.oauth2.sdk.util.CollectionUtils;
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
import org.springframework.expression.ExpressionException;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

/**
 *
 */
public final class FilterUserByOrgId implements Predicate<Object> {

    private static final int ABBREVIATED_USER_ENTITY_LENGTH = 64;
    
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthorizedClientsParser.class);

    private final String orgId;
    private final JSONParser jsonParser;
    private final Expression expression;

    public FilterUserByOrgId(final String id) {
        this.orgId = id;
        this.jsonParser = new JSONParser(JSONParser.DEFAULT_PERMISSIVE_MODE);
        this.expression = new SpelExpressionParser()
                .parseExpression("#this['attributes']['org_details']");
    }
    
    Logger getLogger() {
        return LOGGER;
    }
    
    @Override
    public boolean test(final Object userEntity) {
        final EvaluationContext context = new StandardEvaluationContext(userEntity);

        Collection<?> orgDetails = Collections.emptyList();

        try {
            orgDetails = expression.getValue(context, Collection.class);
        } catch (final ExpressionException e) {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Unable to filter \"{}\" by Org. ID {}: {}.",
                        abbreviateEntity(userEntity), orgId, e.toString());
            }
        }

        return CollectionUtils.isNotEmpty(orgDetails) && orgDetails
                .stream()
                .map(this::extractOrgId)
                .anyMatch(this.orgId::equalsIgnoreCase);
    }
    
    String extractOrgId(final Object orgDetails) {
        String id = StringUtils.EMPTY; // Default value.

        final String organizationDetails = Objects.toString(orgDetails, "{}");

        try {
            final JSONObject organization = (JSONObject) jsonParser.parse(organizationDetails);
            id = organization.getOrDefault("id", id).toString();
        } catch (final ParseException | RuntimeException e) {
            getLogger().warn("Unable to parse: \"{}\": {}.", organizationDetails, e.toString());
        }

        return id;
    }
    
    @Override
    public String toString() {
        return String.format("%s(%s)", getClass().getSimpleName(), this.orgId);
    }

    private static String abbreviateEntity(final Object userEntity) {
        return StringUtils.abbreviate(Objects.toString(userEntity), ABBREVIATED_USER_ENTITY_LENGTH);
    }
    
}
