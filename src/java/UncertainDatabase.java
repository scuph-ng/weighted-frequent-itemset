import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;

/**
 * This class represents an uncertain database with existential probabilities,
 * as used by the wPFIApriori algorithm for uncertain itemset mining.
 *
 * @see wPFIApriori
 * @see wPFIItemset
 * @see wPFIItem
 * @author Nguyen Hoang Phuc (scuph-ng)
 */
public class UncertainDatabase {
  /**
   * Define the list of transactions and the set of items that exist in the
   * database.
   */
  private final String file_name = new String();
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
  public void loadFile(String path) throws IOException {
    file_name.concat(path);
    String thisLine;
    BufferedReader myInput = null;

    try {
      FileInputStream fin = new FileInputStream(new File(path));
      myInput = new BufferedReader(new InputStreamReader(fin));

      while ((thisLine = myInput.readLine()) != null) {

        if (thisLine.isEmpty() == true ||
            thisLine.charAt(0) == '#' || thisLine.charAt(0) == '%'
            || thisLine.charAt(0) == '@') {
          continue;
        }

        processTransactions(thisLine.split(" "));
      }
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      if (myInput != null) {
        myInput.close();
      }
    }
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

  /**
   * Print this database to System.out.
   */
  public void printDatabase() {
    System.out.println("===================  UNCERTAIN DATABASE ===================");
    int count = 0;
    // for each transaction
    for (HashSet<wPFIItem> itemset : transactions) {
      // print the transaction
      System.out.print("0" + count + ":  ");
      System.out.println(itemset.toString());
      System.out.println("");
      count++;
    }
  }

  /**
   * Print this database to System.out.
   */
  public void printDatabaseProperties() {
    System.out.println("=================== DATABASE PROPERTIES ===================");
    System.out.println("File path: " + file_name);
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
