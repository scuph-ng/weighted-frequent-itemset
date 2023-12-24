import java.io.IOException;
import java.util.Collections;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

/**
 * This is the implementation of the paper
 * "Efficient weighted probabilistic frequent itemset mining in uncertain
 * databases"
 *
 * @author Nguyen Hoang Phuc (scuph-ng)
 *
 * @see UncertainDatabase
 * @see wPFIItem
 */
public class wPFIApriori implements wPFIAprioriInterface{
  /**
   * Define the neccesary variables for the algorithm
   */
  protected UncertainDatabase db;
  protected HashSet<wPFIItem> allItems;
  protected HashMap<Integer, Double> weightTable;
  protected HashMap<HashSet<wPFIItem>, Double> supportDict = new HashMap<>();

  protected int k;
  protected int minsup;
  protected int databaseSize;

  protected float t;
  protected float alpha;
  protected double transactionSize;

  private long startTime;
  private long endTime;

  /**
   * Constructor
   *
   * @param database an UncertainDatabase object representing the loaded database.
   */
  public wPFIApriori(UncertainDatabase database) {
    db = database;

    /**
     * Assign the properties of the database.
     */
    databaseSize = database.size();
    allItems = new HashSet<>(database.getAllItems());
    transactionSize = database.getTransactionSize();

    /**
     * TODO: create a method to define whether
     * generate a weight table is necessary or not.
     */
    weightTable = generateWeightTable();
  }

  /**
   * The implementation of Algorithm 1 from the research paper.
   *
   * @param msup_ratio   a float representing the minimum support ratio.
   * @param threshold    a float representing the minimum confidence threshold.
   * @param scale_factor a float representing the scaling factor for the
   *                     probability model.
   */
  @Override
  public void runAlgorithm(float msup_ratio, float threshold, float scale_factor, boolean useProbabilityModel) {
    this.startTime = System.currentTimeMillis();

    this.k = 1;
    this.t = threshold;
    this.minsup = (int) Math.round(msup_ratio * databaseSize);
    this.alpha = scale_factor;

    System.out.println("===========================================================");
    System.out.println("Minimum support ratio: " + msup_ratio);
    System.out.println("Confidence threshold: " + threshold);

    ArrayList<HashSet<HashSet<wPFIItem>>> wPFI = new ArrayList<>();

    System.out.println("===========================================================");

    HashSet<HashSet<wPFIItem>> wPFI_k = scanFindSize1();
    wPFI.add(wPFI_k);

    while (wPFI_k.size() != 0) {
      HashSet<HashSet<wPFIItem>> candidateK = wPFIAprioriGenerate(wPFI_k, useProbabilityModel);

      System.out.printf("There are %d\t size-%d candidates.\n", candidateK.size(), k);

      wPFI_k = scanFindSizeK(candidateK);
      wPFI.add(wPFI_k);
      k++;
    }

    endTime = System.currentTimeMillis();
    System.out.println("===========================================================");
    System.out.printf("Total runtime: %ds", (int) (endTime - startTime) / 1000);
  }

  /**
   * Generate a weight table that assigns a random weight between 0 and 1 to each
   * item.
   *
   * @return a HashMap of integer keys and double values representing the weight
   *         of each item.
   */
  @Override
  public HashMap<Integer, Double> generateWeightTable() {
    HashMap<Integer, Double> weightTable = new HashMap<Integer, Double>();
    Random random = new Random();

    for (wPFIItem item : allItems) {
      weightTable.put(item.getId(), random.nextDouble());
    }

    return weightTable;
  }

  /**
   * Calculate the average weight of items within a given itemset.
   *
   * @param itemset a HashSet of wPFIItem objects representing an itemset.
   *
   * @return a double value representing the average weight of the items in the
   *         itemset.
   */
  @Override
  public double itemsetWeight(HashSet<wPFIItem> itemset) {
    double sumWeight = 0;

    for (wPFIItem item : itemset) {
      sumWeight += weightTable.get(item.getId());
    }

    return sumWeight / itemset.size();
  }

