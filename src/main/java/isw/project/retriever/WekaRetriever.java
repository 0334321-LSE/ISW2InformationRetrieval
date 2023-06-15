package isw.project.retriever;

import isw.project.model.ClassifierEvaluation;
import weka.attributeSelection.BestFirst;
import weka.attributeSelection.CfsSubsetEval;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.CostMatrix;
import weka.classifiers.lazy.IBk;
import weka.classifiers.meta.CostSensitiveClassifier;
import weka.classifiers.meta.FilteredClassifier;
import weka.classifiers.trees.RandomForest;
import weka.core.Instance;
import weka.core.Instances;


import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.Filter;
import weka.filters.supervised.attribute.AttributeSelection;
import weka.filters.supervised.instance.Resample;
import weka.filters.supervised.instance.SpreadSubsample;
import weka.filters.supervised.instance.SMOTE;

import java.util.ArrayList;

import java.util.List;


public class WekaRetriever {

    private static final String RANDOM_FOREST = "Random Forest";
    private static final String NAIVE_BAYES = "Naive Bayes";
    private static final String IBK = "IBk";

    //Attribute selection type
    private static final String FORWARD = "(forward)";
    private static final String BACKWARD ="(backward)";

    //Sampling type
    private static final String NO = "None";
    private static final String UNDER ="Under";
    private static final String OVER ="Over";
    private static final String SMOTE ="Smote";

    //Error type cost
    private static final double FP_WEIGHT = 1.0;
    private static final double FN_WEIGHT = 10.0;

    //Sensitive type
    private static final String THRESHOLD ="Threshold";
    private static final String LEARNING ="Learning";


    //Used classifiers
    private NaiveBayes naiveBayesClassifier;
    private RandomForest randomForestClassifier;
    private IBk ibkClassifier;

    //List of various classifier evaluation
    private final List<ClassifierEvaluation> simpleNaiveBayesList;
    private final List<ClassifierEvaluation> simpleRandomForestList;
    private final List<ClassifierEvaluation> simpleIBkList;

    private final List<ClassifierEvaluation> featureNaiveBayesList;
    private final List<ClassifierEvaluation> featureRandomForestList;
    private final List<ClassifierEvaluation> featureIBkList;

    private final List<ClassifierEvaluation> undersamplingNaiveBayesList;
    private final List<ClassifierEvaluation> undersamplingRandomForestList;
    private final List<ClassifierEvaluation> undersamplingIBkList;

    private final List<ClassifierEvaluation> oversamplingNaiveBayesList;
    private final List<ClassifierEvaluation> oversamplingRandomForestList;
    private final List<ClassifierEvaluation> oversamplingIBkList;

    private final List<ClassifierEvaluation> smoteNaiveBayesList;
    private final List<ClassifierEvaluation> smoteRandomForestList;
    private final List<ClassifierEvaluation> smoteIBkList;

    private final List<ClassifierEvaluation> sensitiveThresholdNaiveBayesList;
    private final List<ClassifierEvaluation> sensitiveThresholdRandomForestList;
    private final List<ClassifierEvaluation> sensitiveThresholdIBkList;


    private final List<ClassifierEvaluation> sensitiveLearningNaiveBayesList;
    private final List<ClassifierEvaluation> sensitiveLearningRandomForestList;
    private final List<ClassifierEvaluation> sensitiveLearningIBkList;

    private final String projName;
    private final int numIter;

