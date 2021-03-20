package tech.haonan.server.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel("统一响应返回类")
public class CommonResponse {

    @ApiModelProperty("响应码")
    private long responseCode;
    @ApiModelProperty("响应信息")
    private String message;
    @ApiModelProperty("响应数据")
    private Object data;


    /**
     * 成功返回 数据为空
     * @param msg  成功返回的信息提升
     * @return  统一返回类
     */
    public static CommonResponse success(String msg){
        return new CommonResponse(200, msg,null);
    }

    /**
     * 成功返回 存在返回数据
     * @param msg 成功返回的信息提升
     * @param obj 成功返回时携带的数据
     * @return  统一返回类
     */
    public static CommonResponse success(String msg,Object obj){
        return new CommonResponse(200, msg,obj);
    }

    /**
     * 失败返回
     * @param msg 失败原因
     * @return 公共返回 对象
     */
    public static CommonResponse error(String msg){
        return new CommonResponse(500, msg,null);
    }

}
