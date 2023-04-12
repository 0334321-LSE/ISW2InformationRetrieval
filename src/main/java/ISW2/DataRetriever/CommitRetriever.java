package ISW2.DataRetriever;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.LogCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryBuilder;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CommitRetriever {

    public List<RevCommit> retrieveAllCommitsInfo(String repoPath, String projectName) throws IOException, GitAPIException {
        RepositoryBuilder repositoryBuilder = new RepositoryBuilder();
        Repository repo = repositoryBuilder.setGitDir(new File(repoPath + "/.git")).build() ;
        Git git = new Git(repo) ;
        LogCommand logCommand = git.log() ;
        Iterable<RevCommit> commitIterable = logCommand.call() ;

        List<RevCommit> revCommitList = new ArrayList<>() ;
        for (RevCommit commit : commitIterable) {
            revCommitList.add(commit) ;
        }
        revCommitList.sort(Comparator.comparingLong(o -> o.getAuthorIdent().getWhen().getTime()));
        saveAllCommitsOnJSON(revCommitList, projectName);
        return revCommitList ;
    }


    public Map<String, ArrayList<RevCommit>> retrieveCommitFromTickets(List<String> ticketsIDs, List<RevCommit> commitList) throws GitAPIException, IOException {
        /*FileRepositoryBuilder repositoryBuilder = new FileRepositoryBuilder();
        Repository repo = repositoryBuilder.setGitDir(new File(repoPath + "/.git")).build() ;
        Git git = new Git(repo) ;
        LogCommand logCommand = git.log() ;
        Iterable<RevCommit> commitIterable = logCommand.call() ;
        */
        Map<String, ArrayList<RevCommit>> ticketMap = new LinkedHashMap<>();
        for (String ticketID : ticketsIDs) {
            ticketMap.put(ticketID, new ArrayList<>());
        }

        for (RevCommit commit : commitList) {
            String commitTicket = matchTicketAndCommit(commit.getFullMessage(), ticketsIDs);
            if (commitTicket.compareTo("") != 0) {
                ticketMap.get(commitTicket).add(commit);
            }
        }
        try {
            if (!ticketMap.isEmpty())
                System.out.println("Found commits associated to tickets");
        } catch(Exception e) {
            System.out.println("Somethings went wrong with during the ticket-commit matching");
        }

        return ticketMap ;
    }

    private String matchTicketAndCommit(String commitMessage, List<String> ticketsIDs) {
        for (String ticketID : ticketsIDs) {
            if (commitMessage.contains(ticketID)) {
                return ticketID ;
            }
        }
        return "" ;
    }

    private void saveAllCommitsOnJSON(List<RevCommit> commitList, String projectName ) throws IOException {
        FileWriter file = new FileWriter("./"+projectName+"Commits.json");
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
}
