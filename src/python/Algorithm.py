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
        self.__T = []
        self.__I = {}

        self._load_data(filename)
        pass

    def _load_data(self, filename: str) -> None:
        with open(filename, "r") as file:
            for line in file:
                transaction = Itemset()

                for item in line.split():
                    new_item = Item(int(item), ceil(gauss(0.5, 0.125) * 10) / 10)
                    transaction.addItem(new_item)

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


test = wPFIApriori("data/chess_test.dat")
test.printDataset()

