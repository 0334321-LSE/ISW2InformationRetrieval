package isw.project.util;

import isw.project.model.BugTicket;
import isw.project.model.Version;


import java.util.ArrayList;
import java.util.List;

public class VersionUtil {
    private VersionUtil(){ throw new IllegalStateException("This class does not have to be instantiated.");}

    /** Return ticket with OV <= version */
    public static List<BugTicket> getAssociatedTicketUntilVersionID(List<BugTicket> bugTicketList, int versionID){
        List<BugTicket> associatedTicket = new ArrayList<>();
        for(BugTicket bugTicket: bugTicketList){
            if (bugTicket.getOpeningVersion().getVersionInt() <= versionID )
                associatedTicket.add(bugTicket);
        }
        return associatedTicket;
    }

    public static void resetIntValueOfVersion(List<Version> versionList){
        int i =0;
        for(Version version:versionList){
            version.setVersionInt(i);
            i++;
        }
    }
}
