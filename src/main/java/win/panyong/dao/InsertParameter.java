package win.panyong.dao;

/**
 * Created by pan on 2018/12/18 10:25 AM
 */
public class InsertParameter {
    private String id;
    private String table;
    private String columns;
    private String values;

    public String getId() {
        return id;
    }

    public InsertParameter setId(String id) {
        this.id = id;
        return this;
    }

    public String getTable() {
        return table;
    }

    public InsertParameter setTable(String table) {
        this.table = table;
        return this;
    }

    public String getColumns() {
        return columns;
    }

    public InsertParameter setColumns(String columns) {
        this.columns = columns;
        return this;
    }

    public String getValues() {
        return values;
    }

    public InsertParameter setValues(String values) {
        this.values = values;
        return this;
    }
}
