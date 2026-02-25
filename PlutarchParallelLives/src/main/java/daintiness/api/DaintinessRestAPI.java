package daintiness.api;

import daintiness.clustering.BeatClusteringProfile;
import daintiness.clustering.ClusteringProfile;
import daintiness.clustering.EntityClusteringProfile;
import daintiness.clustering.EntityGroup;
import daintiness.clustering.Phase;
import daintiness.clustering.measurements.ChartGroupPhaseMeasurement;
import daintiness.maincontroller.IMainController;
import daintiness.models.PatternData;
import daintiness.utilities.Constants;
import javafx.collections.ObservableList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = {"http://localhost:8080", "http://127.0.0.1:8080"}, 
             allowedHeaders = "*",
             methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.DELETE, RequestMethod.OPTIONS})
public class DaintinessRestAPI {

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(DaintinessRestAPI.class);

    @Autowired
    private ProjectStore projectStore;

    // PROJECT MANAGEMENT 

    @PostMapping("/projects")
    public ResponseEntity<Map<String, String>> createProject() {
        try {
            String projectId = projectStore.createProject();
            Map<String, String> response = new HashMap<>();
            response.put("projectId", projectId);
            return ResponseEntity.ok(response);
        } catch (IllegalStateException e) {
            logger.warn("Project creation rejected: {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error);
        }
    }

    @DeleteMapping("/projects/{projectId}")
    public ResponseEntity<Void> deleteProject(@PathVariable String projectId) {
        boolean deleted = projectStore.delete(projectId);
        return deleted ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }


    @PostMapping("/projects/{projectId}/cleanup")
    public ResponseEntity<Void> cleanupProject(@PathVariable String projectId) {
        projectStore.delete(projectId);
        return ResponseEntity.ok().build();
    }

    // FILE OPERATIONS

    @PostMapping("/projects/{projectId}/load")
    public ResponseEntity<Map<String, Object>> loadFile(
            @PathVariable String projectId,
            @RequestParam("file") MultipartFile file) {
        
        Optional<IMainController> controllerOpt = projectStore.get(projectId);
        if (controllerOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Path tempFile = null;
        try {
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || originalFilename.isEmpty()) {
                originalFilename = "upload.tsv";
            }
            
            String extension = "";
            int lastDot = originalFilename.lastIndexOf('.');
            if (lastDot > 0) {
                extension = originalFilename.substring(lastDot);
            } else {
                extension = ".tsv"; 
            }
            
 
            tempFile = Files.createTempFile("upload-", extension);
            file.transferTo(tempFile.toFile());

            IMainController controller = controllerOpt.get();
            controller.load(tempFile.toFile());

            Map<String, Object> response = new HashMap<>();
            response.put("numberOfBeats", controller.getNumberOfBeats());
            response.put("numberOfEntities", controller.getNumberOfEntities());
            response.put("numberOfTEMs", controller.getNumberOfTEMs());
            response.put("success", true);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getClass().getSimpleName() + ": " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        } finally {
            // Clean up temp file
            if (tempFile != null) {
                try {
                    Files.deleteIfExists(tempFile);
                } catch (IOException ignored) {}
            }
        }
    }

    @PostMapping("/projects/{projectId}/loadSchemaEvo")
    public ResponseEntity<Map<String, Object>> loadSchemaEvoFolder(
            @PathVariable String projectId,
            @RequestParam("heartbeat") MultipartFile heartbeatFile,
            @RequestParam("tables") MultipartFile tablesFile,
            @RequestParam("transitions") MultipartFile transitionsFile) {
        
        Optional<IMainController> controllerOpt = projectStore.get(projectId);
        if (controllerOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        try {
            Path tempFolder = Files.createTempDirectory("schema-evo-");
            Path resultsFolder = Files.createDirectory(tempFolder.resolve("results"));
            
            Path heartbeatPath = resultsFolder.resolve(heartbeatFile.getOriginalFilename().replaceAll(".*[/\\\\]", ""));
            Path tablesPath = resultsFolder.resolve(tablesFile.getOriginalFilename().replaceAll(".*[/\\\\]", ""));
            Path transitionsPath = resultsFolder.resolve(transitionsFile.getOriginalFilename().replaceAll(".*[/\\\\]", ""));
            
            heartbeatFile.transferTo(heartbeatPath.toFile());
            tablesFile.transferTo(tablesPath.toFile());
            transitionsFile.transferTo(transitionsPath.toFile());

            IMainController controller = controllerOpt.get();
            controller.load(tempFolder.toFile());

            Files.walk(tempFolder)
            .sorted(Comparator.reverseOrder())
            .map(Path::toFile)
            .forEach(file -> file.delete());

            Map<String, Object> response = new HashMap<>();
            response.put("numberOfBeats", controller.getNumberOfBeats());
            response.put("numberOfEntities", controller.getNumberOfEntities());
            response.put("numberOfTEMs", controller.getNumberOfTEMs());
            response.put("success", true);
            response.put("loadType", "SCHEMA_EVO");

            return ResponseEntity.ok(response);
        } catch (IOException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            logger.error("Failed to load schema evolution folder for project {}: {}", projectId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @PostMapping("/projects/{projectId}/importProject")
    public ResponseEntity<Map<String, Object>> importProject(
            @PathVariable String projectId,
            @RequestParam("tem") MultipartFile temFile,
            @RequestParam("gpm") MultipartFile gpmFile) {
        
        Optional<IMainController> controllerOpt = projectStore.get(projectId);
        if (controllerOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        try {
            Path tempFolder = Files.createTempDirectory("project-import-");
            
            Path temPath = tempFolder.resolve("tem.tsv");
            Path gpmPath = tempFolder.resolve("gpm.tsv");
            
            temFile.transferTo(temPath.toFile());
            gpmFile.transferTo(gpmPath.toFile());

            IMainController controller = controllerOpt.get();
            controller.importProject(tempFolder.toFile());

            Files.walk(tempFolder)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(file -> file.delete());

            Map<String, Object> response = new HashMap<>();
            response.put("numberOfBeats", controller.getNumberOfBeats());
            response.put("numberOfEntities", controller.getNumberOfEntities());
            response.put("numberOfTEMs", controller.getNumberOfTEMs());
            response.put("numberOfPhases", controller.getPhases().size());
            response.put("numberOfEntityGroups", controller.getEntityGroups().size());
            response.put("success", true);
            response.put("loadType", "PROJECT_IMPORT");

            return ResponseEntity.ok(response);
        } catch (IOException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @PostMapping("/projects/{projectId}/exportProject")
    public ResponseEntity<?> exportProject(@PathVariable String projectId) {
        
        Optional<IMainController> controllerOpt = projectStore.get(projectId);
        if (controllerOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        IMainController controller = controllerOpt.get();
        
        try {
            if (controller.getNumberOfBeats() == 0 || controller.getNumberOfEntities() == 0) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("error", "No data to export. Please load a file first.");
                return ResponseEntity.badRequest().body(error);
            }
            
            List<Phase> phases = controller.getPhases();
            if (phases == null || phases.isEmpty()) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("error", "Cannot export project. Please perform clustering first.");
                return ResponseEntity.badRequest().body(error);
            }
            
            Path tempFolder = Files.createTempDirectory("project-export-");
            controller.exportProject(tempFolder.toFile());
            
            Path zipPath = Files.createTempFile("project-", ".zip");
            try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(zipPath))) {
                Files.walk(tempFolder)
                    .filter(path -> !Files.isDirectory(path))
                    .forEach(path -> {
                        try {
                            ZipEntry zipEntry = new ZipEntry(tempFolder.relativize(path).toString());
                            zos.putNextEntry(zipEntry);
                            Files.copy(path, zos);
                            zos.closeEntry();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
            }
            
            byte[] data = Files.readAllBytes(zipPath);
            ByteArrayResource resource = new ByteArrayResource(data);
            
            Files.walk(tempFolder)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(file -> file.delete());
            Files.deleteIfExists(zipPath);

            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=project-export.zip")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(data.length)
                .body(resource);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "Cannot export project. Please perform clustering first.");
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/projects/{projectId}/info")
    public ResponseEntity<Map<String, Object>> getProjectInfo(@PathVariable String projectId) {
        Optional<IMainController> controllerOpt = projectStore.get(projectId);
        if (controllerOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        IMainController controller = controllerOpt.get();
        Map<String, Object> response = new HashMap<>();
        response.put("numberOfBeats", controller.getNumberOfBeats());
        response.put("numberOfEntities", controller.getNumberOfEntities());
        response.put("numberOfTEMs", controller.getNumberOfTEMs());

        return ResponseEntity.ok(response);
    }

    // CLUSTERING 

    @PostMapping("/projects/{projectId}/cluster")
    public ResponseEntity<Map<String, Object>> clusterData(
            @PathVariable String projectId,
            @RequestBody ClusteringRequest request) {
        
        if (request.getNumberOfPhases() < 1) {
            return ResponseEntity.badRequest().body(
                buildErrorResponse("VALIDATION_ERROR", "numberOfPhases must be at least 1"));
        }
        if (request.getNumberOfEntityGroups() < 1) {
            return ResponseEntity.badRequest().body(
                buildErrorResponse("VALIDATION_ERROR", "numberOfEntityGroups must be at least 1"));
        }
        if (request.getChangesWeight() < 0 || request.getChangesWeight() > 1) {
            return ResponseEntity.badRequest().body(
                buildErrorResponse("VALIDATION_ERROR", "changesWeight must be between 0 and 1"));
        }
        
        Optional<IMainController> controllerOpt = projectStore.get(projectId);
        if (controllerOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        IMainController controller = controllerOpt.get();

        BeatClusteringProfile beatProfile;
        if (request.isTimeClusteringEnabled()) {
            beatProfile = new BeatClusteringProfile(
                request.getNumberOfPhases(),
                request.getChangesWeight(),
                false
            );
        } else {
            beatProfile = new BeatClusteringProfile(request.getNumberOfPhases());
        }

        EntityClusteringProfile entityProfile = null;
        if (request.isEntityClusteringEnabled()) {
            entityProfile = new EntityClusteringProfile(request.getNumberOfEntityGroups());
        }

        ClusteringProfile profile = new ClusteringProfile(beatProfile, entityProfile);
        controller.fitDataToGroupPhaseMeasurements(profile);

        List<Constants.MeasurementType> availableMeasurements = controller.getAvailableMeasurementTypesList();
        List<Constants.AggregationType> availableAggregations = controller.getAvailableAggregationTypesList();
        
        if (!availableMeasurements.isEmpty() && !availableAggregations.isEmpty()) {
            controller.generateChartDataOfType(
                availableMeasurements.get(0),
                availableAggregations.get(0)
            );
        }

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("numberOfPhases", controller.getPhases().size());
        response.put("numberOfEntityGroups", controller.getEntityGroups().size());

        return ResponseEntity.ok(response);
    }

    // CHART DATA 

    @GetMapping("/projects/{projectId}/chartData")
    public ResponseEntity<Map<String, Object>> getChartData(@PathVariable String projectId) {
        Optional<IMainController> controllerOpt = projectStore.get(projectId);
        if (controllerOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        IMainController controller = controllerOpt.get();
        
        try {
            ObservableList<ChartGroupPhaseMeasurement> chartData = controller.getChartData();
            List<Phase> phases = controller.getPhases();

            if (chartData == null || chartData.isEmpty() || phases == null || phases.isEmpty()) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("error", "No chart data available. Please perform clustering first.");
                return ResponseEntity.badRequest().body(error);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("chartData", serializeChartData(chartData, phases));
            response.put("phases", serializePhases(phases));
            response.put("entityGroups", serializeEntityGroups(controller.getEntityGroups()));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "No chart data available. Please perform clustering first.");
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping("/projects/{projectId}/chartData/generate")
    public ResponseEntity<Map<String, Object>> generateChartData(
            @PathVariable String projectId,
            @RequestBody ChartDataRequest request) {
        
        Optional<IMainController> controllerOpt = projectStore.get(projectId);
        if (controllerOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        IMainController controller = controllerOpt.get();

        List<Phase> phases = controller.getPhases();
        if (phases == null || phases.isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "Cannot generate chart data. Please perform clustering first.");
            return ResponseEntity.badRequest().body(error);
        }
        
        try {
            Constants.MeasurementType measurementType = Constants.MeasurementType.valueOf(request.getMeasurementType());
            Constants.AggregationType aggregationType = Constants.AggregationType.valueOf(request.getAggregationType());
            
            controller.generateChartDataOfType(measurementType, aggregationType);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "Invalid measurement or aggregation type: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/projects/{projectId}/measurementTypes")
    public ResponseEntity<Map<String, Object>> getMeasurementTypes(@PathVariable String projectId) {
        Optional<IMainController> controllerOpt = projectStore.get(projectId);
        if (controllerOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        IMainController controller = controllerOpt.get();
        
        Map<String, Object> response = new HashMap<>();
        response.put("measurementTypes", controller.getAvailableMeasurementTypesList().stream()
            .map(Enum::name).collect(Collectors.toList()));
        response.put("aggregationTypes", controller.getAvailableAggregationTypesList().stream()
            .map(Enum::name).collect(Collectors.toList()));
        response.put("currentMeasurementType", controller.getMeasurementType() != null ? 
            controller.getMeasurementType().name() : null);
        response.put("currentAggregationType", controller.getAggregationType() != null ?
            controller.getAggregationType().name() : null);

        return ResponseEntity.ok(response);
    }

    // SORTING

    @PostMapping("/projects/{projectId}/sort")
    public ResponseEntity<Map<String, Object>> sortChartData(
            @PathVariable String projectId,
            @RequestParam String sortType) {
        
        Optional<IMainController> controllerOpt = projectStore.get(projectId);
        if (controllerOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        IMainController controller = controllerOpt.get();
        
        ObservableList<ChartGroupPhaseMeasurement> chartData = controller.getChartData();
        if (chartData == null || chartData.isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "No data to sort. Please perform clustering first.");
            return ResponseEntity.badRequest().body(error);
        }

        try {
            Constants.SortingType type = Constants.SortingType.valueOf(sortType);
            controller.sortChartData(type);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "Invalid sort type: " + sortType);
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "Sorting failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // PATTERNS
    
    @GetMapping("/projects/{projectId}/patterns")
    public ResponseEntity<?> getPatterns(
            @PathVariable String projectId,
            @RequestParam(required = false, defaultValue = "NO_TYPE") String patternType) {
        
        Optional<IMainController> controllerOpt = projectStore.get(projectId);
        if (controllerOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        IMainController controller = controllerOpt.get();
        
        try {
            Constants.PatternType type = Constants.PatternType.valueOf(patternType);
            List<PatternData> patterns = controller.getPatterns(type);
            
            if (patterns == null) {
                // Return empty list, not an error - patterns just haven't been computed yet
                return ResponseEntity.ok(new ArrayList<>());
            }

            return ResponseEntity.ok(serializePatterns(patterns));
        } catch (IllegalArgumentException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "Invalid pattern type: " + patternType);
            return ResponseEntity.badRequest().body(error);
        }
    }

    // CELL DETAILS

    @GetMapping("/projects/{projectId}/cellDetails")
    public ResponseEntity<Map<String, Object>> getCellDetails(
            @PathVariable String projectId,
            @RequestParam int entityGroupId,
            @RequestParam int phaseId) {
        
        Optional<IMainController> controllerOpt = projectStore.get(projectId);
        if (controllerOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        IMainController controller = controllerOpt.get();
        Map<String, Object> response = new HashMap<>();
        
        try {
            daintiness.clustering.EntityGroup entityGroup = controller.getEntityGroupById(entityGroupId);
            if (entityGroup == null) {
                return ResponseEntity.badRequest().body(
                    buildErrorResponse("NOT_FOUND", "EntityGroup not found: " + entityGroupId));
            }
            
            response.put("entityGroupId", entityGroup.getEntityGroupId());
            response.put("entityNames", entityGroup.getGroupComponentsNames());
            response.put("birthBeat", entityGroup.getLifeDetails().getBirthBeatId());
            response.put("deathBeat", entityGroup.getLifeDetails().getDeathBeatId());
            response.put("isAlive", entityGroup.getLifeDetails().isAlive());
            response.put("duration", entityGroup.getLifeDetails().getDuration());

            daintiness.clustering.Phase phase = controller.getPhaseById(phaseId);
            if (phase == null) {
                return ResponseEntity.badRequest().body(
                    buildErrorResponse("NOT_FOUND", "Phase not found: " + phaseId));
            }
            
            List<Integer> phaseBeats = phase.getPhaseComponentsIdList();
            response.put("phaseId", phase.getPhaseId());
            response.put("phaseBeats", phaseBeats);
            response.put("phaseStart", phaseBeats.get(0));
            response.put("phaseEnd", phaseBeats.get(phaseBeats.size() - 1));
            response.put("phaseBeatCount", phaseBeats.size());

            String gpmType = entityGroup.getGPMType(
                phaseBeats.get(0),
                phaseBeats.get(phaseBeats.size() - 1)
            ).toString();
            response.put("gpmType", gpmType);

            ObservableList<ChartGroupPhaseMeasurement> chartData = controller.getChartData();
            Double cellValue = null;
            String color = null;
            
            for (ChartGroupPhaseMeasurement cgpm : chartData) {
                if (cgpm.getEntityGroup().getEntityGroupId() == entityGroupId) {
                    daintiness.models.measurement.IMeasurement measurement = cgpm.getMeasurement(phaseId);
                    if (measurement != null) {
                        cellValue = measurement.getValue();
                        color = measurement.getColor();
                    }
                    break;
                }
            }
            
            response.put("value", cellValue);
            response.put("color", color);
            response.put("aggregationType", controller.getAggregationType().toString());
            response.put("measurementType", controller.getMeasurementType().toString());
            
            List<Map<String, Object>> breakdown = controller.getCellBreakdown(entityGroupId, phaseId);
            response.put("breakdown", breakdown);
            
            String patternType = findPatternForCell(controller, entityGroupId, phaseId);
            response.put("pattern", patternType);
            
            response.put("success", true);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error getting cell details for project {}, entityGroup {}, phase {}: {}", 
                        projectId, entityGroupId, phaseId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(buildErrorResponse("INTERNAL_ERROR", "Failed to get cell details: " + e.getMessage()));
        }
    }

    private String findPatternForCell(IMainController controller, int entityGroupId, int phaseId) {
        try {
            for (Constants.PatternType patternType : Constants.PatternType.values()) {
                if (patternType == Constants.PatternType.NO_TYPE) continue;
                
                List<PatternData> patterns = controller.getPatterns(patternType);
                if (patterns == null) continue;
                
                for (PatternData pattern : patterns) {
                    for (daintiness.models.CellInfo cell : pattern.getPatternCellsList()) {
                        daintiness.clustering.EntityGroup group = controller.getEntityGroupById(entityGroupId);
                        if (group != null && 
                            group.getGroupComponentsNames().contains(cell.getEntityName()) &&
                            cell.getPhaseId() == phaseId) {
                            return pattern.getPatternType().toString();
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.debug("Could not check patterns for cell: {}", e.getMessage());
        }
        return null;
    }
    
    @PostMapping("/projects/{projectId}/patterns/export")
    public ResponseEntity<?> exportPatterns(
            @PathVariable String projectId) throws IOException {
        
        Optional<IMainController> controllerOpt = projectStore.get(projectId);
        if (controllerOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        IMainController controller = controllerOpt.get();
        
        List<PatternData> patterns = controller.getPatterns(Constants.PatternType.NO_TYPE);
        if (patterns == null || patterns.isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "No patterns to export. Please detect patterns first using the Patterns panel.");
            return ResponseEntity.badRequest().body(error);
        }
        
        Path tempFile = Files.createTempFile("patterns-", ".txt");
        controller.printPatterns(tempFile.toFile());
        
        byte[] data = Files.readAllBytes(tempFile);
        ByteArrayResource resource = new ByteArrayResource(data);
        
        Files.deleteIfExists(tempFile);

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=patterns.txt")
            .contentType(MediaType.TEXT_PLAIN)
            .contentLength(data.length)
            .body(resource);
    }

    // HELPER METHODS 

    private List<Map<String, Object>> serializeChartData(
            ObservableList<ChartGroupPhaseMeasurement> chartData, List<Phase> phases) {
        
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (ChartGroupPhaseMeasurement gpm : chartData) {
            Map<String, Object> row = new HashMap<>();
            EntityGroup group = gpm.getEntityGroup();
            
            row.put("entityGroupId", group.getEntityGroupId());
            row.put("entityGroupName", getEntityGroupName(group));
            row.put("isAlive", group.getLifeDetails().isAlive());
            row.put("birthBeatId", group.getLifeDetails().getBirthBeatId());
            row.put("deathBeatId", group.getLifeDetails().getDeathBeatId());
            row.put("duration", group.getLifeDetails().getDuration());
            
            List<Map<String, Object>> phaseMeasurements = new ArrayList<>();
            for (Phase phase : phases) {
                Map<String, Object> phaseData = new HashMap<>();
                int phaseId = phase.getPhaseId();
                
                Constants.GPMType gpmType = group.getGPMType(
                    phase.getFirstPhaseBeat().getBeatId(),
                    phase.getLastPhaseBeat().getBeatId()
                );
                
                phaseData.put("phaseId", phaseId);
                phaseData.put("gpmType", gpmType.name());
                
                if (gpmType.equals(Constants.GPMType.ACTIVE) && gpm.containsMeasurementInPhase(phaseId)) {
                    String color = gpm.getMeasurement(phaseId).getColor();
                    phaseData.put("color", color);
                } else {
                    phaseData.put("color", getColorForGPMType(gpmType));
                }
                
                phaseMeasurements.add(phaseData);
            }
            
            row.put("phaseMeasurements", phaseMeasurements);
            result.add(row);
        }
        
        return result;
    }

    private List<Map<String, Object>> serializePhases(List<Phase> phases) {
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (Phase phase : phases) {
            Map<String, Object> phaseData = new HashMap<>();
            phaseData.put("phaseId", phase.getPhaseId());
            phaseData.put("firstBeatId", phase.getFirstPhaseBeat().getBeatId());
            phaseData.put("lastBeatId", phase.getLastPhaseBeat().getBeatId());
            phaseData.put("size", phase.getPhaseComponents().size());
            result.add(phaseData);
        }
        
        return result;
    }

    private List<Map<String, Object>> serializeEntityGroups(List<EntityGroup> groups) {
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (EntityGroup group : groups) {
            Map<String, Object> groupData = new HashMap<>();
            groupData.put("entityGroupId", group.getEntityGroupId());
            groupData.put("entityGroupName", getEntityGroupName(group));
            groupData.put("isAlive", group.getLifeDetails().isAlive());
            groupData.put("birthBeatId", group.getLifeDetails().getBirthBeatId());
            groupData.put("deathBeatId", group.getLifeDetails().getDeathBeatId());
            groupData.put("duration", group.getLifeDetails().getDuration());
            groupData.put("componentCount", group.getGroupComponents().size());
            
            List<String> components = group.getGroupComponents().stream()
                .map(entity -> entity.getEntityName())
                .collect(Collectors.toList());
            groupData.put("components", components);
            
            result.add(groupData);
        }
        
        return result;
    }

    private List<Map<String, Object>> serializePatterns(List<PatternData> patterns) {
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (PatternData pattern : patterns) {
            Map<String, Object> patternData = new HashMap<>();
            patternData.put("patternType", pattern.getPatternType().name());
            
            List<Map<String, Object>> cells = new ArrayList<>();
            for (var cell : pattern.getPatternCellsList()) {
                Map<String, Object> cellData = new HashMap<>();
                cellData.put("entityName", cell.getEntityName());
                cellData.put("phaseId", cell.getPhaseId());
                cells.add(cellData);
            }
            patternData.put("cells", cells);
            
            result.add(patternData);
        }
        
        return result;
    }

    private String getEntityGroupName(EntityGroup group) {
        if (group.getGroupComponents().size() == 1) {
            return group.getGroupComponents().get(0).getEntityName();
        } else {
            return String.valueOf(group.getEntityGroupId());
        }
    }

    private String getColorForGPMType(Constants.GPMType type) {
        if (type == null) {
            return "#FFFFFF";
        }
        
        if (type == Constants.GPMType.INACTIVE) {
            return "#FFFFFF";  // White
        } else if (type == Constants.GPMType.BIRTH) {
            return "#90EE90";  // Blue
        } else if (type == Constants.GPMType.DEATH) {
            return "#000000";  // Black
        } else if (type == Constants.GPMType.ACTIVE) {
            return "#90EE90";  // Light Green
        } else {
            return "#FFFFFF";
        }
    }

    // SAVE DATA 

    @PostMapping("/projects/{projectId}/saveData")
    public ResponseEntity<?> saveData(@PathVariable String projectId) throws IOException {
        Optional<IMainController> controllerOpt = projectStore.get(projectId);
        if (controllerOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        IMainController controller = controllerOpt.get();
        
        if (controller.getNumberOfBeats() == 0 || controller.getNumberOfEntities() == 0) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "No data to save. Please load a file first.");
            return ResponseEntity.badRequest().body(error);
        }
        
        Path tempFile = Files.createTempFile("data-export-", ".tsv");
        controller.save(tempFile.toFile());
        
        byte[] data = Files.readAllBytes(tempFile);
        ByteArrayResource resource = new ByteArrayResource(data);
        
        Files.deleteIfExists(tempFile);

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=data-export.tsv")
            .contentType(MediaType.parseMediaType("text/tab-separated-values"))
            .contentLength(data.length)
            .body(resource);
    }

    // SESSION MANAGEMENT

    @GetMapping("/sessions/stats")
    public ResponseEntity<Map<String, Object>> getSessionStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("activeProjects", projectStore.getActiveProjectCount());
        return ResponseEntity.ok(stats);
    }

    // HELPER METHODS FOR RESPONSES 

    private Map<String, Object> buildErrorResponse(String errorCode, String message) {
        Map<String, Object> error = new HashMap<>();
        error.put("success", false);
        error.put("errorCode", errorCode);
        error.put("error", message);
        return error;
    }

    private Map<String, Object> buildSuccessResponse() {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        return response;
    }

    // REQUEST CLASSES 

    public static class ClusteringRequest {
        private int numberOfPhases;
        private int numberOfEntityGroups;
        private double changesWeight;
        private boolean timeClusteringEnabled;
        private boolean entityClusteringEnabled;

        public int getNumberOfPhases() { return numberOfPhases; }
        public void setNumberOfPhases(int numberOfPhases) { this.numberOfPhases = numberOfPhases; }
        
        public int getNumberOfEntityGroups() { return numberOfEntityGroups; }
        public void setNumberOfEntityGroups(int numberOfEntityGroups) { 
            this.numberOfEntityGroups = numberOfEntityGroups; 
        }
        
        public double getChangesWeight() { return changesWeight; }
        public void setChangesWeight(double changesWeight) { this.changesWeight = changesWeight; }
        
        public boolean isTimeClusteringEnabled() { return timeClusteringEnabled; }
        public void setTimeClusteringEnabled(boolean timeClusteringEnabled) { 
            this.timeClusteringEnabled = timeClusteringEnabled; 
        }
        
        public boolean isEntityClusteringEnabled() { return entityClusteringEnabled; }
        public void setEntityClusteringEnabled(boolean entityClusteringEnabled) { 
            this.entityClusteringEnabled = entityClusteringEnabled; 
        }
    }

    public static class ChartDataRequest {
        private String measurementType;
        private String aggregationType;

        public String getMeasurementType() { return measurementType; }
        public void setMeasurementType(String measurementType) { 
            this.measurementType = measurementType; 
        }
        
        public String getAggregationType() { return aggregationType; }
        public void setAggregationType(String aggregationType) { 
            this.aggregationType = aggregationType; 
        }
    }
}