package ca.bc.gov.hlth.mohums.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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

/**
 * {@code FilterUserByLastLogEvent} returns true if any of the event provided in the constructor matches
 * the user ID extracted from the tested {@code Object} instances. The tested {@code Object}
 * must be a {@code Map} instance containing keys {@code attributes} &gt; {@code org_details}, where
 * the value of {@code org_details} entry must be a JSON object containing something like
 * {@code {"id":"00001763","name":"Interior Health Authority"}}. If the ID given in the constructor
 * matches the ID in the JSON, {@code FilterUserByOrgId} returns {@literal true}, otherwise it
 * returns {@literal false}. If the {@code Object} or JSON object structure is unexpected, it
 * returns {@literal false}.
 */
public final class FilterUserByLastLogEvent implements Predicate<Object> {

    //TODO do we need this abbreviate here?
    private static final int ABBREVIATED_USER_ENTITY_LENGTH = 64;

    private static final Logger LOGGER = LoggerFactory.getLogger(FilterUserByLastLogEvent.class);

    // We search for the userId attribute in the events
    private static final Expression EXPRESSION = new SpelExpressionParser()
            .parseExpression("#this['attributes']['userId']");

    private final List events;

    public FilterUserByLastLogEvent(List<Object> events) {
        this.events = events;
    }

    @Override
    public String toString() {
        return String.format("%s(%s)", getClass().getSimpleName(), "list of" +events.size()+ "events");
    }


    @Override
    public boolean test(Object eventListEntity) {
        
        String userId="";
        List <Object> eventList = (ArrayList) eventListEntity;
        
        List <Collection> detailsList = new ArrayList<>();
        eventList.stream().map(eventEntity -> {
            EvaluationContext context = new StandardEvaluationContext(eventEntity);
            Collection<?> eventDetails = null;
            try {
                eventDetails = EXPRESSION.getValue(context, Collection.class);
            } catch (SpelEvaluationException e) {
                // This exception is thrown when the evaluated EVENT entity
                // does not contain the nested entries identified by EXPRESSION.
                LOGGER.debug("Unable to filter \"{}\" by User. ID {}: {}.",
                        abbreviateEntity(eventEntity), userId, e);
            }
            return eventDetails;            
        }).forEachOrdered(eventDetails -> {
            detailsList.add(eventDetails);
        });
        
        
                // TODO different logic, need to stream the list of event
//        List <Collection> detailsList = new ArrayList<>();
        
//        events.stream().map(eventEntity -> {
//            EvaluationContext context = new StandardEvaluationContext(eventEntity);
//            Collection<?> eventDetails = null;
//            try {
//                eventDetails = EXPRESSION.getValue(context, Collection.class);
//            } catch (SpelEvaluationException e) {
//                // This exception is thrown when the evaluated EVENT entity
//                // does not contain the nested entries identified by EXPRESSION.
//                LOGGER.debug("Unable to filter \"{}\" by User. ID {}: {}.",
//                        abbreviateEntity(eventDetails), userId, e);
//            }
//            return eventDetails;            
//        }).forEachOrdered(eventDetails -> {
//            detailsList.add(eventDetails);
//        });
        
        
        // TODO NOT OK need to review logic
        return !detailsList.isEmpty() && detailsList
                .stream()
                .map(FilterUserByLastLogEvent::extractUserId)
                .anyMatch(id -> StringUtils.equalsIgnoreCase(userId, id));
    }

    /**
     * @param event a JSON object that should contain an "userId" attribute
     * @return the value of the "userId" attribute from the supplied JSON object.
     */
    public static String extractUserIdForEvent(Object eventDetails) {
        String eventJson = Objects.toString(eventDetails, "{}");
        JSONParser jsonParser = new JSONParser(JSONParser.DEFAULT_PERMISSIVE_MODE);
        String userId = StringUtils.EMPTY; // Default value.

        try {
            userId = ((JSONObject) jsonParser.parse(eventJson))
                    .getOrDefault("userId", userId).toString();
        } catch (ParseException | ClassCastException e) {
            // These exceptions are thrown when the value found by EXPRESSION
            // is not a valid JSON object. We can't stop people from creating
            // 'org_details' attributes that don't contain JSON data.
            LOGGER.warn("Unable to parse: \"{}\": {}.", eventJson, e);
        }

        return userId;
    }

    /**
     * @param user a JSON object that should contain an "id" attribute
     * @return the value of the "id" attribute from the supplied JSON object.
     */
    public static String extractUserId(Object user) {
        String userJson = Objects.toString(user, "{}");
        JSONParser jsonParser = new JSONParser(JSONParser.DEFAULT_PERMISSIVE_MODE);
        String id = StringUtils.EMPTY; // Default value.

        try {
            id = ((JSONObject) jsonParser.parse(userJson))
                    .getOrDefault("id", id).toString();
        } catch (ParseException | ClassCastException e) {
            // These exceptions are thrown when the value found by EXPRESSION
            // is not a valid JSON object. We can't stop people from creating
            // 'org_details' attributes that don't contain JSON data.
            LOGGER.warn("Unable to parse: \"{}\": {}.", userJson, e);
        }

        return id;
    }
    
    private static String abbreviateEntity(Object eventEntity) {
        return StringUtils.abbreviate(Objects.toString(eventEntity), ABBREVIATED_USER_ENTITY_LENGTH);
    }

}
