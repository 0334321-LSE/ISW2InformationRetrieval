package ISW2.DataRetriever;

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

    /** Return commitInfo from a version*/
    public static CommitInfo getCommitsOfVersion(List<RevCommit> commitsList, VersionInfo versionInfo, LocalDate firstDate) {

        List<RevCommit> matchingCommits = new ArrayList<>();
        LocalDate lastDate = versionInfo.getVersionDate();

        for(RevCommit commit : commitsList) {
            //Cast date to local date then compare
            LocalDate commitDate = commit.getCommitterIdent().getWhen().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            //TODO check what is last date in fanfa program
            //if firstDate < commitDate <= lastDate then add the commit in matchingCommits list
            if(commitDate.isAfter(firstDate) && (commitDate.isBefore(lastDate) || commitDate.equals(lastDate))) {
                matchingCommits.add(commit);
            }

        }
        RevCommit lastCommit = null;
        //TODO ASK PROF. WHAT WE DO WHEN A RELEASE DOESN'T HAVE COMMITS
        if(!matchingCommits.isEmpty())
            lastCommit = getLastCommit(matchingCommits);

        return new CommitInfo(versionInfo, matchingCommits, lastCommit);
    }

    /** Get last commit from one commit list */
    private static RevCommit getLastCommit(List<RevCommit> commitsList) {

        RevCommit lastCommit = commitsList.get(0);
        for(RevCommit commit : commitsList) {
            //if commitDate > lastCommitDate then refresh lastCommit
            if(commit.getCommitterIdent().getWhen().after(lastCommit.getCommitterIdent().getWhen())) {
                lastCommit = commit;

            }
        }
        return lastCommit;

    }

    /** From commit gets version*/
    public static VersionInfo getVersionOfCommit(RevCommit commit, List<CommitInfo> CommitsInfo) {

        for(CommitInfo relComm : CommitsInfo) {
            for(RevCommit c : relComm.getCommitList()) {
                if(c.equals(commit)) {
                    return relComm.getVersionInfo();
                }

            }

        }
        return null;

    }


}