    public WekaRetriever(String projName, int numIter){
        this.projName = projName;
        this.numIter = numIter;
        naiveBayesClassifier = new NaiveBayes();
        randomForestClassifier = new RandomForest();
        ibkClassifier = new IBk();

        simpleRandomForestList = new ArrayList<>();
        simpleNaiveBayesList = new ArrayList<>();
        simpleIBkList = new ArrayList<>();

        featureNaiveBayesList = new ArrayList<>();
        featureRandomForestList = new ArrayList<>();
        featureIBkList = new ArrayList<>();

        undersamplingNaiveBayesList = new ArrayList<>();
        undersamplingRandomForestList = new ArrayList<>();
        undersamplingIBkList = new ArrayList<>();

        oversamplingNaiveBayesList = new ArrayList<>();
        oversamplingRandomForestList = new ArrayList<>();
        oversamplingIBkList = new ArrayList<>();


        smoteNaiveBayesList = new ArrayList<>();
        smoteRandomForestList = new ArrayList<>();
        smoteIBkList = new ArrayList<>();

        sensitiveThresholdNaiveBayesList = new ArrayList<>();
        sensitiveThresholdRandomForestList = new ArrayList<>();
        sensitiveThresholdIBkList = new ArrayList<>();

        sensitiveLearningNaiveBayesList = new ArrayList<>();
        sensitiveLearningRandomForestList = new ArrayList<>();
        sensitiveLearningIBkList = new ArrayList<>();


    }
    public List<ClassifierEvaluation> walkForwardValidation() throws Exception {

         for(int i = 1; i<this.numIter; i++){
            DataSource sourceTest = new DataSource("C:/Users/39388/OneDrive/Desktop/ISW2/Projects/DataRetriever/" +
                    "retrieved_data/projectClasses/"+this.projName+"/"
                    +this.projName+"_WF_"+i+"/"+this.projName+"_TE"+(i+1)+".arff");
            DataSource sourceTra = new DataSource("C:/Users/39388/OneDrive/Desktop/ISW2/Projects/DataRetriever/" +
                    "retrieved_data/projectClasses/"+this.projName+"/"
                    +this.projName+"_WF_"+i+"/"+this.projName+"_TR"+i+".arff");

            //VALIDATION WITHOUT FEATURE SELECTION AND WITHOUT SAMPLING
            Instances training = sourceTra.getDataSet();
            Instances testing = sourceTest.getDataSet();
            int numAttributes = training.numAttributes();
            training.setClassIndex(numAttributes - 1);
            testing.setClassIndex(numAttributes - 1);

            simpleValidation(i,training,testing,NO);
            
            //VALIDATION WITH FEATURE SELECTION (BEST FIRST, forward) AND WITHOUT SAMPLING
            //Evaluates the worth of an attribute by measuring the correlation between it and the class
            CfsSubsetEval subsetEval = new CfsSubsetEval();
            BestFirst search = new BestFirst();
            search.setOptions(new String[]{"-D","1"});


            AttributeSelection filter = new AttributeSelection();
            filter.setEvaluator(subsetEval);
            filter.setSearch(search);
            filter.setInputFormat(training);

            Instances filteredTrainingF= Filter.useFilter(training, filter);
            Instances filteredTestingF = Filter.useFilter(testing, filter);

            int numAttrFiltered = filteredTrainingF.numAttributes();
            filteredTrainingF.setClassIndex(numAttrFiltered - 1);

            //VALIDATION WITH FEATURE SELECTION (BEST FIRST-> FORWARD)
            bestFirstFeatureSelection(i,filteredTrainingF,filteredTestingF,FORWARD);

            search.setOptions(new String[]{"-D","0"});
            filter.setSearch(search);
            filter.setInputFormat(training);

            Instances filteredTrainingB = Filter.useFilter(training, filter);
            Instances filteredTestingB = Filter.useFilter(testing, filter);

            numAttrFiltered = filteredTrainingB.numAttributes();
            filteredTrainingB.setClassIndex(numAttrFiltered - 1);

            //VALIDATION WITH FEATURE SELECTION (BEST FIRST-> BACKWARD)
            bestFirstFeatureSelection(i,filteredTrainingB,filteredTestingB,BACKWARD);

            //VALIDATION WITH FEATURE SELECTION (BEST FIRST-> FORWARD) AND WITH SAMPLING (UNDER SAMPLING)
            undersamplingWithFeatureSelection(i,filteredTrainingF,filteredTestingF, FORWARD);

            //VALIDATION WITH FEATURE SELECTION (BEST FIRST-> BACKWARD) AND WITH SAMPLING (UNDER SAMPLING)
            undersamplingWithFeatureSelection(i,filteredTrainingB,filteredTestingB, BACKWARD);

            //VALIDATION WITH FEATURE SELECTION (BEST FIRST-> FORWARD) AND WITH SAMPLING (OVERSAMPLING)
            oversamplingWithFeatureSelection(i,filteredTrainingF,filteredTestingF,FORWARD);
            //VALIDATION WITH FEATURE SELECTION (BEST FIRST-> BACKWARD) AND WITH SAMPLING (OVERSAMPLING)
            oversamplingWithFeatureSelection(i,filteredTrainingB,filteredTestingB,BACKWARD);

            //VALIDATION WITH FEATURE SELECTION (BEST FIRST-> FORWARD) AND WITH SAMPLING (SMOTE)
            smoteWithFeatureSelection(i,filteredTrainingF,filteredTestingF,FORWARD);
            //VALIDATION WITH FEATURE SELECTION (BEST FIRST-> BACKWARD) AND WITH SAMPLING (SMOTE)
            smoteWithFeatureSelection(i,filteredTrainingB,filteredTestingB,BACKWARD);

            //VALIDATION WITH FEATURE SELECTION (BEST FIRST-> FORWARD) AND WITH COST SENSITIVE (SENSITIVE THRESHOLD)
            sensitiveLearningWithFeatureSelection(i ,filteredTrainingF, filteredTestingF, FORWARD);

            //VALIDATION WITH FEATURE SELECTION (BEST FIRST-> BACKWARD) AND WITH COST SENSITIVE (SENSITIVE LEARNING)
            sensitiveLearningWithFeatureSelection(i ,filteredTrainingB, filteredTestingB, BACKWARD);
        }

        List<ClassifierEvaluation> allEvaluationList = new ArrayList<>();
        mergeAllListOrdered(allEvaluationList);

        return allEvaluationList;
    }

