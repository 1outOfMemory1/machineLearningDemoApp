import os
import json
import time
import torch
import cv2
import numpy as np
from build_utils import img_utils
from build_utils import utils
from models import Darknet
from flask import request, Flask


def detect(img_path):
    img_o = cv2.imread(img_path)  # BGR
    assert img_o is not None, "Image Not Found " + img_path
    with torch.no_grad():
        img = img_utils.letterbox(img_o, new_shape=input_size, auto=True, color=(0, 0, 0))[0]
        # Convert
        img = img[:, :, ::-1].transpose(2, 0, 1)  # BGR to RGB, to 3x416x416
        img = np.ascontiguousarray(img)
        img = torch.from_numpy(img).to(device).float()
        img /= 255.0  # scale (0, 255) to (0, 1)
        img = img.unsqueeze(0)  # add batch dimension
        pred = model(img)[0]  # only get inference result
        pred = utils.non_max_suppression(pred, conf_thres=0.1, iou_thres=0.6, multi_label=True)[0]
        if pred is None:
            print("No target detected.")
            return None
        # process detections
        pred[:, :4] = utils.scale_coords(img.shape[2:], pred[:, :4], img_o.shape).round()
        print(pred.shape)
        bboxes = pred[:, :4].detach().cpu().numpy()
        print(bboxes.tolist())
        dic = {}
        dic["faceNum"] = len(bboxes)
        dic["rectangleLocation"] = bboxes.tolist()
        return dic
app = Flask(__name__)


@app.route('/')
def hello_world():
    return 'Hello, World!'


@app.route("/uploadPic", methods=['POST', 'GET'])
def get_pic():
    global labelLocation
    fileLocation = request.form['fileLocation']
    rectangleFrame = detect(fileLocation)
    return str(rectangleFrame)


if __name__ == "__main__":
    img_size = 608  # 必须是32的整数倍 [416, 512, 608]
    cfg = "./my_yolov3.cfg"  # 改成生成的.cfg文件
    weights = "./yolov3spp-12.pt".format(img_size)  # 改成自己训练好的权重文件
    json_path = "./pascal_voc_classes.json"  # json标签文件
    img_path = "test.jpg"
    assert os.path.exists(cfg), "cfg file {} dose not exist.".format(cfg)
    assert os.path.exists(weights), "weights file {} dose not exist.".format(weights)
    assert os.path.exists(json_path), "json file {} dose not exist.".format(json_path)
    json_file = open(json_path, 'r')
    class_dict = json.load(json_file)
    category_index = {v: k for k, v in class_dict.items()}
    input_size = (img_size, img_size)
    device = torch.device("cpu")
    model = Darknet(cfg, img_size)
    model.load_state_dict(torch.load(weights, map_location=device)["model"])
    model.to(device)
    model.eval()
    app.run(host="0.0.0.0",port=9000)





