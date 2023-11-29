import math
import random
import matplotlib.pyplot as plt


def normal_dist(mu, sigma, x):
    # mu = random.random()
    # sigma = random.random()
    # Box-Muller
    f_X = (1 / math.sqrt(2 * math.pi * sigma**2)) ** (-((x - mu) ** 2) / (2 * sigma**2))
    return f_X


random_num = sorted([random.random() for _ in range(1000)])
y = [normal_dist(0.5, 0.125, i) for i in random_num]
plt.plot(random_num, y)
plt.show()

