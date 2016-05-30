package com.sogou.pa.meachineLearing;
 
import java.io.File;
import java.io.IOException;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.Logistic;
import weka.classifiers.trees.J48;
import weka.core.Instances;
import weka.core.converters.ArffLoader;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;


public class Logistic_test {

		/**
		 * @param args
		 * @throws Exception 
		 */
		public static void main(String[] args) throws Exception {
			// TODO Auto-generated method stub
			
			ArffLoader atf = new ArffLoader(); //Reads a source that is in arff (attribute relation file format) format.

	        File inputFile = new File("D:\\JavaWorkplace\\onlineHadoop\\src\\meachineLearing\\diagnosis_part1.arff");//读入训练文件
	        atf.setFile(inputFile);
	        Instances instancesTrain = atf.getDataSet(); // 得到格式化的训练数据
	        
	        //这个文件最后两个属性都是用来分类的 所以我们一个一个来做，先过滤掉一个
	        String[] removeOptions = new String[2];  
	        removeOptions[0] = "-R";                            // "range"  
	        removeOptions[1] = "8";                              // 8th attribute去掉第8 个  
	        Remove remove1 = new Remove();                // new instance of filter
	        remove1.setOptions(removeOptions);                  // set options  
	        remove1.setInputFormat(instancesTrain);   
	        Instances newInstancesTrain1 = Filter.useFilter(instancesTrain, remove1);  // 得到新的数据

	        newInstancesTrain1.setClassIndex(newInstancesTrain1.numAttributes()-1);//设置类别位置
	        
	        inputFile = new File("D:\\JavaWorkplace\\onlineHadoop\\src\\meachineLearing\\diagnosis_part2.arff");//读入测试文件
	        atf.setFile(inputFile);          
	        Instances instancesTest = atf.getDataSet(); // 得到格式化的测试数据
	        remove1.setInputFormat(instancesTest);   
	        Instances newInstancesTest1=Filter.useFilter(instancesTest, remove1);//得到新的测试数据
	        //设置分类属性所在行号（第一行为0号），instancesTest.numAttributes()可以取得属性总数
	        newInstancesTest1.setClassIndex(newInstancesTest1.numAttributes() - 1); 
	        Logistic m_classifier=new Logistic();//Logistic用以建立一个逻辑回归分类器
	        String options[]=new String[4];//训练参数数组
	        options[0]="-R";//cost函数中的预设参数  影响cost函数中参数的模长的比重
	        options[1]="1E-5";//设为1E-5
	        options[2]="-M";//最大迭代次数
	        options[3]="10";//最多迭代计算10次
	        m_classifier.setOptions(options);
	        m_classifier.buildClassifier(newInstancesTrain1); //训练          
	        Evaluation eval = new Evaluation(newInstancesTrain1); //构造评价器 
	        eval.evaluateModel(m_classifier, newInstancesTest1);//用测试数据集来评价m_classifier
	        System.out.println("Logistic Regression on Evaluating Inflammation of urinary bladder");
	        //System.out.println(eval.toSummaryString("=== Summary ===\n",false));  //输出信息
	        System.out.println(eval.toMatrixString("=== Confuddsion Matrix ===\n"));//Confusion Matrix 
	        
		}

	}
