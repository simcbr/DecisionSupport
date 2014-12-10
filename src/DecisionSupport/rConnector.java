package DecisionSupport;

import java.util.ArrayList;
import java.util.Arrays;

import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.RList;
import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RserveException;

import DecisionSupport.perfPanel.HIST_OPTION;
import DecisionSupport.perfPanel.SUMMARY_MODE;



public class rConnector {

	public enum DataModel
	{
	    SVM_T(0), LOGISTIC_T(1), LDA_T(2), QDA_T(3), DTREE_T(4), RFOREST_T(5), KNN_T(6);
	    private final int value;
	    private String[] DataModelStr=
	    	{
	    		"SVM", "LogisticRegression", "LinearDiscriminateAnalysis", "QuadraticDiscriminateAnalysis", 
	    		"DecisionTree", "RandomForest", "K-NearestNeighbor"
	    	};
	    private DataModel(int value) {
	        this.value = value;
	    }

	    public int getValue() {
	        return value;
	    }
	    
	    public String getStr()
	    {
	    	return DataModelStr[value];
	    }
	}
	
	
	
	public static int DateModelSize = DataModel.values().length; 
	
	private RConnection c=null;
	private rSVM svm=null;
	private rDecisionTree tree=null;
	private rRandomForest forest=null;
	private rLogistic logistic = null;
	private rLDA lda=null;
	private rQDA qda=null;
	private rKNN knn=null;
	private rSummary summary=null;
	private rHist hist=null;
	
	private static rConnector connector= null;
	
	private static String HOSTNAME="172.19.52.130"; //"172.19.100.245";   
	// Rserve(args="--RS-conf Rconfig.txt")  to enable remote connection
	
	
	rConnector()
	{
		try {
			c = new RConnection(HOSTNAME, 6311);
		} catch (RserveException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		svm = new rSVM();
		tree = new rDecisionTree();
		forest = new rRandomForest();
		logistic = new rLogistic();
		lda = new rLDA();
		qda = new rQDA();
		knn = new rKNN();
		summary = new rSummary();
		hist = new rHist();
	}
	
	
	public rResult trainModel(DataModel option, fDocument fdoc) throws RserveException, REXPMismatchException
	{
		rResult ret=null;
		
		switch (option)
		{
			case SVM_T:
				ret=svm.trainModel(fdoc);
				break;
				
			case DTREE_T:
				ret=tree.trainModel(fdoc);
				break;
				
			case RFOREST_T:
				ret=forest.trainModel(fdoc);
				break;
				
			case LOGISTIC_T:
				ret=logistic.trainModel(fdoc);
				break;
				
			case LDA_T:
				ret=lda.trainModel(fdoc);
				break;
				
			case QDA_T:
				ret=qda.trainModel(fdoc);
				break;
				
			case KNN_T:
				ret=knn.trainModel(fdoc);
				break;
				
			default:
				break;
		}
		
		return ret;
	}
	
	
	public rResult statSummary(ArrayList<String> choices, fDocument fdoc, fFilter filter, SUMMARY_MODE mode) throws RserveException, REXPMismatchException
	{
		return summary.statSummary(choices, fdoc, filter, mode);
	}
	
	public rResult statHist(ArrayList<String> choices, fDocument fdoc, fFilter filter, HIST_OPTION option) throws RserveException, REXPMismatchException
	{
		return hist.statHist(choices, fdoc, filter, option);
	}	
	
	public RConnection getRC() throws RserveException
	{
		if (c==null)
		{
			c = new RConnection(HOSTNAME, 6311);
		}
		return c;
	}
	
	
	public static rConnector connector()
	{
		if (connector==null)
		{
			connector = new rConnector();
		}
		return connector;
	}
	
	
/*	public static void main(String[] args) throws RserveException, REXPMismatchException {
		// TODO Auto-generated method stub
		rConnector rc = rConnector.connector();
		RConnection c = rc.getRC();
//		REXP x = c.eval("R.version.string");
//		System.out.println(x.asString());
//		double[] d= c.eval("rnorm(100)").asDoubles();
//		
//		c.assign("x", dataX);
//		c.assign("y", dataY);
//		RList l = c.eval("lowess(x,y)").asList();
//		double[] lx = l.at("x").asDoubles();
//		double[] ly = l.at("y").asDouble();		
		
		c.voidEval("library(MASS)");
		c.voidEval("library(ISLR)");
		c.voidEval("attach(Boston)");
		int[] sizes = c.eval("dim(Boston)").asIntegers();
		double[][] data = new double[sizes[0]][sizes[1]];
		RList l;
		for (int i=0; i<sizes[0]; i++)
		{
			String evalString = "Boston[" + Integer.toString(i+1) + ",]";
			//data[i] = c.eval(evalString).asDoubles();
			 l = c.eval(evalString).asList();
			 for (int j=0; j<sizes[1]; j++)
			 {
				 data[i][j] = l.at(j).asDouble();
			 }
			 
		}
		 
		
		
		REXP x = c.eval("lm.fit =lm(medv~lstat)");
		System.out.println(x.asString());
	}*/

	
	
	public String[] frameNames(String libName) throws RserveException, REXPMismatchException
	{
		RConnection c = this.getRC();
		
		c.voidEval("library(MASS)");
		c.voidEval("library(ISLR)");
		c.voidEval("attach(" + libName + ")");
		String[] keys = c.eval("names(" + libName + ")").asStrings();
		
		return keys;
	}
	
	
	
	public Double[][] frameData(String libName) throws RserveException, REXPMismatchException
	{
		RConnection c = this.getRC();
		
		c.voidEval("library(MASS)");
		c.voidEval("library(ISLR)");
		c.voidEval("attach(" + libName + ")");
		int[] sizes = c.eval("dim(" + libName + ")").asIntegers();		
		Double[][] data = new Double[sizes[0]][sizes[1]];
		RList l;
		for (int i=0; i<sizes[0]; i++)
		{
			 String evalString = libName + "[" + Integer.toString(i+1) + ",]";
			//data[i] = c.eval(evalString).asDoubles();
			 l = c.eval(evalString).asList();
			 for (int j=0; j<sizes[1]; j++)
			 {
				 data[i][j] = l.at(j).asDouble();
			 }
		}
		
		return data;		
	}
	
	
	public fDocument frameDocument(String libName)
	{
		fDocument fdoc = new fDocument();
		
		try {
			fdoc.setData(frameData(libName));
			fdoc.setNames(new ArrayList<String>(Arrays.asList(frameNames(libName))));
		} catch (RserveException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (REXPMismatchException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return fdoc;
	}
	
	
}
