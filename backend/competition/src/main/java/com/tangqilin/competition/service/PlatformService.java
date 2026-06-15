package com.tangqilin.competition.service;

import com.tangqilin.competition.common.constant.Roles;
import com.tangqilin.competition.common.exception.BusinessException;
import com.tangqilin.competition.common.result.Result;
import com.tangqilin.competition.common.security.AuthContext;
import com.tangqilin.competition.entity.Competition;
import com.tangqilin.competition.entity.Notification;
import com.tangqilin.competition.entity.Registration;
import com.tangqilin.competition.entity.Score;
import com.tangqilin.competition.entity.User;
import com.tangqilin.competition.entity.Work;
import com.tangqilin.competition.entity.dto.AiQuestionDTO;
import com.tangqilin.competition.entity.dto.CompetitionDTO;
import com.tangqilin.competition.entity.dto.NotificationCreateDTO;
import com.tangqilin.competition.entity.dto.RegistrationCreateDTO;
import com.tangqilin.competition.entity.dto.RegistrationReviewDTO;
import com.tangqilin.competition.entity.dto.ScoreDTO;
import com.tangqilin.competition.entity.dto.WorkAssignReviewerDTO;
import com.tangqilin.competition.entity.dto.WorkSubmitDTO;
import com.tangqilin.competition.storage.FileStorageService;
import com.tangqilin.competition.storage.DatabaseStore;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Service
public class PlatformService {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final DatabaseStore databaseStore;
    private final FileStorageService fileStorageService;

    public PlatformService(DatabaseStore databaseStore, FileStorageService fileStorageService) {
        this.databaseStore = databaseStore;
        this.fileStorageService = fileStorageService;
    }

    public Result<?> publicCompetitions() {
        return Result.success(databaseStore.listCompetitions().stream().map(this::toCompetitionView).toList());
    }

    public Result<?> publicCompetitionDetail(Long id) {
        Competition competition = competitionById(id);
        Map<String, Object> data = toCompetitionView(competition);
        if (Boolean.TRUE.equals(competition.getRankingPublished())) {
            data.put("ranking", rankingRows(competition, false));
        }
        return Result.success(data);
    }

    public Result<?> dashboardSummary() {
        User currentUser = currentUser();
        Map<String, Object> summary = new LinkedHashMap<>();
        long unreadNotifications = databaseStore.countUnreadNotificationsByUserId(currentUser.getId());
        summary.put("unreadNotifications", unreadNotifications);

        if (Roles.STUDENT.equals(currentUser.getRole())) {
            List<Registration> myRegistrationRows = databaseStore.listRegistrationsByUserId(currentUser.getId());
            List<Long> registrationIds = myRegistrationRows.stream().map(Registration::getId).toList();
            long myRegistrations = databaseStore.countRegistrationsByUserId(currentUser.getId());
            long approvedRegistrations = databaseStore.countRegistrationsByUserIdAndStatus(currentUser.getId(), "approved");
            long myWorks = databaseStore.listWorksByRegistrationIds(registrationIds).size();
            summary.put("competitions", databaseStore.countCompetitions());
            summary.put("myRegistrations", myRegistrations);
            summary.put("approvedRegistrations", approvedRegistrations);
            summary.put("myWorks", myWorks);
            summary.put("recommendedCompetitions", recommendedCompetitionViews(currentUser));
        } else if (Roles.TEACHER.equals(currentUser.getRole())) {
            long pendingRegistrations = databaseStore.countRegistrationsByTeacherIdAndStatus(currentUser.getId(), "pending");
            long assignedWorks = teacherVisibleWorks(currentUser).size();
            long scoredWorks = databaseStore.countScoresByTeacherId(currentUser.getId());
            summary.put("pendingRegistrations", pendingRegistrations);
            summary.put("assignedWorks", assignedWorks);
            summary.put("scoredWorks", scoredWorks);
            summary.put("students", teacherStudents(currentUser.getId()).size());
        } else {
            summary.put("users", databaseStore.countUsers());
            summary.put("competitions", databaseStore.countCompetitions());
            summary.put("registrations", databaseStore.countRegistrations());
            summary.put("works", databaseStore.countWorks());
        }
        return Result.success(summary);
    }

