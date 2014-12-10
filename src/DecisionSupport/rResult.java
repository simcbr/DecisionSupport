package DecisionSupport;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class rResult {
	
	private double[] x=null;
	private double[] y=null;
	private double acc=0;
	private double auc=0;
	private double[] dvalues=null;
	private String[] names=null;
	private String[] values=null;
	private Integer[] hist=null;
	private Integer[] periods=null;
	private Date[] dates=null;
	private int[] counts=null;
	
	private ArrayList<Map.Entry<String,Double>> ranksOpened=new ArrayList<Map.Entry<String,Double>>();
	private double avgOpened=0;
	private ArrayList<Map.Entry<String,Double>> ranksClosed=new ArrayList<Map.Entry<String,Double>>();
	private double avgClosed=0;
	
	private ArrayList<Map.Entry<String,Double>> distance=new ArrayList<Map.Entry<String,Double>>();
	
	
	public double[] getX() {
		return x;
	}

	public void setX(double[] x) {
		this.x = x;
	}

	public double[] getY() {
		return y;
	}

	public void setY(double[] y) {
		this.y = y;
	}

	public double getAcc() {
		return acc;
	}

	public void setAcc(double acc) {
		this.acc = acc;
	}

	public double getAuc() {
		return auc;
	}

	public void setAuc(double auc) {
		this.auc = auc;
	}

	public String[] getNames() {
		return names;
	}

	public void setNames(String[] names) {
		this.names = names;
	}

	public Integer[] getHist() {
		return hist;
	}

	public void setHist(int[] hist) {
		this.hist=new Integer[hist.length];
		for (int i=0; i<hist.length; i++)
		{
			this.hist[i]=hist[i];
		}
	}

	public void setHist(Integer[] hist) {
		this.hist=hist;
	}
	
	
	public Integer[] getPeriods() {
		return periods;
	}

	public void setPeriods(int[] periods) {
		this.periods=new Integer[periods.length];
		for (int i=0; i<periods.length; i++)
		{
			this.periods[i]=periods[i];
		}
	}

	public void setPeriods(Integer[] periods) {
		this.periods=periods;
	}

	public Date[] getDates() {
		return dates;
	}

	public void setDates(Date[] dates) {
		this.dates = dates;
	}

	public int[] getCounts() {
		return counts;
	}

	public void setCounts(int[] counts) {
		this.counts = counts;
	}

	public ArrayList<Map.Entry<String,Double>> getRanksOpened() {
		return ranksOpened;
	}
	
	public ArrayList<Map.Entry<String,Double>> getRanksClosed() {
		return ranksClosed;
	}

	public void setRanksOpened(Map<String,ArrayList<Long>> ranks) {
		
		HashMap<String, Double> h = new HashMap<String, Double>();
		setAvgOpened(0);
		double diff=0;
		Date now = new Date();
		for (String key : ranks.keySet())
		{
			diff = 1.0*(now.getTime()-ranks.get(key).get(0))/(1000*60*60*24); // in days
			h.put(key, diff);
			setAvgOpened(getAvgOpened() + diff);
		}
		setAvgOpened(getAvgOpened() / h.size());
		
		this.ranksOpened = new ArrayList<Map.Entry<String, Double>>(h.entrySet());
		Collections.sort(this.ranksOpened,
		         new Comparator<Object>() {
		             public int compare(Object o1, Object o2) {
		                 Map.Entry e1 = (Map.Entry) o1;
		                 Map.Entry e2 = (Map.Entry) o2;
		                 return ((Comparable) e1.getValue()).compareTo(e2.getValue());
		             }
		         });		
		
	}
	
	
	public void setRanksClosed(Map<String,ArrayList<Long>> ranks) {
		
		HashMap<String, Double> h = new HashMap<String, Double>();
		setAvgClosed(0);
		double diff=0;
		for (String key : ranks.keySet())
		{
			if (ranks.get(key).get(1).equals(ranks.get(key).get(0)))
			{
				continue;
			}
			diff =  1.0*(ranks.get(key).get(1)-ranks.get(key).get(0))/(1000*60*60*24); // in days
			h.put(key, diff); 
			setAvgClosed(getAvgClosed() + diff);
		}
		setAvgClosed(getAvgClosed() / h.size());
		
		this.ranksClosed = new ArrayList<Map.Entry<String, Double>>(h.entrySet());
		Collections.sort(this.ranksClosed,
		         new Comparator<Object>() {
		             public int compare(Object o1, Object o2) {
		                 Map.Entry e1 = (Map.Entry) o1;
		                 Map.Entry e2 = (Map.Entry) o2;
		                 return ((Comparable) e1.getValue()).compareTo(e2.getValue());
		             }
		         });		
		
	}

	public double getAvgOpened() {
		return avgOpened;
	}

	public void setAvgOpened(double avgOpened) {
		this.avgOpened = avgOpened;
	}

	public double getAvgClosed() {
		return avgClosed;
	}

	public void setAvgClosed(double avgClosed) {
		this.avgClosed = avgClosed;
	}

	public String[] getValues() {
		return values;
	}

	public void setValues(String[] values) {
		this.values = values;
	}

	public ArrayList<Map.Entry<String,Double>> getDistance() {
		return distance;
	}

	public void setDistance(ArrayList<Map.Entry<String,Double>> distance) {
		this.distance = distance;
	}


}
