package com.hs.fastService.entities.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hs.fastService.ErrorMsg;
import com.hs.fastService.entities.PageInfo;
import org.springframework.data.domain.Page;

import java.io.Serializable;
import java.util.Collection;

@JsonIgnoreProperties(
        value = {
                "hibernateLazyInitializer", "handler", "fieldHandler"
        }
)
public class BaseResponse implements Serializable {
    private int respCode = 0;
    private String respMsg = "SUCCESS";
    private Collection list;
    private PageInfo pageInfo;
    private Object info;

    public static BaseResponse errorResponse(ErrorMsg error) {
        return new BaseResponse(error);
    }

    public static BaseResponse errorResponse(int errorCode, String message) {
        return new BaseResponse(errorCode, message);
    }

    public BaseResponse(){
    }

    public BaseResponse(ErrorMsg error) {
        this(error.getCode(), error.getMessage());
    }

    public BaseResponse(Object info) {
        this.info = info;
    }

    public BaseResponse(Collection list) {
        this.list = list;
    }

    public BaseResponse(int errorCode, String message){
        this.respCode = errorCode;
        this.respMsg = message;
    }

    public int getRespCode() {
        return respCode;
    }

    public void setRespCode(int code) {
        this.respCode = code;
    }

    public String getRespMsg() {
        return respMsg;
    }

    public void setRespMsg(String respMsg) {
        this.respMsg = respMsg;
    }

    public Collection getList() {
        return list;
    }

    public void setList(Collection list) {
        this.list = list;
    }

    public PageInfo getPageInfo() {
        return pageInfo;
    }

    public void setPageInfo(PageInfo pageInfo) {
        this.pageInfo = pageInfo;
    }

    public Object getInfo() {
        return info;
    }

    public void setInfo(Object info) {
        this.info = info;
    }

    public static BaseResponse fromPage(Page page) {
        BaseResponse response = new BaseResponse();
        if (page == null) {
            return response;
        }

        PageInfo pageInfo = new PageInfo();
        pageInfo.setTotalPages(page.getTotalPages());
        pageInfo.setPageNumber(page.getNumber());
        pageInfo.setCount(page.getTotalElements());
        pageInfo.setPageSize(page.getNumberOfElements());

        response.setPageInfo(pageInfo);
        response.setList(page.getContent());
        return response;
    }
}
