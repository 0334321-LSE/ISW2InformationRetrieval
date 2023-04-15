package ISW2.DataRetriever;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {

    //TODO make possible to insert the project name
    public static void main(String[] args) throws IOException, URISyntaxException, GitAPIException {
        findProjectData("tajo");
    }

    private static void findProjectData(String project_name) throws URISyntaxException, IOException, GitAPIException {
        String repoPath = "C:\\Users\\39388\\OneDrive\\Desktop\\ISW2\\Projects\\GitRepository\\" ;

        JiraRetriever retriever = new JiraRetriever() ;
        List<VersionInfo> versionInfoList = retriever.retrieveVersions(project_name) ;
        retriever.printVersionList(versionInfoList);
        List<BugTicket> bugTickets = retriever.retrieveBugTicket(project_name,versionInfoList) ;
        List<String> bugTicketsKeys = retriever.getIssueKeyList(bugTickets);


        CommitRetriever commitRetriever = new CommitRetriever() ;
        List<RevCommit> commitList = commitRetriever.retrieveAllCommitsInfo(repoPath + project_name, project_name);
        Map<String, ArrayList<RevCommit>> ticketAndAssociatedCommit = commitRetriever.retrieveCommitFromTickets(bugTicketsKeys, commitList);

        Proportion.proportion(bugTickets,versionInfoList,project_name);
        /*BugTicket ticketInfoRetriever = new BugTicket();
        ticketInfoRetriever.setVersionInfo(bugTickets,versionInfoList);
        //ticketInfoRetriever.printVersionInformationList(bugTickets);
        List<BugTicket> bugTicketsForProportion = ticketInfoRetriever.correctBugTicketListForProportioning(bugTickets,versionInfoList);

        //System.out.println("\nProportion value is: "+p);
        //ticketInfoRetriever.printVersionInformationList(correctBugTickets);
        ticketInfoRetriever.proportionForInjectedVersion(bugTickets,bugTicketsForProportion,versionInfoList);
        ticketInfoRetriever.printVersionInformationList(bugTickets);*/
        System.out.println("\n\nGoodbye");

    }
}