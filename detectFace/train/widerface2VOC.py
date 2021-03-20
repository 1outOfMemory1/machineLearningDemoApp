# -*- coding: utf-8 -*-

"""
上下两个脚本结合一块用 可以将widerface数据集转变为voc格式的数据集
上下两个脚本结合一块用 可以将widerface数据集转变为voc格式的数据集
上下两个脚本结合一块用 可以将widerface数据集转变为voc格式的数据集
上下两个脚本结合一块用 可以将widerface数据集转变为voc格式的数据集
上下两个脚本结合一块用 可以将widerface数据集转变为voc格式的数据集
上下两个脚本结合一块用 可以将widerface数据集转变为voc格式的数据集
上下两个脚本结合一块用 可以将widerface数据集转变为voc格式的数据集
上下两个脚本结合一块用 可以将widerface数据集转变为voc格式的数据集
上下两个脚本结合一块用 可以将widerface数据集转变为voc格式的数据集
上下两个脚本结合一块用 可以将widerface数据集转变为voc格式的数据集
上下两个脚本结合一块用 可以将widerface数据集转变为voc格式的数据集
上下两个脚本结合一块用 可以将widerface数据集转变为voc格式的数据集
上下两个脚本结合一块用 可以将widerface数据集转变为voc格式的数据集
上下两个脚本结合一块用 可以将widerface数据集转变为voc格式的数据集
上下两个脚本结合一块用 可以将widerface数据集转变为voc格式的数据集
"""



import shutil
import random
import os
import string
from skimage import io

headstr = """\
<annotation>
    <folder>VOC2012</folder>
    <filename>%06d.jpg</filename>
    <source>
        <database>My Database</database>
        <annotation>PASCAL VOC2012</annotation>
        <image>flickr</image>
        <flickrid>NULL</flickrid>
    </source>
    <owner>
        <flickrid>NULL</flickrid>
        <name>company</name>
    </owner>
    <size>
        <width>%d</width>
        <height>%d</height>
        <depth>%d</depth>
    </size>
    <segmented>0</segmented>
"""
objstr = """\
    <object>
        <name>%s</name>
        <pose>Unspecified</pose>
        <truncated>0</truncated>
        <difficult>0</difficult>
        <bndbox>
            <xmin>%d</xmin>
            <ymin>%d</ymin>
            <xmax>%d</xmax>
            <ymax>%d</ymax>
        </bndbox>
    </object>
"""

tailstr = '''\
</annotation>
'''




def writexml(idx, head, bbxes, tail):
    filename = ("Annotations/%06d.xml" % (idx))
    f = open(filename, "w")
    f.write(head)
    for bbx in bbxes:
        f.write(objstr % ('face', bbx[0], bbx[1], bbx[0] + bbx[2], bbx[1] + bbx[3]))
    f.write(tail)
    f.close()


def clear_dir():
    if shutil.os.path.exists(('Annotations')):
        shutil.rmtree(('Annotations'))
    if shutil.os.path.exists(('ImageSets')):
        shutil.rmtree(('ImageSets'))
    if shutil.os.path.exists(('JPEGImages')):
        shutil.rmtree(('JPEGImages'))

    shutil.os.mkdir(('Annotations'))
    shutil.os.makedirs(('ImageSets/Main'))
    shutil.os.mkdir(('JPEGImages'))


def excute_datasets(idx, datatype):

    f = open(('ImageSets/Main/' + datatype + '.txt'), 'a')
    f_bbx = open(('wider_face_split/wider_face_' + datatype + '_bbx_gt.txt'), 'r')

    while True:
        filename = f_bbx.readline().strip('\n')

        if not filename:
            break


        im = io.imread(('WIDER_' + datatype + '/images/' + filename))
        head = headstr % (idx, im.shape[1], im.shape[0], im.shape[2])
        nums = f_bbx.readline().strip('\n')
        bbxes = []
        if nums=='0':
            bbx_info= f_bbx.readline()
            continue
        for ind in range(int(nums)):
            bbx_info = f_bbx.readline().strip(' \n').split(' ')
            bbx = [int(bbx_info[i]) for i in range(len(bbx_info))]
            # x1, y1, w, h, blur, expression, illumination, invalid, occlusion, pose
            if bbx[7] == 0:
                bbxes.append(bbx)
        writexml(idx, head, bbxes, tailstr)
        shutil.copyfile(('WIDER_' + datatype + '/images/' + filename), ('JPEGImages/%06d.jpg' % (idx)))
        f.write('%06d\n' % (idx))
        idx += 1
    f.close()
    f_bbx.close()
    return idx


