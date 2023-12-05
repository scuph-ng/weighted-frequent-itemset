/**
 * This class represents an item from a transaction database
 * as used by the WPFIApriori algorithm uncertain itemset mining.
 * 
 * @see AlgoWPFIApriori
 * @see UncertainTransactionDatabase
 * @see ItemsetWPFIApriori
 * @author Philippe Fournier-Viger
 */
public class wPFIItem {
  // the item id
  private final int id;
  // the probability associated to that item
  private final double probability;

  /**
   * Constructor
   * 
   * @param id          id ot the item
   * @param probability the existential proability
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
   * Get a string representation of this item.
   * 
   * @return a string
   */
  public String toString() {
    return "" + getId() + " (" + probability + ")";
  }

  /**
   * Check if this item is equal to another.
   * 
   * @param object another item
   * @return true if equal, otherwise false.
   */
  public boolean equals(Object object) {
    wPFIItem item = (wPFIItem) object;
    // if the same id, then true
    if ((item.getId() == this.getId())) {
      return true;
    }
    // if not the same id, then false
    return false;
  }

  /**
   * Generate an hash code for that item.
   * 
   * @return an hash code as a int.
   */
  public int hashCode() {
    String string = "" + getId();
    return string.hashCode();
  }

  /**
   * Get the existential probability associated to this item
   * 
   * @return the probability as a double
   */
  public double getProbability() {
    return probability;
  }
}