    /** Merge all the evaluation list into one */
    private void mergeAllListOrdered(List<ClassifierEvaluation> allEvaluationList){
        int n=0;
        for( int i = 0; i<simpleNaiveBayesList.size(); i++){
            allEvaluationList.add(simpleNaiveBayesList.get(i));

            allEvaluationList.add(featureNaiveBayesList.get(n));
            allEvaluationList.add(featureNaiveBayesList.get(n+1));

            allEvaluationList.add(undersamplingNaiveBayesList.get(n));
            allEvaluationList.add(undersamplingNaiveBayesList.get(n+1));

            allEvaluationList.add(oversamplingNaiveBayesList.get(n));
            allEvaluationList.add(oversamplingNaiveBayesList.get(n+1));

            allEvaluationList.add(smoteNaiveBayesList.get(n));
            allEvaluationList.add(smoteNaiveBayesList.get(n+1));

            allEvaluationList.add(sensitiveLearningNaiveBayesList.get(n));
            allEvaluationList.add(sensitiveLearningNaiveBayesList.get(n+1));
            n+=2;
        }

        n=0;
        for( int j = 0; j<simpleRandomForestList.size(); j++){
            allEvaluationList.add(simpleRandomForestList.get(j));

            allEvaluationList.add(featureRandomForestList.get(n));
            allEvaluationList.add(featureRandomForestList.get(n+1));

            allEvaluationList.add(undersamplingRandomForestList.get(n));
            allEvaluationList.add(undersamplingRandomForestList.get(n+1));

            allEvaluationList.add(oversamplingRandomForestList.get(n));
            allEvaluationList.add(oversamplingRandomForestList.get(n+1));

            allEvaluationList.add(smoteRandomForestList.get(n));
            allEvaluationList.add(smoteRandomForestList.get(n+1));

            allEvaluationList.add(sensitiveLearningRandomForestList.get(n));
            allEvaluationList.add(sensitiveLearningRandomForestList.get(n+1));
            n+=2;
        }
        n=0;
        for( int k = 0; k<simpleRandomForestList.size(); k++){
            allEvaluationList.add(simpleIBkList.get(k));

            allEvaluationList.add(featureIBkList.get(n));
            allEvaluationList.add(featureIBkList.get(n+1));

            allEvaluationList.add(undersamplingIBkList.get(n));
            allEvaluationList.add(undersamplingIBkList.get(n+1));

            allEvaluationList.add(oversamplingIBkList.get(n));
            allEvaluationList.add(oversamplingIBkList.get(n+1));

            allEvaluationList.add(smoteIBkList.get(n));
            allEvaluationList.add(smoteIBkList.get(n+1));
            //
            allEvaluationList.add(sensitiveLearningIBkList.get(n));
            allEvaluationList.add(sensitiveLearningIBkList.get(n+1));
            n+=2;
        }
    }