  /**
   * Finds PFIs of size 1
   *
   * @return a HashSet of HashSet of wPFIItem objects representing FPIs of size 1.
   */
  @Override
  public HashSet<HashSet<wPFIItem>> scanFindSize1() {
    HashSet<HashSet<wPFIItem>> new_candidates = new HashSet<HashSet<wPFIItem>>();

    HashSet<wPFIItem> candidate = new HashSet<wPFIItem>();
    for (wPFIItem item : allItems) {
      candidate.add(item);

      double candidate_weight = weightTable.get(item.getId());
      double candidate_confidence = Pr(candidate);

      if (candidate_confidence * candidate_weight >= t)
        new_candidates.add(candidate);

      candidate.clear();
    }

    return new_candidates;
  }

  /**
   * Identify PFIs of size k from a set of candidate PFI.
   *
   * @param wPFI_k a HashSet of HashSet of wPFIItem objects representing candidate
   *               PFIs of size k.
   *
   * @return a HashSet of HashSet of wPFIItem objects representing FPIs of size k.
   */
  @Override
  public HashSet<HashSet<wPFIItem>> scanFindSizeK(HashSet<HashSet<wPFIItem>> wPFI_k) {
    HashSet<HashSet<wPFIItem>> new_candidates = new HashSet<HashSet<wPFIItem>>();

    for (HashSet<wPFIItem> candidate : wPFI_k) {
      double candidate_weight = itemsetWeight(candidate);
      double candidate_confidence = Pr(candidate);

      if (candidate_confidence * candidate_weight >= t)
        new_candidates.add(candidate);
    }

    return new_candidates;
  }

