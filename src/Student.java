package src;
import java.util.ArrayList;
import java.util.List;

public class Student {
    private String name;
    private List<Integer> grades;

    public Student(String name) {
        this.name = name;
        this.grades = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public void addGrade(int grade) {
        grades.add(grade);
    }

    public List<Integer> getGrades() {
        return grades;
    }

    public double getAverageGrade() {
        if (grades.isEmpty()) {
            return 0.0;
        }
        return grades.stream().mapToInt(Integer::intValue).average().orElse(0.0);
    }
}