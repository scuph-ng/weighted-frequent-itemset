import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;
import java.util.List;
import java.util.Set;

/**
 * This class represents an itemset (a set of items)
 * as used by the WPFIApriori algorithm uncertain itemset mining.
 *
 * @see AlgoWPFIApriori
 * @see ItemWPFIApriori
 * @author Philippe Fournier-Viger
 */
public class wPFIItemset {
  private Set<wPFIItem> items = new HashSet<wPFIItem>();
  private double expectedsupport = 0;

  public wPFIItemset() {
  }

  /**
   * Get the expected support of this itemset.
   * 
   * @return expected support value.
   */
  public double getExpectedSupport() {
    return expectedsupport;
  }

  /**
   * Set the expected support to a given value.
   * 
   * @param expectedsupport the value
   */
  void setExpectedSupport(double expectedsupport) {
    this.expectedsupport = expectedsupport;
  }

  /**
   * Get items from that itemset.
   * 
   * @return a list of integers (items).
   */
  public Set<wPFIItem> getItems() {
    return items;
  }

  /**
   * Get the item at at a given position in that itemset
   *
   * @param index the position
   * @return the item (Integer)
   *         public wPFIItem get(int index) {
   *         return items.get(index);
   *         }
   */

  /**
   * Add an item to that itemset
   * 
   * @param value the item to be added
   */
  void addItem(wPFIItem value) {
    items.add(value);
  }

  void clear() {
    items.clear();
  }

  /**
   * Set the items in this itemsets.
   * 
   * @param items a list of items.
   *              void setItems(List<wPFIItem> items) {
   *              this.items = items;
   *              }
   */

  /**
   * Get the number of items in this itemset
   * 
   * @return the item count (int)
   */
  public int size() {
    return items.size();
  }

  /**
   * Check if this itemset contains a given item.
   * 
   * @param item the item
   * @return true, if yes, otherwise false.
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
    // if not the same size, they can't be equal!
    if (items.size() != itemset2.items.size()) {
      return false;
    }
    // for each item
    for (wPFIItem validateItem : items) {
      // check if it is contained in the other itemset
      // if not they are not equal.
      if (!itemset2.contains(validateItem)) {
        return false;
      }
    }
    // they are equal, then return true
    return true;
  }

  public int hashCode(wPFIItemset itemset2) {
    return Objects.hashCode(items);
  }

  /**
   * Get the expected support as a five decimals string
   * 
   * @return a string
   */
  public String getSupportAsString() {
    DecimalFormat format = new DecimalFormat();
    format.setMinimumFractionDigits(0);
    format.setMaximumFractionDigits(5);
    return format.format(expectedsupport);
  }

  /**
   * print this itemset to System.out.
   */
  public void print() {
    System.out.println(toString());
  }

  /**
   * Print the items in this itemset to System.out.
   */
  public void printWithoutSupport() {
    StringBuilder r = new StringBuilder();
    for (wPFIItem attribute : items) {
      r.append(attribute.getId());
      r.append(' ');
    }
    System.out.print(r);
  }

  /**
   * Get a string representation of the items in this itemset.
   */
  public String toString() {
    StringBuilder r = new StringBuilder();
    for (wPFIItem attribute : items) {
      r.append(attribute.getId());
      r.append(' ');
    }
    return r.toString();
  }
}
