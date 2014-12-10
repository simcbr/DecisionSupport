package DecisionSupport;

import org.rosuda.REngine.Rserve.RserveException;

public class rRandomForest extends rMLModel {
	
	private String tag="RandomForest";
	
	public String train(fDocument fdoc) throws RserveException
	{    	
		
		c=getRC();		
		
		System.out.println("RandomForest training...");		
		
	    // load library
	    c.voidEval("library(randomForest)");
		
		// train the model
		// mtry is the number of features used for predictor
		c.voidEval("tm.xdf=randomForest(High~.-Sales, data=xdf, subset=train, mtry=10, importance=TRUE)"); 
		c.voidEval("tm.class=predict(tm.xdf, xdf.test, type=\"response\")");
		c.voidEval("tm.prob=(predict(tm.xdf, xdf.test, type=\"prob\"))[,2]");    // every model has its relevant predict parameters
		
		return tag;
		
/*		// evaluate performance
		//c.voidEval("pred=prediction(tree.pred, High.test)");
		//c.voidEval("perf=performance(pred, measure=\"tpr\", x.measure=\"fpr\")");
		c.voidEval("c=table(bag.pred, High.test)");
		double acc=c.eval("(c[1]+c[4])/(c[1]+c[2]+c[3]+c[4])").asDouble();
		System.out.println("RandomForest accuracy: " + acc);
			
		c.voidEval("pred=prediction(bag.prob, High.test)");
		//c.voidEval("auc=performance(pred, measure=\"tpr\", x.measure=\"fpr\")");
		c.voidEval("auc=performance(pred, \"auc\")");
		double auc=c.eval("unlist(slot(auc, \"y.values\"))").asDouble();
		System.out.println("RandomForest AUC: " + auc);			
		
		c.voidEval("roc=performance(pred, measure=\"tpr\", x.measure=\"fpr\")");
		double[] roc_x = c.eval("unlist(slot(roc, \"x.values\"))").asDoubles();
		double[] roc_y = c.eval("unlist(slot(roc, \"y.values\"))").asDoubles();
		return new rResult(roc_x, roc_y, acc, auc);		*/
		
	}
	

}
