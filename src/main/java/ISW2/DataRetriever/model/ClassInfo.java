package ISW2.DataRetriever.model;

import org.eclipse.jgit.revwalk.RevCommit;

import java.util.ArrayList;
import java.util.List;

public class ClassInfo {
    private String name;
    private String content;
    private VersionInfo versionInfo;
    private List<RevCommit> commits;	//These are the commits of the specified release that have modified the class
    private boolean isBuggy;

    private int size;
    private int nr;
    private int nAuth;
    private int locAdded;
    private int maxLocAdded;
    private double avgLocAdded;
    private int churn;
    private int maxChurn;
    private double avgChurn;

    private List<Integer> addedLinesList;
    private List<Integer> deletedLinesList;


    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setCommits(List<RevCommit> commits) {
        this.commits = commits;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getNr() {
        return nr;
    }

    public void setNr(int nr) {
        this.nr = nr;
    }

    public int getnAuth() {
        return nAuth;
    }

    public void setnAuth(int nAuth) {
        this.nAuth = nAuth;
    }

    public int getLocAdded() {
        return locAdded;
    }

    public void setLocAdded(int locAdded) {
        this.locAdded = locAdded;
    }

    public int getMaxLocAdded() {
        return maxLocAdded;
    }

    public void setMaxLocAdded(int maxLocAdded) {
        this.maxLocAdded = maxLocAdded;
    }

    public double getAvgLocAdded() {
        return avgLocAdded;
    }

    public void setAvgLocAdded(double avgLocAdded) {
        this.avgLocAdded = avgLocAdded;
    }

    public int getChurn() {
        return churn;
    }

    public void setChurn(int churn) {
        this.churn = churn;
    }

    public int getMaxChurn() {
        return maxChurn;
    }

    public void setMaxChurn(int maxChurn) {
        this.maxChurn = maxChurn;
    }

    public double getAvgChurn() {
        return avgChurn;
    }

    public void setAvgChurn(double avgChurn) {
        this.avgChurn = avgChurn;
    }

    public ClassInfo(String name, String content, VersionInfo versionInfo){
      this.name = name;
      this.content = content;
      this.versionInfo = versionInfo;
      this.commits = new ArrayList<>();
      this.isBuggy = false;

      this.size = 0;
      this.nr = 0;
      this.nAuth = 0;
      this.locAdded = 0;
      this.maxLocAdded = 0;
      this.avgLocAdded = 0;
      this.churn = 0;
      this.maxChurn = 0;
      this.avgChurn = 0;

      this.addedLinesList = new ArrayList<>();
      this.deletedLinesList = new ArrayList<>();
  }

    public boolean isBuggy() {
        return isBuggy;
    }

    public void setBuggy(boolean buggy) {
        isBuggy = buggy;
    }

    public String getName() {
        return name;
    }

    public List<RevCommit> getCommits() {
        return commits;
    }

    public void setName(String name) {
        this.name = name;
    }

    public VersionInfo getVersionInfo() {
        return versionInfo;
    }

    public void setVersionInfo(VersionInfo versionInfo) {
        this.versionInfo = versionInfo;
    }


    /**  Check if the classes into the ClassInfo list have been modified, if iv <= ov < fv, then javaClass is buggy */
    public static void updateJavaClassBuggyness(List<ClassInfo> javaClasses, String className, VersionInfo injectedVersion, VersionInfo fixedVersion) {
        //fv is related to the single commit, not to the ticket

        for(ClassInfo javaClass : javaClasses) {
            /*if javaClass has been modified by commit (className contains modified class name) and
            is related to a version v such that iv <= ov < fv, then javaClass is buggy*/
            if(javaClass.getName().equals(className) && javaClass.getVersionInfo().getVersionInt() >= injectedVersion.getVersionInt() && javaClass.getVersionInfo().getVersionInt() < fixedVersion.getVersionInt()) {
                javaClass.setBuggy(true);

            }

        }

    }

    /** check if the classes into the list have been modified in the same release of the commit, in that case add it*/
    public static void updateJavaClassCommits(List<ClassInfo> javaClasses, String className, VersionInfo associatedVersion, RevCommit commit) {

        for(ClassInfo javaClass : javaClasses) {
            //if javaClass has been modified by commit (that is className) and is related to the same release of commit, then add commit to javaClass.commits
            if(javaClass.getName().equals(className) && javaClass.getVersionInfo().getVersionInt() == associatedVersion.getVersionInt() && !javaClass.getCommits().contains(commit)) {
                javaClass.getCommits().add(commit);

            }

        }

    }
}