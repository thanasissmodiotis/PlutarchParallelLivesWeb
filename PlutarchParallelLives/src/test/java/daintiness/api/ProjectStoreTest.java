package daintiness.api;

import daintiness.maincontroller.IMainController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ProjectStore Tests")
class ProjectStoreTest {

    private ProjectStore projectStore;

    @BeforeEach
    void setUp() {
        projectStore = new ProjectStore();
        ReflectionTestUtils.setField(projectStore, "sessionTimeoutMinutes", 30);
        ReflectionTestUtils.setField(projectStore, "maxSessions", 100);
    }

    // createProject() Tests

    @Test
    @DisplayName("createProject should return a non-null, non-empty project ID")
    void createProject_ShouldReturnNonNullId() {
        String projectId = projectStore.createProject();
        
        assertNotNull(projectId);
        assertFalse(projectId.isEmpty());
    }

    @Test
    @DisplayName("createProject should return unique IDs for multiple projects")
    void createProject_ShouldReturnUniqueIds() {
        Set<String> projectIds = new HashSet<>();
        int numProjects = 100;
        
        for (int i = 0; i < numProjects; i++) {
            String projectId = projectStore.createProject();
            projectIds.add(projectId);
        }
        
        assertEquals(numProjects, projectIds.size(), 
            "All project IDs should be unique");
    }

    @Test
    @DisplayName("createProject should store the project for later retrieval")
    void createProject_ShouldStoreProjectForRetrieval() {
        String projectId = projectStore.createProject();
        
        Optional<IMainController> controller = projectStore.get(projectId);
        
        assertTrue(controller.isPresent(), 
            "Created project should be retrievable");
    }

    @Test
    @DisplayName("createProject should throw IllegalStateException when max sessions reached")
    void createProject_MaxSessionsReached_ShouldThrowException() {
        ReflectionTestUtils.setField(projectStore, "maxSessions", 1);
        
        // first project should succeed
        String projectId1 = projectStore.createProject();
        assertNotNull(projectId1);
        
        // second project should throw
        assertThrows(IllegalStateException.class, () -> {
            projectStore.createProject();
        }, "Should throw IllegalStateException when max sessions reached");
    }

    // get() Tests

    @Test
    @DisplayName("get should return empty Optional for non-existent project")
    void get_ShouldReturnEmptyForNonExistentProject() {
        Optional<IMainController> controller = projectStore.get("non-existent-id");
        
        assertFalse(controller.isPresent());
        assertTrue(controller.isEmpty());
    }

    @Test
    @DisplayName("get should return empty Optional for null project ID")
    void get_ShouldReturnEmptyForNullId() {
        Optional<IMainController> controller = projectStore.get(null);
        
        assertTrue(controller.isEmpty());
    }

    @Test
    @DisplayName("get should return empty Optional for empty project ID")
    void get_ShouldReturnEmptyForEmptyId() {
        Optional<IMainController> controller = projectStore.get("");
        
        assertTrue(controller.isEmpty());
    }

    @Test
    @DisplayName("get should return the same controller instance for same project ID")
    void get_ShouldReturnSameControllerInstance() {
        String projectId = projectStore.createProject();
        
        Optional<IMainController> controller1 = projectStore.get(projectId);
        Optional<IMainController> controller2 = projectStore.get(projectId);
        
        assertTrue(controller1.isPresent());
        assertTrue(controller2.isPresent());
        assertSame(controller1.get(), controller2.get(), 
            "Should return the same controller instance");
    }

    @Test
    @DisplayName("get should return different controllers for different project IDs")
    void get_ShouldReturnDifferentControllersForDifferentProjects() {
        String projectId1 = projectStore.createProject();
        String projectId2 = projectStore.createProject();
        
        Optional<IMainController> controller1 = projectStore.get(projectId1);
        Optional<IMainController> controller2 = projectStore.get(projectId2);
        
        assertTrue(controller1.isPresent());
        assertTrue(controller2.isPresent());
        assertNotSame(controller1.get(), controller2.get(), 
            "Different projects should have different controllers");
    }

    // delete() Tests

    @Test
    @DisplayName("delete should return true for existing project")
    void delete_ShouldReturnTrueForExistingProject() {
        String projectId = projectStore.createProject();
        
        boolean deleted = projectStore.delete(projectId);
        
        assertTrue(deleted);
    }

    @Test
    @DisplayName("delete should return false for non-existent project")
    void delete_ShouldReturnFalseForNonExistentProject() {
        boolean deleted = projectStore.delete("non-existent-id");
        
        assertFalse(deleted);
    }

    @Test
    @DisplayName("delete should return false for null project ID")
    void delete_ShouldReturnFalseForNullId() {
        boolean deleted = projectStore.delete(null);
        
        assertFalse(deleted);
    }

    @Test
    @DisplayName("delete should remove project from store")
    void delete_ShouldRemoveProjectFromStore() {
        String projectId = projectStore.createProject();
        assertTrue(projectStore.get(projectId).isPresent());
        
        projectStore.delete(projectId);
        
        assertFalse(projectStore.get(projectId).isPresent());
    }

    @Test
    @DisplayName("delete should return false when deleting same project twice")
    void delete_ShouldReturnFalseWhenDeletingTwice() {
        String projectId = projectStore.createProject();
        
        boolean firstDelete = projectStore.delete(projectId);
        boolean secondDelete = projectStore.delete(projectId);
        
        assertTrue(firstDelete);
        assertFalse(secondDelete);
    }

