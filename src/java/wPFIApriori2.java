import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Random;

public class wPFIApriori2 {
  protected UncertainDatabase db;
  protected HashMap<Integer, Double> weightTable;

  protected int k;
  protected int candidateCount = 0;
  protected int dbScanCount = 0;

  protected long startTime;
  protected long endTime;

  private int itemsetCount;

  public wPFIApriori2(UncertainDatabase database) {
    this.db = database;
  }

  public void runAlgorithm(float msup, float threshold) {
    this.startTime = System.currentTimeMillis();
    this.candidateCount = 0;
    this.itemsetCount = 0;
    this.dbScanCount = 0;

    this.k = 1;
  }

  private static double gaussianDistribution() {
    Random random = new Random();
    double prob = random.nextGaussian() * Math.sqrt(0.125) + 0.5;

    prob = (Double) Math.ceil(prob * 10) / 10.0;
    return prob;
  }

  private HashMap<Integer, Double> generateWeightTable() {
    this.weightTable = new HashMap<Integer, Double>();

    for (wPFIItem item : this.db.getAllItems()) {
      int itemId = item.getId();
      weightTable.getOrDefault(itemId, gaussianDistribution());

      // weightTable.put(itemId, weightTable.getOrDefault(itemId, 0) + 1);
    }

    return weightTable;
  }

  protected Set<wPFIItemset> scanFindSize1() {
    Set<wPFIItemset> candidates = new HashSet<wPFIItemset>();

    for (wPFIItem item : db.getAllItems()) {
      wPFIItemset itemset = new wPFIItemset();
      itemset.addItem(item);
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
            }

          }

          if (!found)
            continue candidateLoop;
        }

        candidate.increaseSupportBy(expectedSupport);
      }
    }
  }

}
