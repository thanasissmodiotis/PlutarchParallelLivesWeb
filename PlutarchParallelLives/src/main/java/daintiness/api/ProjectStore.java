package daintiness.api;

import daintiness.maincontroller.IMainController;
import daintiness.maincontroller.MainControllerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
@EnableScheduling
public class ProjectStore {

    private static final Logger logger = LoggerFactory.getLogger(ProjectStore.class);

    private static class ProjectSession {
        final IMainController controller;
        volatile long lastAccessTime;

        ProjectSession(IMainController controller) {
            this.controller = controller;
            this.lastAccessTime = System.currentTimeMillis();
        }

        void touch() {
            this.lastAccessTime = System.currentTimeMillis();
        }

        boolean isExpired(long timeoutMillis) {
            return System.currentTimeMillis() - lastAccessTime > timeoutMillis;
        }
    }

    private final ConcurrentMap<String, ProjectSession> projects = new ConcurrentHashMap<>();
    private final MainControllerFactory factory = new MainControllerFactory();

    @Value("${daintiness.session.timeout-minutes:30}")
    private int sessionTimeoutMinutes;

    @Value("${daintiness.session.max-sessions:100}")
    private int maxSessions;

    @PostConstruct
    public void init() {
        logger.info("ProjectStore initialized with session timeout: {} minutes, max sessions: {}", 
                    sessionTimeoutMinutes, maxSessions);
    }

    public String createProject() {
        int effectiveMaxSessions = maxSessions > 0 ? maxSessions : Integer.MAX_VALUE;
        if (projects.size() >= effectiveMaxSessions) {
            logger.warn("Max sessions limit reached ({}/{}). Rejecting new project creation.", 
                       projects.size(), effectiveMaxSessions);
            throw new IllegalStateException("Server is busy. Please try again later.");
        }
        
        String id = UUID.randomUUID().toString();
        IMainController controller = factory.getMainController("SIMPLE_MAIN_CONTROLLER");
        projects.put(id, new ProjectSession(controller));
        logger.info("Created project: {} (active sessions: {}/{})", id, projects.size(), effectiveMaxSessions);
        return id;
    }

    public Optional<IMainController> get(String projectId) {
        if (projectId == null || projectId.isEmpty()) {
            return Optional.empty();
        }
        ProjectSession session = projects.get(projectId);
        if (session != null) {
            session.touch();
            return Optional.of(session.controller);
        }
        return Optional.empty();
    }

    public boolean delete(String projectId) {
        if (projectId == null) {
            return false;
        }
        boolean removed = projects.remove(projectId) != null;
        if (removed) {
            logger.debug("Deleted project: {}", projectId);
        }
        return removed;
    }

    public int getActiveProjectCount() {
        return projects.size();
    }

    @Scheduled(fixedRate = 300000) // 5 minutes
    public void cleanupExpiredSessions() {
        long timeoutMillis = sessionTimeoutMinutes * 60 * 1000L;
        int before = projects.size();
        
        projects.entrySet().removeIf(entry -> {
            if (entry.getValue().isExpired(timeoutMillis)) {
                logger.info("Removing expired session: {}", entry.getKey());
                return true;
            }
            return false;
        });
        
        int removed = before - projects.size();
        if (removed > 0) {
            logger.info("Cleaned up {} expired sessions. Active sessions: {}", removed, projects.size());
        }
    }

    public int forceCleanup() {
        int before = projects.size();
        cleanupExpiredSessions();
        return before - projects.size();
    }
}
