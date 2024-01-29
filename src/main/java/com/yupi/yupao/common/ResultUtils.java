package com.yupi.yupao.common;

/**
 * 返回工具类
 * @author 张家霖
 */
public class ResultUtils {

    /**
     * 成功
     * @param data
     * @param <T>
     * @return
     */
    public  static <T> BaseResponse<T> success(T data){
        return new BaseResponse<>(0,data,"ok");
    }

    /**
     * 失败
     * @param errorCode
     * @return
     */
    public static  BaseResponse error(ErrorCode errorCode){
        return new BaseResponse(errorCode.getCode(),null,errorCode.getMessage(),errorCode.getDescription());
    }

    /**
     * 失败
     * @param errorCode
     * @return
     */
    public static  BaseResponse error(ErrorCode errorCode,String message,String description){
        return new BaseResponse(errorCode.getCode(),null,message,description);
    }


    /**
     * 失败
     * @param errorCode
     * @return
     */
    public static  BaseResponse error(ErrorCode errorCode,String description){
        return new BaseResponse(errorCode.getCode(),errorCode.getMessage(),description);
    }

    /**
     * 失败
     * @param errorCode
     * @return
     */
    public static  BaseResponse error(int errorCode,String message,String description){
        return new BaseResponse(errorCode,null,message,description);
    }




}
