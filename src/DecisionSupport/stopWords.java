package DecisionSupport;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class stopWords {
	
	ArrayList<String> words = new ArrayList<String>();
	private final int TOP20=20;
	
	public static stopWords stopper=null;
	
	public static stopWords getInstance()
	{
		if (stopper==null)
		{
			stopper = new stopWords();
		}

		try {
			stopper.installStopper();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return stopper;
	}	
	
	public void installStopper() throws IOException
	{
		FileInputStream fstream = new FileInputStream("stop-words.txt");
		BufferedReader br = new BufferedReader(new InputStreamReader(fstream));

		String strLine;

		//Read File Line By Line
		while ((strLine = br.readLine()) != null)   
		{
			if (!words.contains(strLine))
			{
				words.add(strLine);	
			}
		}

		//Close the input stream
		br.close();
	}
	
	
	public HashMap<String, Integer> segment(String str)
	{
		HashMap<String, Integer> ret=new HashMap<String, Integer>(); 
		
		if (str == null)
		{
			return ret;
		}
		
		//str = str.replaceAll("\\s+(?=[^\\[]*\\])", "");  // look ahead of non \\[ characters , closed with \\]
		
		// let's ignore the words within parentheses first.
		str = str.replaceAll("[\\p{Punct}]","");
		str = str.toLowerCase();
/*		Pattern pattern = Pattern.compile("(?<=\")[^\"]*(?=\")");
		Matcher matcher = pattern.matcher(str);
		int odd=0;
		while (matcher.find()) 
		{
			if (odd%2==0)
			{
	           String tmp = str.substring(matcher.start(), matcher.end()).replaceAll("\\s+", "");
	           CharSequence initial = str.subSequence(matcher.start(), matcher.end());
	           CharSequence target = tmp.subSequence(0, tmp.length());
	           str=str.replace(initial, target);
			}
			odd = odd+1;
        }*/
		 
		String[] objs = str.split("\\s+");
		
		for (String obj:objs)
		{
			Pattern pattern = Pattern.compile("^[\\p{Punct}\\d\\w]$");  // is not single special character or digit
			Matcher matcher = pattern.matcher(obj);
			if (obj.length()>0 && !insensitiveContain(words, obj) && 
					!(matcher.find()) )
			{
				if (!ret.containsKey(obj))
				{
					ret.put(obj, 1);
				}
				else
				{
					int tmp = ret.get(obj);
					ret.put(obj, tmp+1);
				}
			}
		}
		
	
		return ret;
	}
	
	
	private boolean insensitiveContain(ArrayList<String> strList, String obj)
	{
		boolean ret=false;
		
		for (String str:strList)
		{
			if (str.equalsIgnoreCase(obj))
			{
				ret=true;
				break;
			}
		}
		
		return ret;
	}

}
