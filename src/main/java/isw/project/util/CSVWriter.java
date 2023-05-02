package isw.project.util;

import isw.project.file_model.ClassInfoFile;
import isw.project.file_model.CsvEnumaration;
import isw.project.model.ClassInfo;
import isw.project.model.VersionInfo;
import isw.project.retriever.ClassInfoRetriever;
import org.eclipse.jgit.api.errors.GitAPIException;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class CSVWriter {
    private CSVWriter(){}
    public static void writeCsvPerRelease(String projName, List<ClassInfo> javaClassesList, int lastVersionID) throws IOException {

        for(int i=1; i<=lastVersionID; i++) {
            List<ClassInfo> filteredJavaClassesList = ClassInfoUtil.filterJavaClassesByVersion(javaClassesList, i);
            if(filteredJavaClassesList.size() == 0)
                continue;
            ClassInfoFile labelingTesting = new ClassInfoFile(projName, CsvEnumaration.TESTING, 0, i, filteredJavaClassesList);
            labelingTesting.writeOnArff(false);	//"true" indicates that csv file will be deleted and only arff file will remain

        }

    }
    public static void writeArffForWalkForward(String projName, List<ClassInfo> javaClassesList, List<VersionInfo> versionInfoList, ClassInfoRetriever classInfoRetriever) throws IOException, GitAPIException {
        //VersionInfoList contains only version with commit associated
        int end;
        if (versionInfoList.size()%2 == 0) end = versionInfoList.size()/2;
        else end = (versionInfoList.size()+1)/2;

        //Since it has 2 version, start WalkForward
        for(int i=2; i<=end; i++){
            List<ClassInfo> filteredTrainingJavaClassesList = new ArrayList<>();

            for (int j=1; j<i; j++){
                int versionID = versionInfoList.get(j-1).getVersion().getVersionInt();
                //Get classes until the version under testing
                List<ClassInfo> temporaryFilteredJavaClassesList = ClassInfoUtil.filterJavaClassesByVersion(javaClassesList,versionID );
                filteredTrainingJavaClassesList.addAll(temporaryFilteredJavaClassesList);
            }
            int trainingVersionID = versionInfoList.get(i-2).getVersion().getVersionInt();
            classInfoRetriever.labelClassesUntilVersionID(versionInfoList,filteredTrainingJavaClassesList,trainingVersionID);

            ClassInfoFile labelingTraining = new ClassInfoFile(projName, CsvEnumaration.TRAINING, i-1, i-1, filteredTrainingJavaClassesList);
            labelingTraining.writeOnArff(false);

            int testingVersionID = versionInfoList.get(i-1).getVersion().getVersionInt();

            List<ClassInfo> filteredTestingJavaClassesList = ClassInfoUtil.filterJavaClassesByVersion(javaClassesList, testingVersionID);

            classInfoRetriever.labelClasses(versionInfoList,filteredTestingJavaClassesList);

            ClassInfoFile labelingTesting = new ClassInfoFile(projName, CsvEnumaration.TESTING, i-1, i, filteredTestingJavaClassesList);
            labelingTesting.writeOnArff(false);

        }
    }

}
