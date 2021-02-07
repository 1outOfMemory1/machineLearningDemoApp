import os
import zipfile
from tensorflow.keras.preprocessing.image import ImageDataGenerator
import pandas as pd
import numpy as np
import tensorflow as tf
import os
import matplotlib.pyplot as plt

# 下一行打开是  关闭cuda 使用cpu 否则会调用cuda
# os.environ["CUDA_VISIBLE_DEVICES"] = "-1"

# 解压猫狗数据集 来源是kaggle的  https://www.kaggle.com/c/dogs-vs-cats
# local_zip = "/media/yhn/固态存储盘/project/kaggle/dogs-vs-cats.zip"
# zip_ref = zipfile.ZipFile(local_zip, 'r')
# zip_ref.extractall("./data")
# zip_ref.close()


base_dir = "./data"
train_dir = os.path.join(base_dir, "train")
test_dir = os.path.join(base_dir, "test")

fileListInTrainDir = os.listdir(train_dir)
# kaggle的猫狗数据集中 已经使用文件名帮我们分好类了 使用 split分割 数组第一个表示他的种类
# 例如他的一个文件的名字是 cat.cat1.jpg  第一个就是种类
# 使用pandas 将文件名字和他的种类构造成元组塞入list中
fileNameList = []
categoriesList = []
for fileName in fileListInTrainDir:
    category = fileName.split(".")[0]
    if category == "dog":
        categoriesList.append("dog")
    else:
        categoriesList.append("cat")
    fileNameList.append(fileName)
fileName_category_pd = pd.DataFrame({
    "fileName": fileNameList,
    "category": categoriesList})

# print(len(fileName_category_pd))

# print(np.array(fileName_category_pd["category"]))
# 神经网络构建  结构如下所示

"""
Model: "sequential"
_________________________________________________________________
Layer (type)                 Output Shape              Param #   
=================================================================
conv2d (Conv2D)              (None, 148, 148, 16)      448       
_________________________________________________________________
dropout (Dropout)            (None, 148, 148, 16)      0         
_________________________________________________________________
max_pooling2d (MaxPooling2D) (None, 74, 74, 16)        0         
_________________________________________________________________
conv2d_1 (Conv2D)            (None, 72, 72, 32)        4640      
_________________________________________________________________
dropout_1 (Dropout)          (None, 72, 72, 32)        0         
_________________________________________________________________
max_pooling2d_1 (MaxPooling2 (None, 36, 36, 32)        0         
_________________________________________________________________
conv2d_2 (Conv2D)            (None, 34, 34, 64)        18496     
_________________________________________________________________
dropout_2 (Dropout)          (None, 34, 34, 64)        0         
_________________________________________________________________
max_pooling2d_2 (MaxPooling2 (None, 17, 17, 64)        0         
_________________________________________________________________
flatten (Flatten)            (None, 18496)             0         
_________________________________________________________________
dropout_3 (Dropout)          (None, 18496)             0         
_________________________________________________________________
dense (Dense)                (None, 512)               9470464   
_________________________________________________________________
dropout_4 (Dropout)          (None, 512)               0         
_________________________________________________________________
dense_1 (Dense)              (None, 2)                 1026      
=================================================================
Total params: 9,495,074
Trainable params: 9,495,074
Non-trainable params: 0
_________________________________________________________________
"""
model = tf.keras.models.Sequential([
    tf.keras.layers.Conv2D(16, (3, 3), activation="relu", input_shape=(150, 150, 3)),
    tf.keras.layers.Dropout(0.25),
    tf.keras.layers.MaxPool2D(2, 2),
    tf.keras.layers.Conv2D(32, (3, 3), activation="relu"),
    tf.keras.layers.Dropout(0.25),
    tf.keras.layers.MaxPool2D(2, 2),
    tf.keras.layers.Conv2D(64, (3, 3), activation="relu"),
    tf.keras.layers.Dropout(0.25),
    tf.keras.layers.MaxPool2D(2, 2),
    tf.keras.layers.Flatten(),
    tf.keras.layers.Dropout(0.25),
    tf.keras.layers.Dense(512, activation="relu"),
    tf.keras.layers.Dropout(0.25),
    tf.keras.layers.Dense(2, activation="softmax")
])

print(model.summary())
# 使用二进制交叉熵损失函数 (binary_crossentropy) 然后使用预测准确率(accuracy)为评价指标
model.compile(loss='binary_crossentropy',
              optimizer='adam',
              metrics=['accuracy'])

