package ISW2.DataRetriever;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {

    public static void main(String[] args) throws IOException, URISyntaxException, GitAPIException {
        findProjectData("bookkeeper");
    }

    private static void findProjectData(String project_name) throws URISyntaxException, IOException, GitAPIException {
        String repoPath = "C:\\Users\\39388\\OneDrive\\Desktop\\ISW2\\Projects\\GitRepository\\" ;

        //Retrieve info from JIRA and execute proportion
        JiraRetriever retriever = new JiraRetriever() ;
        List<VersionInfo> versionInfoList = retriever.retrieveVersions(project_name) ;
        System.out.print("\n ----------------------------------------------\n\t\t\t"+project_name.toUpperCase()+" versions list N:" +versionInfoList.size());
        retriever.printVersionList(versionInfoList);
        List<BugTicket> bugTickets = retriever.retrieveBugTicket(project_name,versionInfoList) ;
        List<String> bugTicketsKeys = retriever.getIssueKeyList(bugTickets);
        Proportion.proportion(bugTickets,versionInfoList,project_name);

        //Retrieve commits form git for each ticket
        CommitRetriever commitRetriever = new CommitRetriever() ;
        List<RevCommit> commitList = commitRetriever.retrieveAllCommitsInfo(repoPath + project_name, project_name);
        Map<String, ArrayList<RevCommit>> ticketAndAssociatedCommit = commitRetriever.retrieveCommitFromTickets(bugTicketsKeys, commitList);



        System.out.println("\n\nGoodbye");

    }
}