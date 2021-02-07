import tensorflow as tf
from keras.preprocessing import image
import os
import sys

try:
    filepath = sys.argv[1]
except Exception:
    print("Python执行时没有传入参数")

os.environ["CUDA_VISIBLE_DEVICES"] = "-1"

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

model.load_weights("/root/tfPython/detectCatsAndDogs/detectCatsAndDogs.h5")

pic = image.load_img(filepath, target_size=(150, 150))
pic = image.img_to_array(pic)
pic = pic.reshape(1, 150, 150, 3)
type = model.predict(pic)
if type[0][0] == 0:
    print("狗")
else:
    print("猫")