    private void resetClassifiers(Instances filteredTraining) throws Exception {
        naiveBayesClassifier = new NaiveBayes();
        naiveBayesClassifier.buildClassifier(filteredTraining);
        randomForestClassifier = new RandomForest();
        randomForestClassifier.buildClassifier(filteredTraining);
        ibkClassifier = new IBk();
        ibkClassifier.buildClassifier(filteredTraining);
    }
    /** Does the simple evaluation without any feature selection/sampling/cost sensitive */
    private void simpleValidation(int i, Instances training, Instances testing, String direction) throws Exception {

        //Build the classifiers
        naiveBayesClassifier.buildClassifier(training);
        randomForestClassifier.buildClassifier(training);
        ibkClassifier.buildClassifier(training);

        Evaluation evaluation = new Evaluation(testing);
        //simple Naive Bayes
        ClassifierEvaluation simpleNaiveBayes = new ClassifierEvaluation(this.projName, i, NAIVE_BAYES, direction, NO, NO);
        simpleNaiveBayesList.add(evaluateClassifier(evaluation,simpleNaiveBayes,naiveBayesClassifier,training,testing));

        evaluation = new Evaluation(testing);
        //simple RandomForest
        ClassifierEvaluation simpleRandomForest = new ClassifierEvaluation(this.projName, i, RANDOM_FOREST, direction, NO, NO);
        simpleRandomForestList.add(evaluateClassifier(evaluation,simpleRandomForest,randomForestClassifier,training,testing));

        evaluation = new Evaluation(testing);
        //simple IBK
        ClassifierEvaluation simpleIBk = new ClassifierEvaluation(this.projName, i, IBK, direction, NO, NO);
        simpleIBkList.add(evaluateClassifier(evaluation,simpleIBk,ibkClassifier,training,testing));

    }

    /** Does validation with Best first feature selection */
    private void bestFirstFeatureSelection(int i, Instances filteredTraining, Instances filteredTesting,String direction) throws Exception {

        resetClassifiers(filteredTraining);

        Evaluation evaluation = new Evaluation(filteredTesting);
        //Naive Bayes with feature selection
        ClassifierEvaluation featureNaiveBayes = new ClassifierEvaluation(this.projName, i, NAIVE_BAYES, direction, NO, NO);
        featureNaiveBayesList.add(evaluateClassifier(evaluation,featureNaiveBayes,naiveBayesClassifier,filteredTraining,filteredTesting));

        evaluation = new Evaluation(filteredTesting);
        // RandomForest with feature selection
        ClassifierEvaluation featureRandomForest = new ClassifierEvaluation(this.projName, i, RANDOM_FOREST, direction, NO, NO);
        featureRandomForestList.add(evaluateClassifier(evaluation,featureRandomForest,randomForestClassifier,filteredTraining,filteredTesting));

        evaluation = new Evaluation(filteredTesting);
        //IBK with feature selection
        ClassifierEvaluation featureIBk = new ClassifierEvaluation(this.projName, i, IBK, direction, NO, NO);
        featureIBkList.add(evaluateClassifier(evaluation,featureIBk,ibkClassifier,filteredTraining,filteredTesting));

    }

