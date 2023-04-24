package ISW2.DataRetriever.retriever;

import ISW2.DataRetriever.model.BugTicket;
import ISW2.DataRetriever.model.ClassInfo;
import ISW2.DataRetriever.model.CommitInfo;
import ISW2.DataRetriever.model.VersionInfo;
import ISW2.DataRetriever.util.ClassInfoUtil;
import ISW2.DataRetriever.util.VersionInfoUtil;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryBuilder;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.util.io.DisabledOutputStream;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static ISW2.DataRetriever.util.ClassInfoUtil.updateJavaClassBuggyness;
import static ISW2.DataRetriever.util.CommitInfoUtil.getVersionOfCommit;

public class ClassInfoRetriever {

    private Git git;
    private Repository repo;
    private List<VersionInfo> versionInfoList;
    private List<BugTicket> ticketsWithAV;

    public ClassInfoRetriever(String repoPath, List<VersionInfo> versionInfoList, List<BugTicket> bugTicketList) throws IOException {
        RepositoryBuilder repositoryBuilder = new RepositoryBuilder();
        Repository repo = repositoryBuilder.setGitDir(new File(repoPath + "/.git")).build() ;
        Git git = new Git(repo) ;

        this.git = git;
        this.repo= git.getRepository();
        this.versionInfoList = versionInfoList;

        this.ticketsWithAV = bugTicketList;
    }

    /** Get java-classes mapped into <String String> map where the first is the class path and
     the second is the code of the class*/
    private Map<String, String> getClasses(RevCommit commit) throws IOException {

        Map<String, String> javaClasses = new HashMap<>();

        RevTree tree = commit.getTree();	//We get the tree of the files and the directories that were belonging to the repository when commit was pushed
        TreeWalk treeWalk = new TreeWalk(this.repo);	//We use a TreeWalk to iterate over all files in the Tree recursively
        treeWalk.addTree(tree);
        treeWalk.setRecursive(true);

        while(treeWalk.next()) {
            //We are keeping only Java classes that are not involved in tests
            if(treeWalk.getPathString().contains(".java") && !treeWalk.getPathString().contains("/test/")) {
                //We are retrieving (name class, content class) couples
                javaClasses.put(treeWalk.getPathString(), new String(this.repo.open(treeWalk.getObjectId(0)).getBytes(), StandardCharsets.UTF_8));
            }
        }
        treeWalk.close();

        return javaClasses;

    }

    /** This method returns a list of JavaClass instances with:
	 * - Class name
	 * - Class content
	 * - Release
	 * - Binary value "isBuggy"*/

    public List<ClassInfo> labelClasses(List<CommitInfo> relCommAssociations) throws GitAPIException, IOException {

        List<ClassInfo> javaClasses = buildAllJavaClasses(relCommAssociations);

        for(BugTicket ticket : this.ticketsWithAV) {
            doLabeling(javaClasses, ticket, relCommAssociations);

        }
        return javaClasses;

    }

    private void doLabeling(List<ClassInfo> javaClasses, BugTicket ticket, List<CommitInfo> commitInfoList) throws GitAPIException, IOException {

        List<RevCommit> commitsAssociatedWIssue = ticket.getAssociatedCommit();
        //TODO fix comments
        for(RevCommit commit : commitsAssociatedWIssue) {
            VersionInfo associatedVersionInfo = getVersionOfCommit(commit, commitInfoList);
            //associatedVersionInfo can be null if commit date is after last release date; in that case we ignore the commit
            //(it is trying to fix a issue that hypothetically should be already closed)
            if(associatedVersionInfo != null) {
                List<String> modifiedClasses = getModifiedClasses(commit);

                for(String modifClass : modifiedClasses) {
                    ClassInfoUtil.updateJavaClassBuggyness(javaClasses, modifClass, ticket.getInjectedVersion(), associatedVersionInfo);

                }

            }

        }

    }

    private List<String> getModifiedClasses(RevCommit commit) throws IOException {
        //Here there will be the names of the classes that have been modified by the commit
        List<String> modifiedClasses = new ArrayList<>();

        try(DiffFormatter diffFormatter = new DiffFormatter(DisabledOutputStream.INSTANCE);
            ObjectReader reader = this.repo.newObjectReader()) {

            CanonicalTreeParser newTreeIter = new CanonicalTreeParser();
            ObjectId newTree = commit.getTree();
            newTreeIter.reset(reader, newTree);

            //It's the previous commit of the commit we are considering
            RevCommit commitParent = commit.getParent(0);
            CanonicalTreeParser oldTreeIter = new CanonicalTreeParser();
            ObjectId oldTree = commitParent.getTree();
            oldTreeIter.reset(reader, oldTree);

            diffFormatter.setRepository(this.repo);
            List<DiffEntry> entries = diffFormatter.scan(oldTreeIter, newTreeIter);

            //Every entry contains info for each file involved in the commit (old path name, new path name, change type (that could be MODIFY, ADD, RENAME, etc.))
            for(DiffEntry entry : entries) {
                //We are keeping only Java classes that are not involved in tests
                if(entry.getChangeType().equals(DiffEntry.ChangeType.MODIFY) && entry.getNewPath().contains(".java") && !entry.getNewPath().contains("/test/")) {
                    modifiedClasses.add(entry.getNewPath());
                }

            }

        } catch(ArrayIndexOutOfBoundsException e) {
            //commit has no parents: skip this commit, return an empty list and go on

        }

        return modifiedClasses;

    }

