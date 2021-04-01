import glob
import os
current_dir = "/Users/ellsafiorenza/Downloads/Stickearn/tracking/frames"
path_in_drive = "data/obj/"

file_train = open(current_dir + 'train.txt', 'w')
file_test = open(current_dir + 'test.txt', 'w')

for x in ['train','test']:
    for file in glob.iglob(os.path.join(current_dir, x, '*.jpg')):
        title, ext = os.path.splitext(os.path.basename(file))
        if x == 'train':
            file_train.write(path_in_drive + x + "/" + title + '.jpg' + "\n")
        elif x == 'test':
            file_test.write(path_in_drive + x + "/" + title + '.jpg' + "\n")
