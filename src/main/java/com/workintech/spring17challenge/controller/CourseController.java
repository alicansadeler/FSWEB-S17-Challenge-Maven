package com.workintech.spring17challenge.controller;

import com.workintech.spring17challenge.entity.Course;
import com.workintech.spring17challenge.exceptions.ApiException;
import com.workintech.spring17challenge.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/courses")
public class CourseController {
    private List<Course> courses;
    private CourseGpa highCourseGpa;
    private CourseGpa mediumCourseGpa;
    private CourseGpa lowCourseGpa;

    @Autowired
    public CourseController(List<Course> courses,
                           @Qualifier("highCourseGpa") CourseGpa highCourseGpa,
                            @Qualifier("mediumCourseGpa") CourseGpa mediumCourseGpa,
                            @Qualifier("lowCourseGpa") CourseGpa lowCourseGpa) {
        this.highCourseGpa = highCourseGpa;
        this.mediumCourseGpa = mediumCourseGpa;
        this.lowCourseGpa = lowCourseGpa;
        this.courses = new ArrayList<>();
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<Course> getCourses() {
        if (!courses.isEmpty()) {
            return courses.stream().toList();
        } else {
            return null;
        }
    }

    @GetMapping("/{name}")
    @ResponseStatus(HttpStatus.OK)
    public Course getCourse(@PathVariable String name) {
        Optional<Course> courseOpt = courses.stream()
                .filter(course -> course.getName().equalsIgnoreCase(name))
                .findFirst();
        if (courseOpt.isPresent()) {
            return courseOpt.get();
        } else {
            throw new ApiException("Name değeri bulunamdı", HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> postCourse(@RequestBody Course course) {

        if (course.getName() == null || course.getGrade() == null) {
            throw new ApiException("Course objesi null değer içeremez.", HttpStatus.BAD_REQUEST);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("course", course);
        response.put("totalGpa", credit(course));
        courses.add(course);
        return response;
    }

    private int credit(Course course) {
        int credit = course.getCredit();
        int totalGpa = 0;

        if (credit <= 2) {
            totalGpa = course.getGrade().getCoefficient() * credit * lowCourseGpa.getGpa();
        } else if (credit == 3) {
            totalGpa = course.getGrade().getCoefficient() * credit * mediumCourseGpa.getGpa();
        } else if (credit == 4) {
            totalGpa = course.getGrade().getCoefficient() * credit * highCourseGpa.getGpa();
        } else {
            throw new ApiException("Geçersiz credit değeri", HttpStatus.BAD_REQUEST);
        }

        return totalGpa;
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Map<String, Object> updateCourse(@PathVariable int id, @RequestBody Course updateCourse) {
      Optional<Course> oldCourseOpt = courses.stream().filter(course -> course.getId() == id).findFirst();

      if (oldCourseOpt.isPresent()) {
          Course oldCourse = oldCourseOpt.get();
          oldCourse.setName(updateCourse.getName());
          oldCourse.setGrade(updateCourse.getGrade());
          oldCourse.setCredit(updateCourse.getCredit());

          Map<String, Object> response = new HashMap<>();
          response.put("course", updateCourse);
          response.put("totalGpa", credit(updateCourse));
          return response;
      } else {
          throw new ApiException("Course bulunamadi", HttpStatus.NOT_FOUND);
      }
    }


    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Course deleteCourse(@PathVariable int id) {
        if (id <= 0) {
            throw new ApiException("ID value cannot be negative", HttpStatus.BAD_REQUEST);
        }

        Optional<Course> courseOpt = courses.stream()
                .filter(course -> course.getId() == id)
                .findFirst();

        if (courseOpt.isPresent()) {
            Course removedCourse = courseOpt.get();
            courses.remove(removedCourse);
            return removedCourse;
        } else {
            throw new ApiException("Course not found", HttpStatus.NOT_FOUND);
        }
    }

}