    /** Returns ClassInfo list obtained from the classes contained into a CommitInfo list*/
    public static List<ClassInfo> buildAllJavaClasses(List<CommitInfo> CommitsAssociatedWithVersion) {

        List<ClassInfo> javaClasses = new ArrayList<>();

        for(CommitInfo commitInfo : CommitsAssociatedWithVersion) {
            if(commitInfo.getVersionInfo().getVersionName().equals("NULL"))
                continue;
            for(Map.Entry<String, String> entryMap : commitInfo.getJavaClasses().entrySet()) {
                javaClasses.add(new ClassInfo(entryMap.getKey(), entryMap.getValue(), commitInfo.getVersionInfo()));

            }

        }
        return javaClasses;

    }

    /** This method, for each CommitInfo , retrieves all the classes that were present
     * on that version date, and then sets these classes as attribute of the instance*/
    public void getVersionAndClassAssociation(List<CommitInfo> CommitsAssociatedWithVersion) throws IOException {

        for(CommitInfo commitInfo : CommitsAssociatedWithVersion) {
            if(commitInfo.getCommitList().isEmpty())
                //jmp to next iteration if that version doesn't have commits
                continue;
            Map<String, String> javaClasses = getClasses(commitInfo.getLastCommit());
            commitInfo.setJavaClasses(javaClasses);

        }

    }

    /** For each ClassInfo instance, retrieves a list of ALL the commits (not only the ones associated with some ticket) that have modified
     * the specified class for the specified release (where class and release are Class info attributes)*/
    public void assignCommitsToClasses(List<ClassInfo> javaClasses, List<RevCommit> commits, List<CommitInfo> CommitsAssociatedWithVersion) throws IOException {

        for(RevCommit commit : commits) {
            VersionInfo associatedVersion = VersionInfoUtil.getVersionOfCommit(commit, CommitsAssociatedWithVersion);

            if(associatedVersion != null) {		//There are also commits with no associatedRelease because their date is latter than last release date
                List<String> modifiedClasses = getModifiedClasses(commit);

                for(String modifClass : modifiedClasses) {
                    ClassInfoUtil.updateJavaClassCommits(javaClasses, modifClass, associatedVersion, commit);

                }

            }

        }

    }

    /** This method initializes two lists:
     <br /> List of numbers of added lines by each commit; every entry is associated to one specific commit
     <br /> List of numbers of deleted lines by each commit; every entry is associated to one specific commit
     <br /> These lists will be used to calculate sum, max & avg*/
    public void computeAddedAndDeletedLinesList(ClassInfo javaClass) throws IOException {

        for(RevCommit comm : javaClass.getCommits()) {
            try(DiffFormatter diffFormatter = new DiffFormatter(DisabledOutputStream.INSTANCE)) {

                RevCommit parentComm = comm.getParent(0);

                diffFormatter.setRepository(this.repo);
                diffFormatter.setDiffComparator(RawTextComparator.DEFAULT);

                List<DiffEntry> diffs = diffFormatter.scan(parentComm.getTree(), comm.getTree());
                for(DiffEntry entry : diffs) {
                    if(entry.getNewPath().equals(javaClass.getName())) {
                        javaClass.getAddedLinesList().add(getAddedLines(diffFormatter, entry));
                        javaClass.getDeletedLinesList().add(getDeletedLines(diffFormatter, entry));

                    }

                }

            } catch(ArrayIndexOutOfBoundsException e) {
                //commit has no parents: skip this commit, return an empty list and go on

            }

        }


    }

    private int getAddedLines(DiffFormatter diffFormatter, DiffEntry entry) throws IOException {

        int addedLines = 0;
        for(Edit edit : diffFormatter.toFileHeader(entry).toEditList()) {
            addedLines += edit.getEndA() - edit.getBeginA();

        }
        return addedLines;

    }

    private int getDeletedLines(DiffFormatter diffFormatter, DiffEntry entry) throws IOException {

        int deletedLines = 0;
        for(Edit edit : diffFormatter.toFileHeader(entry).toEditList()) {
            deletedLines += edit.getEndB() - edit.getBeginB();

        }
        return deletedLines;

    }

}
