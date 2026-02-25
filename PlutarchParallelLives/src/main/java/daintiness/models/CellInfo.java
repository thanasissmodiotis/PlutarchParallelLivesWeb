package daintiness.models;

public class CellInfo {
	private final String EntityName;
	private final int PhaseId;

	public CellInfo(String entityName, int phaseId) {
		this.EntityName = entityName;
		this.PhaseId = phaseId;
	}
	
	public String getEntityName() {
        return EntityName;
    }
	public int getPhaseId() {
        return PhaseId;
    }
	
	@Override
    public String toString() {

        return  "Entity Name : " + EntityName +
                " at phaseId: " + PhaseId;
    }

}
