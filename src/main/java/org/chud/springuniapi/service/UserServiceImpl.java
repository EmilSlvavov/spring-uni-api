package org.chud.springuniapi.service;

import org.chud.springuniapi.dto.request.CreateUserRequest;
import org.chud.springuniapi.dto.request.UpdateUserProfileRequest;
import org.chud.springuniapi.dto.request.UpdateUserRequest;
import org.chud.springuniapi.dto.response.CourseSummaryResponse;
import org.chud.springuniapi.dto.response.UserDisplayResponse;
import org.chud.springuniapi.dto.response.UserResponse;
import org.chud.springuniapi.dto.response.UserSoftDeleteResponse;
import org.chud.springuniapi.entity.Course;
import org.chud.springuniapi.entity.Role;
import org.chud.springuniapi.entity.User;
import org.chud.springuniapi.enums.RoleName;
import org.chud.springuniapi.exception.BusinessRuleViolationException;
import org.chud.springuniapi.exception.DuplicateResourceException;
import org.chud.springuniapi.exception.ResourceNotFoundException;
import org.chud.springuniapi.mapper.UserMapper;
import org.chud.springuniapi.repository.UserRepository;
import org.chud.springuniapi.repository.projection.CourseSummaryRow;
import org.chud.springuniapi.service.serviceInterface.IUserService;
import org.chud.springuniapi.service.serviceInterface.internal.IUserServiceInternal;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

//The only class allowed to touch UserRepository. Anything outside the User aggregate
//that needs a user goes through IUserServiceInternal instead.
@Service
@Transactional(readOnly = true)
public class UserServiceImpl implements IUserService, IUserServiceInternal {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(
            UserRepository userRepository,
            UserMapper userMapper,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public List<UserResponse> findAll(Boolean deleted) {
        List<User> users = userRepository.findAll();
        //get the map of courses filtered by soft delete used to show users with their courses
        Map<Long, List<CourseSummaryResponse>> coursesByUser = listCoursesByUserId(users, deleted);

        return users.stream()
                .map(user -> userMapper.toResponse(
                        user,
                        coursesOfForMultipleUsers(
                                coursesByUser,
                                user)))
                .toList();
    }

    @Override
    public UserResponse findById(Long id, Boolean deleted) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));

        return userMapper.toResponse(user, coursesOfForSingleUser(user, deleted));
    }

    @Override
    public List<UserDisplayResponse> findDisplayLabels() {
        return userRepository.findAllProjectedBy().stream()
                .map(view ->
                        new UserDisplayResponse(view.getName(),
                                view.getDisplayLabel()))
                .toList();
    }

    @Override
    public List<UserSoftDeleteResponse> findAllBySoftDeleted(boolean isDeleted) {
        List<User> users = userRepository.findUsersByDeleted(isDeleted);
        Map<Long, List<CourseSummaryResponse>> coursesByUser = listCoursesByUserId(users, null);

        return users.stream()
                .map(user -> userMapper.toResponseWithSoftDelete(
                        user,
                        coursesOfForMultipleUsers(
                                coursesByUser,
                                user)))
                .toList();
    }

    @Override
    @Transactional
    public UserResponse update(Long id, UpdateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));

        user.setName(request.name()); // no save() flush() will save changes
        return userMapper.toResponse(user, coursesOfForSingleUser(user, null));
    }

    @Override
    @Transactional
    public UserResponse updateProfile(Long id, UpdateUserProfileRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));

        user.setBio(request.bio());
        user.setDateOfBirth(request.dateOfBirth());

        return userMapper.toResponse(user, coursesOfForSingleUser(user, null));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        User user = userRepository.findWithCoursesById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));

        for (Course course : Set.copyOf(user.getCourses())) {
            user.withdraw(course);
        }

        userRepository.delete(user);
    }

    @Override
    @Transactional
    public UserSoftDeleteResponse softDelete(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));

        user.setDeleted(true);
        return userMapper.toResponseWithSoftDelete(user, coursesOfForSingleUser(user, null));
    }

    @Override
    @Transactional
    public UserSoftDeleteResponse restoreSoftDelete(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));

        user.setDeleted(false);
        return userMapper.toResponseWithSoftDelete(user, coursesOfForSingleUser(user, null));
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public User loadWithCourses(Long id) {
        return userRepository.findWithCoursesById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
    }

    @Override
    public Optional<User> loadById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    public Optional<User> loadByEmail(String email) {
        return userRepository.findUserByEmailIgnoreCase(email);
    }

    @Override
    public Optional<RoleName> roleOf(Long id) {
        return userRepository.findRoleNameByUserId(id);
    }

    //flush stays in service layer. Otherwise, facade would be reaching for repo layer
    //which defeats its purpose.
    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public UserResponse describe(User user) {
        //push pending join table rows out so the summary query below sees them
        userRepository.flush();
        return userMapper.toResponse(user, coursesOfForSingleUser(user, null));
    }

    //changed method so it checks if it won the race condition and throws if not
    @Override
    @Transactional
    public UserResponse create(CreateUserRequest request, Role role) {
        if (userRepository.existsByEmailIgnoreCase(request.email())) {
            throw new DuplicateResourceException(
                    "Email '%s' is already registered".formatted(request.email()));
        }

        String password = passwordEncoder.encode(request.password());

        User saved;
        try {
            saved = userRepository.saveAndFlush(
                    new User(
                            request.name(),
                            request.email(),
                            password,
                            role));
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateResourceException(
                    "Email '%s' is already registered".formatted(request.email())
            );
        }
        //a fresh user has no courses yet, so there is nothing to query for
        return userMapper.toResponse(saved, List.of());
    }

    @Override
    @Transactional
    public UserResponse assignRole(Long id, Role role) {
        User user = userRepository.findWithCoursesById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User", id));

        if (role.getRoleName() == RoleName.ADMIN && !user.getCourses().isEmpty()) {
            throw new BusinessRuleViolationException(
                "a user enrolled in courses cannot be promoted to ADMIN");
        }

        user.setRole(role);

        return userMapper.toResponse(user, coursesOfForSingleUser(user, null));
    }

    //get the courses by user with filter for soft delete value
    private Map<Long, List<CourseSummaryResponse>> listCoursesByUserId(List<User> users, Boolean deleted) {
        //get the ids of the users
        List<Long> ids = users.stream().map(User::getId).toList();
        if (ids.isEmpty()) { //check for empty list
            return Map.of();
        }

        //findCourseSummariesByUserIds returns a list of CourseSummaryRow and then we apply
        // the groupByOwner method which returns a map with keys of owner ids and values of CourseSummaryResponse
        return CourseSummaryRow.groupByOwner(
                userRepository.findCourseSummariesByUserIds(ids, deleted));
    }

    //for this method you have a map of ownerId values and their courses and the second parameter
    // is the user you want the courses of. You get the courses of that user in a list or if
    // there arent any you set his courses to be an empty list
    //used for pulling out one entry after the map is already built
    private List<CourseSummaryResponse> coursesOfForMultipleUsers(Map<Long, List<CourseSummaryResponse>> coursesByUser,
                                                                  User user) {

        return coursesByUser.getOrDefault(user.getId(), List.of());
    }

    //used for single entity paths which dont deal with multiple users like update etc.
    private List<CourseSummaryResponse> coursesOfForSingleUser(User user, Boolean deleted) {
        return coursesOfForMultipleUsers(listCoursesByUserId(List.of(user), deleted), user);
    }
}
