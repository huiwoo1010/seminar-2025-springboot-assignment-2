CREATE TABLE IF NOT EXISTS timetables(
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    name VARCHAR(64) NOT NULL,
    year INT NOT NULL,
    semester VARCHAR(16) NOT NULL,

    UNIQUE KEY unique_timetable(user_id, year, semester, name)
);

CREATE TABLE IF NOT EXISTS timetable_courses(
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    timetable_id BIGINT NOT NULL,
    course_id BIGINT NOT NULL,

    UNIQUE KEY unique_timetable_course(timetable_id, course_id),
    FOREIGN KEY (timetable_id) REFERENCES timetables(id) ON DELETE CASCADE,
    FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE
);