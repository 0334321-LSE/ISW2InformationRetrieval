package ISW2.DataRetriever;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {

    private static final String PROJECT_NAME = "bookkeeper" ;
    public static void main(String[] args) throws IOException, URISyntaxException, GitAPIException {
        String repoPath = "C:\\Users\\39388\\OneDrive\\Desktop\\ISW2\\Projects\\GitRepository\\" ;

        JiraRetriever retriever = new JiraRetriever() ;
        List<String> bugTicketKeyList = retriever.retrieveBugTicket(PROJECT_NAME) ;
        //Logger.getGlobal().log(Level.INFO, bugTicketKeyList.toString());
        List<VersionInfo> versionInfoList = retriever.retrieveVersions(PROJECT_NAME) ;
        Logger.getGlobal().info(versionInfoList.toString());
        CommitRetriever commitRetriever = new CommitRetriever() ;
        List<RevCommit> commitList = commitRetriever.retrieveAllCommitsInfo(repoPath + PROJECT_NAME, PROJECT_NAME);
        Map<String, ArrayList<RevCommit>> ticketAndAssociatedCommit = commitRetriever.retrieveCommitFromTickets(bugTicketKeyList, commitList);
        System.out.println("Goodbye");



    }
}