    public Result<?> competitionRecommendations() {
        User currentUser = currentUser();
        if (!Roles.STUDENT.equals(currentUser.getRole())) {
            throw BusinessException.forbidden("只有学生可以查看推荐竞赛");
        }
        return Result.success(recommendedCompetitionViews(currentUser));
    }

    public Result<?> saveCompetition(Long id, CompetitionDTO competitionDTO) {
        requireRole(Roles.ADMIN);
        validateCompetition(competitionDTO);
        Competition competition = id == null ? new Competition() : competitionById(id);
        competition.setName(competitionDTO.getName().trim());
        competition.setDescription(trim(competitionDTO.getDescription()));
        competition.setOrganizer(trim(competitionDTO.getOrganizer()));
        competition.setLevel(trim(competitionDTO.getLevel()));
        competition.setRegistrationStartTime(competitionDTO.getRegistrationStartTime());
        competition.setRegistrationEndTime(competitionDTO.getRegistrationEndTime());
        competition.setSubmissionEndTime(competitionDTO.getSubmissionEndTime());
        if (competition.getRankingPublished() == null) {
            competition.setRankingPublished(false);
        }
        databaseStore.saveCompetition(competition);

        if (id == null) {
            broadcastNotification("新竞赛发布", "新竞赛《" + competition.getName() + "》已开放报名，请及时查看。");
        }
        return Result.success(toCompetitionView(competition));
    }

    public Result<?> deleteCompetition(Long id) {
        requireRole(Roles.ADMIN);
        competitionById(id);
        boolean hasRegistration = databaseStore.existsRegistrationByCompetitionId(id);
        if (hasRegistration) {
            throw BusinessException.badRequest("该竞赛已有报名记录，暂不支持删除");
        }
        databaseStore.deleteCompetition(id);
        return Result.success();
    }

    public Result<?> createRegistration(RegistrationCreateDTO registrationCreateDTO) {
        User currentUser = currentUser();
        if (!Roles.STUDENT.equals(currentUser.getRole())) {
            throw BusinessException.forbidden("只有学生可以报名");
        }
        if (registrationCreateDTO == null || registrationCreateDTO.getCompetitionId() == null || registrationCreateDTO.getTeacherId() == null) {
            throw BusinessException.badRequest("请选择竞赛和指导教师");
        }
        Competition competition = competitionById(registrationCreateDTO.getCompetitionId());
        if (!"registration_open".equals(competitionStatus(competition))) {
            throw BusinessException.badRequest("当前不在报名时间内");
        }
        User teacher = userById(registrationCreateDTO.getTeacherId());
        if (!Roles.TEACHER.equals(teacher.getRole())) {
            throw BusinessException.badRequest("指导教师不存在");
        }
        boolean exists = databaseStore.existsRegistrationByCompetitionIdAndUserId(competition.getId(), currentUser.getId());
        if (exists) {
            throw BusinessException.badRequest("你已经报名过该竞赛");
        }

        Registration registration = new Registration();
        registration.setUserId(currentUser.getId());
        registration.setCompetitionId(competition.getId());
        registration.setTeacherId(teacher.getId());
        registration.setTeamName(trim(registrationCreateDTO.getTeamName()));
        registration.setTeamMembers(trim(registrationCreateDTO.getTeamMembers()));
        registration.setApplicationForm(fileStorageService.store(registrationCreateDTO.getApplicationForm(), "applications"));
        registration.setStatus("pending");
        registration.setComment("等待指导教师审核");
        databaseStore.saveRegistration(registration);

        notifyUser(teacher.getId(), "新的竞赛报名待审核", currentUser.getName() + " 报名了《" + competition.getName() + "》，请及时审核。");
        return Result.success(toRegistrationView(registration));
    }

    public Result<?> myRegistrations() {
        User currentUser = currentUser();
        if (!Roles.STUDENT.equals(currentUser.getRole())) {
            throw BusinessException.forbidden("只有学生可以查看自己的报名");
        }
        List<Map<String, Object>> rows = databaseStore.listRegistrationsByUserId(currentUser.getId()).stream()
                .map(this::toRegistrationView)
                .toList();
        return Result.success(rows);
    }

