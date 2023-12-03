import java.util.ArrayList;
import java.util.List;

/**
 * This class represents a set of itemsets found in an uncertain database,
 * as used by the WPFIApriori algorithm uncertain itemset mining.
 * They are ordered by size. For example, level 1 means itemsets of size 1
 * (that contains 1 item).
 *
 * @see AlgoWPFIApriori
 * @see ItemWPFIApriori
 * @see ItemsetWPFIApriori
 * @author Philippe Fournier-Viger
 */
public class wPFIItemsets {
  // A list containing itemsets ordered by size
  // Level i contains itemsets of size i
  private final List<List<wPFIItemset>> levels = new ArrayList<List<wPFIItemset>>();
  // itemset class� par
  // taille

  // The number of itemsets
  private int itemsetsCount = 0;
  // A name given to those itemsets
  private final String name;

  /**
   * Constructor.
   * 
   * @param name a name to give to these itemsets
   */
  public wPFIItemsets(String name) {
    // remember the name
    this.name = name;
    // We create an empty level 0 by default.
    levels.add(new ArrayList<wPFIItemset>());
  }

  /**
   * Print all itemsets to the console (system.out).
   */
  public void printItemsets() {
    // print name
    System.out.println(" ------- " + name + " -------");
    int patternCount = 0;
    int levelCount = 0;
    // for each level
    for (List<wPFIItemset> level : levels) {
      // for each itemset in that level
      System.out.println("  L" + levelCount + " ");
      for (wPFIItemset itemset : level) {
        // print the itemset with the support and its utility value
        System.out.print("  pattern " + patternCount + ":  ");
        itemset.printWithoutSupport();
        System.out.print("support :  " + itemset.getSupportAsString());
        // increase counter to get the next pattern id
        patternCount++;
        System.out.println("");
      }
      levelCount++;
    }
    System.out.println(" --------------------------------");
  }

  /**
   * Add an itemset to these itemsets.
   * 
   * @param itemset the itemset to be added
   * @param k       the size of the itemset
   */
  public void addItemset(wPFIItemset itemset, int k) {
    while (levels.size() <= k) {
      levels.add(new ArrayList<wPFIItemset>());
    }
    levels.get(k).add(itemset);
    itemsetsCount++;
  }

  /**
   * Get the itemsets stored in this structure as a List of List where
   * position i contains the list of itemsets of size i.
   * 
   * @return the itemsets.
   */
  public List<List<wPFIItemset>> getLevels() {
    return levels;
  }

  /**
   * Get the total number of itemsets.
   * 
   * @return the itemset count.
   */
  public int getItemsetsCount() {
    return itemsetsCount;
  }

}
