import math
import random
import matplotlib.pyplot as plt


def normal_dist(mu, sigma, x):
    # mu = random.random()
    # sigma = random.random()
    # Box-Muller
    f_X = (1 / math.sqrt(2 * math.pi * sigma**2)) ** (-((x - mu) ** 2) / (2 * sigma**2))
    return round(f_X, 1)


size = 1000
y = [random.gauss(0.5, 0.125) for _ in range(size)]

plt.hist(sorted(y))
plt.show()

