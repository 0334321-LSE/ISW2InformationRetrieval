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

    public List<Integer> getAddedLinesList() {
        return addedLinesList;
    }

    public List<Integer> getDeletedLinesList() {
        return deletedLinesList;
    }

    public void setDeletedLinesList(List<Integer> deletedLinesList) {
        this.deletedLinesList = deletedLinesList;
    }

    public void setAddedLinesList(List<Integer> addedLinesList) {
        this.addedLinesList = addedLinesList;
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



}