  /**
   * Calculate the support of a given itemset within a specific transaction.
   *
   * @param j       an integer value representing the index of the transaction to
   *                be analyzed.
   * @param itemset a HashSet of wPFIItem objects representing the itemset for
   *                which suport is calculated.
   *
   * @return a double value representing the probability of the given itemset
   *         occurring in the specified transaction.
   */
  @Override
  public double itemsetSupportInTransaction(int j, HashSet<wPFIItem> itemset) {
    HashSet<wPFIItem> transaction = db.getTransactions().get(j);

    if (itemset.size() > transaction.size())
      return 0;

    double probability = 1;

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
   * Calculate the probability of a given itemset occurring in a transaction.
   *
   * @param itemset a HashSet of wPFIItem objects representing an itemset.
   * 
   * @return a double value representing the probability of the given itemset
   *         occurring in a transaction.
   */
  @Override
  public double Pr(HashSet<wPFIItem> itemset) {
    double[][] P = new double[minsup + 1][databaseSize + 1];
    double mu_itemset = 0;

    double[] probabilities = new double[databaseSize];

    for (int i = 0; i < databaseSize; i++) {
      probabilities[i] = itemsetSupportInTransaction(i, itemset);
      mu_itemset += probabilities[i];
    }

    supportDict.put(itemset, mu_itemset);

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
    return P[minsup][databaseSize];
  }

  /**
   * Find the minimum weight of the items within the given itemset.
   *
   * @param itemset a HashSet of wPFIItem objects representing an itemset.
   *
   * @return a double value representing the minimum weight of any item in the
   *         given itemset.
   */
  @Override
  public double minWeightItemset(HashSet<wPFIItem> itemset) {
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
   * The implementation of Algorithm 2 in the research paper.
   * Generate candidate PFI of size k from PFI of size k-1.
   *
   * @param wPFI_K_1            a HashSet of HashSet of wPFIItem objects
   *                            representing the
   *                            frequent itemsets of size k-1.
   * @param useProbabilityModel a boolean flag indicating whether to use the
   *                            probability model for filtering candiate itemset.
   *
   * @return a HashSet of HashSet of wPFIItem objects representing candidate
   *         PFI of size k.
   */
  @Override
  public HashSet<HashSet<wPFIItem>> wPFIAprioriGenerate(HashSet<HashSet<wPFIItem>> wPFI_K_1,
      boolean useProbabilityModel) {
    HashSet<HashSet<wPFIItem>> candidateK = new HashSet<HashSet<wPFIItem>>();

    HashSet<wPFIItem> I_ = new HashSet<wPFIItem>();
    HashSet<wPFIItem> differentSet = new HashSet<>();
    HashSet<wPFIItem> tempCandidate = new HashSet<>();

    for (HashSet<wPFIItem> candidate : wPFI_K_1) {
      I_.addAll(candidate);
    }

    double maxWeight = Collections.max(weightTable.values());
    double mu_ = calculateMu_(maxWeight, 0, databaseSize);

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

        if (useProbabilityModel) {
          if (!conditionAlgorithm3(candidate, item, mu_)) {
            tempCandidate.clear();
            continue;
          }
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
        if (useProbabilityModel) {
          if (!conditionAlgorithm3(candidate, item, mu_)) {
            tempCandidate.clear();
            continue;
          }
        }

        candidateK.add(new HashSet<>(tempCandidate));
        tempCandidate.clear();
      }

      differentSet.clear();
    }

    return candidateK;
  }

  /**
   * Calculate the factorial of a given non-negative integer n.
   *
   * @param n an integer representing the non-negative number for which the
   *          factorial is to be calculated.
   * @return a double value representing the factorial of n.
   */
  @Override
  public double factorial(int n) {
    if (n == 0 || n == 1)
      return 1.0;

    double result = 1;
    for (int i = 2; i <= n; i++)
      result *= i;

    return result;
  }

  /**
   * Calculate the CDF of Poisson Distribution at a given k value.
   *
   * @param k      an integer representing the number of occurences.
   * @param lambda a double value representing the average rate of occurences.
   *
   * @return a double value representing the CDF at step k.
   */
  @Override
  public double CDF(int k, double lambda) {
    double result = 0;
    for (int i = 0; i <= k; i++) {
      result += Math.pow(lambda, i) / factorial(i);
    }

    result *= Math.pow(Math.E, -lambda);
    return result;
  }

  /**
   * This method approximates the mu_ threshold using a binary search algorithm.
   *
   * @param maxWeight a double value representing the maximum weight in the weight
   *                  table.
   * @param lower     an integer representing the lower bound for the binary
   *                  search.
   * @param upper     an integer representing the upper bound for the binary
   *                  search.
   * @return a double value representing the mu_ threshold.
   */
  @Override
  public double calculateMu_(double maxWeight, int lower, int upper) {
    double epsilon = 0.000001;
    double lowerDouble = (double) lower;
    double upperDouble = (double) upper;

    while (upperDouble - lowerDouble > epsilon) {
      double value = 1 - CDF(minsup - 1, (upperDouble + lowerDouble) / 2.0) - t / maxWeight;

      if (value > 0)
        upperDouble -= (upperDouble + lowerDouble) / 2.0;
      else if (value < 0)
        lowerDouble += (upperDouble + lowerDouble) / 2.0;
      else
        break;
    }

    return (upperDouble + lowerDouble) / 2.0;
  }

  /**
   * The implementation of additional conditions from Algorithm 3 in the research
   * paper. These condition will be added in the algorithm 2 to early prune the
   * candidates.
   *
   * @param itemset a HashSet of wPFIItem objects.
   * @param item    a wPFIItem object.
   * @param mu_     a double value representing the minimum support threshold.
   *
   * @return a boolean flag indicating whether the given itemset and item satisfy
   *         the conditions of the algorithm.
   */
  @Override
  public boolean conditionAlgorithm3(HashSet<wPFIItem> itemset, wPFIItem item, double mu_) {
    if (itemset == null || item == null)
      return false;

    HashSet<wPFIItem> itemWrapper = new HashSet<>();
    itemWrapper.add(item);

    if (supportDict.get(itemset) == null)
      Pr(itemset);
    if (supportDict.get(itemWrapper) == null)
      Pr(itemWrapper);

    double mu_X = supportDict.get(itemset);
    double mu_I = supportDict.get(itemWrapper);

    if (mu_X < mu_ || mu_I < mu_)
      return false;

    if (mu_X * mu_I < alpha * transactionSize * mu_)
      return false;

    return true;
  }

  public static void main(String[] args) throws IOException {
    String pathWrapper = "./../../data/" + args[0] + ".dat";

    UncertainDatabase database = new UncertainDatabase();
    database.loadFile(pathWrapper, false);

    float msup_ratio = Float.parseFloat(args[1]);
    float threshold = Float.parseFloat(args[2]);
    float scale_factor = Float.parseFloat(args[3]);
    boolean useProbabilityModel = Boolean.parseBoolean(args[4]);

    wPFIApriori apriori = new wPFIApriori(database);
    apriori.runAlgorithm(msup_ratio, threshold, scale_factor, useProbabilityModel);
  }
}
