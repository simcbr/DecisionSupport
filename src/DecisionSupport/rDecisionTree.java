package DecisionSupport;

import org.rosuda.REngine.Rserve.RserveException;

/*
 * This class defines the decision tree class for rConnector
 */
public class rDecisionTree extends rMLModel{
	
	private String tag="DecisionTree";
	
	public String train(fDocument fdoc) throws RserveException
	{			
		c=getRC();
		
		System.out.println("DecisionTree training...");
		
	    // load library
	    c.voidEval("library(tree)");	    
		
		// train the model
		c.voidEval("tree.xdf=tree(High~.-Sales, xdf, subset=train)");
		c.voidEval("cv.xdf=cv.tree(tree.xdf,FUN=prune.misclass)");
		c.voidEval("bestSize=cv.xdf$size[which.min(cv.xdf$dev)]");
		c.voidEval("prune.xdf=prune.misclass(tree.xdf, best=bestSize)");
		c.voidEval("tm.class=predict(prune.xdf, xdf.test, type=\"class\")");
		c.voidEval("tm.prob=(predict(prune.xdf, xdf.test, decision.values=TRUE))[,2]"); // get the probability to be Yes
		
		return tag;
		
/*		// evaluate performance
		//c.voidEval("pred=prediction(tree.pred, High.test)");
		//c.voidEval("perf=performance(pred, measure=\"tpr\", x.measure=\"fpr\")");
		c.voidEval("c=table(tree.pred, High.test)");
		int bestSize = c.eval("bestSize").asInteger();
		double acc=c.eval("(c[1]+c[4])/(c[1]+c[2]+c[3]+c[4])").asDouble();
		//System.out.println("DecisionTree best tree size: " + bestSize);
		System.out.println("DecisionTree accuracy: " + acc);
		
		
		c.voidEval("pred=prediction(tree.prob, High.test)");
		//c.voidEval("auc=performance(pred, measure=\"tpr\", x.measure=\"fpr\")");
		c.voidEval("auc=performance(pred, \"auc\")");
		double auc=c.eval("unlist(slot(auc, \"y.values\"))").asDouble();
		System.out.println("DecisionTree AUC: " + auc);		
		
		c.voidEval("roc=performance(pred, measure=\"tpr\", x.measure=\"fpr\")");
		double[] roc_x = c.eval("unlist(slot(roc, \"x.values\"))").asDoubles();
		double[] roc_y = c.eval("unlist(slot(roc, \"y.values\"))").asDoubles();
		return new rResult(roc_x, roc_y, acc, auc);	*/	
	}
	
	
}