    /** Does validation with Best first feature selection and undersampling */
    private void undersamplingWithFeatureSelection(int i, Instances filteredTraining, Instances filteredTesting, String direction) throws Exception {

        resetClassifiers(filteredTraining);

        SpreadSubsample spreadSubsample = new SpreadSubsample();
        spreadSubsample.setInputFormat(filteredTraining);
        spreadSubsample.setOptions(new String[] {"-M", "1.0"});
        FilteredClassifier fc = new FilteredClassifier();
        fc.setFilter(spreadSubsample);

        Evaluation evaluation = new Evaluation(filteredTesting);
        //Naive Bayes with feature selection and under sampling
        fc.setClassifier(naiveBayesClassifier);
        fc.buildClassifier(filteredTraining);
        ClassifierEvaluation undersamplingNaiveBayes = new ClassifierEvaluation(this.projName, i, NAIVE_BAYES, direction, UNDER, NO);
        undersamplingNaiveBayesList.add(evaluateClassifier(evaluation,undersamplingNaiveBayes,fc,filteredTraining,filteredTesting));

        fc = new FilteredClassifier();
        fc.setFilter(spreadSubsample);
        evaluation = new Evaluation(filteredTesting);
        // RandomForest with feature selection and under sampling
        fc.setClassifier(randomForestClassifier);
        fc.buildClassifier(filteredTraining);
        ClassifierEvaluation undersamplingRandomForest = new ClassifierEvaluation(this.projName, i, RANDOM_FOREST, direction, UNDER, NO);
        undersamplingRandomForestList.add(evaluateClassifier(evaluation,undersamplingRandomForest,fc,filteredTraining,filteredTesting));

        fc = new FilteredClassifier();
        fc.setFilter(spreadSubsample);
        evaluation = new Evaluation(filteredTesting);
        //IBK with feature selection and under sampling
        fc.setClassifier(ibkClassifier);
        fc.buildClassifier(filteredTraining);
        ClassifierEvaluation undersamplingIBk = new ClassifierEvaluation(this.projName, i, IBK, direction, UNDER, NO);
        undersamplingIBkList.add(evaluateClassifier(evaluation,undersamplingIBk,fc,filteredTraining,filteredTesting));

    }

    /** Does validation with Best first feature selection and oversampling */
    private void oversamplingWithFeatureSelection(int i, Instances filteredTraining, Instances filteredTesting, String direction) throws Exception {

        resetClassifiers(filteredTraining);
        //sampleSizePercent is equal to Y where Y/2 is equal to the percentage of the majority instance
        int notBuggy = getIsntBuggyIstanceNumber(filteredTraining);
        double percentage = ((double) notBuggy /filteredTraining.size())*100;


        Resample resample = new Resample();
        resample.setInputFormat(filteredTraining);
        resample.setOptions(new String[] {"-B", "1.0","-Z", Double.toString(2*percentage)});
        FilteredClassifier fc = new FilteredClassifier();
        fc.setFilter(resample);

        Evaluation evaluation = new Evaluation(filteredTesting);
        //Naive Bayes with feature selection and oversampling
        fc.setClassifier(naiveBayesClassifier);
        fc.buildClassifier(filteredTraining);
        ClassifierEvaluation oversamplingNaiveBayes = new ClassifierEvaluation(this.projName, i, NAIVE_BAYES, direction, OVER, NO);
        oversamplingNaiveBayesList.add(evaluateClassifier(evaluation,oversamplingNaiveBayes,fc,filteredTraining,filteredTesting));

        fc = new FilteredClassifier();
        fc.setFilter(resample);
        evaluation = new Evaluation(filteredTesting);
        // RandomForest with feature selection and oversampling
        fc.setClassifier(randomForestClassifier);
        fc.buildClassifier(filteredTraining);
        ClassifierEvaluation oversamplingRandomForest = new ClassifierEvaluation(this.projName, i, RANDOM_FOREST, direction, OVER, NO);
        oversamplingRandomForestList.add(evaluateClassifier(evaluation,oversamplingRandomForest,fc,filteredTraining,filteredTesting));

        fc = new FilteredClassifier();
        fc.setFilter(resample);
        evaluation = new Evaluation(filteredTesting);
        //IBK with feature selection and oversampling
        fc.setClassifier(ibkClassifier);
        fc.buildClassifier(filteredTraining);
        ClassifierEvaluation oversamplingIBk = new ClassifierEvaluation(this.projName, i, IBK, direction, OVER, NO);
        oversamplingIBkList.add(evaluateClassifier(evaluation,oversamplingIBk,fc,filteredTraining,filteredTesting));

    }