    public Result<?> teacherRegistrations() {
        User currentUser = currentUser();
        if (!Roles.TEACHER.equals(currentUser.getRole()) && !Roles.ADMIN.equals(currentUser.getRole())) {
            throw BusinessException.forbidden("没有权限查看报名信息");
        }
        List<Registration> registrations = Roles.ADMIN.equals(currentUser.getRole())
                ? databaseStore.listRegistrations()
                : databaseStore.listRegistrationsByTeacherId(currentUser.getId());
        List<Map<String, Object>> rows = registrations.stream()
                .map(this::toRegistrationView)
                .toList();
        return Result.success(rows);
    }

    public Result<?> allRegistrations() {
        requireRole(Roles.ADMIN);
        return Result.success(databaseStore.listRegistrations().stream().map(this::toRegistrationView).toList());
    }

    public Result<?> reviewRegistration(Long id, RegistrationReviewDTO registrationReviewDTO) {
        User currentUser = currentUser();
        Registration registration = registrationById(id);
        if (!Roles.ADMIN.equals(currentUser.getRole()) && !registration.getTeacherId().equals(currentUser.getId())) {
            throw BusinessException.forbidden("只能审核自己指导的报名");
        }
        if (registrationReviewDTO == null || !List.of("approved", "rejected").contains(registrationReviewDTO.getStatus())) {
            throw BusinessException.badRequest("审核结果必须为 approved 或 rejected");
        }
        boolean workSubmitted = databaseStore.findWorkByRegistrationId(registration.getId()).isPresent();
        if (workSubmitted && !registration.getStatus().equals(registrationReviewDTO.getStatus())) {
            throw BusinessException.badRequest("该报名已提交作品，不能再变更审核结果");
        }
        registration.setStatus(registrationReviewDTO.getStatus());
        registration.setComment(trim(registrationReviewDTO.getComment()));
        databaseStore.saveRegistration(registration);

        Competition competition = competitionById(registration.getCompetitionId());
        String action = "approved".equals(registration.getStatus()) ? "已通过" : "未通过";
        notifyUser(registration.getUserId(), "报名审核结果", "你提交的《" + competition.getName() + "》报名申请" + action + "。");
        return Result.success(toRegistrationView(registration));
    }

    public Result<?> submitWork(WorkSubmitDTO workSubmitDTO) {
        User currentUser = currentUser();
        if (!Roles.STUDENT.equals(currentUser.getRole())) {
            throw BusinessException.forbidden("只有学生可以提交作品");
        }
        if (workSubmitDTO == null || workSubmitDTO.getRegistrationId() == null || !StringUtils.hasText(workSubmitDTO.getTitle())) {
            throw BusinessException.badRequest("请选择报名记录并填写作品标题");
        }
        Registration registration = registrationById(workSubmitDTO.getRegistrationId());
        if (!registration.getUserId().equals(currentUser.getId())) {
            throw BusinessException.forbidden("不能为他人的报名提交作品");
        }
        if (!"approved".equals(registration.getStatus())) {
            throw BusinessException.badRequest("报名尚未通过，不能提交作品");
        }
        Competition competition = competitionById(registration.getCompetitionId());
        String status = competitionStatus(competition);
        if (!"submission_open".equals(status)) {
            if ("ended".equals(status)) {
                throw BusinessException.badRequest("作品提交已截止");
            }
            throw BusinessException.badRequest("作品提交尚未开放");
        }
        if (LocalDateTime.now().isAfter(competition.getSubmissionEndTime())) {
            throw BusinessException.badRequest("作品提交已截止");
        }
        databaseStore.findWorkByRegistrationId(registration.getId()).ifPresent(work -> {
            throw BusinessException.badRequest("该报名已提交过作品");
        });

        Work work = new Work();
        work.setRegistrationId(registration.getId());
        work.setTitle(workSubmitDTO.getTitle().trim());
        work.setDescription(trim(workSubmitDTO.getDescription()));
        work.setFilePath(fileStorageService.store(workSubmitDTO.getFile(), "works"));
        work.setReviewTeacherId(registration.getTeacherId());
        work.setSubmitTime(LocalDateTime.now());

        Map<String, Object> aiReview = buildAiReview(workSubmitDTO.getTitle(), workSubmitDTO.getDescription());
        work.setAiScore((Integer) aiReview.get("score"));
        work.setAiComment((String) aiReview.get("comment"));
        databaseStore.saveWork(work);

        notifyUser(registration.getTeacherId(), "新的作品待评审", currentUser.getName() + " 提交了作品《" + work.getTitle() + "》，请及时查看。");
        return Result.success(toWorkView(work));
    }

