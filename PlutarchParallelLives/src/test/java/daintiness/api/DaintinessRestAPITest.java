package daintiness.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DaintinessRestAPITest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProjectStore projectStore;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String TEST_TSV_PATH = "src/test/resources/test_data.tsv";

    // PROJECT MANAGEMENT TESTS 

    @Test
    @Order(1)
    @DisplayName("POST /api/projects - Should create new project with valid JSON contract")
    void createProject_ShouldReturnProjectId() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/projects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projectId").exists())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        JsonNode json = objectMapper.readTree(response);
        String projectId = json.get("projectId").asText();

        assertNotNull(projectId);
        assertFalse(projectId.isEmpty());
        assertTrue(projectStore.get(projectId).isPresent());

        projectStore.delete(projectId);
    }

    @Test
    @Order(2)
    @DisplayName("POST /api/projects - Should return 503 when max sessions reached")
    void createProject_MaxSessionsReached_ShouldReturn503() throws Exception {
        int originalMaxSessions = (int) ReflectionTestUtils.getField(projectStore, "maxSessions");
        
        try {
            ReflectionTestUtils.setField(projectStore, "maxSessions", 1);
            
            MvcResult result1 = mockMvc.perform(post("/api/projects"))
                    .andExpect(status().isOk())
                    .andReturn();
            String projectId1 = objectMapper.readTree(
                    result1.getResponse().getContentAsString()
            ).get("projectId").asText();

            mockMvc.perform(post("/api/projects"))
                    .andExpect(status().isServiceUnavailable())
                    .andExpect(jsonPath("$.error").exists());
            
            projectStore.delete(projectId1);
        } finally {
            ReflectionTestUtils.setField(projectStore, "maxSessions", originalMaxSessions);
        }
    }

    // FILE LOADING TESTS 

    @Test
    @Order(10)
    @DisplayName("POST /api/projects/{id}/load - Should load TSV file")
    void loadFile_ValidTsv_ShouldSucceed() throws Exception {
        String projectId = createProject();

        byte[] fileContent = Files.readAllBytes(Paths.get(TEST_TSV_PATH));
        MockMultipartFile file = new MockMultipartFile(
                "file", "test_data.tsv", "text/tab-separated-values", fileContent
        );

        MvcResult loadResult = mockMvc.perform(
                multipart("/api/projects/" + projectId + "/load").file(file)
        ).andReturn();
        
        assertEquals(200, loadResult.getResponse().getStatus());

        JsonNode json = objectMapper.readTree(loadResult.getResponse().getContentAsString());
        assertTrue(json.get("success").asBoolean());
        assertEquals(3, json.get("numberOfBeats").asInt());
        assertEquals(2, json.get("numberOfEntities").asInt());

        projectStore.delete(projectId);
    }

    @Test
    @Order(11)
    @DisplayName("POST /api/projects/{id}/load - Should return 404 for non-existent project")
    void loadFile_NonExistentProject_ShouldReturn404() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.tsv", "text/tab-separated-values", "test".getBytes()
        );

        mockMvc.perform(
                multipart("/api/projects/non-existent-id/load").file(file)
        ).andExpect(status().isNotFound());
    }

    // CLUSTERING TESTS 

    @Test
    @Order(20)
    @DisplayName("POST /api/projects/{id}/cluster - Should cluster data")
    void clusterData_ValidRequest_ShouldSucceed() throws Exception {
        String projectId = createAndLoadProject();

        String clusterRequest = "{" +
                "\"numberOfPhases\": 2," +
                "\"numberOfEntityGroups\": 2," +
                "\"changesWeight\": 0.5," +
                "\"timeClusteringEnabled\": true," +
                "\"entityClusteringEnabled\": true" +
                "}";

        mockMvc.perform(
                post("/api/projects/" + projectId + "/cluster")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(clusterRequest)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.numberOfPhases").exists())
                .andExpect(jsonPath("$.numberOfEntityGroups").exists());

        projectStore.delete(projectId);
    }

    @Test
    @Order(21)
    @DisplayName("POST /api/projects/{id}/cluster - Should reject invalid numberOfPhases (0)")
    void clusterData_InvalidPhases_ShouldReturn400() throws Exception {
        String projectId = createAndLoadProject();

        String clusterRequest = "{" +
                "\"numberOfPhases\": 0," +
                "\"numberOfEntityGroups\": 2," +
                "\"changesWeight\": 0.5," +
                "\"timeClusteringEnabled\": true," +
                "\"entityClusteringEnabled\": true" +
                "}";

        mockMvc.perform(
                post("/api/projects/" + projectId + "/cluster")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(clusterRequest)
        )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));

        projectStore.delete(projectId);
    }

    @Test
    @Order(22)
    @DisplayName("POST /api/projects/{id}/cluster - Should reject invalid numberOfEntityGroups (0)")
    void clusterData_InvalidEntityGroups_ShouldReturn400() throws Exception {
        String projectId = createAndLoadProject();

        String clusterRequest = "{" +
                "\"numberOfPhases\": 2," +
                "\"numberOfEntityGroups\": 0," +
                "\"changesWeight\": 0.5," +
                "\"timeClusteringEnabled\": true," +
                "\"entityClusteringEnabled\": true" +
                "}";

        mockMvc.perform(
                post("/api/projects/" + projectId + "/cluster")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(clusterRequest)
        )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));

        projectStore.delete(projectId);
    }

    @Test
    @Order(23)
    @DisplayName("POST /api/projects/{id}/cluster - Should reject invalid changesWeight (-0.1)")
    void clusterData_InvalidChangesWeight_ShouldReturn400() throws Exception {
        String projectId = createAndLoadProject();

        String clusterRequest = "{" +
                "\"numberOfPhases\": 2," +
                "\"numberOfEntityGroups\": 2," +
                "\"changesWeight\": -0.1," +
                "\"timeClusteringEnabled\": true," +
                "\"entityClusteringEnabled\": true" +
                "}";

        mockMvc.perform(
                post("/api/projects/" + projectId + "/cluster")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(clusterRequest)
        )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));

        projectStore.delete(projectId);
    }

    @Test
    @Order(24)
    @DisplayName("POST /api/projects/{id}/cluster - Should reject changesWeight > 1.0")
    void clusterData_ChangesWeightTooHigh_ShouldReturn400() throws Exception {
        String projectId = createAndLoadProject();

        String clusterRequest = "{" +
                "\"numberOfPhases\": 2," +
                "\"numberOfEntityGroups\": 2," +
                "\"changesWeight\": 1.5," +
                "\"timeClusteringEnabled\": true," +
                "\"entityClusteringEnabled\": true" +
                "}";

        mockMvc.perform(
                post("/api/projects/" + projectId + "/cluster")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(clusterRequest)
        )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));

        projectStore.delete(projectId);
    }

    // CHART DATA TESTS 
    @Test
    @Order(30)
    @DisplayName("GET /api/projects/{id}/chartData - Should return chart data after clustering")
    void getChartData_AfterClustering_ShouldSucceed() throws Exception {
        String projectId = createLoadAndClusterProject();

        mockMvc.perform(get("/api/projects/" + projectId + "/chartData"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.chartData").isArray())
                .andExpect(jsonPath("$.phases").isArray())
                .andExpect(jsonPath("$.entityGroups").isArray());

        projectStore.delete(projectId);
    }

    @Test
    @Order(31)
    @DisplayName("GET /api/projects/{id}/chartData - Should return 400 before clustering")
    void getChartData_BeforeClustering_ShouldReturn400() throws Exception {
        String projectId = createAndLoadProject();

        mockMvc.perform(get("/api/projects/" + projectId + "/chartData"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());

        projectStore.delete(projectId);
    }

    @Test
    @Order(32)
    @DisplayName("GET /api/projects/{id}/chartData - Should return 404 for non-existent project")
    void getChartData_NonExistentProject_ShouldReturn404() throws Exception {
        mockMvc.perform(get("/api/projects/non-existent-id/chartData"))
                .andExpect(status().isNotFound());
    }

    // SORTING TESTS 

    @Test
    @Order(40)
    @DisplayName("POST /api/projects/{id}/sort - Should sort chart data")
    void sortChartData_ValidSortType_ShouldSucceed() throws Exception {
        String projectId = createLoadAndClusterProject();

        mockMvc.perform(
                post("/api/projects/" + projectId + "/sort")
                        .param("sortType", "BIRTH_ASCENDING")
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        projectStore.delete(projectId);
    }

    @Test
    @Order(41)
    @DisplayName("POST /api/projects/{id}/sort - Should reject invalid sort type")
    void sortChartData_InvalidSortType_ShouldReturn400() throws Exception {
        String projectId = createLoadAndClusterProject();

        mockMvc.perform(
                post("/api/projects/" + projectId + "/sort")
                        .param("sortType", "INVALID_SORT")
        )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());

        projectStore.delete(projectId);
    }

    @Test
    @Order(42)
    @DisplayName("POST /api/projects/{id}/sort - Should return 400 before clustering")
    void sortChartData_BeforeClustering_ShouldReturn400() throws Exception {
        String projectId = createAndLoadProject();

        mockMvc.perform(
                post("/api/projects/" + projectId + "/sort")
                        .param("sortType", "BIRTH_ASCENDING")
        )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());

        projectStore.delete(projectId);
    }

    // PATTERNS TESTS 

    @Test
    @Order(50)
    @DisplayName("GET /api/projects/{id}/patterns - Should return patterns list")
    void getPatterns_ValidType_ShouldReturnList() throws Exception {
        String projectId = createLoadAndClusterProject();

        mockMvc.perform(
                get("/api/projects/" + projectId + "/patterns")
                        .param("patternType", "NO_TYPE")
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        projectStore.delete(projectId);
    }

    @Test
    @Order(51)
    @DisplayName("GET /api/projects/{id}/patterns - Should return 400 for invalid patternType")
    void getPatterns_InvalidType_ShouldReturn400() throws Exception {
        String projectId = createLoadAndClusterProject();

        mockMvc.perform(
                get("/api/projects/" + projectId + "/patterns")
                        .param("patternType", "INVALID_PATTERN_TYPE")
        )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());

        projectStore.delete(projectId);
    }

    // CELL DETAILS TESTS 

    @Test
    @Order(55)
    @DisplayName("GET /api/projects/{id}/cellDetails - Should return cell details")
    void getCellDetails_ValidCell_ShouldSucceed() throws Exception {
        String projectId = createLoadAndClusterProject();

        mockMvc.perform(
                get("/api/projects/" + projectId + "/cellDetails")
                        .param("entityGroupId", "0")
                        .param("phaseId", "0")
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.entityGroupId").exists())
                .andExpect(jsonPath("$.phaseId").exists())
                .andExpect(jsonPath("$.value").exists());

        projectStore.delete(projectId);
    }

    @Test
    @Order(56)
    @DisplayName("GET /api/projects/{id}/cellDetails - Should return 404 for non-existent project")
    void getCellDetails_NonExistentProject_ShouldReturn404() throws Exception {
        mockMvc.perform(
                get("/api/projects/non-existent-id/cellDetails")
                        .param("entityGroupId", "0")
                        .param("phaseId", "0")
        )
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(57)
    @DisplayName("GET /api/projects/{id}/cellDetails - Should return 400 for invalid indices")
    void getCellDetails_InvalidIndices_ShouldReturn400() throws Exception {
        String projectId = createLoadAndClusterProject();

        mockMvc.perform(
                get("/api/projects/" + projectId + "/cellDetails")
                        .param("entityGroupId", "999")
                        .param("phaseId", "999")
        )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());

        projectStore.delete(projectId);
    }

    @Test
    @Order(58)
    @DisplayName("GET /api/projects/{id}/cellDetails - Should return 400 before clustering")
    void getCellDetails_BeforeClustering_ShouldReturn400() throws Exception {
        String projectId = createAndLoadProject();

        mockMvc.perform(
                get("/api/projects/" + projectId + "/cellDetails")
                        .param("entityGroupId", "0")
                        .param("phaseId", "0")
        )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());

        projectStore.delete(projectId);
    }

    // EXPORT TESTS 

    @Test
    @Order(60)
    @DisplayName("POST /api/projects/{id}/exportProject - Should export project as ZIP")
    void exportProject_AfterClustering_ShouldSucceed() throws Exception {
        String projectId = createLoadAndClusterProject();

        mockMvc.perform(post("/api/projects/" + projectId + "/exportProject"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", 
                        org.hamcrest.Matchers.containsString("attachment")));

        projectStore.delete(projectId);
    }

    @Test
    @Order(61)
    @DisplayName("POST /api/projects/{id}/exportProject - Should return 400 before clustering")
    void exportProject_BeforeClustering_ShouldReturn400() throws Exception {
        String projectId = createAndLoadProject();

        mockMvc.perform(post("/api/projects/" + projectId + "/exportProject"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());

        projectStore.delete(projectId);
    }

    @Test
    @Order(62)
    @DisplayName("POST /api/projects/{id}/saveData - Should save data as TSV")
    void saveData_WithLoadedData_ShouldSucceed() throws Exception {
        String projectId = createAndLoadProject();

        mockMvc.perform(post("/api/projects/" + projectId + "/saveData"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition",
                        org.hamcrest.Matchers.containsString("attachment")));

        projectStore.delete(projectId);
    }

    // SESSION STATS TESTS 

    @Test
    @Order(70)
    @DisplayName("GET /api/sessions/stats - Should return correct active project count")
    void getSessionStats_ShouldReturnCorrectCount() throws Exception {
        // Create 2 projects
        String projectId1 = createProject();
        String projectId2 = createProject();

        try {
            MvcResult result = mockMvc.perform(get("/api/sessions/stats"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.activeProjects").isNumber())
                    .andReturn();

            JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
            int activeProjects = json.get("activeProjects").asInt();
            
            assertTrue(activeProjects >= 2, 
                    "Active projects should be at least 2, was: " + activeProjects);
        } finally {
            projectStore.delete(projectId1);
            projectStore.delete(projectId2);
        }
    }

    // HELPER METHODS
    private String createProject() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/projects"))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readTree(
                result.getResponse().getContentAsString()
        ).get("projectId").asText();
    }

    private String createAndLoadProject() throws Exception {
        String projectId = createProject();

        byte[] fileContent = Files.readAllBytes(Paths.get(TEST_TSV_PATH));
        MockMultipartFile file = new MockMultipartFile(
                "file", "test_data.tsv", "text/tab-separated-values", fileContent
        );

        MvcResult result = mockMvc.perform(
                multipart("/api/projects/" + projectId + "/load").file(file)
        ).andReturn();
        
        if (result.getResponse().getStatus() != 200) {
            throw new AssertionError("Load failed: " + result.getResponse().getContentAsString());
        }

        return projectId;
    }

    private String createLoadAndClusterProject() throws Exception {
        String projectId = createAndLoadProject();

        String clusterRequest = "{" +
                "\"numberOfPhases\": 2," +
                "\"numberOfEntityGroups\": 2," +
                "\"changesWeight\": 0.5," +
                "\"timeClusteringEnabled\": true," +
                "\"entityClusteringEnabled\": true" +
                "}";

        mockMvc.perform(
                post("/api/projects/" + projectId + "/cluster")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(clusterRequest)
        ).andExpect(status().isOk());

        return projectId;
    }
}