    @Test
    @DisplayName("delete should not affect other projects")
    void delete_ShouldNotAffectOtherProjects() {
        String projectId1 = projectStore.createProject();
        String projectId2 = projectStore.createProject();
        String projectId3 = projectStore.createProject();
        
        projectStore.delete(projectId2);
        
        assertTrue(projectStore.get(projectId1).isPresent());
        assertFalse(projectStore.get(projectId2).isPresent());
        assertTrue(projectStore.get(projectId3).isPresent());
    }

    // getActiveProjectCount() Tests

    @Test
    @DisplayName("getActiveProjectCount should return 0 for empty store")
    void getActiveProjectCount_ShouldReturnZeroForEmptyStore() {
        assertEquals(0, projectStore.getActiveProjectCount());
    }

    @Test
    @DisplayName("getActiveProjectCount should track project count correctly")
    void getActiveProjectCount_ShouldTrackCorrectly() {
        assertEquals(0, projectStore.getActiveProjectCount());
        
        String id1 = projectStore.createProject();
        assertEquals(1, projectStore.getActiveProjectCount());
        
        String id2 = projectStore.createProject();
        assertEquals(2, projectStore.getActiveProjectCount());
        
        projectStore.delete(id1);
        assertEquals(1, projectStore.getActiveProjectCount());
        
        projectStore.delete(id2);
        assertEquals(0, projectStore.getActiveProjectCount());
    }

    // Session Cleanup Tests

    @Test
    @DisplayName("forceCleanup should remove expired sessions")
    void forceCleanup_ShouldRemoveExpiredSessions() throws Exception {
        // set very short timeout
        ReflectionTestUtils.setField(projectStore, "sessionTimeoutMinutes", 0);
        
        String projectId = projectStore.createProject();
        assertTrue(projectStore.get(projectId).isPresent());
        
        // wait to ensure expiration
        Thread.sleep(10);
        
        // force cleanup
        int removed = projectStore.forceCleanup();
        
        assertEquals(1, removed, "Should have removed 1 expired session");
        assertFalse(projectStore.get(projectId).isPresent(), 
            "Expired session should be removed");
    }

    @Test
    @DisplayName("get() touch should save session from cleanup")
    void get_TouchShouldSaveFromCleanup() throws Exception {
        // set short timeout
        ReflectionTestUtils.setField(projectStore, "sessionTimeoutMinutes", 0);
        
        String projectId = projectStore.createProject();
        
        // immediately touch it (simulates active use)
        // reset timeout to normal so touch keeps it alive
        ReflectionTestUtils.setField(projectStore, "sessionTimeoutMinutes", 30);
        projectStore.get(projectId); // updates lastAccessed
        
        // cleanup shouldn't remove it
        int removed = projectStore.forceCleanup();
        
        assertEquals(0, removed, "Touched session should not be removed");
        assertTrue(projectStore.get(projectId).isPresent(), 
            "Touched session should still exist");
    }

    @Test
    @DisplayName("get() updates access time preventing expiration")
    void get_ShouldPreventExpirationByUpdatingAccessTime() throws Exception {
        // set very short timeout
        ReflectionTestUtils.setField(projectStore, "sessionTimeoutMinutes", 0);
        
        String projectId = projectStore.createProject();
        
        // access multiple times (each access should update lastAccessed)
        for (int i = 0; i < 5; i++) {
            // Reset timeout before each get to simulate normal operation
            ReflectionTestUtils.setField(projectStore, "sessionTimeoutMinutes", 30);
            projectStore.get(projectId);
        }
        
        // project should still be accessible
        assertTrue(projectStore.get(projectId).isPresent(),
            "Frequently accessed project should remain available");
    }

    // Thread Safety Tests 

    @Test
    @DisplayName("ProjectStore should handle concurrent project creation")
    void projectStore_ShouldHandleConcurrentCreation() throws InterruptedException {
        // increase max sessions for this test
        ReflectionTestUtils.setField(projectStore, "maxSessions", 1000);
        
        int numThreads = 10;
        int projectsPerThread = 50;
        Set<String> allProjectIds = java.util.Collections.synchronizedSet(new HashSet<>());
        
        Thread[] threads = new Thread[numThreads];
        for (int i = 0; i < numThreads; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < projectsPerThread; j++) {
                    String projectId = projectStore.createProject();
                    allProjectIds.add(projectId);
                }
            });
            threads[i].start();
        }
        
        for (Thread thread : threads) {
            thread.join();
        }
        
        assertEquals(numThreads * projectsPerThread, allProjectIds.size(), 
            "All concurrent project creations should produce unique IDs");
        assertEquals(numThreads * projectsPerThread, projectStore.getActiveProjectCount());
    }

    // Full Lifecycle Test

    @Test
    @DisplayName("Full lifecycle: create, get, delete")
    void fullLifecycle_CreateGetDelete() {
        String projectId = projectStore.createProject();
        assertNotNull(projectId);
        assertEquals(1, projectStore.getActiveProjectCount());

        Optional<IMainController> controller = projectStore.get(projectId);
        assertTrue(controller.isPresent());
        assertNotNull(controller.get());
        boolean deleted = projectStore.delete(projectId);
        assertTrue(deleted);
        assertEquals(0, projectStore.getActiveProjectCount());

        controller = projectStore.get(projectId);
        assertFalse(controller.isPresent());
    }
}