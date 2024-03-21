package ca.bc.gov.hlth.mohums.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final EventRepository eventRepository;
    private final UserSpecifications userSpecifications;

    @Autowired
    public UserService(UserRepository userRepository, RoleRepository roleRepository, EventRepository eventRepository, UserSpecifications userSpecifications) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.eventRepository = eventRepository;
        this.userSpecifications = userSpecifications;
    }

    public Optional<UserDTO> getUserByID(String id) {
        return userRepository.findById(id).map(user -> UserDTOMapper.convertToDTO(user, Map.of(), false));
    }

    public List<UserDTO> getUsers(Optional<String> email,
                                  Optional<String> firstName,
                                  Optional<String> lastName,
                                  Optional<String> search,
                                  Optional<String> username,
                                  Optional<String> organizationId,
                                  Optional<String> clientId,
                                  Optional<String[]> selectedRoles,
                                  Optional<String> lastLogAfter) {

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

        //maybe convert to map? quicker lookup -> service class where list is converted to hash map
        List<LastLogDate> loginEvents;
        if(lastLogAfter.isPresent()) {
            long lastLogAfterEpoch = LocalDate.parse(lastLogAfter.get()).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
            if(clientId.isPresent()){
                loginEvents = eventRepository.findMohApplicationsLastLoginEventsWithGivenRoleAfterGivenDate(lastLogAfterEpoch, clientId.get());
            } else {
                loginEvents = eventRepository.findMohApplicationsLastLoginEventsAfterGivenDate(lastLogAfterEpoch);
            }
        } else {
            loginEvents = List.of();
        }
//for last log before
//        ZonedDateTime currentTime = ZonedDateTime.now(ZoneId.of("America/Los_Angeles")).minusYears(1);
//
//        // Convert the ZonedDateTime to Instant
//        Instant instant = currentTime.toInstant();
//
//        // Get the milliseconds from the Instant
//        long currentTimeMillis = instant.toEpochMilli();

        List<UserEntity> searchResults = userRepository.findAll(userSpec);
        if(!loginEvents.isEmpty()){
            Map<String, Long> a = loginEvents.stream().collect(Collectors.toMap(LastLogDate::getUserId, LastLogDate::getLastLogin));
            searchResults = searchResults.stream().filter(user -> a.containsKey(user.getId())).collect(Collectors.toList());
            //add to DTO
        }


        return searchResults.stream().map(user -> UserDTOMapper.convertToDTO(user, roleIdNameMap, organizationId.isPresent())).collect(Collectors.toList());
    }

}