if __name__ == '__main__':
    clear_dir()
    idx = 1
    idx = excute_datasets(idx, 'train')
    idx = excute_datasets(idx, 'val')
    print('Complete...')


#上下两个脚本结合一块用 可以将widerface数据集转变为voc格式的数据集
#上下两个脚本结合一块用 可以将widerface数据集转变为voc格式的数据集
#上下两个脚本结合一块用 可以将widerface数据集转变为voc格式的数据集
#上下两个脚本结合一块用 可以将widerface数据集转变为voc格式的数据集
#上下两个脚本结合一块用 可以将widerface数据集转变为voc格式的数据集

#
# """
#  @Usage: generate custom voc-format-dataset labels, convert .xml to .txt for each image
#  @author: sun qian
#  @date: 2019/9/25
#  @note: dataset file structure must be modified as:
#  --VOCdevkit
#    --VOC2012
#      --Annotations
#      --ImageSets
#         --Main (include train.txt, test.txt, val.txt)
#      --JPEGImages
#      --labels
#  @ merge val and test: Run command: type 2012_test.txt 2012_val.txt  > test.txt
# """
# import xml.etree.ElementTree as ET
# import os
# from os import getcwd
#
# # file list - train.txt, test.txt, val.txt
# sets = [('2012', 'train'), ('2012', 'val')]
#
# # class name
# classes = ["face"]
#
#
# def convert(size, box):
#     dw = 1. / size[0]
#     dh = 1. / size[1]
#     x = (box[0] + box[1]) / 2.0
#     y = (box[2] + box[3]) / 2.0
#     w = box[1] - box[0]
#     h = box[3] - box[2]
#     x = x * dw
#     w = w * dw
#     y = y * dh
#     h = h * dh
#     return (x, y, w, h)
#
#
# def convert_annotation(year, image_id):
#     in_file = open('VOCdevkit/VOC%s/Annotations/%s.xml' % (year, image_id))
#     out_file = open('VOCdevkit/VOC%s/labels/%s.txt' % (year, image_id), 'w')
#     tree = ET.parse(in_file)
#     root = tree.getroot()
#     size = root.find('size')
#     w = int(size.find('width').text)
#     h = int(size.find('height').text)
#
#     for obj in root.iter('object'):
#         difficult = obj.find('difficult').text
#         cls = obj.find('name').text
#         if cls not in classes or int(difficult) == 1:
#             continue
#         cls_id = classes.index(cls)
#         xmlbox = obj.find('bndbox')
#         b = (float(xmlbox.find('xmin').text), float(xmlbox.find('xmax').text), float(xmlbox.find('ymin').text),
#              float(xmlbox.find('ymax').text))
#         bb = convert((w, h), b)
#         out_file.write(str(cls_id) + " " + " ".join([str(a) for a in bb]) + '\n')
#
#
# if __name__ == '__main__':
#     wd = getcwd()
#     for year, image_set in sets:
#         if not os.path.exists('VOCdevkit/VOC%s/labels/' % (year)):
#             os.makedirs('VOCdevkit/VOC%s/labels/' % (year))
#         image_ids = open('VOCdevkit/VOC%s/ImageSets/Main/%s.txt' % (year, image_set)).read().strip().split()
#         list_file = open('%s_%s.txt' % (year, image_set), 'w')
#         for image_id in image_ids:
#             line = '%s/VOCdevkit/VOC%s/JPEGImages/%s.jpg\n' % (wd, year, image_id)
#             list_file.write(line.replace("\\", '/'))
#             convert_annotation(year, image_id)
#         list_file.close()