# 这个函数用于分割训练集合验证集  不用手动分割了
def train_test_split_func(fileName_category_pd: pd.DataFrame, test_scale: float):
    sizeOfDf = len(fileName_category_pd)
    sizeOfTrain = int(sizeOfDf * (1 - test_scale))
    trainDf = fileName_category_pd[:sizeOfTrain]
    testDf = fileName_category_pd[sizeOfTrain:]
    return trainDf, testDf


train_df, test_df = train_test_split_func(fileName_category_pd, test_scale=0.1)
train_df = train_df.reset_index(drop=True)
test_df = test_df.reset_index(drop=True)
# print(train_df, test_df)

print("Dogs in train set: ", len(train_df[train_df['category'] == 'dog']))
print("Cats in train set: ", len(train_df[train_df['category'] == 'cat']))
print("Dogs in test set: ", len(test_df[test_df['category'] == 'dog']))
print("Cats in test set: ", len(test_df[test_df['category'] == 'cat']))


# 因为要操作的是图片 我们上方拿到的都是文件名字和种类 所以需要一个类帮我们加载图片资源
# 这里ImageDataGenerator这个类就是帮我们加载图片资源用的  他的一些参数
# 例如 rotation_range 是随机旋转0 - 50度
# height_shift_range 是图片沿着高度方向随机偏移 0-20%
# 这样做的目的是为了防止过拟合 让数据集的泛化效果更好
train_datagen = \
    ImageDataGenerator(rescale=1. / 255,
                       rotation_range=50,
                       width_shift_range=0.2,
                       height_shift_range=0.2,
                       shear_range=0.2,
                       zoom_range=0.2,
                       horizontal_flip=True,
                       fill_mode="nearest")
test_datagen = ImageDataGenerator(rescale=1. / 255)

train_generator = train_datagen. \
    flow_from_dataframe(train_df,
                        './data/train/',
                        x_col='fileName',
                        y_col='category',
                        target_size=(150, 150),
                        class_mode='categorical',
                        batch_size=64,
                        color_mode='rgb',
                        shuffle=True)

test_generator = test_datagen. \
    flow_from_dataframe(test_df,
                        './data/train/',
                        x_col='fileName',
                        y_col='category',
                        target_size=(150, 150),
                        class_mode='categorical',
                        batch_size=64,
                        color_mode='rgb',
                        shuffle=True)

history = model.fit(
    train_generator,
    steps_per_epoch=len(train_df) // 64,
    epochs= 50,
    validation_data=test_generator,
    validation_steps=len(test_df) // 64)


model.save_weights('cats_vs_dogs_WEIGHTS.h5')
print('Training accuracy: {:.3f}'.format(history.history['accuracy'][-1]))
print('Validation accuracy: {:.3f}'.format(history.history['val_accuracy'][-1]))
# 画出损失值得图像和准确率值的图像

fig, (ax1, ax2) = plt.subplots(2, 1, figsize=(12, 12))
ax1.plot(history.history["loss"], color="red", label="Training loss")
ax1.plot(history.history["val_loss"], color="b", label="Validation loss")

ax1.set_xticks(np.arange(1, 10, 1))
ax1.set_yticks(np.arange(0, 1, 0.1))

ax2.plot(history.history["accuracy"], color="red", label="Training accuracy")
ax2.plot(history.history["val_accuracy"], color="b", label="Validation accuracy")

ax2.set_xticks(np.arange(1, 10, 1))
ax2.set_yticks(np.arange(0, 1, 0.1))

legend = plt.legend(loc="best", shadow=True)
plt.tight_layout()
plt.show()

"""
以下是一些训练测试  去除某些测试验证过拟合问题
20 轮 加上图片旋转 平移等 和 dropout层 发现 准确率很高 解决了一部分的过拟合问题
Training accuracy: 0.799
Validation accuracy: 0.797

20 轮 减去图片旋转 平移等 过拟合问题很严重 测试集数据一会就会不升反降
Training accuracy: 0.986
Validation accuracy: 0.833

20 轮  减去图片旋转 平移等 和 dropout层  过拟合问题最严重 测试集loss 疯狂跳动
Training accuracy: 0.995
Validation accuracy: 0.821

50 轮 加上图片旋转 平移等 和 dropout层  准确率 很高
Training accuracy: 0.860
Validation accuracy: 0.890

"""


