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

    public void setName(String name) {
        this.name = name;
    }

    public VersionInfo getVersionInfo() {
        return versionInfo;
    }

    public void setVersionInfo(VersionInfo versionInfo) {
        this.versionInfo = versionInfo;
    }

    public static void updateJavaClassBuggyness(List<ClassInfo> javaClasses, String className, VersionInfo injectedVersion, VersionInfo fixedVersion) {
        //fv is related to the single commit, not to the ticket

        for(ClassInfo javaClass : javaClasses) {
            //if javaClass has been modified by commit (that is className) and is related to a version v such that iv <= ov < fv, then javaClass is buggy
            if(javaClass.getName().equals(className) && javaClass.getVersionInfo().getVersionInt() >= injectedVersion.getVersionInt() && javaClass.getVersionInfo().getVersionInt() < fixedVersion.getVersionInt()) {
                javaClass.setBuggy(true);

            }

        }

    }
}