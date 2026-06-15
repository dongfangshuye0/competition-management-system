package com.tangqilin.competition.storage;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.tangqilin.competition.common.constant.Roles;
import com.tangqilin.competition.entity.Competition;
import com.tangqilin.competition.entity.Notification;
import com.tangqilin.competition.entity.Registration;
import com.tangqilin.competition.entity.Score;
import com.tangqilin.competition.entity.User;
import com.tangqilin.competition.entity.Work;
import com.tangqilin.competition.mapper.CompetitionMapper;
import com.tangqilin.competition.mapper.NotificationMapper;
import com.tangqilin.competition.mapper.RegistrationMapper;
import com.tangqilin.competition.mapper.ScoreMapper;
import com.tangqilin.competition.mapper.UserMapper;
import com.tangqilin.competition.mapper.WorkMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Comparator;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Component
// 统一封装 MyBatis-Plus 数据访问，避免业务层直接依赖多个 Mapper。
public class DatabaseStore {

    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final CompetitionMapper competitionMapper;
    private final RegistrationMapper registrationMapper;
    private final WorkMapper workMapper;
    private final ScoreMapper scoreMapper;
    private final NotificationMapper notificationMapper;

    public DatabaseStore(PasswordEncoder passwordEncoder,
                         UserMapper userMapper,
                         CompetitionMapper competitionMapper,
                         RegistrationMapper registrationMapper,
                         WorkMapper workMapper,
                         ScoreMapper scoreMapper,
                         NotificationMapper notificationMapper) {
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
        this.competitionMapper = competitionMapper;
        this.registrationMapper = registrationMapper;
        this.workMapper = workMapper;
        this.scoreMapper = scoreMapper;
        this.notificationMapper = notificationMapper;
    }

    @PostConstruct
    public void init() {
        seedDemoData();
    }

