package ISW2.DataRetriever;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.LogCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryBuilder;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CommitRetriever {

    /** Retrieve all commits from repository local clone*/
    public List<RevCommit> retrieveAllCommitsInfo(String repoPath, String projectName) throws IOException, GitAPIException {
        RepositoryBuilder repositoryBuilder = new RepositoryBuilder();
        Repository repo = repositoryBuilder.setGitDir(new File(repoPath + "/.git")).build() ;
        Git git = new Git(repo) ;
        LogCommand logCommand = git.log() ;
        Iterable<RevCommit> commitIterable = logCommand.call() ;

        List<RevCommit> revCommitList = new ArrayList<>() ;
        for (RevCommit commit : commitIterable) {
            revCommitList.add(commit) ;
        }
        revCommitList.sort(Comparator.comparingLong(o -> o.getAuthorIdent().getWhen().getTime()));
        saveAllCommitsOnJSON(revCommitList, projectName);
        return revCommitList ;
    }

    /** Add to bug tickets associated commits and then discard ticket without any commit*/
    public void retrieveCommitFromTickets(List<BugTicket> bugTicketList, List<RevCommit> commitList) {

        for (BugTicket bugTicket: bugTicketList){
            bugTicket.setAssociatedCommit(matchTicketIssueIDCommit(commitList,bugTicket.getIssueKey()));
        }

        discardTicketWithoutCommit(bugTicketList);
        System.out.println("\nValid ticket with associated commits are: "+bugTicketList.size());
        System.out.println("\nRemaining commits are: "+countCommit(bugTicketList));

    }

    /** From ticketIssueID retrieve associated commit*/
    private ArrayList<RevCommit> matchTicketIssueIDCommit(List<RevCommit> commitList,String BugTicketID) {
        ArrayList<RevCommit> associatedCommitList = new ArrayList<>();
        for(RevCommit commit: commitList){
            if (commit.getFullMessage().contains(BugTicketID))
                associatedCommitList.add(commit);
        }
        return associatedCommitList;
    }

    /** Count the number of commit*/
    private int countCommit(List<BugTicket> bugTicketList){
        int counter=0;
        for( BugTicket bugTicket: bugTicketList)
            counter += bugTicket.getAssociatedCommit().size();
        return counter;
    }



    private void saveAllCommitsOnJSON(List<RevCommit> commitList, String projectName ) throws IOException {
        FileWriter file = new FileWriter("./projectsCommits/"+projectName+"Commits.json");
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("ProjectName", projectName);
        JSONArray jsonArray = new JSONArray();

        for (RevCommit revCommit : commitList) {
            JSONObject jsonRow = new JSONObject();

            jsonRow.put("Commit-ID",revCommit.getId());
            jsonRow.put("Author",revCommit.getAuthorIdent());
            PersonIdent authorIdent = revCommit.getAuthorIdent();
            jsonRow.put("Date",authorIdent.getWhen());
            jsonRow.put("Body",revCommit.getFullMessage());
            jsonArray.put(jsonRow);
        }
        jsonObject.put("COMMIT-LIST",jsonArray);
        file.write(jsonObject.toString()+"\n");
        file.close();
    }

    private void discardTicketWithoutCommit(List<BugTicket> bugTicketList){
        bugTicketList.removeIf(bugTicket-> bugTicket.getAssociatedCommit().size()==0);
    }
}
