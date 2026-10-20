package org.example.schoolback.service;

import org.apache.commons.lang3.StringUtils;
import org.example.schoolback.dto.UserDTO;
import org.example.schoolback.entity.Role;
import org.example.schoolback.entity.User;
import org.example.schoolback.repository.UserRepository;
import org.example.schoolback.util.Updater;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;

@Service
@Transactional
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EnrollmentHistoryService enrollmentHistoryService;

    /**
     * Сколько опыта нужно на один уровень. Значение условное: в описании
     * возможности порог не задан, поэтому вынесено в одно место, чтобы менять его здесь.
     */
    private static final long EXPERIENCE_PER_RANK = 100L;

    @Transactional(readOnly = true)
    public Optional<User> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()) {
            return userRepository.findByLogin(authentication.getName());
        }
        return Optional.empty();
    }

    @Transactional(readOnly = true)
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }


    public User createUser(UserDTO userDTO, Converter<UserDTO, User> converter) {
        final User newUser = converter.convert(userDTO);
        validateCreate(newUser);
        newUser.setCoins(0L);

        return userRepository.save(newUser);
    }

    public Page<User> getByRole(Role role, Pageable pageable) {
        return userRepository.findByRole(role, pageable);
    }

    public User updateUser(Long userId, UserDTO userDTO, Updater<User, UserDTO> updater) {
        final User existUser = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException(String.format("User not found with id: %d", userId)));

        final User updatedUser = new User();
        updater.update(updatedUser, userDTO);
        validateUpdate(userId, updatedUser);

        updatedUser.setId(userId);
        //пользователь при правке собирается заново из присланных полей, а опыт и уровень
        //в запросе не приходят: переносим уже накопленное, иначе правка профиля его обнулила бы
        updatedUser.setExperience(existUser.getExperience());
        updatedUser.setRankLevel(existUser.getRankLevel());
        //пароль меняется, только если его прислали: иначе остаётся прежний
        if (updatedUser.getPassword() == null) {
            updatedUser.setPassword(existUser.getPassword());
        }

        return userRepository.save(updatedUser);
    }

    public void addCoins(Long userId, Long countCoins) {
        Optional<User> student = userRepository.findById(userId);
        if (student.isEmpty()) {
            throw new IllegalArgumentException(String.format("Student not found with id: %d", userId));
        }

        Optional<User> teacher = getCurrentUser();

        if (teacher.isEmpty()) {
            throw new IllegalArgumentException(String.format("Teacher not found with id: %d", userId));
        }

        User existStudent = student.get();
        User existTeacher = teacher.get();


        Long currentCoins = existStudent.getCoins();
        existStudent.setCoins(currentCoins + countCoins);

        userRepository.save(existStudent);
        enrollmentHistoryService.saveEnrollmentHistory(existTeacher, existStudent, countCoins);
    }

    public void deleteUser(Long userId) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty()) {
            throw new IllegalArgumentException(String.format("User not found with id: %d", userId));
        }
        userRepository.delete(user.get());
    }

    /**
     * Начисление за принятую задачу: монеты и опыт сразу, уровень пересчитывается от опыта.
     * Запись в историю начислений делает вызывающая служба — она знает причину начисления.
     *
     * @return обновлённый пользователь
     */
    public User addTaskReward(Long studentId, long coins, long experience) {
        if (coins < 0 || experience < 0) {
            throw new IllegalArgumentException("Reward cannot be negative");
        }

        final User student = userRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException(String.format("Student not found with id: %d", studentId)));

        //монеты у давно созданных пользователей могут быть не заданы, поэтому пустое значение считаем нулём
        student.setCoins((student.getCoins() == null ? 0L : student.getCoins()) + coins);
        student.setExperience((student.getExperience() == null ? 0L : student.getExperience()) + experience);
        student.setRankLevel(rankForExperience(student.getExperience()));

        return userRepository.save(student);
    }

    /** Уровень: единица плюс по одному за каждые EXPERIENCE_PER_RANK опыта. */
    private static int rankForExperience(long experience) {
        return (int) (1 + experience / EXPERIENCE_PER_RANK);
    }

    public void setCoinsForUser(Long userId, Long coins) {
        if(coins < 0){
            throw new IllegalArgumentException("Coins cannot be negative");
        }
        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty()) {
            throw new IllegalArgumentException(String.format("User not found with id: %d", userId));
        }
        user.get().setCoins(coins);
        userRepository.save(user.get());
    }

    /**
     * Поля проверяются аннотациями на объекте запроса, здесь остаётся только то,
     * для чего нужен поход в базу: не занят ли логин и не занята ли почта.
     */
    private void validateCreate(User user) {
        if (userRepository.findByLogin(user.getLogin()).isPresent()) {
            throw new IllegalArgumentException(String.format("User with login %s already exists", user.getLogin()));
        }
        //почта необязательна: проверяем занятость только если она указана,
        //иначе второй пользователь без почты совпал бы с первым таким же
        if (!StringUtils.isBlank(user.getEmail()) && userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new IllegalArgumentException(String.format("User with email %s already exists", user.getEmail()));
        }
    }

    private void validateUpdate(Long userId, User updatedUser) {
        final User existUser = userRepository.findById(userId).get();

        //если в рамках обновления был изменён логин, то мы проверяем, есть ли в бд уже пользователь с таким логином
        if (!existUser.getLogin().equals(updatedUser.getLogin()) && userRepository.findByLogin(updatedUser.getLogin()).isPresent()) {
            throw new IllegalArgumentException(String.format("User with login %s already exists", updatedUser.getLogin()));
        }
        //если в рамках обновления была изменена почта, то мы проверяем, есть ли в бд уже пользователь с такой почтой
        //Objects.equals, а не equals: у существующего пользователя почты может не быть
        if (!StringUtils.isBlank(updatedUser.getEmail())
                && !Objects.equals(existUser.getEmail(), updatedUser.getEmail())
                && userRepository.findByEmail(updatedUser.getEmail()).isPresent()) {
            throw new IllegalArgumentException(String.format("User with email %s already exists", updatedUser.getEmail()));
        }
    }


}
