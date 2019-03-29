package com.hs.fastService.entities;

public class PageInfo {
    public static final int DEFAULT_PAGE_SIZE = 10;

    // 每页有多少条 (返回时表示当前返回了多少条数据)
    private int pageSize = DEFAULT_PAGE_SIZE;
    // 当前页
    private int pageNumber = 0;
    // 总页数
    private int totalPages;
    // 数据总条数
    private long count;

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }
}

