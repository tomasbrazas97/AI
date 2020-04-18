// Tomas Brazas G00349242
package ie.gmit.sw;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.concurrent.*;

import net.sourceforge.jFuzzyLogic.FIS;
import net.sourceforge.jFuzzyLogic.FunctionBlock;
import net.sourceforge.jFuzzyLogic.rule.Variable;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.stream.Collectors;


public class NodeParser {
	// Constants
	private final int MAX = 20;
	private final int TITLE_WEIGHT = 50;
	private final int HEADING1_WEIGHT = 25;
	private final int PARAGRAPH_WEIGHT = 1;
	
	// Variables
	private Map<String, Integer> wordMap = new ConcurrentHashMap<>();
	private WordFrequency[] words;
	private Map<String, Integer> sortedByCount;
	private String term;

	private List<String> ignoreWords;
	private Set<String> closed = new ConcurrentSkipListSet<>();
	private Queue<DocumentNode> queue = new PriorityQueue<>(Comparator.comparing(DocumentNode::getScore));
	
	// Parser
	public NodeParser(String url, String term) throws Exception {
		this.term = term;
		System.out.println("Searching...");
		Document doc = Jsoup.connect(url + term).get();
		int score = getHeuristicScore(doc);

		// Load ignored file, add url to closed set, queue doc, process search duckduckgo
		loadIgnoreWords();
		closed.add(url);
		queue.offer(new DocumentNode(doc, score));		
		search();

		// Print Map
		sortedByCount = sortByValue(wordMap);
		WordFrequency[] words = new WordFrequency[20];
		words = getWordFrequencyKeyValue(20);
		System.out.println("Done - Finished searching");
	}
	
	// Search process
	private void search() {		
		// While the size of closedList is less than MAX and the openList isn't empty,
		// search for the term entered 
		while(!queue.isEmpty() && closed.size() <= MAX) {
			DocumentNode node = queue.poll();
			Document doc = node.getDocument();
			
			Elements edges = doc.select("a[href]");
			
			for(Element e : edges) {
				String link = e.absUrl("href");
				
				if(closed.size() <= MAX && link != null && !closed.contains(link)) {
					try {
						System.out.println(link);
						Document child = Jsoup.connect(link).get();
						int score = getHeuristicScore(child);
						closed.add(link);
						queue.offer(new DocumentNode(child, score));
					} catch (IOException ex) {
						
					}
					
				}
			}
		}
		
	}

	// Heuristic Score 
	private int getHeuristicScore(Document doc) {
		int heuristicScore = 0;
		double fuzzyScore = 0;
		int titleScore = 0;
		int headingScore = 0;
		int bodyScore = 0;

		// Assign titleScore
		titleScore += getFrequency(doc.title()) * TITLE_WEIGHT;

		String title = doc.title();

		System.out.println(closed.size() + " >> " + title);
		Elements headings = doc.select("h1");
		for (Element heading : headings){
			String h1 = heading.text();
			// Assign headingScore
			headingScore  += getFrequency(h1) * HEADING1_WEIGHT;
		}

		// Assign body score
		String body = doc.body().text();
		bodyScore  = getFrequency(body) * PARAGRAPH_WEIGHT;

		fuzzyScore = getFuzzyHeuristics(titleScore, headingScore, bodyScore);


		if(fuzzyScore > 20.0)
		{
			index(title, headings.text(), body);
		}

		heuristicScore = titleScore + headingScore + bodyScore;


		// Print Score that will be passed into wcloud.fcl
		System.out.println("Fuzzy title to process: " + titleScore);
		System.out.println("Fuzzy heading to process: " + headingScore);
		System.out.println("Fuzzy body to process: " + bodyScore);

		System.out.println("Fuzzy score: " + fuzzyScore);

		return heuristicScore;
	}
	
	private String[] parseString(String... string){
		String fullString = String.join(" ", string);
		String delims = "[ #1234567890,.\"\'-/:$%&();?!| ]+";


		for (char c : fullString.toCharArray()) {
			if (!(c >= 'a' && c <= 'z') && !(c >= 'A' && c <= 'Z')) {
				String[] wordArray = fullString.split(delims);
				return wordArray;
			}
		}
		return null;

	}
	
	// Frequency of term in wordArray 
	private int getFrequency(String s){
		int count = 0;
		String[] wordArray = parseString(s);
		try {
			for (String word : wordArray) {
				if (word.equalsIgnoreCase(term))
					count++;
			}
			System.out.println("Frequency Count: " + count);
			return count;
		}catch (Exception e){
			return 0;
		}

	}

	// String array text gets added to a single string
	private void index(String... blockOfText){
		String[] wordArray = parseString(blockOfText);

		for (String word : wordArray) {
			try{
				// Extract word from wordArray and add to map after removing ignore words
				if(!ignoreWords.contains(word) && word.length() > 4){
					Integer n = wordMap.get(word);
					n = (n == null) ? 1 : ++n;
					wordMap.put(word, n);

				}
			}
			catch (Exception ex){

			}


		}
	}

	// Load FIS
	private double getFuzzyHeuristics(int titles, int headings, int body){
		FIS fis = FIS.load("./res/wcloud.fcl", true);
		FunctionBlock functionBlock = fis.getFunctionBlock("wcloud");
		// Variables
		fis.setVariable("title", titles);
		fis.setVariable("heading", headings);
		fis.setVariable("body", body);
		fis.evaluate();
		// Get output variables
		Variable score = functionBlock.getVariable("score");

		return score.getValue();
	}
	
	// 
	public WordFrequency[] getWordFrequencyKeyValue(int numberOfWords) {
		words = new WordFrequency[numberOfWords+1];
		int i = 1;
		// Output Map, word and counter int
		for(Map.Entry<String, Integer> entry: sortedByCount.entrySet()){
			System.out.println("Top Word: " + entry.getKey() + " >> Counter: " + entry.getValue());
			words[i] = new WordFrequency(entry.getKey(),entry.getValue());

			if(i == numberOfWords){
				return words;
			}
			i++;
		}
		return words;
	}

	// Load ignorewords.txt and put them into array
	private void loadIgnoreWords() throws FileNotFoundException {
		Scanner s = new Scanner(new File("./res/ignorewords.txt"));
		ignoreWords = new ArrayList<>();
		while (s.hasNext()){
			ignoreWords.add(s.next());
		}
		s.close();
	}
	
	private static Map<String, Integer> sortByValue(final Map<String, Integer> wordCounts) {
		return wordCounts.entrySet()
				.stream()
				.sorted((Map.Entry.<String, Integer>comparingByValue().reversed()))
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));

	}

	// Main method
	public static void main(String[] args) throws Exception {
		new NodeParser("https://duckduckgo.com/html/?q=", "Software");
		
		
	}

}
