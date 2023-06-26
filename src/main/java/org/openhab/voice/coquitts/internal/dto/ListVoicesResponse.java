package org.openhab.voice.coquitts.internal.dto;

import java.util.List;

public class ListVoicesResponse {
    private int count;
    private boolean has_prev;
    private boolean has_next;
    private List<Person> result;

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public boolean isHas_prev() {
        return has_prev;
    }

    public void setHas_prev(boolean has_prev) {
        this.has_prev = has_prev;
    }

    public boolean isHas_next() {
        return has_next;
    }

    public void setHas_next(boolean has_next) {
        this.has_next = has_next;
    }

    public List<Person> getResult() {
        return result;
    }

    public void setResult(List<Person> result) {
        this.result = result;
    }

    @Override
    public String toString() {
        return "ListVoicesResponse{" + "count=" + count + ", has_prev=" + has_prev + ", has_next=" + has_next + ", result="
                + result + '}';
    }

    public class Person {
        private String id;
        private String name;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return "Person{" + "id='" + id + '\'' + ", name='" + name + '\'' + '}';
        }
    }
}