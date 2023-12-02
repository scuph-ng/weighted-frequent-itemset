def calculate_expected_support(transactions, itemset):
    # Count occurrences of the itemset in transactions
    itemset_count = sum(
        1
        for transaction in transactions
        if all((item, prob) in transaction for item, prob in itemset)
    )

    # Calculate the expected support
    total_transactions = len(transactions)
    expected_support = itemset_count / total_transactions

    return expected_support


# Example transactions with existential probabilities
transactions = [
    [(1, 0.1), (2, 0.2), (3, 0.3)],
    [(4, 0.4), (2, 0.2), (6, 0.6)],
    [(7, 0.7), (8, 0.8), (3, 0.3)],
]

# Example itemset with probabilities (you can replace this with your itemset)
itemset = [(2, 0.2), (3, 0.3)]

# Calculate expected support
expected_support = calculate_expected_support(transactions, itemset)

# Print the result
print("Expected Support for Itemset {}: {:.2%}".format(itemset, expected_support))

