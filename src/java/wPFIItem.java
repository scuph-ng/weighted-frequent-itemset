/**
 * This class represents an item from an uncertain database
 * as used by the wPFIApriori algorithm for uncertain itemset mining.
 *
 * @author Nguyen Hoang Phuc (scuph-ng)
 * 
 * @see UncertainDatabase
 * @see wPFIApriori
 * @see wPFIItemset
 */
public class wPFIItem {
  private final int id;
  private final double probability;

  /**
   * Constructor
   *
   * @param id          id of the item
   * @param probability the existential probability
   */
  public wPFIItem(int id, double probability) {
    this.id = id;
    this.probability = probability;
  }

  /**
   * Get the item id.
   * 
   * @return a int.
   */
  public int getId() {
    return id;
  }

  /**
   * Get the existential probability of the item.
   *
   * @return a double
   */
  public double getProbability() {
    return probability;
  }

  /**
   * Check if this item is equal to another.
   *
   * @param object another item
   * @return true if equal, otherwise false.
   */
  public boolean equals(Object object) {
    wPFIItem anotherItem = (wPFIItem) object;
    if ((anotherItem.getId() == this.getId())) {
      return true;
    }
    return false;
  }

  /**
   * Generate a hash code for the item.
   *
   * @return a hash code as a int.
   */
  public int hashCode() {
    String string = "" + getId();
    return string.hashCode();
  }

  /**
   * Get a string representation of this item.
   * 
   * @return a string
   */
  public String toString() {
    return getId() + " ";
  }

  /**
   * Get a sting representation of this item and its probability/
   */
  public String toStringWithProbability() {
    return "(" + getId() + "," + probability + ") ";
  }
}
