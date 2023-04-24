package isw2_data_retriever.retriever;

import isw2_data_retriever.model.BugTicket;
import isw2_data_retriever.model.VersionInfo;
import isw2_data_retriever.model.Version;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.revwalk.RevCommit;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static isw2_data_retriever.model.VersionInfo.getCommitsOfVersion;

public class CommitRetriever {

    private List<Version> versionList;

    public CommitRetriever(List<Version> versionList){
        this.versionList = versionList;
    }

    /** Retrieve all commits */
    public List<RevCommit> retrieveAllCommitsInfo(String repoPath, String projectName) throws IOException, GitAPIException {
        RepositoryBuilder repositoryBuilder = new RepositoryBuilder();
        Repository repo = repositoryBuilder.setGitDir(new File(repoPath + "/.git")).build() ;
        Git git = new Git(repo) ;
        List<RevCommit> revCommitList = new ArrayList<>() ;
        // to get all branches
        //List<Ref> branchesList = git.branchList().setListMode(ListBranchCommand.ListMode.ALL).call();
        //for (Ref branch : branchesList) {
        //Iterable<RevCommit> commitsListIterable = git.log().add(repo.resolve(branch.getName())).call();
            Iterable<RevCommit> commitsListIterable = git.log().call();
            LocalDate lastVersionDate = versionList.get(versionList.size()-1).getVersionDate();
            for (RevCommit commit : commitsListIterable) {
                //If commits is before the last version date it's ok
                LocalDate commitDate = LocalDate.from(commit.getAuthorIdent().getWhen().toInstant().atZone(ZoneId.systemDefault()));
                if(commitDate.isBefore(lastVersionDate))
                        revCommitList.add(commit);
                }
        //}
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
        //getRemainingCommits(bugTicketList,commitList);
        System.out.println("\nValid ticket with associated commits are: "+bugTicketList.size());
        System.out.println("\nRemaining commits are: "+countCommit(bugTicketList));

    }

    /** From ticketIssueID retrieve associated commit*/
    private ArrayList<RevCommit> matchTicketIssueIDCommit(List<RevCommit> commitList,String BugTicketID) {
        ArrayList<RevCommit> associatedCommitList = new ArrayList<>();
        Pattern pattern = Pattern.compile( BugTicketID + "+[^0-9]") ;

        for(RevCommit commit: commitList){
            String commitMessage = commit.getFullMessage() ;
            Matcher matcher = pattern.matcher(commitMessage) ;
            if (matcher.find())
                associatedCommitList.add(commit);
        }
        return associatedCommitList;
    }

    /** Returns a list of VersionInfo that associates a version with all the commits to it related,
     * and specifies the last commit in temporal order*/
    public List<VersionInfo> getVersionAndCommitsAssociations(List<RevCommit> allCommitsList, List<Version> versionList) throws ParseException {

        List<VersionInfo> CommitsAssociatedWithVersion = new ArrayList<>();

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        //firstDate is the date of the previous release; for the first release we take 01/01/1900 as lower bound
        LocalDate firstDate = formatter.parse("1900-01-01").toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        for(Version version : versionList) {
            //Skip the NULL version
            if(version.getVersionName().equals("NULL"))
                continue;
            CommitsAssociatedWithVersion.add(getCommitsOfVersion(allCommitsList, version, firstDate));
            firstDate = version.getVersionDate();
        }
        //Remove if that version doesn't have commits
        CommitsAssociatedWithVersion.removeIf(commitInfo -> commitInfo.getCommitList().size()==0 );

        return CommitsAssociatedWithVersion;

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

    private static void discardTicketWithoutCommit(List<BugTicket> bugTicketList){
        bugTicketList.removeIf(bugTicket-> bugTicket.getAssociatedCommit().size()==0);
    }

    public void getRemainingCommits(List<BugTicket> bugTicketList, List<RevCommit> commitList) {
        List<RevCommit> remainingCommit = new ArrayList<>();
        for (BugTicket bugTicket: bugTicketList){
            remainingCommit.addAll(bugTicket.getAssociatedCommit());
        }
        commitList.removeAll(commitList);
        commitList.addAll(remainingCommit);
    }
}
