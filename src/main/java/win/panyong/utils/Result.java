package win.panyong.utils;

import com.alibaba.fastjson2.JSONObject;

public class Result {
    private final Integer rspCode;
    private final String rspInfo;
    private final JSONObject rspResult;

    private Result(Builder builder) {
        this.rspCode = builder.rspCode;
        this.rspInfo = builder.rspInfo;
        this.rspResult = builder.rspResult;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder ok() {
        return new Builder().rspCode(1).rspInfo("success");
    }

    public static Builder ok(JSONObject rspResult) {
        return new Builder().rspCode(1).rspInfo("success").setData(rspResult);
    }

    public static Builder fail() {
        return new Builder().rspCode(0).rspInfo("fail");
    }

    public static Builder fail(String rspInfo) {
        return new Builder().rspCode(0).rspInfo(rspInfo);
    }

    public static Builder fail(Integer rspCode, String rspInfo) {
        return new Builder().rspCode(rspCode).rspInfo(rspInfo);
    }

    private JSONObject getResult() {
        return new JSONObject().fluentPut("rspCode", rspCode).fluentPut("rspInfo", rspInfo).fluentPut("rspResult", rspResult);
    }

    @Override
    public String toString() {
        return getResult().toJSONString();
    }

    public static class Builder {
        private Integer rspCode = 0;
        private String rspInfo = "fail";
        private JSONObject rspResult = new JSONObject();

        private Builder() {
        }

        public Builder rspCode(Integer rspCode) {
            this.rspCode = rspCode;
            return this;
        }

        public Builder rspInfo(String rspInfo) {
            this.rspInfo = rspInfo;
            return this;
        }

        public Builder putData(String key, Object value) {
            this.rspResult.put(key, value);
            return this;
        }

        public Builder setData(Object rspResult) {
            this.rspResult = ObjectUtil.objectToJSONObject(rspResult);
            return this;
        }

        public Result build() {
            return new Result(this);
        }

        public String buildJsonString() {
            return this.build().toString();
        }

        public String buildJsonString(Object extParam) {
            this.rspResult.putAll(ObjectUtil.objectToJSONObject(extParam));
            return buildJsonString();
        }

    }
}



