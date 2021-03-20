
import cv2
import numpy as np

cv2.namedWindow('input_image', 0)
cv2.resizeWindow('input_image', 600, 800)
img = np.zeros((512,512,3), dtype = np.uint8)
# img = cv2.imread("F:/test.jpg")

cv2.rectangle(img, (10,10), (510,510), (0, 0,255),2)
cv2.imshow("input_image", img)  # 显示图片，后面会讲解
cv2.waitKey(0)  # 等待按键
