package team.a9043.sign_in_system.pojo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SisSignInExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public SisSignInExample() {
        oredCriteria = new ArrayList<Criteria>();
    }

    public void setOrderByClause(String orderByClause) {
        this.orderByClause = orderByClause;
    }

    public String getOrderByClause() {
        return orderByClause;
    }

    public void setDistinct(boolean distinct) {
        this.distinct = distinct;
    }

    public boolean isDistinct() {
        return distinct;
    }

    public List<Criteria> getOredCriteria() {
        return oredCriteria;
    }

    public void or(Criteria criteria) {
        oredCriteria.add(criteria);
    }

    public Criteria or() {
        Criteria criteria = createCriteriaInternal();
        oredCriteria.add(criteria);
        return criteria;
    }

    public Criteria createCriteria() {
        Criteria criteria = createCriteriaInternal();
        if (oredCriteria.size() == 0) {
            oredCriteria.add(criteria);
        }
        return criteria;
    }

    protected Criteria createCriteriaInternal() {
        Criteria criteria = new Criteria();
        return criteria;
    }

    public void clear() {
        oredCriteria.clear();
        orderByClause = null;
        distinct = false;
    }

    protected abstract static class GeneratedCriteria {
        protected List<Criterion> criteria;

        protected GeneratedCriteria() {
            super();
            criteria = new ArrayList<Criterion>();
        }

        public boolean isValid() {
            return criteria.size() > 0;
        }

        public List<Criterion> getAllCriteria() {
            return criteria;
        }

        public List<Criterion> getCriteria() {
            return criteria;
        }

        protected void addCriterion(String condition) {
            if (condition == null) {
                throw new RuntimeException("Value for condition cannot be null");
            }
            criteria.add(new Criterion(condition));
        }

        protected void addCriterion(String condition, Object value, String property) {
            if (value == null) {
                throw new RuntimeException("Value for " + property + " cannot be null");
            }
            criteria.add(new Criterion(condition, value));
        }

        protected void addCriterion(String condition, Object value1, Object value2, String property) {
            if (value1 == null || value2 == null) {
                throw new RuntimeException("Between values for " + property + " cannot be null");
            }
            criteria.add(new Criterion(condition, value1, value2));
        }

        public Criteria andSsiIdIsNull() {
            addCriterion("ssi_id is null");
            return (Criteria) this;
        }

        public Criteria andSsiIdIsNotNull() {
            addCriterion("ssi_id is not null");
            return (Criteria) this;
        }

        public Criteria andSsiIdEqualTo(Integer value) {
            addCriterion("ssi_id =", value, "ssiId");
            return (Criteria) this;
        }

        public Criteria andSsiIdNotEqualTo(Integer value) {
            addCriterion("ssi_id <>", value, "ssiId");
            return (Criteria) this;
        }

        public Criteria andSsiIdGreaterThan(Integer value) {
            addCriterion("ssi_id >", value, "ssiId");
            return (Criteria) this;
        }

        public Criteria andSsiIdGreaterThanOrEqualTo(Integer value) {
            addCriterion("ssi_id >=", value, "ssiId");
            return (Criteria) this;
        }

        public Criteria andSsiIdLessThan(Integer value) {
            addCriterion("ssi_id <", value, "ssiId");
            return (Criteria) this;
        }

        public Criteria andSsiIdLessThanOrEqualTo(Integer value) {
            addCriterion("ssi_id <=", value, "ssiId");
            return (Criteria) this;
        }

        public Criteria andSsiIdIn(List<Integer> values) {
            addCriterion("ssi_id in", values, "ssiId");
            return (Criteria) this;
        }

        public Criteria andSsiIdNotIn(List<Integer> values) {
            addCriterion("ssi_id not in", values, "ssiId");
            return (Criteria) this;
        }

        public Criteria andSsiIdBetween(Integer value1, Integer value2) {
            addCriterion("ssi_id between", value1, value2, "ssiId");
            return (Criteria) this;
        }

        public Criteria andSsiIdNotBetween(Integer value1, Integer value2) {
            addCriterion("ssi_id not between", value1, value2, "ssiId");
            return (Criteria) this;
        }

        public Criteria andSsIdIsNull() {
            addCriterion("ss_id is null");
            return (Criteria) this;
        }

        public Criteria andSsIdIsNotNull() {
            addCriterion("ss_id is not null");
            return (Criteria) this;
        }

        public Criteria andSsIdEqualTo(Integer value) {
            addCriterion("ss_id =", value, "ssId");
            return (Criteria) this;
        }

        public Criteria andSsIdNotEqualTo(Integer value) {
            addCriterion("ss_id <>", value, "ssId");
            return (Criteria) this;
        }

        public Criteria andSsIdGreaterThan(Integer value) {
            addCriterion("ss_id >", value, "ssId");
            return (Criteria) this;
        }

        public Criteria andSsIdGreaterThanOrEqualTo(Integer value) {
            addCriterion("ss_id >=", value, "ssId");
            return (Criteria) this;
        }

        public Criteria andSsIdLessThan(Integer value) {
            addCriterion("ss_id <", value, "ssId");
            return (Criteria) this;
        }

        public Criteria andSsIdLessThanOrEqualTo(Integer value) {
            addCriterion("ss_id <=", value, "ssId");
            return (Criteria) this;
        }

        public Criteria andSsIdIn(List<Integer> values) {
            addCriterion("ss_id in", values, "ssId");
            return (Criteria) this;
        }

        public Criteria andSsIdNotIn(List<Integer> values) {
            addCriterion("ss_id not in", values, "ssId");
            return (Criteria) this;
        }

        public Criteria andSsIdBetween(Integer value1, Integer value2) {
            addCriterion("ss_id between", value1, value2, "ssId");
            return (Criteria) this;
        }

        public Criteria andSsIdNotBetween(Integer value1, Integer value2) {
            addCriterion("ss_id not between", value1, value2, "ssId");
            return (Criteria) this;
        }

        public Criteria andSsiWeekIsNull() {
            addCriterion("ssi_week is null");
            return (Criteria) this;
        }

        public Criteria andSsiWeekIsNotNull() {
            addCriterion("ssi_week is not null");
            return (Criteria) this;
        }

        public Criteria andSsiWeekEqualTo(Integer value) {
            addCriterion("ssi_week =", value, "ssiWeek");
            return (Criteria) this;
        }

        public Criteria andSsiWeekNotEqualTo(Integer value) {
            addCriterion("ssi_week <>", value, "ssiWeek");
            return (Criteria) this;
        }

        public Criteria andSsiWeekGreaterThan(Integer value) {
            addCriterion("ssi_week >", value, "ssiWeek");
            return (Criteria) this;
        }

        public Criteria andSsiWeekGreaterThanOrEqualTo(Integer value) {
            addCriterion("ssi_week >=", value, "ssiWeek");
            return (Criteria) this;
        }

        public Criteria andSsiWeekLessThan(Integer value) {
            addCriterion("ssi_week <", value, "ssiWeek");
            return (Criteria) this;
        }

        public Criteria andSsiWeekLessThanOrEqualTo(Integer value) {
            addCriterion("ssi_week <=", value, "ssiWeek");
            return (Criteria) this;
        }

        public Criteria andSsiWeekIn(List<Integer> values) {
            addCriterion("ssi_week in", values, "ssiWeek");
            return (Criteria) this;
        }

        public Criteria andSsiWeekNotIn(List<Integer> values) {
            addCriterion("ssi_week not in", values, "ssiWeek");
            return (Criteria) this;
        }

        public Criteria andSsiWeekBetween(Integer value1, Integer value2) {
            addCriterion("ssi_week between", value1, value2, "ssiWeek");
            return (Criteria) this;
        }

        public Criteria andSsiWeekNotBetween(Integer value1, Integer value2) {
            addCriterion("ssi_week not between", value1, value2, "ssiWeek");
            return (Criteria) this;
        }

        public Criteria andSsiCreateTimeIsNull() {
            addCriterion("ssi_create_time is null");
            return (Criteria) this;
        }

        public Criteria andSsiCreateTimeIsNotNull() {
            addCriterion("ssi_create_time is not null");
            return (Criteria) this;
        }

        public Criteria andSsiCreateTimeEqualTo(LocalDateTime value) {
            addCriterion("ssi_create_time =", value, "ssiCreateTime");
            return (Criteria) this;
        }

        public Criteria andSsiCreateTimeNotEqualTo(LocalDateTime value) {
            addCriterion("ssi_create_time <>", value, "ssiCreateTime");
            return (Criteria) this;
        }

        public Criteria andSsiCreateTimeGreaterThan(LocalDateTime value) {
            addCriterion("ssi_create_time >", value, "ssiCreateTime");
            return (Criteria) this;
        }

        public Criteria andSsiCreateTimeGreaterThanOrEqualTo(LocalDateTime value) {
            addCriterion("ssi_create_time >=", value, "ssiCreateTime");
            return (Criteria) this;
        }

        public Criteria andSsiCreateTimeLessThan(LocalDateTime value) {
            addCriterion("ssi_create_time <", value, "ssiCreateTime");
            return (Criteria) this;
        }

        public Criteria andSsiCreateTimeLessThanOrEqualTo(LocalDateTime value) {
            addCriterion("ssi_create_time <=", value, "ssiCreateTime");
            return (Criteria) this;
        }

        public Criteria andSsiCreateTimeIn(List<LocalDateTime> values) {
            addCriterion("ssi_create_time in", values, "ssiCreateTime");
            return (Criteria) this;
        }

        public Criteria andSsiCreateTimeNotIn(List<LocalDateTime> values) {
            addCriterion("ssi_create_time not in", values, "ssiCreateTime");
            return (Criteria) this;
        }

        public Criteria andSsiCreateTimeBetween(LocalDateTime value1, LocalDateTime value2) {
            addCriterion("ssi_create_time between", value1, value2, "ssiCreateTime");
            return (Criteria) this;
        }

        public Criteria andSsiCreateTimeNotBetween(LocalDateTime value1, LocalDateTime value2) {
            addCriterion("ssi_create_time not between", value1, value2, "ssiCreateTime");
            return (Criteria) this;
        }

        public Criteria andSsiAttRateIsNull() {
            addCriterion("ssi_att_rate is null");
            return (Criteria) this;
        }

        public Criteria andSsiAttRateIsNotNull() {
            addCriterion("ssi_att_rate is not null");
            return (Criteria) this;
        }

        public Criteria andSsiAttRateEqualTo(Double value) {
            addCriterion("ssi_att_rate =", value, "ssiAttRate");
            return (Criteria) this;
        }

        public Criteria andSsiAttRateNotEqualTo(Double value) {
            addCriterion("ssi_att_rate <>", value, "ssiAttRate");
            return (Criteria) this;
        }

        public Criteria andSsiAttRateGreaterThan(Double value) {
            addCriterion("ssi_att_rate >", value, "ssiAttRate");
            return (Criteria) this;
        }

        public Criteria andSsiAttRateGreaterThanOrEqualTo(Double value) {
            addCriterion("ssi_att_rate >=", value, "ssiAttRate");
            return (Criteria) this;
        }

        public Criteria andSsiAttRateLessThan(Double value) {
            addCriterion("ssi_att_rate <", value, "ssiAttRate");
            return (Criteria) this;
        }

        public Criteria andSsiAttRateLessThanOrEqualTo(Double value) {
            addCriterion("ssi_att_rate <=", value, "ssiAttRate");
            return (Criteria) this;
        }

        public Criteria andSsiAttRateIn(List<Double> values) {
            addCriterion("ssi_att_rate in", values, "ssiAttRate");
            return (Criteria) this;
        }

        public Criteria andSsiAttRateNotIn(List<Double> values) {
            addCriterion("ssi_att_rate not in", values, "ssiAttRate");
            return (Criteria) this;
        }

        public Criteria andSsiAttRateBetween(Double value1, Double value2) {
            addCriterion("ssi_att_rate between", value1, value2, "ssiAttRate");
            return (Criteria) this;
        }

        public Criteria andSsiAttRateNotBetween(Double value1, Double value2) {
            addCriterion("ssi_att_rate not between", value1, value2, "ssiAttRate");
            return (Criteria) this;
        }
    }

    public static class Criteria extends GeneratedCriteria {

        protected Criteria() {
            super();
        }
    }

    public static class Criterion {
        private String condition;

        private Object value;

        private Object secondValue;

        private boolean noValue;

        private boolean singleValue;

        private boolean betweenValue;

        private boolean listValue;

        private String typeHandler;

        public String getCondition() {
            return condition;
        }

        public Object getValue() {
            return value;
        }

        public Object getSecondValue() {
            return secondValue;
        }

        public boolean isNoValue() {
            return noValue;
        }

        public boolean isSingleValue() {
            return singleValue;
        }

        public boolean isBetweenValue() {
            return betweenValue;
        }

        public boolean isListValue() {
            return listValue;
        }

        public String getTypeHandler() {
            return typeHandler;
        }

        protected Criterion(String condition) {
            super();
            this.condition = condition;
            this.typeHandler = null;
            this.noValue = true;
        }

        protected Criterion(String condition, Object value, String typeHandler) {
            super();
            this.condition = condition;
            this.value = value;
            this.typeHandler = typeHandler;
            if (value instanceof List<?>) {
                this.listValue = true;
            } else {
                this.singleValue = true;
            }
        }

        protected Criterion(String condition, Object value) {
            this(condition, value, null);
        }

        protected Criterion(String condition, Object value, Object secondValue, String typeHandler) {
            super();
            this.condition = condition;
            this.value = value;
            this.secondValue = secondValue;
            this.typeHandler = typeHandler;
            this.betweenValue = true;
        }

        protected Criterion(String condition, Object value, Object secondValue) {
            this(condition, value, secondValue, null);
        }
    }
}