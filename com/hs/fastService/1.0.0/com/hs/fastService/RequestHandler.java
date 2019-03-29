package com.hs.fastService;

import com.hs.fastService.entities.response.BaseResponse;

import java.util.Map;

/**
 * 自定义处理请求参数和返回参数
 */
public interface RequestHandler {

    /**
     * 处理请求参数
     */
    default Map handleRequest(Map requestParams, RequestConfig config) {
        return requestParams;
    }

    /**
     * 处理返回参数
     * @return 如果返回的是 String 类型则直接返回给客户端，其他类型则自动包装成BaseResponse 格式化成json再返回
     */
    default Object handleResponse(BaseResponse response, RequestConfig config) {
        return response;
    }

}
