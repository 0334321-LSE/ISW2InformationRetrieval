package isw.project.control;

import isw.project.file_model.EvaluationFile;
import isw.project.model.*;
import isw.project.retriever.ClassInfoRetriever;
import isw.project.retriever.CommitRetriever;
import isw.project.retriever.JiraRetriever;
import isw.project.retriever.WekaRetriever;
import isw.project.util.CSVWriter;
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

        //Retrieve info from JIRA and execute proportion
        JiraRetriever retriever = new JiraRetriever() ;
        List<Version> versionList = retriever.retrieveVersions(projectName) ;
        LOGGER.log(Level.INFO,"\n ----------------------------------------------\n\t\t\t {} versions list N:"+versionList.size()
                ,projectName.toUpperCase());
        retriever.printVersionList(versionList);
        List<BugTicket> bugTickets = retriever.retrieveBugTicket(projectName, versionList) ;
        Proportion.proportion(bugTickets, versionList,projectName);

        //Retrieve commits form git for each ticket
        CommitRetriever commitRetriever = new CommitRetriever(versionList) ;
        List<RevCommit> allCommitsList = commitRetriever.retrieveAllCommitsInfo(repoPath.toString(), projectName);
        commitRetriever.retrieveCommitFromTickets(bugTickets, allCommitsList);

        //For each version, add the list of all commits of that version
        List<VersionInfo> versionInfoList = commitRetriever.getVersionAndCommitsAssociations(allCommitsList, versionList);
        ClassInfoRetriever classInfoRetriever = new ClassInfoRetriever(repoPath.toString(), versionList,bugTickets);
        classInfoRetriever.getVersionAndClassAssociation(versionInfoList);

        //Obtain a list of classInfo with all the java classes in the project
        List<ClassInfo> javaClassesList = ClassInfoRetriever.buildAllJavaClasses(versionInfoList);
        classInfoRetriever.assignCommitsToClasses(javaClassesList,allCommitsList, versionInfoList);

        //Compute metrics update javaClassesList adding metric values
        ComputeMetrics computeMetrics = new ComputeMetrics(classInfoRetriever, javaClassesList, versionInfoList, bugTickets);
        javaClassesList = computeMetrics.doAllMetricsComputation();

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
