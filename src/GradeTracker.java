package src;
import java.util.ArrayList;
import java.util.List;

public class GradeTracker {
    private List<Student> students;

    public GradeTracker() {
        this.students = new ArrayList<>();
    }

    public void addStudent(Student student) {
        this.students.add(student);
    }

    public boolean removeStudent(String studentName) {
        return this.students.removeIf(student -> student.getName().equalsIgnoreCase(studentName));
    }

    public List<Student> getStudents() {
        return students;
    }

    public double calculateOverallAverage() {
        if (students.isEmpty()) {
            return 0.0;
        }
        return students.stream().filter(s -> !s.getGrades().isEmpty()).mapToDouble(Student::getAverageGrade).average().orElse(0.0);
    }

    public int getHighestGrade() {
        return students.stream().flatMap(student -> student.getGrades().stream()).mapToInt(Integer::intValue).max().orElse(0);
    }

    public int getLowestGrade() {
        return students.stream().flatMap(student -> student.getGrades().stream()).mapToInt(Integer::intValue).min().orElse(0);
    }

    public String getStudentListAsString() {
        if (students.isEmpty()) {
            return "No student data available.";
        }
        StringBuilder report = new StringBuilder();
        for (Student student : students) {
            report.append(String.format("Student: %-15s | Grades: %-20s | Average: %.2f\n", student.getName(), student.getGrades().toString(), student.getAverageGrade()));
        }
        return report.toString();
    }

    public String getSummaryReportAsString() {
        if (students.isEmpty()) {
            return getStudentListAsString();
        }
        StringBuilder report = new StringBuilder(getStudentListAsString());
        report.append("\n--- Overall Class Statistics ---\n");
        report.append(String.format("Overall Class Average: %.2f\n", calculateOverallAverage()));
        report.append(String.format("Highest Grade in Class: %d\n", getHighestGrade()));
        report.append(String.format("Lowest Grade in Class: %d\n", getLowestGrade()));
        report.append("------------------------------------\n");
        return report.toString();
    }
}