package name.hergeth.baseservice.responses;

import java.util.Collection;

public class ObjectResponse<T> {
    T objekt;
    Integer pos;
    Integer total_count;

    public ObjectResponse(T objekt, Integer pos, Integer total_count) {
        this.objekt = objekt;
        this.pos = pos;
        this.total_count = total_count;
    }

    public ObjectResponse(T objekt) {
        this.objekt = objekt;
        this.pos = 0;
        this.total_count = objekt instanceof Collection ? ((Collection)objekt).size() : 100000;
    }

    public T getObjekt() {
        return objekt;
    }

    public void setObjekt(T objekt) {
        this.objekt = objekt;
    }

    public Integer getPos() {
        return pos;
    }

    public void setPos(Integer pos) {
        this.pos = pos;
    }

    public Integer getTotal_count() {
        return total_count;
    }

    public void setTotal_count(Integer total_count) {
        this.total_count = total_count;
    }

    @Override
    public String toString() {
        return "ObjectResponse{" +
                "objekt=" + objekt +
                ", pos=" + pos +
                ", total_count=" + total_count +
                '}';
    }
}
