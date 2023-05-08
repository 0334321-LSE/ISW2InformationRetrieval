package isw.project.control;

import isw.project.file_model.EvaluationFile;
import isw.project.model.*;
import isw.project.retriever.ClassInfoRetriever;
import isw.project.retriever.CommitRetriever;
import isw.project.retriever.JiraRetriever;
import isw.project.retriever.WekaRetriever;
import isw.project.util.CSVWriter;
import isw.project.util.LogWriter;
import org.eclipse.jgit.revwalk.RevCommit;


import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.List;

import java.util.logging.Level;
import java.util.logging.Logger;

public class ExecutionFlow {

    /** This private constructor to hide the public one: utility classes do not have to be instantiated. */
    private ExecutionFlow(){throw new IllegalStateException("This class does not have to be instantiated.");}
    private static final Logger LOGGER = Logger.getLogger(ExecutionFlow.class.getName());

    /** Starts the operation flow of the program */
    public static void findProjectData(String projectName) throws Exception {
        // "C:\\Users\\39388\\OneDrive\\Desktop\\ISW2\\Projects\\GitRepository\\"
        Path repoPath = Paths.get("C:","Users","39388","OneDrive","Desktop","ISW2","Projects","GitRepository",projectName);

        LOGGER.log(Level.INFO, ()-> String.format("%n%s data retrieving is starting.",projectName.toUpperCase()));
        //Retrieve info from JIRA and execute proportion
        JiraRetriever retriever = new JiraRetriever() ;
        List<Version> versionList = retriever.retrieveVersions(projectName) ;
        LOGGER.log(Level.INFO, ()->String.format("%n ----------------------------------------------%n\t\t\t %s versions list N: %s",projectName.toUpperCase(),versionList.size()));
        retriever.printVersionList(versionList);
        List<BugTicket> bugTicketList = retriever.retrieveBugTicket(projectName, versionList) ;
        Proportion.proportion(bugTicketList, versionList,projectName);

        //Retrieve commits form git for each ticket
        CommitRetriever commitRetriever = new CommitRetriever(versionList) ;
        List<RevCommit> allCommitsList = commitRetriever.retrieveAllCommitsInfo(repoPath.toString(), projectName);
        commitRetriever.retrieveCommitFromTickets(bugTicketList, allCommitsList);
        LogWriter.writeTicketLog(projectName,bugTicketList);

        //For each version, add the list of all commits of that version
        List<VersionInfo> versionInfoList = commitRetriever.getVersionAndCommitsAssociations(allCommitsList, versionList);
        ClassInfoRetriever classInfoRetriever = new ClassInfoRetriever(repoPath.toString(), versionList,bugTicketList);
        classInfoRetriever.getVersionAndClassAssociation(versionInfoList);
        LogWriter.writeVersionLog(projectName,versionInfoList);
        //Remove if that version doesn't have commits
        versionInfoList.removeIf(versionInfo -> versionInfo.getCommitList().isEmpty() );

        //Obtain a list of classInfo with all the java classes in the project
        List<ClassInfo> javaClassesList = ClassInfoRetriever.buildAllJavaClasses(versionInfoList);
        classInfoRetriever.assignCommitsToClasses(javaClassesList,allCommitsList, versionInfoList);

        //Compute metrics update javaClassesList adding metric values
        ComputeMetrics computeMetrics = new ComputeMetrics(classInfoRetriever, javaClassesList, versionInfoList, bugTicketList);
        javaClassesList = computeMetrics.doAllMetricsComputation();
        classInfoRetriever.labelClasses(versionInfoList,javaClassesList);
        LogWriter.writeBuggyClassesLog(projectName,versionInfoList);

        //Create arff file for WalkForward validation technique
        CSVWriter.writeArffForWalkForward(projectName,javaClassesList,versionInfoList, classInfoRetriever);


        int wfIterationNumber;
        if (versionInfoList.size()%2 == 0) wfIterationNumber = versionInfoList.size()/2 +1;
        else wfIterationNumber = (versionInfoList.size()+1)/2;

        //Uses WekaAPI to obtains accuracy metrics of classifiers
        WekaRetriever wekaRetriever = new WekaRetriever(projectName,wfIterationNumber);
        List<ClassifierEvaluation> allEvaluation =  wekaRetriever.walkForwardValidation();

        //Save on csv the results
        EvaluationFile evaluationFileDetails = new EvaluationFile(projectName, allEvaluation, "details");
        evaluationFileDetails.reportEvaluationOnCsv();

        LOGGER.info("\n\nGoodbye");

    }
}
