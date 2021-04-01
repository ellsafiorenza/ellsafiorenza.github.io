import os
import numpy as np
import pandas as pd

directory = '/Users/ellsafiorenza/Downloads/Stickearn/tracking/frames'

train_directory = os.path.join(directory, 'train')
test_directory = os.path.join(directory, 'test')

if not os.path.exists(train_directory):
    os.makedirs(train_directory)

if not os.path.exists(test_directory):
    os.makedirs(test_directory)

filenames = []

for filename in os.listdir(directory):
    if filename.endswith(".jpg"):
        filename_xml = filename[:-4] + ".txt"
        if os.path.isfile(os.path.join(directory, filename_xml)):
            filenames.append(filename[:-4])
    else:
        continue

df = pd.DataFrame(filenames, columns=['filename'])

train_percentage = 0.75 #decimal (0.01 - 0.99)

msk = np.random.rand(len(df)) < train_percentage

train = df[msk]

test = df[~msk]

for index, row in train.iterrows():
    os.rename(os.path.join(directory, row['filename'] + ".jpg"), os.path.join(train_directory, row['filename'] + ".jpg"))
    os.rename(os.path.join(directory, row['filename'] + ".txt"), os.path.join(train_directory, row['filename'] + ".txt"))

for index, row in test.iterrows():
    os.rename(os.path.join(directory, row['filename'] + ".jpg"), os.path.join(test_directory, row['filename'] + ".jpg"))
    os.rename(os.path.join(directory, row['filename'] + ".txt"), os.path.join(test_directory, row['filename'] + ".txt"))
