package org.mhb.parser;

import java.util.Comparator;
import java.util.Objects;

public class RecordDetail implements Comparable<RecordDetail> {

    private String name;
    private String type;
    private Long minimumValue;
    private Long maximumValue;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getMinimumValue() {
        return minimumValue;
    }

    public void setMinimumValue(Long minimumValue) {
        this.minimumValue = minimumValue;
    }

    public Long getMaximumValue() {
        return maximumValue;
    }

    public void setMaximumValue(Long maximumValue) {
        this.maximumValue = maximumValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RecordDetail that = (RecordDetail) o;
        return name.equals(that.name) &&
                type.equals(that.type) &&
                Objects.equals(minimumValue, that.minimumValue) &&
                Objects.equals(maximumValue, that.maximumValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, type);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("RecordDetail [");
        sb.append("name='").append(name).append("'");
        sb.append(", type='").append(type).append("'");
        sb.append(", minimumValue=").append(minimumValue);
        sb.append(", maximumValue=").append(maximumValue);
        sb.append(']');
        return sb.toString();
    }

    @Override
    public int compareTo(RecordDetail o) {
        return Comparator.nullsLast(Comparator.comparing(RecordDetail::getMaximumValue))
                .compare(this, o);
    }
}
