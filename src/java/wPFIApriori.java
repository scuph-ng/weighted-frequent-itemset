import java.io.IOException;
import java.util.Collections;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

/**
 * This class represent the frequent itemset mining algorithm in uncertain
 * datasets.
 *
 * @author Nguyen Hoang Phuc (scuph-ng)
 *
 * @see UncertainDatabase
 * @see wPFIItem
 */
public class wPFIApriori {
  /**
   * Define the neccesary variables for the algorithm
   */
  protected UncertainDatabase db;
  protected HashSet<wPFIItem> allItems;
  protected HashMap<Integer, Double> weightTable;

  protected int k;
  protected int minsup;
  protected int databaseSize;

  protected float t;

  protected long startTime;
  protected long endTime;

  /**
   * Constructor
   *
   * @param database the loaded database
   */
  public wPFIApriori(UncertainDatabase database) {
    db = database;

    /**
     * Assign the properties of the database.
     */
    databaseSize = database.size();
    allItems = database.getAllItems();

    /**
     * TODO: create a method to define whether
     * generate a weight table is necessary or not.
     */
    weightTable = generateWeightTable();
  }

  /**
   * The algorithm 1 from the research paper.
   *
   * @param msup_ratio   the ratio of minsup in the database
   * @param threshold    the probabilistic frequent threshold
   * @param scale_factor the scale factor
   */
  public void runAlgorithm(float msup_ratio, float threshold, float scale_factor) {
    startTime = System.currentTimeMillis();

    k = 1;
    t = threshold;
    minsup = (int) Math.round(msup_ratio * databaseSize);

    System.out.println("===========================================================");
    System.out.println("Minimum support ratio: " + minsup);
    System.out.println("Confidence threshold: " + threshold);

    ArrayList<ArrayList<HashSet<wPFIItem>>> wPFI = new ArrayList<ArrayList<HashSet<wPFIItem>>>();

    System.out.println("===========================================================");

    /**
     * Scan and find the size-1 probabilistic frequent itemset.
     */
    ArrayList<HashSet<wPFIItem>> wPFI_k = scanFindSize1();
    wPFI.add(wPFI_k);

    /**
     * Use while iteration to find the wPFI until no more new wPFI is discover.
     */
    while (wPFI_k.size() != 0) {
      HashSet<HashSet<wPFIItem>> candidateHashSet = new HashSet<>(wPFI_k);
      System.out.printf("There are %d\t size-%d candidates.", candidateHashSet.size(), k);
      System.out.println();

      ArrayList<HashSet<wPFIItem>> candidateK = wPFIAprioriGenerate(wPFI.get(k - 1));
      wPFI_k = scanFindSizeK(candidateK);
      wPFI.add(wPFI_k);
      k++;
    }

    endTime = System.currentTimeMillis();
    System.out.println("===========================================================");
    System.out.printf("Total runtime: %dms", endTime - startTime);
  }

  /**
   * Generate a weight table for each item in the database.
   */
  protected HashMap<Integer, Double> generateWeightTable() {
    HashMap<Integer, Double> weightTable = new HashMap<Integer, Double>();
    Random random = new Random();

    for (wPFIItem item : allItems) {
      weightTable.put(item.getId(), random.nextDouble());
    }

    return weightTable;
  }

  /**
   * Return the mean of item weight in a given itemset.
   *
   * @param itemset the itemset which used to calculate
   * @return a double
   */
  protected double itemsetWeight(HashSet<wPFIItem> itemset) {
    double sumWeight = 0;

    for (wPFIItem item : itemset) {
      sumWeight += weightTable.get(item.getId());
    }

    return sumWeight / itemset.size();
  }

  /**
   * Scan and find the size-1 probabilistic frequent itemsets.
   *
   * @return a list of itemset that satisfied the condition
   */
  protected ArrayList<HashSet<wPFIItem>> scanFindSize1() {
    ArrayList<HashSet<wPFIItem>> new_candidates = new ArrayList<HashSet<wPFIItem>>();

    HashSet<wPFIItem> candidate = new HashSet<wPFIItem>();
    for (wPFIItem item : allItems) {
      candidate.add(item);

      double candidate_weight = weightTable.get(item.getId());
      double candidate_confidence = Pr(candidate);

      // System.out.print(candidate.toString());
      // System.out.printf("\t%2f\t", candidate_weight);
      // System.out.println(candidate_confidence);

      if (candidate_confidence * candidate_weight >= t)
        new_candidates.add(candidate);

      candidate.clear();
    }

    return new_candidates;
  }

  /**
   * Scan and find the size-k probabilistic frequent itemsets.
   *
   * @param wPFI_k list of the size-k candidate itemsets
   * @return a list of the itemset that satisfied the condition
   */
  protected ArrayList<HashSet<wPFIItem>> scanFindSizeK(ArrayList<HashSet<wPFIItem>> wPFI_k) {
    ArrayList<HashSet<wPFIItem>> new_candidates = new ArrayList<HashSet<wPFIItem>>();

    for (HashSet<wPFIItem> candidate : wPFI_k) {
      double candidate_weight = itemsetWeight(candidate);
      double candidate_confidence = Pr(candidate);

      // System.out.print(candidate.toString());
      // System.out.printf("\t%2f\t", candidate_weight);
      // System.out.println(candidate_confidence);

      if (candidate_confidence * candidate_weight >= t)
        new_candidates.add(candidate);
    }

    return new_candidates;
  }

