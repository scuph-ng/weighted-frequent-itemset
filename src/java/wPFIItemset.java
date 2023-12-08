import java.util.HashSet;
import java.util.Set;

/**
 * This class represents an itemset (a set of items)
 * as used by the wPFIApriori algorithm for uncertain itemset mining.
 *
 * @see UncertainDatabase
 * @see wPFIItem
 * @see wPFIApriori
 * @author Nguyen Hoang Phuc (scuph-ng)
 */
public class wPFIItemset {
  private Set<wPFIItem> items = new HashSet<wPFIItem>();
  private double expectedsupport = 0;

  public wPFIItemset() {
  }

  /**
   * Get the expected support of this itemset.
   *
   * @return a double
   */
  public double getExpectedSupport() {
    return expectedsupport;
  }

  /**
   * Set the expected support to a given value.
   *
   * @param value the expected support of the itemset
   */
  void setExpectedSupport(double value) {
    this.expectedsupport = value;
  }

  /**
   * Get all items from the itemset.
   *
   * @return a set of items.
   */
  public Set<wPFIItem> getItems() {
    return items;
  }

  /**
   * Add an item to the itemset
   * 
   * @param item the item to be added
   */
  void addItem(wPFIItem item) {
    items.add(item);
  }

  /**
   * Remove all the items from the itemset.
   */
  void clear() {
    items.clear();
  }

  /**
   * Get the itemset size
   *
   * @return an int
   */
  public int size() {
    return items.size();
  }

  /**
   * Check if this itemset contains a given item.
   *
   * @param item the item to be checked
   * @return true if yes, otherwise false.
   */
  public boolean contains(wPFIItem item) {
    return items.contains(item);
  }

  /**
   * Check if this itemset is equal to another one.
   *
   * @param itemset2 the other itemset
   * @return true if yes, otherwise false
   */
  public boolean isEqualTo(wPFIItemset itemset2) {
    if (items.size() != itemset2.items.size()) {
      return false;
    }

    for (wPFIItem validateItem : items) {
      if (!itemset2.contains(validateItem)) {
        return false;
      }
    }

    return true;
  }

  /**
   * print this itemset to System.out.
   */
  public void print() {
    System.out.println(toString());
  }

  /**
   * Print the items with its probability in this itemset to System.out.
   */
  public void printWithProbability() {
    System.out.println(toStringWithSupport());
  }

  /**
   * Get a string representation of the items in this itemset.
   */
  public String toString() {
    String str = "";
    for (wPFIItem item : items)
      str += item.getId() + " ";

    return str;
  }

  /**
   * Get a string representation of the items with its probability.
   */
  public String toStringWithSupport() {
    String str = "";
    for (wPFIItem item : items)
      str += "(" + item.getId() + ", " + item.getProbability() + ") ";

    return str;
  }
}
