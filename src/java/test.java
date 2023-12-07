import java.util.Objects;
import java.util.HashSet;
import java.util.Set;
import java.util.ArrayList;
import java.util.List;

class test {
  public static class Item {
    private final String name;
    private final int value;

    public Item(String name, int value) {
      this.name = name;
      this.value = value;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null || getClass() != obj.getClass()) {
        return false;
      }
      Item other = (Item) obj;
      return Objects.equals(name, other.name) && value == other.value;
    }

    @Override
    public int hashCode() {
      return Objects.hash(name, value);
    }
  }

  public static void main(String[] args) {
    // Create two sets
    Set<Item> set1 = new HashSet<>();
    set1.add(new Item("Item1", 10));
    set1.add(new Item("Item2", 20));

    Set<Item> set2 = new HashSet<>();
    set2.add(new Item("Item2", 20));
    set2.add(new Item("Item1", 10));

    // Check if sets are equal using hash code and equals
    int hashCode1 = set1.hashCode();
    int hashCode2 = set2.hashCode();

    if (hashCode1 == hashCode2 && set1.equals(set2)) {
      System.out.println("Sets are equal");
    } else {
      System.out.println("Sets are not equal");
    }
  }
  // protected static double probItemsetInTransaction(int j, wPFIItemset itemset,
  // List<wPFIItemset> dataset) {
  // wPFIItemset transaction = dataset.get(j - 1);
  //
  // if (itemset.size() > transaction.size())
  // return 0;
  //
  // double prob = 1;
  //
  // for (wPFIItem item : itemset.getItems()) {
  // boolean found = false;
  //
  // for (wPFIItem itemTX : transaction.getItems()) {
  // if (itemTX.equals(item)) {
  // found = true;
  //
  // System.out.println(itemTX.getId() + " " + itemTX.getProbability());
  // prob *= itemTX.getProbability();
  // break;
  // }
  // }
  //
  // if (!found)
  // return 0.0;
  // }
  //
  // return prob;
  // }
  //
  // protected static double frequentnessProbability(int i, int j, wPFIItemset
  // itemset, List<wPFIItemset> dataset) {
  // if (i == 0)
  // return 1;
  //
  // if (j == 0)
  // return 0;
  //
  // double prob = probItemsetInTransaction(j, itemset, dataset);
  //
  // double firstProb = frequentnessProbability(i - 1, j - 1, itemset, dataset) *
  // prob;
  // double secondProb = frequentnessProbability(i, j - 1, itemset, dataset) * (1
  // - prob);
  //
  // return firstProb + secondProb;
  // }
  //
  // public static void main(String[] args) {
  // List<wPFIItemset> dataset = new ArrayList<wPFIItemset>();
  //
  // wPFIItemset transaction0 = new wPFIItemset();
  // transaction0.addItem(new wPFIItem(1, 0.6));
  // transaction0.addItem(new wPFIItem(2, 0.4));
  // dataset.add(transaction0);
  //
  // wPFIItemset transaction1 = new wPFIItemset();
  // transaction1.addItem(new wPFIItem(1, 0.8));
  // transaction1.addItem(new wPFIItem(2, 0.2));
  // dataset.add(transaction1);
  //
  // wPFIItemset transaction2 = new wPFIItemset();
  // transaction2.addItem(new wPFIItem(2, 0.5));
  // transaction2.addItem(new wPFIItem(3, 0.5));
  // dataset.add(transaction2);
  //
  // wPFIItemset transaction = new wPFIItemset();
  // transaction.addItem(new wPFIItem(1, 0.1));
  // transaction.addItem(new wPFIItem(2, 0.1));
  //
  // System.out.println(frequentnessProbability(1, 3, transaction, dataset));
  // // System.out.println(probItemsetInTransaction(1, transaction, dataset));
  // }
}
