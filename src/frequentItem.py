from mlxtend.preprocessing import TransactionEncoder
from mlxtend.frequent_patterns import apriori
import pandas as pd

# Sample transaction dataset
transactions = [
    ["milk", "bread", "butter"],
    ["bread", "butter"],
    ["milk", "bread", "butter", "eggs"],
    ["milk", "bread"],
]


def read_data(filename):
    with open(filename, "r") as file:
        transactions = [list(map(int, line.split())) for line in file]
    return transactions


dataset = read_data("chess_test.dat")

te = TransactionEncoder()
te_ary = te.fit(dataset).transform(dataset)
df = pd.DataFrame(te_ary, columns=te.columns_)

frequent_itemsets = apriori(df, min_support=0.1, use_colnames=True, verbose=True)
print(frequent_itemsets.tail(10))
