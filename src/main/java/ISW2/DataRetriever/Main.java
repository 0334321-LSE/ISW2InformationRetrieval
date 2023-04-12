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
    //TODO make possible to insert the project name
    public static void main(String[] args) throws IOException, URISyntaxException, GitAPIException {
        String repoPath = "C:\\Users\\39388\\OneDrive\\Desktop\\ISW2\\Projects\\GitRepository\\" ;

        JiraRetriever retriever = new JiraRetriever() ;
        List<BugTicket> bugTickets = retriever.retrieveBugTicket(PROJECT_NAME) ;
        List<String> bugTicketsKeys = retriever.getIssueKeyList(bugTickets);
        //Logger.getGlobal().log(Level.INFO, bugTicketKeyList.toString());
        List<VersionInfo> versionInfoList = retriever.retrieveVersions(PROJECT_NAME) ;
        retriever.printVersionList(versionInfoList);
        //Logger.getGlobal().info(versionInfoList.toString());
        CommitRetriever commitRetriever = new CommitRetriever() ;
        List<RevCommit> commitList = commitRetriever.retrieveAllCommitsInfo(repoPath + PROJECT_NAME, PROJECT_NAME);
        Map<String, ArrayList<RevCommit>> ticketAndAssociatedCommit = commitRetriever.retrieveCommitFromTickets(bugTicketsKeys, commitList);

        BugTicket ticketInfoRetriever = new BugTicket();
        ticketInfoRetriever.getVersionInformation(bugTickets,versionInfoList);
        ticketInfoRetriever.printVersionInformationList(bugTickets);
        List<BugTicket> bugTicketsForProportion = ticketInfoRetriever.correctBugTicketListForProportioning(bugTickets,versionInfoList);

        //System.out.println("\nProportion value is: "+p);
        //ticketInfoRetriever.printVersionInformationList(correctBugTickets);
        ticketInfoRetriever.proportionForInjectedVersion(bugTickets,bugTicketsForProportion,versionInfoList);
        ticketInfoRetriever.printVersionInformationList(bugTickets);
        System.out.println("\n\nGoodbye");




    }
}