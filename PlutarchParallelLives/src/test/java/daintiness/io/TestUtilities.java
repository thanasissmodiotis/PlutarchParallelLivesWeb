package daintiness.io;

import javafx.collections.ObservableList;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;

import daintiness.clustering.EntityGroup;
import daintiness.clustering.Phase;
import daintiness.clustering.measurements.ChartGroupPhaseMeasurement;
import daintiness.models.Beat;
import daintiness.models.Entity;
import daintiness.models.LifeDetails;

import java.util.List;
import java.util.Map;

public class TestUtilities {

    public void testGPM(ObservableList<ChartGroupPhaseMeasurement> chartData, Map<Integer, Map<Integer, Double>> entityNameToGPMMap) {
        Assumptions.assumeTrue(chartData.size() == entityNameToGPMMap.size());

        for (ChartGroupPhaseMeasurement expectedGPM: chartData) {
            int expectedEntityGroupID = expectedGPM.getEntityGroup().getEntityGroupId();
            Assumptions.assumeTrue(entityNameToGPMMap.containsKey(expectedEntityGroupID));
            Assumptions.assumeTrue(expectedGPM.getNumberOfMeasurements() == entityNameToGPMMap.get(expectedEntityGroupID).size());

            for (Integer beatId: entityNameToGPMMap.get(expectedEntityGroupID).keySet()) {
                Assumptions.assumeTrue(expectedGPM.containsMeasurementInPhase(beatId));
                double expectedMeasurement = expectedGPM.getMeasurement(beatId).getValue();
                double actualMeasurement = entityNameToGPMMap.get(expectedEntityGroupID).get(beatId);

                Assertions.assertEquals(expectedMeasurement, actualMeasurement);
            }
        }
    }

    public void testEntityGroups(List<EntityGroup> expectedEntityGroupList, List<EntityGroup> actualEntityGroupList) {
        Assumptions.assumeTrue(expectedEntityGroupList.size() == actualEntityGroupList.size());

        for (int i=0; i < expectedEntityGroupList.size(); i++) {
            EntityGroup expectedEntityGroup = expectedEntityGroupList.get(i);
            EntityGroup actualEntityGroup = actualEntityGroupList.get(i);

            Assertions.assertAll(
                    () -> Assertions.assertEquals(expectedEntityGroup.getEntityGroupId(), actualEntityGroup.getEntityGroupId()),
                    () -> testEntityLists(expectedEntityGroup.getGroupComponents(), actualEntityGroup.getGroupComponents()),
                    () -> testEntityNameList(expectedEntityGroup.getGroupComponentsNames(), actualEntityGroup.getGroupComponentsNames()),
                    () -> testLifeDetails(expectedEntityGroup.getLifeDetails(), actualEntityGroup.getLifeDetails())
            );
        }
    }

    public void testEntityLists(List<Entity> expectedEntityList, List<Entity> actualEntityList) {
        Assumptions.assumeTrue(expectedEntityList.size() == actualEntityList.size());

        for (int i=0; i < expectedEntityList.size(); i++) {
            testEntity(expectedEntityList.get(i), actualEntityList.get(i));
        }
    }

    public void testEntity(Entity expectedEntity, Entity actualEntity) {
        Assertions.assertAll(
                () -> Assertions.assertEquals(expectedEntity.getEntityName(), actualEntity.getEntityName()),
                () -> Assertions.assertEquals(expectedEntity.getEntityId(), actualEntity.getEntityId()),
                () -> testLifeDetails(expectedEntity.getLifeDetails(), actualEntity.getLifeDetails())
        );
    }

    public void testEntityNameList(List<String> expectedEntityNamesList, List<String> actualEntityNamesList) {
        Assumptions.assumeTrue(expectedEntityNamesList.size() == actualEntityNamesList.size());

        for (String expectedName: expectedEntityNamesList) {
            Assertions.assertTrue(actualEntityNamesList.contains(expectedName));
        }
    }

    public void testLifeDetails(LifeDetails expectedLifeDetails, LifeDetails actualLifeDetails) {
        Assertions.assertAll(
                () -> Assertions.assertEquals(expectedLifeDetails.isAlive(), actualLifeDetails.isAlive()),
                () -> Assertions.assertEquals(expectedLifeDetails.getDuration(), actualLifeDetails.getDuration()),
                () -> Assertions.assertEquals(expectedLifeDetails.getBirthBeatId(), actualLifeDetails.getBirthBeatId()),
                () -> Assertions.assertEquals(expectedLifeDetails.getDeathBeatId(), actualLifeDetails.getDeathBeatId())
        );
    }

    public void testPhases(List<Phase> expectedPhaseList, List<Phase> actualPhaseList) {
        Assumptions.assumeTrue(expectedPhaseList.size() == actualPhaseList.size());

        for (int i = 0; i < expectedPhaseList.size(); i++) {
            Phase expectedPhase = expectedPhaseList.get(i);
            Phase actualPhase = actualPhaseList.get(i);
            Assertions.assertAll(
                    () -> Assertions.assertEquals(expectedPhase.getPhaseId(), actualPhase.getPhaseId()),
                    () -> testBeatList(expectedPhase.getPhaseComponents(), actualPhase.getPhaseComponents()),
                    () -> testBeatIdList(expectedPhase.getPhaseComponentsIdList(), actualPhase.getPhaseComponentsIdList()),
                    () -> testBeat(expectedPhase.getFirstPhaseBeat(), actualPhase.getFirstPhaseBeat()),
                    () -> testBeat(expectedPhase.getLastPhaseBeat(), actualPhase.getLastPhaseBeat())
            );
        }

    }

    public void testBeatIdList(List<Integer> expectedBeatIdList, List<Integer> actualBeatIdList) {
        Assumptions.assumeTrue(expectedBeatIdList.size() == actualBeatIdList.size());

        for (int i=0; i < expectedBeatIdList.size(); i++) {
            Assertions.assertEquals(expectedBeatIdList.get(i), actualBeatIdList.get(i));
        }
    }

    public void testBeatList(List<Beat> expectedBeatList, List<Beat> actualBeatList) {
        Assumptions.assumeTrue(expectedBeatList.size() == actualBeatList.size());

        for (int i=0; i < expectedBeatList.size(); i++) {
            testBeat(expectedBeatList.get(i), actualBeatList.get(i));
        }
    }

    public void testBeat(Beat expectedBeat, Beat actualBeat) {
        Assertions.assertAll(
                () -> Assertions.assertEquals(expectedBeat.getBeatId(), actualBeat.getBeatId()),
                () -> Assertions.assertEquals(expectedBeat.getDate(), actualBeat.getDate())
        );
    }
}
