Tomas Brazas G00349242
WCLOUD Artificial Interligence Project 

The aim of the application is to run a threaded search an AI search to follow hyperlinks and then parsed with Jsoup.
Apply Heuristics such as Fuzzy Logic and generate a word cloud with web app api.

NOTICE: The ServiceHandler does not compile in cmd prompt and therefore results in error 404 everytime /doProcess is loaded up.
The first page is fine. Throws http/servlet deplpoyment/cannot find symbol errors even though followed video,
tried various fixes; Apache logs stating java runtime being too new. NodeParser ran as java app shows that core is working.

-Application succesfully executes without manual intervention in the Tomcat webapps directory.
-Threaded search
-Heuristic Search
-Fuzzy Logic
-ignorewords.txt and wcloud.fcl stored in res folder.
-Integrated JSoup
-Option for user to switch between google/duckduckgo and yahoo in serviceHandler.

NodeParser.java
- Main Class
- When ran, instance of nodeparser; searches duckduckgo with chosen term. Loads ignoredtxt file for ignored
words, adds url to a closed set, queues doc and processes duckduckgo search. Sorts out the map and prints out top
20 most frequent words from the map. Calculates the fuzzy value.

______________________________________________________________________________________________
search()
While the size of closed list is elss than max and the openlist isnt empty, the search continues for the term entered. Documents enter the queue,
documentNode class is used to retrieve document node and assign it to Document variable.
For each elemt in elements, absolute url gets a url attribute (href). 

getHeuristicScore() - takes in document node
Initializes scoring, assigns titlescore to document titles. Assigns headingscore and frequency of headings. Assigns bodyscore.
HeuristicScore is calculated. All the variables are printed out to console for further processing. Records frequency of word.

getFrequency() - takes in String
Records the frequency of term. Initially 0. Calls parseString method that removes any numbers and such. The frequency is calculated.
Counter goes up when a word matches the term entered into search.

index() - takes in string
String array "blockoftext" gets added to a single string. Parsed and put into an array. Extracts word from array and adds to map
after removing ignore words. 

getFuzzyHeuristics() - takes in ints of title,headings and body
Load fis from file. Sets fis variables and get score variable using getValue().

WordFrequency[] getWordFrequencyKeyValue() - takes in int number of words
Outputs the map containing the word and int counter of frequency. For loop runs trhough the array and sorts it by count using sortedByCount.

loadIgnoreWords()
Loads ignorewords text file from res folder. Adds ignore words into an array list using while loop.

