import java.io.IOException;
import java.util.Collections;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Random;

public class wPFIApriori2 {
  protected UncertainDatabase db;
  protected Set<wPFIItem> allItems;
  protected HashMap<Integer, Double> weightTable;

  protected int k;
  protected int minsup;
  protected int dbScanCount = 0;
  protected int candidateCount = 0;

  protected double t;

  protected long startTime;
  protected long endTime;

  private int itemsetCount;

  public wPFIApriori2(UncertainDatabase database) {
    this.db = database;
    this.allItems = database.getAllItems();
  }

  public void runAlgorithm(double msup_ratio, double threshold, double scale_factor) {
    this.startTime = System.currentTimeMillis();
    this.candidateCount = 0;
    this.itemsetCount = 0;
    this.dbScanCount = 0;

    this.t = threshold;
    this.minsup = (int) msup_ratio * db.size();

    this.k = 1;

    List<List<wPFIItemset>> wPFI = new ArrayList<List<wPFIItemset>>();
    Set<wPFIItem> I = db.getAllItems();

    this.weightTable = generateWeightTable(db);

    List<wPFIItemset> wPFI_1 = scanFindSize1();
    wPFI.add(wPFI_1);

    // while (true) {
    // List<wPFIItemset> candidateK;
    // }
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
    List<wPFIItemset> candidates = new ArrayList<wPFIItemset>();

    for (wPFIItem item : db.getAllItems()) {
      wPFIItemset itemset = new wPFIItemset();
      itemset.addItem(item);

      double item_weight = this.weightTable.get(item.getId());
      if (item_weight * frequentnessProbability(2, db.size() - 1, itemset) >= this.t)

        candidates.add(itemset);
    }

    return candidates;
  }

  // protected void calCandidateSupport(Set<wPFIItemset> candidateK) {
  // this.dbScanCount += 1;
  //
  // for (wPFIItemset transaction : this.db.getTransactions()) {
  // candidateLoop: for (wPFIItemset candidate : candidateK) {
  // double expectedSupport = 0;
  //
  // for (wPFIItem item : candidate.getItems()) {
  // boolean found = false;
  //
  // for (wPFIItem itemTX : transaction.getItems()) {
  // if (itemTX.getId() < item.getId())
  // break;
  //
  // if (itemTX.getId() == item.getId()) {
  // found = true;
  //
  // if (expectedSupport == 0)
  // expectedSupport = itemTX.getProbability();
  // else
  // expectedSupport *= itemTX.getProbability();
  //
  // break;
  // }
  // }
  //
  // if (!found)
  // continue candidateLoop;
  // }
  //
  // candidate.increaseSupportBy(expectedSupport);
  // }
  // }
  // }

  private double probItemsetInTransaction(wPFIItemset itemset, int j) {
    wPFIItemset transactionJ = this.db.getTransactions().get(j);

    double prob = 0;
    for (wPFIItem item : itemset.getItems()) {
      for (wPFIItem itemTX : transactionJ.getItems()) {
        if (itemTX.getId() > item.getId())
          break;

        if (itemTX.getId() == item.getId()) {
          if (prob == 0)
            prob = itemTX.getProbability();
          else
            prob *= itemTX.getProbability();
          break;
        }
      }
    }

    return prob;
  }

  protected double frequentnessProbability(int i, int j, wPFIItemset itemset) {
    if (i > j)
      return 0;

    if (i == 0)
      return 1;

    double prob = probItemsetInTransaction(itemset, j);
    return frequentnessProbability(i - 1, j - 1, itemset) * prob
        + frequentnessProbability(i, j - 1, itemset) * (1 - prob);
  }

  private double itemsetWeight(wPFIItemset itemset, wPFIItem iItem) {
    double meansWeight = iItem.getProbability();

    for (wPFIItem item : itemset.getItems()) {
      meansWeight += this.weightTable.get(item.getId());
    }

    meansWeight /= itemset.size() + 1.0;

    return meansWeight;
  }

  private wPFIItem minWeightItemset(wPFIItemset itemset) {
    double minWeight = 1.1;
    double w;

    wPFIItem argmin = null;

    for (wPFIItem item : itemset.getItems()) {
      w = this.weightTable.get(item.getId());
      if (w < minWeight) {
        minWeight = w;
        argmin = item;
      }
    }

    return argmin;
  }

  protected List<wPFIItemset> wPFIAprioriGenerate(List<wPFIItemset> candidateK_1) {
    List<wPFIItemset> candidateK = new ArrayList<wPFIItemset>();

    Set<wPFIItem> I_ = new HashSet<wPFIItem>();

    for (wPFIItemset candidate : candidateK_1) {
      I_.addAll(candidate.getItems());
    }

    Set<wPFIItem> differentSet;
    wPFIItemset tempCandidate;
    wPFIItem minI;

    for (wPFIItemset candidate : candidateK_1) {
      differentSet = new HashSet<>(I_);
      differentSet.removeAll(candidate.getItems());

      for (wPFIItem item : differentSet) {
        if (itemsetWeight(candidate, item) >= this.t) {
          tempCandidate = candidate;
          tempCandidate.addItem(item);
          candidateK.add(tempCandidate);
        }
      }

      minI = minWeightItemset(candidate);
    }

    if (candidateK.size() == 0)
      return null;
    else
      return candidateK;
  }

  public static void main(String[] args) throws IOException {
    UncertainDatabase database = new UncertainDatabase();
    database.loadFile("../../data/chess.dat");
    wPFIApriori2 test = new wPFIApriori2(database);
    test.runAlgorithm(0.2, 0.1, 0.6);
  }
}
