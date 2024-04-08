package ca.bc.gov.hlth.mohums.user;

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

    public Optional<UserDTO> getUserByID(String id) {
        return userRepository.findById(id).map(user -> UserDTOMapper.convertToDTO(user, Map.of(), false, ""));
    }

    public List<UserDTO> getUsers(Optional<String> email,
                                  Optional<String> firstName,
                                  Optional<String> lastName,
                                  Optional<String> search,
                                  Optional<String> username,
                                  Optional<String> organizationId,
                                  Optional<String> clientId,
                                  Optional<String[]> selectedRoles,
                                  Optional<String> lastLogAfter,
                                  Optional<String> lastLogBefore) {

        //TODO: make a check to see if lastLogBefore/After is the only attribute

        Specification<UserEntity> userSpec = Specification.where(userSpecifications.fromMohApplicationsRealm())
                .and(userSpecifications.notServiceAccount());

        if (search.isPresent()) {
            String searchValue = search.get();
            userSpec = userSpec.and((userSpecifications.userParamsLike(searchValue)));
        } else {
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
        }

        if(organizationId.isPresent()) {
            userSpec = userSpec.and(userSpecifications.hasOrganizationWithGivenId(organizationId.get()));
        }

        //if client id + selected roles present
        //fetch roles from RoleEntity based on client id, names, moh_applications realm
        //map of role id -> role name (role name will always be unique because it returns from one client only)
        //fetch users based on list of role ids
        //attach role names to the response object
        Map<String, String> roleIdNameMap = new HashMap<>();

        if(clientId.isPresent()) {
            List<RoleEntity> clientRoles;

            if(selectedRoles.isPresent()){
                clientRoles = roleRepository.findMohApplicationsRolesByClientAndNames(clientId.get(), List.of(selectedRoles.get()));
            } else {
                clientRoles = roleRepository.findMohApplicationsRolesByClient(clientId.get());
            }
            if(!clientRoles.isEmpty()){
                clientRoles.forEach(r -> roleIdNameMap.put(r.getId(), r.getName()));
                userSpec = userSpec.and(userSpecifications.rolesLike(new ArrayList<>(roleIdNameMap.keySet())));
            }
        }


        //if that's the only search param then do just this and fetch additional user info from result set - no need to fetch all the users and filter out
        Map<String, String> loginEvents;
        if(lastLogAfter.isPresent()) {
            if(clientId.isPresent()){
                loginEvents = eventService.getLastLoginEventsWithGivenClientAfterGivenDate(lastLogAfter.get(), clientId.get());
            } else {
                loginEvents = eventService.getLastLoginEventsAfterGivenDate(lastLogAfter.get());
            }
        } else if (lastLogBefore.isPresent()){
            if(clientId.isPresent()){
                loginEvents = eventService.getLastLoginEventsWithGivenClientBeforeGivenDate(lastLogBefore.get(), clientId.get());
            } else {
                loginEvents = eventService.getLastLoginEventsBeforeGivenDate(lastLogBefore.get());
            }
        } else {
            loginEvents = Map.of();
        }

        List<UserEntity> searchResults = userRepository.findAll(userSpec);
        if(!loginEvents.isEmpty()){
            searchResults = searchResults.stream().filter(user -> loginEvents.containsKey(user.getId())).collect(Collectors.toList());
            //add to DTO
        }


        return searchResults.stream().map(user -> UserDTOMapper.convertToDTO(user, roleIdNameMap, organizationId.isPresent(), loginEvents.get(user.getId()))).collect(Collectors.toList());
    }

}
