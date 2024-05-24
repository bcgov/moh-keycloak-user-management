package ca.bc.gov.hlth.mohums.userSearch;

import ca.bc.gov.hlth.mohums.userSearch.user.UserSearchParameters;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class UserSearchParametersTest {

    @ParameterizedTest
    @MethodSource("provideDataForUserSearchParam")
    @SuppressWarnings("unchecked")
    public void testIsSearchByLastLogOnly(List<Optional> constructorParameters, boolean expectedResult) {
        UserSearchParameters searchParams = new UserSearchParameters(constructorParameters.get(0), constructorParameters.get(1),
                constructorParameters.get(2), constructorParameters.get(3),
                constructorParameters.get(4), constructorParameters.get(5),
                constructorParameters.get(6), constructorParameters.get(7),
                constructorParameters.get(8), constructorParameters.get(9), constructorParameters.get(10));
        assertEquals(expectedResult, searchParams.isSearchByLastLogOnly());
    }

    private static Stream<Arguments> provideDataForUserSearchParam() {

        return Stream.of(
                Arguments.of(List.of(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty()), false),
                Arguments.of(List.of(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.of("01-01-1900"), Optional.empty()), true),
                Arguments.of(List.of(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.of("01-01-1900")), true),
                Arguments.of(List.of(Optional.empty(), Optional.of("email@example.com"), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty()), false),
                Arguments.of(List.of(Optional.empty(), Optional.empty(), Optional.of("FirstName"), Optional.of("LastName"), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty()), false),
                Arguments.of(List.of(Optional.empty(), Optional.of("email@example.com"), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.of("01-01-1900"), Optional.empty()), false)

                );
    }
}
