package com.vinsguru.students;

import com.vinsguru.students.dto.StudentRequest;
import com.vinsguru.students.dto.StudentResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Transactional
class StudentControllerIntegrationTest {

    @LocalServerPort
    private int port;

    private RestClient restClient;

    @BeforeEach
    void setUp() {
        restClient = RestClient.builder()
                .baseUrl("http://localhost:" + port)
                .build();
    }

    private ResponseEntity<StudentResponse> createStudent(String name, Integer age, String studentClass) {
        StudentRequest request = new StudentRequest(name, age, studentClass);
        return restClient.post()
                .uri("/api/students")
                .body(request)
                .retrieve()
                .toEntity(StudentResponse.class);
    }

    @Test
    @DisplayName("Should create a student")
    void shouldCreateStudent() {
        ResponseEntity<StudentResponse> response = createStudent("Alice", 12, "6A");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().id()).isNotNull();
        assertThat(response.getBody().name()).isEqualTo("Alice");
        assertThat(response.getBody().age()).isEqualTo(12);
        assertThat(response.getBody().studentClass()).isEqualTo("6A");
    }

    @Test
    @DisplayName("Should return 400 ProblemDetail when creating a student with invalid data")
    void shouldReturn400WhenCreatingInvalidStudent() {
        StudentRequest request = new StudentRequest("", null, "");

        assertThatThrownBy(() -> restClient.post()
                .uri("/api/students")
                .body(request)
                .retrieve()
                .toEntity(StudentResponse.class))
                .isInstanceOf(RestClientResponseException.class)
                .satisfies(ex -> assertThat(((RestClientResponseException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    @DisplayName("Should return all students")
    void shouldReturnAllStudents() {
        createStudent("Bob", 14, "8B");
        createStudent("Carol", 15, "9A");

        ResponseEntity<StudentResponse[]> response = restClient.get()
                .uri("/api/students")
                .retrieve()
                .toEntity(StudentResponse[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(List.of(response.getBody())).hasSizeGreaterThanOrEqualTo(2);
    }

    @Test
    @DisplayName("Should return a student by id")
    void shouldReturnStudentById() {
        Long id = createStudent("Dave", 16, "10A").getBody().id();

        ResponseEntity<StudentResponse> response = restClient.get()
                .uri("/api/students/{id}", id)
                .retrieve()
                .toEntity(StudentResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().name()).isEqualTo("Dave");
    }

    @Test
    @DisplayName("Should return 404 ProblemDetail when student id does not exist")
    void shouldReturn404WhenStudentNotFound() {
        assertThatThrownBy(() -> restClient.get()
                .uri("/api/students/{id}", 99999)
                .retrieve()
                .toEntity(StudentResponse.class))
                .isInstanceOf(RestClientResponseException.class)
                .satisfies(ex -> assertThat(((RestClientResponseException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    @DisplayName("Should update an existing student")
    void shouldUpdateStudent() {
        Long id = createStudent("Eve", 13, "7C").getBody().id();
        StudentRequest updateRequest = new StudentRequest("Eve Updated", 14, "8C");

        ResponseEntity<StudentResponse> response = restClient.put()
                .uri("/api/students/{id}", id)
                .body(updateRequest)
                .retrieve()
                .toEntity(StudentResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().name()).isEqualTo("Eve Updated");
        assertThat(response.getBody().age()).isEqualTo(14);
        assertThat(response.getBody().studentClass()).isEqualTo("8C");
    }

    @Test
    @DisplayName("Should return 404 ProblemDetail when updating a student that does not exist")
    void shouldReturn404WhenUpdatingNonExistentStudent() {
        StudentRequest updateRequest = new StudentRequest("Ghost", 20, "12A");

        assertThatThrownBy(() -> restClient.put()
                .uri("/api/students/{id}", 99999)
                .body(updateRequest)
                .retrieve()
                .toEntity(StudentResponse.class))
                .isInstanceOf(RestClientResponseException.class)
                .satisfies(ex -> assertThat(((RestClientResponseException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    @DisplayName("Should delete an existing student")
    void shouldDeleteStudent() {
        Long id = createStudent("Frank", 17, "11A").getBody().id();

        ResponseEntity<Void> deleteResponse = restClient.delete()
                .uri("/api/students/{id}", id)
                .retrieve()
                .toBodilessEntity();
        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        assertThatThrownBy(() -> restClient.get()
                .uri("/api/students/{id}", id)
                .retrieve()
                .toEntity(StudentResponse.class))
                .isInstanceOf(RestClientResponseException.class)
                .satisfies(ex -> assertThat(((RestClientResponseException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    @DisplayName("Should return 404 ProblemDetail when deleting a student that does not exist")
    void shouldReturn404WhenDeletingNonExistentStudent() {
        assertThatThrownBy(() -> restClient.delete()
                .uri("/api/students/{id}", 99999)
                .retrieve()
                .toBodilessEntity())
                .isInstanceOf(RestClientResponseException.class)
                .satisfies(ex -> assertThat(((RestClientResponseException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    @DisplayName("Should include RFC 7807 ProblemDetail body when student is not found")
    void shouldReturnProblemDetailBodyWhenNotFound() {
        assertThatThrownBy(() -> restClient.get()
                .uri("/api/students/{id}", 99999)
                .retrieve()
                .toEntity(StudentResponse.class))
                .isInstanceOf(RestClientResponseException.class)
                .satisfies(ex -> {
                    ProblemDetail problemDetail = ((RestClientResponseException) ex).getResponseBodyAs(ProblemDetail.class);
                    assertThat(problemDetail).isNotNull();
                    assertThat(problemDetail.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
                    assertThat(problemDetail.getTitle()).isEqualTo("Student Not Found");
                });
    }

}
