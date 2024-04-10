package ca.bc.gov.hlth.mohums.userSearch.user;

import ca.bc.gov.hlth.mohums.userSearch.event.EventService;
import ca.bc.gov.hlth.mohums.userSearch.role.RoleEntity;
import ca.bc.gov.hlth.mohums.userSearch.role.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final EventService eventService;
    private final UserSpecifications userSpecifications;

    @Autowired
    public UserService(UserRepository userRepository, RoleRepository roleRepository, EventService eventService, UserSpecifications userSpecifications) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.eventService = eventService;
        this.userSpecifications = userSpecifications;
    }

    public List<UserDTO> getUsers(UserSearchParameters userSearchParams) {

        Specification<UserEntity> userSpec = Specification.where(userSpecifications.fromMohApplicationsRealm())
                .and(userSpecifications.notServiceAccount());

        Map<String, Object> loginEventsMap = new HashMap<>();
        Map<String, String> roleIdNameMap = new HashMap<>();
        List<UserEntity> searchResults;


        if (userSearchParams.isSearchByLastLogOnly()) {
            loginEventsMap = getUsersLoginEvents(userSearchParams);
            searchResults = findAllUsersThatSatisfyLoginEventConstraint(loginEventsMap);
        } else {
            if (userSearchParams.isBasicSearch()) {
                userSpec = buildBasicSearchSpecification(userSpec, userSearchParams);
            } else {
                loginEventsMap = getUsersLoginEvents(userSearchParams);
                roleIdNameMap = getClientRoles(userSearchParams);
                userSpec = buildAdvancedSearchSpecification(userSpec, userSearchParams, roleIdNameMap);
            }
            searchResults = findAllUsersThatSatisfySpecification(userSpec, loginEventsMap);
        }

        boolean briefRepresentation = userSearchParams.getOrganizationId().isEmpty();

        return mapResultsToDTO(searchResults, roleIdNameMap, loginEventsMap, briefRepresentation);
    }

    private List<UserDTO> mapResultsToDTO(List<UserEntity> searchResults, Map<String, String> roleIdNameMap, Map<String, Object> loginEventsMap, boolean briefRepresentation) {
        return searchResults.stream().map(user -> UserDTOMapper.convertToDTO(user, roleIdNameMap, briefRepresentation, loginEventsMap.get(user.getId()))).collect(Collectors.toList());
    }

    private List<UserEntity> findAllUsersThatSatisfyLoginEventConstraint(Map<String, Object> loginEventsMap) {
        List<String> userIds = new ArrayList<>(loginEventsMap.keySet());
        return userRepository.findMohApplicationsUsersByIdList(userIds);
    }

    private List<UserEntity> findAllUsersThatSatisfySpecification(Specification<UserEntity> userSpec, Map<String, Object> loginEventsMap) {
        List<UserEntity> searchResults = userRepository.findAll(userSpec);
        if (!loginEventsMap.isEmpty()) {
            searchResults = searchResults.stream().filter(user -> loginEventsMap.containsKey(user.getId())).collect(Collectors.toList());
        }
        return searchResults;

    }

    @SuppressWarnings({"OptionalGetWithoutIsPresent"})
    private Specification<UserEntity> buildBasicSearchSpecification(Specification<UserEntity> userSpec, UserSearchParameters userSearchParams) {
        String searchValue = userSearchParams.getSearch().get();
        userSpec = userSpec.and((userSpecifications.userParamsLike(searchValue)));
        return userSpec;
    }


    private Specification<UserEntity> buildAdvancedSearchSpecification(Specification<UserEntity> userSpec, UserSearchParameters userSearchParams, Map<String, String> roleIdNameMap) {
        Optional<String> firstName = userSearchParams.getFirstName();
        Optional<String> lastName = userSearchParams.getLastName();
        Optional<String> username = userSearchParams.getUsername();
        Optional<String> email = userSearchParams.getEmail();
        Optional<String> organizationId = userSearchParams.getOrganizationId();

        if (firstName.isPresent()) {
            userSpec = userSpec.and(userSpecifications.firstNameLike(firstName.get()));
        }
        if (lastName.isPresent()) {
            userSpec = userSpec.and(userSpecifications.lastNameLike(lastName.get()));
        }
        if (username.isPresent()) {
            userSpec = userSpec.and(userSpecifications.usernameLike(username.get()));
        }
        if (email.isPresent()) {
            userSpec = userSpec.and(userSpecifications.emailLike(email.get()));
        }
        if (organizationId.isPresent()) {
            userSpec = userSpec.and(userSpecifications.hasOrganizationWithGivenId(organizationId.get()));
        }
        if (!roleIdNameMap.isEmpty()) {
            userSpec = userSpec.and(userSpecifications.rolesLike(new ArrayList<>(roleIdNameMap.keySet())));
        }
        return userSpec;
    }

    private Map<String, Object> getUsersLoginEvents(UserSearchParameters userSearchParams) {
        Map<String, Object> loginEvents;
        Optional<String> lastLogAfter = userSearchParams.getLastLogAfter();
        Optional<String> lastLogBefore = userSearchParams.getLastLogBefore();
        Optional<String> clientId = userSearchParams.getClientId();

        if (lastLogAfter.isPresent()) {
            if (clientId.isPresent()) {
                loginEvents = eventService.getLastLoginEventsWithGivenClientAfterGivenDate(lastLogAfter.get(), clientId.get());
            } else {
                loginEvents = eventService.getLastLoginEventsAfterGivenDate(lastLogAfter.get());
            }
        } else if (lastLogBefore.isPresent()) {
            if (clientId.isPresent()) {
                loginEvents = eventService.getLastLoginEventsWithGivenClientBeforeGivenDate(lastLogBefore.get(), clientId.get());
            } else {
                loginEvents = eventService.getLastLoginEventsBeforeGivenDate(lastLogBefore.get());
            }
        } else {
            loginEvents = Map.of();
        }
        return loginEvents;
    }

    private Map<String, String> getClientRoles(UserSearchParameters userSearchParams) {
        Optional<String> clientId = userSearchParams.getClientId();
        Optional<String[]> selectedRoles = userSearchParams.getSelectedRoles();
        Map<String, String> roleIdNameMap = new HashMap<>();

        if (clientId.isPresent()) {
            List<RoleEntity> clientRoles;

            if (selectedRoles.isPresent()) {
                clientRoles = roleRepository.findMohApplicationsRolesByClientAndNames(clientId.get(), List.of(selectedRoles.get()));
            } else {
                clientRoles = roleRepository.findMohApplicationsRolesByClient(clientId.get());
            }
            clientRoles.forEach(r -> roleIdNameMap.put(r.getId(), r.getName()));
        }
        return roleIdNameMap;
    }
}
