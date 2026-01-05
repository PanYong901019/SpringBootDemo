package win.panyong.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Page<T> {
    /**
     * 开始行
     */
    private final int since = 0;
    /**
     * 当前页
     */
    private int page = 1;
    /**
     * 每页条数
     */
    private int size = 10;
    /**
     * 共多少条
     */
    private int totalCount;
    /**
     * 共多少页
     */
    private int totalPage;
    /**
     * 查询条件
     */
    private Map<String, String> search = new HashMap<>();
    /**
     * 数据
     */
    private List<T> data;

    public Page() {
    }

    public Page(int page) {
        this.page = page;
    }

    public Page(int page, int size) {
        this.page = page;
        this.size = size;
    }

    public Page(int page, int size, Map<String, String> search) {
        this.page = page;
        this.size = size;
        this.search = search;
    }

    public int getPage() {
        return page;
    }

    public Page<T> setPage(int page) {
        this.page = page;
        return this;
    }

    public int getSince() {
        return (page - 1) * size;
    }

    public int getSize() {
        return size;
    }

    public Page<T> setSize(int size) {
        this.size = size;
        return this;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public Page<T> setTotalCount(int totalCount) {
        this.totalCount = totalCount;
        this.totalPage = (totalCount + this.size - 1) / this.size;
        return this;
    }

    public int getTotalPage() {
        return totalPage;
    }

    public Map<String, String> getSearch() {
        return search;
    }

    public Page<T> setSearch(Map<String, String> search) {
        this.search = search;
        return this;
    }

    public Page<T> putSearch(String key, String value) {
        this.search.put(key, value);
        return this;
    }

    public List<T> getData() {
        return data;
    }

    public Page<T> setData(List<T> data) {
        this.data = data;
        return this;
    }
}
