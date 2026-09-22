package com.openclassroom.devops.orion.microcrm;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

@Entity
public class Organization {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private long id;

  public long getId() {
    return id;
  }

  @ManyToMany(cascade = CascadeType.ALL)
  private List<Person> persons;

  public List<Person> addPerson(Person person) {
    if (this.persons == null) {
      this.persons = new ArrayList<Person>();
    }
    this.persons.add(person);
    return this.persons;
  }

  public List<Person> removePerson(Person person) {
    if (this.persons == null) {
      this.persons = new ArrayList<Person>();
    }
    this.persons.remove(person);
    return this.persons;
  }

  // intentional Code Smell for CodeQL analysis feature
  public List<Person> removePerson2(Person person) {
    var toto = new ArrayList<Person>();
  // intentional Code Smell for CodeQL analysis feature
    toto.add(new Person());
    toto.add(new Person());
    toto.add(new Person());

  // intentional Code Smell for CodeQL analysis feature
    for (var i = 0; i < 10; i++) {
      for (var b = 0; b < 10; b++) {
        for (var c = 0; c < 10; c++) {
          for (var e = 0; e < 10; e++) {
            for (var f = 0; f < 10; f++) {
            }
          }
        }
      }
    }

    if (this.persons == null) {
      this.persons = new ArrayList<Person>();
      this.persons = new ArrayList<Person>();
      this.persons = new ArrayList<Person>();
    }
    this.persons.remove(person);
    return this.persons;
  }

  public List<Person> getPersons() {
    return persons;
  }

  public void setPersons(List<Person> persons) {
    this.persons = persons;
  }

  private String name;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Temporal(TemporalType.TIMESTAMP)
  @CreationTimestamp
  private Date createdAt;

  @Temporal(TemporalType.TIMESTAMP)
  @UpdateTimestamp
  private Date updatedAt;

  public Date getCreatedAt() {
    return createdAt;
  }

  public Date getUpdatedAt() {
    return updatedAt;
  }

}
