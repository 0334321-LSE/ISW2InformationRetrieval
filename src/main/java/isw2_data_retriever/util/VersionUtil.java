package isw2_data_retriever.util;

import isw2_data_retriever.model.BugTicket;
import isw2_data_retriever.model.VersionInfo;
import isw2_data_retriever.model.Version;
import org.eclipse.jgit.revwalk.RevCommit;

import java.util.ArrayList;
import java.util.List;

public class VersionUtil {
    private VersionUtil(){ throw new IllegalStateException("This class does not have to be instantiated.");}

    /** Returns versionInfo from the name of version*/
    public static Version getVersionInfoFromName (String name, List<Version> list){
        for (Version version: list){
            if( version.getVersionName().equals(name))
                return version;
        }
        //if it doesn't find anything return null version
        return list.get(0);
    }

    /** Get the corresponding version of commit*/
    public static Version getVersionOfCommit(RevCommit commit, List<VersionInfo> CommitsAssociatedWithVersion){
        for(VersionInfo versionInfo : CommitsAssociatedWithVersion) {
            for(RevCommit c : versionInfo.getCommitList()) {
                if(c.equals(commit)) {
                    return versionInfo.getVersion();
                }

            }

        }
        return null;
    }

    /** Return ticket with OV == version */
    public static List<BugTicket> getAssociatedTicket(List<BugTicket> bugTicketList, int versionID){
        List<BugTicket> associatedTicket = new ArrayList<>();
        for(BugTicket bugTicket: bugTicketList){
            if (bugTicket.getOpeningVersion().getVersionInt() == versionID )
                associatedTicket.add(bugTicket);
        }
        return associatedTicket;
    }
}
