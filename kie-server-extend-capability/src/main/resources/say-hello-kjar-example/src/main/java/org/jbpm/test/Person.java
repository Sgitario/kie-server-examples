package org.jbpm.test;

public class Person  implements java.io.Serializable {

    static final long serialVersionUID = 1L;
    
    private Integer age;
    
    private String name;

    public Person() {
    }

    public Person(String name, Integer age) {
        this.name = name;
        this.age = age;
    }
    
    public Integer getAge() {
        return this.age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }
    
    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }
}