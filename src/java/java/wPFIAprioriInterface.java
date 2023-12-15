import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Interface for weighted probabilistic frequent itemset mining in uncertain databases.
 */
public interface wPFIAprioriInterface {
    /**
     * The implementation of Algorithm 1 from the research paper.
     *
     * @param msup_ratio   a float representing the minimum support ratio.
     * @param threshold    a float representing the minimum confidence threshold.
     * @param scale_factor a float representing the scaling factor for the
     *                     probability model.
     */
    void runAlgorithm(float msup_ratio, float threshold, float scale_factor, boolean useProbabilityModel);

    /**
     * Generate a weight table that assigns a random weight between 0 and 1 to each
     * item.
     *
     * @return a Map of integer keys and double values representing the weight
     *         of each item.
     */
    Map<Integer, Double> generateWeightTable();

    /**
     * Calculate the average weight of items within a given itemset.
     *
     * @param itemset a HashSet of wPFIItem objects representing an itemset.
     *
     * @return a double value representing the average weight of the items in the
     *         itemset.
     */
    double itemsetWeight(HashSet<wPFIItem> itemset);

    /**
     * Find PFIs of size 1.
     *
     * @return a Set of HashSet of wPFIItem objects representing FPIs of size 1.
     */
    Set<HashSet<wPFIItem>> scanFindSize1();

    /**
     * Identify PFIs of size k from a set of candidate PFIs.
     *
     * @param wPFI_k a HashSet of HashSet of wPFIItem objects representing candidate PFIs of size k.
     * @return a Set of HashSet of wPFIItem objects representing FPIs of size k.
     */
    Set<HashSet<wPFIItem>> scanFindSizeK(HashSet<HashSet<wPFIItem>> wPFI_k);

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
    double itemsetSupportInTransaction(int j, HashSet<wPFIItem> itemset);

    /**
     * Calculate the probability of a given itemset occurring in a transaction.
     *
     * @param itemset a HashSet of wPFIItem objects representing an itemset.
     * 
     * @return a double value representing the probability of the given itemset
     *         occurring in a transaction.
     */
    double Pr(HashSet<wPFIItem> itemset);

    /**
     * Find the minimum weight of the items within the given itemset.
     *
     * @param itemset a HashSet of wPFIItem objects representing an itemset.
     *
     * @return a double value representing the minimum weight of any item in the
     *         given itemset.
     */
    double minWeightItemset(HashSet<wPFIItem> itemset);

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
     * @return a Set of HashSet of wPFIItem objects representing candidate
     *         PFI of size k.
     */
    Set<HashSet<wPFIItem>> wPFIAprioriGenerate(HashSet<HashSet<wPFIItem>> wPFI_K_1, boolean useProbabilityModel);

    /**
     * Calculate the factorial of a given non-negative integer n.
     *
     * @param n an integer representing the non-negative number for which the
     *          factorial is to be calculated.
     * @return a double value representing the factorial of n.
     */
    double factorial(int n);

    /**
     * Calculate the CDF of Poisson Distribution at a given k value.
     *
     * @param k      an integer representing the number of occurences.
     * @param lambda a double value representing the average rate of occurences.
     *
     * @return a double value representing the CDF at step k.
     */
    double CDF(int k, double lambda);

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
    double calculateMu_(double maxWeight, int lower, int upper);

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
    boolean conditionAlgorithm3(HashSet<wPFIItem> itemset, wPFIItem item, double mu_);
}
