
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class count
{
	public static String strFilter(String str) throws PatternSyntaxException
	{			
		String specialChar="[0-9`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？_]";
		Pattern pat = Pattern.compile(specialChar);             
		Matcher mat = pat.matcher(str);                      
		return mat.replaceAll(" ");  	
		}


	public static void main(String[] args) throws FileNotFoundException
	{
		File file=new File(args[0]);
		if(!file.exists())
		{
			System.out.println("No File Exists!");
			return;
		}
		Scanner scanner=new Scanner(file);
		HashMap<String, Integer > hashMap=new HashMap<String,Integer>();
		
		System.out.println("-----------------The Article------------------");
		while(scanner.hasNextLine())
		{
			String line=scanner.nextLine();
			System.out.println(line);
			line=strFilter(line);
			String[] lineWords=line.split("\\W+");
			Set<String> wordSet=hashMap.keySet();
			for(int i=0;i<lineWords.length;i++)
			{
				String currentWord = lineWords[i].toLowerCase();
				if(currentWord.length()>1) {
					//if the word has already existed，
					if(wordSet.contains(currentWord))
					{
						Integer counts=hashMap.get(currentWord);
						counts++;
						hashMap.put(currentWord, counts);
					}
					else 
					{
						hashMap.put(currentWord, 1);
					}
				}
			}
			
		}
		// Remove "" and stop words in the hashMap
		System.out.println("---------------Stop Words：---------------");
        File file1 = new File("stop_words.txt");
        FileInputStream swText = null;
        try {
        	swText = new FileInputStream(file1);
        } catch (FileNotFoundException e) {
            System.out.println("No File Exists!");
        }
        BufferedReader buf = new BufferedReader(new InputStreamReader(swText));
        String str; 
        try {
            while ((str = buf.readLine()) != null) {
                str = str.trim();
                String[] stopWords = str.split(",");
                for (int i = 0; i < stopWords.length; i++) {
                	System.out.println(stopWords[i]);
                    hashMap.remove(stopWords[i]);
                }
            }
        hashMap.remove("");
        }catch (IOException e) {
            e.printStackTrace();
        }

		System.out.println("---------------Term Frequencies：---------------");
		List<Map.Entry<String, Integer>> termFrequencyList = new LinkedList<>();
		termFrequencyList.addAll(hashMap.entrySet());
		termFrequencyList.sort(Comparator.comparingInt(e -> e.getValue()));
		termFrequencyList.forEach(System.out::println);
	    System.out.println("----------------The End--------------");
		
	}
}

