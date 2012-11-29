package com.github.jmkgreen.morphia.query;

import com.mongodb.DBObject;
import org.bson.types.CodeWScope;

public class WhereCriteria extends AbstractCriteria implements Criteria {

    private Object js;

    public WhereCriteria(String js) {
        this.js = js;
    }

    public WhereCriteria(CodeWScope js) {
        this.js = js;
    }

    public void addTo(DBObject obj) {
        obj.put(FilterOperator.WHERE.val(), this.js);
    }

    public String getFieldName() {
        return FilterOperator.WHERE.val();
    }

}
