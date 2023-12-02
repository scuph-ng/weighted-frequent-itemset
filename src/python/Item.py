class Item:
    def __init__(self, id: int, probability: float) -> None:
        self.__id = id
        self.__probability = probability

    def getId(self) -> int:
        return self.__id

    def getProbability(self) -> float:
        return self.__probability

    def equals(self, item_id: int) -> bool:
        if self.__id == item_id:
            return True

        return False