  /**
   * Calculate the probability the the given itemset is exist in the j-th
   * transaction.
   *
   * @param j       the index of the transaction
   * @param itemset the itemset used to calculate the probability
   * @return a double
   */
  protected double itemsetSupportInTransaction(int j, HashSet<wPFIItem> itemset) {
    HashSet<wPFIItem> transaction = db.getTransactions().get(j);

    /**
     * The itemset will not exist in the transaction if its size is larger than the
     * transaction's.
     */
    if (itemset.size() > transaction.size())
      return 0;

    double probability = 1;

    /**
     * The probability would be return as 0 if an item in the itemset is not exist
     * in the transaction.
     */
    for (wPFIItem item : itemset) {
      boolean found = false;

      for (wPFIItem itemTransaction : transaction) {
        if (itemTransaction.equals(item)) {
          found = true;
          probability *= itemTransaction.getProbability();
          break;
        }
      }

      if (!found)
        return 0;
    }
    return probability;
  }

  /**
   * Calculate the probability the given itemset exists in the first j
   * transactions at least i-times. Or P(sup(itemset) >= i) in the first j
   * transactions
   *
   * @param itemset the itemset used for calculating support
   * @return a double
   */
  protected double Pr(HashSet<wPFIItem> itemset) {
    double[][] P = new double[minsup + 1][databaseSize + 1];
    double mu_itemset = 0;

    double[] probabilities = new double[databaseSize];

    for (int i = 0; i < databaseSize; i++) {
      probabilities[i] = itemsetSupportInTransaction(i, itemset);
      mu_itemset += probabilities[i];
    }

    for (int j = 0; j <= databaseSize; j++) {
      P[0][j] = 1.0;
    }

    for (int i = 1; i <= minsup; i++) {
      if (P[i - 1][databaseSize - minsup + i] < t) {
        return 0.0;
      }

      for (int j = i; j <= databaseSize; j++) {
        P[i][j] = P[i - 1][j - 1] * probabilities[j - 1] + P[i][j - 1] * (1 - probabilities[j - 1]);
      }
    }
    // System.out.println(itemset + "\t" + P[minsup][databaseSize]);
    return P[minsup][databaseSize];
  }

  /**
   * Find the minimum weight of the items in the given itemset.
   *
   * @param itemset
   * @return a double
   */
  protected double minWeightItemset(HashSet<wPFIItem> itemset) {
    double minWeight = 1.1;
    double itemWeight;

    for (wPFIItem item : itemset) {
      itemWeight = weightTable.get(item.getId());

      if (itemWeight < minWeight) {
        minWeight = itemWeight;
      }
    }

    return minWeight;
  }

  /**
   * Algorithm 2 in the research paper.
   * This algorithm is used for generating the size-k candidate itemsets and early
   * pruning.
   *
   * @param wPFI_K_1 the size-k-1 wPF itemsets
   * @return a list of size-k candidates itemsets.
   */
  protected ArrayList<HashSet<wPFIItem>> wPFIAprioriGenerate(ArrayList<HashSet<wPFIItem>> wPFI_K_1) {
    ArrayList<HashSet<wPFIItem>> candidateK = new ArrayList<HashSet<wPFIItem>>();

    HashSet<wPFIItem> I_ = new HashSet<wPFIItem>();
    HashSet<wPFIItem> differentSet = new HashSet<>();
    HashSet<wPFIItem> tempCandidate = new HashSet<>();

    for (HashSet<wPFIItem> candidate : wPFI_K_1) {
      I_.addAll(candidate);
    }

    for (HashSet<wPFIItem> candidate : wPFI_K_1) {
      differentSet.addAll(I_);
      differentSet.removeAll(candidate);

      for (wPFIItem item : differentSet) {
        tempCandidate.addAll(candidate);
        tempCandidate.add(item);

        if (itemsetWeight(tempCandidate) < t) {
          tempCandidate.clear();
          continue;
        }

        candidateK.add(new HashSet<>(tempCandidate));
        tempCandidate.clear();
      }

      double argmin = minWeightItemset(candidate);

      tempCandidate.clear();
      differentSet.addAll(allItems);
      differentSet.removeAll(I_);
      // differentSet.removeAll(candidate);

      for (wPFIItem item : differentSet) {
        tempCandidate.addAll(candidate);
        tempCandidate.add(item);

        if (itemsetWeight(tempCandidate) < t) {
          tempCandidate.clear();
          continue;
        }
        if (weightTable.get(item.getId()) >= argmin) {
          tempCandidate.clear();
          continue;
        }

        candidateK.add(new HashSet<>(tempCandidate));
        tempCandidate.clear();
      }

      differentSet.clear();
    }

    return candidateK;
  }

  /**
   * Algorithm 3 in the research paper.
   */
  public void wPFIAprioriGenerate() {
    ArrayList<HashSet<wPFIItem>> candidateK = new ArrayList<HashSet<wPFIItem>>();
    double maxWeight = (Collections.max(weightTable.values()));
  }

  public static void main(String[] args) throws IOException {
    UncertainDatabase database = new UncertainDatabase();
    database.loadFile("./../../data/connect.dat", false);

    float msup_ratio = Float.parseFloat(args[0]);
    float threshold = Float.parseFloat(args[1]);
    float scale_factor = Float.parseFloat(args[2]);
    
    wPFIApriori apriori = new wPFIApriori(database);
    apriori.runAlgorithm(msup_ratio, threshold, scale_factor);
  }
}