    /** Does validation with Best first feature selection and smote */
    private void smoteWithFeatureSelection(int i, Instances filteredTraining, Instances filteredTesting, String direction) throws Exception {

        resetClassifiers(filteredTraining);
        int notBuggy = getIsntBuggyIstanceNumber(filteredTraining);
        int buggy = filteredTraining.size()-notBuggy;
        Integer buggyInteger = buggy;
        final boolean condition = buggyInteger.equals(0);

        double percentage = ((double) (notBuggy-buggy)/buggy) * 100;
        //
        if (condition) percentage = 0;


        FilteredClassifier fc = new FilteredClassifier();

        SMOTE smote = new SMOTE();
        smote.setInputFormat(filteredTraining);
        smote.setClassValue("1");
        smote.setPercentage(percentage);

        fc.setFilter(smote);

        Evaluation evaluation = new Evaluation(filteredTesting);
        //Naive Bayes with feature selection and oversampling
        fc.setClassifier(naiveBayesClassifier);
        fc.buildClassifier(filteredTraining);
        ClassifierEvaluation smoteNaiveBayes = new ClassifierEvaluation(this.projName, i, NAIVE_BAYES, direction, SMOTE, NO);
        smoteNaiveBayesList.add(evaluateClassifier(evaluation,smoteNaiveBayes,fc,filteredTraining,filteredTesting));

        fc = new FilteredClassifier();
        fc.setFilter(smote);
        evaluation = new Evaluation(filteredTesting);
        // RandomForest with feature selection and oversampling
        fc.setClassifier(randomForestClassifier);
        fc.buildClassifier(filteredTraining);
        ClassifierEvaluation smoteRandomForest = new ClassifierEvaluation(this.projName, i, RANDOM_FOREST, direction, SMOTE, NO);
        smoteRandomForestList.add(evaluateClassifier(evaluation,smoteRandomForest,fc,filteredTraining,filteredTesting));

        fc = new FilteredClassifier();
        fc.setFilter(smote);
        evaluation = new Evaluation(filteredTesting);
        //IBK with feature selection and oversampling
        fc.setClassifier(ibkClassifier);
        fc.buildClassifier(filteredTraining);
        ClassifierEvaluation smoteIBk = new ClassifierEvaluation(this.projName, i, IBK, direction, SMOTE, NO);
        smoteIBkList.add(evaluateClassifier(evaluation,smoteIBk,fc,filteredTraining,filteredTesting));

    }

