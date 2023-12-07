import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.List;
import java.util.Set;

public class wPFIApriori2 {
  protected UncertainDatabase db;
  protected Set<wPFIItem> allItems;
  protected HashMap<Integer, Double> weightTable;

  protected int k;
  protected int minsup;
  protected int dbScanCount = 0;
  protected int lastTransactionIndex;

  protected double t;

  protected long startTime;
  protected long endTime;

  public wPFIApriori2(UncertainDatabase database) {
    db = database;
    allItems = database.getAllItems();
    lastTransactionIndex = db.size() - 1;
  }

  public void runAlgorithm(double msup_ratio, double threshold, double scale_factor) {
    startTime = System.currentTimeMillis();
    dbScanCount = 0;
    k = 1;

    t = threshold;
    minsup = (int) msup_ratio * db.size();

    List<List<wPFIItemset>> wPFI = new ArrayList<List<wPFIItemset>>();

    // TODO: create a method to define whether
    // generate a weightable is necessary or not
    weightTable = generateWeightTable(db);

    List<wPFIItemset> wPFI_k = scanFindSize1();

    wPFI.add(wPFI_k);

    while (wPFI_k.size() != 0) {
      System.out.printf("There are %d candidates size %d", wPFI_k.size(), k);
      System.out.println();

      List<wPFIItemset> candidateK = wPFIAprioriGenerate(wPFI.get(k - 1));
      wPFI_k = scanFindSizeK(candidateK);
      wPFI.add(wPFI_k);
      k++;
    }

    // System.out.println(wPFI.get(k - 2).get(0).toString());
  }

  private static double gaussianDistribution() {
    Random random = new Random();
    double prob = random.nextGaussian() * Math.sqrt(0.125) + 0.5;

    prob = (Double) Math.ceil(prob * 10) / 10.0;
    return prob;
  }

  private HashMap<Integer, Double> generateWeightTable(UncertainDatabase db) {
    HashMap<Integer, Double> weightTable = new HashMap<Integer, Double>();
    Random random = new Random();

    for (wPFIItem item : db.getAllItems()) {
      int itemId = item.getId();
      weightTable.put(itemId, random.nextDouble());
    }

    return weightTable;
  }

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

      if (verifyCandidate(new_candidates, candidate))
        new_candidates.add(candidate);
    }

    return new_candidates;
  }

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

  private boolean verifyCandidate(List<wPFIItemset> candidates, wPFIItemset candidate) {
    for (wPFIItemset validator : candidates) {
      if (validator.isEqualTo(candidate))
        return false;
    }
    return true;
  }

  private double probItemsetInTransaction(int j, wPFIItemset itemset) {
    wPFIItemset transactionJ = db.getTransactions().get(j - 1);

    if (itemset.size() > transactionJ.size())
      return 0;

    double prob = 1;

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

  private double itemsetWeight(wPFIItemset itemset) {
    double sumWeight = 0;

    for (wPFIItem item : itemset.getItems()) {
      sumWeight += weightTable.get(item.getId());
    }

    return sumWeight /= itemset.size();
  }

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

  // Algorithm 2
  // TODO: implement algorithm 2
  protected List<wPFIItemset> wPFIAprioriGenerate(List<wPFIItemset> wPFI_K_1) {
    List<wPFIItemset> candidateK = new ArrayList<wPFIItemset>();
    Set<wPFIItem> I_ = new HashSet<wPFIItem>();

    for (wPFIItemset candidate : wPFI_K_1) {
      I_.addAll(candidate.getItems());
    }

    Set<wPFIItem> differentSet = I_;
    // wPFIItemset tempCandidate;
    // wPFIItem minI;
    double argmin;

    for (wPFIItemset candidate : wPFI_K_1) {
      System.out.println(I_.size());
      differentSet.addAll(I_);
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
    database.loadFile("../../data/connect.dat");
    wPFIApriori2 test = new wPFIApriori2(database);
    test.runAlgorithm(0.2, 0.6, 0.6);
  }
}
