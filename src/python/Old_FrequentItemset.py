"""
Course: 503040 Design and Analysis of Algorithms
Problem: Weighted Frequent Itemset Mining in Uncertain Datasets
Author: Nguyen Hoang Phuc
Github: scuph-ng
"""

from math import e as E
from math import exp, ceil
from random import gauss, uniform
from time import perf_counter
from decimal import Decimal, getcontext


class Item:
    def __init__(self, value: int) -> None:
        self.value = value
        self.prob = ceil(gauss(0.5, 0.125) * 10) / 10

    def setProb(self, new_prob: float) -> None:
        self.prob = new_prob


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
            self.T   list[list[tuple[int, float]]]
                Dataset is a list of transactions
        """

        with open(filename, "r") as f:
            for line in f:
                transaction = [Item(int(item)) for item in line.split()]
                self.T.append(transaction)

            self._generate_weight_table()

        f.close()
        return

    def _generate_weight_table(self):
        unique_item = {item.value for tx in self.T for item in tx}
        for item in unique_item:
            self.w[item] = round(uniform(1e-6, 1), 1)

    def _chernoff_bound(self, X: set[int]) -> bool:
        def _expected_support(X: set[int]) -> float:
            S_e = 0

            for tx in self.T:
                tx_items = {item.value for item in tx}

                if not X.issubset(tx_items):
                    continue

                p_tx = 1
                for item in tx:
                    if item.value in X:
                        p_tx *= item.prob
                S_e += p_tx

            print(S_e)
            return S_e

        # -----------------------------------------------------

        mu = _expected_support(X)
        sigma = (self.msup * self.n - mu - 1) / mu

        if sigma >= 2 * E - 1 and pow(2, -sigma * mu) < self.t:
            return False

        if 0 < sigma < 2 * E - 1 and exp(-pow(sigma, 2) * mu / 4) < self.t:
            return False

        return True

    def _Pr(self, X: list[Item]) -> float:
        """
        Calculate the existential probability of an itemset
        --------------------
        Input:
        self.T      list[list[Item]]
            List of transactions in dataset

        X           list[Item]
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

        X_items = [item.value for item in X]

        for transaction in self.T:
            tx_items = [item.value for item in transaction]
            p_X_i = 1

            for item in X_items:
                if item not in tx_items:
                    p_X_i = 0
                    break
                p_X_i *= self.I[item] * inv_n

            f_X = [0] * (self.n + 1)
            f_X[0] = (1 - p_X_i) * fX[0]

            for k in range(1, self.n + 1):
                f_X[k] = p_X_i * fX[k - 1] + (1 - p_X_i) * fX[k]
            fX = f_X[:]

        return sum(fX[int(self.msup * self.n) :])

    def _itemset_support(self, X: list[Item], TX) -> float:
        result = 1

        for item in X:
            if item in TX:
                result *= item.prob
            else:
                result *= 1 - item.prob

        return result

    def _itemset_weight(self, X: list[Item]) -> float:
        """
        Input:
            X       list[Item]
                an itemset of integers
            w       dict[frozenset[int], float]
                dictionary contains candidates and their weight

        Functionality:
            calculate the mean weight of items in the itemset

        Output:
            w_X    float
                the average weight of the itemset
        """

        w_X = sum(self.w[item.value] for item in X)
        return w_X / len(X)

    def _scan_find_size_1(self):
        """
        Input:
            T       list[list[Item]]
                list of transactions in dataset
            I       dict{int, int}
                dictionary of items and their count
            msup    float
                the minimum support

        Functionality:
            scan the dataset and generate the size_1 candidates and their support

        Output:
            L1      list[list[tuple[int, float]]]
                list of the size_1 candidates
            mu_1    dict[frozenset[int], int]
                dictionary of candidates and their count
        """

        L1 = []
        mu_1 = {}
        count = {}

        for tx in self.T:
            for item in tx:
                count[item.value] = count.get(item.value, 0) + 1

        # self.I = support_count

        for value, occurence in count.items():
            if float(occurence) < self.msup * self.n:
                continue

            if self.w[value] * self._Pr([value]) < self.t:
                continue

            L1.append([value])
            mu_1[frozenset([value])] = occurence

        return L1, mu_1

    def _scan_find_size_k(self, CK: list[list[Item]]):
        """
        Input:
            CK      list[list[Item]]
                list of candidates
            T       list[list[Item]]
                list of transactions
            msup    float
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

        def _condition(candidate: list[Item], count: int) -> bool:
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
                if set(candidate).issubset(transaction):
                    frozen_candidate = frozenset(candidate)
                    support_count[frozen_candidate] = (
                        support_count.get(frozen_candidate, 0) + 1
                    )

        for candidate, count in support_count.items():
            if _condition(candidate, count):
                LK.append(candidate)
                mu_k[candidate] = count

        return LK, mu_k

    def _algorithm_2(self, LK_: list[list[tuple[int, float]]]):
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

    def _algorithm_3(self, LK_: list[list[tuple[int, float]]]):
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
                (mu_decimal**i) * Decimal(exp(-mu)) / _factorial(i)
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
        filename: str,
        max_length: int = -1,
        msup_ratio: float = 0.3,
        threshold: float = 0.6,
        scale_factor: float = 0.6,
        algorithm: int = 3,
    ):
        """
        Input:
        data_file       str
            Dataset name or directory

        msup_ratio      float
            the ratio of minimum support in dataset

            threshold       float
                the probabilistic frequent threshold

            scale_factor    float
                the scale factor

            algorithm       int
                Values: {2, 3}
                Default: 3
                indicate the generating and pruning algorithm to be used

        Functionality:
            find all frequent itemsets that satisfy the given the input conditions

        Output:
            L   list[list[tuple[int, float]]]
                list of the itemsets that are the result of the algorithm
        """
        print("Processing input data...")
        self._read_data(filename)

        if max_length != -1:
            self.T = self.T[:max_length]
            self.n = max_length
        else:
            self.n = len(self.T)

        self.msup = msup_ratio
        self.t = threshold
        self.alpha = scale_factor

        print(self._chernoff_bound({1, 3}))
        return

        start_time = perf_counter()
        L1, support = self._scan_find_size_1()
        CK = [{i} for i in support.keys()]
        L = [L1]
        k = 1

        print("-----------------------------------------------------------")
        print("There are %d transactions in this dataset" % self.n)
        print("There are %d distinct items in this dataset" % len(self.I.keys()))
        print("-----------------------------------------------------------")
        print("Dataset: ", filename)
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
    filename="./data/mushroom.dat", msup_ratio=0.2, threshold=0.6, algorithm=2
)
# print("Result:")
# for i in L[-1]:
#     print(i)

