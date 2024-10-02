package org.chaostocosmos.leap.spring.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "USERS")
/**
 * Users Entity
 * 
 * @author 9ins
 */
public class Users {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    /**
     * ID
     */
    long id;
    
    @Column(name = "name")
    /**
     * User name
     */
    String name;

    /**
     * Age
     */
    int age;

    /**
     * Address
     */
    String address;

    /**
     * Job
     */
    String job;

    /**
     * Default constructor
     */
    public Users() {
    }

    /**
     * Constructs with name, age, address, job
     * @param name
     * @param age
     * @param address
     * @param job
     */
    public Users(String name, int age, String address, String job) {
        this.name = name;
        this.age = age;
        this.address = address;
        this.job = job;
    }

    /**
     * Get entity ID
     * @return
     */
    public long getId() {
        return this.id;
    }

    /**
     * Set entity ID
     * @param id
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Get user name
     * @return
     */
    public String getName() {
        return this.name;
    }

    /**
     * Set user name
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get user age
     * @return
     */
    public int getAge() {
        return this.age;
    }

    /**
     * Set user age
     * @param age
     */
    public void setAge(int age) {
        this.age = age;
    }

    /**
     * Get user address
     * @return
     */
    public String getAddress() {
        return this.address;
    }

    /**
     * Set user address
     * @param address
     */
    public void setAddress(String address) {
        this.address = address;
    }

    /**
     * Get user job
     * @return
     */
    public String getJob() {
        return this.job;
    }

    /**
     * Set user job
     * @param job
     */
    public void setJob(String job) {
        this.job = job;
    }

    @Override
    public String toString() {
        return "{" +
            " id='" + id + "'" +
            ", name='" + name + "'" +
            ", age='" + age + "'" +
            ", address='" + address + "'" +
            ", job='" + job + "'" +
            "}";
    }
}
