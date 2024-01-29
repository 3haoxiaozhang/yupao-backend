package com.yupi.yupao.once;

import com.alibaba.excel.EasyExcel;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 导入星球用户到数据库
 */
public class ImportXingQiuUser {
    public static void main(String[] args) {

        String fileName = "C:\\Users\\张家霖\\IdeaProjects\\yupao-backend\\src\\main\\resources\\testExcel.xlsx";
        List<XingQiuTableUserInfo> userInfoslist = EasyExcel.read(fileName).head(XingQiuTableUserInfo.class).sheet().doReadSync();
        for (XingQiuTableUserInfo xingQiuTableUserInfo : userInfoslist) {
            System.out.println(xingQiuTableUserInfo);
        }

        Map<String, List<XingQiuTableUserInfo>> listMap = userInfoslist.stream().
                filter(item -> StringUtils.isNotEmpty(item.getUserName()))
                .collect(Collectors.groupingBy(xingQiuTableUserInfo -> xingQiuTableUserInfo.getUserName()));
        for (Map.Entry<String, List<XingQiuTableUserInfo>> stringListEntry : listMap.entrySet()) {
              if(stringListEntry.getValue().size()>1){
                  System.out.println("user="  +stringListEntry.getKey());

              }
        }

        System.out.println("不重复昵称数="+listMap.keySet().size());

    }
}
