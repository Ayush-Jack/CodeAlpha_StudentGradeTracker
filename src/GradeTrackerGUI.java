package src;
import javax.swing.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class GradeTrackerGUI extends JFrame {
    private static final String DATA_FILE = "student_grades.csv";
    private GradeTracker gradeTracker;
    private final Map<String, Student> studentMap;

    private final JTextField nameField;
    private final JTextField gradeField;
    private final JTextArea displayArea;
    private final JButton addButton;
    private final JButton reportButton;
    private final JButton removeButton;
    private final JButton clearButton;

    public GradeTrackerGUI() {
        this.gradeTracker = loadGradeTracker();
        this.studentMap = new HashMap<>();
        populateStudentMap();

        // Frame setup
        setTitle("Student Grade Tracker");
        setSize(650, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // --- Control Panel Setup ---
        JPanel controlPanel = new JPanel(new BorderLayout(10, 10));
        controlPanel.setBorder(BorderFactory.createTitledBorder("Manage Grades"));

        // Input Fields Panel
        JPanel fieldsPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        fieldsPanel.add(new JLabel("Student Name:"));
        nameField = new JTextField();
        fieldsPanel.add(nameField);
        fieldsPanel.add(new JLabel("Grade (0-100):"));
        gradeField = new JTextField();
        fieldsPanel.add(gradeField);

        controlPanel.add(fieldsPanel, BorderLayout.NORTH);

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        addButton = new JButton("Add Grade");
        removeButton = new JButton("Remove Student");
        reportButton = new JButton("Show Summary Report");
        clearButton = new JButton("Clear Statistics");
        buttonPanel.add(addButton);
        buttonPanel.add(removeButton);
        buttonPanel.add(reportButton);
        buttonPanel.add(clearButton);

        controlPanel.add(buttonPanel, BorderLayout.CENTER);

        // Display Area
        displayArea = new JTextArea();
        displayArea.setEditable(false);
        displayArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(displayArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Summary Report"));

        // Add components to frame
        add(controlPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        // Add Action Listeners
        addButton.addActionListener(new AddGradeListener());
        removeButton.addActionListener(new RemoveStudentListener());
        reportButton.addActionListener(_ -> displayArea.setText(gradeTracker.getSummaryReportAsString()));
        clearButton.addActionListener(_ -> updateStudentListView());

        // Add Window Listener for saving data on close
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                saveGradeTracker();
            }
        });

        // Make frame visible
        setLocationRelativeTo(null);
        setVisible(true);
        updateStudentListView();
    }

    private void updateStudentListView() {
        displayArea.setText(gradeTracker.getStudentListAsString());
    }

    private void populateStudentMap() {
        for (Student student : gradeTracker.getStudents()) {
            studentMap.put(student.getName(), student);
        }
    }

    private void saveGradeTracker() {
        File dataFile = new File(DATA_FILE);
        try (PrintWriter writer = new PrintWriter(new FileWriter(dataFile))) {
            for (Student student : gradeTracker.getStudents()) {
                StringBuilder line = new StringBuilder(student.getName());
                for (Integer grade : student.getGrades()) {
                    line.append(",").append(grade);
                }
                writer.println(line);
            }
            System.out.println("Data saved successfully to: " + dataFile.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("Error saving data: " + e.getMessage());
            JOptionPane.showMessageDialog(this, "Could not save student data to CSV.", "Save Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private GradeTracker loadGradeTracker() {
        GradeTracker loadedTracker = new GradeTracker();
        File dataFile = new File(DATA_FILE);
        if (dataFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(dataFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.trim().isEmpty()) continue;
                    String[] parts = line.split(",");
                    if (parts.length > 0) {
                        String name = parts[0];
                        Student student = new Student(name);
                        for (int i = 1; i < parts.length; i++) {
                            student.addGrade(Integer.parseInt(parts[i]));
                        }
                        loadedTracker.addStudent(student);
                    }
                }
                System.out.println("Data loaded successfully from " + DATA_FILE);
            } catch (IOException | NumberFormatException e) {
                System.err.println("Error loading data: " + e.getMessage());
                JOptionPane.showMessageDialog(this, "Could not load student data from CSV. A new session will be started.", "Load Error", JOptionPane.ERROR_MESSAGE);
                return new GradeTracker();
            }
        }
        return loadedTracker;
    }

    private class RemoveStudentListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String name = nameField.getText().trim();
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(GradeTrackerGUI.this, "Please enter the name of the student to remove.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int response = JOptionPane.showConfirmDialog(
                GradeTrackerGUI.this,
                "Are you sure you want to remove " + name + " and all their grades?",
                "Confirm Removal",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
            );

            if (response == JOptionPane.YES_OPTION) {
                boolean removed = gradeTracker.removeStudent(name);
                if (removed) {
                    studentMap.remove(name);
                    updateStudentListView();
                    nameField.setText("");
                    gradeField.setText("");
                    JOptionPane.showMessageDialog(GradeTrackerGUI.this, "Student " + name + " has been removed.", "Success", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(GradeTrackerGUI.this, "Student " + name + " not found.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    private class AddGradeListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String name = nameField.getText().trim();
            String gradeText = gradeField.getText().trim();

            if (name.isEmpty() || gradeText.isEmpty()) {
                JOptionPane.showMessageDialog(GradeTrackerGUI.this, "Name and grade cannot be empty.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                int grade = Integer.parseInt(gradeText);
                if (grade < 0 || grade > 100) {
                    JOptionPane.showMessageDialog(GradeTrackerGUI.this, "Grade must be between 0 and 100.", "Input Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Get or create student
                Student student = studentMap.get(name);
                if (student == null) {
                    student = new Student(name);
                    studentMap.put(name, student);
                    gradeTracker.addStudent(student);
                }

                // Add grade and provide feedback
                student.addGrade(grade);
                JOptionPane.showMessageDialog(GradeTrackerGUI.this, "Grade added for " + name, "Success", JOptionPane.INFORMATION_MESSAGE);

                // Clear fields for next entry
                nameField.setText("");
                gradeField.setText("");
                nameField.requestFocus();
                updateStudentListView();

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(GradeTrackerGUI.this, "Please enter a valid number for the grade.", "Input Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}