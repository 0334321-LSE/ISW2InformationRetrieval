package isw2_data_retriever.retriever;

import isw2_data_retriever.control.ExecutionFlow;
import isw2_data_retriever.file_model.EvaluationFile;
import isw2_data_retriever.model.ClassifierEvaluation;
import weka.classifiers.lazy.IBk;
import weka.classifiers.trees.RandomForest;
import weka.core.Instances;


import weka.classifiers.Evaluation;
import weka.classifiers.meta.FilteredClassifier;
import weka.classifiers.bayes.NaiveBayes;
import weka.filters.supervised.instance.Resample;
import weka.core.converters.ConverterUtils.DataSource;
import weka.classifiers.evaluation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;


public class WekaRetriever {
    private static final Logger LOGGER = Logger.getLogger(WekaRetriever.class.getName());

    private static final String RANDOM_FOREST = "Random Forest";
    private static final String NAIVE_BAYES = "Naive Bayes";
    private static final String IBK = "IBk";

    private String projName;
    private int numIter;

    public WekaRetriever(String projName, int numIter){
        this.projName = projName;
        this.numIter = numIter;
    }
    public List<ClassifierEvaluation> testWekaApi() throws Exception {


        List<ClassifierEvaluation> simpleRandomForestList = new ArrayList<>();
        List<ClassifierEvaluation> simpleNaiveBayesList = new ArrayList<>();
        List<ClassifierEvaluation> simpleIBkList = new ArrayList<>();

        for(int i = 1; i<this.numIter; i++){
            DataSource sourceTest = new DataSource("C:/Users/39388/OneDrive/Desktop/ISW2/Projects/DataRetriever/" +
                    "retrieved_data/projectClasses/"+this.projName+"/"
                    +this.projName+"_WF_"+i+"/"+this.projName+"_TE"+(i+1)+".arff");
            DataSource sourceTra = new DataSource("C:/Users/39388/OneDrive/Desktop/ISW2/Projects/DataRetriever/" +
                    "retrieved_data/projectClasses/"+this.projName+"/"
                    +this.projName+"_WF_"+i+"/"+this.projName+"_TR"+i+".arff");

            Instances training = sourceTra.getDataSet();
            Instances testing = sourceTest.getDataSet();

            int numAttributes = training.numAttributes();
            training.setClassIndex(numAttributes - 1);
            testing.setClassIndex(numAttributes - 1);

            NaiveBayes naiveBayesClassifier = new NaiveBayes();
            naiveBayesClassifier.buildClassifier(training);

            RandomForest randomForestClassifier = new RandomForest();
            randomForestClassifier.buildClassifier(training);

            IBk ibkClassifier = new IBk();
            ibkClassifier.buildClassifier(training);

            Evaluation evaluation = new Evaluation(testing);

            //simple Naive Bayes
            evaluation.evaluateModel(naiveBayesClassifier, testing);
            ClassifierEvaluation simpleNaiveBayes = new ClassifierEvaluation(this.projName, i, NAIVE_BAYES, false, false, false);
            simpleNaiveBayes.setTrainingPercent(100.0*training.numInstances()/(training.numInstances()+testing.numInstances()));
            simpleNaiveBayes.setPrecision(evaluation.precision(0));
            simpleNaiveBayes.setRecall(evaluation.recall(0));
            simpleNaiveBayes.setAuc(evaluation.areaUnderROC(0));
            simpleNaiveBayes.setKappa(evaluation.kappa());
            simpleNaiveBayes.setTp(evaluation.numTruePositives(0));
            simpleNaiveBayes.setFp(evaluation.numFalsePositives(0));
            simpleNaiveBayes.setTn(evaluation.numTrueNegatives(0));
            simpleNaiveBayes.setFn(evaluation.numFalseNegatives(0));
            simpleNaiveBayesList.add(simpleNaiveBayes);
            LOGGER.info("\n------------------------------------"+
                    "\n Walk forward iteration: "+i+
                    "\nNaiveBayes: "+
                    "\nPrecision =" + evaluation.precision(0)+
                    "\nRecall =" + evaluation.recall(0)+
                    "\nAUC = " + evaluation.areaUnderROC(0)+
                    "\nkappa = " + evaluation.kappa());

            //simple RandomForest
            evaluation.evaluateModel(randomForestClassifier,testing);
            ClassifierEvaluation simpleRandomForest = new ClassifierEvaluation(this.projName, i, RANDOM_FOREST, false, false, false);
            simpleRandomForest.setTrainingPercent(100.0*training.numInstances()/(training.numInstances()+testing.numInstances()));
            simpleRandomForest.setPrecision(evaluation.precision(0));
            simpleRandomForest.setRecall(evaluation.recall(0));
            simpleRandomForest.setAuc(evaluation.areaUnderROC(0));
            simpleRandomForest.setKappa(evaluation.kappa());
            simpleRandomForest.setTp(evaluation.numTruePositives(0));
            simpleRandomForest.setFp(evaluation.numFalsePositives(0));
            simpleRandomForest.setTn(evaluation.numTrueNegatives(0));
            simpleRandomForest.setFn(evaluation.numFalseNegatives(0));
            simpleRandomForestList.add(simpleRandomForest);
            LOGGER.info(
                    "\nRandomForest: "+
                    "\nPrecision =" + evaluation.precision(0)+
                    "\nRecall =" + evaluation.recall(0)+
                    "\nAUC = " + evaluation.areaUnderROC(0)+
                    "\nkappa = " + evaluation.kappa());

            //simple IBK
            evaluation.evaluateModel(ibkClassifier,testing);
            ClassifierEvaluation simpleIBk = new ClassifierEvaluation(this.projName, i, IBK, false, false, false);
            simpleIBk.setTrainingPercent(100.0*training.numInstances()/(training.numInstances()+testing.numInstances()));
            simpleIBk.setPrecision(evaluation.precision(0));
            simpleIBk.setRecall(evaluation.recall(0));
            simpleIBk.setAuc(evaluation.areaUnderROC(0));
            simpleIBk.setKappa(evaluation.kappa());
            simpleIBk.setTp(evaluation.numTruePositives(0));
            simpleIBk.setFp(evaluation.numFalsePositives(0));
            simpleIBk.setTn(evaluation.numTrueNegatives(0));
            simpleIBk.setFn(evaluation.numFalseNegatives(0));
            simpleIBkList.add(simpleIBk);
            LOGGER.info(
                    "\nIBk: "+
                    "\nPrecision =" + evaluation.precision(0)+
                    "\nRecall =" + evaluation.recall(0)+
                    "\nAUC = " + evaluation.areaUnderROC(0)+
                    "\nkappa = " + evaluation.kappa());



        }
        List<ClassifierEvaluation> allEvaluationList = new ArrayList<>(simpleNaiveBayesList);
        allEvaluationList.addAll(simpleRandomForestList);
        allEvaluationList.addAll(simpleIBkList);
        return allEvaluationList;
    }

}
