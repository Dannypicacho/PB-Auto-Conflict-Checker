import java.util.ArrayList;

public class Team {
    public ArrayList<String> avoidConflicts;
    public ArrayList<String> unfinishedUsers;
    public ArrayList<String> dontKnowConflicts;
    public float chemScore;
    public String[] members;
    // public boolean playerMode;

    public Team(){
        avoidConflicts = new ArrayList<String>();
        unfinishedUsers = new ArrayList<String>();
        dontKnowConflicts = new ArrayList<String>();
        chemScore = 0;
        // members = null;
        // playerMode = false;
    }
}
