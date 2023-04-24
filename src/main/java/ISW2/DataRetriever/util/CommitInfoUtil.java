package ISW2.DataRetriever.util;

import ISW2.DataRetriever.model.BugTicket;
import ISW2.DataRetriever.model.CommitInfo;
import ISW2.DataRetriever.model.VersionInfo;
import org.eclipse.jgit.revwalk.RevCommit;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

public class CommitInfoUtil {
    private CommitInfoUtil(){throw new IllegalStateException("This class does not have to be instantiated.");}

    /** Return commitInfo from a versionInfo*/
    public static CommitInfo getCommitsOfVersion(List<RevCommit> commitsList, VersionInfo versionInfo, LocalDate firstDate) {

        List<RevCommit> matchingCommits = new ArrayList<>();
        LocalDate lastDate = versionInfo.getVersionDate();

        for(RevCommit commit : commitsList) {
            //Cast date to local date then compare
            LocalDate commitDate = commit.getCommitterIdent().getWhen().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            //if firstDate < commitDate <= lastDate then add the commit in matchingCommits list
            if(commitDate.isAfter(firstDate) && (commitDate.isBefore(lastDate) || commitDate.equals(lastDate))) {
                matchingCommits.add(commit);
            }

        }
        RevCommit lastCommit = null;
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

    public void getRemainingCommits(List<BugTicket> bugTicketList, List<RevCommit> commitList) {
        List<RevCommit> remainingCommit = new ArrayList<>();
        for (BugTicket bugTicket: bugTicketList){
            remainingCommit.addAll(bugTicket.getAssociatedCommit());
        }
        commitList.removeAll(commitList);
        commitList.addAll(remainingCommit);
    }

    /** From one commit gets corresponding version*/
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
