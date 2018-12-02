
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import static java.nio.file.Files.readAllBytes;
import static java.nio.file.Paths.get;
import java.util.concurrent.LinkedBlockingQueue;

class DataSpaces{
    public static LinkedBlockingQueue<String> wordspace = new LinkedBlockingQueue<String>();
    public static LinkedBlockingQueue<Map<String,Integer>> freqspace = new LinkedBlockingQueue<Map<String,Integer>>();
    public static List<Map<String,Integer>> alphaFreqspace;
    public static List<Map<String,Integer>> synAlphaFreqspace = Collections.synchronizedList(new ArrayList<Map<String,Integer>>());
}

class Worker implements Runnable{
    private Thread thread;
    public Worker() {
        thread = new Thread(this);
        thread.start();
    }

    @Override
    public void run() {
    	Map<String,Integer> wordFreqs = new HashMap<String, Integer>();
        
        while(true){
            String word = null;
            try{
            	word = (String) DataSpaces.wordspace.poll(10,TimeUnit.SECONDS);
                if(word == null) break;
            } catch (InterruptedException e) {
                break;
            }
            
            if(!TwentyNine.stopWords.contains(word) && (word.length()>1)){
                    if(wordFreqs.containsKey(word)){
                        wordFreqs.put(word, wordFreqs.get(word)+1);
                    }else{
                        wordFreqs.put(word, 1);
                    }
            }
        }
        
        try {
            DataSpaces.freqspace.put(wordFreqs);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void join() throws InterruptedException{
        this.thread.join();
    }   
}

class Merger implements Runnable{
    private String alpha;
    private Thread thread;
    public Merger(String alpha) {
        this.alpha = alpha;
        thread = new Thread(this);
        thread.start();
    }
    @Override
    public void run() {
        Map<String,Integer> part = new HashMap<String, Integer>();
        for(Map<String,Integer>partial : DataSpaces.alphaFreqspace) {
            for (String key : partial.keySet()) {
                if (key.matches(alpha + "\\w*")) {
                    if (part.containsKey(key)) {
                        part.put(key, part.get(key) + partial.get(key));
                    } else {
                        part.put(key, partial.get(key));
                    }
                }
            }
        }
        DataSpaces.synAlphaFreqspace.add(part);
    }

    public void join() throws InterruptedException {
        this.thread.join();
    }
}

public class TwentyNine {
    public static List<String> stopWords = new ArrayList<String>();
    private static List<String> wordList = new ArrayList<String>();
    
    public static void main(String args[]) throws InterruptedException {
        try {
            stopWords = new ArrayList<String>(Arrays.asList(new String(readAllBytes(get("../stop_words.txt"))).split(",")));
            wordList = new ArrayList<String>(Arrays.asList(new String(readAllBytes(get("../"+args[0]))).toLowerCase().replaceAll("[^a-z]+", " ").split(" ")));
        } catch (IOException e) {
            e.printStackTrace();
        }

        for(String word : wordList){
            DataSpaces.wordspace.put(word);
        }
        
        List<Worker> workers = new ArrayList<Worker>();
        for(int i=0;i<3;i++){
           workers.add(new Worker());
        }
        for(Worker w : workers){
            w.join();
        }
        List<Map<String,Integer>> freqs = new ArrayList<Map<String, Integer>>();
        DataSpaces.freqspace.drainTo(freqs);
        DataSpaces.alphaFreqspace = Collections.synchronizedList(freqs);
        
        List<Merger> mergers = new ArrayList<Merger>();
        Merger Merger1 = new Merger("[a-e]");
        Merger Merger2 = new Merger("[f-j]");
        Merger Merger3 = new Merger("[k-o]");
        Merger Merger4 = new Merger("[p-t]");
        Merger Merger5 = new Merger("[u-z]");
        mergers.add(Merger1);
        mergers.add(Merger2);
        mergers.add(Merger3);
        mergers.add(Merger4);
        mergers.add(Merger5);
        for(Merger m : mergers){
            m.join();
        }
        Map<String,Integer> freqsMap = new HashMap<String, Integer>();
        for(Map<String,Integer> part : DataSpaces.synAlphaFreqspace){
        	freqsMap.putAll(part);
        }
        TreeMap<String,Integer> sortedMap = new TreeMap<String,Integer>(new frequencyComparator(freqsMap));
        sortedMap.putAll(freqsMap);

        int count = 0;
        for(Map.Entry<String, Integer> entry : sortedMap.entrySet()){
            System.out.println(entry.getKey()+ " - " + entry.getValue());
            if(count>=24) break;
            count++;
        }
    }
    static class frequencyComparator implements Comparator<String> {
        Map<String, Integer> termFrequency;
        public frequencyComparator(Map<String, Integer> base) {
            this.termFrequency = base;
        }
        public int compare(String a, String b) {
            if (termFrequency.get(a) >= termFrequency.get(b)) {
                return -1;
            } else {
                return 1;
            }
        }
    }
}