    /** Does validation with Best first feature selection and cost sensitive (threshold sensitive)*/
    private void sensitiveLearningWithFeatureSelection(int i, Instances filteredTraining, Instances filteredTesting, String direction) throws Exception {

        resetClassifiers(filteredTraining);
        //Create the cost sensitive classifier and set costMatrix
        CostSensitiveClassifier csc = new CostSensitiveClassifier();
        csc.setCostMatrix(createCostMatrix());

        csc.setMinimizeExpectedCost(false);


        // SENSITIVE LEARNING
        Evaluation evaluation = new Evaluation(filteredTesting,csc.getCostMatrix());
        //Naive Bayes with feature selection and oversampling
        csc.setClassifier(naiveBayesClassifier);
        csc.buildClassifier(filteredTraining);
        ClassifierEvaluation costSensitiveNaiveBayes = new ClassifierEvaluation(this.projName, i, NAIVE_BAYES, direction, NO, LEARNING);
        sensitiveLearningNaiveBayesList.add(evaluateClassifier(evaluation,costSensitiveNaiveBayes,csc,filteredTraining,filteredTesting));

        csc = new CostSensitiveClassifier();
        csc.setCostMatrix(createCostMatrix());
        csc.setMinimizeExpectedCost(false);
        evaluation =  new Evaluation(filteredTesting,csc.getCostMatrix());
        // RandomForest with feature selection and oversampling
        csc.setClassifier(randomForestClassifier);
        csc.buildClassifier(filteredTraining);
        ClassifierEvaluation costSensitiveRandomForest = new ClassifierEvaluation(this.projName, i, RANDOM_FOREST, direction, NO, LEARNING);
        sensitiveLearningRandomForestList.add(evaluateClassifier(evaluation,costSensitiveRandomForest,csc,filteredTraining,filteredTesting));

        csc = new CostSensitiveClassifier();
        csc.setCostMatrix(createCostMatrix());
        csc.setMinimizeExpectedCost(false);
        evaluation = new Evaluation(filteredTesting,csc.getCostMatrix());
        //IBK with feature selection and oversampling
        csc.setClassifier(ibkClassifier);
        csc.buildClassifier(filteredTraining);
        ClassifierEvaluation costSensitiveIBk = new ClassifierEvaluation(this.projName, i, IBK, direction, NO, LEARNING);
        sensitiveLearningIBkList.add(evaluateClassifier(evaluation,costSensitiveIBk,csc,filteredTraining,filteredTesting));

    }

    private int getIsntBuggyIstanceNumber(Instances training){
        int notBuggyInstance = 0;
        for (Instance instance: training){
            if ( instance.toString(instance.numAttributes()-1).equals("false") ) notBuggyInstance++;
        }
        return notBuggyInstance;
    }

    private CostMatrix createCostMatrix() {
        CostMatrix costMatrix = new CostMatrix(2);
        costMatrix.setCell(0, 0, 0.0);
        costMatrix.setCell(1, 0, WekaRetriever.FP_WEIGHT);
        costMatrix.setCell(0, 1, WekaRetriever.FN_WEIGHT);
        costMatrix.setCell(1, 1, 0.0);
        return costMatrix;
    }

    private static ClassifierEvaluation evaluateClassifier(Evaluation evaluation, ClassifierEvaluation classifierEvaluation, AbstractClassifier classifierType,Instances training,Instances testing) throws Exception {
        evaluation.evaluateModel(classifierType,testing);
        classifierEvaluation.setTrainingPercent(100.0*training.numInstances()/(training.numInstances()+testing.numInstances()));
        classifierEvaluation.setPrecision(evaluation.precision(0));
        classifierEvaluation.setRecall(evaluation.recall(0));
        classifierEvaluation.setAuc(evaluation.areaUnderROC(0));
        classifierEvaluation.setKappa(evaluation.kappa());
        classifierEvaluation.setTp(evaluation.numTruePositives(0));
        classifierEvaluation.setFp(evaluation.numFalsePositives(0));
        classifierEvaluation.setTn(evaluation.numTrueNegatives(0));
        classifierEvaluation.setFn(evaluation.numFalseNegatives(0));
        return classifierEvaluation;
    }
}
