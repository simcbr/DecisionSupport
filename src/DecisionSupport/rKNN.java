package DecisionSupport;

import org.rosuda.REngine.Rserve.RserveException;

public class rKNN extends rMLModel{

	private String tag="KNN";
	
	protected String train(fDocument fdoc) throws RserveException
	{    
		c=getRC();		
		
		System.out.println("K-NearestNeighbor training...");		
		
	    // load library
		c.voidEval("library(class)");
		
		// train the model
		// mtry is the number of features used for predictor
		c.voidEval("tm.class=knn(data.matrix(xdf[train,names(xdf)!=\"Sales\"]),data.matrix(xdf[-train,names(xdf)!=\"Sales\"]),High[train],k=10)"); 
		c.voidEval("tm.prob=knn(data.matrix(xdf[train,names(xdf)!=\"Sales\"]),data.matrix(xdf[-train,names(xdf)!=\"Sales\"]),High[train],k=10, prob=TRUE)");
		c.voidEval("tm.prob=attributes(tm.prob)$prob");	
		
		return tag;
	}

	
}