    public Result<?> myWorks() {
        User currentUser = currentUser();
        if (!Roles.STUDENT.equals(currentUser.getRole())) {
            throw BusinessException.forbidden("只有学生可以查看自己的作品");
        }
        List<Long> registrationIds = databaseStore.listRegistrationsByUserId(currentUser.getId()).stream()
                .map(Registration::getId)
                .toList();
        List<Map<String, Object>> rows = databaseStore.listWorksByRegistrationIds(registrationIds).stream()
                .map(this::toWorkView)
                .toList();
        return Result.success(rows);
    }

    public Result<?> teacherWorks() {
        User currentUser = currentUser();
        if (!Roles.TEACHER.equals(currentUser.getRole()) && !Roles.ADMIN.equals(currentUser.getRole())) {
            throw BusinessException.forbidden("没有权限查看作品");
        }
        List<Work> works = Roles.ADMIN.equals(currentUser.getRole()) ? databaseStore.listWorks() : teacherVisibleWorks(currentUser);
        List<Map<String, Object>> rows = works.stream()
                .map(this::toWorkView)
                .toList();
        return Result.success(rows);
    }

    public Result<?> allWorks() {
        requireRole(Roles.ADMIN);
        return Result.success(databaseStore.listWorks().stream().map(this::toWorkView).toList());
    }

    public Result<?> assignReviewer(Long workId, WorkAssignReviewerDTO workAssignReviewerDTO) {
        requireRole(Roles.ADMIN);
        if (workAssignReviewerDTO == null || workAssignReviewerDTO.getReviewerId() == null) {
            throw BusinessException.badRequest("请选择评审教师");
        }
        Work work = workById(workId);
        User reviewer = userById(workAssignReviewerDTO.getReviewerId());
        if (!Roles.TEACHER.equals(reviewer.getRole())) {
            throw BusinessException.badRequest("评审教师不存在");
        }
        work.setReviewTeacherId(reviewer.getId());
        databaseStore.saveWork(work);
        notifyUser(reviewer.getId(), "新的作品评审任务", "你被分配评审作品《" + work.getTitle() + "》。");
        return Result.success(toWorkView(work));
    }

    public Result<?> scoreWork(Long workId, ScoreDTO scoreDTO) {
        User currentUser = currentUser();
        Work work = workById(workId);
        Registration registration = registrationById(work.getRegistrationId());
        if (!Roles.ADMIN.equals(currentUser.getRole())
                && !Objects.equals(work.getReviewTeacherId(), currentUser.getId())
                && !Objects.equals(registration.getTeacherId(), currentUser.getId())) {
            throw BusinessException.forbidden("只能评审自己负责的作品");
        }
        if (scoreDTO == null || scoreDTO.getScore() == null || scoreDTO.getScore() < 0 || scoreDTO.getScore() > 100) {
            throw BusinessException.badRequest("评分必须在 0 到 100 之间");
        }
        Score score = databaseStore.findScoreByWorkAndTeacher(workId, currentUser.getId()).orElseGet(Score::new);
        score.setWorkId(work.getId());
        score.setTeacherId(currentUser.getId());
        score.setScore(scoreDTO.getScore());
        score.setComment(trim(scoreDTO.getComment()));
        databaseStore.saveScore(score);

        notifyUser(registration.getUserId(), "作品评审已更新", "你的作品《" + work.getTitle() + "》收到了新的评审结果。");
        return Result.success(toWorkView(work));
    }

    public Result<?> notifications() {
        User currentUser = currentUser();
        List<Map<String, Object>> rows = databaseStore.listNotificationsByUserId(currentUser.getId()).stream()
                .map(this::toNotificationView)
                .toList();
        return Result.success(rows);
    }

    public Result<?> markNotificationRead(Long id) {
        User currentUser = currentUser();
        Notification notification = databaseStore.findNotificationById(id)
                .orElseThrow(() -> BusinessException.notFound("通知不存在"));
        if (!notification.getUserId().equals(currentUser.getId())) {
            throw BusinessException.forbidden("不能操作他人的通知");
        }
        notification.setReadStatus(true);
        databaseStore.saveNotification(notification);
        return Result.success(toNotificationView(notification));
    }

