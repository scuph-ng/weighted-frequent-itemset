import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.List;
import java.util.Set;

/**
 * This class represent the frequent itemset mining algorithm in uncertain
 * datasets.
 *
 * @see UncertainDatabase
 * @see wPFIItemset
 * @see wPFIItem
 *
 * @author Nguyen Hoang Phuc (scuph-ng)
 */
public class wPFIApriori {
  /**
   * Define the neccesary variables for the algorithm
   */
  protected UncertainDatabase db;
  protected Set<wPFIItem> allItems;
  protected HashMap<Integer, Double> weightTable;

  protected int k;
  protected int minsup;
  // protected int dbScanCount = 0;
  protected int lastTransactionIndex;

  protected double t;

  protected long startTime;
  protected long endTime;

  /**
   * Constructor
   *
   * @param database the loaded database
   */
  public wPFIApriori(UncertainDatabase database) {
    db = database;
    database.printDatabaseProperties();
    /**
     * Assign the properties of the database.
     */
    allItems = database.getAllItems();
    lastTransactionIndex = db.size() - 1;
  }

  /**
   * The algorithm 1 from the research paper.
   *
   * @param msup_ratio   the ratio of minsup in the database
   * @param threshold    the probabilistic frequent threshold
   * @param scale_factor the scale factor
   */
  public void runAlgorithm(double msup_ratio, double threshold, double scale_factor) {
    startTime = System.currentTimeMillis();

    k = 1;
    t = threshold;
    minsup = (int) msup_ratio * db.size();

    List<List<wPFIItemset>> wPFI = new ArrayList<List<wPFIItemset>>();

    /**
     * TODO: create a method to define whether
     * generate a weight table is necessary or not.
     */
    weightTable = generateWeightTable();

    System.out.println("===========================================================");
    /**
     * Scan and find the size-1 probabilistic frequent itemset.
     */
    List<wPFIItemset> wPFI_k = scanFindSize1();
    wPFI.add(wPFI_k);

    /**
     * Use while iteration to find the wPFI until no more new wPFI is found.
     */
    while (wPFI_k.size() != 0) {
      System.out.printf("There are %d candidates size %d", wPFI_k.size(), k);
      System.out.println();

      List<wPFIItemset> candidateK = wPFIAprioriGenerate(wPFI.get(k - 1));
      wPFI_k = scanFindSizeK(candidateK);
      wPFI.add(wPFI_k);
      k++;
    }
  }

  /**
   * Generate a weight table for each item in the database.
   */
  private HashMap<Integer, Double> generateWeightTable() {
    HashMap<Integer, Double> weightTable = new HashMap<Integer, Double>();
    Random random = new Random();

    for (wPFIItem item : allItems) {
      weightTable.put(item.getId(), random.nextDouble());
    }

    return weightTable;
  }

  /**
   * Scan and find the size-1 probabilistic frequent itemsets.
   *
   * @return a list of itemset that satisfied the condition
   */
  protected List<wPFIItemset> scanFindSize1() {
    List<wPFIItemset> new_candidates = new ArrayList<wPFIItemset>();

    for (wPFIItem item : db.getAllItems()) {
      wPFIItemset candidate = new wPFIItemset();
      candidate.addItem(item);

      double candidate_weight = weightTable.get(item.getId());
      double candidate_confidence = frequentnessProbability(minsup, db.size(), candidate);

      // System.out.print(itemset.toString());
      // System.out.printf("\t%2f\t", candidate_weight);
      // System.out.println(candidate_confidence);

      if (candidate_confidence * candidate_weight < t)
        continue;

      // if (verifyCandidate(new_candidates, candidate))
      new_candidates.add(candidate);
    }

    return new_candidates;
  }

  /**
   * Scan and find the size-k probabilistic frequent itemsets.
   *
   * @param wPFI_k list of the size-k candidate itemsets
   * @return a list of the itemset that satisfied the condition
   */
  protected List<wPFIItemset> scanFindSizeK(List<wPFIItemset> wPFI_k) {
    List<wPFIItemset> new_candidates = new ArrayList<wPFIItemset>();

    for (wPFIItemset candidate : wPFI_k) {
      double candidate_weight = itemsetWeight(candidate);
      double candidate_confidence = frequentnessProbability(minsup, db.size(), candidate);

      // System.out.print(candidate.toString());
      // System.out.printf("\t%2f\t", candidate_weight);
      // System.out.println(candidate_confidence);

      if (candidate_confidence * candidate_weight >= t)
        new_candidates.add(candidate);
    }

    return new_candidates;
  }

  /**
   * TODO: decide whether this method is necessary.
   */
  private boolean verifyCandidate(List<wPFIItemset> candidates, wPFIItemset candidate) {
    for (wPFIItemset validator : candidates) {
      if (validator.isEqualTo(candidate))
        return false;
    }
    return true;
  }

