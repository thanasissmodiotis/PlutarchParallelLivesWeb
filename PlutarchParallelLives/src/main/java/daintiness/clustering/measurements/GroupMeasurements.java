package daintiness.clustering.measurements;

import java.util.List;

import daintiness.clustering.EntityGroup;


public class GroupMeasurements {
    private final EntityGroup entityGroup;
    private final int activity;
    private final List<GroupPhaseMeasurement> groupPhaseMeasurementsList;
//    private final List<Integer> nonEmptyPhases;

    public GroupMeasurements(EntityGroup entityGroup, int activity, List<GroupPhaseMeasurement> groupPhaseMeasurementsList) {
        this.entityGroup = entityGroup;
        this.activity = activity;
        this.groupPhaseMeasurementsList = groupPhaseMeasurementsList;
//        this.nonEmptyPhases = new ArrayList<>();
//        findNonEmptyPhases();
    }

    public EntityGroup getEntityGroup() {
        return entityGroup;
    }

    public int getActivity() {
        return activity;
    }

    public List<GroupPhaseMeasurement> getGroupPhaseMeasurementsList() {
        return groupPhaseMeasurementsList;
    }

//    public List<Integer> getNonEmptyPhases() {
//        return nonEmptyPhases;
//    }

//    private void findNonEmptyPhases() {
//        for(GroupPhaseMeasurement gpm: groupPhaseMeasurementsList) {
//            nonEmptyPhases.add(gpm.getPhase().getPhaseId());
//        }
//    }


//    public GroupPhaseMeasurement getGPMByPhase(Phase phase) {
//        for (GroupPhaseMeasurement gpm: groupPhaseMeasurementsList) {
//            if (phase.getPhaseId() == gpm.getPhase().getPhaseId()) {
//                return  gpm;
//            }
//        }
//        return null;
//    }
//
//    public boolean hasGPMInPhase(Phase phase) {
//        return nonEmptyPhases.contains(phase.getPhaseId());
//    }
}
