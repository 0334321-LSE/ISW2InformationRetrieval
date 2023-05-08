package isw.project.file_model;

import java.io.*;
import java.util.List;

import isw.project.model.ClassifierEvaluation;
import org.jetbrains.annotations.NotNull;


public class EvaluationFile {

    private final String projName;
    private final List<ClassifierEvaluation> evaluationsList;
    private final String description;

    public EvaluationFile(String projName, List<ClassifierEvaluation> evaluationList, String description) {
        this.projName = projName;
        this.evaluationsList = evaluationList;
        this.description = description;

    }

    private  @NotNull File createANewFile() throws IOException {

        String dirPath = "./retrieved_data/projectEvaluation/"+this.projName+ File.separator;

        String pathname = dirPath+ this.projName+"Evaluation.csv";
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

    public void reportEvaluationOnCsv() throws  IOException{
        File file = createANewFile();
        try(FileWriter fw = new FileWriter(file)) {
            fw.write( "DATASET,"+
                    "TRAIN_RELEASES," +
                    "%TRAIN_INSTANCES," +
                    "CLASSIFIER," +
                    "FEATURE_SELECTION," +
                    "BALANCING," +
                    "COST_SENSITIVE," +
                    "PRECISION," +
                    "RECALL," +
                    "AUC," +
                    "KAPPA," +
                    "TP," +
                    "FP," +
                    "TN," +
                    "FN,\n");

            writeDataOnFile(fw);
        }

    }
    private void writeDataOnFile(FileWriter fw) throws IOException {

        for (ClassifierEvaluation evaluation : this.evaluationsList) {


            fw.write(this.projName + ","); //ProjName
            if(this.description.equals("details")) { //Details
                fw.write(evaluation.getWalkForwardIterationIndex()+ ",");
                fw.write((evaluation.getTrainingPercent())+ ",");
            }
            else {
                fw.write("None"+ ",");
                fw.write("None"+ ",");
            }
            fw.write(evaluation.getClassifier() + ","); //Classifiers
            if(evaluation.isFeatureSelection()) {
                fw.write("Best first"+ ",");
            }
            else {
                fw.write("None"+ ",");
            }
            fw.write(evaluation.getSampling()+ ","); //Sampling type


            fw.write(evaluation.getCostSensitive()+ ",");  //Cost sensitive


            fw.write(evaluation.getPrecision() + ","); //Precision
            fw.write(evaluation.getRecall() + ","); //Recall
            fw.write(evaluation.getAuc()+ ","); //Area under the curve
            fw.write(evaluation.getKappa() + ","); //Kappa
            fw.write(evaluation.getTp() + ","); //TP
            fw.write(evaluation.getFp() + ","); //FP
            fw.write(evaluation.getTn() + ","); //TN
            fw.write(evaluation.getFn() + ","); //FN
            fw.write("\n");



        }
    }

}