    public Result<?> createNotification(NotificationCreateDTO notificationCreateDTO) {
        requireRole(Roles.ADMIN);
        if (notificationCreateDTO == null || !StringUtils.hasText(notificationCreateDTO.getTitle()) || !StringUtils.hasText(notificationCreateDTO.getContent())) {
            throw BusinessException.badRequest("通知标题和内容不能为空");
        }
        if (Boolean.TRUE.equals(notificationCreateDTO.getBroadcast()) || notificationCreateDTO.getUserId() == null) {
            broadcastNotification(notificationCreateDTO.getTitle().trim(), notificationCreateDTO.getContent().trim());
            return Result.success(Map.of("broadcast", true, "count", databaseStore.countUsers()));
        }
        userById(notificationCreateDTO.getUserId());
        notifyUser(notificationCreateDTO.getUserId(), notificationCreateDTO.getTitle().trim(), notificationCreateDTO.getContent().trim());
        return Result.success(Map.of("broadcast", false, "count", 1));
    }

    public Result<?> competitionRanking(Long competitionId) {
        Competition competition = competitionById(competitionId);
        boolean allowHidden = false;
        try {
            User currentUser = currentUser();
            allowHidden = Roles.ADMIN.equals(currentUser.getRole()) || Roles.TEACHER.equals(currentUser.getRole());
        } catch (BusinessException ignored) {
            allowHidden = false;
        }
        if (!allowHidden && !Boolean.TRUE.equals(competition.getRankingPublished())) {
            throw BusinessException.forbidden("成绩尚未公布");
        }
        return Result.success(rankingRows(competition, allowHidden));
    }

    public Result<?> publishRanking(Long competitionId) {
        requireRole(Roles.ADMIN);
        Competition competition = competitionById(competitionId);
        competition.setRankingPublished(true);
        databaseStore.saveCompetition(competition);
        broadcastNotification("成绩公布通知", "《" + competition.getName() + "》的成绩与排名已公布。");
        return Result.success(toCompetitionView(competition));
    }

