package DecisionSupport;

import org.rosuda.REngine.Rserve.RserveException;

public class rLogistic extends rMLModel{
	
	private String tag="Logistic Regression";
	
	public String train(fDocument fdoc) throws RserveException
	{   
	
		c=getRC();		
		
		System.out.println("Logistic Regression training...");		
		
	    // load library
		c.voidEval("library(ROCR)");
		
		// train the model
		// mtry is the number of features used for predictor
		c.voidEval("tm.xdf=glm(High~.-Sales, data=xdf, family=binomial)"); 
		c.voidEval("tm.prob=predict(tm.xdf, xdf.test, type=\"response\")");  //lr.prob is the probability
		c.voidEval("tm.class=rep(\"No\",length(High.test))");                 // transfer it to class label
		c.voidEval("tm.class[tm.prob>0.5]=\"Y\"");
		
		return tag;
/*		// evaluate performance
		c.voidEval("c=table(lr.pred, High.test)");
		double acc=c.eval("(c[1]+c[4])/(c[1]+c[2]+c[3]+c[4])").asDouble();
		System.out.println("Logistic Regression accuracy: " + acc);
		
		c.voidEval("pred=prediction(lr.prob, High.test)");
		c.voidEval("auc=performance(pred, \"auc\")");
		double auc=c.eval("unlist(slot(auc, \"y.values\"))").asDouble();
		System.out.println("Logistic Regression AUC: " + auc);
		
		c.voidEval("roc=performance(pred, measure=\"tpr\", x.measure=\"fpr\")");
		double[] roc_x = c.eval("unlist(slot(roc, \"x.values\"))").asDoubles();
		double[] roc_y = c.eval("unlist(slot(roc, \"y.values\"))").asDoubles();
		return new rResult(roc_x, roc_y, acc, auc);*/
			
	}	

}
