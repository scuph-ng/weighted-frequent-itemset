"""
Course: 503040 Design and Analysis of Algorithms
Problem: Weighted Frequent Itemset Mining in Uncertain Datasets
Author: Nguyen Hoang Phuc
Github: scuph-ng
"""

import math
from random import random, gauss
from time import perf_counter
from decimal import Decimal, getcontext


class FrequentItemsetAlgorithm:
    def __init__(self):
        self.n = 0
        self.t = 1
        self.msup = 1
        self.alpha = 0
        self.T = list()
        self.I = dict()
        self.w = dict()

    def _read_data(self, filename: str):
        """
        Input:
        filename    str
            Dataset name or directory to the dataset

        Functionality:
            read the dataset to T and obtain the size n of dataset

        Output:
            self.T   list[list[tuple(int, float)]]
                Dataset is a list of transactions
        """
        with open(filename, "r") as f:
            for line in f:
                transaction = [(int(item), gauss(0.5, 0.125)) for item in line.split()]
                self.T.append(transaction)

            # for item in self.T[0]:
            #     print("(%d, %.2f)" % (item[0], item[1]))

        f.close()
        return

    # TODO: re-write other functions to adapt to T's changes

    def _Pr(self, X: list[list[tuple[int, float]]]) -> float:
        """
        Calculate the existential probability of an itemset
        --------------------
        Input:
        self.T      list[list[tuple[int, float]]]
            List of transactions in dataset

        X           dict[frozenset[int], float]
            An itemset with existential probability assigned to each item

        self.n      int
            The number of transactions

        --------------------
        Return:
        result:     float
            The existential probability of an itemset
        """
        inv_n = 1 / self.n
        fX = [1] + [0 for _ in range(self.n)]

        for transaction in self.T:
            p_X_i = 1

            for item in X:
                if item not in transaction:
                    p_X_i = 0
                    break
                p_X_i *= self.I[item] * inv_n

            f_X = [0] * (self.n + 1)
            f_X[0] = (1 - p_X_i) * fX[0]

            for k in range(1, self.n + 1):
                f_X[k] = p_X_i * fX[k - 1] + (1 - p_X_i) * fX[k]
            fX = f_X[:]

        return sum(fX[self.msup :])

    def _itemset_support(self, X: list[tuple[int, float]], TX) -> float:
        result = 1

        for item in X:
            if item in TX:
                result *= item[1]
            else:
                result *= 1 - item[1]

        return result

    def _itemset_weight(self, X: list[tuple[int, float]]) -> float:
        """
        Input:
            X       list[tuple[int, float]]
                an itemset of integers
            w       dict{frozenset, float}
                dictionary contains candidates and their weight

        Functionality:
            calculate the mean weight of items in the itemset

        Output:
            w(X)    float
                the average weight of the itemset
        """
        weight = sum(self.w[item[0]] for item in X)
        return weight / len(X)

    def _scan_find_size_1(self):
        """
        Input:
            T       list[list[tuple[int, float]]]
                list of transactions in dataset
            I       dict{int, int}
                dictionary of items and their support
            msup    int
                the minimum support

        Functionality:
            scan the dataset and generate the size_1 candidates and their support

        Output:
            L1      list[list[int]]
                list of the size_1 candidates
            mu_1    dict{frozenset(int), int}
                dictionary of candidates and their support
        """

        L1 = []
        mu_1 = {}
        support_count = {}

        for tx in self.T:
            for item in tx:
                support_count[item[0]] = support_count.get(item[0], 0) + 1

        # self.I = support_count

        for item, count in support_count.items():
            self.w[item] = 1

            if count < self.msup:
                continue

            if self.w[item] * self._Pr({item}) < self.t:
                continue

            L1.append([item])
            mu_1[frozenset([item])] = count

        return L1, mu_1

    def _scan_find_size_k(self, CK: list[set[int]]):
        """
        Input:
            CK      list[set(int)]
                list of candidates
            T       list[set(int)]
                list of transactions
            msup    int
                the minimum support for PFI mining
            w       dict{frozenset, float}
                the weight table
            t       float
                the probabilistic frequent threshold

        Functionality:
            scan the dataset and generate the size_k candidates and their support

        Output:
            LK      list[list[int]]
                list of size-k candidates
            mu_k    dict{frozenset, int}
                dictionary contains candidates and their support
        """

        def _condition(candidate: set[int], count: int) -> bool:
            if count < self.msup:
                return False

            candidate_w = self._itemset_weight(candidate)
            if candidate_w * self._Pr(candidate) < self.t:
                return False

            return True

        LK = []
        mu_k = {}
        support_count = {}

        for transaction in self.T:
            for candidate in CK:
                if candidate.issubset(transaction):
                    frozen_candidate = frozenset(candidate)
                    support_count[frozen_candidate] = (
                        support_count.get(frozen_candidate, 0) + 1
                    )

        for candidate, count in support_count.items():
            if _condition(candidate, count):
                LK.append(candidate)
                mu_k[candidate] = count

        return LK, mu_k

    def _algorithm_2(self, LK_: list[set[int]]):
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
        I_ = set([item for candidate in LK_ for item in candidate])

        for X in LK_:
            for I_i in I_ - set(X):
                union_set = set(X) | {I_i}
                # union_set.add(I_i)

                if self._itemset_weight(union_set) >= self.t:
                    CK.append(union_set)

            I_m = min([self.w[i] for i in X])

            for I_i in set(self.I.keys()) - I_ - set(X):
                union_set = set(X) | {I_i}

                if self.w[I_i] >= I_m:
                    continue
                if self._itemset_weight(union_set) < self.t:
                    continue

                CK.append(union_set)

        return CK

    def _algorithm_3(self, LK_: list[set[int]]):
        """
        Input:
            LK      list[set(int)]
                list of size-k candidates
            w       dict{frozenset, float}
                dictionary contains candidates and their support
            I       dict{int, int}
                dictionary contains items and their occurrence
            t       float
                the probabilistic frequent threshold
            msup    int
                the minimum support
            alpha   float
                the scale factor

        Output:
            CK      list[set(int)]
                list of size-k candidates
        """

        def _factorial(n: int) -> Decimal:
            """
            Function to calculate the factorial of an integer
            """
            result = Decimal(1)
            for i in range(2, n + 1):
                result *= Decimal(i)
            return result

        def _F(k: int, mu: float) -> float:
            """
            Function to calculate the PoissonDB pmf
            """
            getcontext().prec = 50
            mu_decimal = Decimal(mu)
            result = sum(
                (mu_decimal**i) * Decimal(math.exp(-mu)) / _factorial(i)
                for i in range(k + 1)
            )
            return float(result)

        def _calMu(m: float, lower: float = 0, upper: float = self.n) -> float:
            """
            Function to calculate the value of mu that satisfy the pre-defined conditions
            """
            epsilon = 1e-6

            while (upper - lower) > epsilon:
                middle = (upper + lower) / 2
                value = 1 - _F(self.msup - 1, middle) - self.t / m

                if value > 0:
                    upper = middle
                elif value < 0:
                    lower = middle
                else:
                    return middle

            return (upper + lower) / 2

        CK = []
        m = max(self.w.values())
        mu_ = _calMu(m)
        I_ = set(i for X in LK_ for i in X)

        for X in LK_:
            mu_X = sum(self.I[item] / self.n for item in X)

            for I_i in I_ - set(X):
                mu_I_i = self.I[I_i] / self.n
                union_set = set(X) | {I_i}

                if self._itemset_weight(union_set) < self.t:
                    continue
                if min(mu_X, mu_I_i) < mu_:
                    continue
                if mu_X * mu_I_i < self.alpha * self.n * mu_:
                    continue
                CK.append(union_set)

            I_m = min(self.w[item] for item in X)

            for I_i in set(self.I.keys()) - I_ - set(X):
                mu_I_i = self.I[I_i] / self.n
                union_set = set(X) | {I_i}

                if self._itemset_weight(union_set) < self.t:
                    continue
                if self.w[I_i] >= I_m:
                    continue
                if min(mu_X, mu_I_i) < mu_:
                    continue
                if mu_X * mu_I_i < self.alpha * self.n * mu_:
                    continue

                CK.append(union_set)

        return CK

    # Algorithm 1
    def wPFI_Apriori(
        self,
        data_file: str,
        max_length: int = -1,
        msup_ratio: float = 0.3,
        threshold: float = 0.6,
        scale_factor: float = 0.6,
        algorithm: int = 3,
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
        # start_time = perf_counter()
        print("Processing input data...")
        self._read_data(data_file)
        return

        if max_length != -1:
            self.T = self.T[:max_length]
            self.n = max_length
        else:
            self.n = len(self.T)

        self.msup = msup_ratio
        self.t = threshold
        self.alpha = scale_factor

        L1, support = self._scan_find_size_1()
        CK = [{i} for i in support.keys()]
        L = [L1]
        k = 1

        print("-----------------------------------------------------------")
        print("There are %d transactions in this dataset" % self.n)
        print("There are %d distinct items in this dataset" % len(self.I.keys()))
        print("-----------------------------------------------------------")
        print("Dataset: ", data_file)
        print("Algorithm: ", algorithm)
        print("Minimum support ratio: ", msup_ratio)
        print("Probabilistic frequent threshold: ", threshold)
        print("Scale factor: ", scale_factor)
        print("-----------------------------------------------------------")

        while 1:
            print("Running %i-th iteration with %i candidates..." % (k, len(CK)))

            k += 1
            if algorithm == 3:
                CK = self._algorithm_3(L[-1])
            elif algorithm == 2:
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
        print("Runtime: %.2fs" % float(end_time - start_time))
        return L


test = FrequentItemsetAlgorithm()
L = test.wPFI_Apriori(
    data_file="./data/chess.dat", msup_ratio=0.4, threshold=0.6, algorithm=2
)
# print("Result:")
# for i in L[-1]:
#     print(i)

