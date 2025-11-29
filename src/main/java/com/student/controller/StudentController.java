package com.student.controller;

import com.student.dao.StudentDAO;
import com.student.model.Student;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

@WebServlet("/student")
public class StudentController extends HttpServlet {

    private StudentDAO studentDAO;

    @Override
    public void init() {
        studentDAO = new StudentDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getParameter("action");

        if (action == null) {
            action = "list";
        }

        switch (action) {
            case "new":
                showNewForm(request, response);
                break;
            case "edit":
                showEditForm(request, response);
                break;
            case "delete":
                deleteStudent(request, response);
                break;
            case "search":
                searchStudents(request, response); // <-- new line
                break;
            case "sort":
        sortStudents(request, response);
        break;
    case "filter":
        filterStudents(request, response);
        break;
            default:
                listStudents(request, response);
                break;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getParameter("action");

        switch (action) {
            case "insert":
                insertStudent(request, response);
                break;
            case "update":
                updateStudent(request, response);
                break;
        }
    }

    // List all students
   private void listStudents(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {

    // 1. Get current page
    String pageParam = request.getParameter("page");
    int currentPage = (pageParam != null) ? Integer.parseInt(pageParam) : 1;

    // 2. Records per page
    int recordsPerPage = 10;

    // 3. Get total records and calculate total pages
    int totalRecords = studentDAO.getTotalStudents();
    int totalPages = (int) Math.ceil((double) totalRecords / recordsPerPage);

    // Handle edge cases
    if (currentPage < 1) currentPage = 1;
    if (currentPage > totalPages) currentPage = totalPages;

    // 4. Calculate offset
    int offset = (currentPage - 1) * recordsPerPage;

    // 5. Get paginated students
    List<Student> students = studentDAO.getStudentsPaginated(offset, recordsPerPage);

    // 6. Set attributes
    request.setAttribute("students", students);
    request.setAttribute("currentPage", currentPage);
    request.setAttribute("totalPages", totalPages);

    // 7. Forward to JSP
    RequestDispatcher dispatcher = request.getRequestDispatcher("/views/student-list.jsp");
    dispatcher.forward(request, response);
}


    // Show form for new student
    private void showNewForm(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {

    request.setAttribute("action", "insert");  // THÊM DÒNG NÀY
    RequestDispatcher dispatcher = request.getRequestDispatcher("/views/student-form.jsp");
    dispatcher.forward(request, response);
}


    // Show form for editing student
   private void showEditForm(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {

    int id = Integer.parseInt(request.getParameter("id"));
    Student existingStudent = studentDAO.getStudentById(id);

    request.setAttribute("student", existingStudent);
    request.setAttribute("action", "update");  // THÊM DÒNG NÀY

    RequestDispatcher dispatcher = request.getRequestDispatcher("/views/student-form.jsp");
    dispatcher.forward(request, response);
}


    // Insert new student
    private void insertStudent(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Get parameters and create Student object
        String studentCode = request.getParameter("studentCode");
        String fullName = request.getParameter("fullName");
        String email = request.getParameter("email");
        String major = request.getParameter("major");

        Student student = new Student(studentCode, fullName, email, major);

        if (!validateStudent(student, request)) {
    request.setAttribute("student", student);
    request.setAttribute("action", "insert"); // <-- cần thêm dòng này
    RequestDispatcher dispatcher = request.getRequestDispatcher("/views/student-form.jsp");
    dispatcher.forward(request, response);
    return;
}


        // If valid, proceed with DAO insert
        if (studentDAO.addStudent(student)) {
            response.sendRedirect("student?action=list&message=Student added successfully");
        } else {
            response.sendRedirect("student?action=list&error=Failed to add student");
        }
    }

    private void updateStudent(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Get parameters and create Student object
        int id = Integer.parseInt(request.getParameter("id"));
        String studentCode = request.getParameter("studentCode");
        String fullName = request.getParameter("fullName");
        String email = request.getParameter("email");
        String major = request.getParameter("major");

        Student student = new Student(studentCode, fullName, email, major);
        student.setId(id);

        // Validate student
        if (!validateStudent(student, request)) {
    request.setAttribute("student", student);
    request.setAttribute("action", "update"); // <-- cần thêm dòng này
    RequestDispatcher dispatcher = request.getRequestDispatcher("/views/student-form.jsp");
    dispatcher.forward(request, response);
    return;
}

        // If valid, proceed with DAO update
        if (studentDAO.updateStudent(student)) {
            response.sendRedirect("student?action=list&message=Student updated successfully");
        } else {
            response.sendRedirect("student?action=list&error=Failed to update student");
        }
    }

    // Delete student
    private void deleteStudent(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        int id = Integer.parseInt(request.getParameter("id"));

        if (studentDAO.deleteStudent(id)) {
            response.sendRedirect("student?action=list&message=Student deleted successfully");
        } else {
            response.sendRedirect("student?action=list&error=Failed to delete student");
        }
    }

    private void searchStudents(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // 1. Lấy tham số keyword từ request
        String keyword = request.getParameter("keyword");

        // 2. Xác định gọi phương thức DAO nào
        List<Student> students;
        if (keyword == null || keyword.trim().isEmpty()) {
            // Nếu keyword rỗng hoặc null → hiển thị tất cả sinh viên
            students = studentDAO.getAllStudents();
        } else {
            // Nếu có keyword → gọi phương thức searchStudents() của DAO
            students = studentDAO.searchStudents(keyword.trim());
        }

        // 3. Thiết lập attribute cho request
        request.setAttribute("students", students); // danh sách sinh viên trả về
        request.setAttribute("keyword", keyword);   // giữ keyword để hiển thị trong ô tìm kiếm

        // 4. Chuyển tiếp (forward) đến JSP để hiển thị kết quả
        RequestDispatcher dispatcher = request.getRequestDispatcher("/views/student-list.jsp");
        dispatcher.forward(request, response);
    }

    private boolean validateStudent(Student student, HttpServletRequest request) {
        boolean isValid = true;

        //Validate Student Code
        String codePattern = "[A-Z]{2}[0-9]{3,}";
        String code = student.getStudentCode();
        if (code == null || code.trim().isEmpty()) {
            request.setAttribute("errorCode", "Student code is required");
            isValid = false;
        } else if (!code.matches(codePattern)) {
            request.setAttribute("errorCode", "Invalid format. Use 2 uppercase letters + 3+ digits (e.g., SV001, IT123)");
            isValid = false;
        }

        //Validate Full Name
        String fullName = student.getFullName();
        if (fullName == null || fullName.trim().isEmpty()) {
            request.setAttribute("errorName", "Full name is required");
            isValid = false;
        } else if (fullName.trim().length() < 2) {
            request.setAttribute("errorName", "Full name must be at least 2 characters");
            isValid = false;
        }

        //Validate Email 
        String email = student.getEmail();
        if (email != null && !email.trim().isEmpty()) {
            String emailPattern = "^[A-Za-z0-9+_.-]+@(.+)$";
            if (!email.matches(emailPattern)) {
                request.setAttribute("errorEmail", "Invalid email format");
                isValid = false;
            }
        }

        // Validate Major
        String major = student.getMajor();
        if (major == null || major.trim().isEmpty()) {
            request.setAttribute("errorMajor", "Major is required");
            isValid = false;
        }

        return isValid;
    }
    
    // Sort students by column
private void sortStudents(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {

    String sortBy = request.getParameter("sortBy");
    String order = request.getParameter("order");

    List<Student> students = studentDAO.getStudentsSorted(sortBy, order);

    request.setAttribute("students", students);
    request.setAttribute("sortBy", sortBy);
    request.setAttribute("order", order);
    request.setAttribute("selectedMajor", request.getParameter("major")); // giữ filter nếu muốn


    RequestDispatcher dispatcher = request.getRequestDispatcher("/views/student-list.jsp");
    dispatcher.forward(request, response);
}

// Filter students by major
private void filterStudents(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {

    String major = request.getParameter("major");

    List<Student> students = studentDAO.getStudentsByMajor(major);

    request.setAttribute("students", students);
request.setAttribute("selectedMajor", major); // giữ dropdown sau submit
request.setAttribute("sortBy", request.getParameter("sortBy")); // optional
request.setAttribute("order", request.getParameter("order"));   // optional


    RequestDispatcher dispatcher = request.getRequestDispatcher("/views/student-list.jsp");
    dispatcher.forward(request, response);
}

    
   

}
