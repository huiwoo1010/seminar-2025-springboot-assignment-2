CREATE TABLE IF NOT EXISTS courses (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  year INT NOT NULL,
  term VARCHAR(10) NOT NULL,
  category VARCHAR(255),
  college VARCHAR(255),
  department VARCHAR(255),
  program VARCHAR(255),
  grade INT,
  course_code VARCHAR(50) NOT NULL,
  class_code VARCHAR(50) NOT NULL,
  title VARCHAR(1000) NOT NULL,
  credit INT NOT NULL,
  professor VARCHAR(255),
  room VARCHAR(255),
  UNIQUE KEY ux_courses_yttcc (year, term, course_code, class_code)
);

CREATE TABLE IF NOT EXISTS course_time_slots (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  course_id BIGINT NOT NULL,
  day VARCHAR(16) NOT NULL,
  start_min INT NOT NULL,
  end_min INT NOT NULL,
  place VARCHAR(255),
  CONSTRAINT fk_slots_course
    FOREIGN KEY (course_id) REFERENCES courses(id)
    ON DELETE CASCADE
);

CREATE INDEX ix_slots_course ON course_time_slots(course_id);
CREATE INDEX ix_slots_day_start_end ON course_time_slots(day, start_min, end_min);
