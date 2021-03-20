import os
import json
import time
import torch
import cv2
import numpy as np
from matplotlib import pyplot as plt
from build_utils import img_utils
from build_utils import torch_utils
from build_utils import utils
from models import Darknet
from draw_box_utils import draw_box


def main():
    img_size = 512  # 必须是32的整数倍 [416, 512, 608]
    cfg = "cfg/my_yolov3.cfg"  # 改成生成的.cfg文件
    weights = "F:\机器学习数据\yolov3_spp\weights\yolov3spp-12.pt".format(img_size)  # 改成自己训练好的权重文件
    json_path = "./data/pascal_voc_classes.json"  # json标签文件
    img_path = "F:/0_Parade_marchingband_1_15.jpg"
    assert os.path.exists(cfg), "cfg file {} dose not exist.".format(cfg)
    assert os.path.exists(weights), "weights file {} dose not exist.".format(weights)
    assert os.path.exists(json_path), "json file {} dose not exist.".format(json_path)
    assert os.path.exists(img_path), "image file {} dose not exist.".format(img_path)

    json_file = open(json_path, 'r')
    class_dict = json.load(json_file)
    category_index = {v: k for k, v in class_dict.items()}

    input_size = (img_size, img_size)

    device = torch.device("cpu")

    model = Darknet(cfg, img_size)
    model.load_state_dict(torch.load(weights, map_location=device)["model"])
    model.to(device)

    model.eval()
    with torch.no_grad():
        # init
        img = torch.zeros((1, 3, img_size, img_size), device=device)
        model(img)

        img_o = cv2.imread(img_path)  # BGR
        assert img_o is not None, "Image Not Found " + img_path

        img = img_utils.letterbox(img_o, new_shape=input_size, auto=True, color=(0, 0, 0))[0]
        # Convert
        img = img[:, :, ::-1].transpose(2, 0, 1)  # BGR to RGB, to 3x416x416
        img = np.ascontiguousarray(img)

        img = torch.from_numpy(img).to(device).float()
        img /= 255.0  # scale (0, 255) to (0, 1)
        img = img.unsqueeze(0)  # add batch dimension

        t1 = torch_utils.time_synchronized()
        pred = model(img)[0]  # only get inference result
        t2 = torch_utils.time_synchronized()
        print(t2 - t1)

        pred = utils.non_max_suppression(pred, conf_thres=0.1, iou_thres=0.6, multi_label=True)[0]
        t3 = time.time()
        print(t3 - t2)

        if pred is None:
            print("No target detected.")
            exit(0)

        # process detections
        pred[:, :4] = utils.scale_coords(img.shape[2:], pred[:, :4], img_o.shape).round()
        print(pred.shape)

        bboxes = pred[:, :4].detach().cpu().numpy()
        scores = pred[:, 4].detach().cpu().numpy()
        classes = pred[:, 5].detach().cpu().numpy().astype(np.int) + 1

        cv2.namedWindow('input_image', 0)
        cv2.resizeWindow('input_image', 600, 800)
        print(bboxes.tolist())
        draw_1 = img_o
        for ele in range(len(bboxes.tolist())) :
            x = int(bboxes.tolist()[ele][0])
            y = int(bboxes.tolist()[ele][1])
            x1 = int(bboxes.tolist()[ele][2])
            y1 = int(bboxes.tolist()[ele][3])
            draw_1 =cv2.rectangle(draw_1, (x,y), (x1,y1), (0, 0,255),1)
        cv2.imshow("input_image",draw_1)
        cv2.waitKey(0)



if __name__ == "__main__":
    main()
