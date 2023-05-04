package isw.project.retriever;

import isw.project.model.Version;
import isw.project.model.VersionInfo;
import isw.project.model.BugTicket;
import isw.project.model.ClassInfo;
import isw.project.util.ClassInfoUtil;
import isw.project.util.VersionUtil;
import org.eclipse.jgit.api.Git;
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

import static isw.project.model.ClassInfo.updateJavaClassBuggyness;

public class ClassInfoRetriever {

    private Git gitReference;
    private Repository repository;
    private final List<Version> versionList;
    private List<BugTicket> ticketsWithAV;

    public ClassInfoRetriever(String repoPath, List<Version> versionList, List<BugTicket> bugTicketList) throws IOException {
        RepositoryBuilder repositoryBuilder = new RepositoryBuilder();
        Repository repo = repositoryBuilder.setGitDir(new File(repoPath + "/.git")).build() ;
        Git git = new Git(repo) ;

        this.gitReference = git;
        this.repository = git.getRepository();
        this.versionList = versionList;

        this.ticketsWithAV = bugTicketList;
    }

    /** Get java-classes mapped into <String String> map where the first is the class path and
     the second is the code of the class*/
    private Map<String, String> getClasses(RevCommit commit) throws IOException {

        Map<String, String> javaClasses = new HashMap<>();

        RevTree tree = commit.getTree();	//We get the tree of the files and the directories that were belonging to the repository when commit was pushed
        TreeWalk treeWalk = new TreeWalk(this.repository);	//We use a TreeWalk to iterate over all files in the Tree recursively
        treeWalk.addTree(tree);
        treeWalk.setRecursive(true);

        while(treeWalk.next()) {
            //We are keeping only Java classes that are not involved in tests
            if(treeWalk.getPathString().contains(".java") && !treeWalk.getPathString().contains("/test/")) {
                //We are retrieving (name class, content class) couples
                javaClasses.put(treeWalk.getPathString(), new String(this.repository.open(treeWalk.getObjectId(0)).getBytes(), StandardCharsets.UTF_8));
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

    /**Used to label the buggy of training set classes, with snoring, so without using all the tickets */
    public void labelClassesUntilVersionID(List<VersionInfo> versionInfoList, List<ClassInfo> javaClasses, int versionID) throws  IOException {
        List<BugTicket> bugTicketList = VersionUtil.getAssociatedTicketUntilVersionID(this.ticketsWithAV,versionID);
        ClassInfoUtil.initializateBuggyness(javaClasses);
        //label classes only with the available tickets in that version
        for(BugTicket ticket : bugTicketList) {
            doLabeling(javaClasses, ticket, versionInfoList);

        }

    }
    /**Used to label the buggy of testing set classes, without snoring using all the tickets */
    public void labelClasses(List<VersionInfo> versionInfoList, List<ClassInfo> javaClasses) throws  IOException {
        ClassInfoUtil.initializateBuggyness(javaClasses);
        //Label testing set with all the available tickets
        for(BugTicket ticket : this.ticketsWithAV) {
            doLabeling(javaClasses, ticket, versionInfoList);

        }

    }

    /**For each commit, obtain associated version, the classes modified in that version and update buggy metrics*/
    private void doLabeling(List<ClassInfo> javaClasses, BugTicket ticket, List<VersionInfo> versionInfoList) throws IOException {

        List<RevCommit> ticketAssociatedCommit = ticket.getAssociatedCommit();

        for(RevCommit commit : ticketAssociatedCommit) {
            Version associatedVersion = VersionInfo.getVersionOfCommit(commit, versionInfoList);
            //associatedVersion can be null if commit date is after last release date; in that case we ignore the commit
            //(it is trying to fix a issue that hypothetically should be already closed)
            if(associatedVersion != null) {
                List<String> modifiedClasses = getModifiedClasses(commit);

                for(String modifiedClass : modifiedClasses) {
                    updateJavaClassBuggyness(javaClasses, modifiedClass, ticket.getInjectedVersion(), associatedVersion);

                }

            }

        }

    }

    public List<String> getModifiedClasses(RevCommit commit) throws IOException {
        //Here there will be the names of the classes that have been modified by the commit
        List<String> modifiedClasses = new ArrayList<>();

        try(DiffFormatter diffFormatter = new DiffFormatter(DisabledOutputStream.INSTANCE);
            ObjectReader reader = this.repository.newObjectReader()) {

            CanonicalTreeParser newTreeIter = new CanonicalTreeParser();
            ObjectId newTree = commit.getTree();
            newTreeIter.reset(reader, newTree);

            //It's the previous commit of the commit we are considering
            RevCommit commitParent = commit.getParent(0);
            CanonicalTreeParser oldTreeIter = new CanonicalTreeParser();
            ObjectId oldTree = commitParent.getTree();
            oldTreeIter.reset(reader, oldTree);

            diffFormatter.setRepository(this.repository);
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

    /** Returns ClassInfo list obtained from the classes contained into a VersionInfo list*/
    public static List<ClassInfo> buildAllJavaClasses(List<VersionInfo> CommitsAssociatedWithVersion) {

        List<ClassInfo> javaClasses = new ArrayList<>();

        for(VersionInfo versionInfo : CommitsAssociatedWithVersion) {
            if(versionInfo.getVersion().getVersionName().equals("NULL"))
                continue;
            for(Map.Entry<String, String> entryMap : versionInfo.getJavaClasses().entrySet()) {
                javaClasses.add(new ClassInfo(entryMap.getKey(), entryMap.getValue(), versionInfo.getVersion()));

            }

        }

        return javaClasses;
    }


    /** This method, for each VersionInfo , retrieves all the classes that were present
     * on that version date, and then sets these classes as attribute of the instance*/
    public void getVersionAndClassAssociation(List<VersionInfo> CommitsAssociatedWithVersion) throws IOException {

        for(VersionInfo versionInfo : CommitsAssociatedWithVersion) {
            if(versionInfo.getCommitList().isEmpty())
                //jmp to next iteration if that version doesn't have commits
                continue;
            Map<String, String> javaClasses = getClasses(versionInfo.getLastCommit());
            versionInfo.setJavaClasses(javaClasses);

        }

    }

    /** For each ClassInfo instance, retrieves a list of ALL the commits (not only the ones associated with some ticket) that have modified
     * the specified class for the specified release (where class and release are Class info attributes)*/
    public void assignCommitsToClasses(List<ClassInfo> javaClasses, List<RevCommit> commits, List<VersionInfo> CommitsAssociatedWithVersion) throws IOException {

        for(RevCommit commit : commits) {
            Version associatedVersion = Version.getVersionOfCommit(commit, CommitsAssociatedWithVersion);

            if(associatedVersion != null) {		//There are also commits with no associatedRelease because their date is latter than last release date
                List<String> modifiedClasses = getModifiedClasses(commit);

                for(String modifClass : modifiedClasses) {
                    ClassInfo.updateJavaClassCommits(javaClasses, modifClass, associatedVersion, commit);

                }

            }

        }

    }

    public void computeAddedAndDeletedLinesList(ClassInfo javaClass) throws IOException {

        for(RevCommit comm : javaClass.getCommits()) {
            try(DiffFormatter diffFormatter = new DiffFormatter(DisabledOutputStream.INSTANCE)) {

                RevCommit parentComm = comm.getParent(0);

                diffFormatter.setRepository(this.repository);
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
            addedLines += edit.getEndB() - edit.getBeginB();

        }
        return addedLines;

    }

    private int getDeletedLines(DiffFormatter diffFormatter, DiffEntry entry) throws IOException {

        int deletedLines = 0;
        for(Edit edit : diffFormatter.toFileHeader(entry).toEditList()) {
            deletedLines += edit.getEndA() - edit.getBeginA();

        }
        return deletedLines;

    }

}
