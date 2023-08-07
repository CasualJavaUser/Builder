package com.boxhead.builder.game_objects;

import com.boxhead.builder.Job;
import com.boxhead.builder.Jobs;
import com.boxhead.builder.utils.Vector2i;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

import static com.boxhead.builder.game_objects.Villager.Order.Type.*;

public class SchoolBuilding extends ProductionBuilding {
    private static final String notStudent = "Villager does not study here";

    private final Shift[] studentShifts;

    private final Set<Villager> students;

    public SchoolBuilding(Buildings.Type type, Vector2i gridPosition) {
        super(type, gridPosition);
        studentShifts = new Shift[SHIFTS_PER_JOB];
        students = new HashSet<>(type.studentCapacity * SHIFTS_PER_JOB);

        if (SHIFTS_PER_JOB != 3) throw new IllegalStateException();
        for (int i = 0; i < SHIFTS_PER_JOB; i++) {
            studentShifts[i] = new Shift(DEFAULT_SHIFT_TIMES[i], type.getShiftActivity(i) ? type.studentCapacity : 0);
        }
    }

    public boolean isRecruiting() {
        for (Shift shift : studentShifts) {
            if (shift.employees.size() < shift.maxEmployees) return true;
        }
        return false;
    }

    public Shift getStudentShift(Villager student) {
        for (Shift shift : studentShifts) {
            if (shift.employees.contains(student))
                return shift;
        }
        return null;
    }

    public void addStudent(Villager student) {
        Shift bestShift = Arrays.stream(studentShifts)
                .filter(shift -> shift.employees.size() < shift.maxEmployees)
                .min(Comparator.comparingDouble(shift -> (double) shift.employees.size() / (double) shift.maxEmployees))
                .orElseThrow();

        bestShift.employees.add(student);
        students.add(student);
    }

    public void removeStudent(Villager student) {
        for (Shift shift : studentShifts) {
            if (shift.employees.remove(student)) {
                students.remove(student);
                student.quitSchool();
                return;
            }
        }
        throw new IllegalArgumentException(notStudent);
    }

    @Override
    public void startShift(Job.ShiftTime shiftTime) {
        super.startShift(shiftTime);
        for (Shift shift : studentShifts) {
            if (shift.shiftTime == shiftTime) {
                for (Villager student : shift.employees) {
                    student.giveOrder(EXIT);
                    student.giveOrder(GO_TO, this);
                    student.giveOrder(CLOCK_IN);
                }
            }
        }
    }

    @Override
    public void endShift(Job.ShiftTime shiftTime) {
        super.endShift(shiftTime);
        for (Shift shift : studentShifts) {
            if (shift.shiftTime == shiftTime) {
                for (Villager student : shift.employees) {
                    if (student.isClockedIn()) {
                        student.giveOrder(EXIT, this);
                        student.giveOrder(CLOCK_OUT);
                        if (student.getHome() != null) {
                            student.giveOrder(GO_TO, student.getHome());
                        }
                    }
                }
                break;
            }
        }
    }

    public void endStudentShift(Villager student) {
        for (Shift shift : studentShifts) {
            if (shift.employees.contains(student)) {
                student.giveOrder(EXIT, this);
                student.giveOrder(CLOCK_OUT);
                break;
            }
        }
    }

    public void teach() {
        for (Shift shift : studentShifts) {
            for (Villager student : shift.employees) {
                if (student.isClockedIn()) {
                    Jobs.STUDENT.continuousTask(student, this);
                    if (student.getEducation() >= 1f) {
                        endStudentShift(student);
                        removeStudent(student);
                    }
                }
            }
        }
    }

    public int getNumberOfStudents() {
        return students.size();
    }

    public int getOverallStudentCapacity() {
        int capacity = 0;
        for (int i = 0; i < SHIFTS_PER_JOB; i++) {
            if (type.getShiftActivity(i))
                capacity += type.studentCapacity;
        }
        return capacity;
    }
}
