from __future__ import annotations
from Item import Item


class Itemset:
    def __init__(self) -> None:
        self.__items = []
        self.__expected_support = 0

    def len(self) -> int:
        return len(self.__items)

    def get(self, index: int) -> Item:
        return self.__items[index]

    def getItems(self) -> list[Item]:
        return self.__items

    def getExpectedSupport(self) -> float:
        return self.__expected_support

    def setExpectedSupport(self, expected_supp: float) -> None:
        self.__expected_support = expected_supp

    def addItem(self, item: Item) -> None:
        self.__items.append(item)

    def addSupport(self, supp: float) -> None:
        self.__expected_support += supp

    def contains(self, item: Item) -> bool:
        if item in self.__items:
            return True

        return False

    def isLexicallySmaller(self, itemset: Itemset) -> bool:
        for i in range(itemset.len()):
            if self.get(i).getId() > itemset.get(i).getId():
                return False

        return True

    def isEqualTo(self, itemset: Itemset) -> bool:
        if self.len() != itemset.len():
            return False

        for item in self.__items:
            if not itemset.contains(item):
                return False

        return True

    def cloneItemsetExclude(self, ex_item: Item) -> Itemset:
        newItemset = Itemset()

        for item in self.__items:
            if not item.equals(ex_item.getId()):
                newItemset.addItem(item)

        return newItemset

    def compareExceptLastItem(self, itemset: Itemset):
        if itemset.len() != self.len():
            return None

        itemsetLen = itemset.len()

        for i in range(itemsetLen):
            if i == itemsetLen - 1:
                if self.get(i).getId() >= itemset.get(i).getId():
                    return None

            elif self.get(i).getId() != itemset.get(i).getId():
                return None

        return itemset.get(itemsetLen - 1)

