import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class represents an uncertain database with existential probabilities,
 * as used by the wPFIApriori algorithm for uncertain itemset mining.
 *
 * @author Nguyen Hoang Phuc (scuph-ng)
 *
 * @see wPFIApriori
 * @see wPFIItem
 */
public class UncertainDatabase {
  /**
   * Define the list of transactions and the set of items that exist in the
   * database.
   */
  private final HashSet<wPFIItem> allItems = new HashSet<wPFIItem>();
  private final ArrayList<HashSet<wPFIItem>> transactions = new ArrayList<>();

  /**
   * Get the database size.
   *
   * @return int
   */
  public int size() {
    return transactions.size();
  }

  /**
   * Get the list of transactions.
   *
   * @return the list of Transactions.
   */
  public ArrayList<HashSet<wPFIItem>> getTransactions() {
    return transactions;
  }

  /**
   * Get the set of items in this database.
   *
   * @return a Set of Items
   */
  public HashSet<wPFIItem> getAllItems() {
    return allItems;
  }

  /**
   * Load a transaction database from a file.
   *
   * @param path the directory of the file
   * @throws IOException exception if error while reading the file.
   */
  public void loadFile(String path, boolean hasProbability) throws IOException {
    String thisLine;
    BufferedReader myInput = null;
    int maxSize = 10000;
    int lineCount = 0;

    try {
      FileInputStream fin = new FileInputStream(new File(path));
      myInput = new BufferedReader(new InputStreamReader(fin));

      while ((thisLine = myInput.readLine()) != null) {

        if (thisLine.isEmpty() == true ||
            thisLine.charAt(0) == '#' || thisLine.charAt(0) == '%'
            || thisLine.charAt(0) == '@') {
          continue;
        }

        if (hasProbability)
          processTransactionsWithProbability(thisLine.split(" "));
        else
          processTransactions(thisLine.split(" "));
        
        lineCount++;
        if (lineCount >= maxSize)
          break;
      }
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      if (myInput != null) {
        myInput.close();
      }
    }

    printDatabaseProperties(path);
  }

  /**
   * Process a transaction from a list of items, then add it to the
   * transaction list.
   *
   * @param itemsString the list of items
   */
  private void processTransactions(String itemsString[]) {
    HashSet<wPFIItem> transaction = new HashSet<>();

    for (String itemString : itemsString) {
      int itemID = Integer.parseInt(itemString);
      double value = gaussianDistribution();

      wPFIItem item = new wPFIItem(itemID, value);
      transaction.add(item);
      allItems.add(item);
    }

    transactions.add(transaction);
  }

  private void processTransactionsWithProbability(String itemsString[]) {
    HashSet<wPFIItem> transaction = new HashSet<>();
    String pattern = "\\((\\d+),(\\d+\\.\\d+)\\)";

    Pattern p = Pattern.compile(pattern);

    for (String itemString : itemsString) {
      try {
        Matcher matcher = p.matcher(itemString);
        if (!matcher.find())
          continue;

        int itemID = Integer.parseInt(matcher.group(1));
        double value = Double.parseDouble(matcher.group(2));

        if (value == 0)
          continue;

        wPFIItem item = new wPFIItem(itemID, value);
        transaction.add(item);
        allItems.add(item);
      } catch (Exception e) {
        e.printStackTrace();
        return;
      }
    }

    transactions.add(transaction);
  }

  /**
   * Print this database to System.out.
   */
  public void printDatabase(boolean hasProbability) {
    System.out.println("===================  UNCERTAIN DATABASE ===================");
    int count = 0;

    for (HashSet<wPFIItem> itemset : transactions) {
      System.out.print("0" + count + ":\t");

      if (hasProbability) {
        String str = "";

        for (wPFIItem item : itemset)
          str += item.toStringWithProbability();
        System.out.print(str);

      } else
        System.out.print(itemset.toString());

      System.out.println();
      count++;
    }
  }

  /**
   * Print this database to System.out.
   */
  public void printDatabaseProperties(String path) {
    System.out.println("=================== DATABASE PROPERTIES ===================");
    System.out.println("File path: " + path);
    System.out.println("Database size: " + transactions.size());
    System.out.println("Distinct items: " + allItems.size());
  }

  /**
   * Generate a random probability drawn from the Gaussian distribution.
   * Means is 0.5.
   * Variance is 0.125.
   */
  private static double gaussianDistribution() {
    Random random = new Random();
    double prob = random.nextGaussian() * Math.sqrt(0.125) + 0.5;

    prob = Math.round(prob * 10) / (Double) 10.0;

    if (prob > 1)
      return 1;

    if (prob <= 0)
      return 0.1;

    return prob;
  }
}
