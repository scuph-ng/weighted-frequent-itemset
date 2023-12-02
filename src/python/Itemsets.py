from Itemset import Itemset


class Itemsets:
    def __init__(self, name: str) -> None:
        self.name = name
        self.__levels = [[Itemset()]]
        self.__itemsetsCount = 0

    def addItemset(self, itemset: Itemset, k: int) -> None:
        while len(self.__levels) <= k:
            self.__levels.append([Itemset()])

        self.__levels[k].append(itemset)
        self.__itemsetsCount += 1

    def getLevels(self) -> list[list[Itemset]]:
        return self.__levels

    def getItemsetsCount(self) -> int:
        return self.__itemsetsCount
