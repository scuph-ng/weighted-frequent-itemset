from math import ceil
from random import gauss
from Item import Item
from Itemset import Itemset


class wPFIApriori:
    # __T__ = []
    # __I__ = {}
    #
    # __k__ = 0
    # __dbScan__ = 0
    # __itemsets__ = 0
    # __candidates__ = 0

    def __init__(self, filename: str) -> None:
        self.__T = list()
        self.__I = set()

        self._load_data(filename)
        pass

    def _load_data(self, filename: str) -> None:
        with open(filename, "r") as file:
            for line in file:
                transaction = Itemset()

                for item in line.split():
                    new_item = Item(int(item), ceil(gauss(0.5, 0.125) * 10) / 10)
                    transaction.addItem(new_item)
                    self.__I.add(new_item)

                self.__T.append(transaction)

        self.__n = len(self.__T)
        file.close()

    def printDataset(self) -> None:
        print("===== UNCERTAIN DATABASE =====")
        count = 0

        for transaction in self.__T:
            print(count, ":\t", end=" ")
            for item in transaction.getItems():
                print(item.getId(), end=" ")
            count += 1
            print()

    def _calculateSupportEachCandidate(self, candidatesK: set[Itemset]) -> None:
        for transaction in self.__T:
            candidateFound = True

            for candidate in candidatesK:
                expectedSupport = 0

                for itemK in candidate.getItems():
                    found = False

                    for item in transaction.getItems():
                        if item.getId() == itemK.getId():
                            found = True

                            if expectedSupport == 0:
                                expectedSupport = item.getProb()
                            else:
                                expectedSupport *= item.getProb()
                            break

                        elif item.getId() < itemK.getId():
                            break

                    if not found:
                        candidateFound = False
                        break

                if candidateFound:
                    candidate.addSupport(expectedSupport)

    def _generateCandidateSize1(self) -> set[Itemset]:
        candidates = set()

        for item in self.__I:
            itemset = Itemset()
            itemset.addItem(item)
            candidates.add(itemset)

        return candidates

    def _generateCandidateSizeK(self, levelK_1: set[Itemset]) -> set[Itemset]:
        candidates = set()
        itemsets = list(levelK_1)

        for i in range(len(levelK_1)):
            itemset1 = itemsets[i]

            for j in range(len(levelK_1)):
                itemset2 = itemsets[j]

                missing = itemset1.compareExceptLastItem(itemset2)

                if missing is not None:
                    candidate = Itemset()

                    for item in itemset1.getItems():
                        candidate.addItem(item)

                    candidate.addItem(missing)

                    if self._allSubsetFPI(candidate, levelK_1):
                        candidates.add(candidate)

        return candidates

    def _allSubsetFPI(self, candidate: Itemset, levelK_1: set[Itemset]):
        if candidate.len() == 1:
            return True

        for item in candidate.getItems():
            subset = Itemset()
            found = False

            for itemset in levelK_1:
                if itemset.isEqualTo(subset):
                    found = True
                    break

            if not found:
                return False

        return True


test = wPFIApriori("data/chess_test.dat")

