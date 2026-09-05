package com.vinsguru.students.mapper;

import com.vinsguru.students.dto.StudentRequest;
import com.vinsguru.students.dto.StudentResponse;
import com.vinsguru.students.entity.Student;
import org.springframework.stereotype.Component;

@Component
public class StudentMapper {

    public Student toEntity(StudentRequest request) {
        return new Student(request.name(), request.age(), request.studentClass());
    }

    public StudentResponse toResponse(Student student) {
        return new StudentResponse(student.getId(), student.getName(), student.getAge(), student.getStudentClass());
    }

}
