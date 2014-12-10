package DecisionSupport;

import org.rosuda.REngine.Rserve.RserveException;

/*
 * This class defines the SVM class for rConnector
 */
public class rSVM extends rMLModel{
	
	private String tag="SVM";
	
	public String train(fDocument fdoc) throws RserveException
	{	    
		
		c=getRC();		
		
		System.out.println("SVM training...");
		
	    // load library
	    c.voidEval("library(e1071)");
	    		
		// train the model
		// mtry is the number of features used for predictor
		c.voidEval("tune.out=tune(svm, High~.-Sales, data=xdf[train,], kernel=\"radial\", "
				+ "ranges=list(cost=c(0.1,1,10,100,1000),gamma=c(0.5,1,2,3,4)))");
		c.voidEval("tm.xdf=tune.out$best.model");
		c.voidEval("tm.class=predict(tm.xdf, xdf.test, type=\"class\")");
		c.voidEval("tm.prob=attributes(predict(tm.xdf, xdf.test, decision.values=TRUE))$decision.values");
		
		return tag;
		
/*		// evaluate performance
		// if we need calculate the ROC, we need the real decision.values which is a real number, previous
		// bag.pred is a factor which is labeled with two classes name 
		c.voidEval("c=table(bag.pred, High.test)");
		double acc=c.eval("(c[1]+c[4])/(c[1]+c[2]+c[3]+c[4])").asDouble();
		System.out.println("SVM accuracy: " + acc);
		
		c.voidEval("pred=prediction(bag.prob, High.test)");
		//c.voidEval("auc=performance(pred, measure=\"tpr\", x.measure=\"fpr\")");
		c.voidEval("auc=performance(pred, \"auc\")");
		double auc=c.eval("unlist(slot(auc, \"y.values\"))").asDouble();
		System.out.println("SVM AUC: " + auc);		
		
		c.voidEval("roc=performance(pred, measure=\"tpr\", x.measure=\"fpr\")");
		double[] roc_x = c.eval("unlist(slot(roc, \"x.values\"))").asDoubles();
		double[] roc_y = c.eval("unlist(slot(roc, \"y.values\"))").asDoubles();
		return new rResult(roc_x, roc_y, acc, auc);	*/	
	}
		
	
	
}
