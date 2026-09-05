package com.vinsguru.students.service;

import com.vinsguru.students.dto.StudentRequest;
import com.vinsguru.students.dto.StudentResponse;
import com.vinsguru.students.entity.Student;
import com.vinsguru.students.exceptions.StudentNotFoundException;
import com.vinsguru.students.mapper.StudentMapper;
import com.vinsguru.students.repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class StudentService {

    private final StudentRepository studentRepository;
    private final StudentMapper studentMapper;

    public StudentService(StudentRepository studentRepository, StudentMapper studentMapper) {
        this.studentRepository = studentRepository;
        this.studentMapper = studentMapper;
    }

    public StudentResponse create(StudentRequest request) {
        Student student = studentMapper.toEntity(request);
        Student saved = studentRepository.save(student);
        return studentMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<StudentResponse> findAll() {
        return studentRepository.findAll()
                .stream()
                .map(studentMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public StudentResponse findById(Long id) {
        Student student = getStudentOrThrow(id);
        return studentMapper.toResponse(student);
    }

    public StudentResponse update(Long id, StudentRequest request) {
        Student student = getStudentOrThrow(id);
        student.setName(request.name());
        student.setAge(request.age());
        student.setStudentClass(request.studentClass());
        return studentMapper.toResponse(student);
    }

    public void delete(Long id) {
        Student student = getStudentOrThrow(id);
        studentRepository.delete(student);
    }

    private Student getStudentOrThrow(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new StudentNotFoundException(id));
    }

}
