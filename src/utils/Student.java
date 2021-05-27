package utils;

public class Student{
	public String name;
	public int age;

	public Student(String n, int a){
		this.name = n;
		this.age = a;
	}

	public String toString(){
		return this.name+": "+this.age;
	}
}
