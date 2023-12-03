import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Random;

/**
 * This class represents a transactions database with existential probabilities,
 * as used by the WPFIApriori algorithm uncertain itemset mining.
 * 
 * @see AlgoWPFIApriori
 * @see UncertainTransactionDatabase
 * @see ItemsetWPFIApriori
 * @see ItemWPFIApriori
 * @author Philippe Fournier-Viger
 */
public class UncertainDatabase {

  // this is the set of items in the database
  private final Set<wPFIItem> allItems = new HashSet<wPFIItem>();
  // this is the list of transactions in the database
  private final List<wPFIItemset> transactions = new ArrayList<wPFIItemset>();

  /**
   * Load a transaction database from a file.
   * 
   * @param path the path of the file
   * @throws IOException exception if error while reading the file.
   */
  public void loadFile(String path) throws IOException {
    String thisLine;
    BufferedReader myInput = null;
    try {
      FileInputStream fin = new FileInputStream(new File(path));
      myInput = new BufferedReader(new InputStreamReader(fin));
      // for each transaction (line) in the input file
      while ((thisLine = myInput.readLine()) != null) {
        // if the line is a comment, is empty or is a
        // kind of metadata
        if (thisLine.isEmpty() == true ||
            thisLine.charAt(0) == '#' || thisLine.charAt(0) == '%'
            || thisLine.charAt(0) == '@') {
          continue;
        }

        // process the transaction
        processTransactions(thisLine.split(" "));
      }
    } catch (Exception e) {
      // catch exceptions
      e.printStackTrace();
    } finally {
      if (myInput != null) {
        // close the file
        myInput.close();
      }
    }
  }

  private static double gaussianDistribution() {
    Random random = new Random();
    double prob = random.nextGaussian() * Math.sqrt(0.125) + 0.5;

    prob = Math.round(prob * 10) / (Double) 10.0;

    if (prob > 1)
      return 1;

    if (prob < 0)
      return 0.1;

    return prob;
  }

  private void processTransactions(String itemsString[]) {
    // create a new itemset oject representing the transaction
    wPFIItemset transaction = new wPFIItemset();
    // for each item
    for (String itemString : itemsString) {
      int itemID = Integer.parseInt(itemString);
      double value = gaussianDistribution();

      wPFIItem item = new wPFIItem(itemID, value);
      transaction.addItem(item);
      allItems.add(item);
    }
    // add the itemset to the transaction to the in-memory database
    transactions.add(transaction);
  }

  /**
   * Print this database to System.out.
   */
  public void printDatabase() {
    System.out
        .println("===================  UNCERTAIN DATABASE ===================");
    int count = 0;
    // for each transaction
    for (wPFIItemset itemset : transactions) {
      // print the transaction
      System.out.print("0" + count + ":  ");
      itemset.print();
      System.out.println("");
      count++;
    }
  }

  /**
   * Get the number of transactions.
   * 
   * @return a int
   */
  public int size() {
    return transactions.size();
  }

  /**
   * Get the list of transactions.
   * 
   * @return the list of Transactions.
   */
  public List<wPFIItemset> getTransactions() {
    return transactions;
  }

  /**
   * Get the set of items in this database.
   * 
   * @return a Set of Integers
   */
  public Set<wPFIItem> getAllItems() {
    return allItems;
  }

}
