package isw2_data_retriever.control;

import isw2_data_retriever.file_model.EvaluationFile;
import isw2_data_retriever.model.*;
import isw2_data_retriever.retriever.ClassInfoRetriever;
import isw2_data_retriever.retriever.CommitRetriever;
import isw2_data_retriever.retriever.JiraRetriever;
import isw2_data_retriever.retriever.WekaRetriever;
import isw2_data_retriever.util.CSVWriter;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ExecutionFlow {
    private static final Logger LOGGER = Logger.getLogger(ExecutionFlow.class.getName());

    public static void findProjectData(String projectName) throws Exception {
        String repoPath = "C:\\Users\\39388\\OneDrive\\Desktop\\ISW2\\Projects\\GitRepository\\" ;

        //Retrieve info from JIRA and execute proportion
        JiraRetriever retriever = new JiraRetriever() ;
        List<Version> versionList = retriever.retrieveVersions(projectName) ;
        LOGGER.info("\n ----------------------------------------------\n\t\t\t"+projectName.toUpperCase()+" versions list N:" + versionList.size());
        retriever.printVersionList(versionList);
        List<BugTicket> bugTickets = retriever.retrieveBugTicket(projectName, versionList) ;
        Proportion.proportion(bugTickets, versionList,projectName);

        //Retrieve commits form git for each ticket
        CommitRetriever commitRetriever = new CommitRetriever(versionList) ;
        List<RevCommit> allCommitsList = commitRetriever.retrieveAllCommitsInfo(repoPath + projectName, projectName);
        commitRetriever.retrieveCommitFromTickets(bugTickets, allCommitsList);

        //For each version, add the list of all commits of that version
        List<VersionInfo> versionInfoList = commitRetriever.getVersionAndCommitsAssociations(allCommitsList, versionList);
        ClassInfoRetriever classInfoRetriever = new ClassInfoRetriever(repoPath + projectName, versionList,bugTickets);
        classInfoRetriever.getVersionAndClassAssociation(versionInfoList);

        //Obtain a list of classInfo with all the java classes in the project
        List<ClassInfo> javaClassesList = ClassInfoRetriever.buildAllJavaClasses(versionInfoList);
        classInfoRetriever.assignCommitsToClasses(javaClassesList,allCommitsList, versionInfoList);

        //Compute metrics update javaClassesList adding metric values
        ComputeMetrics computeMetrics = new ComputeMetrics(classInfoRetriever, javaClassesList, versionInfoList, bugTickets);
        javaClassesList = computeMetrics.doAllMetricsComputation();

        //Create arff file for WalkForward validation technique
        CSVWriter.writeArffForWalkForward(projectName,javaClassesList,versionInfoList, classInfoRetriever);

        int WFIterationNumber;
        if (versionInfoList.size()%2 == 0) WFIterationNumber = versionInfoList.size()/2 +1;
        else WFIterationNumber = (versionInfoList.size()+1)/2;

        //Uses WekaAPI to obtains accuracy metrics of classifiers
        WekaRetriever wekaRetriever = new WekaRetriever(projectName,WFIterationNumber);
        List<ClassifierEvaluation> allEvaluation =  wekaRetriever.testWekaApi();

        //Save on csv the results
        EvaluationFile evaluationFileDetails = new EvaluationFile(projectName, allEvaluation, "details");
        evaluationFileDetails.reportEvaluationOnCsv();

        LOGGER.info("\n\nGoodbye");

    }
}
