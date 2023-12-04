import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Random;

public class wPFIApriori2 {
  protected UncertainDatabase db;
  protected HashSet<wPFIItem> allItems;
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
  }

  public void runAlgorithm(double msup_ratio, double threshold, double scale_factor) {
    this.startTime = System.currentTimeMillis();
    this.candidateCount = 0;
    this.itemsetCount = 0;
    this.dbScanCount = 0;

    this.t = threshold;
    this.minsup = (int) msup_ratio * db.size();

    this.k = 1;

    List<wPFIItemset> wPFI = new ArrayList<wPFIItemset>();
    Set<wPFIItem> I = db.getAllItems();

    this.weightTable = generateWeightTable(db);

    List<wPFIItemset> wPFI_1 = scanFindSize1();

  }

  private static double gaussianDistribution() {
    Random random = new Random();
    double prob = random.nextGaussian() * Math.sqrt(0.125) + 0.5;

    prob = (Double) Math.ceil(prob * 10) / 10.0;
    return prob;
  }

  private HashMap<Integer, Double> generateWeightTable(UncertainDatabase db) {
    HashMap<Integer, Double> weightTable = new HashMap<Integer, Double>();

    for (wPFIItem item : db.getAllItems()) {
      int itemId = item.getId();
      weightTable.put(itemId, gaussianDistribution());
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

  protected void calCandidateSupport(Set<wPFIItemset> candidateK) {
    this.dbScanCount += 1;

    for (wPFIItemset transaction : this.db.getTransactions()) {
      candidateLoop: for (wPFIItemset candidate : candidateK) {
        double expectedSupport = 0;

        for (wPFIItem item : candidate.getItems()) {
          boolean found = false;

          for (wPFIItem itemTX : transaction.getItems()) {
            if (itemTX.getId() < item.getId())
              break;

            if (itemTX.getId() == item.getId()) {
              found = true;

              if (expectedSupport == 0)
                expectedSupport = itemTX.getProbability();
              else
                expectedSupport *= itemTX.getProbability();

              break;
            }
          }

          if (!found)
            continue candidateLoop;
        }

        candidate.increaseSupportBy(expectedSupport);
      }
    }
  }

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

  public static void main(String[] args) throws IOException {
    UncertainDatabase database = new UncertainDatabase();
    database.loadFile("../../data/chess.dat");
    wPFIApriori2 test = new wPFIApriori2(database);
    test.runAlgorithm(0.2, 0.1, 0.6);
  }
}
