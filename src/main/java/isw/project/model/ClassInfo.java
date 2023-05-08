package isw.project.model;

import org.eclipse.jgit.revwalk.RevCommit;

import java.util.ArrayList;
import java.util.List;

public class ClassInfo {
    private String name;
    private String content;
    private final Version version;
    private List<RevCommit> commits;	//These are the commits of the specified release that have modified the class
    private boolean isBuggy;

    private int size;
    private int nr;
    private int nFix;
    private int nAuth;
    private int locAdded;
    private int maxLocAdded;
    private double avgLocAdded;
    private int churn;
    private int maxChurn;
    private double avgChurn;

    private final List<Integer> addedLinesList;
    private final List<Integer> deletedLinesList;

    public List<Integer> getAddedLinesList() {
        return addedLinesList;
    }

    public List<Integer> getDeletedLinesList() {
        return deletedLinesList;
    }

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

    public int getnFix() {
        return nFix;
    }

    public void updateNFix() {
        this.nFix += 1;
    }


    public ClassInfo(String name, String content, Version version){
      this.name = name;
      this.content = content;
      this.version = version;
      this.commits = new ArrayList<>();
      this.isBuggy = false;

      this.size = 0;
      this.nr = 0;
      this.nFix = 0;
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

    public String isBuggy() {
        if(isBuggy) return "true";

        return "false";
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

    public Version getVersion() {
        return version;
    }

}