    public Result<?> answerQuestion(AiQuestionDTO aiQuestionDTO) {
        currentUser();
        if (aiQuestionDTO == null || !StringUtils.hasText(aiQuestionDTO.getQuestion())) {
            throw BusinessException.badRequest("请输入问题内容");
        }
        String question = aiQuestionDTO.getQuestion().trim();
        String answer;

        if (question.contains("报名")) {
            long openCompetitions = databaseStore.listCompetitions().stream()
                    .filter(competition -> "registration_open".equals(competitionStatus(competition)))
                    .count();
            answer = "当前共有 " + openCompetitions + " 个竞赛处于报名期。进入“竞赛列表”选择项目后，上传报名表并指定指导教师即可提交申请。";
        } else if (question.contains("作品") || question.contains("提交")) {
            Competition nearest = databaseStore.listCompetitions().stream()
                    .filter(competition -> LocalDateTime.now().isBefore(competition.getSubmissionEndTime()))
                    .min(Comparator.comparing(Competition::getSubmissionEndTime))
                    .orElse(null);
            if (nearest == null) {
                answer = "当前没有正在开放提交的竞赛。你可以先完成报名，等竞赛进入提交通道后再上传作品文件。";
            } else {
                answer = "作品提交需要先通过报名审核。离现在最近的提交截止是《" + nearest.getName() + "》，截止到 " + formatTime(nearest.getSubmissionEndTime()) + "。";
            }
        } else if (question.contains("审核") || question.contains("通过")) {
            answer = "报名先由指导教师审核，作品则由教师打分并填写评语。你可以在“我的报名”和“我的作品”里实时查看状态变化。";
        } else if (question.contains("成绩") || question.contains("排名")) {
            answer = "成绩由教师评分后汇总生成。管理员公布成绩后，系统会按最终得分自动生成排名并向相关用户发送通知。";
        } else {
            answer = "你可以问我报名流程、作品提交、审核规则、成绩公布这些问题。我会结合系统当前竞赛状态给出具体建议。";
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("question", question);
        data.put("answer", answer);
        data.put("suggestions", List.of("报名需要准备什么材料？", "作品提交后在哪里看评审结果？", "成绩什么时候公布？"));
        return Result.success(data);
    }

    private List<Map<String, Object>> recommendedCompetitionViews(User user) {
        return databaseStore.listCompetitions().stream()
                .filter(competition -> !"ended".equals(competitionStatus(competition)))
                .sorted(Comparator.<Competition>comparingInt(competition -> recommendationScore(user, competition)).reversed()
                        .thenComparing(Competition::getRegistrationEndTime))
                .limit(3)
                .map(competition -> {
                    Map<String, Object> data = toCompetitionView(competition);
                    data.put("recommendationScore", recommendationScore(user, competition));
                    data.put("recommendationReason", recommendationReason(user, competition));
                    return data;
                })
                .toList();
    }

    private int recommendationScore(User user, Competition competition) {
        int score = "registration_open".equals(competitionStatus(competition)) ? 20 : 10;
        String text = (competition.getName() + " " + Objects.toString(competition.getDescription(), "")).toLowerCase();
        if (StringUtils.hasText(user.getMajor())) {
            String major = user.getMajor().toLowerCase();
            if (text.contains("软件") && major.contains("软件")) {
                score += 30;
            }
            if (text.contains("数据") && (major.contains("数据") || major.contains("统计"))) {
                score += 30;
            }
            if (text.contains("人工智能") && (major.contains("人工智能") || major.contains("计算机"))) {
                score += 30;
            }
        }
        if ("国家级".equals(competition.getLevel())) {
            score += 15;
        } else if ("省级".equals(competition.getLevel())) {
            score += 10;
        } else {
            score += 5;
        }
        return score;
    }

    private String recommendationReason(User user, Competition competition) {
        if (StringUtils.hasText(user.getMajor()) && Objects.toString(competition.getDescription(), "").contains("数据")) {
            return "竞赛主题和你的专业方向比较接近，适合做课程成果延展。";
        }
        if ("国家级".equals(competition.getLevel())) {
            return "赛事层级较高，适合作为重点参赛项目。";
        }
        return "当前时间窗口合适，适合尽快准备报名材料。";
    }

    private Map<String, Object> buildAiReview(String title, String description) {
        String merged = (Objects.toString(title, "") + " " + Objects.toString(description, "")).toLowerCase();
        int score = 70;
        if (merged.contains("系统") || merged.contains("平台")) {
            score += 8;
        }
        if (merged.contains("数据") || merged.contains("分析")) {
            score += 6;
        }
        if (merged.contains("创新") || merged.contains("优化") || merged.contains("智能")) {
            score += 8;
        }
        if (Objects.toString(description, "").length() > 40) {
            score += 5;
        }
        score = Math.min(score, 95);

        String comment;
        if (score >= 90) {
            comment = "选题清晰且完成度较高，方案表达成熟，建议在展示材料中进一步突出核心创新点与验证结果。";
        } else if (score >= 80) {
            comment = "作品结构完整，业务问题定义比较明确，后续可以继续补充性能指标、测试结果或用户反馈。";
        } else {
            comment = "作品已经具备基本提交条件，建议继续完善问题分析、方案细节和结果展示，让亮点更集中。";
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("score", score);
        data.put("comment", comment);
        return data;
    }

    private void validateCompetition(CompetitionDTO competitionDTO) {
        if (competitionDTO == null
                || !StringUtils.hasText(competitionDTO.getName())
                || competitionDTO.getRegistrationStartTime() == null
                || competitionDTO.getRegistrationEndTime() == null
                || competitionDTO.getSubmissionEndTime() == null) {
            throw BusinessException.badRequest("竞赛名称和时间不能为空");
        }
        if (competitionDTO.getRegistrationStartTime().isAfter(competitionDTO.getRegistrationEndTime())) {
            throw BusinessException.badRequest("报名开始时间不能晚于报名截止时间");
        }
        if (competitionDTO.getRegistrationEndTime().isAfter(competitionDTO.getSubmissionEndTime())) {
            throw BusinessException.badRequest("作品截止时间不能早于报名截止时间");
        }
    }

    private List<Work> teacherVisibleWorks(User currentUser) {
        List<Long> guidedRegistrationIds = databaseStore.listRegistrationsByTeacherId(currentUser.getId()).stream()
                .map(Registration::getId)
                .toList();
        Map<Long, Work> works = new LinkedHashMap<>();
        databaseStore.listWorksByReviewTeacherId(currentUser.getId())
                .forEach(work -> works.put(work.getId(), work));
        databaseStore.listWorksByRegistrationIds(guidedRegistrationIds)
                .forEach(work -> works.put(work.getId(), work));
        return works.values().stream()
                .sorted(Comparator.comparing(Work::getId).reversed())
                .toList();
    }

    private List<User> teacherStudents(Long teacherId) {
        List<Long> studentIds = databaseStore.listRegistrationsByTeacherId(teacherId).stream()
                .map(Registration::getUserId)
                .distinct()
                .toList();
        return studentIds.stream().map(this::userById).toList();
    }

    private Map<String, Object> toCompetitionView(Competition competition) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", competition.getId());
        data.put("name", competition.getName());
        data.put("description", competition.getDescription());
        data.put("organizer", competition.getOrganizer());
        data.put("level", competition.getLevel());
        data.put("registrationStartTime", competition.getRegistrationStartTime());
        data.put("registrationEndTime", competition.getRegistrationEndTime());
        data.put("submissionEndTime", competition.getSubmissionEndTime());
        data.put("status", competitionStatus(competition));
        data.put("statusLabel", competitionStatusLabel(competition));
        data.put("rankingPublished", Boolean.TRUE.equals(competition.getRankingPublished()));
        data.put("registrationCount", databaseStore.countRegistrationsByCompetitionId(competition.getId()));
        return data;
    }

    private Map<String, Object> toRegistrationView(Registration registration) {
        Competition competition = competitionById(registration.getCompetitionId());
        User student = userById(registration.getUserId());
        User teacher = userById(registration.getTeacherId());
        boolean workSubmitted = databaseStore.findWorkByRegistrationId(registration.getId()).isPresent();
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", registration.getId());
        data.put("competitionId", competition.getId());
        data.put("competitionName", competition.getName());
        data.put("competitionLevel", competition.getLevel());
        data.put("studentId", student.getId());
        data.put("studentName", student.getName());
        data.put("studentUsername", student.getUsername());
        data.put("teacherId", teacher.getId());
        data.put("teacherName", teacher.getName());
        data.put("teamName", registration.getTeamName());
        data.put("teamMembers", registration.getTeamMembers());
        data.put("applicationForm", registration.getApplicationForm());
        data.put("status", registration.getStatus());
        data.put("statusLabel", registrationStatusLabel(registration.getStatus()));
        data.put("comment", registration.getComment());
        data.put("createTime", registration.getCreateTime());
        data.put("workSubmitted", workSubmitted);
        data.put("canSubmitWork", "approved".equals(registration.getStatus())
                && !workSubmitted
                && "submission_open".equals(competitionStatus(competition)));
        return data;
    }

    private Map<String, Object> toWorkView(Work work) {
        Registration registration = registrationById(work.getRegistrationId());
        Competition competition = competitionById(registration.getCompetitionId());
        User student = userById(registration.getUserId());
        User guideTeacher = userById(registration.getTeacherId());
        User reviewTeacher = work.getReviewTeacherId() == null ? null : userById(work.getReviewTeacherId());
        List<Score> teacherScores = databaseStore.listScoresByWorkId(work.getId());

        Double averageScore = teacherScores.stream().mapToInt(Score::getScore).average().orElse(Double.NaN);
        Integer finalScore = teacherScores.isEmpty() ? work.getAiScore() : (int) Math.round(averageScore);
        String latestComment = teacherScores.isEmpty() ? work.getAiComment() : teacherScores.get(teacherScores.size() - 1).getComment();

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", work.getId());
        data.put("registrationId", registration.getId());
        data.put("competitionId", competition.getId());
        data.put("competitionName", competition.getName());
        data.put("studentName", student.getName());
        data.put("studentUsername", student.getUsername());
        data.put("guideTeacherName", guideTeacher.getName());
        data.put("reviewTeacherId", work.getReviewTeacherId());
        data.put("reviewTeacherName", reviewTeacher == null ? null : reviewTeacher.getName());
        data.put("title", work.getTitle());
        data.put("description", work.getDescription());
        data.put("filePath", work.getFilePath());
        data.put("submitTime", work.getSubmitTime());
        data.put("aiScore", work.getAiScore());
        data.put("aiComment", work.getAiComment());
        data.put("finalScore", finalScore);
        data.put("finalComment", latestComment);
        data.put("scores", teacherScores.stream().map(score -> {
            User teacher = userById(score.getTeacherId());
            Map<String, Object> scoreView = new LinkedHashMap<>();
            scoreView.put("id", score.getId());
            scoreView.put("teacherId", teacher.getId());
            scoreView.put("teacherName", teacher.getName());
            scoreView.put("score", score.getScore());
            scoreView.put("comment", score.getComment());
            scoreView.put("updateTime", score.getUpdateTime());
            return scoreView;
        }).toList());
        return data;
    }

    private Map<String, Object> toNotificationView(Notification notification) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", notification.getId());
        data.put("title", notification.getTitle());
        data.put("content", notification.getContent());
        data.put("read", Boolean.TRUE.equals(notification.getReadStatus()));
        data.put("createTime", notification.getCreateTime());
        return data;
    }

