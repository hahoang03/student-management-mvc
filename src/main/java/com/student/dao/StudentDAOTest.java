package com.student.dao;

import com.student.model.Student;
import java.util.List;

public class StudentDAOTest {

    public static void main(String[] args) {
        StudentDAO dao = new StudentDAO();
        List<Student> results = dao.searchStudents("");

        System.out.println("Found " + results.size() + " students");
        for (Student s : results) {
            System.out.println(s);  
        }
    }
}
