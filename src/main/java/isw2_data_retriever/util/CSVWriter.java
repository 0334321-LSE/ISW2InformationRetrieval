package isw2_data_retriever.util;

import isw2_data_retriever.file_model.ClassInfoFile;
import isw2_data_retriever.file_model.CsvEnumaration;
import isw2_data_retriever.model.ClassInfo;
import isw2_data_retriever.model.VersionInfo;
import isw2_data_retriever.model.Version;
import isw2_data_retriever.retriever.ClassInfoRetriever;
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
            ClassInfoFile labelingTesting = new ClassInfoFile(projName, CsvEnumaration.TESTING, i-1, filteredJavaClassesList);
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
            // TODO quando facciamo labeling delle classi, consideriamo i ticket fino all'ultima versione di training per le training classes e una versione in più per testing classes
            //TODO RECALCULATE BUGGYNESS
            for (int j=1; j<i; j++){
                int versionID = versionInfoList.get(j-1).getVersion().getVersionInt();
                //Get classes until the version under testing
                List<ClassInfo> temporaryFilteredJavaClassesList = ClassInfoUtil.filterJavaClassesByVersion(javaClassesList,versionID );
                filteredTrainingJavaClassesList.addAll(temporaryFilteredJavaClassesList);
            }
            int versionID = versionInfoList.get(i-1).getVersion().getVersionInt();
            classInfoRetriever.labelClassesUntilVersionID(versionInfoList,filteredTrainingJavaClassesList,versionID);

            ClassInfoFile labelingTraining = new ClassInfoFile(projName, CsvEnumaration.TRAINING, i-1, filteredTrainingJavaClassesList);
            labelingTraining.writeOnArff(false);

            List<ClassInfo> filteredTestingJavaClassesList = ClassInfoUtil.filterJavaClassesByVersion(javaClassesList, versionID);
            classInfoRetriever.labelClassesUntilVersionID(versionInfoList,filteredTestingJavaClassesList,versionID);

            ClassInfoFile labelingTesting = new ClassInfoFile(projName, CsvEnumaration.TESTING, i, filteredTestingJavaClassesList);
            labelingTesting.writeOnArff(false);

        }
    }
}
