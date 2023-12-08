from __future__ import annotations
from time import perf_counter, perf_counter_ns
from math import exp, sqrt
from typing import overload, Any
from random import gauss, uniform
from decimal import Decimal, getcontext


class Item:
    def __init__(self, id: int, value: float) -> None:
        self.id = id
        self.probability = value

    def isEqualTo(self, item: Item) -> bool:
        if item.id == self.id:
            return True
        return False


class FrequentItemsetAlgorithm:
    def __init__(self) -> None:
        self.n = 0
        self.t = 1
        self.msup = 1
        self.alpha = 0
        self.T = []
        self.I = set()
        self.w = dict()

    def _random_prob(self) -> float:
        prob = int(gauss(0.5, sqrt(0.125)) * 10)
        return prob / 10

    def _read_data(self, filename: str) -> None:
        """
        Input:
            filename    str
                the dataset name

        Functionality:
            read the dataset to T and obtain the size n of dataset

        Output:
            T   list[set[int]]
                list of transactions, each transaction is an itemset that contains integers
            n   int
                dataset size
        """
        with open(filename, "r") as file:
            for line in file:
                transaction = set()
                for id in line.split():
                    self.I.add(int(id))
                    transaction.add(Item(int(id), self._random_prob()))

                self.T.append(transaction)

            self.n = len(self.T)

        file.close()
        return

    def _generate_weight_table(self) -> None:
        for id in self.I:
            self.w[id] = uniform(0.1, 1)

    def _prob_X_in_transaction(self, X: set[int], j: int) -> float:
        """
        Input:
            j       int
                the index
            X       set[Item]
                the itemset
            T       list[set[Item]]
                the uncertain database

        Ouput:
            the probability the itemset X exist in the j-th transaction
        """
        transaction = self.T[j]

        if len(X) > len(transaction):
            return 0

        prob = 1

        for item in X:
            found = False
            for itemT in transaction:
                if item == itemT.id:
                    found = True
                    prob = prob * itemT.probability
                    break
            if found:
                return 0
        return prob

    def _Pr(self, X: set[int]) -> float:
        """
        Input:
            T       list[set(int)]
                list of transactions in dataset
            X       set(int)
                an itemset
            n       int
                the dataset size

        Functionality:
            calculate the existential probability of the itemset

        Output:
            result  float
                the existential probability of the itemset
        """
        P = [[0.0 for _ in range(self.n + 1)] for _ in range(self.msup + 1)]
        P[0][0] = 1.0

        for j in range(1, self.n + 1):
            P[0][j] = 1.0
            P[1][j] = P[1][j - 1] + self._prob_X_in_transaction(X, j - 1) * (
                1 - P[1][j - 1]
            )

        for i in range(2, self.msup + 1):
            for j in range(i, self.n + 1):
                prob = self._prob_X_in_transaction(X, j - 1)
                P[i][j] = P[i - 1][j - 1] * prob + P[i][j - 1] * (1 - prob)

        return P[self.msup][self.n]

    @overload
    def _itemset_weight(self, X: set[Item]) -> float:
        """
        Input:
            X       set[int]
                an itemset of integers
            w       dict{frozenset, float}
                dictionary contains candidates and their weight

        Functionality:
            calculate the mean weight of items in the itemset

        Output:
            w(X)    float
                the average weight of the itemset
        """
        weight = sum(self.w[item.id] for item in X)
        return weight / len(X)

    @overload
    def _itemset_weight(self, X: set[int]) -> float:
        weight = sum(self.w[id] for id in X)
        return weight / len(X)

    def _itemset_weight(self, X: Any = None) -> float:
        if X is None:
            raise TypeError("X cannot be None in _itemset_weight")
        elif isinstance(X, set) and all(isinstance(x, int) for x in X):
            return self._itemset_weight(X)
        elif isinstance(X, set) and all(isinstance(x, Item) for x in X):
            return self._itemset_weight(X)
        else:
            raise TypeError("Invalid argument type for _itemset_weight")

    def _scan_find_size_1(self):
        """
        Input:
            T       list[set[int]]
                list of transactions in dataset
            I       set[int]
                set of items in database
            msup    int
                the minimum support

        Functionality:
            scan the dataset and generate the size_1 candidates and their support

        Output:
            L1      list[set[int]]
                list of the size_1 candidates
            mu_1    dict{frozenset[int], float}
                dictionary of candidates and their support
        """
        L1 = []
        mu_1 = {}

        for id in self.I:
            itemset = {id}
            itemset_support = self._Pr(itemset)

            if itemset_support * self.w[id] >= self.t:
                L1.append(itemset)
                mu_1[frozenset(itemset)] = itemset_support

        return L1, mu_1

    def _scan_find_size_k(self, CK: list[set[int]]):
        """
        Input:
            CK      list[set[int]]
                list of candidates
            T       list[set[Item]]
                list of transactions
            msup    int
                the minimum support for PFI mining
            w       dict{int, float}
                the weight table
            t       float
                the probabilistic frequent threshold

        Functionality:
            scan the dataset and generate the size_k candidates and their support

        Output:
            LK      list[set[int]]
                list of size-k candidates
            mu_k    dict{frozenset[Item], float}
                dictionary contains candidates and their support
        """

        LK = []
        mu_k = {}

        for candidate in CK:
            candidate_support = self._Pr(candidate)

            if candidate_support * self._itemset_weight(candidate) >= self.t:
                LK.append(candidate)
                mu_k[frozenset(candidate)] = candidate_support

        return LK, mu_k

    def _algorithm_2(self, LK_1: list[set[int]]):
        """
        Input:
            LK      list[set(int)]
                list of size-k candidates
            w       dict{frozenset, float}
                dictionary contains candidates and their support
            I       dict{int, int}
                dictionary contains items and their occurrence
            t       int
                the probabilistic frequent threshold
            msup    int
                the minimum support

        Output:
            CK      list[set(int)]
                list of size-k candidates
        """
        CK = []
        I_ = set([item for candidate in LK_1 for item in candidate])

        for X in LK_1:
            for I_i in I_ - X:
                union_set = X | {I_i}

                if self._itemset_weight(union_set) >= self.t:
                    CK.append(union_set)

            I_m = min([self.w[x] for x in X])

            for I_i in self.I - I_ - X:
                union_set = X | {I_i}

                if self.w[I_i] >= I_m:
                    continue
                if self._itemset_weight(union_set) < self.t:
                    continue

                CK.append(union_set)

        return CK

    def _algorithm_3(self, LK_: list[set[int]]):
        pass

    #     """
    #     Input:
    #         LK      list[set(int)]
    #             list of size-k candidates
    #         w       dict{frozenset, float}
    #             dictionary contains candidates and their support
    #         I       dict{int, int}
    #             dictionary contains items and their occurrence
    #         t       float
    #             the probabilistic frequent threshold
    #         msup    int
    #             the minimum support
    #         alpha   float
    #             the scale factor
    #
    #     Output:
    #         CK      list[set(int)]
    #             list of size-k candidates
    #     """
    #
    #     def _factorial(n: int) -> Decimal:
    #         """
    #         Function to calculate the factorial of an integer
    #         """
    #         result = Decimal(1)
    #         for i in range(2, n + 1):
    #             result *= Decimal(i)
    #         return result
    #
    #     def _F(k: int, mu: float) -> float:
    #         """
    #         Function to calculate the PoissonDB pmf
    #         """
    #         getcontext().prec = 50
    #         mu_decimal = Decimal(mu)
    #         result = sum(
    #             (mu_decimal**i) * Decimal(exp(-mu)) / _factorial(i)
    #             for i in range(k + 1)
    #         )
    #         return float(result)
    #
    #     def _calMu(m: float, lower: float = 0, upper: float = self.n) -> float:
    #         """
    #         Function to calculate the value of mu that satisfy the pre-defined conditions
    #         """
    #         epsilon = 1e-6
    #
    #         while (upper - lower) > epsilon:
    #             middle = (upper + lower) / 2
    #             value = 1 - _F(self.msup - 1, middle) - self.t / m
    #
    #             if value > 0:
    #                 upper = middle
    #             elif value < 0:
    #                 lower = middle
    #             else:
    #                 return middle
    #
    #         return (upper + lower) / 2
    #
    #     CK = []
    #     m = max(self.w.values())
    #     mu_ = _calMu(m)
    #     I_ = set(i for X in LK_ for i in X)
    #
    #     for X in LK_:
    #         mu_X = sum(self.I[item] / self.n for item in X)
    #
    #         for I_i in I_ - set(X):
    #             mu_I_i = self.I[I_i] / self.n
    #             union_set = set(X) | {I_i}
    #
    #             if self._itemset_weight(union_set) < self.t:
    #                 continue
    #             if min(mu_X, mu_I_i) < mu_:
    #                 continue
    #             if mu_X * mu_I_i < self.alpha * self.n * mu_:
    #                 continue
    #             CK.append(union_set)
    #
    #         I_m = min(self.w[item] for item in X)
    #
    #         for I_i in set(self.I.keys()) - I_ - set(X):
    #             mu_I_i = self.I[I_i] / self.n
    #             union_set = set(X) | {I_i}
    #
    #             if self._itemset_weight(union_set) < self.t:
    #                 continue
    #             if self.w[I_i] >= I_m:
    #                 continue
    #             if min(mu_X, mu_I_i) < mu_:
    #                 continue
    #             if mu_X * mu_I_i < self.alpha * self.n * mu_:
    #                 continue
    #
    #             CK.append(union_set)
    #
    #     return CK

    # Algorithm 1
    def wPFI_Apriori(
        self,
        data_file: str,
        msup_ratio: float = 0.3,
        threshold: float = 0.6,
        scale_factor: float = 0.6,
        algorithm: int = 2,
    ):
        """
        Input:
            data_file       str
                the dataset name
            msup_ratio      float
                the ratio of minimum support in dataset
            threshold       float
                the probabilistic frequent threshold
            scale_factor    float
                the scale factor
            algorithm       int{2, 3}
                indicate the generating and pruning algorithm to be used

        Functionality:
            find all frequent itemsets that satisfy the given the input conditions

        Output:
            L   list[set[int]]
                list of the itemsets that are the result of the algorithm
        """
        start_time = perf_counter()

        self._read_data(data_file)
        self._generate_weight_table()
        print("Processing input data...")

        self.msup = int(msup_ratio * self.n)
        self.t = threshold
        self.alpha = scale_factor

        print("-----------------------------------------------------------")
        print("Dataset: ", data_file.split("/")[-1])
        print("Minimum support: ", msup_ratio)
        print("Probabilistic frequent threshold: ", threshold)
        print("-----------------------------------------------------------")

        print("There are %d transactions in this dataset" % self.n)
        print("There are %d distinct items in this dataset" % len(self.I))
        print("-----------------------------------------------------------")

        LK, support = self._scan_find_size_1()
        L = [LK]
        k = 1

        while 1:
            print("Running %i-th iteration with %i candidates..." % (k, len(LK)))

            k += 1
            # if algorithm == 3:
            #     CK = self._algorithm_3(L[-1])
            # elif algorithm == 2:
            CK = self._algorithm_2(L[-1])

            LK, support_k = self._scan_find_size_k(CK)

            if not LK:
                L = [sorted(map(tuple, map(sorted, LK))) for LK in L]
                support = {tuple(sorted(k)): v for k, v in support.items()}
                break
            else:
                L.append(LK)
                support.update(support_k)

        end_time = perf_counter()
        print("Runtime: %.2fs" % (end_time - start_time))
        return L[-1]


test = FrequentItemsetAlgorithm()
L = test.wPFI_Apriori(data_file="../../data/connect.dat", msup_ratio=0.4, threshold=0.6)
print(L)

