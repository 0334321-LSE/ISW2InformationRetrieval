package isw2_data_retriever;

import isw2_data_retriever.control.ComputeMetrics;
import isw2_data_retriever.control.Proportion;
import isw2_data_retriever.model.BugTicket;
import isw2_data_retriever.model.ClassInfo;
import isw2_data_retriever.model.VersionInfo;
import isw2_data_retriever.model.Version;
import isw2_data_retriever.retriever.ClassInfoRetriever;
import isw2_data_retriever.retriever.CommitRetriever;
import isw2_data_retriever.retriever.JiraRetriever;
import isw2_data_retriever.util.CSVWriter;
import org.eclipse.jgit.api.errors.GitAPIException  ;
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
        List<Version> versionList = retriever.retrieveVersions(project_name) ;
        System.out.print("\n ----------------------------------------------\n\t\t\t"+project_name.toUpperCase()+" versions list N:" + versionList.size());
        retriever.printVersionList(versionList);
        List<BugTicket> bugTickets = retriever.retrieveBugTicket(project_name, versionList) ;
        Proportion.proportion(bugTickets, versionList,project_name);

        //Retrieve commits form git for each ticket
        CommitRetriever commitRetriever = new CommitRetriever(versionList) ;
        List<RevCommit> allCommitsList = commitRetriever.retrieveAllCommitsInfo(repoPath + project_name, project_name);
        commitRetriever.retrieveCommitFromTickets(bugTickets, allCommitsList);

        //For each version, add the list of all commits of that version
        List<VersionInfo> versionInfoList = commitRetriever.getVersionAndCommitsAssociations(allCommitsList, versionList);
        ClassInfoRetriever classInfoRetriever = new ClassInfoRetriever(repoPath + project_name, versionList,bugTickets);
        classInfoRetriever.getVersionAndClassAssociation(versionInfoList);

        //Obtain a list of classInfo with all the java classes in the project
        List<ClassInfo> javaClassesList = ClassInfoRetriever.buildAllJavaClasses(versionInfoList);
        classInfoRetriever.assignCommitsToClasses(javaClassesList,allCommitsList, versionInfoList);

        //Compute metrics update javaClassesList adding metric values
        ComputeMetrics computeMetrics = new ComputeMetrics(classInfoRetriever, javaClassesList, versionInfoList, bugTickets);
        javaClassesList = computeMetrics.doAllMetricsComputation();

        //CSVWriter.writeCsvPerRelease(project_name,javaClassesList, versionList.size()-1);
        CSVWriter.writeArffForWalkForward(project_name,javaClassesList,versionInfoList, classInfoRetriever);
        System.out.println("\n\nGoodbye");

    }
}