    private List<Map<String, Object>> rankingRows(Competition competition, boolean includeHidden) {
        if (!includeHidden && !Boolean.TRUE.equals(competition.getRankingPublished())) {
            throw BusinessException.forbidden("成绩尚未公布");
        }

        List<Map<String, Object>> rows = new ArrayList<>();
        List<Registration> registrations = databaseStore.listRegistrationsByCompetitionId(competition.getId()).stream()
                .filter(registration -> "approved".equals(registration.getStatus()))
                .toList();

        for (Registration registration : registrations) {
            Optional<Work> workOptional = databaseStore.findWorkByRegistrationId(registration.getId());
            if (workOptional.isEmpty()) {
                continue;
            }
            Work work = workOptional.get();
            Map<String, Object> workView = toWorkView(work);
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("registrationId", registration.getId());
            row.put("studentName", userById(registration.getUserId()).getName());
            row.put("teamName", registration.getTeamName());
            row.put("title", work.getTitle());
            row.put("finalScore", workView.get("finalScore"));
            row.put("finalComment", workView.get("finalComment"));
            rows.add(row);
        }

        rows.sort((a, b) -> Integer.compare((Integer) b.get("finalScore"), (Integer) a.get("finalScore")));
        for (int i = 0; i < rows.size(); i++) {
            rows.get(i).put("rank", i + 1);
        }
        return rows;
    }

