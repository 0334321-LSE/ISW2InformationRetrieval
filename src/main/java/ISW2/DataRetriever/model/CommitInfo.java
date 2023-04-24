package ISW2.DataRetriever.model;

import org.eclipse.jgit.revwalk.RevCommit;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CommitInfo {
    private VersionInfo versionInfo;
    private List<RevCommit> commitList;
    private RevCommit lastCommit;
    private Map<String,String> javaClasses;

    public CommitInfo(VersionInfo version,List<RevCommit> commitList, RevCommit lastCommit){
        this.versionInfo = version;
        this.commitList=commitList;
        this.lastCommit=lastCommit;
        this.javaClasses=null;
    }

    public VersionInfo getVersionInfo() {
        return versionInfo;
    }

    private void setVersionInfo(VersionInfo versionInfo) {
        this.versionInfo = versionInfo;
    }

    public List<RevCommit> getCommitList() {
        return commitList;
    }

    private void setCommitList(List<RevCommit> commitList) {
        this.commitList = commitList;
    }

    public RevCommit getLastCommit() {
        return lastCommit;
    }

    private void setLastCommit(RevCommit lastCommit) {
        this.lastCommit = lastCommit;
    }

    public Map<String, String> getJavaClasses() {
        return javaClasses;
    }

    public void setJavaClasses(Map<String, String> javaClasses) {
        this.javaClasses = javaClasses;
    }





}