  /**
   * Calculate the probability the the given itemset is exist in the j-th
   * transaction.
   *
   * @param j       the index of the transaction
   * @param itemset the itemset used to calculate the probability
   * @return a double
   */
  private double probItemsetInTransaction(int j, wPFIItemset itemset) {
    wPFIItemset transactionJ = db.getTransactions().get(j - 1);

    /**
     * The itemset will not exist in the transaction if its size is larger than the
     * transaction's.
     */
    if (itemset.size() > transactionJ.size())
      return 0;

    double prob = 1;

    /**
     * The probability would be return as 0 if an item in the itemset is not exist
     * in the transaction.
     */
    for (wPFIItem item : itemset.getItems()) {
      boolean found = false;

      for (wPFIItem itemTX : transactionJ.getItems()) {
        if (itemTX.equals(item)) {
          found = true;
          prob *= itemTX.getProbability();
          break;
        }
      }

      if (!found)
        return 0;
    }
    return prob;
  }

  /**
   * Calculate the probability the given itemset exists in the first j
   * transactions at least i-times. Or P(sup(itemset) >= i) in the first j
   * transactions
   *
   * @param i       the minimum support of the itemset in the first j
   *                transactions
   * @param j       the number of transactions used for calculating
   * @param itemset the itemset used for calculating support
   * @return a double
   */
  protected double frequentnessProbability(int i, int j, wPFIItemset itemset) {
    if (i == 0)
      return 1;

    if (j == 0)
      return 0;

    double prob = probItemsetInTransaction(j, itemset);

    double firstProb = frequentnessProbability(i - 1, j - 1, itemset) * prob;
    double secondProb = frequentnessProbability(i, j - 1, itemset) * (1 - prob);

    return firstProb + secondProb;
  }

  /**
   * Calculate the means of item's weight in the given itemset.
   *
   * @param itemset the itemset
   * @return a double
   */
  private double itemsetWeight(wPFIItemset itemset) {
    double sumWeight = 0;

    for (wPFIItem item : itemset.getItems()) {
      sumWeight += weightTable.get(item.getId());
    }

    return sumWeight /= itemset.size();
  }

  /**
   * Find the minimum weight of the items in the given itemset.
   *
   * @param itemset
   * @return a double
   */
  private double minWeightItemset(wPFIItemset itemset) {
    double minWeight = 1.1;
    double w;

    // wPFIItem argmin = new wPFIItem(-1, 0);

    for (wPFIItem item : itemset.getItems()) {
      w = weightTable.get(item.getId());
      if (w < minWeight) {
        minWeight = w;
        // argmin = item;
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
  protected List<wPFIItemset> wPFIAprioriGenerate(List<wPFIItemset> wPFI_K_1) {
    List<wPFIItemset> candidateK = new ArrayList<wPFIItemset>();
    Set<wPFIItem> I_ = new HashSet<wPFIItem>();

    for (wPFIItemset candidate : wPFI_K_1) {
      I_.addAll(candidate.getItems());
    }

    // Set<wPFIItem> differentSet = new HashSet<wPFIItem>();
    // wPFIItemset tempCandidate;
    // wPFIItem minI;
    double argmin;

    for (wPFIItemset candidate : wPFI_K_1) {
      // System.out.println(I_.size());
      // differentSet.addAll(I_);
      Set<wPFIItem> differentSet = new HashSet<wPFIItem>(I_);
      differentSet.removeAll(candidate.getItems());

      // System.out.print(candidate.size() + "\t");
      // System.out.println(differentSet.size());

      for (wPFIItem item : differentSet) {
        wPFIItemset tempCandidate = candidate;
        tempCandidate.addItem(item);

        if (itemsetWeight(tempCandidate) < t)
          continue;

        // if (verifyCandidate(candidateK, tempCandidate))
        // continue;

        candidateK.add(tempCandidate);
      }

      // argmin = minWeightItemset(candidate);
      //
      // differentSet = allItems;
      // differentSet.removeAll(I_);
      // // differentSet.removeAll(candidate.getItems());
      //
      // for (wPFIItem item : differentSet) {
      // tempCandidate = candidate;
      // tempCandidate.addItem(item);
      //
      // if (itemsetWeight(tempCandidate) < t)
      // continue;
      // if (weightTable.get(item.getId()) >= argmin)
      // continue;
      // if (verifyCandidate(candidateK, tempCandidate))
      // continue;
      //
      // candidateK.add(tempCandidate);
      // }
    }

    return candidateK;
  }

  public static void main(String[] args) throws IOException {
    UncertainDatabase database = new UncertainDatabase();
    database.loadFile("./../../data/connect.dat");
    wPFIApriori test = new wPFIApriori(database);
    test.runAlgorithm(0.2, 0.6, 0.6);
  }
}