    private String competitionStatus(Competition competition) {
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(competition.getRegistrationStartTime())) {
            return "upcoming";
        }
        if (!now.isAfter(competition.getRegistrationEndTime())) {
            return "registration_open";
        }
        if (!now.isAfter(competition.getSubmissionEndTime())) {
            return "submission_open";
        }
        return "ended";
    }

    private String competitionStatusLabel(Competition competition) {
        return switch (competitionStatus(competition)) {
            case "upcoming" -> "即将开始";
            case "registration_open" -> "报名中";
            case "submission_open" -> "作品提交中";
            default -> "已结束";
        };
    }

    private String registrationStatusLabel(String status) {
        return switch (status) {
            case "approved" -> "已通过";
            case "rejected" -> "未通过";
            default -> "待审核";
        };
    }

    private void notifyUser(Long userId, String title, String content) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setReadStatus(false);
        databaseStore.saveNotification(notification);
    }

    private void broadcastNotification(String title, String content) {
        databaseStore.listUsers().forEach(user -> notifyUser(user.getId(), title, content));
    }

    private User currentUser() {
        User user = AuthContext.getUser();
        if (user == null) {
            throw BusinessException.unauthorized("请先登录");
        }
        return user;
    }

    private void requireRole(String role) {
        if (!role.equals(currentUser().getRole())) {
            throw BusinessException.forbidden("没有权限执行该操作");
        }
    }

    private Competition competitionById(Long id) {
        return databaseStore.findCompetitionById(id)
                .orElseThrow(() -> BusinessException.notFound("竞赛不存在"));
    }

    private Registration registrationById(Long id) {
        return databaseStore.findRegistrationById(id)
                .orElseThrow(() -> BusinessException.notFound("报名记录不存在"));
    }

    private Work workById(Long id) {
        return databaseStore.findWorkById(id)
                .orElseThrow(() -> BusinessException.notFound("作品不存在"));
    }

    private User userById(Long id) {
        return databaseStore.findUserById(id)
                .orElseThrow(() -> BusinessException.notFound("用户不存在"));
    }

    private String trim(String text) {
        return text == null ? null : text.trim();
    }

    private String formatTime(LocalDateTime time) {
        return time == null ? "" : DATE_TIME_FORMATTER.format(time);
    }
}
