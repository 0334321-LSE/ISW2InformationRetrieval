package ISW2.DataRetriever;

import ISW2.DataRetriever.control.Proportion;
import ISW2.DataRetriever.model.BugTicket;
import ISW2.DataRetriever.model.ClassInfo;
import ISW2.DataRetriever.model.CommitInfo;
import ISW2.DataRetriever.model.VersionInfo;
import ISW2.DataRetriever.util.ClassInfoRetriever;
import ISW2.DataRetriever.util.CommitRetriever;
import ISW2.DataRetriever.util.JiraRetriever;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.*;

public class Main {

    public static void main(String[] args) throws IOException, URISyntaxException, GitAPIException, ParseException {
        findProjectData("bookkeeper");
    }

    private static void findProjectData(String project_name) throws URISyntaxException, IOException, GitAPIException, ParseException {
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
        List<RevCommit> allCommitsList = commitRetriever.retrieveAllCommitsInfo(repoPath + project_name, project_name);
        commitRetriever.retrieveCommitFromTickets(bugTickets, allCommitsList);
        //TODO is necessary to match ALL the commits to the version not only the right one
        //For each version, add the list of all commits of that version
        List<CommitInfo> commitInfo = commitRetriever.getVersionAndCommitsAssociations(allCommitsList,versionInfoList);
        ClassInfoRetriever classInfoRetriever = new ClassInfoRetriever(repoPath + project_name,versionInfoList,bugTickets);
        classInfoRetriever.getVersionAndClassAssociation(commitInfo);
        //
        List<ClassInfo> javaClassesList = classInfoRetriever.labelClasses(commitInfo);


        System.out.println("\n\nGoodbye");

    }
}