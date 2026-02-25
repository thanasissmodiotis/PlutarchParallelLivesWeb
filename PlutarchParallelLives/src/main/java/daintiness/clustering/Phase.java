package daintiness.clustering;

import java.util.ArrayList;
import java.util.List;

import daintiness.models.Beat;


public class Phase {
    private int phaseId;
    private final List<Beat> phaseComponents;
    private final List<Integer> phaseComponentsIdList;



    public Phase(int phaseId, Beat firstBeat) {
        this.phaseId = phaseId;
        this.phaseComponents = new ArrayList<>();
        this.phaseComponentsIdList = new ArrayList<>();
        phaseComponents.add(firstBeat);
        phaseComponentsIdList.add(firstBeat.getBeatId());
    }

    public Phase(int phaseId, List<Integer> phaseComponentsIdList) {
        this.phaseId = phaseId;
        this.phaseComponentsIdList = phaseComponentsIdList;
        this.phaseComponents = null;
    }

    public int getPhaseId() {
        return phaseId;
    }


    public List<Beat> getPhaseComponents() {
        return phaseComponents;
    }


    public List<Integer> getPhaseComponentsIdList() {
        return phaseComponentsIdList;
    }


    public void addBeat(Beat newBeat) {
        phaseComponents.add(newBeat);
        phaseComponentsIdList.add(newBeat.getBeatId());
    }


    public Beat getFirstPhaseBeat() {
        return phaseComponents.get(0);
    }


    public Beat getLastPhaseBeat() {
        return phaseComponents.get(phaseComponents.size() - 1);
    }


    public boolean containsBeat(int beatId) {
        return phaseComponentsIdList.contains(beatId);
    }


    public void mergeWithPhase(Phase newPhase) {

        for (Beat beat: newPhase.getPhaseComponents()) {
            addBeat(beat);
        }
    }


    public void setPhaseId(int phaseId) {
        this.phaseId = phaseId;
    }

    @Override
    public String toString() {
        return "{" + phaseId +
                "," + getFirstPhaseBeat().getBeatId() +
                "," + getLastPhaseBeat().getBeatId() + "}";
    }
}

