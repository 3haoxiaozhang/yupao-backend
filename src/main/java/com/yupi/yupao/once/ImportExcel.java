package com.yupi.yupao.once;

import com.alibaba.excel.EasyExcel;

import java.util.List;

/**
 * 导入Excel 数据
 */
public class ImportExcel {

    public static void main(String[] args)
        {
        // 写法1：JDK8+ ,不用额外写一个DemoDataListener
        // since: 3.0.0-beta1
        String fileName = "C:\\Users\\张家霖\\IdeaProjects\\yupao-backend\\src\\main\\resources\\testExcel.xlsx";

        EasyExcel.read(fileName, XingQiuTableUserInfo.class, new TableListener()).sheet().doRead();

       // readByListener(fileName);
            synchronousRead(fileName);
    }


    public static void readByListener(String fileName){
        EasyExcel.read(fileName, XingQiuTableUserInfo.class, new TableListener()).sheet().doRead();
    }

    public static void synchronousRead(String fileName){

        List<XingQiuTableUserInfo> list = EasyExcel.read(fileName).head(XingQiuTableUserInfo.class).sheet().doReadSync();
        for (XingQiuTableUserInfo xingQiuTableUserInfo : list) {
            System.out.println(xingQiuTableUserInfo);
        }
    }
}