    public Optional<User> findUserById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(userMapper.selectById(id));
    }

    public Optional<User> findUserByUsername(String username) {
        if (!StringUtils.hasText(username)) {
            return Optional.empty();
        }
        return Optional.ofNullable(userMapper.selectOne(Wrappers.<User>lambdaQuery()
                .eq(User::getUsername, username.trim())));
    }

    public List<User> listUsers() {
        return userMapper.selectList(null).stream()
                .sorted(Comparator.comparing(User::getId).reversed())
                .toList();
    }

    public long countUsers() {
        return userMapper.selectCount(null);
    }

    public List<User> listUsersByRole(String role) {
        if (!StringUtils.hasText(role)) {
            return Collections.emptyList();
        }
        return userMapper.selectList(Wrappers.<User>lambdaQuery()
                        .eq(User::getRole, role.trim()))
                .stream()
                .sorted(Comparator.comparing(User::getId).reversed())
                .toList();
    }

    public long countUsersByRole(String role) {
        if (!StringUtils.hasText(role)) {
            return 0;
        }
        return userMapper.selectCount(Wrappers.<User>lambdaQuery()
                .eq(User::getRole, role.trim()));
    }

    public User saveUser(User user) {
        LocalDateTime now = LocalDateTime.now();
        if (user.getId() == null) {
            user.setCreateTime(now);
            user.setIsDeleted(0);
            user.setUpdateTime(now);
            userMapper.insert(user);
        } else {
            user.setUpdateTime(now);
            userMapper.updateById(user);
        }
        return user;
    }

    public void deleteUser(Long id) {
        userMapper.deleteById(id);
    }

    public Optional<Competition> findCompetitionById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(competitionMapper.selectById(id));
    }

    public List<Competition> listCompetitions() {
        return competitionMapper.selectList(null).stream()
                .sorted(Comparator.comparing(Competition::getId).reversed())
                .toList();
    }

    public long countCompetitions() {
        return competitionMapper.selectCount(null);
    }

    public Competition saveCompetition(Competition competition) {
        LocalDateTime now = LocalDateTime.now();
        if (competition.getId() == null) {
            competition.setCreateTime(now);
            competition.setIsDeleted(0);
            competition.setUpdateTime(now);
            competitionMapper.insert(competition);
        } else {
            competition.setUpdateTime(now);
            competitionMapper.updateById(competition);
        }
        return competition;
    }

    public void deleteCompetition(Long id) {
        competitionMapper.deleteById(id);
    }

    public Optional<Registration> findRegistrationById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(registrationMapper.selectById(id));
    }

    public List<Registration> listRegistrations() {
        return registrationMapper.selectList(null).stream()
                .sorted(Comparator.comparing(Registration::getId).reversed())
                .toList();
    }

    public long countRegistrations() {
        return registrationMapper.selectCount(null);
    }

    public long countRegistrationsByUserId(Long userId) {
        if (userId == null) {
            return 0;
        }
        return registrationMapper.selectCount(Wrappers.<Registration>lambdaQuery()
                .eq(Registration::getUserId, userId));
    }

    public long countRegistrationsByUserIdAndStatus(Long userId, String status) {
        if (userId == null || !StringUtils.hasText(status)) {
            return 0;
        }
        return registrationMapper.selectCount(Wrappers.<Registration>lambdaQuery()
                .eq(Registration::getUserId, userId)
                .eq(Registration::getStatus, status.trim()));
    }

    public long countRegistrationsByTeacherIdAndStatus(Long teacherId, String status) {
        if (teacherId == null || !StringUtils.hasText(status)) {
            return 0;
        }
        return registrationMapper.selectCount(Wrappers.<Registration>lambdaQuery()
                .eq(Registration::getTeacherId, teacherId)
                .eq(Registration::getStatus, status.trim()));
    }

    public long countRegistrationsByCompetitionId(Long competitionId) {
        if (competitionId == null) {
            return 0;
        }
        return registrationMapper.selectCount(Wrappers.<Registration>lambdaQuery()
                .eq(Registration::getCompetitionId, competitionId));
    }

    public boolean existsRegistrationByCompetitionId(Long competitionId) {
        if (competitionId == null) {
            return false;
        }
        return registrationMapper.selectCount(Wrappers.<Registration>lambdaQuery()
                .eq(Registration::getCompetitionId, competitionId)) > 0;
    }

    public boolean existsRegistrationByCompetitionIdAndUserId(Long competitionId, Long userId) {
        if (competitionId == null || userId == null) {
            return false;
        }
        return registrationMapper.selectCount(Wrappers.<Registration>lambdaQuery()
                .eq(Registration::getCompetitionId, competitionId)
                .eq(Registration::getUserId, userId)) > 0;
    }

    public boolean existsRegistrationByUserIdOrTeacherId(Long userId) {
        if (userId == null) {
            return false;
        }
        return registrationMapper.selectCount(Wrappers.<Registration>lambdaQuery()
                .eq(Registration::getUserId, userId)
                .or()
                .eq(Registration::getTeacherId, userId)) > 0;
    }

    public List<Registration> listRegistrationsByUserId(Long userId) {
        if (userId == null) {
            return Collections.emptyList();
        }
        return registrationMapper.selectList(Wrappers.<Registration>lambdaQuery()
                        .eq(Registration::getUserId, userId))
                .stream()
                .sorted(Comparator.comparing(Registration::getId).reversed())
                .toList();
    }

    public List<Registration> listRegistrationsByTeacherId(Long teacherId) {
        if (teacherId == null) {
            return Collections.emptyList();
        }
        return registrationMapper.selectList(Wrappers.<Registration>lambdaQuery()
                        .eq(Registration::getTeacherId, teacherId))
                .stream()
                .sorted(Comparator.comparing(Registration::getId).reversed())
                .toList();
    }

    public List<Registration> listRegistrationsByCompetitionId(Long competitionId) {
        if (competitionId == null) {
            return Collections.emptyList();
        }
        return registrationMapper.selectList(Wrappers.<Registration>lambdaQuery()
                        .eq(Registration::getCompetitionId, competitionId))
                .stream()
                .sorted(Comparator.comparing(Registration::getId).reversed())
                .toList();
    }

    public Registration saveRegistration(Registration registration) {
        LocalDateTime now = LocalDateTime.now();
        if (registration.getId() == null) {
            registration.setCreateTime(now);
            registration.setIsDeleted(0);
            registration.setUpdateTime(now);
            registrationMapper.insert(registration);
        } else {
            registration.setUpdateTime(now);
            registrationMapper.updateById(registration);
        }
        return registration;
    }

    public void deleteRegistration(Long id) {
        registrationMapper.deleteById(id);
    }

    public Optional<Work> findWorkById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(workMapper.selectById(id));
    }

    public Optional<Work> findWorkByRegistrationId(Long registrationId) {
        if (registrationId == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(workMapper.selectOne(Wrappers.<Work>lambdaQuery()
                .eq(Work::getRegistrationId, registrationId)));
    }

    public List<Work> listWorks() {
        return workMapper.selectList(null).stream()
                .sorted(Comparator.comparing(Work::getId).reversed())
                .toList();
    }

    public long countWorks() {
        return workMapper.selectCount(null);
    }

    public List<Work> listWorksByRegistrationIds(Collection<Long> registrationIds) {
        if (registrationIds == null || registrationIds.isEmpty()) {
            return Collections.emptyList();
        }
        return workMapper.selectList(Wrappers.<Work>lambdaQuery()
                        .in(Work::getRegistrationId, registrationIds))
                .stream()
                .sorted(Comparator.comparing(Work::getId).reversed())
                .toList();
    }

    public List<Work> listWorksByReviewTeacherId(Long teacherId) {
        if (teacherId == null) {
            return Collections.emptyList();
        }
        return workMapper.selectList(Wrappers.<Work>lambdaQuery()
                        .eq(Work::getReviewTeacherId, teacherId))
                .stream()
                .sorted(Comparator.comparing(Work::getId).reversed())
                .toList();
    }

    public Work saveWork(Work work) {
        LocalDateTime now = LocalDateTime.now();
        if (work.getId() == null) {
            work.setCreateTime(now);
            work.setIsDeleted(0);
            work.setUpdateTime(now);
            workMapper.insert(work);
        } else {
            work.setUpdateTime(now);
            workMapper.updateById(work);
        }
        return work;
    }

    public Optional<Score> findScoreByWorkAndTeacher(Long workId, Long teacherId) {
        if (workId == null || teacherId == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(scoreMapper.selectOne(Wrappers.<Score>lambdaQuery()
                .eq(Score::getWorkId, workId)
                .eq(Score::getTeacherId, teacherId)));
    }

    public List<Score> listScores() {
        return scoreMapper.selectList(null).stream()
                .sorted(Comparator.comparing(Score::getId).reversed())
                .toList();
    }

    public long countScoresByTeacherId(Long teacherId) {
        if (teacherId == null) {
            return 0;
        }
        return scoreMapper.selectCount(Wrappers.<Score>lambdaQuery()
                .eq(Score::getTeacherId, teacherId));
    }

    public boolean existsScoreByTeacherId(Long teacherId) {
        return countScoresByTeacherId(teacherId) > 0;
    }

    public List<Score> listScoresByWorkId(Long workId) {
        if (workId == null) {
            return Collections.emptyList();
        }
        return scoreMapper.selectList(Wrappers.<Score>lambdaQuery()
                        .eq(Score::getWorkId, workId))
                .stream()
                .sorted(Comparator.comparing(Score::getId))
                .toList();
    }

    public Score saveScore(Score score) {
        LocalDateTime now = LocalDateTime.now();
        if (score.getId() == null) {
            score.setCreateTime(now);
            score.setIsDeleted(0);
            score.setUpdateTime(now);
            scoreMapper.insert(score);
        } else {
            score.setUpdateTime(now);
            scoreMapper.updateById(score);
        }
        return score;
    }

    public Optional<Notification> findNotificationById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(notificationMapper.selectById(id));
    }

    public List<Notification> listNotifications() {
        return notificationMapper.selectList(null).stream()
                .sorted(Comparator.comparing(Notification::getId).reversed())
                .toList();
    }

    public long countUnreadNotificationsByUserId(Long userId) {
        if (userId == null) {
            return 0;
        }
        return notificationMapper.selectCount(Wrappers.<Notification>lambdaQuery()
                .eq(Notification::getUserId, userId)
                .eq(Notification::getReadStatus, false));
    }

    public List<Notification> listNotificationsByUserId(Long userId) {
        if (userId == null) {
            return Collections.emptyList();
        }
        return notificationMapper.selectList(Wrappers.<Notification>lambdaQuery()
                        .eq(Notification::getUserId, userId))
                .stream()
                .sorted(Comparator.comparing(Notification::getId).reversed())
                .toList();
    }

    public Notification saveNotification(Notification notification) {
        LocalDateTime now = LocalDateTime.now();
        if (notification.getId() == null) {
            notification.setCreateTime(now);
            notification.setIsDeleted(0);
            notification.setUpdateTime(now);
            notificationMapper.insert(notification);
        } else {
            notification.setUpdateTime(now);
            notificationMapper.updateById(notification);
        }
        return notification;
    }

    private void seedDemoData() {
        User admin = ensureUser("admin", "admin123", "系统管理员", Roles.ADMIN, null, null, "13800000000", "admin@campus.local");
        User teacher1 = ensureUser("teacher01", "teacher123", "王老师", Roles.TEACHER, "计算机科学与技术", null, "13900000001", "teacher01@campus.local");
        User teacher2 = ensureUser("teacher02", "teacher123", "李老师", Roles.TEACHER, "人工智能", null, "13900000002", "teacher02@campus.local");
        User student1 = ensureUser("20240001", "student123", "张同学", Roles.STUDENT, "软件工程", "软件工程1班", "13700000001", "student01@campus.local");
        User student2 = ensureUser("20240002", "student123", "陈同学", Roles.STUDENT, "数据科学", "数据科学2班", "13700000002", "student02@campus.local");

        LocalDateTime now = LocalDateTime.now();
        Competition comp1 = ensureCompetition(
                "全国大学生软件设计大赛",
                "面向软件工程、计算机与人工智能相关专业，提交系统设计、实现与答辩材料。",
                "信息工程学院",
                "国家级",
                now.minusDays(5),
                now.plusDays(7),
                now.plusDays(20),
                false
        );
        Competition comp2 = ensureCompetition(
                "校级数据分析挑战赛",
                "聚焦数据建模、商业分析与可视化展示，鼓励跨专业组队。",
                "教务处",
                "校级",
                now.plusDays(1),
                now.plusDays(10),
                now.plusDays(25),
                false
        );
        Competition comp3 = ensureCompetition(
                "省级创新创业项目路演",
                "面向创新创业团队的项目书、原型系统和现场路演。",
                "创新创业学院",
                "省级",
                now.minusDays(20),
                now.minusDays(10),
                now.minusDays(2),
                true
        );

        seedRegistrations(student1, student2, teacher1, teacher2, comp1, comp3);
        seedWorksAndScores(teacher1, teacher2);
        seedNotifications(admin, teacher1, student1);
    }

    private User ensureUser(String username, String password, String name, String role, String major, String className, String phone, String email) {
        return findUserByUsername(username).orElseGet(() -> {
            User user = new User();
            user.setUsername(username);
            user.setPassword(passwordEncoder.encode(password));
            user.setName(name);
            user.setRole(role);
            user.setMajor(major);
            user.setClassName(className);
            user.setPhone(phone);
            user.setEmail(email);
            return saveUser(user);
        });
    }

    private Competition ensureCompetition(String name,
                                          String description,
                                          String organizer,
                                          String level,
                                          LocalDateTime registrationStartTime,
                                          LocalDateTime registrationEndTime,
                                          LocalDateTime submissionEndTime,
                                          Boolean rankingPublished) {
        Optional<Competition> existing = listCompetitions().stream()
                .filter(competition -> name.equals(competition.getName()))
                .findFirst();
        if (existing.isPresent()) {
            return existing.get();
        }
        Competition competition = new Competition();
        competition.setName(name);
        competition.setDescription(description);
        competition.setOrganizer(organizer);
        competition.setLevel(level);
        competition.setRegistrationStartTime(registrationStartTime);
        competition.setRegistrationEndTime(registrationEndTime);
        competition.setSubmissionEndTime(submissionEndTime);
        competition.setRankingPublished(rankingPublished);
        return saveCompetition(competition);
    }

    private void seedRegistrations(User student1,
                                   User student2,
                                   User teacher1,
                                   User teacher2,
                                   Competition comp1,
                                   Competition comp3) {
        if (!listRegistrations().isEmpty()) {
            return;
        }
        Registration reg1 = new Registration();
        reg1.setUserId(student1.getId());
        reg1.setCompetitionId(comp1.getId());
        reg1.setTeacherId(teacher1.getId());
        reg1.setTeamName("启程队");
        reg1.setTeamMembers("张同学、陈同学");
        reg1.setApplicationForm("/uploads/demo/application-demo.pdf");
        reg1.setStatus("approved");
        reg1.setComment("材料完整，可以准备提交作品。");
        saveRegistration(reg1);

        Registration reg2 = new Registration();
        reg2.setUserId(student2.getId());
        reg2.setCompetitionId(comp1.getId());
        reg2.setTeacherId(teacher1.getId());
        reg2.setTeamName("智创队");
        reg2.setTeamMembers("陈同学");
        reg2.setApplicationForm("/uploads/demo/application-demo-2.pdf");
        reg2.setStatus("pending");
        reg2.setComment("等待教师审核");
        saveRegistration(reg2);

        Registration reg3 = new Registration();
        reg3.setUserId(student1.getId());
        reg3.setCompetitionId(comp3.getId());
        reg3.setTeacherId(teacher2.getId());
        reg3.setTeamName("星火队");
        reg3.setTeamMembers("张同学");
        reg3.setApplicationForm("/uploads/demo/application-demo-3.pdf");
        reg3.setStatus("approved");
        reg3.setComment("已进入作品评审阶段");
        saveRegistration(reg3);
    }

    private void seedWorksAndScores(User teacher1, User teacher2) {
        if (!listWorks().isEmpty()) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        List<Registration> registrations = listRegistrations();
        if (registrations.isEmpty()) {
            return;
        }

        registrations.stream()
                .filter(registration -> "启程队".equals(registration.getTeamName()))
                .findFirst()
                .ifPresent(registration -> {
                    Work work = new Work();
                    work.setRegistrationId(registration.getId());
                    work.setTitle("智慧竞赛管理平台");
                    work.setDescription("围绕竞赛报名、评审和通知流程设计的一体化平台。");
                    work.setFilePath("/uploads/demo/work-demo.zip");
                    work.setReviewTeacherId(teacher1.getId());
                    work.setAiScore(84);
                    work.setAiComment("选题贴合业务场景，结构完整，建议补充异常流程与数据指标展示。");
                    work.setSubmitTime(now.minusDays(1));
                    saveWork(work);

                    Score score = new Score();
                    score.setWorkId(work.getId());
                    score.setTeacherId(teacher1.getId());
                    score.setScore(88);
                    score.setComment("功能闭环较完整，界面交互清晰，建议继续打磨评审流程细节。");
                    saveScore(score);
                });

        registrations.stream()
                .filter(registration -> "星火队".equals(registration.getTeamName()))
                .findFirst()
                .ifPresent(registration -> {
                    Work work = new Work();
                    work.setRegistrationId(registration.getId());
                    work.setTitle("创业项目路演分析报告");
                    work.setDescription("以创业训练项目为主题的商业分析与路演材料。");
                    work.setFilePath("/uploads/demo/work-demo-2.pdf");
                    work.setReviewTeacherId(teacher2.getId());
                    work.setAiScore(90);
                    work.setAiComment("项目表达完整，商业逻辑清晰，若能补充风险评估会更有说服力。");
                    work.setSubmitTime(now.minusDays(6));
                    saveWork(work);

                    Score score = new Score();
                    score.setWorkId(work.getId());
                    score.setTeacherId(teacher2.getId());
                    score.setScore(92);
                    score.setComment("路演材料成熟度高，建议加强数据来源说明。");
                    saveScore(score);
                });
    }

    private void seedNotifications(User admin, User teacher1, User student1) {
        if (!listNotifications().isEmpty()) {
            return;
        }
        Notification notice1 = new Notification();
        notice1.setUserId(student1.getId());
        notice1.setTitle("报名审核通过");
        notice1.setContent("你报名的“全国大学生软件设计大赛”已通过审核，请按时提交作品。");
        notice1.setReadStatus(false);
        saveNotification(notice1);

        Notification notice2 = new Notification();
        notice2.setUserId(teacher1.getId());
        notice2.setTitle("新的报名待审核");
        notice2.setContent("陈同学提交了新的竞赛报名，请及时审核。");
        notice2.setReadStatus(false);
        saveNotification(notice2);

        Notification notice3 = new Notification();
        notice3.setUserId(admin.getId());
        notice3.setTitle("系统初始化完成");
        notice3.setContent("演示数据已装载到数据库，可直接体验学生、教师和管理员流程。");
        notice3.setReadStatus(false);
        saveNotification(notice3);
    }
}
