package ISW2.DataRetriever.util;

import ISW2.DataRetriever.model.CommitInfo;
import ISW2.DataRetriever.model.VersionInfo;
import org.eclipse.jgit.revwalk.RevCommit;

import java.util.List;

public class VersionInfoUtil {
    private VersionInfoUtil(){ throw new IllegalStateException("This class does not have to be instantiated.");}

    /** Returns versionInfo from the name of version*/
    public static VersionInfo getVersionInfoFromName (String name, List<VersionInfo> list){
        for (VersionInfo version: list){
            if( version.getVersionName().equals(name))
                return version;
        }
        //if it doesn't find anything return null version
        return list.get(0);
    }

    //TODO create util classes for all model classes, put into those static method etc...
    /** Get the corresponding version of commit*/
    public static VersionInfo getVersionOfCommit(RevCommit commit, List<CommitInfo> CommitsAssociatedWithVersion){
        for(CommitInfo commitInfo : CommitsAssociatedWithVersion) {
            for(RevCommit c : commitInfo.getCommitList()) {
                if(c.equals(commit)) {
                    return commitInfo.getVersionInfo();
                }

            }

        }
        return null;
    }
}
