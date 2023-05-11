package isw.project.file_model;

import isw.project.model.ClassInfo;

import org.jetbrains.annotations.NotNull;

import java.io.*;

import java.util.List;


public class ClassInfoFile {

    private final String projName;
    private final CsvEnumaration csvName;
    private final int iterationIndex;

    private final int versionIndex;
    private final List<ClassInfo> javaClassesList;

    public ClassInfoFile(String projName, CsvEnumaration csvName, int iterationIndex, int versionIndex, List<ClassInfo> javaClassesList){
        this.projName = projName;
        this.csvName = csvName;
        this.iterationIndex = iterationIndex;
        this.versionIndex = versionIndex;
        this.javaClassesList = javaClassesList;

    }

    private String enumToFilename() {

        return switch (csvName) {
            case TRAINING -> "_TR" + versionIndex;
            case TESTING -> "_TE" + versionIndex;
            default -> null;
        };

    }
    private String enumToWFDirectoryName() {

       return this.projName+"_WF_"+this.iterationIndex;

    }

    private  @NotNull File createANewFile(String projName, String endPath) throws IOException {

        String dirPath = "./retrieved_data/projectClasses/"+this.projName+ File.separator  + enumToWFDirectoryName()+ File.separator;

        String pathname = dirPath + projName + enumToFilename() + endPath;

        File dir = new File(dirPath);
        File file = new File(pathname);

        if(!dir.exists() && !file.mkdirs()) {
            throw new IOException(); //Exception: dir creation impossible
        }

        if(file.exists() && !file.delete()) {
            throw new IOException(); //Exception: file deletion impossible
        }

        return file;
    }

    /** write all the ClassInfo data on a CSV file*/
    public void writeOnCsv() throws IOException {

        File file = createANewFile(projName, ".csv");

        try(FileWriter fw = new FileWriter(file)) {

            fw.write("JAVA_CLASS," +
                    "VERSION," +
                    "LOC," +
                    "LOC_ADDED," +
                    "MAX_LOC_ADDED," +
                    "AVG_LOC_ADDED," +
                    "LOC_DELETED," +
                    "MAX_LOC_DELETED," +
                    "AVG_LOC_DELETED," +
                    "CHURN," +
                    "MAX_CHURN," +
                    "AVG_CHURN," +
                    "FIXED_DEFECTS," +
                    "NUMBER_OF_COMMITS," +
                    "NUMBER_OF_AUTHORS," +
                    "N_REVISION,"+
                    "IS_BUGGY\n");



            writeDataOnFile( fw, false);
        }
    }

    /**Write for all the ClassInfo elements the data into a ARFF file */
    public void writeOnArff() throws IOException {

        String fileNameStr = enumToFilename();

        writeOnCsv();
        File file = createANewFile(projName, ".arff");
        try (FileWriter wr = new FileWriter(file)) {

       /*     wr.write("@relation " + this.projName + fileNameStr + "\n");
            wr.write("@attribute SIZE numeric\n");
            wr.write("@attribute REVISION numeric\n");
            wr.write("@attribute FIXED_DEFECTS numeric\n");
            wr.write("@attribute N_AUTH numeric\n");
            wr.write("@attribute LOC_ADDED numeric\n");
            wr.write("@attribute MAX_LOC_ADDED numeric\n");
            wr.write("@attribute AVG_LOC_ADDED numeric\n");
            wr.write("@attribute CHURN numeric\n");
            wr.write("@attribute MAX_CHURN numeric\n");
            wr.write("@attribute AVG_CHURN numeric\n");
            wr.write("@attribute NUMBER_OF_COMMITS numeric\n");
            wr.write("@attribute IS_BUGGY {'true', 'false'}\n");
            wr.write("@data\n");*/

            wr.write("@relation " + this.projName + fileNameStr + "\n");
            wr.write("@attribute LOC numeric\n");
            wr.write("@attribute LOC_ADDED numeric\n");
            wr.write("@attribute MAX_LOC_ADDED numeric\n");
            wr.write("@attribute AVG_LOC_ADDED numeric\n");
            wr.write("@attribute LOC_DELETED numeric\n");
            wr.write("@attribute MAX_LOC_DELETED numeric\n");
            wr.write("@attribute AVG_LOC_DELETED numeric\n");
            wr.write("@attribute CHURN numeric\n");
            wr.write("@attribute MAX_CHURN numeric\n");
            wr.write("@attribute AVG_CHURN numeric\n");
            wr.write("@attribute FIXED_DEFECTS numeric\n");
            wr.write("@attribute NUMBER_OF_COMMITS numeric\n");
            wr.write("@attribute NUMBER_OF_AUTHORS numeric\n");
            wr.write("@attribute REVISION_NUMBER numeric\n");
            wr.write("@attribute IS_BUGGY {'true', 'false'}\n");
            wr.write("@data\n");

            writeDataOnFile(wr,true);

        }

    }

    private void writeDataOnFile(FileWriter fw, boolean isArff) throws IOException {

        for (ClassInfo javaClass : this.javaClassesList) {

            if (!isArff) {
                fw.write(javaClass.getName() + ","); //JAVA_CLASS
                fw.write(javaClass.getVersion().getVersionInt() + ","); //VERSION
            }
            fw.write(javaClass.getSize() + ","); //SIZE(LINES OF CODE)
            fw.write(javaClass.getLocAdded() + ","); //LOC_ADDED
            fw.write(javaClass.getMaxLocAdded() + ","); //MAX_LOC_ADDED
            fw.write(javaClass.getAvgLocAdded() + ","); //AVG_LOC_ADDED
            fw.write(javaClass.getLocDeleted() + ","); //LOC_DELETED
            fw.write(javaClass.getMaxLocDeleted() + ","); //MAX_LOC_DELETED
            fw.write(javaClass.getAvgLocDeleted() + ","); //AVG_LOC_DELETED
            fw.write(javaClass.getChurn() + ","); //CHURN
            fw.write(javaClass.getMaxChurn() + ","); //MAX_CHURN
            fw.write(javaClass.getAvgChurn() + ","); //AVG_CHURN
            fw.write(javaClass.getFixedDefects() + ","); //FIXED_DEFECTS
            fw.write(javaClass.getCommits().size() + ","); //NUMBER_OF_COMMITS
            fw.write(javaClass.getnAuth() + ","); //NUMBER_OF_AUTHORS
            fw.write(javaClass.getRevisionNumber()+","); //NUMBER_OF_REVISION
            fw.write(javaClass.isBuggy()); //IS_BUGGY

            fw.write("\n");



        }
    }
}