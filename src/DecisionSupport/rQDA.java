package DecisionSupport;

import org.rosuda.REngine.Rserve.RserveException;

public class rQDA extends rMLModel {
	
	private String tag="QDA";
	
	public String train(fDocument fdoc) throws RserveException
	{    
		c=getRC();		
		
		System.out.println("Quadratic Discriminant Analysis training...");		
		
	    // load library
		c.voidEval("library(ROCR)");
		c.voidEval("library(MASS)");
		
		// train the model
		// mtry is the number of features used for predictor
		c.voidEval("tm.xdf=qda(High~.-Sales, data=xdf, subset=train)"); 
		c.voidEval("tm.pred=predict(tm.xdf, xdf.test)");
		c.voidEval("tm.prob=(tm.pred$posterior)[,2]");
		c.voidEval("tm.class=as.character(tm.pred$class)");             // transfer it to class label
		
		return tag;
		
/*		// evaluate performance
		c.voidEval("c=table(qda.class, High.test)");
		double acc=c.eval("(c[1]+c[4])/(c[1]+c[2]+c[3]+c[4])").asDouble();
		System.out.println("QDA accuracy: " + acc);
		
		c.voidEval("pred=prediction(qda.prob, High.test)");
		//c.voidEval("auc=performance(pred, measure=\"tpr\", x.measure=\"fpr\")");
		c.voidEval("auc=performance(pred, \"auc\")");
		double auc=c.eval("unlist(slot(auc, \"y.values\"))").asDouble();
		System.out.println("QDA AUC: " + auc);
		
		c.voidEval("acc=performance(pred, \"acc\")");
		acc=c.eval("max(unlist(slot(acc, \"y.values\")))").asDouble();
		System.out.println("QDA ACC: " + acc);
		
		c.voidEval("roc=performance(pred, measure=\"tpr\", x.measure=\"fpr\")");
		double[] roc_x = c.eval("unlist(slot(roc, \"x.values\"))").asDoubles();
		double[] roc_y = c.eval("unlist(slot(roc, \"y.values\"))").asDoubles();
		return new rResult(roc_x, roc_y, acc, auc);*/
			
	}		

}
